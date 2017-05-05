package com.sogou.pluto.dao;

import com.sogou.pluto.Config;
import com.sogou.pluto.common.CommonUtils;
import com.sogou.pluto.db.JDBCUtils;
import com.sogou.pluto.model.Job;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.LongStream;

/**
 * Created by Tao Li on 2016/6/1.
 */
public class JobDao {
  private final static String TABLE_NAME = "jobs";

  public void createJob(Job job) throws SQLException {
    JDBCUtils.execute(String.format(
        "INSERT INTO %s (name, userId, baseImage, tarLocation, startScript, state, startTime)" +
            "VALUES(%s, %s, %s, %s, %s, %s, %s)",
        TABLE_NAME,
        CommonUtils.formatSQLValue(job.getName()),
        CommonUtils.formatSQLValue(job.getUserId()),
        CommonUtils.formatSQLValue(job.getBaseImage()),
        CommonUtils.formatSQLValue(job.getTarLocation()),
        CommonUtils.formatSQLValue(job.getStartScript()),
        CommonUtils.formatSQLValue(Job.STATE_WAIT),
        CommonUtils.formatSQLValue(CommonUtils.now())));
  }

  private void updateJob(Job job, String whereClause) throws SQLException {
    JDBCUtils.execute(String.format(
        "UPDATE %s SET name=%s, userId=%s, baseImage=%s, tarLocation=%s, startScript=%s, " +
            "state=%s, startTime=%s, endTime=%s, host=%s %s",
        TABLE_NAME,
        CommonUtils.formatSQLValue(job.getName()),
        CommonUtils.formatSQLValue(job.getUserId()),
        CommonUtils.formatSQLValue(job.getBaseImage()),
        CommonUtils.formatSQLValue(job.getTarLocation()),
        CommonUtils.formatSQLValue(job.getStartScript()),
        CommonUtils.formatSQLValue(job.getState()),
        CommonUtils.formatSQLValue(job.getStartTime()),
        CommonUtils.formatSQLValue(job.getEndTime()),
        CommonUtils.formatSQLValue(job.getHost()),
        whereClause));
  }

  public void updateJobById(Job job, long id) throws SQLException {
    updateJob(job, String.format("WHERE id=%s", id));
  }

  public void updateJobByIdAndStateAndHost(Job job, long id, String state, String host)
      throws SQLException {
    updateJob(job, String.format("WHERE id=%s AND state='%s' AND host='%s'", id, state, host));
  }

  public void updateJobsStateAndHostByIds(String state, String host, long[] ids)
      throws SQLException {
    JDBCUtils.execute(String.format(
        "UPDATE %s SET state=%s, host=%s WHERE id in (%s)",
        TABLE_NAME,
        CommonUtils.formatSQLValue(state),
        CommonUtils.formatSQLValue(host),
        LongStream.of(ids).mapToObj(id -> String.valueOf(id)).collect(Collectors.joining(", "))));
  }

  private List<Job> getJobs(String whereClause) throws SQLException {
    String sql = String.format("SELECT * FROM %s %s", TABLE_NAME, whereClause);
    Connection conn = Config.POOL.getConnection();
    try {
      try (PreparedStatement stmt = conn.prepareStatement(sql)) {
        try (ResultSet rs = stmt.executeQuery()) {
          List<Job> jobs = new ArrayList<>();
          while (rs.next()) {
            jobs.add(new Job(
                rs.getLong("id"),
                rs.getString("name"),
                rs.getString("userId"),
                rs.getString("baseImage"),
                rs.getString("tarLocation"),
                rs.getString("startScript"),
                rs.getString("state"),
                rs.getString("startTime"),
                rs.getString("endTime"),
                rs.getString("host"),
                rs.getString("gpuId")));
          }
          return jobs;
        }
      }
    } finally {
      Config.POOL.releaseConnection(conn);
    }
  }

  private Job getJob(String whereClause) throws SQLException {
    List<Job> jobs = getJobs(whereClause);
    return jobs.size() == 0 ? null : jobs.get(0);
  }

  public List<Job> getJobsByState(String state, int limit) throws SQLException {
    return getJobs(String.format("WHERE state='%s' ORDER BY id ASC LIMIT %s", state, limit));
  }

  public List<Job> getJobsByStateAndHost(String state, String host) throws SQLException {
    return getJobs(String.format("WHERE state='%s' And host='%s' ORDER BY id ASC", state, host));
  }

  public Job getJobById(long id) throws SQLException {
    return getJob(String.format("WHERE id=%s ORDER BY id ASC", id));
  }

  public List<Job> getJobsByUserId(String userId, int start, int length) throws SQLException {
    return getJobs(String.format(
        "WHERE userId='%s' ORDER BY id DESC LIMIT %s, %s", userId, start, length));
  }

  public List<Job> getJobs(int start, int length) throws SQLException {
    return getJobs(String.format("ORDER BY id DESC LIMIT %s, %s", start, length));
  }
}
