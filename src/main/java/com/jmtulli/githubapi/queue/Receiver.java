package com.jmtulli.githubapi.queue;

import java.io.IOException;

import com.jmtulli.githubapi.GitHubAPI;
import com.jmtulli.githubapi.data.ProcessStatus;
import com.jmtulli.githubapi.exception.GitHubApiException;
import com.jmtulli.githubapi.web.GitRepository;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.DeliverCallback;

public class Receiver {
  public static String errorsFound = null;
  private final String gitUrl;

  public Receiver(String gitUrl) {
    this.gitUrl = gitUrl;
  }

  public void listen(Channel channel) {
    try {
      channel.queueDeclare(gitUrl, false, true, true, null);
      System.out.println("Receiver aguardando mensagens..." + gitUrl);
      DeliverCallback deliverCallback = (consumerTag, delivery) -> {
        String message = new String(delivery.getBody(), "UTF-8");
        System.out.println("Receiver mensagem recebida: " + message);
        doWork(gitUrl, message);
      };
      channel.basicConsume(gitUrl, deliverCallback, consumerTag -> {
      });
    } catch (IOException e) {
      throw new GitHubApiException("Error getting queue. " + e.getMessage());
    }
  }

  private void doWork(String gitUrl, String id) {
    System.out.println("Receiver Do work - " + Thread.currentThread().getName() + " - id " + id);
    if (ProcessStatus.NEW == GitHubAPI.getStatus(id)) {
      GitHubAPI.markProccessing(id);
      GitRepository repository = new GitRepository(gitUrl);
      repository.process();
      GitHubAPI.markCompleted(id);
      System.out.println("Receiver completando id " + id);
    }
    System.out.println("Receiver Fim work" + Thread.currentThread().getName() + " - id: " + id);
  }

}
