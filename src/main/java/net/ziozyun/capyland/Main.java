package net.ziozyun.capyland;

import org.bukkit.plugin.java.JavaPlugin;

import net.ziozyun.capyland.actions.UpdatingTheWhiteListAction;
import net.ziozyun.capyland.helpers.RequestHelper;
import net.ziozyun.capyland.listeners.OnInteractMapListener;

public final class Main extends JavaPlugin {
  @Override
  public void onEnable() {
    var config = getConfig();
    
    RequestHelper.host = config.getString("host");
    RequestHelper.token = config.getString("token");

    var server = getServer();
    var pluginManager = server.getPluginManager();

    var updatingTheWhiteList = new UpdatingTheWhiteListAction(this);
    var onInteractMapListener = new OnInteractMapListener();

    updatingTheWhiteList.execute();
    pluginManager.registerEvents(onInteractMapListener, this);
  }

  @Override
  public void onDisable() {
    // 
  }
}
