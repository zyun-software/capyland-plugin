package com.zyunsoftware.capydevmc.app.utilities;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class FileUtility {
  public static File dataFolder;

  public static String read(String fileName) {
    File file = new File(dataFolder, fileName);
    if (!file.exists() || !file.isFile()) {
      return "";
    }

    try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
      StringBuilder content = new StringBuilder();
      String line;
      while ((line = reader.readLine()) != null) {
        content.append(line);
      }
      return content.toString();
    } catch (Exception e) {
      e.printStackTrace();
      return "";
    }
  }

  public static List<String> readAsList(String fileName) {
    String input = read(fileName);

    if (input.isEmpty()) {
      return new ArrayList<>();
    }

    String[] elements = input.split(",");
    List<String> resultList = new ArrayList<>(Arrays.asList(elements));
    return resultList;
  }

  public static void write(String fileName, String content) {
    try {
      File file = new File(dataFolder, fileName);
      if (!file.exists()) {
        file.createNewFile();
      }

      try (FileWriter writer = new FileWriter(file)) {
        writer.write(content);
      }
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  public static void write(String fileName, List<String> list) {
    String content = String.join(",", list);
    write(fileName, content);
  }
}