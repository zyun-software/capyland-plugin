package com.zyunsoftware.capydevmc.app.utilities;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;

public class ItemUtility {
  public static ItemMeta getMetaFromCustomKnowlageBook(ItemStack item) {
    if (
      item == null ||
      item.getType() != Material.KNOWLEDGE_BOOK ||
      !item.hasItemMeta()
    ) {
      return null;
    }

    ItemMeta meta = item.getItemMeta();

    if (
      !meta.hasDisplayName() ||
      !meta.hasLore()
    ) {
      return null;
    }

    return meta;
  }

  public static boolean compareDisplayNames(String displayName1, String displayName2) {
    String cleanStr1 = displayName1.replaceAll("[&§]", "");
    String cleanStr2 = displayName2.replaceAll("[&§]", "");

    return cleanStr1.equals(cleanStr2);
  }

  public static ItemStack createCustomItemStack(
    String displayName,
    List<String> loreLines,
    Material material
  ) {
    ItemStack itemStack = new ItemStack(material);
    ItemMeta itemMeta = itemStack.getItemMeta();

    if (displayName != null && !displayName.isEmpty()) {
      itemMeta.displayName(Component.text(displayName));
    }

    if (loreLines != null && !loreLines.isEmpty()) {
      List<Component> lore = new ArrayList<>();

      for (String line : loreLines) {
        lore.add(Component.text(line));
      }

      itemMeta.lore(lore);
    }

    itemStack.setItemMeta(itemMeta);

    return itemStack;
  }

  public static boolean isInteractWithCustomKnowlageBook(PlayerInteractEvent event, String displayName) {
    Player player = event.getPlayer();

    if (event.getAction() != Action.RIGHT_CLICK_AIR && event.getAction() != Action.RIGHT_CLICK_BLOCK) {
      return false;
    }

    ItemStack heldItem = player.getInventory().getItemInMainHand();

    if (heldItem.getType() != Material.KNOWLEDGE_BOOK) {
      return false;
    }

    ItemMeta itemMeta = ItemUtility.getMetaFromCustomKnowlageBook(heldItem);
    
    if (itemMeta == null) {
      return false;
    }

    String itemMetaDisplayName = LegacyComponentSerializer.legacyAmpersand()
      .serialize(itemMeta.displayName());

    if (compareDisplayNames(itemMetaDisplayName, displayName)) {
      return true;
    }

    return false;
  }
}
