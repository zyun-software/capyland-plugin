package net.ziozyun.capyland.listeners;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.entity.Tameable;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;

import net.md_5.bungee.api.ChatColor;

public class PetOwnershipListener implements Listener {
  @EventHandler
  public void onEntityDamageByEntity(EntityDamageByEntityEvent event) {
    var damager = event.getDamager();
    var entity = event.getEntity();

    if (!(damager instanceof Player)) {
      return;
    }

    var player = (Player) damager;

    var item = player.getInventory().getItemInMainHand();
    if (item == null || item.getType() != Material.BONE) {
      return;
    }

    var meta = item.getItemMeta();

    if (meta == null || !meta.hasDisplayName()) {
      return;
    }

    var nickname = meta.getDisplayName();

    var newOwner = Bukkit.getPlayer(nickname);

    if (newOwner == null) {
      player.sendMessage(
          ChatColor.RED + "Гравця з псевдонімом " + ChatColor.GOLD + nickname + ChatColor.RED + " не знайдено");
      return;
    }

    if (!(entity instanceof Tameable)) {
      return;
    }

    var tameable = (Tameable) entity;

    if (!player.isOp()) {
      if (!tameable.isTamed() || !(tameable.getOwner() instanceof Player)) {
        return;
      }

      var owner = (Player) tameable.getOwner();
      if (!owner.getUniqueId().equals(player.getUniqueId())) {
        player.sendMessage(
            ChatColor.RED + "Ви не являєтесь власником цієї тварини");
        return;
      }
    }

    event.setCancelled(true);

    tameable.setTamed(true);
    tameable.setOwner(newOwner);

    player.sendMessage(ChatColor.GREEN + "Ви передали право власності на тварину " + ChatColor.GOLD + nickname);
    newOwner.sendMessage(ChatColor.GREEN + "Вам " + ChatColor.GOLD + player.getName() + ChatColor.GREEN
        + " передав право власності на тварину");
  }
}
