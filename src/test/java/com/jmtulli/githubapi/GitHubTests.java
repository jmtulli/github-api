package com.jmtulli.githubapi;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
public class GitHubTests {

  @Autowired
  private MockMvc gitTestingMock;

  @Test
  public void redirectTest() throws Exception {
    this.gitTestingMock.perform(get("/")).andDo(print()).andExpect(status().is(302));
  }

  @Test
  public void invalidUrlTest() throws Exception {
    this.gitTestingMock.perform(get("/0000")).andDo(print()).andExpect(status().is(404)).andExpect(content().string(""));
  }

  @Test
  public void invalidGitRepositoryTest() throws Exception {
    this.gitTestingMock.perform(get("/invalidUser/invalidRepository")).andDo(print()).andExpect(status().is(404)).andExpect(content().string("Url https://github.com/invalidUser/invalidRepository not found"));
  }

  @Test
  public void initialRequestTest() throws Exception {
    this.gitTestingMock.perform(get("/jmtulli/trustly_api")).andDo(print()).andExpect(status().is(307));
  }

}
