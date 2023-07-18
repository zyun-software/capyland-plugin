package net.ziozyun.capyland.listeners;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import net.ziozyun.capyland.helpers.RequestHelper;
import net.ziozyun.capyland.helpers.UserHelper;

public class ChatListener implements Listener {
  private double _radiusSquared;

  public ChatListener(double radiusSquared) {
    _radiusSquared = radiusSquared;
  }

  @EventHandler
  public void onPlayerChat(AsyncPlayerChatEvent event) {
    event.setCancelled(true);
    var player = event.getPlayer();

    if (!UserHelper.isAuthorized(player)) {
      return;
    }

    var nickname = player.getName();

    var coordinates = UserHelper.getCoordinates(player);

    var message = event.getMessage()
        .replaceAll("[\\r\\n]+", " ")
        .replaceAll("\\s{2,}", " ")
        .replace("#loc", coordinates)
        .replace("#корди", coordinates);

    if (message.equals("#скін")) {
      var skinUrl = RequestHelper.getSkinUrl(player);
      if (skinUrl != null) {
        UserHelper.setSkin(player, skinUrl);
        player.sendMessage(ChatColor.GREEN + "Скін оновлено");
        return;
      }
      player.sendMessage(ChatColor.YELLOW + "У вас не встановлено скін в Капіботі");
      return;
    }

    if (message.startsWith("!")) {
      if (message.length() > 1) {
        for (var onlinePlayer : Bukkit.getOnlinePlayers()) {
          var name = onlinePlayer.getName();
          if (message.contains(name)) {
            UserHelper.playLevelUpSound(onlinePlayer);
            message = message.replace(name, ChatColor.GOLD + name + ChatColor.RESET);
          }
        }

        Bukkit.broadcastMessage(ChatColor.GOLD + nickname + ChatColor.RESET + ": " + message.substring(1));
      } else {
        player.sendMessage(ChatColor.RED + "Необхідно ввести повідомлення");
      }

      return;
    }

    var find = false;
    var text = ChatColor.GOLD + nickname + ChatColor.GRAY + ": " + message;

    var world1 = player.getWorld();
    for (var nearbyPlayer : Bukkit.getOnlinePlayers()) {
      var world2 = nearbyPlayer.getWorld();
      if (world1.equals(world2) && player.getLocation().distanceSquared(nearbyPlayer.getLocation()) <= _radiusSquared) {
        var notMe = !nearbyPlayer.getName().equals(nickname);
        var notSpectator = !(nearbyPlayer.getGameMode() == GameMode.SPECTATOR);

        var name = nearbyPlayer.getName();
        if (message.contains(name)) {
          UserHelper.playLevelUpSound(nearbyPlayer);
          text = text
              .replace(name, ChatColor.GOLD + name + ChatColor.GRAY);
        }

        if (notMe) {
          nearbyPlayer.sendMessage(text);
        }

        if (!find && notMe && notSpectator) {
          find = true;
        }
      }
    }

    player.sendMessage(find ? text : ChatColor.RED + "Вас ніхто не почув");
  }

  @EventHandler
  public void onPlayerDeath(PlayerDeathEvent event) {
    var player = event.getEntity();
    var playerName = ChatColor.GOLD + player.getName() + ChatColor.YELLOW;

    event.setDeathMessage(playerName + " склеїв ласти");

    var coordinates = UserHelper.getCoordinates(player);
    player
        .sendMessage(ChatColor.RED + "Ви склеїли ласти! Бігти за вашими речами сюди: " + ChatColor.GOLD + coordinates);
  }

  @EventHandler
  public void onPlayerJoin(PlayerJoinEvent event) {
    var player = event.getPlayer();
    event.setJoinMessage(ChatColor.GOLD + player.getName() + ChatColor.YELLOW + " завітав на Долину Капібар");
  }

  @EventHandler
  public void onPlayerQuit(PlayerQuitEvent event) {
    var player = event.getPlayer();
    event.setQuitMessage(ChatColor.GOLD + player.getName() + ChatColor.YELLOW + " покинув Долину Капібар");
  }
}
