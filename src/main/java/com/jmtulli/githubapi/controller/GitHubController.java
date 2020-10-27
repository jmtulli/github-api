package com.jmtulli.githubapi.controller;

import org.springframework.cache.annotation.Cacheable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import com.jmtulli.githubapi.GitHubAPI;

@RestController
public class GitHubController {

  @GetMapping(path = "/{gitUser}/{repository}")
//  @Cacheable(value = "gitRepo")
  public ResponseEntity startProcess(@PathVariable String gitUser, @PathVariable String repository) {
    return GitHubAPI.startProcess(gitUser, repository);
  }

  @GetMapping(path = "/{id}")
  public ResponseEntity checkProcess(@PathVariable String id) {
    return (GitHubAPI.checkProcess(id) != null) ? ResponseEntity.ok(GitHubAPI.checkProcess(id)) : ResponseEntity.status(HttpStatus.TEMPORARY_REDIRECT).body("Processing requests... Check result on: https://jmtulli-githubapi.herokuapp.com/   http://localhost:8080/" + id);
  }

}
