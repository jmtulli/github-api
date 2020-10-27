/**
 * This performs all the work. It makes an HTTP request, checks the response, and then gathers up
 * all the links on the page. Perform a searchForWord after the successful crawl
 * 
 * @param url - The URL to visit
 * @return whether or not the crawl was successful
 */
package com.jmtulli.githubapi;

import static com.jmtulli.githubapi.util.ApplicationConstants.URL_GITHUB;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import com.jmtulli.githubapi.data.FileCounters;
import com.jmtulli.githubapi.exception.GitHubApiException;
import com.jmtulli.githubapi.queue.Receiver;
import com.jmtulli.githubapi.queue.Sender;
import com.jmtulli.githubapi.web.GitRepository;

public class GitHubAPI {
  public static final List<String> idList = new ArrayList<>();

  public static ResponseEntity startProcess(String gitUser, String gitRepository) {

    long startTimer = System.nanoTime();

    String gitUrl = URL_GITHUB + "/" + gitUser + "/" + gitRepository;
    String id = UUID.randomUUID().toString();
    System.out.println("url: " + gitUrl);

    if (GitRepository.isValidGitUrl(gitUrl)) {
      idList.add(id);
      new Receiver(gitUrl).listen();
      new Sender(gitUrl).send(id);
      return ResponseEntity.status(HttpStatus.TEMPORARY_REDIRECT).body("Processing requests... Check result on: https://jmtulli-githubapi.herokuapp.com/  http://localhost:8080/" + id);
    }
    long endTimer = System.nanoTime();
    System.out.println("Time: " + (endTimer - startTimer));
    throw new GitHubApiException("Git repository " + gitUrl + " not found.");
  }

  public static Map<String, FileCounters> checkProcess(String id) {
    return Receiver.getResults(id);
  }

}
