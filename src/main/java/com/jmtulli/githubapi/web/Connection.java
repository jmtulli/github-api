package com.jmtulli.githubapi.web;

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

public class Connection {

//  private static final HttpClient httpClient = HttpClient.newBuilder().build();
//  private HttpClient httpClient;
  private HttpRequest request;
  private String url;

  public Connection(String url) {
    this(url, null, null);
  }

  public Connection(String url, String headerName, String headerValue) {
//    this.httpClient = HttpClient.newBuilder().build();
    this.url = url;
    if (headerName != null && headerValue != null) {
      this.request = HttpRequest.newBuilder().uri(URI.create(url)).setHeader(headerName, headerValue).build();
    } else {
      this.request = HttpRequest.newBuilder().uri(URI.create(url)).build();
    }
  }

  public InputStream getResponse() {
    try {
//      CompletableFuture<HttpResponse<InputStream>> futureResponse = httpClient.sendAsync(request, BodyHandlers.ofInputStream());
      CompletableFuture<HttpResponse<InputStream>> futureResponse = HttpClient.newBuilder().build().sendAsync(request, BodyHandlers.ofInputStream());
      HttpResponse<InputStream> response = futureResponse.get();
      if (response.statusCode() == 200) {
        return response.body();
      } else if (response.statusCode() == 404) {
        throw new GitUrlNotFoundException(url.substring(0, url.indexOf(URL_ALL_BRANCHES)));
      }
      throw new GitHubApiException(response.headers().firstValue("status").orElse("Connection error."));
    } catch (InterruptedException | ExecutionException e) {
      throw new GitHubApiException("Connection error. " + e.getMessage());
    }
  }

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
