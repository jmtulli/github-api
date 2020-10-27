package com.jmtulli.githubapi.controller;

import static com.jmtulli.githubapi.util.ApplicationConstants.URL_GITHUB_API;

import java.io.IOException;

import javax.servlet.http.HttpServletResponse;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import com.jmtulli.githubapi.GitHubAPI;

@RestController
public class GitHubController {

  @GetMapping(path = "/")
  public void instructions(HttpServletResponse response) {
    try {
      response.sendRedirect(URL_GITHUB_API);
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  @GetMapping(path = "/{id}")
  public ResponseEntity checkProcess(@PathVariable String id, HttpServletResponse response) {
    return GitHubAPI.processResult(id);
  }

  @GetMapping(path = "/{gitUser}/{repository}")
  public ResponseEntity startProcess(@PathVariable String gitUser, @PathVariable String repository) {
    return GitHubAPI.startProcess(gitUser, repository);
  }

}
