package com.zyunsoftware.capydevmc.infrastructure.adapters.minecraft;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

import org.apache.commons.codec.digest.DigestUtils;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;

import com.onarandombox.MultiverseCore.MultiverseCore;
import com.onarandombox.MultiverseCore.api.MultiverseWorld;
import com.zyunsoftware.capydevmc.domain.models.minecraft.MinecraftCoordinatesModel;
import com.zyunsoftware.capydevmc.domain.models.minecraft.MinecraftRepository;
import com.zyunsoftware.capydevmc.infrastructure.utilities.ConfigUtility;
import com.zyunsoftware.capydevmc.infrastructure.utilities.TitleUtility;

import net.kyori.adventure.text.Component;

public class MinecraftAdapter implements MinecraftRepository {
  private Player _player;
  private String _password = "";
  private String[] _args = {};

  @Override
  public List<String> getOnlineNicknames() {
    List<String> onlineNicknames = new ArrayList<>();
    for (Player player : Bukkit.getOnlinePlayers()) {
      onlineNicknames.add(player.getName());
    }

    return onlineNicknames;
  }

  @Override
  public void selectPlayer(String nickname) {
    _player = Bukkit.getPlayer(nickname);
  }

  @Override
  public MinecraftCoordinatesModel getCoordinates() {
    Location location = _player.getLocation();

    int x = (int) location.getX();
    int y = (int) location.getY();
    int z = (int) location.getZ();

    MinecraftCoordinatesModel coordinates = new MinecraftCoordinatesModel(
      x, y, z
    );

    return coordinates;
  }

  @Override
  public MinecraftCoordinatesModel getMainWorldSpawnCoordinates() {
    MultiverseCore mvCore = (MultiverseCore) Bukkit.getPluginManager().getPlugin("Multiverse-Core");

    if (mvCore == null) {
      return null;
    }

    MultiverseWorld mvWorld = mvCore.getMVWorldManager().getMVWorld(ConfigUtility.getString("world.main"));
    if (mvWorld == null) {
      return null;
    }

    int x = (int) mvWorld.getSpawnLocation().getX();
    int y = (int) mvWorld.getSpawnLocation().getY();
    int z = (int) mvWorld.getSpawnLocation().getZ();

    MinecraftCoordinatesModel coordinates = new MinecraftCoordinatesModel(x, y, z);
          
    return coordinates;
  }

  @Override
  public String getIp() {
    String ipAddress = _player.getAddress().getAddress().getHostAddress();

    return ipAddress;
  }

  @Override
  public String getNickname() {
    String nickname = _player.getName();

    return nickname;
  }

  @Override
  public void showMessage(String text) {
    _player.sendMessage(text);
  }

  @Override
  public void showTitle(String title, String subtitle, int fadeIn, int stay, int fadeOut) {
    TitleUtility.sendTitle(_player, title, subtitle, fadeIn, stay, fadeOut);
  }

  @Override
  public void clearTitle() {
    TitleUtility.clearTitle(_player);
  }

  @Override
  public boolean inLobby() {
    String name = ConfigUtility.getString("world.lobby");
    boolean result = _player.getWorld().getName().equals(name);

    return result;
  }

  @Override
  public void teleportToLobby() {
    String name = ConfigUtility.getString("world.lobby");
    _teleportTo(name, (world) -> world.getSpawnLocation());
  }

  @Override
  public void teleportToMain(int x, int y, int z) {
    String name = ConfigUtility.getString("world.main");
    _teleportTo(name, (world) -> new Location(world, x, y, z));
  }

  private void _teleportTo(String name, Function<World, Location> getLocation) {
    MultiverseCore multiverseCore = (MultiverseCore) Bukkit.getPluginManager().getPlugin("Multiverse-Core");
    MultiverseWorld targetWorld = multiverseCore.getMVWorldManager().getMVWorld(name);
    if (targetWorld != null) {
      World world = targetWorld.getCBWorld();
      Location location = getLocation.apply(world);
      _player.teleport(location);
    } else {
      Component reason = Component.text("Ваше повідомлення для кіка");
      _player.kick(reason);
    }
  }

  @Override
  public void setPassword(String value) {
    _password = DigestUtils.md5Hex(value);
  }

  @Override
  public String getPassword() {
    return _password;
  }

  @Override
  public String getConfigString(String key) {
    String result = ConfigUtility.getString(key);
    return result;
  }

  @Override
  public void setArgs(String[] value) {
    _args = value;
  }

  @Override
  public String[] getArgs() {
    return _args;
  }
}
