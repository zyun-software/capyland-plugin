package net.ziozyun.capyland.listeners;

import java.util.Map;

import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerLoginEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.plugin.java.JavaPlugin;

import net.md_5.bungee.api.ChatColor;
import net.ziozyun.capyland.helpers.RequestHelper;
import net.ziozyun.capyland.helpers.UserHelper;

public class AuthListener implements Listener {
  private JavaPlugin _plugin;
  private String _needAuth = ChatColor.GOLD + "Необхідна авторизація в Капіботі";
  private boolean _isTest;

  public AuthListener(JavaPlugin plugin, boolean isTest) {
    _plugin = plugin;
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
        Bukkit.getScheduler().runTaskLater(_plugin, () -> {
          player.kickPlayer(ChatColor.RED + "Не вдалося відправити запит на авторизацію в Капібота");
        }, 20L);
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
