package com.jmtulli.githubapi.controller;

import static com.jmtulli.githubapi.util.ApplicationConstants.URL_GITHUB_API;

import java.io.IOException;

import javax.servlet.http.HttpServletResponse;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import com.jmtulli.githubapi.GitHubAPI;

/**
 * Controll all the requests from server.
 * 
 * @author Jose Tulli
 *
 */
@RestController
public class GitHubController {

  /**
   * Redirect the client to the instructions page
   * 
   * @param response - The object used to redirect client
   */
  @GetMapping(path = "/")
  public void instructions(HttpServletResponse response) {
    try {
      response.sendRedirect(URL_GITHUB_API);
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  /**
   * Check the progress of the request
   * 
   * @param id - The ID of the requested Repository
   * @return ResponseEntity - Status of the given request
   */
  @GetMapping(path = "/{id}")
  public ResponseEntity checkProcess(@PathVariable String id) {
    return GitHubAPI.checkResult(id);
  }

  /**
   * Start the processing of the given repository
   * 
   * @param gitUser - The username of the github repository
   * @param repository - The name of the github repository
   * @return ResponseEntity - Status of the given request
   */
  @GetMapping(path = "/{gitUser}/{repository}")
  public ResponseEntity startProcess(@PathVariable String gitUser, @PathVariable String repository) {
    return GitHubAPI.startProcess(gitUser, repository);
  }

}
