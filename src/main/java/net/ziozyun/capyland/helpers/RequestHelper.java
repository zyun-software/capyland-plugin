package net.ziozyun.capyland.helpers;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;

public class RequestHelper {
  public static String host;
  public static String token;

  private static String request(String route) throws MalformedURLException, ProtocolException, IOException {
    var url = new URL(host + route);

    var connection = (HttpURLConnection) url.openConnection();

    connection.setRequestMethod("POST");

    connection.setRequestProperty("Content-Type", "application/json");
    connection.setRequestProperty("token", token);

    connection.setDoOutput(true);

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

  private static String[] getStringArray(String route) throws MalformedURLException, ProtocolException, IOException  {
    String[] emptyArray = {};

    var responseString = request(route);
    
    var result = responseString.length() > 0
      ? responseString.split(",")
      : emptyArray;

    return result;
  }

  public static String[] whitelist() throws MalformedURLException, ProtocolException, IOException {
    var result = getStringArray("/whitelist");

    return result;
  }

  public static String[] financialAccounts() throws MalformedURLException, ProtocolException, IOException {
    var result = getStringArray("/financial-accounts");

    return result;
  }
}
