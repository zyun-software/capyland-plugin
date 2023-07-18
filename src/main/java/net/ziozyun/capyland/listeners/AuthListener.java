package net.ziozyun.capyland.listeners;

import org.bukkit.GameMode;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerLoginEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import net.md_5.bungee.api.ChatColor;
import net.ziozyun.capyland.helpers.RequestHelper;
import net.ziozyun.capyland.helpers.UserHelper;

public class AuthListener implements Listener {
  @EventHandler
  public void onPlayerLogin(PlayerLoginEvent event) {
    var player = event.getPlayer();

    var skinUrl = RequestHelper.getSkinUrl(player);
    if (skinUrl != null) {
      UserHelper.setSkin(player, skinUrl);
    }

    try {
      var whitelist = RequestHelper.whitelist();
      if (!whitelist.contains(player.getName())) {
        event.disallow(
            PlayerLoginEvent.Result.KICK_OTHER,
            ChatColor.RED + "У вас відсутнє громадянство");
      }
    } catch (Exception e) {
      e.printStackTrace();
      event.disallow(
          PlayerLoginEvent.Result.KICK_OTHER,
          ChatColor.DARK_RED + "Виникла помилка під час отримання списку громадян");
    }
  }

  @EventHandler
  public void onPlayerJoin(PlayerJoinEvent event) {
    var player = event.getPlayer();
    player.setOp(false);

    try {
      var whitelist = RequestHelper.whitelist();
      if (!whitelist.contains(player.getName())) {
        player.kickPlayer(ChatColor.RED + "У вас відсутнє громадянство");
      }
    } catch (Exception e) {
      player.kickPlayer(ChatColor.DARK_RED + "Виникла помилка під час отримання списку громадян");
    }

    if (!UserHelper.isAuthorized(player)) {
      player.setGameMode(GameMode.SPECTATOR);

      UserHelper.sendAuthorizeRequest(player);

      return;
    }

    UserHelper.updateParameters(player);
  }

  @EventHandler
  public void onPlayerQuit(PlayerQuitEvent event) {
    var player = event.getPlayer();
    UserHelper.removeFromTeam(player);
  }

  @EventHandler
  public void onPlayerMove(PlayerMoveEvent event) {
    var player = event.getPlayer();

    if (!UserHelper.isAuthorized(player)) {
      player.setGameMode(GameMode.SPECTATOR);
      event.setCancelled(true);
    }
  }

  @EventHandler
  public void onPlayerInteract(PlayerInteractEvent event) {
    var player = event.getPlayer();

    if (!UserHelper.isAuthorized(player)) {
      player.setGameMode(GameMode.SPECTATOR);
      event.setCancelled(true);
    }
  }

  @EventHandler
  public void onPlayerInteractEntity(PlayerInteractEntityEvent event) {
    var player = event.getPlayer();

    if (!UserHelper.isAuthorized(player)) {
      player.setGameMode(GameMode.SPECTATOR);
      event.setCancelled(true);
    }
  }
}
