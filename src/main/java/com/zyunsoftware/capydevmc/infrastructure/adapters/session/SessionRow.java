package com.zyunsoftware.capydevmc.infrastructure.adapters.session;

public class SessionRow {
  public final String nickname;
  public final String ip;

  public SessionRow(String nickname, String ip) {
    this.nickname = nickname;
    this.ip = ip;
  }
}
