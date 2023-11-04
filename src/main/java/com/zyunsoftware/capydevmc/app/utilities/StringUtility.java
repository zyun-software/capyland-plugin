package com.zyunsoftware.capydevmc.app.utilities;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class StringUtility {
  public static Integer findIntegerInString(String str) {
    Pattern pattern = Pattern.compile("-?\\d+");
    Matcher matcher = pattern.matcher(str);

    if (matcher.find()) {
      String numberStr = matcher.group();
      return Integer.parseInt(numberStr);
    }

    return null;
  }
}
