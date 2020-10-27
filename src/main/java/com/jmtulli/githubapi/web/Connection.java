package com.jmtulli.githubapi.web;

import static com.jmtulli.githubapi.util.ApplicationConstants.RETRY_TIME_AFTER_TOO_MANY_REQUEST;
import static com.jmtulli.githubapi.util.ApplicationConstants.URL_ALL_BRANCHES;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.http.HttpResponse.BodyHandlers;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;

import com.jmtulli.githubapi.exception.GitHubApiException;
import com.jmtulli.githubapi.exception.GitUrlNotFoundException;
import com.jmtulli.githubapi.queue.RabbitFactory;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.ConnectionFactory;

/**
 * Handles all the HTTP connections of the API.
 * 
 * @author Jose Tulli
 *
 * @param url - The url of the repository
 * @param headerName - The name of the header to include in the request
 * @param headerValue - The value of the header to include in the request
 * @return Connection - A HTTP connection
 */
public class Connection {

  private static final HttpClient httpClient = HttpClient.newBuilder().build();
  private HttpRequest request;
  private String url;

  public Connection(String url) {
    this(url, null, null);
  }

  public Connection(String url, String headerName, String headerValue) {
    this.url = url;
    if (headerName != null && headerValue != null) {
      this.request = HttpRequest.newBuilder().uri(URI.create(url)).setHeader(headerName, headerValue).build();
    } else {
      this.request = HttpRequest.newBuilder().uri(URI.create(url)).build();
    }
  }

  /**
   * Get HTTP response of the connection. Handles connection error and retry in case of a server
   * error indicating too many requests.
   * 
   * @return InputStream - The response returned by the connection request
   */
  public InputStream getResponse() {
    try {
      CompletableFuture<HttpResponse<InputStream>> futureResponse = httpClient.sendAsync(request, BodyHandlers.ofInputStream());
      HttpResponse<InputStream> response = futureResponse.get();
      if (response.statusCode() == 429) {
        Thread.sleep(RETRY_TIME_AFTER_TOO_MANY_REQUEST);
        futureResponse = httpClient.sendAsync(request, BodyHandlers.ofInputStream());
        response = futureResponse.get();
      }

      if (response.statusCode() == 200) {
        return response.body();
      } else if (response.statusCode() == 404) {
        throw new GitUrlNotFoundException(url.substring(0, url.indexOf(URL_ALL_BRANCHES)));
      } else if (response.statusCode() == 429) {
        throw new GitHubApiException("Too many request. Please try again later.");
      }
      throw new GitHubApiException("Connection error. " + response.statusCode());
    } catch (InterruptedException | ExecutionException e) {
      throw new GitHubApiException("Connection error. " + e.getMessage());
    }
  }

  /**
   * Factory for RabbitMQ channel
   * 
   * @return Channel - Channel used to get a RabbitMQ queue
   */
  public static Channel getRabbitChannel() {
    ConnectionFactory factory = RabbitFactory.getFactory();
    try {
      com.rabbitmq.client.Connection connection = factory.newConnection();
      return connection.createChannel();
    } catch (TimeoutException | IOException e) {
      throw new GitHubApiException("Error creating queue connection.");
    }
  }

}
