package com.jmtulli.githubapi.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class GitHubController {

  @GetMapping
  public String Hello() {
    return "Hello";
  }
}
