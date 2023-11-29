package com.zyunsoftware.capydevmc.app.actions.listeners;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.annotation.Nullable;

import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.inventory.PrepareAnvilEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.util.Vector;

import com.zyunsoftware.capydevmc.app.CapylandPlugin;
import com.zyunsoftware.capydevmc.app.utilities.FileUtility;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;

public class JailListener implements Listener {
  private List<String> _locked = new ArrayList<>();
  private List<String> _bound;
  private List<String> _jail;

  private List<PotionEffectType> _potionEffectTypes = new ArrayList<>();

  private String _jailFileName = "jail.txt";
  private String _boundFileName = "bound.txt";

  public static int fullAvailable = 30;
  public static int lockSeconds = 30;
  public static int distance = 10;

  public JailListener() {
    _jail = FileUtility.readAsList(_jailFileName);
    _bound = FileUtility.readAsList(_boundFileName);

    _potionEffectTypes.add(PotionEffectType.BLINDNESS);
    _potionEffectTypes.add(PotionEffectType.CONFUSION);
    _potionEffectTypes.add(PotionEffectType.SLOW_DIGGING);
    _potionEffectTypes.add(PotionEffectType.SLOW);
  }
  
  @EventHandler
  public void onAnvilPrepare(PrepareAnvilEvent event) {
    ItemStack[] items = event.getInventory().getContents();

    ItemStack result = determineAnvilResult(items);

    if (result.getType() != Material.AIR) {
      event.setResult(result);
    }
  }

  @EventHandler
  public void onInventoryClick(InventoryClickEvent event) {
    if (event.getInventory().getType() == InventoryType.ANVIL) {
      if (event.getSlotType() == InventoryType.SlotType.RESULT) {
        ItemStack resultItem = event.getCurrentItem();
        if (resultItem != null && resultItem.getType() == Material.PAPER && _getFl(resultItem).equals("Шокер")) {
          ItemMeta meta = resultItem.getItemMeta();
          if (
            meta.hasDisplayName() &&
            LegacyComponentSerializer
              .legacyAmpersand()
              .serialize(meta.displayName())
              .equals("Шокер")) {
            event.setCancelled(true);
            event.getWhoClicked().getInventory().addItem(resultItem);
            event.getInventory().clear();
          }
        }
      }
    }
  }

  private ItemStack determineAnvilResult(ItemStack[] items) {
    if (
      items[0] != null && items[1] != null &&
      _getFl(items[0]).equals("Шокер") &&
      _getFl(items[1]).equals("Батарейка")
    ) {
      ItemStack customItem = new ItemStack(Material.PAPER);

      List<Component> lore = new ArrayList<>();
      lore.add(Component.text("Шокер"));
      lore.add(Component.text("Заряд шокера: " + fullAvailable));

      ItemMeta meta = customItem.getItemMeta();
      meta.displayName(Component.text("Шокер"));
      meta.lore(lore);

      customItem.setItemMeta(meta);
      return customItem;
    } else {
      return new ItemStack(Material.AIR);
    }
  }

  private String _getFl(@Nullable ItemStack item) {
    if (item == null) {
      return "";
    }

    ItemMeta meta = item.getItemMeta();
    if (meta == null) {
      return "";
    }

    List<Component> lore = meta.lore();
    if (lore == null || lore.size() < 1) {
      return "";
    }

    String fl = LegacyComponentSerializer.legacyAmpersand()
      .serialize(lore.get(0));

    return fl;
  }

  @EventHandler
  public void onEntityDamage(EntityDamageEvent event) {
    if (!(event.getEntity() instanceof Player)) {
      return;
    }

    Player player = (Player) event.getEntity();
    if (_isLocked(player)) {
      event.setCancelled(true);

      return;
    }
  }

  @EventHandler
  public void onEntityDamageByEntity(EntityDamageByEntityEvent event) {
      if (!(event.getDamager() instanceof Player) || !(event.getEntity() instanceof Player)) {
      return;
    }

    Player attacker = (Player) event.getDamager();
    Player target = (Player) event.getEntity();

    String attackerNickname = attacker.getName();
    String targetNickname = target.getName();

    String pair = attackerNickname + ":" + targetNickname;

    if (_isLocked(attacker)) {
      event.setCancelled(true);
      return;
    }

    ItemStack item = attacker.getInventory().getItemInMainHand();
    ItemMeta meta = item.getItemMeta();
    List<Component> lore = meta.lore();

    if (
      !_isMaterialInMainHand(attacker, Material.PAPER) ||
      lore.size() == 0 || item == null || item.getAmount() != 1 ||
      meta == null || lore == null) {
      return;
    }

    String fl = LegacyComponentSerializer.legacyAmpersand()
      .serialize(lore.get(0));

    if (lore.size() == 1 && fl.equals("Ключ") && _isBoundTarget(targetNickname)) {
      event.setCancelled(true);

      List<String> toRemove = new ArrayList<>();
      for (String boundEntry : _bound) {
        String[] nicknames = boundEntry.split(":");
        if (nicknames.length == 2 && nicknames[1].equals(targetNickname)) {
          toRemove.add(boundEntry);
        }
      }

      for (String itemq : toRemove) {
        _bound.remove(itemq);
      }

      FileUtility.write(_boundFileName, _bound);

      _playSound(attacker, Sound.ENTITY_SHEEP_SHEAR);
      _playSound(target, Sound.ENTITY_SHEEP_SHEAR);

      attacker.sendMessage("§aВи зняли наручники з §6" + targetNickname);
      target.sendMessage("§eЗ вас зняв наручники §6" + attackerNickname);
      return;
    }

    if (lore.size() == 1 && fl.equals("Наручники")
        && (_locked.contains(targetNickname) || _jail.contains(targetNickname))) {
      event.setCancelled(true);
      _clearHeldItemSlot(attacker);
      _locked.remove(targetNickname);
      if (!_bound.contains(pair)) {
        _bound.add(pair);
        FileUtility.write(_boundFileName, _bound);
      }

      _playSound(attacker, Sound.ENTITY_LEASH_KNOT_PLACE);
      _playSound(target, Sound.ENTITY_LEASH_KNOT_PLACE);

      attacker.sendMessage("§aВи надягли наручники на §6" + targetNickname);
      target.sendMessage("§eНа вас надяг наручники §6" + attackerNickname);

      return;
    }

    if (lore.size() != 2 || _isLocked(target) || !fl.equals("Шокер")) {
      return;
    }

    String rep = "Заряд шокера: ";

    int available = Integer.parseInt(LegacyComponentSerializer
      .legacyAmpersand()
      .serialize(lore.get(1)).replace(rep, "")) - 1;

    lore = new ArrayList<>();
    lore.add(Component.text("Шокер"));
    lore.add(Component.text(rep + available));

    meta.lore(lore);
    item.setItemMeta(meta);

    if (available < 1) {
      _playSound(attacker, Sound.ENTITY_ITEM_BREAK);

      attacker.sendMessage("§eЗарядіть шокер");

      return;
    }

    event.setCancelled(true);

    if (!_locked.contains(targetNickname)) {
      _locked.add(targetNickname);
    }

    int duration = 20 * lockSeconds;

    Bukkit.getScheduler().runTaskLater(CapylandPlugin.getInstance(), () -> {
      _locked.remove(targetNickname);
    }, duration);

    _playSound(attacker, Sound.ENTITY_LIGHTNING_BOLT_IMPACT);
    _playSound(target, Sound.ENTITY_LIGHTNING_BOLT_IMPACT);

    Collection<PotionEffect> activeEffects = target.getActivePotionEffects();
    for (PotionEffect effect : activeEffects) {
      PotionEffectType potionEffectType = effect.getType();
      if (_potionEffectTypes.contains(potionEffectType)) {
        target.removePotionEffect(potionEffectType);
      }
    }

    for (PotionEffectType potionEffectType : _potionEffectTypes) {
      PotionEffect potionEffect = new PotionEffect(potionEffectType, duration, 0);
      target.addPotionEffect(potionEffect);
    }

    attacker.sendMessage("§aВи оглушили §6" + targetNickname);
    target.sendMessage("§eВас оглушив §6" + attackerNickname);
  }

  @EventHandler
  public void onPlayerJoin(PlayerJoinEvent event) {
    Player player = event.getPlayer();
    String nickname = player.getName();

    if (_locked.contains(nickname)) {
      player.sendMessage("§eВи оглушені");
    }

    if (_isBoundTarget(nickname)) {
      player.sendMessage("§eВи зв'язані");
    }

    if (_jail.contains(nickname)) {
      player.sendMessage("§eВи у в'язниці");
    }
  }

  @EventHandler
  public void onPlayerQuit(PlayerQuitEvent event) {
    Player player = event.getPlayer();
    String nickname = player.getName();

    if (_locked.contains(nickname)) {
      if (!_isBoundTarget(nickname)) {
        _bound.add(nickname + ":" + nickname);
        FileUtility.write(_boundFileName, _bound);
      }
    }
  }

  @EventHandler
  public void onPlayerMove(PlayerMoveEvent event) {
    Player player = event.getPlayer();
    String nickname = player.getName();

    if (_isLocked(player)) {
      event.setCancelled(true);
      return;
    }

    if (!_jail.contains(nickname)) {
      return;
    }

    if (_isBlockUnder(player, Material.IRON_BLOCK, distance)) {
      return;
    }

    player.setGameMode(GameMode.SURVIVAL);
    _jail.remove(player.getName());
    FileUtility.write(_jailFileName, _jail);
    player.sendMessage("§eВи вийшли з в'язниці");
  }

  @EventHandler
  public void onPlayerInteract(PlayerInteractEvent event) {
    Player player = event.getPlayer();
    String nickname = player.getName();

    if (_isLocked(player)) {
      event.setCancelled(true);
      return;
    }

    if (!event.getAction().toString().contains("RIGHT")) {
      return;
    }

    if (!_isMaterialInMainHand(player, Material.LEAD)) {
      return;
    }

    Entity entity = (Entity) player;

    boolean canTeleport = entity.isOnGround();

    if (!canTeleport) {
      player.sendMessage("§cСюди не можна перемістити в'язня(-ів)");
    }

    for (String boundEntry : _bound) {
      String[] nicknames = boundEntry.split(":");
      if (nicknames.length == 2 && nicknames[0].equals(nickname)) {
        Player prisoner = Bukkit.getPlayer(nicknames[1]);
        if (prisoner != null) {
          if (canTeleport) {
            _teleportPlayerToPlayer(prisoner, player);
            Location location = player.getLocation();
            prisoner.setBedSpawnLocation(location, true);
            if (_isBlockUnder(prisoner, Material.IRON_BLOCK, distance)) {
              prisoner.setGameMode(GameMode.ADVENTURE);
              String name = prisoner.getName();
              if (!_jail.contains(name)) {
                _jail.add(name);
                FileUtility.write(_jailFileName, _jail);
              }
              prisoner.sendMessage("§eВи потрапили до в'язниці");
            }
          }
        }
      }
    }
  }

  @EventHandler
  public void onPlayerInteractEntity(PlayerInteractEntityEvent event) {
    Player player = event.getPlayer();

    if (_isLocked(player)) {
      event.setCancelled(true);
    }
  }

  private boolean _isLocked(Player player) {
    String nickname = player.getName();
    boolean result = _locked.contains(nickname) || _isBoundTarget(nickname);

    return result;
  }

  private boolean _isBoundTarget(String nickname) {
    for (String boundEntry : _bound) {
      String[] nicknames = boundEntry.split(":");
      if (nicknames.length == 2 && nicknames[1].equals(nickname)) {
        return true;
      }
    }

    return false;
  }

  private static void _playSound(Player player, Sound sound) {
    Bukkit.getScheduler().runTaskLater(CapylandPlugin.getInstance(), () -> {
      player.playSound(player.getLocation(), sound, 1.0f, 1.0f);
    }, 0L);
  }

  private static boolean _isBlockUnder(Player player, Material block, int distance) {
    Location playerLocation = player.getLocation();

    for (int y = playerLocation.getBlockY(); y >= playerLocation.getBlockY() - distance; y--) {
      Location blockLocation = new Location(player.getWorld(), playerLocation.getBlockX(), y, playerLocation.getBlockZ());
      Material blockType = blockLocation.getBlock().getType();

      if (blockType == block) {
        return true;
      }
    }

    return false;
  }

  private static boolean _isMaterialInMainHand(Player player, Material material) {
    ItemStack mainHandItem = player.getEquipment().getItemInMainHand();
    boolean result = mainHandItem != null && mainHandItem.getType() == material;

    return result;
  }

  private static void _teleportPlayerToPlayer(Player playerToTeleport, Player destinationPlayer) {
    playerToTeleport.teleport(destinationPlayer);

    Vector direction = destinationPlayer.getLocation().getDirection();
    playerToTeleport.setVelocity(direction);
  }

  private static void _clearHeldItemSlot(Player player) {
    PlayerInventory inventory = player.getInventory();
    inventory.clear(inventory.getHeldItemSlot());
    player.updateInventory();
  }
}
