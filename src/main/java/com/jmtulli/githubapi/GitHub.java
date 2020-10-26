/**
 * This performs all the work. It makes an HTTP request, checks the response, and then gathers up
 * all the links on the page. Perform a searchForWord after the successful crawl
 * 
 * @param url - The URL to visit
 * @return whether or not the crawl was successful
 */
package com.jmtulli.githubapi;

import static com.jmtulli.githubapi.util.ApplicationConstants.URL_GITHUB;

import java.util.Map;

import com.jmtulli.githubapi.data.FileCounters;
import com.jmtulli.githubapi.web.GitRepository;

public class GitHub {

  public static Map<String, FileCounters> startProcess(String gitUser, String gitRepository) {

    long startTimer = System.nanoTime();

    // String gitRepository = "https://github.com/jmtulli/trustly_api";
    // String gitRepository = "https://github.com/OpenFeign/feign";

    String gitUrl = URL_GITHUB + "/" + gitUser + "/" + gitRepository;
    System.out.println("url: " + gitUrl);

    Map<String, FileCounters> map = new GitRepository(gitUrl).process();

    long endTimer = System.nanoTime();

    System.out.println("Time: " + (endTimer - startTimer));

    return map;
  }

}
