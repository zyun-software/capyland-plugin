package com.zyunsoftware.capydevmc.app;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.Server;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.ShapedRecipe;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

import com.zyunsoftware.capydevmc.app.actions.commands.ApiCommand;
import com.zyunsoftware.capydevmc.app.actions.commands.ReloadConfigCommand;
import com.zyunsoftware.capydevmc.app.actions.listeners.ChatListener;
import com.zyunsoftware.capydevmc.app.actions.listeners.JailListener;
import com.zyunsoftware.capydevmc.app.actions.listeners.PetOwnershipListener;

import net.kyori.adventure.text.Component;

public class CapylandPlugin extends JavaPlugin {
  private static CapylandPlugin _instancePlugin;

  public CapylandPlugin() {
    saveDefaultConfig();
    _instancePlugin = this;
  }

  @Override
  public void onEnable() {
    PluginManager pluginManager = getServer().getPluginManager();

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
