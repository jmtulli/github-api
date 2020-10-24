package com.jmtulli.githubapi.web;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.http.HttpResponse.BodyHandlers;

public class Connection {

  private HttpClient httpClient;
  private HttpRequest request;

  public Connection(String url) {
    this(url, null, null);
  }

  public Connection(String url, String headerName, String headerValue) {
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
      // System.out.println("Conection to " + url + " - Status: " + response.statusCode());
    } catch (IOException | InterruptedException e) {
      e.printStackTrace();
      return null;
    }
    return response.body();
  }

}
