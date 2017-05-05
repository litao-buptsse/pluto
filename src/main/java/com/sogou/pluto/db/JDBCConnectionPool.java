package com.sogou.pluto.db;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

/**
 * Created by Tao Li on 4/11/16.
 */
public class JDBCConnectionPool extends ConnectionPool<Connection> {
  private String url;
  private Properties info;

  public JDBCConnectionPool(String driver, String url) throws SQLException {
    this(driver, url, null);
  }

  public JDBCConnectionPool(String driver, String url, Properties info) throws SQLException {
    try {
      Class.forName(driver);
    } catch (ClassNotFoundException e) {
      throw new SQLException(e);
    }
    this.url = url;
    this.info = info;
  }

  @Override
  protected Connection createConnection() throws SQLException {
    if (info == null) {
      return DriverManager.getConnection(url);
    } else {
      return DriverManager.getConnection(url, info);
    }
  }

  @Override
  protected void closeConnection(Connection conn) throws SQLException {
    conn.close();
  }
}