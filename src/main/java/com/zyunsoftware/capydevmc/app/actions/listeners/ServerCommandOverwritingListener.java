package com.zyunsoftware.capydevmc.app.actions.listeners;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;

import com.zyunsoftware.capydevmc.infrastructure.utilities.ConfigUtility;

public class ServerCommandOverwritingListener implements Listener {
  @EventHandler
  public void onCommand(PlayerCommandPreprocessEvent event) {
    String command = event.getMessage().toLowerCase();
    Player player = event.getPlayer();
    if (command.startsWith("/pl") || command.startsWith("/plugins")) {
      event.setCancelled(true);
      String message = ConfigUtility.getString("message.command.plugins");
      player.sendMessage(message);
    } else if (command.startsWith("/help")) {
      event.setCancelled(true);
      String message = ConfigUtility.getString("message.command.help");
      player.sendMessage(message);
    }
  }
}
