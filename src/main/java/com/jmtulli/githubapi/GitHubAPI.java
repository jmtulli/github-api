/**
 * This performs all the work. It makes an HTTP request, checks the response, and then gathers up
 * all the links on the page. Perform a searchForWord after the successful crawl
 * 
 * @param url - The URL to visit
 * @return whether or not the crawl was successful
 */
package com.jmtulli.githubapi;

import static com.jmtulli.githubapi.util.ApplicationConstants.URL_GITHUB;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import com.jmtulli.githubapi.exception.GitHubApiException;
import com.jmtulli.githubapi.queue.Receiver;
import com.jmtulli.githubapi.queue.Sender;
import com.jmtulli.githubapi.web.GitRepository;

public class GitHubAPI {
  private static final Set<String> currentIdList = new HashSet<>();

  public static ResponseEntity startProcess(String gitUser, String gitRepository) {
    String gitUrl = URL_GITHUB + "/" + gitUser + "/" + gitRepository;
    String id = UUID.randomUUID().toString();

    GitRepository repository = new GitRepository(gitUrl);

    if (repository.getResultMap() != null) {
      return ResponseEntity.ok(repository.getResultMap());
    }

    if (repository.isValidGitUrl()) {
      currentIdList.add(id);
      new Receiver(gitUrl).listen();
      new Sender(gitUrl).send(id);
      return ResponseEntity.status(HttpStatus.TEMPORARY_REDIRECT).body("Processing requests... Check progress on: https://jmtulli-githubapi.herokuapp.com/  http://localhost:8080/" + id);
    }

    throw new GitHubApiException("Git repository " + gitUrl + " not found.");
  }

  // public static Map<String, FileCounters> processResult1(String id) {
  // return Receiver.getResults(id);
  // }

  public static ResponseEntity processResult(String id) {
    if (!currentIdList.contains(id)) {
      return ResponseEntity.notFound().build();
    }

    if (Receiver.getResults(id)) {
      currentIdList.remove(id);
      return ResponseEntity.status(HttpStatus.TEMPORARY_REDIRECT).body("Processing completed... Check results on: https://jmtulli-githubapi.herokuapp.com/  http://localhost:8080/" + URL_GITHUB);
    }

    return ResponseEntity.status(HttpStatus.TEMPORARY_REDIRECT).body("Processing requests... Check progress on: https://jmtulli-githubapi.herokuapp.com/  http://localhost:8080/" + id);


    // return (GitHubAPI.processResult(id) != null) ? ResponseEntity.ok(GitHubAPI.processResult(id))
    // : ResponseEntity.status(HttpStatus.TEMPORARY_REDIRECT).body("Processing requests... Check
    // progress on: https://jmtulli-githubapi.herokuapp.com/ http://localhost:8080/" + id);


  }

}
