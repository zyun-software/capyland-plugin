package net.ziozyun.capyland;

import org.bukkit.plugin.java.JavaPlugin;

import net.ziozyun.capyland.actions.WebServerAction;
import net.ziozyun.capyland.helpers.RequestHelper;
import net.ziozyun.capyland.helpers.UserHelper;
import net.ziozyun.capyland.listeners.AuthListener;
import net.ziozyun.capyland.listeners.ChatListener;
import net.ziozyun.capyland.listeners.QRCodeAccountNumberListener;
import net.ziozyun.capyland.listeners.ToggleViewTagNicknameListener;

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

    UserHelper.plugin = this;

    UserHelper.createTeam();

    RequestHelper.from = config.getString("from");
    RequestHelper.host = config.getString("host");
    RequestHelper.token = config.getString("token");

    var authListener = new AuthListener(this);
    pluginManager.registerEvents(authListener, this);

    var qRCodeAccountNumberListener = new QRCodeAccountNumberListener();
    pluginManager.registerEvents(qRCodeAccountNumberListener, this);
  
    var toggleViewTagNicknameListener = new ToggleViewTagNicknameListener(this);
    pluginManager.registerEvents(toggleViewTagNicknameListener, this);

    var chatListener = new ChatListener(this);
    pluginManager.registerEvents(chatListener, this);

    this._webServerAction.startServer();
  }

  @Override
  public void onDisable() {
    this._webServerAction.stopServer();
    UserHelper.dropTeam();
  }
}
