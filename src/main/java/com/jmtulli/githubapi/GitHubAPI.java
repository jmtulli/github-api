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

/**
 * Processing class of the API. Handles the requests, start the processing tasks for the repository
 * and return the status to the server.
 * 
 * @author Jose Tulli
 *
 * @param gitUser - The user name of the github
 * @param gitRepositoryName - The name of the github repository
 * @return ResponseEntity - Status of the requests
 */
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
      return ResponseEntity.status(HttpStatus.TEMPORARY_REDIRECT).body("Processing requests... Refresh this page or check progress on: https://jmtulli-githubapi.herokuapp.com/" + id);
    }

    throw new GitHubApiException("Git repository " + gitRepository + " not found.");
  }

  // Return information to server about the status of the request for a determined repository
  public static ResponseEntity checkResult(String id) {
    if (!currentIdList.containsKey(id)) {
      return ResponseEntity.notFound().build();
    }

    if (ProcessStatus.DONE == currentIdList.get(id)) {
      return ResponseEntity.status(HttpStatus.TEMPORARY_REDIRECT).body("Processing completed... Make the original request again to get the results.");
    }

    return ResponseEntity.status(HttpStatus.TEMPORARY_REDIRECT).body("Processing requests... Go back to original request or check progress on: https://jmtulli-githubapi.herokuapp.com/" + id);
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
