package org.example.exceptions;

public class ResponseStatusException extends RuntimeException {
    public ResponseStatusException(String message) {
        super(message);
    }
}
