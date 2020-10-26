package com.jmtulli.githubapi.queue;

import java.io.IOException;
import java.net.URISyntaxException;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.util.concurrent.TimeoutException;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.DeliverCallback;

public class Receiver {

  public void start() {
    ConnectionFactory factory = new ConnectionFactory();
    try {
      factory.setUri("amqp://qgkjffns:0aqoD3P-WBsvN9EPQLfJb-HBPtVt7wBa@coyote.rmq.cloudamqp.com/qgkjffns");
    } catch (KeyManagementException | NoSuchAlgorithmException | URISyntaxException e1) {
      System.out.println("URI erro");
      e1.printStackTrace();
    }
    try {
      Connection connection = factory.newConnection();
      Channel channel = connection.createChannel();
      channel.queueDeclare("testeQName", false, false, false, null);
      System.out.println("aguardando mensagens...");
      DeliverCallback deliverCallback = (consumerTag, delivery) -> {
        String message = new String(delivery.getBody(), "UTF-8");
        System.out.println("mensagem recebida: " + message);
        doWork(message);
      };
      channel.basicConsume("testeQName", deliverCallback, consumerTag -> {
      });
    } catch (IOException | TimeoutException e) {
      System.out.println("Connection erro");
      e.printStackTrace();
    }
  }

  public static void doWork(String task) {
    System.out.println("Do work - " + Thread.currentThread().getName() + " - Task " + task);
    try {
      Thread.sleep(3000);
    } catch (InterruptedException e) {
      e.printStackTrace();
    }
  }

}
