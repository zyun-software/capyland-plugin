package com.zyunsoftware.capydevmc.app.actions.listeners;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

import com.zyunsoftware.capydevmc.app.DependencyInjection;
import com.zyunsoftware.capydevmc.domain.models.minecraft.MinecraftRepository;
import com.zyunsoftware.capydevmc.domain.services.AuthorizationService;

import io.papermc.paper.event.player.AsyncChatEvent;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;

public class ChatCommandsOpListener implements Listener {
  @EventHandler
  public void onPlayerChat(AsyncChatEvent event) {
    Component messageComponent = event.message();
    Player player = event.getPlayer();

    if (!player.isOp()) {
      return;
    }

    String message = LegacyComponentSerializer.legacyAmpersand().serialize(messageComponent);

    AuthorizationService authorizationService = DependencyInjection.getAuthorizationService();
    MinecraftRepository minecraftRepository = authorizationService.getMinecraftRepository();

    minecraftRepository.selectPlayer(player.getName());

    String approveCommand = "#підтвердити гравця ";
    if (message.startsWith(approveCommand)) {
      String nickname = message.replace(approveCommand, "");
      authorizationService.setApproved(nickname, true);
      event.setCancelled(true);
      return;
    }
    
    String cancelCommand = "#скасувати підтвердження гравця ";
    if (message.startsWith(cancelCommand)) {
      String nickname = message.replace(cancelCommand, "");
      authorizationService.setApproved(nickname, false);
      event.setCancelled(true);
      return;
    }
  }
}
