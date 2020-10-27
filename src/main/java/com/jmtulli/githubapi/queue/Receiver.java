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
  private final String gitRepository;

  public Receiver(String gitRepository) {
    this.gitRepository = gitRepository;
  }

  public void listen(Channel channel) {
    try {
      channel.queueDeclare(gitRepository, false, false, true, null);
      System.out.println("Receiver aguardando mensagens..." + gitRepository);
      DeliverCallback deliverCallback = (consumerTag, delivery) -> {
        String message = new String(delivery.getBody(), "UTF-8");
        System.out.println("Receiver mensagem recebida: " + message);
        doWork(gitRepository, message);
      };
      channel.basicConsume(gitRepository, deliverCallback, consumerTag -> {
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
