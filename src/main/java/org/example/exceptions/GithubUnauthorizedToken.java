package org.example.exceptions;

public class GithubUnauthorizedToken  extends Exception {
    public GithubUnauthorizedToken() {
        super("The token doesn't have permission to access this resource");
    }
}
