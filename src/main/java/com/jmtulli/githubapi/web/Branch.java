package com.jmtulli.githubapi.web;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.jmtulli.githubapi.data.FileCounters;
import com.jmtulli.githubapi.util.Utils;

public class Branch {
  private static final String URL_BLOB = "/blob/";
  private static final String URL_FIND = "/find/";
  private static final String URL_GITHUB = "https://github.com";
  private static final String PATTERN_FILE_LINES = "\\s*(\\d*?)\\sline";
  private static final String PATTERN_FILE_SIZE = "\\s*(\\d*?.\\d*?)\\s(Byte|KB|MB|GB)";
  private static final String PATTERN_TREE_LIST = "data-url=\"(.*?)\"";
  private String gitRepository;

  public Branch(String gitRepository) {
    this.gitRepository = gitRepository;
  }

  public String getBranchPaths(String branchName) {
    String treeList = getTreeList(branchName);
    String paths = getPaths(treeList);
    return paths;
  }

  private String getTreeList(String branchName) {
    String treeList = null;
    Pattern pattern = Pattern.compile(PATTERN_TREE_LIST);
    Matcher matcher;
    String lineReader;

    InputStream response = new Connection(gitRepository + URL_FIND + branchName).getResponse();

    BufferedReader reader = new BufferedReader(new InputStreamReader(response));

    try {
      while ((lineReader = reader.readLine()) != null) {
        if (lineReader.contains("class=\"js-tree-finder\"")) {
          matcher = pattern.matcher(lineReader);
          if (matcher.find()) {
            // System.out.println("file tree: " + branchName + "|" + matcher.group(1));
            treeList = matcher.group(1);
          }
        }
      }
    } catch (IOException e) {
      e.printStackTrace();
    }

    return treeList;
  }

  private String getPaths(String fileTree) {
    String filesPath = null;

    InputStream response = new Connection(URL_GITHUB + fileTree, "X-Requested-With", "XMLHttpRequest").getResponse();

    BufferedReader reader = new BufferedReader(new InputStreamReader(response));

    try {
      filesPath = reader.readLine();
      // System.out.println("file path: " + filesPath);
    } catch (IOException e) {
      e.printStackTrace();
    }

    return filesPath;
  }

  public List<String> getFilesUrl(String relativePaths, String branchName) {
    String basePath = gitRepository + URL_BLOB + branchName + "/";
    return Utils.parsePaths(basePath, relativePaths);
  }

  public void processResult(List<String> filesUrl, Map<String, FileCounters> resultMap) {
    filesUrl.forEach(fileUrl -> {
      InputStream response = new Connection(fileUrl).getResponse();

      String fileExtension = Utils.getFileExtension(fileUrl);
      System.out.println(fileExtension);

      int lines = 0;
      double size = 0;

      BufferedReader reader = new BufferedReader(new InputStreamReader(response));
      String lineReader;
      try {
        while ((lineReader = reader.readLine()) != null) {
          if (lineReader.contains("<div class=\"text-mono f6 flex-auto pr-3 flex-order-2 flex-md-order-1 mt-2 mt-md-0\">")) {
            while ((lineReader = reader.readLine()) != null) {
              if (lineReader.contains("sloc)")) {
                Pattern pattern = Pattern.compile(PATTERN_FILE_LINES);
                Matcher matcher = pattern.matcher(lineReader);
                if (matcher.find()) {
                  lines = Integer.parseInt(matcher.group(1));
                }
              } else if (lineReader.contains("Byte") || lineReader.contains("KB") || lineReader.contains("MB") || lineReader.contains("GB")) {
                Pattern pattern = Pattern.compile(PATTERN_FILE_SIZE);
                Matcher matcher = pattern.matcher(lineReader);
                if (matcher.find()) {
                  size = Utils.getSizeInBytes(Double.parseDouble(matcher.group(1)), matcher.group(2));
                }
                if (resultMap.containsKey(fileExtension)) {
                  resultMap.get(fileExtension).addLines(lines);
                  resultMap.get(fileExtension).addSize(size);
                } else {
                  resultMap.put(fileExtension, new FileCounters(lines, size));
                }
                break;
              }
            }
          }
        }
      } catch (NumberFormatException | IOException e) {
        e.printStackTrace();
      }
    });
  }

}
