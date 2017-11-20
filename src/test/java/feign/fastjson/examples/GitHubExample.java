package feign.fastjson.examples;

import java.util.List;

import feign.Feign;
import feign.Param;
import feign.RequestLine;
import feign.fastjson.FastjsonDecoder;

/**
 * adapted from {@code com.example.retrofit.GitHubClient}
 */
public class GitHubExample {

  public static void main(String... args) {
    GitHub github = Feign.builder()
        .decoder(new FastjsonDecoder())
        .target(GitHub.class, "https://api.github.com");

    System.out.println("Let's fetch and print a list of the contributors to this library.");
    List<Contributor> contributors = github.contributors("netflix", "feign");
    for (Contributor contributor : contributors) {
      System.out.println(contributor.login + " (" + contributor.contributions + ")");
    }
  }

  interface GitHub {

    @RequestLine("GET /repos/{owner}/{repo}/contributors")
    List<Contributor> contributors(@Param("owner") String owner, @Param("repo") String repo);
  }

  static class Contributor {

    private String login;
    private int contributions;

    public void setLogin(String login) {
      this.login = login;
    }

    public void setContributions(int contributions) {
      this.contributions = contributions;
    }
  }
}
