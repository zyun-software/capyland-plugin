package net.ziozyun.capyland.listeners;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.ArrayList;
import java.util.List;

import net.md_5.bungee.api.ChatColor;
import net.ziozyun.capyland.helpers.UserHelper;


public class ToggleViewTagNicknameListener implements Listener {
  private JavaPlugin _plugin;

  public ToggleViewTagNicknameListener(JavaPlugin plugin) {
    _plugin = plugin;
  }

  @EventHandler
  public void onPlayerJoin(PlayerJoinEvent event) {
    var player = event.getPlayer();
    var nickname = player.getName();

    UserHelper.addToTeam(nickname);
  }

  @EventHandler
  public void onPlayerQuit(PlayerQuitEvent event) {
    var player = event.getPlayer();
    var nickname = player.getName();

    UserHelper.removeFromTeam(nickname);
  }

  @EventHandler
  public void onPlayerInteractEntity(PlayerInteractEntityEvent event) {
    if (!(event.getRightClicked() instanceof Player)) {
      return;
    }

    var nickname = ((Player) event.getRightClicked()).getName();

    event.getPlayer().sendMessage(ChatColor.YELLOW + "Перед вами " + ChatColor.GOLD);
  }
}
