package com.jmtulli.githubapi.web;

import static com.jmtulli.githubapi.util.ApplicationConstants.HEADER_NAME;
import static com.jmtulli.githubapi.util.ApplicationConstants.HEADER_VALUE;
import static com.jmtulli.githubapi.util.ApplicationConstants.PATTERN_CLASS_RESULT;
import static com.jmtulli.githubapi.util.ApplicationConstants.PATTERN_CLASS_TREE_FINDER;
import static com.jmtulli.githubapi.util.ApplicationConstants.PATTERN_FILE_LINES;
import static com.jmtulli.githubapi.util.ApplicationConstants.PATTERN_FILE_SIZE;
import static com.jmtulli.githubapi.util.ApplicationConstants.PATTERN_TREE_LIST;
import static com.jmtulli.githubapi.util.ApplicationConstants.URL_BLOB;
import static com.jmtulli.githubapi.util.ApplicationConstants.URL_FIND;
import static com.jmtulli.githubapi.util.ApplicationConstants.URL_GITHUB;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.jmtulli.githubapi.data.FileCounters;
import com.jmtulli.githubapi.exception.GitHubApiException;
import com.jmtulli.githubapi.util.Utils;

public class Branch {

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
    String treeList = "";
    Pattern pattern = Pattern.compile(PATTERN_TREE_LIST);
    Matcher matcher;
    String lineReader;

    InputStream response = new Connection(gitRepository + URL_FIND + branchName).getResponse();
    BufferedReader reader = new BufferedReader(new InputStreamReader(response));

    try {
      while ((lineReader = reader.readLine()) != null) {
        if (lineReader.contains(PATTERN_CLASS_TREE_FINDER)) {
          matcher = pattern.matcher(lineReader);
          if (matcher.find()) {
            treeList = matcher.group(1);
          }
        }
      }
    } catch (Exception e) {
      throw new GitHubApiException("Error getting file tree list. " + e.getMessage());
    }

    return treeList;
  }

  private String getPaths(String fileTree) {
    String filesPath = "";

    InputStream response = new Connection(URL_GITHUB + fileTree, HEADER_NAME, HEADER_VALUE).getResponse();
    BufferedReader reader = new BufferedReader(new InputStreamReader(response));

    try {
      filesPath = reader.readLine();
    } catch (Exception e) {
      e.printStackTrace();
      throw new GitHubApiException("Error getting files path. " + e.getMessage());
    }

    return filesPath;
  }

  public List<String> getFilesUrl(String relativePaths, String branchName) {
    String basePath = gitRepository + URL_BLOB + branchName + "/";
    return Utils.parsePaths(basePath, relativePaths);
  }

  public Map<String, FileCounters> processResult(List<String> filesUrl, Map<String, FileCounters> resultMap) {
//    filesUrl.forEach(url -> processResultForFile(url, resultMap));
    for (int i = 0; i< filesUrl.size();i++) {
      if (i/10 == 0) {
        try {
          Thread.sleep(1000);
        } catch (InterruptedException e) {
          e.printStackTrace();
        }
      }
      processResultForFile(filesUrl.get(i), resultMap);
    }
    return resultMap;
  }

  private void processResultForFile(String fileUrl, Map<String, FileCounters> resultMap) {
    System.out.println("branch process file: " + fileUrl);
    InputStream response = new Connection(fileUrl).getResponse();
    System.out.println("branch process file got response: " + fileUrl);
    BufferedReader reader = new BufferedReader(new InputStreamReader(response));
    System.out.println("branch process file got reader: " + fileUrl);

    Optional<FileCounters> counters = parseFileResponse(reader);

    if (counters.isPresent()) {
      String fileExtension = Utils.getFileExtension(fileUrl);
      if (resultMap.containsKey(fileExtension)) {
        resultMap.get(fileExtension).addLines(counters.get().getLines());
        resultMap.get(fileExtension).addSize(counters.get().getSize());
      } else {
        resultMap.put(fileExtension, counters.get());
      }
    }
  }

  private Optional<FileCounters> parseFileResponse(BufferedReader reader) {
    String lineReader;
    int lines = 0;
    double size = 0;

    try {
      while ((lineReader = reader.readLine()) != null) {
        if (lineReader.contains(PATTERN_CLASS_RESULT)) {
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
              return Optional.of(new FileCounters(lines, size));
            }
          }
        }
      }
    } catch (Exception e) {
      throw new GitHubApiException("Error parsing response. " + e.getMessage());
    }
    return Optional.empty();
  }

}
