package org.example.exceptions;

public class GithubConnectionException extends Throwable {
    public GithubConnectionException(String message, Throwable cause) {
        super(message, cause);
    }
}
