package org.example.exceptions;

public class GithubUserDoesNotHaveAccessToRepo extends Exception {

    public GithubUserDoesNotHaveAccessToRepo(String username, String repo) {
        super("User " + username + " does not have a repository named " + repo + ", or the token does not have enough permissions to access it!");
    }
}