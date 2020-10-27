package com.jmtulli.githubapi.util;

import java.util.ArrayList;
import java.util.List;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import static com.jmtulli.githubapi.util.ApplicationConstants.*;

public class Utils {

  public static Double getSizeInBytes(double size, String unit) {
    double sizeInBytes = size;
    if ("KB".equals(unit)) {
      sizeInBytes = size * 1024;
    } else if ("MB".equals(unit)) {
      sizeInBytes = size * 1024 * 1024;
    } else if ("GB".equals(unit)) {
      sizeInBytes = size * 1024 * 1024 * 1024;
    }
    return sizeInBytes;
  }

  public static List<String> parsePaths(String basePath, String relativePaths) {
    List<String> filesUrl = new ArrayList<>();

    JsonObject jsonObject = new JsonParser().parse(relativePaths).getAsJsonObject();
    JsonArray jsonArray = jsonObject.getAsJsonArray("paths");
    jsonArray.forEach(path -> {
      filesUrl.add(basePath + path.getAsString());
    });

    return filesUrl;
  }

  public static String getFileExtension(String fullFilename) {
    String filename = fullFilename.substring(fullFilename.lastIndexOf("/") + 1);
    return filename.lastIndexOf(".") >= 0 ? filename.substring(filename.lastIndexOf(".") + 1) : STRING_NO_EXTENSION;
  }

}

