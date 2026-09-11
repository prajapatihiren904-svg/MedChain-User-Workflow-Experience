package com.medchain.exception;

/**
 * Package: com.medchain.exception
 * Purpose: Custom checked exception thrown when inventory stock is insufficient for a request.
 */
public class StockOutException extends Exception {
    // [CONCEPT: Custom Exception Handling]
    private int availableStock;

    public StockOutException(String message) {
        super(message);
        this.availableStock = 0;
    }

    public StockOutException(String message, int availableStock) {
        super(message);
        this.availableStock = availableStock;
    }

    public int getAvailableStock() {
        return availableStock;
    }
}
