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
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import com.jmtulli.githubapi.exception.GitHubApiException;
import com.jmtulli.githubapi.exception.GitUrlNotFoundException;

public class Connection {

  private static final HttpClient httpClient = HttpClient.newBuilder().build();
  private static final ExecutorService executorService = Executors.newCachedThreadPool();
  private static final HttpClient httpClientConcurrent = HttpClient.newBuilder().executor(executorService).build();
  private HttpRequest request;
  private String url;

  public Connection() {}

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

  public InputStream getResponse() {
    try {
      HttpResponse<InputStream> response = httpClient.send(request, BodyHandlers.ofInputStream());
      if (response.statusCode() == 200) {
        return response.body();
      } else if (response.statusCode() == 404) {
        throw new GitUrlNotFoundException(url.substring(0, url.indexOf(URL_ALL_BRANCHES)));
      }
      throw new GitHubApiException(response.headers().firstValue("status").orElse("Connection error."));
    } catch (InterruptedException | IOException e) {
      throw new GitHubApiException(e.getMessage());
    }
  }

  public InputStream getConcurrentResponse(String url) {
    System.out.println("con " + url);
    CompletableFuture<HttpResponse<InputStream>> futureResponse = httpClientConcurrent.sendAsync(HttpRequest.newBuilder(URI.create(url)).build(), BodyHandlers.ofInputStream());
    try {
      HttpResponse<InputStream> response = futureResponse.get();
      if (response.statusCode() == 200) {
        return response.body();
      } else if (response.statusCode() == 404) {
        throw new GitUrlNotFoundException(url.substring(0, url.indexOf(URL_ALL_BRANCHES)));
      }
      throw new GitHubApiException(response.headers().firstValue("status").orElse("Connection error. Status " + response.statusCode() + "."));
    } catch (InterruptedException | ExecutionException e) {
      e.printStackTrace();
      throw new GitHubApiException(e.getMessage());
    }
  }

}
