package net.ziozyun.capyland.listeners;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import net.md_5.bungee.api.ChatColor;
import net.ziozyun.capyland.helpers.UserHelper;


public class ToggleViewTagNicknameListener implements Listener {
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
    var clickedEntity = event.getRightClicked();
    if (!(clickedEntity instanceof Player)) {
      return;
    }

    var player = event.getPlayer();
    var clickedPlayer = (Player) clickedEntity;
    var displayName = ChatColor.GOLD + clickedPlayer.getName();

    player.sendTitle(displayName, "", 10, 40, 10);
  }
}
