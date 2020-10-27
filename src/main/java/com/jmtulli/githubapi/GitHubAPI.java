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

  public static ResponseEntity startProcess(String gitUser, String gitRepository) {
    String gitUrl = URL_GITHUB + "/" + gitUser + "/" + gitRepository;

    System.out.println("API Start url: " + gitUrl);

    String id = Integer.toString(gitUrl.hashCode());
    if (id == null)
      throw new GitHubApiException("Error getting GitHub Repository Url.");

    if (!currentIdList.containsKey(id)) {
      currentIdList.put(id, ProcessStatus.NEW);
      System.out.println("API novo id " + id);
    }

    GitRepository repository = new GitRepository(gitUrl);

    if (ProcessStatus.DONE == currentIdList.get(id)) {
      System.out.println("API id done: " + id);
      return ResponseEntity.ok(repository.getResultMap());
    }

    if (repository.isValidGitUrl()) {
      if (ProcessStatus.NEW == currentIdList.get(id)) {
        System.out.println("API vai iniciar fila novo id " + id);
        Channel channel = Connection.getRabbitChannel();
        new Receiver(gitUrl).listen(channel);
        new Sender(gitUrl).send(id, channel);
      }
      return ResponseEntity.status(HttpStatus.TEMPORARY_REDIRECT).body("Processing requests... Check progress on: https://jmtulli-githubapi.herokuapp.com/  http://localhost:8080/" + id);
    }

    throw new GitHubApiException("Git repository " + gitUrl + " not found.");
  }

  public static ResponseEntity processResult(String id) {
    if (!currentIdList.containsKey(id)) {
      return ResponseEntity.notFound().build();
    }

    // if (!currentIdList.contains(id)) {
    // HttpHeaders httpHeaders = new HttpHeaders();
    // try {
    // httpHeaders.setLocation(new URI("http://www.google.com.br"));
    // } catch (URISyntaxException e) {
    // e.printStackTrace();
    // }
    // return new ResponseEntity(httpHeaders, HttpStatus.TEMPORARY_REDIRECT);
    // }

    if (ProcessStatus.DONE == currentIdList.get(id)) {
      return ResponseEntity.status(HttpStatus.TEMPORARY_REDIRECT).body("Processing completed... Check results on: https://jmtulli-githubapi.herokuapp.com/  http://localhost:8080/" + URL_GITHUB);
    }

    // if (Receiver.getResults(id)) {
    // currentIdList.remove(id);
    // return ResponseEntity.status(HttpStatus.TEMPORARY_REDIRECT).body("Processing completed...
    // Check results on: https://jmtulli-githubapi.herokuapp.com/ http://localhost:8080/" +
    // URL_GITHUB);
    // }

    return ResponseEntity.status(HttpStatus.TEMPORARY_REDIRECT).body("Processing requests... Check progress on: https://jmtulli-githubapi.herokuapp.com/  http://localhost:8080/" + id);

    // return (GitHubAPI.processResult(id) != null) ? ResponseEntity.ok(GitHubAPI.processResult(id))
    // : ResponseEntity.status(HttpStatus.TEMPORARY_REDIRECT).body("Processing requests... Check
    // progress on: https://jmtulli-githubapi.herokuapp.com/ http://localhost:8080/" + id);

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
