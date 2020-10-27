package com.jmtulli.githubapi.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import com.jmtulli.githubapi.GitHubAPI;

@RestController
public class GitHubController {

  @GetMapping(path = "/{gitUser}/{repository}")
  public ResponseEntity startProcess(@PathVariable String gitUser, @PathVariable String repository) {
    return GitHubAPI.startProcess(gitUser, repository);
  }

  @GetMapping(path = "/{id}")
  public ResponseEntity checkProcess(@PathVariable String id) {
    return GitHubAPI.processResult(id);
  }

}
