package com.zyunsoftware.capydevmc.infrastructure.adapters.api;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

import com.zyunsoftware.capydevmc.domain.models.api.ApiRepository;
import com.zyunsoftware.capydevmc.infrastructure.utilities.ConfigUtility;

public class ApiAdapter implements ApiRepository {
  private HttpClient _client;

  public ApiAdapter() {
    _client = HttpClient.newHttpClient();
  }

  @Override
  public void sendAuthorizationRequest(String nickname, String ip) {
    String json = "{"
      + "\"action\":\"authorization-request\","
      + "\"data\":{"
      + "\"nickname\":\"" + _escapeSpecialChars(nickname) + "\","
      + "\"ip\":\"" + _escapeSpecialChars(ip) + "\""
      + "}"
      + "}";

    try {
      _sendPost(json);
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  private void _sendPost(String data) {
    String url = ConfigUtility.getString("api.url");
    String token = ConfigUtility.getString("api.token");

    HttpRequest request = HttpRequest.newBuilder()
      .uri(URI.create(url))
      .header("Content-Type", "application/json")
      .header("X-Token", token)
      .POST(HttpRequest.BodyPublishers.ofString(data))
      .build();

      _client.sendAsync(request, HttpResponse.BodyHandlers.discarding());
  }

  private static String _escapeSpecialChars(String input) {
    return input
      .replace("\\", "\\\\")
      .replace("\"", "\\\"")
      .replace("\n", "\\n")
      .replace("\r", "\\r")
      .replace("\t", "\\t");
  }
}
