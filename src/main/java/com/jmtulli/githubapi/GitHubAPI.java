/**
 * This performs all the work. It makes an HTTP request, checks the response, and then gathers up
 * all the links on the page. Perform a searchForWord after the successful crawl
 * 
 * @param url - The URL to visit
 * @return whether or not the crawl was successful
 */
package com.jmtulli.githubapi;

import static com.jmtulli.githubapi.util.ApplicationConstants.URL_GITHUB;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import com.jmtulli.githubapi.data.ProcessStatus;
import com.jmtulli.githubapi.exception.GitHubApiException;
import com.jmtulli.githubapi.queue.Receiver;
import com.jmtulli.githubapi.queue.Sender;
import com.jmtulli.githubapi.web.Connection;
import com.jmtulli.githubapi.web.GitRepository;
import com.rabbitmq.client.Channel;

public class GitHubAPI {
  private static final ConcurrentMap<String, ProcessStatus> currentIdList = new ConcurrentHashMap<>();

  public static ResponseEntity startProcess(String gitUser, String gitRepositoryName) {
    String gitRepository = URL_GITHUB + "/" + gitUser + "/" + gitRepositoryName;

    System.out.println("Start processing url: " + gitRepository);

    String id = Integer.toString(gitRepository.hashCode());
    if (id == null)
      throw new GitHubApiException("Error getting GitHub Repository Url.");

    if (!currentIdList.containsKey(id)) {
      currentIdList.put(id, ProcessStatus.NEW);
    }

    GitRepository repository = new GitRepository(gitRepository);

    if (ProcessStatus.DONE == currentIdList.get(id)) {
      return ResponseEntity.ok(repository.getResultMap());
    }

    if (repository.isValidGitUrl()) {
      if (ProcessStatus.NEW == currentIdList.get(id)) {
        Channel channel = Connection.getRabbitChannel();
        new Receiver(gitRepository).listen(channel);
        new Sender(gitRepository).send(id, channel);
      }
      return ResponseEntity.status(HttpStatus.TEMPORARY_REDIRECT).body("Processing requests... Check progress on: https://jmtulli-githubapi.herokuapp.com/  http://localhost:8080/" + id);
    }

    throw new GitHubApiException("Git repository " + gitRepository + " not found.");
  }

  public static ResponseEntity processResult(String id) {
    if (!currentIdList.containsKey(id)) {
      return ResponseEntity.notFound().build();
    }

    if (ProcessStatus.DONE == currentIdList.get(id)) {
      return ResponseEntity.status(HttpStatus.TEMPORARY_REDIRECT).body("Processing completed... Check results on: https://jmtulli-githubapi.herokuapp.com/  http://localhost:8080/" + URL_GITHUB);
    }

    return ResponseEntity.status(HttpStatus.TEMPORARY_REDIRECT).body("Processing requests... Check progress on: https://jmtulli-githubapi.herokuapp.com/  http://localhost:8080/" + id);
  }

  public static ProcessStatus getStatus(String id) {
    return currentIdList.get(id);
  }

  public static void markCompleted(String id) {
    currentIdList.replace(id, ProcessStatus.DONE);
  }

  public static void markProccessing(String id) {
    currentIdList.replace(id, ProcessStatus.PROCESSING);
  }

}
