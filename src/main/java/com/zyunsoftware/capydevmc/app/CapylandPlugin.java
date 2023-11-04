package com.zyunsoftware.capydevmc.app;

import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

import com.zyunsoftware.capydevmc.app.actions.commands.ApiCommand;
import com.zyunsoftware.capydevmc.app.actions.commands.ReloadConfigCommand;
import com.zyunsoftware.capydevmc.app.actions.listeners.ChatListener;

public class CapylandPlugin extends JavaPlugin {
  private static CapylandPlugin _instancePlugin;

  public CapylandPlugin() {
    saveDefaultConfig();
    _instancePlugin = this;
  }

  @Override
  public void onEnable() {
    PluginManager pluginManager = getServer().getPluginManager();

    pluginManager.registerEvents(new ChatListener(), this);

    getCommand("перезавантажити_конфігураційний_файл").setExecutor(new ReloadConfigCommand());
    getCommand("api").setExecutor(new ApiCommand());
  }

  public static CapylandPlugin getInstance() {
    return _instancePlugin;
  }
}
