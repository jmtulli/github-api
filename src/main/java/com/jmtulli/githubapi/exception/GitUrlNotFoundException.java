package com.jmtulli.githubapi.exception;

public class GitUrlNotFoundException extends RuntimeException {
  public GitUrlNotFoundException(String url) {
    super(url);
  }
}
