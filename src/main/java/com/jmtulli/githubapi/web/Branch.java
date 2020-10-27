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

/**
 * Responsible to manage the Github branches.
 * 
 * @author Jose Tulli
 *
 * @param gitRepository - The github repository
 */
public class Branch {
  private String gitRepository;

  public Branch(String gitRepository) {
    this.gitRepository = gitRepository;
  }

  /**
   * Get the paths of the files of a given branch
   * 
   * @param branchName - Name of the branch
   * @return String - A String containing the paths for all the files of the branch
   */
  public String getBranchPaths(String branchName) {
    String treeList = getTreeList(branchName);
    String paths = getPaths(treeList);
    return paths;
  }

  /**
   * Get the tree list path of the files of a given branch
   * 
   * @param branchName - Name of the branch
   * @return String - A String containing the tree list to get all the files of the branch
   */
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

  /**
   * Get the paths of the files from a tree list
   * 
   * @param fileTree - The tree list
   * @return String - A String containing the list of paths for all the files in this file tree
   */
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

  /**
   * Get the full url of the files from a branch
   * 
   * @param relativePaths - The relative paths of the files
   * @param branchName - The name of the branch
   * @return List - List of the full url of all the files of the branch
   */
  public List<String> getFilesUrl(String relativePaths, String branchName) {
    String basePath = gitRepository + URL_BLOB + branchName + "/";
    return Utils.parsePaths(basePath, relativePaths);
  }

  public Map<String, FileCounters> processResult(List<String> filesUrl, Map<String, FileCounters> resultMap) {
    filesUrl.forEach(url -> processResultForFile(url, resultMap));
    return resultMap;
  }

  /**
   * Get the number of lines and size in bytes of the given file
   * 
   * @param fileUrl - The full path of the file
   * @param resultMap - Map containing all the file extensions and their respective number of lines
   *        and sizes
   */
  private void processResultForFile(String fileUrl, Map<String, FileCounters> resultMap) {
    InputStream response = new Connection(fileUrl).getResponse();
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
  }

  /**
   * Parse the document to get the line numbers and the file sizes
   * 
   * @param reader - The reader for the document
   * @return FileCounters - Object to keep line numbers and file sizes
   */
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
