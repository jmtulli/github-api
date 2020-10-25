package com.jmtulli.githubapi.web;

import static com.jmtulli.githubapi.util.ApplicationConstants.URL_ALL_BRANCHES;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.http.HttpResponse.BodyHandler;
import java.net.http.HttpResponse.BodyHandlers;
import java.time.Duration;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import com.jmtulli.githubapi.exception.GitHubApiException;
import com.jmtulli.githubapi.exception.GitUrlNotFoundException;

public class Connection {

  private static final HttpClient httpClient = HttpClient.newBuilder().connectTimeout(Duration.ofHours(1)).build();
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

  public InputStream getResponse() {
    HttpResponse<InputStream> response;
    try {
      response = httpClient.send(request, BodyHandlers.ofInputStream());

//    CompletableFuture<HttpResponse<InputStream>> r = httpClient.sendAsync(request, BodyHandlers.ofInputStream());
//
//      InputStream inp = null;
//      try {
//        inp = r.thenApply(HttpResponse::body).get(1, TimeUnit.HOURS);
//        System.out.println("inp1: " + inp);
//        return inp;
//      } catch (ExecutionException | TimeoutException | InterruptedException e) {
//        e.printStackTrace();
//      }
//      System.out.println("inp2: " + inp);
//      
//        Integer sts=0;
//        try {
//          sts = r.thenApply(HttpResponse::statusCode).get(1, TimeUnit.HOURS);
//          System.out.println("sts1: " + sts);
//        } catch (ExecutionException | TimeoutException | InterruptedException e) {
//          e.printStackTrace();
//        }
//        System.out.println("sts2: " + sts);
//        return null;
      if (response.statusCode() == 200) {
        return response.body();
      } else if (response.statusCode() == 404) {
        throw new GitUrlNotFoundException(url.substring(0, url.indexOf(URL_ALL_BRANCHES)));
      }
      throw new GitHubApiException(response.headers().allValues("status").get(0));
    } catch (InterruptedException | IOException e) {
      e.printStackTrace();
      throw new GitHubApiException(e.getMessage());
    }
  }

}
