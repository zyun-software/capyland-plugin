package com.zyunsoftware.capydevmc.app.actions.listeners;

import java.util.Set;

import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.World;
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
  private String _repl(String text, Player pl) {
    Set<String> keys = ConfigUtility.getKeys("repl");
    for (String key : keys) {
      text = text.replaceAll(key, ConfigUtility.getString("repl." + key));
    }

    return text;
  }

  @EventHandler
  public void onPlayerChat(AsyncChatEvent event) {
    Player player = event.getPlayer();
    String nickname = player.getName();
    String text = _repl(LegacyComponentSerializer
      .legacyAmpersand()
      .serialize(event.message())
      .replace("<" + nickname + "> ", "")
      .trim(), player);

    /*String lettersToCheck = "ЁЪЫЭ";

    String lowercaseText = text.toLowerCase();
    String lowercaseLettersToCheck = lettersToCheck.toLowerCase();

    for (int i = 0; i < lowercaseLettersToCheck.length(); i++) {
      char letter = lowercaseLettersToCheck.charAt(i);
      boolean containsLetter = lowercaseText.contains(String.valueOf(letter));

      if (containsLetter) {
        
        Bukkit.getScheduler().runTaskLater(CapylandPlugin.getInstance(), () -> {
          player.kick(Component.text("§сДержавною будьте ласкаві!"));
          CapylandPlugin.getInstance().getServer().broadcast(Component.text("§e" + player.getName() + " §7забув державну мову"));
        }, 0L);

        return;
      }
    }*/

    if (text.startsWith("!")) {
      event.renderer(new ChatRenderer() {
        @Override
        public Component render(Player player, Component source, Component message, Audience audience) {
          String nickname = player.getName();
          String rpNickname = ConfigUtility.getString("rp-nicknames." + nickname, "§e" + nickname);

          String text = _repl(LegacyComponentSerializer
            .legacyAmpersand()
            .serialize(message)
            .substring(1)
            .trim(), player);

          Component result = Component.text(rpNickname + "§7: §f" + text);

          return result;
        }
      });

      return;
    }

    event.setCancelled(true);

    text = ConfigUtility.getString("rp-nicknames." + nickname, "§e" + nickname) + "§7: §7" + text;

    boolean find = false;

    World world1 = player.getWorld();

    for (Player nearbyPlayer : Bukkit.getOnlinePlayers()) {
      World world2 = nearbyPlayer.getWorld();
      if (world1.equals(world2) && player.getLocation().distance(nearbyPlayer.getLocation()) <= 50) {
        boolean notMe = !nearbyPlayer.getName().equals(nickname);
        boolean notSpectator = !(nearbyPlayer.getGameMode() == GameMode.SPECTATOR);

        if (notMe) {
          nearbyPlayer.sendMessage(text);
        }

        if (!find && notMe && notSpectator) {
          find = true;
        }
      }
    }

    player.sendMessage(find ? text : "§cВас ніхто не почув");
  }
}
