package com.jmtulli.githubapi.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import com.jmtulli.githubapi.GitHubApi;

@RestController
public class GitHubController {

  @GetMapping(path = "/repository")
  public String startProcess() {
    GitHubApi.startProcess();
    return "Hello";
  }
}
