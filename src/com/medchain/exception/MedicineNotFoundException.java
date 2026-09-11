package com.medchain.exception;

/**
 * Package: com.medchain.exception
 * Purpose: Custom checked exception thrown when a medicine is not found in the catalog or network.
 */
public class MedicineNotFoundException extends Exception {
    // [CONCEPT: Custom Exception Handling]
    public MedicineNotFoundException(String message) {
        super(message);
    }
}
