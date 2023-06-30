package net.ziozyun.capyland.helpers;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

public class UserHelper {
  private static Map<String, Set<String>> ipDictionary = new HashMap<>();

  public static void add(String ip, String nickname) {
    ipDictionary.compute(ip, (key, existingSet) -> {
      if (existingSet == null) {
        var newSet = new TreeSet<>(String.CASE_INSENSITIVE_ORDER);
        newSet.add(nickname);

        return newSet;
      } else if (!existingSet.contains(nickname)) {
        existingSet.add(nickname);
      }

      return existingSet;
    });
  }

  public static boolean exists(String ip, String nickname) {
    var result = ipDictionary.entrySet().stream()
      .anyMatch(entry -> entry.getKey().equalsIgnoreCase(ip)
      && entry.getValue().contains(nickname));

    return result;
  }
}
