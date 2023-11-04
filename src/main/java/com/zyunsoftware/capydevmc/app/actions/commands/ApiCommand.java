package com.zyunsoftware.capydevmc.app.actions.commands;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import com.zyunsoftware.capydevmc.infrastructure.utilities.ConfigUtility;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;

public class ApiCommand implements CommandExecutor, TabCompleter {
  @Override
  public boolean onCommand(
    CommandSender sender,
    Command command,
    String label,
    String[] args
  ) {
    if (!(sender instanceof Player)) {
      return false;
    }

    Player player = (Player) sender;

    if (args.length < 1) {
      player.sendMessage("§cНедостатньо аргументів");
      return true;
    }

    PlayerInventory inventory = player.getInventory();
    ItemStack itemInMainHand = inventory.getItemInMainHand();

    String api = args[0];

    if (api.equals("прибрати_лор") && player.isOp()) {
      _removeLore(itemInMainHand);
      return true;
    }
    
    String url = ConfigUtility.getString("api-chat." + api + ".url", null);
    String token = ConfigUtility.getString("api-chat." + api + ".token", null);

    if (url == null || token == null) {
      player.sendMessage("§cAPI не знайдено");
      return true;
    }

    List<String> lores = new ArrayList<>();
    for (ItemStack item : inventory.getContents()) {
      if (_isApiItem(item, api)) {
        lores.add(item.getAmount() + "&" + _getLore(item));
      }
    }

    String data =
      "command&&" +
      player.getName() + "&&" +
      (itemInMainHand == null ? "0" : itemInMainHand.getAmount()) + "&&" +
      String.join(" ", Arrays.copyOfRange(args, 1, args.length)) +
      "&&" + String.join("`", lores);

    String response = _sendPostRequest(url, token, data);

    if (response == null) {
      player.sendMessage("§cAPI не відповідає");
      return true;
    }

    if (!response.matches("^((setnbt&[^&]+|clearnbt)&&)?message&[^&]+$")) {
      player.sendMessage(response);
      player.sendMessage("§cAPI надіслав не вірну відповідь");
      return true;
    }

    String[] commands = response.split("&&");

    if (commands[0].startsWith("message&")) {
      player.sendMessage(commands[0].split("&")[1]);
      return true;
    }

    if (commands[0].startsWith("setnbt&")) {
      if (itemInMainHand == null || itemInMainHand.getType().isAir()) {
        player.sendMessage("§cПотрібно взяти предмет в руку щоб виконати цю команду API");
        return true;
      }

      if (!_getLore(itemInMainHand).isEmpty()) {
        player.sendMessage("§cУ цього предмета вже встановлені NBT теги");
        return true;
      }

      ItemMeta meta = itemInMainHand.getItemMeta();
      List<Component> lore = meta.lore();
      if (lore == null) {
        lore = new ArrayList<>();
      }

      String[] nbtLines = commands[0].split("&")[1].split("`");
      String[] selectedLines = Arrays.copyOfRange(nbtLines, 0, Math.min(9, nbtLines.length));
      for (String line : selectedLines) {
        lore.add(Component.text(line));
      }

      lore.add(Component.text(api));

      meta.lore(lore);
      itemInMainHand.setItemMeta(meta);

      player.sendMessage(commands[1].split("&")[1]);

      return true;
    }

    if (commands[0].equals("clearnbt")) {
      for (ItemStack item : inventory.getContents()) {
        if (_isApiItem(item, api)) {
          _removeLore(item);
        }
      }

      player.sendMessage(commands[1].split("&")[1]);

      return true;
    }

    player.sendMessage(response);

    return true;
  }

  @Override
  public @Nullable List<String> onTabComplete(
    @NotNull CommandSender sender,
    @NotNull Command command,
    @NotNull String alias,
    @NotNull String[] args
  ) {
    if (!(sender instanceof Player) || args.length == 0) {
      return null;
    }

    Player player = (Player) sender;
    Set<String> apiKeys = ConfigUtility.getKeys("api-chat");
    List<String> completions = new ArrayList<>();

    if (args.length == 1) {
      for (String key : apiKeys) {
        if (key.toLowerCase().startsWith(args[0].toLowerCase())) {
          completions.add(key);
        }
      }
    } else if (args.length > 1) {
      String api = args[0];
      String url = ConfigUtility.getString("api-chat." + api + ".url", null);
      String token = ConfigUtility.getString("api-chat." + api + ".token", null);

      if (url != null && token != null) {
        String data =
          "tab&&" +
          player.getName() + "&&" +
          String.join(" ", Arrays.copyOfRange(args, 1, args.length));

        String response = _sendPostRequest(url, token, data);

        if (response != null) {
          completions.addAll(Arrays.asList(response.split("&")));
        }
      }
    }

    return completions;
  }

  private void _removeLore(@Nullable ItemStack item) {
    if (item == null || item.getType().isAir()) {
      return;
    }

    ItemMeta meta = item.getItemMeta();
    if (meta == null) {
      return;
    }

    List<Component> lore = meta.lore();
    if (lore == null) {
      lore = new ArrayList<>();
    }

    lore.clear();
    meta.lore(lore);
    item.setItemMeta(meta);
  }

  private String _getLore(@Nullable ItemStack item) {
    if (item == null || item.getType().isAir()) {
      return "";
    }

    ItemMeta meta = item.getItemMeta();
    if (meta == null) {
      return "";
    }

    List<Component> lore = meta.lore();

    if (lore == null) {
      return "";
    }

    List<String> result = new ArrayList<>();
    for (int i = 0, size = lore.size(); i < size - 1; i++) {
      Component line = lore.get(i);
      result.add(LegacyComponentSerializer.legacyAmpersand()
        .serialize(line)
        .replaceAll("§.", ""));
    }

    return String.join("`", result);
  }

  private boolean _isApiItem(@Nullable ItemStack item, String api) {
    return item != null && !item.getType().isAir() && _isAPINBT(item, api) && item.getItemMeta() != null;
  }

  private boolean _isAPINBT(@Nullable ItemStack item, String api) {
    if (item == null || item.getType().isAir()) {
      return false;
    }

    ItemMeta meta = item.getItemMeta();
    if (meta == null) {
      return false;
    }

    List<Component> lore = meta.lore();

    if (lore == null || lore.isEmpty()) {
      return false;
    }

    int size = lore.size();
    return LegacyComponentSerializer.legacyAmpersand()
      .serialize(lore.get(size - 1))
      .equals(api);
  }

  private String _sendPostRequest(String url, String token, String data) {
    try {
      URI uri = new URI(url);
      HttpURLConnection connection = (HttpURLConnection) uri.toURL().openConnection();

      connection.setRequestMethod("POST");
      connection.setRequestProperty("Content-Type", "application/json");
      connection.setRequestProperty("Token", token);

      connection.setDoOutput(true);

      try (OutputStream os = connection.getOutputStream()) {
        byte[] input = data.getBytes(StandardCharsets.UTF_8);
        os.write(input, 0, input.length);
      }

      int responseCode = connection.getResponseCode();
      if (responseCode == HttpURLConnection.HTTP_OK) {
        try (BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()))) {
          StringBuilder response = new StringBuilder();
          String inputLine;
          while ((inputLine = in.readLine()) != null) {
            response.append(inputLine);
          }

          return response.toString();
        }
      }
    } catch (IOException e) {
      e.printStackTrace();
    } catch (Exception e) {
      e.printStackTrace();
    }

    return null;
  }
}
