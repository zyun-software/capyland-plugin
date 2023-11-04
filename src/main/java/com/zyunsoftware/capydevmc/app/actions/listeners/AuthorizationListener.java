package com.zyunsoftware.capydevmc.app.actions.listeners;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerPortalEvent;

import com.zyunsoftware.capydevmc.app.DependencyInjection;
import com.zyunsoftware.capydevmc.domain.models.minecraft.MinecraftRepository;
import com.zyunsoftware.capydevmc.domain.services.AuthorizationService;

public class AuthorizationListener implements Listener {
    @EventHandler
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
    }
}
