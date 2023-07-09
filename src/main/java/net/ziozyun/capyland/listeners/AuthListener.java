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
  private String _needAuth = ChatColor.GOLD + "Необхідна авторизація в Капіботі";
  private boolean _isTest;

  public AuthListener(boolean isTest) {
    _isTest = isTest;
  }

  @EventHandler
  public void onPlayerLogin(PlayerLoginEvent event) {
    var nickname = event.getPlayer().getName();

    var skinUrl = RequestHelper.getSkinUrl(nickname);
    if (skinUrl != null) {
      UserHelper.setSkin(nickname, skinUrl);
    }

    try {
      var whitelist = RequestHelper.whitelist();
      if (!whitelist.contains(nickname)) {
        event.disallow(
          PlayerLoginEvent.Result.KICK_OTHER,
          ChatColor.RED + "У вас відсутнє громадянство"
        );
      }
    } catch (Exception e) {
      e.printStackTrace();
      event.disallow(
        PlayerLoginEvent.Result.KICK_OTHER,
        ChatColor.DARK_RED + "Виникла помилка під час отримання списку громадян"
      );
    }
  }

  @EventHandler
  public void onPlayerJoin(PlayerJoinEvent event) {
    var player = event.getPlayer();
    event.setJoinMessage(ChatColor.GOLD + player.getName() + ChatColor.YELLOW + " завітав на Долину Капібар");

    if (!UserHelper.exists(player)) {
      player.setGameMode(GameMode.SPECTATOR);
      player.sendMessage(_needAuth);

      try {
        RequestHelper.sendAuthorizeRequest(player.getName());
      } catch (Exception e) {
        e.printStackTrace();
        player.kickPlayer(ChatColor.RED + "Не вдалося відправити запит на авторизацію в Капібота");
      }

      return;
    }

    if (!player.getName().equals("CapyLand")) {
      player.setOp(_isTest);
    }
  }

  @EventHandler
  public void onPlayerQuit(PlayerQuitEvent event) {
    var player = event.getPlayer();
    event.setQuitMessage(ChatColor.GOLD + player.getName() + ChatColor.YELLOW + " покинув Долину Капібар");
    if (!player.getName().equals("CapyLand")) {
      player.setOp(false);
    }
  }

  @EventHandler
  public void onPlayerMove(PlayerMoveEvent event) {
    var player = event.getPlayer();

    if (!UserHelper.exists(player)) {
      player.setGameMode(GameMode.SPECTATOR);
      player.sendMessage(_needAuth);
      event.setCancelled(true);
    }
  }

  @EventHandler
  public void onPlayerInteract(PlayerInteractEvent event) {
    var player = event.getPlayer();

    if (!UserHelper.exists(player)) {
      player.setGameMode(GameMode.SPECTATOR);
      player.sendMessage(_needAuth);
      event.setCancelled(true);
    }
  }

  @EventHandler
  public void onPlayerInteractEntity(PlayerInteractEntityEvent event) {
    var player = event.getPlayer();

    if (!UserHelper.exists(player)) {
      player.setGameMode(GameMode.SPECTATOR);
      player.sendMessage(_needAuth);
      event.setCancelled(true);
    }
  }
}
