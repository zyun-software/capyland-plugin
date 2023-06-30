package net.ziozyun.capyland.listeners;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import javax.imageio.ImageIO;

import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.entity.ItemFrame;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerLoginEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.meta.MapMeta;
import org.bukkit.map.MapCanvas;
import org.bukkit.map.MapRenderer;
import org.bukkit.map.MapView;
import org.bukkit.plugin.java.JavaPlugin;

import net.md_5.bungee.api.ChatColor;
import net.ziozyun.capyland.helpers.RequestHelper;
import net.ziozyun.capyland.helpers.UserHelper;

public class OnPlayerInteractListener implements Listener {
  private JavaPlugin _plugin;
  private Map<String, MapRenderer> _dictionary = new HashMap<>();
  private String _needAuth = ChatColor.GOLD + "Необхідна авторизація в Капіботі";

  public OnPlayerInteractListener(JavaPlugin plugin) {
    _plugin = plugin;
  }

  @EventHandler
  public void onPlayerLogin(PlayerLoginEvent event) {
      var nickname = event.getPlayer().getName();
      try {
        var whitelist = RequestHelper.whitelist();
        if (!whitelist.contains(nickname)) {
          event.disallow(
            PlayerLoginEvent.Result.KICK_OTHER,
            ChatColor.RED + "У вас відсутнє громадянство"
          );
        }
      } catch (Exception e) {
        e.printStackTrace();
        event.disallow(
          PlayerLoginEvent.Result.KICK_OTHER,
          ChatColor.DARK_RED + "Виникла помилка під час отримання списку громадян"
        );
      }
  }

  @EventHandler
  public void onPlayerJoin(PlayerJoinEvent event) {
    var player = event.getPlayer();

    var ip = player.getAddress().getAddress().getHostAddress();
    var nickname = player.getName();

    event.setJoinMessage(ChatColor.GOLD + nickname + ChatColor.YELLOW + " завітав на Долину Капібар");

    if (!UserHelper.exists(ip, nickname)) {
      player.setGameMode(GameMode.SPECTATOR);
      player.sendMessage(_needAuth);
      try {
        RequestHelper.sendAuthorizeRequest(nickname);
      } catch (Exception e) {
        Bukkit.getScheduler().runTaskLater(_plugin, () -> {
          player.kickPlayer(ChatColor.RED + "Не вдалося відправити запит на авторизацію в Капібота");
        }, 20L);
      }
    }
  }

  @EventHandler
  public void onPlayerQuit(PlayerQuitEvent event) {
    event.setQuitMessage(ChatColor.GOLD + event.getPlayer().getName() + ChatColor.YELLOW + " покинув Долину Капібар");
  }

  @EventHandler
  public void onPlayerMove(PlayerMoveEvent event) {
    var player = event.getPlayer();

    if (_lock(player)) {
      player.sendMessage(_needAuth);
      event.setCancelled(true);
    }
  }

  @EventHandler
  public void onPlayerChat(AsyncPlayerChatEvent event) {
    var player = event.getPlayer();

    if (_lock(player)) {
      event.setCancelled(true);
    }
  }

  @EventHandler
  public void onPlayerInteractEntity(PlayerInteractEntityEvent event) {
    if (!(event.getRightClicked() instanceof ItemFrame)) {
      return;
    }

    var itemFrame = (ItemFrame) event.getRightClicked();
    var frameItem = itemFrame.getItem();

    if (!(frameItem.getItemMeta() instanceof MapMeta)) {
      return;
    }

    var mapMeta = (MapMeta) frameItem.getItemMeta();
    var mapView = mapMeta.getMapView();

    var name = mapMeta.getDisplayName();

    try {
      var numbers = RequestHelper.financialAccounts();

      var found = false;
      for (var number : numbers) {
        if (number.equals(name)) {
          found = true;
          break;
        }
      }

      if (!found) {
        return;
      }

      if (!_dictionary.containsKey(name)) {
        var renderer = new MapRenderer() {
          @Override
          public void render(MapView mapView, MapCanvas mapCanvas, Player player) {
            var qrCodeImage = _generateQRCode(name);
            mapCanvas.drawImage(0, 0, qrCodeImage);
          }
        };

        _dictionary.put(name, renderer);
      }

      var renderer = _dictionary.get(name);

      var renderers = mapView.getRenderers();

      if (renderers.contains(renderer)) {
        return;
      }

      for (var item : renderers) {
        mapView.removeRenderer(item);
      }

      mapView.addRenderer(renderer);
    } catch (Exception e) {
      e.printStackTrace();
      return;
    }
  }

  private static boolean _lock(Player player) {
    var ip = player.getAddress().getAddress().getHostAddress();
    var nickname = player.getName();

    var result = player.getGameMode() == GameMode.SPECTATOR && !UserHelper.exists(ip, nickname);
    return result;
  } 

  private static BufferedImage _generateQRCode(String text) {
    var imageUrl = "https://api.qrserver.com/v1/create-qr-code/?size=128x128&data=" + text;

    try {
      var url = new URL(imageUrl);
      var image = ImageIO.read(url);

      return image;
    } catch (IOException e) {
      e.printStackTrace();
      return null;
    }
  }
}
