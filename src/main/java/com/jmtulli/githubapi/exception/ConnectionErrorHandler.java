package com.jmtulli.githubapi.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class ConnectionErrorHandler {

  @ResponseStatus(value = HttpStatus.NOT_FOUND)
  @ExceptionHandler(GitUrlNotFoundException.class)
  public String handle(GitUrlNotFoundException ex) {
    return "Url " + ex.getMessage() + " not found";
  }

  @ResponseStatus(value = HttpStatus.BAD_REQUEST)
  @ExceptionHandler(GitHubApiException.class)
  public String handle(GitHubApiException ex) {
    return ex.getMessage();
  }

}
