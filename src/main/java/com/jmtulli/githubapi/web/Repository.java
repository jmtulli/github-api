package com.jmtulli.githubapi.web;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.jmtulli.githubapi.data.FileCounters;

/**
 * This performs all the work. It makes an HTTP request, checks the response, and then gathers up
 * all the links on the page. Perform a searchForWord after the successful crawl
 * 
 * @param url - The URL to visit
 * @return whether or not the crawl was successful
 */

public class Repository {
  private static final String PATTERN_BRANCH_NAME = ">(.*)<\\/a>";
  private static final String URL_ALL_BRANCHES = "/branches/all";

  private Branch branch;
  private Map<String, FileCounters> resultMap = new HashMap<>();
  private String gitRepository;

  public Repository(String gitRepository) {
    this.gitRepository = gitRepository;
  }

  public void process() {
    this.branch = new Branch(gitRepository);
    ArrayList<String> allBranches = getAllBranchesNames();
    allBranches.forEach(branchName -> {
      String relativePaths = branch.getBranchPaths(branchName);
      List<String> filesUrl = branch.getFilesUrl(relativePaths, branchName);
      branch.processResult(filesUrl, resultMap);
    });

    resultMap.entrySet().forEach(entry -> {
      System.out.println(entry.getKey() + ": Lines = " + entry.getValue().getLines() + "; Size = " + entry.getValue().getSize());
    });

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
        if (lineReader.contains("<a class=\"branch-name")) {
          matcher = pattern.matcher(lineReader);
          if (matcher.find()) {
            allBranches.add(matcher.group(1));
          }
        }
      }
    } catch (IOException e) {
      e.printStackTrace();
    }

    return allBranches;
  }

}
