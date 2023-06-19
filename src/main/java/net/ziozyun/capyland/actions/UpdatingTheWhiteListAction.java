package net.ziozyun.capyland.actions;

import java.util.Arrays;
import java.util.logging.Logger;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.Server;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitRunnable;

import net.ziozyun.capyland.helpers.RequestHelper;
import net.ziozyun.capyland.interfaces.Action;


public class UpdatingTheWhiteListAction implements Action {
  private int _interval;
  private Server _server;
  private Logger _logger;
  private ConsoleCommandSender _consoleSender;
  private Plugin _plugin;

  public UpdatingTheWhiteListAction(Plugin plugin) {
    _plugin = plugin;

    _server = plugin.getServer();
    _logger = plugin.getLogger();
    _consoleSender = _server.getConsoleSender();

    var config = plugin.getConfig();

    _interval = config.getInt("interval", 20);
  }

  @Override
  public void execute() {
    new BukkitRunnable() {
      @Override
      public void run() {
        var whitelistedPlayers = Bukkit.getWhitelistedPlayers();

        try {
          var serverNicknames = RequestHelper.whitelist();
          var serverNicknameList = Arrays.asList(serverNicknames);

          for (var player : whitelistedPlayers) {
            var nickname = player.getName();
            var find = serverNicknameList.contains(nickname);

            if (!find) {
              _server.dispatchCommand(_consoleSender, "kick " + nickname + " Вас було позбавлено громадянства");
              _server.dispatchCommand(_consoleSender, "whitelist remove " + nickname);
              _server.broadcastMessage(
                ChatColor.GOLD + nickname + ChatColor.RED + " позбавлено громадянства"
              );
            }
          }

          for (var nickname : serverNicknames) {
            var find = whitelistedPlayers.stream()
              .map(OfflinePlayer::getName)
              .anyMatch(nickname::equalsIgnoreCase);

            if (!find) {
              _server.dispatchCommand(_consoleSender, "whitelist add " + nickname);
              _server.broadcastMessage(
                ChatColor.GOLD + nickname + ChatColor.GREEN + " щойно отримав громадянство"
              );
            }
          }
        } catch (Exception e) {
          _logger.severe("Помилка отримання білого списку гравців від бота");
        }
      }
    }.runTaskTimer(_plugin, 0, _interval);
  }
}
