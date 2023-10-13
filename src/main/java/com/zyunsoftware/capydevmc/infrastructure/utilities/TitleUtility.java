package com.zyunsoftware.capydevmc.infrastructure.utilities;

import java.time.Duration;

import org.bukkit.entity.Player;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.title.Title;

public class TitleUtility {
  public static void sendTitle(Player player, String title, String subtitle, int fadeIn, int stay, int fadeOut) {
    Title titleMessage = Title.title(
      Component.text(title),
      Component.text(subtitle),
      Title.Times.times(
        Duration.ofSeconds(fadeIn),
        Duration.ofSeconds(stay),
        Duration.ofSeconds(fadeOut)
      )
    );

    player.showTitle(titleMessage);
  }

  public static void clearTitle(Player player) {
    Title clearTitle = Title.title(Component.empty(), Component.empty());
    player.showTitle(clearTitle);
  }
}
