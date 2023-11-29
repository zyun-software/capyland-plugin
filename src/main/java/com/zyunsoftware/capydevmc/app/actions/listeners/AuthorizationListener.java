package com.zyunsoftware.capydevmc.app.actions.listeners;

import java.net.InetAddress;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerLoginEvent;

import com.zyunsoftware.capydevmc.app.CapylandPlugin;

import net.kyori.adventure.text.Component;

public class AuthorizationListener implements Listener {
  /*@EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
    Player player = event.getPlayer();

    if (!CapylandPlugin.getInstance().hasIP(player.getName(), player.getAddress().getAddress().getHostAddress())) {
      Bukkit.getScheduler().runTaskLater(CapylandPlugin.getInstance(), () -> {
        player.kick(Component.text("§cЦя сесія не авторизована"));
      } , 20L);
    }
  }*/

  @EventHandler
  public void onPlayerLogin(PlayerLoginEvent event) {
    InetAddress ipAddress = event.getRealAddress();

    if (ipAddress != null) {
      String ipAddressString = ipAddress.getHostAddress();

      if (!CapylandPlugin.getInstance().hasIP(event.getPlayer().getName(), ipAddressString)) {
        event.disallow(PlayerLoginEvent.Result.KICK_WHITELIST, Component.text("§cЦя сесія не авторизована"));
      }
    } else {
      event.disallow(PlayerLoginEvent.Result.KICK_OTHER, Component.text("§cПомилка під час отримання IP-адреси"));
    }
  }

  /*@EventHandler
  public void onPlayerJoin(PlayerJoinEvent event) {
    Player player = event.getPlayer();

    AuthorizationService authorizationService = DependencyInjection.getAuthorizationService();
    MinecraftRepository minecraftRepository = authorizationService.getMinecraftRepository();

    minecraftRepository.selectPlayer(player.getName());

    authorizationService.controlSelected();
  }

  @EventHandler
  public void onPlayerPortal(PlayerPortalEvent event) {
    Player player = event.getPlayer();
    AuthorizationService authorizationService = DependencyInjection.getAuthorizationService();
    MinecraftRepository minecraftRepository = authorizationService.getMinecraftRepository();
    minecraftRepository.selectPlayer(player.getName());

    boolean inLobby = minecraftRepository.inLobby();

    if (inLobby) {
      event.setCancelled(true);
    }

    boolean isAuthorized = authorizationService.audit();
    if (isAuthorized) {
      authorizationService.teleportToMain();
    }
  }*/
}
