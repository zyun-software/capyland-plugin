package net.ziozyun.capyland.listeners;

import org.bukkit.Material;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.PrepareAnvilEvent;

public class JailListener implements Listener {
  @EventHandler
  public void onPrepareAnvil(PrepareAnvilEvent event) {
    if (event.getInventory().contains(Material.NETHER_STAR)) {
      event.setResult(null);
    }
  }

  /*
   * @EventHandler
   * public void onPlayerItemHeld(PlayerItemHeldEvent event) {
   * var player = event.getPlayer();
   * var item = player.getInventory().getItemInMainHand();
   * 
   * if (item.getType() != Material.NETHER_STAR) {
   * return;
   * }
   * 
   * var meta = item.getItemMeta();
   * 
   * meta.setDisplayName("Знерухомувач");
   * 
   * var lore = meta.getLore();
   * if (lore == null) {
   * lore = new ArrayList<>();
   * }
   * 
   * lore.add("Доступно: " + ChatColor.GOLD + ChatColor.BOLD + 30);
   * 
   * meta.setLore(lore);
   * 
   * item.setItemMeta(meta);
   * }
   */
}
