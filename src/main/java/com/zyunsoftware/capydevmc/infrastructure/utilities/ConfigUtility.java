package com.zyunsoftware.capydevmc.infrastructure.utilities;

import org.bukkit.configuration.file.FileConfiguration;

import com.zyunsoftware.capydevmc.app.CapylandPlugin;

public class ConfigUtility {
  private static FileConfiguration _getConfig() {
    return CapylandPlugin.getInstance().getConfig();
  }

  public static String getString(String key) {
    String result = _getConfig().getString(key, key);

    return result;
  }
}
