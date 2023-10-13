package com.zyunsoftware.capydevmc.infrastructure;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

import com.zyunsoftware.capydevmc.infrastructure.utilities.ConfigUtility;

public class Api {
  public static Connection getMysqlConnection() throws SQLException {
    Connection connection = DriverManager.getConnection(
      ConfigUtility.getString("mysql.url"),
      ConfigUtility.getString("mysql.user"),
      ConfigUtility.getString("mysql.password")
    );

    return connection;
  }
 }
