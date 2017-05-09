package com.sogou.pluto.core.execution;

import com.sogou.pluto.Config;
import com.sogou.pluto.common.CommonUtils;
import com.sogou.pluto.model.Gpu;
import com.sogou.pluto.model.Job;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

/**
 * Created by Tao Li on 2016/6/2.
 */
public class JobExecuteController implements Runnable {
  private final Logger LOG = LoggerFactory.getLogger(JobExecuteController.class);

  private volatile boolean isRunning = false;
  private final static long CHECK_INTERVAL = 3;
  private final static int CHECK_JOB_BATCH = 20;
  private final static long PREEMPT_INTERVAL = 1;

  private int jobQueueSize;
  private int workerNum;
  private BlockingQueue<Job> jobQueue;
  private ExecutorService workerPool;
  private final String host;

  public JobExecuteController(int jobQueueSize, int workerNum, String host) {
    this.jobQueueSize = jobQueueSize;
    this.workerNum = workerNum;
    this.host = host;
    jobQueue = new ArrayBlockingQueue<>(jobQueueSize, true);
    workerPool = Executors.newFixedThreadPool(workerNum);
  }

  private class Worker implements Runnable {
    private void runJob(Job job) {
      String state = Job.STATE_FAIL;

      Executor executor = new Executor(job);
      try {
        if (executor.exec()) {
          state = Job.STATE_SUCC;
        }
      } catch (IOException e) {
        LOG.error("Fail to exec job " + job.getId(), e);
      } finally {
        releaseGpu(Stream.of(job.getGpuIds().split(","))
            .mapToInt(gpuId -> Integer.parseInt(gpuId)).toArray());
      }

      job.setState(state);
      job.setEndTime(CommonUtils.now());
      try {
        Config.JOB_DAO.updateJobById(job, job.getId());
      } catch (SQLException e) {
        LOG.error("Fail to update job state: " + job.getId());
      }
    }

    @Override
    public void run() {
      while (isRunning && !Thread.currentThread().isInterrupted()) {
        try {
          Job job = jobQueue.take();
          runJob(job);
        } catch (InterruptedException e) {
          LOG.warn("interrupted", e);
          Thread.currentThread().interrupt();
        }
      }
    }
  }

  @Override
  public void run() {
    isRunning = true;

    try {
      onlineGpus();
    } catch (SQLException e) {
      LOG.error("Fail to online gpus", e);
      return;
    }

    IntStream.iterate(0, n -> n + 1).limit(workerNum)
        .forEach(i -> workerPool.submit(new Worker()));

    try {
      cleanZombieJobs();
    } catch (SQLException e) {
      LOG.error("Fail to clean zombie jobs", e);
    }

    while (isRunning && !Thread.currentThread().isInterrupted()) {
      List<Job> jobs = null;
      try {
        jobs = Config.JOB_DAO.getJobsByState(Job.STATE_WAIT, CHECK_JOB_BATCH);
      } catch (SQLException e) {
        LOG.error("Fail to get WAIT jobs", e);
      }

      if (jobs != null) {
        jobs.stream().forEach(job -> {
          if (jobQueue.size() < jobQueueSize) {
            List<Gpu> gpus = acquireAvailableGpus(job.getGpuNum());
            if (gpus.size() == job.getGpuNum()) {
              if (preemptJob(job, gpus)) {
                LOG.info(String.format("preempt jobId: %s, gpuIds: %s", job.getId(), gpus));
                jobQueue.add(job);
              } else {
                releaseGpu(gpus.stream().mapToInt(gpu -> gpu.getGpuId()).toArray());
              }
            }
          }
        });
      }

      try {
        TimeUnit.SECONDS.sleep(CHECK_INTERVAL);
      } catch (InterruptedException e) {
        LOG.warn("interrupted", e);
        Thread.currentThread().interrupt();
      }
    }
  }

  private void cleanZombieJobs() throws SQLException {
    List<Job> lockJobs = Config.JOB_DAO.getJobsByStateAndHost(Job.STATE_LOCK, host);
    if (lockJobs.size() > 0) {
      Config.JOB_DAO.updateJobsStateAndHostByIds(
          Job.STATE_WAIT, null, lockJobs.stream().mapToLong(job -> job.getId()).toArray());
    }
    List<Job> runJobs = Config.JOB_DAO.getJobsByStateAndHost(Job.STATE_RUN, host);
    if (runJobs.size() > 0) {
      Config.JOB_DAO.updateJobsStateAndHostByIds(
          Job.STATE_FAIL, host, runJobs.stream().mapToLong(job -> job.getId()).toArray());
    }
  }

  private boolean preemptJob(Job job, List<Gpu> gpus) {
    try {
      boolean needToRollback = false;

      job.setState(Job.STATE_LOCK);
      job.setHost(host);
      job.setGpuIds(gpus.stream().map(gpu -> String.valueOf(gpu.getGpuId()))
          .collect(Collectors.joining(",")));
      Config.JOB_DAO.updateJobById(job, job.getId());

      try {
        TimeUnit.SECONDS.sleep(PREEMPT_INTERVAL);
      } catch (InterruptedException e) {
        LOG.warn("interrupted", e);
        Thread.currentThread().interrupt();
        needToRollback = true;
      }

      try {
        job = Config.JOB_DAO.getJobById(job.getId());
        if (job.getState().equals(Job.STATE_LOCK) && job.getHost().equals(host)) {
          job.setState(Job.STATE_RUN);
          Config.JOB_DAO.updateJobById(job, job.getId());
          return true;
        }
      } catch (SQLException e) {
        LOG.error("Failed to preempt job: " + job.getId(), e);
        needToRollback = true;
      }

      if (needToRollback) {
        job.setState(Job.STATE_WAIT);
        job.setHost(null);
        job.setGpuIds(null);
        Config.JOB_DAO.updateJobByIdAndStateAndHost(job, job.getId(), Job.STATE_LOCK, host);
      }
    } catch (SQLException e) {
      LOG.error("Failed to preempt job: " + job.getId(), e);
    }

    return false;
  }

  private void onlineGpus() throws SQLException {
    Config.GPU_DAO.updateStateByNode(Gpu.STATE_AVAILABLE, Config.HOST);
  }

  private void offlineGpus() throws SQLException {
    Config.GPU_DAO.updateStateByNode(Gpu.STATE_OFFLINE, Config.HOST);
  }

  private List<Gpu> acquireAvailableGpus(int num) {
    List<Gpu> gpus = new ArrayList<>();

    try {
      List<Gpu> allAvailableGpus =
          Config.GPU_DAO.getGpusByStateAndNode(Gpu.STATE_AVAILABLE, Config.HOST);
      if (allAvailableGpus.size() >= num) {
        gpus.addAll(allAvailableGpus.subList(0, num));
      }
    } catch (SQLException e) {
      LOG.error("fail to get gpus", e);
    }

    if (gpus.size() == num) {
      try {
        Config.GPU_DAO.updateStateByNodeAndGpuIds(Gpu.STATE_IN_USE, Config.HOST,
            gpus.stream().mapToInt(gpu -> gpu.getGpuId()).toArray());
      } catch (SQLException e) {
        LOG.error("fail to update gpu state");
        try {
          Config.GPU_DAO.updateStateByNodeAndGpuIds(Gpu.STATE_AVAILABLE, Config.HOST,
              gpus.stream().mapToInt(gpu -> gpu.getGpuId()).toArray());
        } catch (SQLException ex) {
          // ignore
        }
      }
    }

    return gpus;
  }

  private void releaseGpu(int[] gpuIds) {
    try {
      Config.GPU_DAO.updateStateByNodeAndGpuIds(Gpu.STATE_AVAILABLE, Config.HOST, gpuIds);
    } catch (SQLException e) {
      LOG.error("fail to release gpus", e);
    }
  }

  public void shutdown() {
    isRunning = false;

    try {
      offlineGpus();
    } catch (SQLException e) {
      LOG.error("Fail to shutdown JobExecuteController", e);
    }
  }
}
