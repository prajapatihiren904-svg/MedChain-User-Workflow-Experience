package com.medchain.util;

/**
 * Package: com.medchain.util
 * Purpose: Validates menu inputs, emails, phone numbers and other text values.
 */
public class InputValidator {
    
    public static boolean isValidEmail(String email) {
        if (email == null) return false;
        return email.matches("^[A-Za-z0-9+_.-]+@(.+)$");
    }

    public static boolean isValidPhone(String phone) {
        if (phone == null) return false;
        // Validates standard 10 digit Indian mobile numbers
        return phone.matches("^[6-9]\\d{9}$");
    }

    public static int validateMenuChoice(String input, int min, int max) {
        if (input == null) return -1;
        try {
            int choice = Integer.parseInt(input.trim());
            if (choice >= min && choice <= max) {
                return choice;
            }
        } catch (NumberFormatException e) {
            // Silence number format exceptions and return -1
        }
        return -1;
    }

    public static boolean isValidPassword(String password) {
        if (password == null || password.length() < 6) {
            return false;
        }
        // Must contain at least one digit and must NOT be entirely numeric
        return password.matches(".*\\d.*") && !password.matches("^\\d+$");
    }
}
