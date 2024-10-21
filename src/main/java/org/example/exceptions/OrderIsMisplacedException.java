package org.example.exceptions;

public class OrderIsMisplacedException extends RuntimeException {
  public OrderIsMisplacedException(String message) {
    super(message);
  }
}
