package com.sogou.pluto.core.execution;

import com.sogou.pluto.Config;
import com.sogou.pluto.common.CommonUtils;
import com.sogou.pluto.model.Job;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

/**
 * Created by Tao Li on 04/05/2017.
 */
public class Executor {
  private final Logger LOG = LoggerFactory.getLogger(JobExecuteController.class);

  private Job job;

  public Executor(Job job) {
    this.job = job;
  }

  public boolean exec() throws IOException {
    String initCommand = String.format("bin/ext/init.sh %s %s", job.getId(), job.getTarLocation());
    if (CommonUtils.runProcess(initCommand) != 0) {
      throw new IOException("Fail to init job " + job.getId());
    }

    String jobHomeDir = Config.DATA_DIR + "/" + job.getId();
    String jobContainerDir = jobHomeDir + "/container";
    String jobLogDir = jobHomeDir + "/logs";
    String jobStdOutLogFile = jobLogDir + "/stdout";
    String jobStdErrLogFile = jobLogDir + "/stderr";

    // TODO 1. GPU config 2. ssh port and framework ui port
    String command = String.format(
        "/usr/sbin/sshd -D & " +
            "NV_GPU=%s nvidia-docker run --net=host --rm -v %s:/search -w /search %s %s >%s 2>%s",
        job.getGpuId(), jobContainerDir, job.getBaseImage(), job.getStartScript(),
        jobStdOutLogFile, jobStdErrLogFile);
    LOG.info(String.format("Begin to execute jobId: %s, command: %s", job.getId(), command));
    int exitCode = CommonUtils.runProcess(command);
    LOG.info(String.format("Finish jobId: %s, exitCode: %s", job.getId(), exitCode));

    return exitCode == 0;
  }
}
