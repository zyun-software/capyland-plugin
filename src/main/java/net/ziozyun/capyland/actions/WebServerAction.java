package net.ziozyun.capyland.actions;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.logging.Logger;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.plugin.Plugin;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;

import net.ziozyun.capyland.Main;
import net.ziozyun.capyland.helpers.RequestHelper;
import net.ziozyun.capyland.helpers.UserHelper;

public class WebServerAction {
  private HttpServer httpServer;
  private Logger logger;
  private int port;
  private String token;
  private boolean isTest;

  public WebServerAction(Plugin plugin, boolean isTest) {
    logger = plugin.getLogger();

    var config = plugin.getConfig();
    port = config.getInt("port", 8080);
    token = config.getString("token", "");
    this.isTest = isTest;
  }

  public void startServer() {
    try {
      httpServer = HttpServer.create(new InetSocketAddress(port), 0);
      httpServer.createContext("/send-command", exchange -> {
        Bukkit.getScheduler().runTaskLater(Main.getInstance(), () -> {
          String response;
          var code = 400;

          try {
            var token = _getRequestParam(exchange, "token");
            var command = _getRequestParam(exchange, "command");
            var nickname = _getRequestParam(exchange, "nickname");

            var player = Bukkit.getPlayer(nickname);

            if (!WebServerAction.this.token.equals(token)) {
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
                    var gameMode = isTest ? GameMode.CREATIVE : GameMode.SURVIVAL;
                    player.setGameMode(gameMode);
                    player.setOp(isTest);
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
        }, 0);
      });

      httpServer.setExecutor(null);
      httpServer.start();

      logger.info("Веб-сервер запущений на порту " + port);
    } catch (IOException e) {
      e.printStackTrace();
      logger.warning("Помилка запуску веб-сервера: " + e.getMessage());
    }
  }

  public void stopServer() {
    if (httpServer != null) {
      httpServer.stop(0);
      logger.info("Веб-сервер зупинений");
    }
  }

  private String _getRequestParam(HttpExchange exchange, String paramName) {
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
}
