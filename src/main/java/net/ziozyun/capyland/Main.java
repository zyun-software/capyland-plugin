package net.ziozyun.capyland;

import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

import java.lang.Math;

import net.ziozyun.capyland.actions.WebServerAction;
import net.ziozyun.capyland.helpers.RequestHelper;
import net.ziozyun.capyland.helpers.UserHelper;
import net.ziozyun.capyland.listeners.AuthListener;
import net.ziozyun.capyland.listeners.ChatListener;
import net.ziozyun.capyland.listeners.QRCodeAccountNumberListener;
import net.ziozyun.capyland.listeners.ToggleViewTagNicknameListener;

public final class Main extends JavaPlugin {
  private WebServerAction _webServerAction;

  @Override
  public void onEnable() {
    var config = getConfig();

    var server = getServer();
    var pluginManager = server.getPluginManager();

    UserHelper.plugin = this;

    UserHelper.createTeam();

    var radius = config.getDouble("radius", 30.0);
    var radiusSquared = Math.pow(radius, 2);

    RequestHelper.from = config.getString("from");
    RequestHelper.host = config.getString("host");
    RequestHelper.token = config.getString("token");

    var isTest = RequestHelper.from.equalsIgnoreCase("test");

    for (var onlinePlayer : Bukkit.getOnlinePlayers()) {
      var nickname = onlinePlayer.getName();
      UserHelper.addToTeam(nickname);
      if (!UserHelper.exists(onlinePlayer)) {
        try {
          RequestHelper.sendAuthorizeRequest(nickname);
        } catch (Exception e) {}
      }      
    }

    var authListener = new AuthListener(this, isTest);
    pluginManager.registerEvents(authListener, this);

    var qRCodeAccountNumberListener = new QRCodeAccountNumberListener();
    pluginManager.registerEvents(qRCodeAccountNumberListener, this);
  
    var toggleViewTagNicknameListener = new ToggleViewTagNicknameListener(this);
    pluginManager.registerEvents(toggleViewTagNicknameListener, this);

    var chatListener = new ChatListener(this, radiusSquared);
    pluginManager.registerEvents(chatListener, this);

     _webServerAction = new WebServerAction(this, isTest);
    this._webServerAction.startServer();
  }

  @Override
  public void onDisable() {
    this._webServerAction.stopServer();
    UserHelper.dropTeam();
  }
}
