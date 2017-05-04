package com.sogou.pluto;

import com.sogou.pluto.common.CommonUtils;
import com.sogou.pluto.dao.JobDao;
import com.sogou.pluto.db.ConnectionPoolException;
import com.sogou.pluto.db.JDBCConnectionPool;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.collect.ImmutableMap;
import io.dropwizard.Configuration;

import java.util.Collections;
import java.util.Map;
import java.util.Properties;

/**
 * Created by Tao Li on 5/31/16.
 */
public class Config extends Configuration {
  private Map<String, String> plutoConf = Collections.emptyMap();
  private Map<String, String> plutoDbConf = Collections.emptyMap();

  @JsonProperty("pluto")
  public Map<String, String> getPlutoConf() {
    return plutoConf;
  }

  @JsonProperty("pluto")
  public void setPlutoConf(Map<String, String> conf) {
    plutoConf = buildConfiguration(conf);
  }

  @JsonProperty("plutoDb")
  public Map<String, String> getPlutoDbConf() {
    return plutoDbConf;
  }

  @JsonProperty("plutoDb")
  public void setPlutoDbConf(Map<String, String> conf) {
    plutoDbConf = buildConfiguration(conf);
  }

  private Map<String, String> buildConfiguration(Map<String, String> conf) {
    final ImmutableMap.Builder<String, String> builder = ImmutableMap.builder();
    for (Map.Entry<String, String> entry : conf.entrySet()) {
      builder.put(entry.getKey(), entry.getValue());
    }
    return builder.build();
  }

  public static String DATA_DIR;
  public static int JOB_QUEUE_SIZE;
  public static int WORKER_NUM;
  public static String HOST;

  public static JDBCConnectionPool POOL;
  public static JobDao JOB_DAO;

  public static void initStaticConfig(Config conf) throws ConnectionPoolException {
    Map<String, String> beaverConf = conf.getPlutoConf();
    Map<String, String> beaverDBConf = conf.getPlutoDbConf();

    DATA_DIR = beaverConf.getOrDefault("dataDir", "data");
    JOB_QUEUE_SIZE = Integer.parseInt(beaverConf.getOrDefault("jobQueueSize", "20"));
    WORKER_NUM = Integer.parseInt(beaverConf.getOrDefault("workerNum", "10"));
    HOST = beaverConf.getOrDefault("host", CommonUtils.ip());

    // init db connection pool
    POOL = constructJDBCConnectionPool(beaverDBConf);

    // init dao object
    JOB_DAO = new JobDao();
  }

  private static JDBCConnectionPool constructJDBCConnectionPool(Map<String, String> conf)
      throws ConnectionPoolException {
    return constructJDBCConnectionPool(conf, new Properties());
  }

  private static JDBCConnectionPool constructJDBCConnectionPool(
      Map<String, String> conf, Properties info) throws ConnectionPoolException {
    String driver = conf.get("driverClass");
    String url = conf.get("url");
    JDBCConnectionPool pool = new JDBCConnectionPool(driver, url, info);

    if (conf.containsKey("initConnectionNum")) {
      pool.setInitConnectionNum(Integer.parseInt(conf.get("initConnectionNum")));
    }
    if (conf.containsKey("minConnectionNum")) {
      pool.setMinConnectionNum(Integer.parseInt(conf.get("minConnectionNum")));
    }
    if (conf.containsKey("maxConnectionNum")) {
      pool.setMaxConnectionNum(Integer.parseInt(conf.get("maxConnectionNum")));
    }
    if (conf.containsKey("idleTimeout")) {
      pool.setIdleTimeout(Long.parseLong(conf.get("idleTimeout")));
    }
    if (conf.containsKey("idleQueueSize")) {
      pool.setIdleQueueSize(Integer.parseInt(conf.get("idleQueueSize")));
    }
    if (conf.containsKey("idleConnectionCloseThreadPoolSize")) {
      pool.setIdleConnectionCloseThreadPoolSize(
          Integer.parseInt(conf.get("idleConnectionCloseThreadPoolSize")));
    }

    return pool;
  }
}
