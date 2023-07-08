package net.ziozyun.capyland.helpers;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

public class UserHelper {
  private static Map<String, Set<String>> ipDictionary = new HashMap<>();
  public static JavaPlugin plugin;

  public static void add(Player player) {
    var ip = player.getAddress().getAddress().getHostAddress();
    var nickname = player.getName();

    ipDictionary.compute(ip, (key, existingSet) -> {
      if (existingSet == null) {
        var newSet = new TreeSet<>(String.CASE_INSENSITIVE_ORDER);
        newSet.add(nickname);

        return newSet;
      } else if (!existingSet.contains(nickname)) {
        existingSet.add(nickname);
      }

      return existingSet;
    });
  }

  public static boolean exists(Player player) {
    var ip = player.getAddress().getAddress().getHostAddress();
    var nickname = player.getName();

    var result = ipDictionary.entrySet().stream()
      .anyMatch(entry -> entry.getKey().equalsIgnoreCase(ip)
      && entry.getValue().contains(nickname));

    return result;
  }

  public static void removeNicknameFromAllIPs(String nickname) {
    for (var nicknames : ipDictionary.values()) {
      nicknames.remove(nickname);
    }
  }

  public static void setSkin(String nickname, String url) {
    var server = plugin.getServer();
    var commandSender = server.getConsoleSender();

    Bukkit.getScheduler().runTaskLater(plugin, () -> {
      server.dispatchCommand(commandSender, "skin clear " + nickname);
    }, 10L);

    Bukkit.getScheduler().runTaskLater(plugin, () -> {
      server.dispatchCommand(commandSender, "sr CreateCustom " + nickname + " " + url);
    }, 10L);

    Bukkit.getScheduler().runTaskLater(plugin, () -> {
      server.dispatchCommand(commandSender, "sr applyskin " + nickname);
    }, 80L);
  }

  public static void createTeam() {
    var server = plugin.getServer();
    var commandSender = server.getConsoleSender();

    Bukkit.getScheduler().runTaskLater(plugin, () -> {
      server.dispatchCommand(commandSender, "team add Player");
      server.dispatchCommand(commandSender, "team modify Player nametagVisibility never");
    }, 10L);
  }

  public static void dropTeam() {
    var server = plugin.getServer();
    var commandSender = server.getConsoleSender();

    server.dispatchCommand(commandSender, "team remove Player");
  }

  public static void addToTeam(String nickname) {
    var server = plugin.getServer();
    var commandSender = server.getConsoleSender();

    Bukkit.getScheduler().runTaskLater(plugin, () -> {
      server.dispatchCommand(commandSender, "team join Player " + nickname);
    }, 10L);
  }

  public static void removeFromTeam(String nickname) {
    var server = plugin.getServer();
    var commandSender = server.getConsoleSender();

    Bukkit.getScheduler().runTaskLater(plugin, () -> {
      server.dispatchCommand(commandSender, "team leave " + nickname);
    }, 10L);
  }

  public static String getCoordinates(Player player) {
    var location = player.getLocation();

    var x = location.getBlockX();
    var y = location.getBlockY();
    var z = location.getBlockZ();

    var result = "[" + x + ", " + y + ", " + z + "]";

    return result;
  }
}
