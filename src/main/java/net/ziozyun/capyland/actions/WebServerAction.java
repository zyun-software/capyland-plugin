package net.ziozyun.capyland.actions;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.logging.Logger;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitRunnable;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;

import net.ziozyun.capyland.helpers.RequestHelper;
import net.ziozyun.capyland.helpers.UserHelper;

public class WebServerAction {
  private HttpServer _httpServer;
  private Logger _logger;
  private int _port;
  private String _token;
  private Plugin _plugin;
  private boolean _isTest;

  public WebServerAction(Plugin plugin, boolean isTest) {
    _logger = plugin.getLogger();
    _plugin = plugin;

    var config = plugin.getConfig();
    _port = config.getInt("port", 8080);
    _token = config.getString("token", "");
    _isTest = isTest;
  }
    
    public void startServer() {
      try {
        _httpServer = HttpServer.create(new InetSocketAddress(_port), 0);
        _httpServer.createContext("/send-command", exchange -> {
          Bukkit.getScheduler().runTaskLater(_plugin, () -> {
            var response = "Приймаю тільки POST!";
            var code = 400;

            try {
              var token = getRequestParam(exchange, "token");
              var command = getRequestParam(exchange, "command");
              var nickname = getRequestParam(exchange, "nickname");

              var player = Bukkit.getPlayer(nickname);

              if (!_token.equals(token)) {
                code = 403;
                response = "Не вірний токен!";
              } else {
                switch (command) {
                  case "apply-skin":
                    if (player != null) {
                      var skinUrl = RequestHelper.getSkinUrl(nickname);
                      if (skinUrl != null) {
                        UserHelper.setSkin(nickname, skinUrl);
                      }
                    }

                    code = 200;
                    response = "Встановити скін";
                    break;
                  case "logout":
                    if (player != null) {
                      player.kickPlayer(ChatColor.GREEN + "Ви успішно вийшли через Капібота");
                    }

                    UserHelper.removeNicknameFromAllIPs(nickname);
                    code = 200;
                    response = "Вихід з облікового запису";

                    break;
                  case "to-authorize":
                    if (player != null && !UserHelper.exists(player)) {
                      var gameMode = _isTest ? GameMode.CREATIVE : GameMode.SURVIVAL;
                      player.setGameMode(gameMode);
                      player.setOp(_isTest);
                      player.sendMessage(ChatColor.GREEN + "Ви успішно авторизувалися через Капібота");
                      UserHelper.add(player);
                    }
                    code = 200;
                    response = "Авторизація";

                    break;
                  case "update-citizen-list":
                    var whitelist = RequestHelper.whitelist();
                    for (var onlinePlayer : Bukkit.getOnlinePlayers()) {
                      var name = onlinePlayer.getName();
                      if (!whitelist.contains(name)) {
                        onlinePlayer.kickPlayer(ChatColor.DARK_RED + "Вас депортувала Служба безпеки Долини Капібар");
                        UserHelper.removeNicknameFromAllIPs(name);
                      }
                    }
                    code = 200;
                    response = "Оновлення списку громадян";

                    break;
                  default:
                    code = 404;
                    response = "Команду не знайдено";

                    break;
                }
              }
            } catch (Exception e) {
              e.printStackTrace();
              response = "Помилка";
              code = 500;
            }

            try {
              exchange.sendResponseHeaders(code, response.getBytes().length);
              var outputStream = exchange.getResponseBody();
              outputStream.write(response.getBytes());
              outputStream.close();
            } catch (IOException e) {
              e.printStackTrace();
            }
          }, 10L);
        });

        _httpServer.setExecutor(null);
        _httpServer.start();

        _logger.info("Веб-сервер запущений на порту " + _port);
      } catch (IOException e) {
        e.printStackTrace();
        _logger.warning("Помилка запуску веб-сервера: " + e.getMessage());
      }
  }

  private String getRequestParam(HttpExchange exchange, String paramName) {
    var query = exchange.getRequestURI().getQuery();
    var params = query.split("&");
    for (var param : params) {
      var keyValue = param.split("=");
      if (keyValue.length == 2 && keyValue[0].equals(paramName)) {
        return keyValue[1];
      }
    }

    return null;
  }

  public void stopServer() {
    if (_httpServer != null) {
      _httpServer.stop(0);
      _logger.info("Веб-сервер зупинений");
    }
  } 
}
