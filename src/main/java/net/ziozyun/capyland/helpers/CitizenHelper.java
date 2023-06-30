package net.ziozyun.capyland.helpers;

import java.util.Arrays;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitRunnable;

public class CitizenHelper {
  public static Plugin plugin;

  public static void issueCitizenship(String nickname) {
    new BukkitRunnable() {
      @Override
      public void run() {
        Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "whitelist add " + nickname);
        Bukkit.broadcastMessage(
          ChatColor.GOLD + nickname + ChatColor.GREEN + " щойно отримав громадянство"
        );
      }
    }.runTaskLater(plugin, 20L);
  }

  public static void depriveOfCitizenship(String nickname) {
    new BukkitRunnable() {
      @Override
      public void run() {
        var consoleSender = Bukkit.getConsoleSender();
        Bukkit.dispatchCommand(consoleSender, "kick " + nickname + " Вас було позбавлено громадянства");
        Bukkit.dispatchCommand(consoleSender, "whitelist remove " + nickname);
        Bukkit.broadcastMessage(
          ChatColor.GOLD + nickname + ChatColor.RED + " позбавлено громадянства"
        );
      }
    }.runTaskLater(plugin, 20L);
  }

  public static void updateCitizenship(String[] serverNicknames) {
    new BukkitRunnable() {
      @Override
      public void run() {
        var whitelistedPlayers = Bukkit.getWhitelistedPlayers();
        var serverNicknameList = Arrays.asList(serverNicknames);

        for (var player : whitelistedPlayers) {
          var nickname = player.getName();
          var find = serverNicknameList.contains(nickname);

          if (!find) {
            depriveOfCitizenship(nickname);
          }
        }

        for (var nickname : serverNicknames) {
          var find = whitelistedPlayers.stream()
            .map(OfflinePlayer::getName)
            .anyMatch(nickname::equalsIgnoreCase);

          if (!find) {
            issueCitizenship(nickname);
          }
        }
      }
    }.runTaskLater(plugin, 20L);
  }
}
