package com.jmtulli.githubapi.web;

import static com.jmtulli.githubapi.util.ApplicationConstants.URL_ALL_BRANCHES;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.http.HttpResponse.BodyHandlers;

import com.jmtulli.githubapi.exception.GitHubApiException;
import com.jmtulli.githubapi.exception.GitUrlNotFoundException;

public class Connection {

  private HttpClient httpClient;
  private HttpRequest request;
  private String url;

  public Connection(String url) {
    this(url, null, null);
  }

  public Connection(String url, String headerName, String headerValue) {
    this.url = url;
    this.httpClient = HttpClient.newHttpClient();

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
      if (response.statusCode() == 200) {
        return response.body();
      } else if (response.statusCode() == 404) {
        throw new GitUrlNotFoundException(url.substring(0, url.indexOf(URL_ALL_BRANCHES)));
      }
      throw new GitHubApiException(response.headers().allValues("status").get(0));
    } catch (IOException | InterruptedException e) {
      e.printStackTrace();
      return null;
    }
  }

}
