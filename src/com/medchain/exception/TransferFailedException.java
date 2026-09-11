package com.medchain.exception;

/**
 * Package: com.medchain.exception
 * Purpose: Custom checked exception thrown when an inventory transfer request fails to execute or route.
 */
public class TransferFailedException extends Exception {
    // [CONCEPT: Custom Exception Handling]
    public TransferFailedException(String message) {
        super(message);
    }
}
