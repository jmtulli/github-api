package com.jmtulli.githubapi.web;

import static com.jmtulli.githubapi.util.ApplicationConstants.PATTERN_BRANCH_NAME;
import static com.jmtulli.githubapi.util.ApplicationConstants.PATTERN_CLASS_BRANCH_NAME;
import static com.jmtulli.githubapi.util.ApplicationConstants.URL_ALL_BRANCHES;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.jmtulli.githubapi.data.FileCounters;
import com.jmtulli.githubapi.exception.GitHubApiException;

/**
 * Responsible to manage the current GitRepository. Manage the branches and keep the result of the
 * processing.
 * 
 * @author Jose Tulli
 *
 * @param gitRepository - The github repository
 */
public class GitRepository {
  private static final ConcurrentMap<String, Map<String, FileCounters>> resultMap = new ConcurrentHashMap<>();
  private Branch branch;
  private String gitRepository;

  public GitRepository(String gitRepository) {
    this.gitRepository = gitRepository;
  }

  /**
   * Check if a git url is valid
   * 
   * @return boolean - True if the url is valid
   */
  public boolean isValidGitUrl() {
    return new Connection(gitRepository + URL_ALL_BRANCHES).getResponse() != null;
  }

  /**
   * Process the given github repository. Loop over each branch and trigger the processing for all
   * of it's files
   * 
   */
  public void process() {
    this.branch = new Branch(gitRepository);
    if (resultMap.get(gitRepository) == null) {
      resultMap.put(gitRepository, new ConcurrentHashMap<>());
    }

    ArrayList<String> allBranches = getAllBranchesNames();
    allBranches.forEach(branchName -> {
      String relativePaths = branch.getBranchPaths(branchName);
      List<String> filesUrl = branch.getFilesUrl(relativePaths, branchName);
      branch.processResult(filesUrl, resultMap.get(gitRepository));
    });
    System.out.println("End of processing files for repository " + gitRepository);
  }

  /**
   * Get the names of all the branches of the current repository
   * 
   * @return ArrayList - List of all the branches
   */
  private ArrayList<String> getAllBranchesNames() {
    ArrayList<String> allBranches = new ArrayList<>();
    Pattern pattern = Pattern.compile(PATTERN_BRANCH_NAME);
    Matcher matcher;
    String lineReader;

    InputStream response = new Connection(gitRepository + URL_ALL_BRANCHES).getResponse();
    BufferedReader reader = new BufferedReader(new InputStreamReader(response));

    try {
      while ((lineReader = reader.readLine()) != null) {
        if (lineReader.contains(PATTERN_CLASS_BRANCH_NAME)) {
          matcher = pattern.matcher(lineReader);
          if (matcher.find()) {
            allBranches.add(matcher.group(1));
          }
        }
      }
    } catch (Exception e) {
      e.printStackTrace();
      throw new GitHubApiException("Error getting branches names. " + e.getMessage());
    }

    return allBranches;
  }

  /**
   * Get the result of processing the repository
   * 
   * @return Map - Map containing all the file extensions and their respective number of lines and
   *         sizes
   */
  public Map<String, FileCounters> getResultMap() {
    return resultMap.get(gitRepository);
  }

}
