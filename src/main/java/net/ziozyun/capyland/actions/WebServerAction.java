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

import net.ziozyun.capyland.helpers.CitizenHelper;
import net.ziozyun.capyland.helpers.RequestHelper;
import net.ziozyun.capyland.helpers.UserHelper;

public class WebServerAction {
  private HttpServer _httpServer;
  private Logger _logger;
  private int _port;
  private String _token;
  private Plugin _plugin;

  public WebServerAction(Plugin plugin) {
    _logger = plugin.getLogger();
    _plugin = plugin;

    var config = plugin.getConfig();
    _port = config.getInt("port", 8080);
    _token = config.getString("token", "");
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
                case "to-authorize":
                  new BukkitRunnable() {
                    @Override
                    public void run() {
                      var player = Bukkit.getPlayer(nickname);
                      if (player != null) {
                        player.setGameMode(GameMode.SURVIVAL);
                        player.sendMessage(ChatColor.GREEN + "Ви успішно авторизувалися через Капібота");

                        var ip = player.getAddress().getAddress().getHostAddress();

                        UserHelper.add(ip, nickname);
                      }
                    }
                  }.runTaskLater(_plugin, 20L);
                  code = 200;
                  response = "Авторизація";
                  break;
                case "issue-citizenship":
                  if (nickname != null) {
                    CitizenHelper.issueCitizenship(nickname);
                  }
                  code = 200;
                  response = "Видача громадянства";
                  break;
                case "deprive-of-citizenship":
                  if (nickname != null) {
                    CitizenHelper.issueCitizenship(nickname);
                  }
                  code = 200;
                  response = "Позбавлення громадянства";
                  break;
                case "update-citizenship":
                  try {
                    var serverNicknames = RequestHelper.whitelist();
                    CitizenHelper.updateCitizenship(serverNicknames);

                    code = 200;
                    response = "Список громадян оновлено";

                    Bukkit.broadcastMessage(
                      ChatColor.GREEN + response
                    );
                  } catch (Exception exception) {
                    response = "Помилка оновлення списку громадян";

                    Bukkit.broadcastMessage(
                      ChatColor.RED + response
                    );
                  }
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
