package org.example.exceptions;

public class InvalidUsernameAndPasswordException extends RuntimeException {
    public InvalidUsernameAndPasswordException(String message) {
        super(message);
    }
}
