package org.example.exceptions;

public class GithubUserDoesNotExistException extends Exception {

    public GithubUserDoesNotExistException(String username) {
        super("User " + username + " does not exist");
    }
}
