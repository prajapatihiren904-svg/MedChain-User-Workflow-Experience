package com.medchain.exception;

/**
 * Package: com.medchain.exception
 * Purpose: Custom checked exception thrown when registering a user with an already registered email.
 */
public class DuplicateUserException extends Exception {
    // [CONCEPT: Custom Exception Handling]
    public DuplicateUserException(String message) {
        super(message);
    }
}
