package net.ziozyun.capyland.actions;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.logging.Logger;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.GameMode;
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

  public WebServerAction(Plugin plugin) {
    _logger = plugin.getLogger();
    _plugin = plugin;

    var config = plugin.getConfig();
    _port = config.getInt("port", 8080);
    _token = config.getString("token", "");
    _isTest = config.getString("from", "").equalsIgnoreCase("test");
  }
    
  public void startServer() {
    try {
      _httpServer = HttpServer.create(new InetSocketAddress(_port), 0);
      _httpServer.createContext("/send-command", new HttpHandler() {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
          var response = "Приймаю тільки POST!";
          var code = 400;

          if ("POST".equals(exchange.getRequestMethod())) {      
            var token = getRequestParam(exchange, "token");

            if (!_token.equals(token)) {
              code = 403;
              response = "Не вірний токен!";
            } else {
              var command = getRequestParam(exchange, "command");
              var nickname = getRequestParam(exchange, "nickname");

              switch (command) {
                case "apply-skin":
                  Bukkit.getScheduler().runTaskLater(_plugin, () -> {
                    var player = Bukkit.getPlayer(nickname);
                    if (player != null) {
                      var skinUrl = RequestHelper.getSkinUrl(nickname);
                      if (skinUrl != null) {
                        UserHelper.setSkin(nickname, skinUrl);
                      }
                    }
                  }, 20L);
                  code = 200;
                  response = "Встановити скін";
                  break;
                case "kick":
                  Bukkit.getScheduler().runTaskLater(_plugin, () -> {
                    var player = Bukkit.getPlayer(nickname);
                    if (player != null) {
                      player.kickPlayer(ChatColor.GOLD + "Вас було вислано за кордон Службою безпеки Долини Капібар");
                    }
                  }, 20L);
                  code = 200;
                  response = "Кік";
                  break;
                case "to-authorize":
                  Bukkit.getScheduler().runTaskLater(_plugin, () -> {
                    var player = Bukkit.getPlayer(nickname);
                    if (player != null && player.getGameMode() == GameMode.SPECTATOR) {
                      var gameMode = _isTest ? GameMode.CREATIVE : GameMode.SURVIVAL;
                      player.setGameMode(gameMode);
                      player.sendMessage(ChatColor.GREEN + "Ви успішно авторизувалися через Капібота");

                      var ip = player.getAddress().getAddress().getHostAddress();

                      UserHelper.add(ip, nickname);
                    }
                  }, 20L);
                  code = 200;
                  response = "Авторизація";
                  break;
                default:
                  code = 404;
                  response = "Команду не знайдено";
                  break;
              }
            }
          }

          exchange.sendResponseHeaders(code, response.getBytes().length);
          var outputStream = exchange.getResponseBody();
          outputStream.write(response.getBytes());
          outputStream.close();
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
      });

      _httpServer.setExecutor(null);
      _httpServer.start();

      _logger.info("Веб-сервер запущений на порту " + _port);
    } catch (IOException e) {
      e.printStackTrace();
      _logger.warning("Помилка запуску веб-сервера: " + e.getMessage());
    }
  }

  public void stopServer() {
    if (_httpServer != null) {
      _httpServer.stop(0);
      _logger.info("Веб-сервер зупинений");
    }
  } 
}
