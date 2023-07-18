package net.ziozyun.capyland.helpers;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.util.Arrays;
import java.util.Base64;
import java.util.Random;
import java.util.TreeSet;

import org.bukkit.entity.Player;

public class RequestHelper {
  public static String host;
  public static String token;
  public static String from;

  private static String _request(String route, String postData)
      throws MalformedURLException, IOException, URISyntaxException {
    var uri = new URI(host + route);
    var connection = (HttpURLConnection) uri.toURL().openConnection();

    connection.setRequestMethod("POST");
    connection.setRequestProperty("Content-Type", "application/json");
    connection.setRequestProperty("token", token);
    connection.setRequestProperty("from", from);
    connection.setDoOutput(true);

    try (var outputStream = connection.getOutputStream()) {
      var postDataBytes = postData.getBytes(StandardCharsets.UTF_8);
      outputStream.write(postDataBytes);
      outputStream.flush();
    }

    var response = new StringBuilder();

    try (var reader = new BufferedReader(new InputStreamReader(connection.getInputStream()))) {
      String line;
      while ((line = reader.readLine()) != null) {
        response.append(line);
      }
    }

    return response.toString();
  }

  private static String[] _getStringArray(String route)
      throws MalformedURLException, ProtocolException, IOException, URISyntaxException {
    String[] emptyArray = {};

    var responseString = _request(route, "");

    var result = responseString.length() > 0
        ? responseString.split(",")
        : emptyArray;

    return result;
  }

  public static TreeSet<String> whitelist()
      throws MalformedURLException, ProtocolException, IOException, URISyntaxException {
    var array = _getStringArray("/api/whitelist");
    var result = new TreeSet<>(String.CASE_INSENSITIVE_ORDER);

    var list = Arrays.asList(array);

    result.addAll(list);

    return result;
  }

  public static String getSkinUrl(Player player) {
    var nickname = player.getName();
    var url = host + "/skins/" + nickname + "/main.png";
    if (isURLValid(url)) {
      return url;
    }

    return null;
  }

  public static boolean isURLValid(String urlString) {
    try {
      var client = HttpClient.newHttpClient();
      var request = HttpRequest.newBuilder()
          .uri(URI.create(urlString))
          .method("HEAD", HttpRequest.BodyPublishers.noBody())
          .build();
      var response = client.send(request, HttpResponse.BodyHandlers.discarding());
      var responseCode = response.statusCode();
      return (responseCode == 200);
    } catch (Exception e) {
      return false;
    }
  }

  public static String[] financialAccounts()
      throws MalformedURLException, ProtocolException, IOException, URISyntaxException {
    var result = _getStringArray("/api/financial-accounts");

    return result;
  }

  public static AuthorizeRequestData sendAuthorizeRequest(Player player)
      throws MalformedURLException, ProtocolException, IOException, URISyntaxException {
    var ip = player.getAddress().getAddress().getHostAddress();
    var nickname = player.getName();
    var random = new Random();
    var code = random.nextInt(900) + 100;
    var token = generateToken(32);
    var postData = "{\"nickname\":\"" + URLEncoder.encode(nickname, "UTF-8") + "\",\"token\":\"" + token
        + "\",\"code\":" + code + ",\"ip\":\"" + ip + "\"}";
    _request("/api/send-authorize-request", postData);

    var data = new AuthorizeRequestData(token, code);
    return data;
  }

  public static String getAuthorizeToken(Player player)
      throws MalformedURLException, ProtocolException, IOException, URISyntaxException {
    var nickname = player.getName();
    var postData = "{\"nickname\":\"" + URLEncoder.encode(nickname, "UTF-8") + "\"}";
    var token = _request("/api/get-authorize-token", postData);

    return token;
  }

  private static final SecureRandom _random = new SecureRandom();

  public static String generateToken(int length) {
    var bytes = new byte[length];
    _random.nextBytes(bytes);
    var result = Base64
        .getEncoder()
        .encodeToString(bytes);

    return result;
  }

  public static class AuthorizeRequestData {
    public String token;
    public int code;

    public AuthorizeRequestData(String token, int code) {
      this.token = token;
      this.code = code;
    }
  }
}
