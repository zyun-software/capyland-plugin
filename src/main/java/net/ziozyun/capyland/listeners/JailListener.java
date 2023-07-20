package net.ziozyun.capyland.listeners;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import net.ziozyun.capyland.helpers.FileHelper;
import net.ziozyun.capyland.helpers.UserHelper;

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
    _jail = FileHelper.readAsList(_jailFileName);
    _bound = FileHelper.readAsList(_boundFileName);

    _potionEffectTypes.add(PotionEffectType.BLINDNESS);
    _potionEffectTypes.add(PotionEffectType.CONFUSION);
    _potionEffectTypes.add(PotionEffectType.SLOW_DIGGING);
    _potionEffectTypes.add(PotionEffectType.SLOW);
  }

  @EventHandler
  public void onEntityDamageByEntity(EntityDamageByEntityEvent event) {
    if (!(event.getDamager() instanceof Player) || !(event.getEntity() instanceof Player)) {
      return;
    }

    var attacker = (Player) event.getDamager();
    var target = (Player) event.getEntity();

    var attackerNickname = attacker.getName();
    var targetNickname = target.getName();

    var pair = attackerNickname + ":" + targetNickname;

    if (_isLocked(attacker)) {
      event.setCancelled(true);
      return;
    }

    if (UserHelper.isMaterialInMainHand(attacker, Material.SHEARS) && _isBoundTarget(targetNickname)) {
      event.setCancelled(true);

      List<String> toRemove = new ArrayList<>();
      for (var boundEntry : _bound) {
        var nicknames = boundEntry.split(":");
        if (nicknames.length == 2 && nicknames[1].equals(targetNickname)) {
          toRemove.add(boundEntry);
        }
      }

      for (var item : toRemove) {
        _bound.remove(item);
      }

      FileHelper.write(_boundFileName, _bound);

      UserHelper.playSound(attacker, Sound.ENTITY_SHEEP_SHEAR);
      UserHelper.playSound(target, Sound.ENTITY_SHEEP_SHEAR);

      attacker.sendMessage(ChatColor.GREEN + "Ви розв'язали " + ChatColor.GOLD + targetNickname);
      target.sendMessage(ChatColor.YELLOW + "Вас розв'язав " + ChatColor.GOLD + attackerNickname);
      return;
    }

    if (UserHelper.isMaterialInMainHand(attacker, Material.STRING) && _locked.contains(targetNickname)) {
      event.setCancelled(true);
      UserHelper.clearHeldItemSlot(attacker);
      _locked.remove(targetNickname);
      if (!_bound.contains(pair)) {
        _bound.add(pair);
        FileHelper.write(_boundFileName, _bound);
      }

      UserHelper.playSound(attacker, Sound.ENTITY_LEASH_KNOT_PLACE);
      UserHelper.playSound(target, Sound.ENTITY_LEASH_KNOT_PLACE);

      attacker.sendMessage(ChatColor.GREEN + "Ви зв'язали " + ChatColor.GOLD + targetNickname);
      target.sendMessage(ChatColor.YELLOW + "Вас зв'язав " + ChatColor.GOLD + attackerNickname);

      return;
    }

    if (!UserHelper.isMaterialInMainHand(attacker, Material.NETHER_STAR) || _isLocked(target)) {
      return;
    }

    var available = 0;

    var item = attacker.getInventory().getItemInMainHand();
    var meta = item.getItemMeta();
    var lore = meta.getLore();

    var rep = "Доступно: " + ChatColor.GOLD + ChatColor.BOLD;

    if (lore == null || lore.size() != 1
        || !Pattern.compile("^" + rep + "\\d+$").matcher(lore.get(0)).matches()) {
      lore = new ArrayList<>();
      lore.add(rep + fullAvailable);
    }

    available = Integer.parseInt(lore.get(0).replace(rep, ""));
    available--;

    lore = new ArrayList<>();
    lore.add(rep + available);

    meta.setLore(lore);
    item.setItemMeta(meta);

    if (available < 0) {
      UserHelper.playSound(attacker, Sound.ENTITY_ITEM_BREAK);
      UserHelper.clearHeldItemSlot(attacker);

      attacker.sendMessage(ChatColor.YELLOW + "Оглушувач було зламано");

      return;
    }

    event.setCancelled(true);

    if (!_locked.contains(targetNickname)) {
      _locked.add(targetNickname);
    }

    var duration = 20 * lockSeconds;

    Bukkit.getScheduler().runTaskLater(UserHelper.plugin, () -> {
      _locked.remove(targetNickname);
    }, duration);

    UserHelper.playSound(attacker, Sound.ENTITY_LIGHTNING_BOLT_IMPACT);
    UserHelper.playSound(target, Sound.ENTITY_LIGHTNING_BOLT_IMPACT);

    var activeEffects = target.getActivePotionEffects();
    for (var effect : activeEffects) {
      var potionEffectType = effect.getType();
      if (_potionEffectTypes.contains(potionEffectType)) {
        target.removePotionEffect(potionEffectType);
      }
    }

    for (var potionEffectType : _potionEffectTypes) {
      var potionEffect = new PotionEffect(potionEffectType, duration, 0);
      target.addPotionEffect(potionEffect);
    }

    attacker.sendMessage(ChatColor.GREEN + "Ви оглушили " + ChatColor.GOLD + targetNickname);
    target.sendMessage(ChatColor.YELLOW + "Вас оглушив " + ChatColor.GOLD + attackerNickname);
  }

  @EventHandler
  public void onEntityDamage(EntityDamageEvent event) {
    if (!(event.getEntity() instanceof Player)) {
      return;
    }

    var player = (Player) event.getEntity();
    if (_isLocked(player)) {
      event.setCancelled(true);

      return;
    }
  }

  @EventHandler
  public void onPlayerMove(PlayerMoveEvent event) {
    var player = event.getPlayer();
    var nickname = player.getName();

    if (_isLocked(player)) {
      event.setCancelled(true);
      return;
    }

    if (!_jail.contains(nickname)) {
      return;
    }

    if (UserHelper.isBlockUnder(player, Material.DIAMOND_BLOCK, distance)) {
      return;
    }

    player.setGameMode(GameMode.SURVIVAL);
    _jail.remove(player.getName());
    FileHelper.write(_jailFileName, _jail);
    player.sendMessage(ChatColor.YELLOW + "Ви вийшли з в'язниці");
  }

  @EventHandler
  public void onPlayerInteract(PlayerInteractEvent event) {
    var player = event.getPlayer();
    var nickname = player.getName();

    if (_isLocked(player)) {
      event.setCancelled(true);
      return;
    }

    if (!event.getAction().toString().contains("RIGHT")) {
      return;
    }

    if (!UserHelper.isMaterialInMainHand(player, Material.LEAD)) {
      return;
    }

    var canTeleport = player.isOnGround();

    if (!canTeleport) {
      player.sendMessage(ChatColor.RED + "Сюди не можна перемістити в'язня(-ів)");
    }

    for (var boundEntry : _bound) {
      var nicknames = boundEntry.split(":");
      if (nicknames.length == 2 && nicknames[0].equals(nickname)) {
        var prisoner = Bukkit.getPlayer(nicknames[1]);
        if (prisoner != null) {
          if (canTeleport) {
            UserHelper.teleportPlayerToPlayer(prisoner, player);
            var location = player.getLocation();
            prisoner.setBedSpawnLocation(location, true);
            if (UserHelper.isBlockUnder(prisoner, Material.DIAMOND_BLOCK, distance)) {
              prisoner.setGameMode(GameMode.ADVENTURE);
              var name = prisoner.getName();
              if (!_jail.contains(name)) {
                _jail.add(name);
                FileHelper.write(_jailFileName, _jail);
              }
              prisoner.sendMessage(ChatColor.YELLOW + "Ви потрапили до в'язниці");
            }
          }
        }
      }
    }
  }

  @EventHandler
  public void onPlayerInteractEntity(PlayerInteractEntityEvent event) {
    var player = event.getPlayer();

    if (_isLocked(player)) {
      event.setCancelled(true);
    }
  }

  @EventHandler
  public void onPlayerJoin(PlayerJoinEvent event) {
    var player = event.getPlayer();
    var nickname = player.getName();

    if (_locked.contains(nickname)) {
      player.sendMessage(ChatColor.YELLOW + "Ви оглушені");
    }

    if (_isBoundTarget(nickname)) {
      player.sendMessage(ChatColor.YELLOW + "Ви зв'язані");
    }

    if (_jail.contains(nickname)) {
      player.sendMessage(ChatColor.YELLOW + "Ви у в'язниці");
    }
  }

  private boolean _isLocked(Player player) {
    var nickname = player.getName();
    var result = _locked.contains(nickname) || _isBoundTarget(nickname);

    return result;
  }

  private boolean _isBoundTarget(String nickname) {
    for (var boundEntry : _bound) {
      var nicknames = boundEntry.split(":");
      if (nicknames.length == 2 && nicknames[1].equals(nickname)) {
        return true;
      }
    }

    return false;
  }
}
