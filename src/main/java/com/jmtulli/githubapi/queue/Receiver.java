package com.jmtulli.githubapi.queue;

import java.io.IOException;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import com.jmtulli.githubapi.GitHubAPI;
import com.jmtulli.githubapi.data.ProcessStatus;
import com.jmtulli.githubapi.exception.GitHubApiException;
import com.jmtulli.githubapi.web.GitRepository;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.DeliverCallback;

public class Receiver {
  // private static final ConcurrentMap<String, ConcurrentMap<String, FileCounters>> resultsById =
  // new ConcurrentHashMap<>();
  private static final Set<String> completedIds = Collections.synchronizedSet(new HashSet<>());
  public static String errorsFound = null;
  private final String gitUrl;

  public Receiver(String gitUrl) {
    this.gitUrl = gitUrl;
  }

  public void listen(Channel channel) {
    try {
      channel.queueDeclare(gitUrl, false, true, true, null);
      System.out.println("aguardando mensagens...");
      DeliverCallback deliverCallback = (consumerTag, delivery) -> {
        String message = new String(delivery.getBody(), "UTF-8");
        System.out.println("mensagem recebida: " + message);
        doWork(gitUrl, message);
      };
      channel.basicConsume(gitUrl, deliverCallback, consumerTag -> {
      });
    } catch (IOException e) {
      throw new GitHubApiException("Error getting queue. " + e.getMessage());
    }
  }

  private void doWork(String gitUrl, String id) {
    System.out.println("Do work - " + Thread.currentThread().getName() + " - id " + id);
    if (ProcessStatus.NEW == GitHubAPI.getStatus(id)) {
      GitHubAPI.markProccessing(id);
      GitRepository repository = new GitRepository(gitUrl);
      repository.process();
      GitHubAPI.markCompleted(id);
    }
    // completedIds.add(id);
    // if (repository.getResultMap() == null) {
    // resultsById.put(id, repository.getResultMap());
    // } else {
    // resultsById.put(id, repository.process());
    // }
    System.out.println("Fim " + Thread.currentThread().getName() + " - id: " + id);
    // Map<String, FileCounters> map = new GitRepository(gitUrl).process();
    // map.entrySet().forEach(entry -> {
    // System.out.println(entry.getKey() + ": Lines = " + entry.getValue().getLines() + "; Size = "
    // + entry.getValue().getSize());
    // });
  }

  public static boolean getResults1(String id) {
    if (errorsFound != null) {
      // resultsById.remove(id);
      String error = errorsFound;
      errorsFound = null;
      completedIds.remove(id);
      throw new GitHubApiException("Unexpected error. " + error);
    }
    return completedIds.remove(id);
    // return resultsById.get(id);
  }

}
