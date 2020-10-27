package com.jmtulli.githubapi.queue;

import java.io.IOException;

import com.jmtulli.githubapi.exception.GitHubApiException;
import com.rabbitmq.client.Channel;

public class Sender {

  private final String gitRepository;

  public Sender(String gitRepository) {
    this.gitRepository = gitRepository;
  }

  public void send(String id, Channel channel) {

    try {
      channel.queueDeclare(gitRepository, false, true, true, null);
      channel.basicPublish("", gitRepository, null, id.getBytes());
    } catch (IOException e) {
      throw new GitHubApiException("Error sending queue. " + e.getMessage());
    }
  }

}
