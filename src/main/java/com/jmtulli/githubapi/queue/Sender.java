package com.jmtulli.githubapi.queue;

import java.io.IOException;
import java.util.concurrent.TimeoutException;

import com.jmtulli.githubapi.exception.GitHubApiException;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;

public class Sender {

  private final String gitUrl;

  public Sender(String gitUrl) {
    this.gitUrl = gitUrl;
  }

  public void send(String id) {
    ConnectionFactory factory = RabbitFactory.getFactory();

    try {
      Connection connection = factory.newConnection();
      Channel channel = connection.createChannel();
      channel.queueDeclare(gitUrl, false, false, true, null);
      channel.basicPublish("", gitUrl, null, id.getBytes());
    } catch (IOException | TimeoutException e) {
      throw new GitHubApiException("Error sending queue. " + e.getMessage());
    }
  }

}
