package com.zyunsoftware.capydevmc.app.actions.listeners;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

import com.zyunsoftware.capydevmc.infrastructure.utilities.ConfigUtility;

import io.papermc.paper.chat.ChatRenderer;
import io.papermc.paper.event.player.AsyncChatEvent;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;

public class ChatListener implements Listener {
  @EventHandler
  public void onPlayerChat(AsyncChatEvent event) {
    event.renderer(new ChatRenderer() {
      @Override
      public Component render(Player player, Component source, Component message, Audience audience) {
        String nickname = player.getName();
        String rpNickname = ConfigUtility.getString("rp-nicknames." + nickname, "§e" + nickname);

        String text = LegacyComponentSerializer
          .legacyAmpersand()
          .serialize(message);

        Component result = Component.text(rpNickname + "§7: §f" + text);

        return result;
      }
    });
  }
}
