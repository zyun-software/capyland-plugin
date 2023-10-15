package com.zyunsoftware.capydevmc.domain.models.api;

public interface ApiRepository {
  void sendAuthorizationRequest(String nickname, String ip);
}
