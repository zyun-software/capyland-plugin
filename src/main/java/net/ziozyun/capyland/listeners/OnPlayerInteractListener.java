package net.ziozyun.capyland.listeners;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import javax.imageio.ImageIO;

import org.bukkit.GameMode;
import org.bukkit.entity.ItemFrame;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.inventory.meta.MapMeta;
import org.bukkit.map.MapCanvas;
import org.bukkit.map.MapRenderer;
import org.bukkit.map.MapView;

import net.md_5.bungee.api.ChatColor;
import net.ziozyun.capyland.helpers.RequestHelper;
import net.ziozyun.capyland.helpers.UserHelper;

public class OnPlayerInteractListener implements Listener {
  private Map<String, MapRenderer> _dictionary = new HashMap<>();

  @EventHandler
  public void onPlayerJoin(PlayerJoinEvent event) {
    var player = event.getPlayer();

    var ip = player.getAddress().getAddress().getHostAddress();
    var nickname = player.getName();

    if (!UserHelper.exists(ip, nickname)) {
      player.setGameMode(GameMode.SPECTATOR);
    }

    if (player.getGameMode() == GameMode.SPECTATOR) {
      player.sendMessage(ChatColor.GOLD + "Необхідна авторизація в Капіботі");
    }
  }

  @EventHandler
  public void onPlayerMove(PlayerMoveEvent event) {
    var player = event.getPlayer();
    if (player.getGameMode() == GameMode.SPECTATOR) {
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
      return;
    }
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
