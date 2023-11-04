package com.zyunsoftware.capydevmc.app.actions.listeners;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryView;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.ShapedRecipe;
import org.bukkit.inventory.meta.ItemMeta;

import com.zyunsoftware.capydevmc.app.CapylandPlugin;
import com.zyunsoftware.capydevmc.app.DependencyInjection;
import com.zyunsoftware.capydevmc.app.utilities.ItemUtility;
import com.zyunsoftware.capydevmc.app.utilities.StringUtility;
import com.zyunsoftware.capydevmc.domain.models.user.UserEntity;
import com.zyunsoftware.capydevmc.domain.models.user.UserModel;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;

public class BankingListener implements Listener {
  @EventHandler
  public void onPlayerInteract(PlayerInteractEvent event) {
    if (ItemUtility.isInteractWithCustomKnowlageBook(event, getDisplayName())) {
      event.setCancelled(true);

      Block block = event.getClickedBlock();

      if (block != null && block.getType() == Material.ENDER_CHEST) {
        Player player = event.getPlayer();

        _openMainMenuInventoty(player);
      }
    }

    if (ItemUtility.isInteractWithCustomKnowlageBook(event, _getOrderDisplayName())) {
      Player player = event.getPlayer();
      ItemStack heldItem = player.getInventory().getItemInMainHand();

      ArrayList<Component> list = new ArrayList<>(heldItem.lore());
      if (list.size() >= 4) {
        Component fourthElement = list.get(3);
        String code = LegacyComponentSerializer.legacyAmpersand()
          .serialize(fourthElement);
        
        player.sendMessage(code);
      }

      event.setCancelled(true);
    }
  }

  @EventHandler
  public void onInventoryOpen(InventoryOpenEvent event) {
    // Player player = (Player) event.getPlayer();

    // Логіка обробки
  }

  @EventHandler
  public void onInventoryClick(InventoryClickEvent event) {
    Player player = (Player) event.getWhoClicked();

    Inventory clickedInventory = event.getClickedInventory();

    if (clickedInventory == null || !clickedInventory.equals(player.getOpenInventory().getTopInventory())) {
      return;
    }

    InventoryView view = event.getView();
    String viewTitle = LegacyComponentSerializer.legacyAmpersand()
      .serialize(view.title());

    if (viewTitle.equals(_getMainMenuTitle())) {
      ItemStack cursorItem = player.getItemOnCursor();
      ItemMeta itemMeta = cursorItem.getItemMeta();
      if (
        cursorItem != null &&
        cursorItem.getType() == Material.PAPER &&
        cursorItem.getAmount() == 1 &&
        itemMeta != null
      ) {
        String itemMetaDisplayName = LegacyComponentSerializer.legacyAmpersand()
          .serialize(itemMeta.displayName());
        
        String nickname = player.getName();

        UserEntity userEntity = DependencyInjection.getUserRepository()
          .findByNickname(nickname);

        UserModel userModel = userEntity.getModel();
        
        Integer amount = StringUtility.findIntegerInString(itemMetaDisplayName);
        if (amount != null && amount > 0) {
          if (userModel.balance >= amount) {
            userEntity.changeBalance(-amount);
            ItemStack order = ItemUtility.createCustomItemStack(
              _getOrderDisplayName(),
              Arrays.asList(
                "§7Сума: §6" + amount,
                "§7Виписав: §6" + nickname,
                "§7Дійсний до: §623 грудня 2023",
                "§81634530800123"
              ),
              Material.KNOWLEDGE_BOOK
            );
            HashMap<Integer, ItemStack> remainingItems = player.getInventory().addItem(order);
            if (remainingItems.isEmpty()) {
              player.setItemOnCursor(null);
            } else {
              userEntity.changeBalance(amount);
            }
            _openMainMenuInventoty(player);
          } else {
            player.sendMessage("§cУ вас на балансі недостатньо коштів для створення чеку на вказану суму");
          }
        }
      }

      event.setCancelled(true);
    }
  }

  private String _getMainMenuTitle() {
    return "Національний банк";
  }

  private void _openMainMenuInventoty(Player player) {
    Inventory customInventory = Bukkit
      .createInventory(null, 9, Component.text(_getMainMenuTitle()));

    UserEntity userEntity = DependencyInjection.getUserRepository()
      .findByNickname(player.getName());

    UserModel userModel = userEntity.getModel();

    customInventory.setItem(3, ItemUtility.createCustomItemStack(
      "Баланс",
      Arrays.asList(
        "§7Капікоїни: §6" + userModel.balance,
        "§7Дукати: §6" + userModel.credits
      ),
      Material.GOLD_INGOT
    ));
    customInventory.setItem(5, ItemUtility.createCustomItemStack(
      "Виписати чек",
      Arrays.asList(
        "§7Для того щоб виписати"
      ),
      Material.PAPER
    ));

    player.openInventory(customInventory);
  }

  public static void addRecipe() {
    ItemStack knowledgeBook = ItemUtility.createCustomItemStack(
      getDisplayName(),
      Arrays.asList(
        "§eНаціональний банк §6Долини Капібар",
        "§7Для взаємодії натисніть на скриню енду"
      ),
      Material.KNOWLEDGE_BOOK
    );

    NamespacedKey recipeKey = new NamespacedKey(
      CapylandPlugin.getInstance(),
      "capyland_card_recipe"
    );
    ShapedRecipe recipe = new ShapedRecipe(recipeKey, knowledgeBook);
    recipe.shape("ABC", "DEF", "GHI");
    recipe.setIngredient('A', Material.IRON_INGOT);
    recipe.setIngredient('B', Material.COPPER_INGOT);
    recipe.setIngredient('C', Material.GOLD_INGOT);
    recipe.setIngredient('D', Material.AMETHYST_SHARD);
    recipe.setIngredient('E', Material.ENDER_EYE);
    recipe.setIngredient('F', Material.REDSTONE);
    recipe.setIngredient('G', Material.STONE_BUTTON);
    recipe.setIngredient('H', Material.LIGHTNING_ROD);
    recipe.setIngredient('I', Material.PAPER);

    Bukkit.addRecipe(recipe);
  }

  private String _getOrderDisplayName() {
    return "§6§lЧек";
  }

  public static String getDisplayName() {
    return "§6§lКапіКредит";
  }
}
