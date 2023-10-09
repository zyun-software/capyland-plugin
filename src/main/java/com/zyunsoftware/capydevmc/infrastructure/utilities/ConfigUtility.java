package com.zyunsoftware.capydevmc.infrastructure.utilities;

import org.bukkit.configuration.file.FileConfiguration;

import com.zyunsoftware.capydevmc.app.CapylandPlugin;

public class ConfigUtility {
  private static FileConfiguration _getConfig() {
    return CapylandPlugin.getInstance().getConfig();
  }

  public static String getMysqlUrl() {
    return _getConfig().getString("mysql-url", "jdbc:mysql://localhost:3306/db");
  }

  public static String getMysqlUser() {
    return _getConfig().getString("mysql-user", "root");
  }

  public static String getMysqlPassword() {
    return _getConfig().getString("mysql-password", "secret");
  }
}
