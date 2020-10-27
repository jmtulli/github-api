package com.jmtulli.githubapi.queue;

import java.io.IOException;

import com.jmtulli.githubapi.exception.GitHubApiException;
import com.rabbitmq.client.Channel;

public class Sender {

  private final String gitUrl;

  public Sender(String gitUrl) {
    this.gitUrl = gitUrl;
  }

  public void send(String id, Channel channel) {

    try {
      channel.queueDeclare(gitUrl, false, true, true, null);
      channel.basicPublish("", gitUrl, null, id.getBytes());
    } catch (IOException e) {
      throw new GitHubApiException("Error sending queue. " + e.getMessage());
    }
  }

}
