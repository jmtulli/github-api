package com.jmtulli.githubapi.queue;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeoutException;

import com.jmtulli.githubapi.data.FileCounters;
import com.jmtulli.githubapi.exception.GitHubApiException;
import com.jmtulli.githubapi.web.GitRepository;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.DeliverCallback;

public class Receiver {
  private static final Map<String, Map<String, FileCounters>> RESULTS = new ConcurrentHashMap<>();
  public static String errorsFound = null;
  private final String gitUrl;

  public Receiver(String gitUrl) {
    this.gitUrl = gitUrl;
  }

  public void listen() {
    ConnectionFactory factory = RabbitFactory.getFactory();

    try {
      Connection connection = factory.newConnection();
      Channel channel = connection.createChannel();
      channel.queueDeclare(gitUrl, false, false, true, null);
      System.out.println("aguardando mensagens...");
      DeliverCallback deliverCallback = (consumerTag, delivery) -> {
        String message = new String(delivery.getBody(), "UTF-8");
        System.out.println("mensagem recebida: " + message);
        doWork(gitUrl, message);
      };
      channel.basicConsume(gitUrl, deliverCallback, consumerTag -> {
      });
    } catch (IOException | TimeoutException e) {
      throw new GitHubApiException("Error getting queue. " + e.getMessage());
    }
  }

  private void doWork(String gitUrl, String id) {
    System.out.println("Do work - " + Thread.currentThread().getName() + " - Task " + gitUrl);
    RESULTS.put(id, new GitRepository(gitUrl).process());
    System.out.println("Fim " + Thread.currentThread().getName() + " - id: " + id);
    // Map<String, FileCounters> map = new GitRepository(gitUrl).process();
    // map.entrySet().forEach(entry -> {
    // System.out.println(entry.getKey() + ": Lines = " + entry.getValue().getLines() + "; Size = "
    // + entry.getValue().getSize());
    // });
  }

  public static Map<String, FileCounters> getResults(String id) {
    if (errorsFound != null) {
      RESULTS.remove(id);
      String error = errorsFound;
      errorsFound = null;
      throw new GitHubApiException("Unexpected error. " + error);
    }
    return RESULTS.get(id);
  }

}
