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
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.jmtulli.githubapi.data.FileCounters;
import com.jmtulli.githubapi.exception.GitHubApiException;

/**
 * This performs all the work. It makes an HTTP request, checks the response, and then gathers up
 * all the links on the page. Perform a searchForWord after the successful crawl
 * 
 * @param url - The URL to visit
 * @return whether or not the crawl was successful
 */

public class GitRepository {

  private final Map<String, FileCounters> resultMap = new ConcurrentHashMap<>();
  private Branch branch;
  private String gitRepository;

  public GitRepository(String gitRepository) {
    this.gitRepository = gitRepository;
  }

  public static boolean isValidGitUrl(String url) {
    return new Connection(url + URL_ALL_BRANCHES).getResponse() != null;
  }

  public Map<String, FileCounters> process() {
    this.branch = new Branch(gitRepository);
    ArrayList<String> allBranches = getAllBranchesNames();
    allBranches.forEach(branchName -> {
      String relativePaths = branch.getBranchPaths(branchName);
      List<String> filesUrl = branch.getFilesUrl(relativePaths, branchName);
      branch.processResult(filesUrl, resultMap);
    });

    return resultMap;
  }

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

}
