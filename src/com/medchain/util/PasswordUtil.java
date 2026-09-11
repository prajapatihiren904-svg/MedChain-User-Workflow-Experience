package com.medchain.util;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * Package: com.medchain.util
 * Purpose: Provides utility functions for password hashing using simple SHA-256.
 */
public class PasswordUtil {
    // [CONCEPT: String Handling]
    public static String hashPassword(String password) {
        if (password == null) return null;
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] encodedhash = digest.digest(password.getBytes());
            
            StringBuilder hexString = new StringBuilder(2 * encodedhash.length);
            for (byte b : encodedhash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) {
                    hexString.append('0');
                }
                hexString.append(hex);
            }
            return hexString.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("SHA-256 Algorithm not found", e);
        }
    }

    public static boolean checkPassword(String plainPassword, String hashedPassword) {
        if (plainPassword == null || hashedPassword == null) return false;
        return hashPassword(plainPassword).equals(hashedPassword);
    }
}
