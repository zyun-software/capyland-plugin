package com.zyunsoftware.capydevmc.domain.models.session;

public interface SessionRepository {
  void load();

  boolean has(String nickname, String ip);

  void create(String nickname, String ip);

  void remove(String nickname);
}
