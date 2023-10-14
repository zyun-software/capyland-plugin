package com.zyunsoftware.capydevmc.app;

import java.util.logging.Logger;

import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

import com.zyunsoftware.capydevmc.app.actions.commands.ChangePasswordCommand;
import com.zyunsoftware.capydevmc.app.actions.commands.LobbyCommand;
import com.zyunsoftware.capydevmc.app.actions.commands.LoginCommand;
import com.zyunsoftware.capydevmc.app.actions.commands.LogoutCommand;
import com.zyunsoftware.capydevmc.app.actions.commands.RegisterCommand;
import com.zyunsoftware.capydevmc.app.actions.listeners.AuthorizationListener;
import com.zyunsoftware.capydevmc.app.actions.listeners.ChatCommandsOpListener;
import com.zyunsoftware.capydevmc.app.actions.listeners.ServerCommandOverwritingListener;

public class CapylandPlugin extends JavaPlugin {
  private static CapylandPlugin _instancePlugin;

  private Logger _logger;

  public CapylandPlugin() {
    saveDefaultConfig();
    _instancePlugin = this;
    _logger = getLogger();
  }

  @Override
  public void onEnable() {
    DependencyInjection.getMigrationRepository().migrate();

    PluginManager pluginManager = getServer().getPluginManager();

    pluginManager.registerEvents(new AuthorizationListener(), this);
    pluginManager.registerEvents(new ChatCommandsOpListener(), this);
    pluginManager.registerEvents(new ServerCommandOverwritingListener(), this);

    getCommand("register").setExecutor(new RegisterCommand());
    getCommand("login").setExecutor(new LoginCommand());
    getCommand("вихід").setExecutor(new LogoutCommand());
    getCommand("до_лобі").setExecutor(new LobbyCommand());
    getCommand("змінити_пароль").setExecutor(new ChangePasswordCommand());

    new BukkitRunnable() {
      @Override
      public void run() {
        DependencyInjection.getAuthorizationService().controlAll();
      }
    }.runTaskTimer(this, 0, 20);
  }

  @Override
  public void onDisable() {
    _logger.info("Плагін вимкнено.");
  }

  public static CapylandPlugin getInstance() {
    return _instancePlugin;
  }
}
