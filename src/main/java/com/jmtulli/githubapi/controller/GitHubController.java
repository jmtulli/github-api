package com.jmtulli.githubapi.controller;

import java.util.Map;

import org.springframework.cache.annotation.Cacheable;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import com.jmtulli.githubapi.GitHub;
import com.jmtulli.githubapi.data.FileCounters;

@RestController
public class GitHubController {

  @GetMapping(path = "/{gitUser}/{repository}")
//  @Cacheable(value = "gitRepo")
  public Map<String, FileCounters> startProcess(@PathVariable String gitUser, @PathVariable String repository) {
    return GitHub.startProcess(gitUser, repository);
  }

}
