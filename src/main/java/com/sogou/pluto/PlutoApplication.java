package com.sogou.pluto;

import com.sogou.pluto.core.execution.JobExecuteController;
import com.sogou.pluto.resources.JobResources;

import io.dropwizard.Application;
import io.dropwizard.setup.Bootstrap;
import io.dropwizard.setup.Environment;

/**
 * Created by Tao Li on 03/05/2017.
 */
public class PlutoApplication extends Application<Config> {
  @Override
  public void run(Config conf, Environment environment) throws Exception {
    // init static config
    Config.initStaticConfig(conf);

    // start db connection pool
    Config.POOL.start();

    // start JobExecuteController
    JobExecuteController jobExecuteController = new JobExecuteController(
        Config.JOB_QUEUE_SIZE, Config.WORKER_NUM, Config.HOST);
    new Thread(jobExecuteController, "JobExecuteController").start();

    // register resources
    environment.jersey().register(new JobResources());

    // add shutdown hook
    Runtime.getRuntime().addShutdownHook(new Thread() {
      @Override
      public void run() {
        jobExecuteController.shutdown();
        Config.POOL.close();
      }
    });
  }

  @Override
  public String getName() {
    return "pluto";
  }

  @Override
  public void initialize(Bootstrap<Config> bootstrap) {
    // nothing to do yet
  }

  public static void main(String[] args) throws Exception {
    new PlutoApplication().run(args);
  }
}
