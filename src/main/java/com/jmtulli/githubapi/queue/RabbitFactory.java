package com.jmtulli.githubapi.queue;

import java.net.URI;
import java.net.URISyntaxException;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;

import com.jmtulli.githubapi.exception.GitHubApiException;
import com.rabbitmq.client.ConnectionFactory;

public class RabbitFactory {

  public static ConnectionFactory getFactory() {
    ConnectionFactory factory = new ConnectionFactory();
    String amqpUrl = System.getenv("CLOUDAMQP_URL");

    try {
      if (amqpUrl == null) {
        factory.setUri("amqp://qgkjffns:0aqoD3P-WBsvN9EPQLfJb-HBPtVt7wBa@coyote.rmq.cloudamqp.com/qgkjffns");
      } else {
        URI uri = new URI(amqpUrl);
        factory.setUsername(uri.getUserInfo().split(":")[0]);
        factory.setPassword(uri.getUserInfo().split(":")[1]);
        factory.setHost(uri.getHost());
        factory.setPort(uri.getPort());
        factory.setVirtualHost(uri.getPath().substring(1));
      }
    } catch (URISyntaxException | KeyManagementException | NoSuchAlgorithmException e) {
      throw new GitHubApiException("Error getting Rabbit connection. " + e.getMessage());
    }
    return factory;
  }
}
