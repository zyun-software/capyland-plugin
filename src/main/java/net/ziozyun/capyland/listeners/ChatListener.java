package net.ziozyun.capyland.listeners;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.plugin.java.JavaPlugin;

import net.ziozyun.capyland.helpers.UserHelper;

public class ChatListener implements Listener {
  private JavaPlugin _plugin;
  
  public ChatListener(JavaPlugin plugin) {
    _plugin = plugin;
  }

  @EventHandler
  public void onPlayerChat(AsyncPlayerChatEvent event) {
    event.setCancelled(true);
    var player = event.getPlayer();

    if (!UserHelper.exists(player)) {
      return;
    }

    var nickname = player.getName();
    var location = player.getLocation();

    var x = location.getBlockX();
    var y = location.getBlockY();
    var z = location.getBlockZ();

    final var message = event.getMessage().replace("#loc", "[" + x + ", " + y + ", " + z + "]");

    if (message.startsWith("!")) {
      if (message.length() > 1) {
        Bukkit.getScheduler().runTaskLater(_plugin, () -> {
          Bukkit.broadcastMessage(ChatColor.GOLD + nickname + ChatColor.RESET + ": " + message.substring(1));
        }, 10L);
      } else {
        player.sendMessage(ChatColor.RED + "Необхідно ввести повідомлення");
      }

      return;
    }

  }
}
