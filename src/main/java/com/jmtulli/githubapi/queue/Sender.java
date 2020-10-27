package com.jmtulli.githubapi.queue;

import java.io.IOException;

import com.jmtulli.githubapi.exception.GitHubApiException;
import com.rabbitmq.client.Channel;

/**
 * Sender queue responsible for send message with the ID of the repository to be processed.
 * 
 * @author Jose Tulli
 *
 * @param gitRepository - Name of the created queue
 * @return Sender - This object
 */
public class Sender {

  private final String gitRepository;

  public Sender(String gitRepository) {
    this.gitRepository = gitRepository;
  }

  public void send(String id, Channel channel) {

    try {
      channel.queueDeclare(gitRepository, false, false, true, null);
      channel.basicPublish("", gitRepository, null, id.getBytes());
    } catch (IOException e) {
      throw new GitHubApiException("Error sending queue. " + e.getMessage());
    }
  }

}
