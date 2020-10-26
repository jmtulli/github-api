/**
 * This performs all the work. It makes an HTTP request, checks the response, and then gathers up
 * all the links on the page. Perform a searchForWord after the successful crawl
 * 
 * @param url - The URL to visit
 * @return whether or not the crawl was successful
 */
package com.jmtulli.githubapi;

import static com.jmtulli.githubapi.util.ApplicationConstants.URL_GITHUB;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.concurrent.TimeoutException;

import com.jmtulli.githubapi.data.FileCounters;
import com.jmtulli.githubapi.web.GitRepository;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;

public class GitHub {

  public static Map<String, FileCounters> startProcess(String gitUser, String gitRepository) {

    long startTimer = System.nanoTime();

    // String gitRepository = "https://github.com/jmtulli/trustly_api";
    // String gitRepository = "https://github.com/OpenFeign/feign";

    String gitUrl = URL_GITHUB + "/" + gitUser + "/" + gitRepository;
    System.out.println("url: " + gitUrl);

    Map<String, FileCounters> map = null;//new GitRepository(gitUrl).process();

    long endTimer = System.nanoTime();

    System.out.println("Time: " + (endTimer - startTimer));
    
    ConnectionFactory factory = new ConnectionFactory();
    factory.setHost("coyote.rmq.cloudamqp.com");
    factory.setPort(1883);
    factory.setUsername("qgkjffns:qgkjffns");
    factory.setPassword("0aqoD3P-WBsvN9EPQLfJb-HBPtVt7wBa");
    try {
      Connection connection = factory.newConnection();
      Channel channel = connection.createChannel();
      channel.queueDeclare("teste", false,false,false,null);
      channel.basicPublish("", "teste", null, "Hello".getBytes());
      System.out.println("Msg enviada");
    } catch (IOException | TimeoutException e) {
      e.printStackTrace();
    }
    

    return map;
  }

}
