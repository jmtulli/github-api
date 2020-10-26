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
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
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
    String treeList = null;
    Pattern pattern = Pattern.compile(PATTERN_TREE_LIST);
    Matcher matcher;
    String lineReader;

    InputStream response = new Connection(gitRepository + URL_FIND + branchName).getResponse();

    if (response != null) {
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
        throw new GitHubApiException(e.getMessage());
      }
    } else {
      throw new GitHubApiException("Error getting file tree list of repository " + gitRepository + ".");
    }

    return treeList;
  }

  private String getPaths(String fileTree) {
    String filesPath = null;

    InputStream response = new Connection(URL_GITHUB + fileTree, HEADER_NAME, HEADER_VALUE).getResponse();

    if (response != null) {
      BufferedReader reader = new BufferedReader(new InputStreamReader(response));

      try {
        filesPath = reader.readLine();
      } catch (Exception e) {
        e.printStackTrace();
        throw new GitHubApiException(e.getMessage());
      }
    } else {
      throw new GitHubApiException("Error getting file paths of repository " + gitRepository + ".");
    }

    return filesPath;
  }

  public List<String> getFilesUrl(String relativePaths, String branchName) {
    String basePath = gitRepository + URL_BLOB + branchName + "/";
    return Utils.parsePaths(basePath, relativePaths);
  }

  public void processResult(List<String> filesUrl, Map<String, FileCounters> resultMap) {
    filesUrl.forEach(url -> processResultForFile(url, resultMap));
    
//    ExecutorService executor = Executors.newCachedThreadPool();
//
//    for (String url : filesUrl) {
//      System.out.println("files " + url);
//      executor.submit(() -> new Thread(new Runnable() {
//        @Override
//        public void run() {
//          processResultForFile(url, resultMap);
//        }
//      }).start());
//    }

    System.out.println("fim");

  }

  private void processResultForFile(String fileUrl, Map<String, FileCounters> resultMap) {
//    InputStream response = new Connection().getConcurrentResponse(fileUrl);
    InputStream response = new Connection(fileUrl).getResponse();

    if (response != null) {
      BufferedReader reader = new BufferedReader(new InputStreamReader(response));

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
    } else {
      throw new GitHubApiException("Error parsing file " + fileUrl + ".");
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
      throw new GitHubApiException(e.getMessage());
    }
    return Optional.empty();
  }

}
