package com.sogou.pluto.dao;

import com.sogou.pluto.Config;
import com.sogou.pluto.common.CommonUtils;
import com.sogou.pluto.db.JDBCUtils;
import com.sogou.pluto.model.Gpu;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Tao Li on 05/05/2017.
 */
public class GpuDao {
  private final static String TABLE_NAME = "gpus";

  public void updateGpuState(String state, String node, String groupId) throws SQLException {
    JDBCUtils.execute(String.format(
        "UPDATE %s SET state=%s WHERE node=%s AND gpuId=%s",
        TABLE_NAME,
        CommonUtils.formatSQLValue(state),
        CommonUtils.formatSQLValue(node),
        CommonUtils.formatSQLValue(groupId)));
  }

  public void updateGpusState(String state, String node) throws SQLException {
    JDBCUtils.execute(String.format(
        "UPDATE %s SET state=%s WHERE node=%s",
        TABLE_NAME, CommonUtils.formatSQLValue(state), CommonUtils.formatSQLValue(node)));
  }

  private List<Gpu> getGpus(String whereClause) throws SQLException {
    String sql = String.format("SELECT * FROM %s %s", TABLE_NAME, whereClause);
    Connection conn = Config.POOL.getConnection();
    try {
      try (PreparedStatement stmt = conn.prepareStatement(sql)) {
        try (ResultSet rs = stmt.executeQuery()) {
          List<Gpu> gpus = new ArrayList<>();
          while (rs.next()) {
            gpus.add(new Gpu(
                rs.getLong("id"),
                rs.getString("node"),
                rs.getString("gpuId"),
                rs.getString("state")));
          }
          return gpus;
        }
      }
    } finally {
      Config.POOL.releaseConnection(conn);
    }
  }

  public List<Gpu> getGpusByStateAndNode(String state, String node) throws SQLException {
    return getGpus(String.format("WHERE state='%s' And node='%s' ORDER BY id ASC", state, node));
  }
}
