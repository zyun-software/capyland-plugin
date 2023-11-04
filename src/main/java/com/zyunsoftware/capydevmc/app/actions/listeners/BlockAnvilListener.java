package com.zyunsoftware.capydevmc.app.actions.listeners;

import org.bukkit.Material;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryType;

public class BlockAnvilListener implements Listener {
  @EventHandler
  public void onInventoryClick(InventoryClickEvent event) {
    if (event.getInventory().getType() != InventoryType.ANVIL) {
      return;
    }

    if (
      event.getCurrentItem() != null &&
      event.getCurrentItem().getType() == Material.KNOWLEDGE_BOOK
    ) {
      event.setCancelled(true);
    }
  }
}
