package net.ziozyun.capyland.listeners;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import net.ziozyun.capyland.helpers.RequestHelper;
import net.ziozyun.capyland.helpers.UserHelper;

public class ChatListener implements Listener {
  private double _radiusSquared;

  public ChatListener(double radiusSquared) {
    _radiusSquared = radiusSquared;
  }

  @EventHandler
  public void onPlayerChat(AsyncPlayerChatEvent event) {
    event.setCancelled(true);
    var player = event.getPlayer();

    if (!UserHelper.isAuthorized(player)) {
      return;
    }

    var nickname = player.getName();

    var coordinates = UserHelper.getCoordinates(player);

    var message = event.getMessage()
        .replaceAll("[\\r\\n]+", " ")
        .replaceAll("\\s{2,}", " ")
        .replace("#loc", coordinates)
        .replace("#корди", coordinates);

    if (message.equals("#скін")) {
      var skinUrl = RequestHelper.getSkinUrl(player);
      if (skinUrl != null) {
        UserHelper.setSkin(player, skinUrl);
        player.sendMessage(ChatColor.GREEN + "Скін оновлено");
        return;
      }
      player.sendMessage(ChatColor.YELLOW + "У вас не встановлено скін в" + ChatColor.GOLD + " Капіботі");
      return;
    }

    if (message.equals("#вихід")) {
      UserHelper.removeFromAuthorized(player);
      Bukkit.getScheduler().runTaskLater(UserHelper.plugin, () -> {
        player.kickPlayer(ChatColor.GREEN + "Ви успішно вийшли");
      }, 0L);
      return;
    }

    if (message.equals("#громадяни") && player.isOp()) {
      UserHelper.updateTheListOfCitizens();
      player.sendMessage(ChatColor.GREEN + "Список громадян оновлено");
      return;
    }

    if (message.equals("#список гостей") && player.isOp()) {
      var guestList = UserHelper.guestList();
      player.sendMessage(
          ChatColor.YELLOW + "Список гостей: " + ChatColor.GOLD + (guestList.equals("") ? "порожній" : guestList));
      return;
    }

    var incorrectNickname = ChatColor.RED + "Неправельний псевдонім";

    var addGuest = "#додати гостя ";
    if (message.contains(addGuest) && player.isOp()) {
      var guestNickname = message.replace(addGuest, "");
      if (!UserHelper.isValidNickname(guestNickname)) {
        player.sendMessage(incorrectNickname);

        return;
      }

      player.sendMessage(UserHelper.guests.contains(guestNickname) ? ChatColor.YELLOW + "Гостя вже додано"
          : ChatColor.GREEN + "Гостя додано");
      UserHelper.addToGuestByNickname(guestNickname);

      return;
    }

    var removeGuest = "#видалити гостя ";
    if (message.contains(removeGuest) && player.isOp()) {
      var guestNickname = message.replace(removeGuest, "");
      if (!UserHelper.isValidNickname(guestNickname)) {
        player.sendMessage(incorrectNickname);

        return;
      }

      var guest = Bukkit.getPlayerExact(guestNickname);
      if (guest != null) {
        Bukkit.getScheduler().runTaskLater(UserHelper.plugin, () -> {
          guest.kickPlayer(ChatColor.RED + "У вас було відібрано статус гостя");
        }, 0L);
      }

      player.sendMessage(!UserHelper.guests.contains(guestNickname) ? ChatColor.YELLOW + "Гостя не існує"
          : ChatColor.GREEN + "Гостя видалено");
      UserHelper.removeFromGuestByNickname(guestNickname);

      return;
    }

    if (message.equals("#видалити гостей") && player.isOp()) {
      for (var guestNickname : UserHelper.guests) {
        var guest = Bukkit.getPlayerExact(guestNickname);
        if (guest != null) {
          Bukkit.getScheduler().runTaskLater(UserHelper.plugin, () -> {
            guest.kickPlayer(ChatColor.RED + "У вас було відібрано статус гостя");
          }, 0L);
        }
      }
      player.sendMessage(ChatColor.GREEN + "Гостей видалено");
      UserHelper.clearGuests();

      return;
    }

    if (message.equals("#команди")) {
      var text = ChatColor.GOLD + "#скін" + ChatColor.YELLOW + " - оновити скін\n" +
          ChatColor.GOLD + "#вихід" + ChatColor.YELLOW + " - вийти";

      if (player.isOp()) {
        text += "\n" + ChatColor.GOLD + "#громадяни" + ChatColor.YELLOW + " - оновити список громадян" +
            "\n" + ChatColor.GOLD + "#список гостей" + ChatColor.YELLOW + " - показати список гостей" +
            "\n" + ChatColor.GOLD + "#додати гостя [псевдонім]" + ChatColor.YELLOW + " - додати гостя" +
            "\n" + ChatColor.GOLD + "#видалити гостя [псевдонім]" + ChatColor.YELLOW + " - видалити гостя";
      }

      player.sendMessage(text);
      return;
    }

    if (message.startsWith("!")) {
      if (message.length() > 1) {
        for (var onlinePlayer : Bukkit.getOnlinePlayers()) {
          var name = onlinePlayer.getName();
          if (message.contains(name)) {
            UserHelper.playLevelUpSound(onlinePlayer);
            message = message.replace(name, ChatColor.GOLD + name + ChatColor.RESET);
          }
        }

        Bukkit.broadcastMessage(ChatColor.GOLD + nickname + ChatColor.RESET + ": " + message.substring(1));
      } else {
        player.sendMessage(ChatColor.RED + "Необхідно ввести повідомлення");
      }

      return;
    }

    var find = false;
    var text = ChatColor.GOLD + nickname + ChatColor.GRAY + ": " + message;

    var world1 = player.getWorld();
    for (var nearbyPlayer : Bukkit.getOnlinePlayers()) {
      var world2 = nearbyPlayer.getWorld();
      if (world1.equals(world2) && player.getLocation().distanceSquared(nearbyPlayer.getLocation()) <= _radiusSquared) {
        var notMe = !nearbyPlayer.getName().equals(nickname);
        var notSpectator = !(nearbyPlayer.getGameMode() == GameMode.SPECTATOR);

        var name = nearbyPlayer.getName();
        if (message.contains(name)) {
          UserHelper.playLevelUpSound(nearbyPlayer);
          text = text
              .replace(name, ChatColor.GOLD + name + ChatColor.GRAY);
        }

        if (notMe) {
          nearbyPlayer.sendMessage(text);
        }

        if (!find && notMe && notSpectator) {
          find = true;
        }
      }
    }

    player.sendMessage(find ? text : ChatColor.RED + "Вас ніхто не почув");
  }

  @EventHandler
  public void onPlayerDeath(PlayerDeathEvent event) {
    var player = event.getEntity();
    var playerName = ChatColor.GOLD + player.getName() + ChatColor.YELLOW;

    event.setDeathMessage(playerName + " склеїв ласти");

    var coordinates = UserHelper.getCoordinates(player);
    player
        .sendMessage(ChatColor.RED + "Ви склеїли ласти! Бігти за вашими речами сюди: " + ChatColor.GOLD + coordinates);
  }

  @EventHandler
  public void onPlayerJoin(PlayerJoinEvent event) {
    var player = event.getPlayer();
    event.setJoinMessage(ChatColor.GOLD + player.getName() + ChatColor.YELLOW + " завітав на Долину Капібар");
  }

  @EventHandler
  public void onPlayerQuit(PlayerQuitEvent event) {
    var player = event.getPlayer();
    event.setQuitMessage(ChatColor.GOLD + player.getName() + ChatColor.YELLOW + " покинув Долину Капібар");
  }
}
