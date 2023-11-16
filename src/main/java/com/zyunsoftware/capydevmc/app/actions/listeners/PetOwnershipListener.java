package com.zyunsoftware.capydevmc.app.actions.listeners;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Tameable;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;

public class PetOwnershipListener implements Listener {
  @EventHandler
  public void onEntityDamageByEntity(EntityDamageByEntityEvent event) {
    Entity damager = event.getDamager();
    Entity entity = event.getEntity();

    if (!(damager instanceof Player)) {
      return;
    }

    Player player = (Player) damager;

    ItemStack item = player.getInventory().getItemInMainHand();
    if (item == null || item.getType() != Material.BONE) {
      return;
    }

    ItemMeta meta = item.getItemMeta();

    if (meta == null || !meta.hasDisplayName()) {
      return;
    }

    String nickname = LegacyComponentSerializer
      .legacyAmpersand()
      .serialize(meta.displayName());

    Player newOwner = Bukkit.getPlayer(nickname);

    if (newOwner == null) {
      player.sendMessage(
        "§cГравця з псевдонімом §6" + nickname + "§c не знайдено");
      return;
    }

    if (!(entity instanceof Tameable)) {
      return;
    }

    Tameable tameable = (Tameable) entity;

    if (!player.isOp()) {
      if (!tameable.isTamed() || !(tameable.getOwner() instanceof Player)) {
        return;
      }

      Player owner = (Player) tameable.getOwner();
      if (!owner.getUniqueId().equals(player.getUniqueId())) {
        player.sendMessage("§cВи не являєтесь власником цієї тварини");
        return;
      }
    }

    event.setCancelled(true);

    tameable.setTamed(true);
    tameable.setOwner(newOwner);

    player.sendMessage("§aВи передали право власності на тварину §6" + nickname);
    newOwner.sendMessage("§aВам §6" + player.getName() + "§a передав право власності на тварину");
  }
}
