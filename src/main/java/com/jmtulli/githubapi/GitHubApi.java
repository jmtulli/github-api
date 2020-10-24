/**
 * This performs all the work. It makes an HTTP request, checks the response, and then gathers up
 * all the links on the page. Perform a searchForWord after the successful crawl
 * 
 * @param url - The URL to visit
 * @return whether or not the crawl was successful
 */
package com.jmtulli.githubapi;

import com.jmtulli.githubapi.web.Repository;

public class GitHubApi {

  public static void startProcess() {

    long startTimer = System.nanoTime();

    String gitRepository = "https://github.com/jmtulli/trustly_api";
    // String gitRepository = "https://github.com/OpenFeign/feign";

    new Repository(gitRepository).process();

    long endTimer = System.nanoTime();

    System.out.println("Time: " + (endTimer - startTimer));

  }

}
