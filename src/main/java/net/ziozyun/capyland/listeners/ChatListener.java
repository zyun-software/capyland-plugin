package net.ziozyun.capyland.listeners;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.GameMode;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.List;

import net.ziozyun.capyland.helpers.UserHelper;

public class ChatListener implements Listener {
  private JavaPlugin _plugin;
  private double _radiusSquared;
  
  public ChatListener(JavaPlugin plugin, double radiusSquared) {
    _plugin = plugin;
    _radiusSquared = radiusSquared;
  }

  @EventHandler
  public void onPlayerChat(AsyncPlayerChatEvent event) {
    event.setCancelled(true);
    var player = event.getPlayer();

    if (!UserHelper.exists(player)) {
      return;
    }

    var nickname = player.getName();

    var coordinates = UserHelper.getCoordinates(player);

    final var message = event.getMessage()
      .replace("#loc", coordinates)
      .replace("#корди", coordinates);

    if (message.startsWith("!")) {
      if (message.length() > 1) {
        Bukkit.broadcastMessage(ChatColor.GOLD + nickname + ChatColor.RESET + ": " + message.substring(1));
      } else {
        player.sendMessage(ChatColor.RED + "Необхідно ввести повідомлення");
      }

      return;
    }

    var find = false;
    var text = ChatColor.GOLD + nickname + ChatColor.RESET + ": " + ChatColor.GRAY + message;

    for (var nearbyPlayer : Bukkit.getOnlinePlayers()) {
      if (player.getLocation().distanceSquared(nearbyPlayer.getLocation()) <= _radiusSquared) {
        var notMe = !nearbyPlayer.getName().equals(nickname);
        var notSpectator = !(nearbyPlayer.getGameMode() == GameMode.SPECTATOR);

        if (notMe) {
          nearbyPlayer.sendMessage(text);
        }

        if (!find && notMe && notSpectator) {
          find = true;
        }
      }
    }

    player.sendMessage(find ? text : ChatColor.RED + "Вас ніхто не почув");
  }

  @EventHandler
  public void onPlayerDeath(PlayerDeathEvent event) {
    var player = event.getEntity();
    var playerName = ChatColor.GOLD + player.getName() + ChatColor.YELLOW;

    var damageEvent = event.getEntity().getLastDamageCause();

    var deathMessage = playerName + " загинув в незрозумілих обставинах. Здається, це було надзвичайно кумедно!";

    if (damageEvent != null) {
      var damageCause = damageEvent.getCause();
      deathMessage = ChatColor.YELLOW + _getDeathMessage(damageCause, playerName, deathMessage);
    }

    var defaultDeathMessage = event.getDeathMessage();

    event.setDeathMessage(deathMessage);

    var coordinates = UserHelper.getCoordinates(player);
    player.sendMessage(ChatColor.RED + "Ви склеїли ласти! Бігти за вашими речами сюди: " + ChatColor.GOLD + coordinates);
  }

  private String _getDeathMessage(DamageCause damageCause, String playerName, String def) {
    switch (damageCause) {
      case KILL:
        return "Ой, " + playerName + " з'їли моби! Він став вечерею!";
      case WORLD_BORDER:
        return playerName + " пішов за межі світу і втратився. Загублений без кордонів!";
      case CONTACT:
        return playerName + " зіткнувся з чимось твердим... Побачимо його в наступному житті!";
      case ENTITY_ATTACK:
        return "О, " + playerName + " вразив моб! Чи то шкіра зелена, чи Громадянин " + playerName + " просто необережний?";
      case ENTITY_SWEEP_ATTACK:
        return playerName + " влучив усіх мобів навколо себе. Помолімось за їхні душі!";
      case PROJECTILE:
        return playerName + " став жертвою кидка... Слідкуйте, друзі, за політами каменів!";
      case SUFFOCATION:
        return playerName + " задихнувся у тісному просторі. Легендарна боротьба з повітрям!";
      case FALL:
        return playerName + " пропустив останню сходинку. Його кроки не виявились рівними!";
      case FIRE:
        return "Вогонь обняв " + playerName + " і не відпускає. Він стає справжнім гарячим хлопчиком!";
      case FIRE_TICK:
        return playerName + " горить настільки довго, що вирішив заснути. Приємних снів у пеклі!";
      case MELTING:
        return playerName + " розтав у лаві. Він знаходиться у найтеплішому обіймі!";
      case LAVA:
        return playerName + " потонув у лаві. Чи то гарячий спа-сю-сю?";
      case DROWNING:
        return playerName + " занурився у воду і не зміг вийти. Він добре знає василіска!";
      case BLOCK_EXPLOSION:
        return playerName + " вибухнув разом з блоком. Це був дивовижний танець!";
      case ENTITY_EXPLOSION:
        return "Вибух моба прийняв " + playerName + ". Хто би міг подумати, що моби мають вибуховий характер!";
      case VOID:
        return playerName + " зник у прірві. Відтепер його гасло - нічого не бачу, нічого не чую!";
      case LIGHTNING:
        return "Блискавка вдарила " + playerName + ". Він став живим експериментом на проведення електричного струму!";
      case SUICIDE:
        return playerName + " вирішив покласти край своїм пригодам. Нехай його персонаж піде у вічну піксельну сплячку!";
      case STARVATION:
        return playerName + " помирає від голоду. На жаль, в Майнкрафті немає піца-доставки!";
      case POISON:
        return playerName + " з'їв їдку рослину. Це було дійсно їстівне пригода!";
      case MAGIC:
        return "Магія забрала життя " + playerName + ". Деякі речі просто не можуть бути пояснені!";
      case WITHER:
        return playerName + " став жертвою падіння відьминого силу. Вона розіп'яла його на піксельні крила!";
      case FALLING_BLOCK:
        return "Блок впав на " + playerName + ". Він став справжньою підтримкою каменю!";
      case THORNS:
        return playerName + " втратив життя через шипи. Мабуть, він не прочитав інструкцію по використанню!";
      case DRAGON_BREATH:
        return "Дракон облив " + playerName + " своїм диханням. Він знайшов новий спосіб підтримувати свіжість!";
      case CUSTOM:
        return playerName + " помер від власного кастомного пошкодження. Він відчув себе особливо!";
      case FLY_INTO_WALL:
        return playerName + " влетів у стіну. Він має настільки чудові польоти, що не контролює їх!";
      case HOT_FLOOR:
        return playerName + " ступив на гарячу підлогу і згорів. Його стопи виявилися надто чутливими!";
      case CRAMMING:
        return playerName + " був стиснутийміж мобами як сардинка. Він бажав знайти нові способи соціалізації!";
      case DRYOUT:
        return playerName + " висох у водному джерелі. Він став настоянкою з піксельних соків!";
      case FREEZE:
        return playerName + " замерз у льоду. Він став справжнім крижаним блоком!";
      case SONIC_BOOM:
        return playerName + " був вибухнутий швидкістю. Він став головним кандидатом на роль супергероя!";
      default:
        return def;
    }
  }
}
