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
import java.net.URISyntaxException;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.util.Map;
import java.util.concurrent.TimeoutException;

import com.jmtulli.githubapi.data.FileCounters;
import com.jmtulli.githubapi.queue.Receiver;
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

    Map<String, FileCounters> map = null;// new GitRepository(gitUrl).process();

    long endTimer = System.nanoTime();

    System.out.println("Time: " + (endTimer - startTimer));


    // ExecutorService executor = Executors.newCachedThreadPool();
    //
    // for (String url : filesUrl) {
    // System.out.println("files " + url);
    // executor.submit(() -> new Thread(new Runnable() {
    // @Override
    // public void run() {
    // processResultForFile(url, resultMap);
    // }
    // }).start());
    // }

    new Receiver().start();
    new Receiver().start();
    new Receiver().start();
    new Receiver().start();
    new Receiver().start();
    new Receiver().start();

    ConnectionFactory factory = new ConnectionFactory();
    String uri = System.getenv("CLOUDAMQP_URL");
    System.out.println("uri " + uri);
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
      channel.basicPublish("", "testeQName", null, "Message2".getBytes());
      System.out.println("Msg enviada");
    } catch (IOException | TimeoutException e) {
      System.out.println("Connection erro");
      e.printStackTrace();
    }


    return map;
  }

}
