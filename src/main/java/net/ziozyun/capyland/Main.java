package net.ziozyun.capyland;

import org.bukkit.plugin.java.JavaPlugin;

import java.lang.Math;

import net.ziozyun.capyland.helpers.RequestHelper;
import net.ziozyun.capyland.helpers.UserHelper;
import net.ziozyun.capyland.listeners.AuthListener;
import net.ziozyun.capyland.listeners.ChatListener;
import net.ziozyun.capyland.listeners.JailListener;
import net.ziozyun.capyland.listeners.PetOwnershipListener;
import net.ziozyun.capyland.listeners.QRCodeAccountNumberListener;
import net.ziozyun.capyland.listeners.ToggleViewTagNicknameListener;

public final class Main extends JavaPlugin {
  @Override
  public void onEnable() {
    var config = getConfig();

    var server = getServer();
    var pluginManager = server.getPluginManager();

    var from = config.getString("from", "test");

    RequestHelper.from = from;
    RequestHelper.host = config.getString("host", "");
    RequestHelper.token = config.getString("token", "");

    UserHelper.plugin = this;
    UserHelper.opString = config.getString("op", "");
    UserHelper.isTest = from.equalsIgnoreCase("test");

    UserHelper.createTeam();

    UserHelper.updateTheListOfCitizens();

    var authListener = new AuthListener();
    pluginManager.registerEvents(authListener, this);

    var qRCodeAccountNumberListener = new QRCodeAccountNumberListener();
    pluginManager.registerEvents(qRCodeAccountNumberListener, this);

    var toggleViewTagNicknameListener = new ToggleViewTagNicknameListener();
    pluginManager.registerEvents(toggleViewTagNicknameListener, this);

    var radius = config.getDouble("radius", 30.0);
    var radiusSquared = Math.pow(radius, 2);
    var chatListener = new ChatListener(radiusSquared);
    pluginManager.registerEvents(chatListener, this);

    var jailListener = new JailListener();
    pluginManager.registerEvents(jailListener, this);

    var petOwnershipListener = new PetOwnershipListener();
    pluginManager.registerEvents(petOwnershipListener, this);
  }

  @Override
  public void onDisable() {
    UserHelper.dropTeam();
  }
}
