package com.jmtulli.githubapi.exception;

public class GitHubApiException extends RuntimeException {
  public GitHubApiException(String url) {
    super(url);
  }
}
