package com.sogou.pluto.core.execution;

import com.sogou.pluto.Config;
import com.sogou.pluto.common.CommonUtils;
import com.sogou.pluto.model.Job;

import java.io.IOException;

/**
 * Created by Tao Li on 04/05/2017.
 */
public class Executor {
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
        "NV_GPU=4 nvidia-docker run -v %s:/search -w /search %s %s >%s 2>%s",
        jobContainerDir, job.getBaseImage(), job.getStartScript(),
        jobStdOutLogFile, jobStdErrLogFile);
    System.out.println(command);
    return CommonUtils.runProcess(command) == 0;
  }
}
