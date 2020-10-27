package com.jmtulli.githubapi.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import com.jmtulli.githubapi.queue.Receiver;

@RestControllerAdvice
public class ConnectionErrorHandler {

  @ResponseStatus(value = HttpStatus.NOT_FOUND)
  @ExceptionHandler(GitUrlNotFoundException.class)
  public String handle(GitUrlNotFoundException ex) {
    return "Url " + ex.getMessage() + " not found";
  }

  @ResponseStatus(value = HttpStatus.INTERNAL_SERVER_ERROR)
  @ExceptionHandler(GitHubApiException.class)
  public String handle(GitHubApiException ex) {
    Receiver.errorsFound = ex.getMessage();
    System.out.println("erro internal server");
    return ex.getMessage();
  }

}
