package com.medchain.exception;

/**
 * Package: com.medchain.exception
 * Purpose: Custom checked exception thrown when a pharmacy owner tries to login before admin approval.
 */
public class PharmacyNotApprovedException extends Exception {
    // [CONCEPT: Custom Exception Handling]
    public PharmacyNotApprovedException(String message) {
        super(message);
    }
}
