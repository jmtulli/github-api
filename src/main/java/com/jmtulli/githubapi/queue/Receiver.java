package com.jmtulli.githubapi.queue;

import java.io.IOException;

import com.jmtulli.githubapi.GitHubAPI;
import com.jmtulli.githubapi.data.ProcessStatus;
import com.jmtulli.githubapi.exception.GitHubApiException;
import com.jmtulli.githubapi.web.GitRepository;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.DeliverCallback;

/**
 * Receiver queue responsible for listening to queue requests and trigger repository processing.
 * 
 * @author Jose Tulli
 *
 * @param gitRepository - Name of the created queue
 * @return Receiver - This object
 */
public class Receiver {
  private final String gitRepository;

  public Receiver(String gitRepository) {
    this.gitRepository = gitRepository;
  }

  public void listen(Channel channel) {
    try {
      channel.queueDeclare(gitRepository, false, false, true, null);
      DeliverCallback deliverCallback = (consumerTag, delivery) -> {
        String message = new String(delivery.getBody(), "UTF-8");
        doWork(gitRepository, message);
      };
      channel.basicConsume(gitRepository, deliverCallback, consumerTag -> {
      });
    } catch (IOException e) {
      throw new GitHubApiException("Error getting queue. " + e.getMessage());
    }
  }

  private void doWork(String gitUrl, String id) {
    if (ProcessStatus.NEW == GitHubAPI.getStatus(id)) {
      GitHubAPI.markProccessing(id);
      GitRepository repository = new GitRepository(gitUrl);
      repository.process();
      GitHubAPI.markCompleted(id);
    }
  }

}
