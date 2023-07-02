package net.ziozyun.capyland.listeners;

import java.util.Map;

import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerLoginEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.plugin.java.JavaPlugin;

import net.md_5.bungee.api.ChatColor;
import net.ziozyun.capyland.helpers.RequestHelper;
import net.ziozyun.capyland.helpers.UserHelper;

public class AuthListener implements Listener {
  private JavaPlugin _plugin;
  private String _needAuth = ChatColor.GOLD + "Необхідна авторизація в Капіботі";

  public AuthListener(JavaPlugin plugin) {
    _plugin = plugin;
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

    var ip = player.getAddress().getAddress().getHostAddress();
    var nickname = player.getName();

    event.setJoinMessage(ChatColor.GOLD + nickname + ChatColor.YELLOW + " завітав на Долину Капібар");



    if (!UserHelper.exists(ip, nickname)) {
      player.setGameMode(GameMode.SPECTATOR);
      player.sendMessage(_needAuth);
      try {
        RequestHelper.sendAuthorizeRequest(nickname);
      } catch (Exception e) {
        e.printStackTrace();
        Bukkit.getScheduler().runTaskLater(_plugin, () -> {
          player.kickPlayer(ChatColor.RED + "Не вдалося відправити запит на авторизацію в Капібота");
        }, 20L);
      }
    }
  }

  @EventHandler
  public void onPlayerQuit(PlayerQuitEvent event) {
    event.setQuitMessage(ChatColor.GOLD + event.getPlayer().getName() + ChatColor.YELLOW + " покинув Долину Капібар");
  }

  @EventHandler
  public void onPlayerMove(PlayerMoveEvent event) {
    var player = event.getPlayer();

    if (_lock(player)) {
      player.sendMessage(_needAuth);
      event.setCancelled(true);
    }
  }

  @EventHandler
  public void onPlayerChat(AsyncPlayerChatEvent event) {
    var player = event.getPlayer();

    if (_lock(player)) {
      event.setCancelled(true);
    }
  }

  private static boolean _lock(Player player) {
    var ip = player.getAddress().getAddress().getHostAddress();
    var nickname = player.getName();

    var result = player.getGameMode() == GameMode.SPECTATOR && !UserHelper.exists(ip, nickname);
    return result;
  }
}
