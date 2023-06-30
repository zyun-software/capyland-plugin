package net.ziozyun.capyland;

import org.bukkit.plugin.java.JavaPlugin;

import net.ziozyun.capyland.actions.WebServerAction;
import net.ziozyun.capyland.helpers.CitizenHelper;
import net.ziozyun.capyland.helpers.RequestHelper;
import net.ziozyun.capyland.listeners.OnPlayerInteractListener;

public final class Main extends JavaPlugin {
  private WebServerAction _webServerAction;

  public Main() {
    _webServerAction = new WebServerAction(this);
  }

  @Override
  public void onEnable() {
    var config = getConfig();

    var server = getServer();
    var pluginManager = server.getPluginManager();
    var logger = server.getLogger();

    RequestHelper.host = config.getString("host");
    RequestHelper.token = config.getString("token");

    CitizenHelper.plugin = this;

    try {
      var serverNicknames = RequestHelper.whitelist();
      CitizenHelper.updateCitizenship(serverNicknames);
    } catch (Exception exception) {
      logger.severe("Не вдалося отримати білий список від сервера" + exception.getMessage());
    }

    var onPlayerInteractListener = new OnPlayerInteractListener();
    pluginManager.registerEvents(onPlayerInteractListener, this);

    this._webServerAction.startServer();
  }

  @Override
  public void onDisable() {
    this._webServerAction.stopServer();
  }
}
