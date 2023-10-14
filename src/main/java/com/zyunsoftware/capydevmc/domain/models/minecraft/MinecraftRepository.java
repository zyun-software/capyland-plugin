package com.zyunsoftware.capydevmc.domain.models.minecraft;

import java.util.List;

public interface MinecraftRepository {
  List<String> getOnlineNicknames();

  void selectPlayer(String nickname);

  void setArgs(String[] value);

  String[] getArgs();

  void setPassword(String value);

  String getPassword();

  MinecraftCoordinatesModel getCoordinates();

  MinecraftCoordinatesModel getMainWorldSpawnCoordinates();

  String getIp();

  String getNickname();

  String getConfigString(String key);

  void showMessage(String text);

  void showTitle(String title, String subtitle, int fadeIn, int stay, int fadeOut);

  void clearTitle();

  boolean inLobby();

  void teleportToLobby();
  void teleportToMain(int x, int y, int z) ;
}
