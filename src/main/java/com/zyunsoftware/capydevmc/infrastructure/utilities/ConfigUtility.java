package com.zyunsoftware.capydevmc.infrastructure.utilities;

import java.util.Set;

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

  public static String getString(String key, String def) {
    String result = _getConfig().getString(key, def);

    return result;
  }

  public static int getInt(String key) {
    int result = _getConfig().getInt(key, 0);

    return result;
  }

  public static Set<String> getKeys(String key) {
    return _getConfig()
      .getConfigurationSection(key)
      .getKeys(false);
  }
}
