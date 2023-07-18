package net.ziozyun.capyland.listeners;

import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.entity.Player;
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
import net.ziozyun.capyland.helpers.RequestHelper.AuthorizeRequestData;

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

    if (!UserHelper.isAuthorized(player)) {
      player.setGameMode(GameMode.SPECTATOR);

      try {
        var data = RequestHelper.sendAuthorizeRequest(player);
        _waitForApproveAuthorizeRequest(player, data, 0);
      } catch (Exception e) {
        e.printStackTrace();
        player.kickPlayer(ChatColor.RED + "Не вдалося відправити запит на авторизацію в Капібота");
      }

      return;
    }

    _updatePlayerParameters(player);
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

  private void _waitForApproveAuthorizeRequest(Player player, AuthorizeRequestData data, int i) {
    Bukkit.getScheduler().runTaskLater(UserHelper.plugin, () -> {
      var max = 15;
      if (i == max) {
        player.kickPlayer(ChatColor.RED + "Час для підтвердження авторизації вичерпано");
        return;
      }

      try {
        var token = RequestHelper.getAuthorizeToken(player);
        if (token.equals(data.token)) {
          _updatePlayerParameters(player);

          UserHelper.addToAuthorized(player);
          UserHelper.addToTeam(player);

          player.sendMessage(ChatColor.GREEN + "Ви успішно авторизувалися за допомогою" + ChatColor.GOLD + "Капібота");
          return;
        }
      } catch (Exception e) {
        player.sendMessage("піська");
        // ігнор
      }

      player.sendMessage(ChatColor.YELLOW + "Необхідно підтвердити авторизацію в " + ChatColor.GOLD + "Капіботі");
      _waitForApproveAuthorizeRequest(player, data, i + 1);
    }, i == 0 ? 0L : 40L);
  }

  private void _updatePlayerParameters(Player player) {
    var gamemode = UserHelper.isTest ? GameMode.CREATIVE : GameMode.SURVIVAL;
    player.setGameMode(gamemode);

    var op = UserHelper.opString.contains(player.getName());
    player.setOp(op);
  }
}
