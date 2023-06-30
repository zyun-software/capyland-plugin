package net.ziozyun.capyland.helpers;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.Arrays;
import java.util.TreeSet;

public class RequestHelper {
  public static String host;
  public static String token;
  public static String from;

  private static String _request(String route, String postData) throws MalformedURLException, ProtocolException, IOException {
    var url = new URL(host + route);

    var connection = (HttpURLConnection) url.openConnection();

    connection.setRequestMethod("POST");

    connection.setRequestProperty("Content-Type", "application/json");
    connection.setRequestProperty("token", token);
    connection.setRequestProperty("from", from);

    connection.setDoOutput(true);

    var outputStream = new DataOutputStream(connection.getOutputStream());
    outputStream.writeBytes(postData);
    outputStream.flush();
    outputStream.close();

    var inputStream = connection.getInputStream();
    var inputStreamReader = new InputStreamReader(inputStream);
    var reader = new BufferedReader(inputStreamReader);

    String line;

    var response = new StringBuilder();

    while ((line = reader.readLine()) != null) {
      response.append(line);
    }

    reader.close();

    var result = response.toString();

    return result;
  }

  private static String[] _getStringArray(String route) throws MalformedURLException, ProtocolException, IOException  {
    String[] emptyArray = {};

    var responseString = _request(route, "");

    var result = responseString.length() > 0
      ? responseString.split(",")
      : emptyArray;

    return result;
  }

  public static TreeSet<String> whitelist() throws MalformedURLException, ProtocolException, IOException {
    var array = _getStringArray("/api/whitelist");
    var result = new TreeSet<>(String.CASE_INSENSITIVE_ORDER);

    var list = Arrays.asList(array);

    result.addAll(list);

    return result;
  }

  public static String[] financialAccounts() throws MalformedURLException, ProtocolException, IOException {
    var result = _getStringArray("/api/financial-accounts");

    return result;
  }

  public static void sendAuthorizeRequest(String nickname) throws MalformedURLException, ProtocolException, IOException {
    var postData = "{\"nickname\":\"" + URLEncoder.encode(nickname, "UTF-8") + "\"}";
    _request("/api/send-authorize-request", postData);
  }
}
