package com.zyunsoftware.capydevmc.app;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.Server;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.ShapedRecipe;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

import com.zyunsoftware.capydevmc.app.actions.commands.ApiCommand;
import com.zyunsoftware.capydevmc.app.actions.commands.ReloadConfigCommand;
import com.zyunsoftware.capydevmc.app.actions.listeners.AuthorizationListener;
import com.zyunsoftware.capydevmc.app.actions.listeners.ChatListener;
import com.zyunsoftware.capydevmc.app.actions.listeners.JailListener;
import com.zyunsoftware.capydevmc.app.actions.listeners.PetOwnershipListener;
import com.zyunsoftware.capydevmc.infrastructure.Api;

import net.kyori.adventure.text.Component;

public class CapylandPlugin extends JavaPlugin {
  private static CapylandPlugin _instancePlugin;
  private Map<String, List<String>> playerIPs = new HashMap<>();

  public boolean hasIP(String playerName, String ipAddress) {
    if (playerIPs.containsKey(playerName)) {
      List<String> ips = playerIPs.get(playerName);
      return ips.contains(ipAddress);
    }
    return false;
  }

  public CapylandPlugin() {
    saveDefaultConfig();
    _instancePlugin = this;
  }

  private Map<String, List<String>> _loadPlayerIPs() {
    Map<String, List<String>> playerIPs = new HashMap<>();

    try (
      Connection connection = Api.getMysqlConnection();
      Statement statement = connection.createStatement()
    ) {
      String query = "SELECT u.nickname, s.ip_address FROM capylandbot_users u " +
        "JOIN capylandbot_sessions s ON u.id = s.user_id";

      try (ResultSet resultSet = statement.executeQuery(query)) {
        while (resultSet.next()) {
          String nickname = resultSet.getString("nickname");
          String ipAddress = resultSet.getString("ip_address");

          playerIPs.computeIfAbsent(nickname, k -> new ArrayList<>()).add(ipAddress);
        }
      }
      connection.close();
    } catch (SQLException e) {
      e.printStackTrace();
    }

    return playerIPs;
  }

  @Override
  public void onEnable() {
    playerIPs = _loadPlayerIPs();

    PluginManager pluginManager = getServer().getPluginManager();

    pluginManager.registerEvents(new AuthorizationListener(), this);
    pluginManager.registerEvents(new ChatListener(), this);
    pluginManager.registerEvents(new PetOwnershipListener(), this);
    pluginManager.registerEvents(new JailListener(), this);

    getCommand("перезавантажити_конфігураційний_файл").setExecutor(new ReloadConfigCommand());
    getCommand("api").setExecutor(new ApiCommand());

    Server server = CapylandPlugin.getInstance().getServer();
    server.addRecipe(_shocker());
    server.addRecipe(_handcuff());
    server.addRecipe(_key());
    server.addRecipe(_battery());

    new BukkitRunnable() {
      @Override
      public void run() {
        playerIPs = _loadPlayerIPs();

        for (Player player : Bukkit.getOnlinePlayers()) {
          if (!CapylandPlugin.getInstance().hasIP(player.getName(), player.getAddress().getAddress().getHostAddress())) {
            player.kick(Component.text("§cЦю сесію було видалено"));
          }
        }
      }
    }.runTaskTimer(this, 0L, 20L);
  }

  public static CapylandPlugin getInstance() {
    return _instancePlugin;
  }

  private ShapedRecipe _shocker() {
    ItemStack customItem = new ItemStack(Material.PAPER);

    List<Component> lore = new ArrayList<>();
    lore.add(Component.text("Шокер"));
    lore.add(Component.text("Заряд шокера: 0"));

    ItemMeta meta = customItem.getItemMeta();
    meta.displayName(Component.text("Шокер"));
    meta.lore(lore);

    customItem.setItemMeta(meta);

    NamespacedKey key = new NamespacedKey(this, "custom_shocker");
    ShapedRecipe recipe = new ShapedRecipe(key, customItem);

    recipe.shape("ABA", "C C", "CDC");

    recipe.setIngredient('A', Material.AMETHYST_SHARD);
    recipe.setIngredient('B', Material.LIGHTNING_ROD);
    recipe.setIngredient('C', Material.DRIED_KELP_BLOCK);
    recipe.setIngredient('D', Material.COPPER_BLOCK);

    return recipe;
  }

  private ShapedRecipe _handcuff() {
    ItemStack customItem = new ItemStack(Material.PAPER);

    List<Component> lore = new ArrayList<>();
    lore.add(Component.text("Наручники"));

    ItemMeta meta = customItem.getItemMeta();
    meta.displayName(Component.text("Наручники"));
    meta.lore(lore);

    customItem.setItemMeta(meta);

    NamespacedKey key = new NamespacedKey(this, "custom_handcuff");
    ShapedRecipe recipe = new ShapedRecipe(key, customItem);

    recipe.shape("A A", "BCB", "A A");

    recipe.setIngredient('A', Material.IRON_INGOT);
    recipe.setIngredient('B', Material.IRON_NUGGET);
    recipe.setIngredient('C', Material.CHAIN);

    return recipe;
  }

  private ShapedRecipe _key() {
    ItemStack customItem = new ItemStack(Material.PAPER);

    List<Component> lore = new ArrayList<>();
    lore.add(Component.text("Ключ"));

    ItemMeta meta = customItem.getItemMeta();
    meta.displayName(Component.text("Ключ"));
    meta.lore(lore);

    customItem.setItemMeta(meta);

    NamespacedKey key = new NamespacedKey(this, "custom_key");
    ShapedRecipe recipe = new ShapedRecipe(key, customItem);

    recipe.shape("##B", "#B#", "A##");

    recipe.setIngredient('A', Material.SHEARS);
    recipe.setIngredient('B', Material.BLAZE_ROD);

    return recipe;
  }

  private ShapedRecipe _battery() {
    ItemStack customItem = new ItemStack(Material.PAPER);

    List<Component> lore = new ArrayList<>();
    lore.add(Component.text("Батарейка"));

    ItemMeta meta = customItem.getItemMeta();
    meta.displayName(Component.text("Батарейка"));
    meta.lore(lore);

    customItem.setItemMeta(meta);

    NamespacedKey key = new NamespacedKey(this, "custom_battery");
    ShapedRecipe recipe = new ShapedRecipe(key, customItem);

    recipe.shape("ABA", "ACA", "ADA");

    recipe.setIngredient('A', Material.IRON_INGOT);
    recipe.setIngredient('B', Material.NETHERITE_UPGRADE_SMITHING_TEMPLATE);
    recipe.setIngredient('C', Material.AMETHYST_SHARD);
    recipe.setIngredient('D', Material.COPPER_INGOT);

    return recipe;
  }
}
