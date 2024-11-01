package org.example.exceptions;

public class GithubRequestTimeoutException extends Throwable {
    public GithubRequestTimeoutException(String message, Throwable cause) {
        super(message, cause);
    }
}
