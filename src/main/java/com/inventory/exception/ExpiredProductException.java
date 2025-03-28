package com.inventory.exception;

/**
 * Custom exception thrown when a product is expired.
 */
public class ExpiredProductException extends RuntimeException {

    public ExpiredProductException(String message) {
        super(message);
    }
}