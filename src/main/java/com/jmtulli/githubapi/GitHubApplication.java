package com.jmtulli.githubapi;

import org.springframework.amqp.rabbit.annotation.EnableRabbit;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;

@SpringBootApplication
@EnableCaching
@EnableRabbit
public class GitHubApplication {

  public static void main(String[] args) {
    SpringApplication.run(GitHubApplication.class, args);
  }

}
