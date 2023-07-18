package net.ziozyun.capyland.helpers;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import net.md_5.bungee.api.ChatColor;
import net.ziozyun.capyland.helpers.RequestHelper.AuthorizeRequestData;

public class UserHelper {
  private static Map<String, Set<String>> ipDictionary = new HashMap<>();
  public static List<String> guests = new ArrayList<>();
  public static JavaPlugin plugin;
  public static boolean isTest;
  public static String opString;

  public static void addToGuestByNickname(String nickname) {
    if (!guests.contains(nickname)) {
      guests.add(nickname);
    }
  }

  public static void removeFromGuestByNickname(String nickname) {
    if (guests.contains(nickname)) {
      guests.remove(nickname);
    }
  }

  public static void clearGuests() {
    guests.clear();
  }

  public static boolean isGuest(Player player) {
    return guests.contains(player.getName());
  }

  public static String guestList() {
    var result = String.join(", ", guests);
    return result;
  }

  public static void addToAuthorized(Player player) {
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

  public static boolean isAuthorized(Player player) {
    var ip = player.getAddress().getAddress().getHostAddress();
    var nickname = player.getName();

    var result = ipDictionary.entrySet().stream()
        .anyMatch(entry -> entry.getKey().equalsIgnoreCase(ip)
            && entry.getValue().contains(nickname));

    return result;
  }

  public static void removeFromAuthorized(Player player) {
    var nickname = player.getName();
    for (var nicknames : ipDictionary.values()) {
      nicknames.remove(nickname);
    }
  }

  public static void setSkin(Player player, String url) {
    var nickname = player.getName();
    var server = plugin.getServer();
    var commandSender = server.getConsoleSender();

    Bukkit.getScheduler().runTaskLater(plugin, () -> {
      server.dispatchCommand(commandSender, "skin clear " + nickname);
    }, 0);

    Bukkit.getScheduler().runTaskLater(plugin, () -> {
      server.dispatchCommand(commandSender, "sr CreateCustom " + nickname + " " + url);
    }, 0);

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
    }, 0);
  }

  public static void dropTeam() {
    var server = plugin.getServer();
    var commandSender = server.getConsoleSender();

    server.dispatchCommand(commandSender, "team remove Player");
  }

  public static void addToTeam(Player player) {
    var nickname = player.getName();
    var server = plugin.getServer();
    var commandSender = server.getConsoleSender();

    Bukkit.getScheduler().runTaskLater(plugin, () -> {
      server.dispatchCommand(commandSender, "team join Player " + nickname);
    }, 0);
  }

  public static void removeFromTeam(Player player) {
    var nickname = player.getName();
    var server = plugin.getServer();
    var commandSender = server.getConsoleSender();

    Bukkit.getScheduler().runTaskLater(plugin, () -> {
      server.dispatchCommand(commandSender, "team leave " + nickname);
    }, 0);
  }

  public static String getCoordinates(Player player) {
    var location = player.getLocation();

    var x = location.getBlockX();
    var y = location.getBlockY();
    var z = location.getBlockZ();

    var result = "[" + x + ", " + y + ", " + z + "]";

    return result;
  }

  public static void playLevelUpSound(Player player) {
    Bukkit.getScheduler().runTaskLater(plugin, () -> {
      player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1.0f, 1.0f);
    }, 0L);
  }

  public static void waitForApproveAuthorizeRequest(Player player, AuthorizeRequestData data, int i) {
    var max = 15;
    if (i == max) {
      Bukkit.getScheduler().runTaskLater(plugin, () -> {
        player.kickPlayer(ChatColor.RED + "Час для підтвердження авторизації вичерпано");
      }, 0L);
      return;
    }

    Bukkit.getScheduler().runTaskLater(plugin, () -> {
      try {
        var token = RequestHelper.getAuthorizeToken(player);
        if (token.equals("exit")) {
          player.kickPlayer(ChatColor.YELLOW + "Ви відхилили запит на авторизацію");
          return;
        }
        if (token.equals(data.token)) {
          updateParameters(player);

          UserHelper.addToAuthorized(player);
          UserHelper.addToTeam(player);

          player.sendMessage(ChatColor.GREEN + "Ви успішно авторизувалися за допомогою" + ChatColor.GOLD + " Капібота");
          return;
        }
      } catch (Exception e) {
        // ігнор
      }

      player.sendMessage(ChatColor.YELLOW + "Необхідно підтвердити авторизацію в" + ChatColor.GOLD + " Капіботі"
          + ChatColor.YELLOW + ". Код авторизації: " + ChatColor.GOLD + data.code);
      waitForApproveAuthorizeRequest(player, data, i + 1);
    }, i == 0 ? 0L : 40L);
  }

  public static void updateParameters(Player player) {
    Bukkit.getScheduler().runTaskLater(plugin, () -> {
      var gamemode = isTest ? GameMode.CREATIVE : GameMode.SURVIVAL;
      player.setGameMode(gamemode);

      var op = opString.contains(player.getName());
      player.setOp(op);
    }, 0L);
  }

  public static void sendAuthorizeRequest(Player player) {
    Bukkit.getScheduler().runTaskLater(plugin, () -> {
      player.setGameMode(GameMode.SPECTATOR);
      try {
        var data = RequestHelper.sendAuthorizeRequest(player);
        waitForApproveAuthorizeRequest(player, data, 0);
      } catch (Exception e) {
        e.printStackTrace();
        player.kickPlayer(ChatColor.RED + "Не вдалося відправити запит на авторизацію в Капібота");
      }
    }, 0L);
  }

  public static void updateTheListOfCitizens() {
    Bukkit.getScheduler().runTaskLater(plugin, () -> {
      try {
        var whitelist = RequestHelper.whitelist();
        for (var onlinePlayer : Bukkit.getOnlinePlayers()) {
          onlinePlayer.setOp(false);

          removeFromAuthorized(onlinePlayer);
          removeFromTeam(onlinePlayer);
          sendAuthorizeRequest(onlinePlayer);

          if (!whitelist.contains(onlinePlayer.getName())) {
            onlinePlayer.kickPlayer(ChatColor.RED + "У вас відсутнє громадянство");
          }
        }
      } catch (Exception e) {
        for (var onlinePlayer : Bukkit.getOnlinePlayers()) {
          onlinePlayer.kickPlayer(ChatColor.DARK_RED + "Виникла помилка під час отримання списку громадян");
        }
      }
    }, 0L);
  }

  public static boolean isValidNickname(String username) {
    var regex = "^[a-zA-Z0-9_]{3,16}$";
    var pattern = Pattern.compile(regex);
    var matcher = pattern.matcher(username);

    return matcher.matches();
  }
}
