package com.medchain.util;

/**
 * Package: com.medchain.util
 * Purpose: Handles statutory calculations such as CGST/SGST/IGST breakdown (fixed at 12% for medical supplies).
 */
public class GSTCalculator {
    public static final double GST_RATE = 0.12;

    public static double calculateGST(double subtotal) {
        return Math.round((subtotal * GST_RATE) * 100.0) / 100.0;
    }

    public static double calculateTotal(double subtotal) {
        return Math.round((subtotal + calculateGST(subtotal)) * 100.0) / 100.0;
    }
}
