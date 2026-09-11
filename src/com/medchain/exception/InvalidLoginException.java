package com.medchain.exception;

/**
 * Package: com.medchain.exception
 * Purpose: Custom checked exception thrown when authentication credentials are invalid.
 */
public class InvalidLoginException extends Exception {
    // [CONCEPT: Custom Exception Handling]
    public InvalidLoginException(String message) {
        super(message);
    }
}
