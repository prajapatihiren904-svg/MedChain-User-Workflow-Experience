package com.medchain.util;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

/**
 * Package: com.medchain.util
 * Purpose: Handles date calculations, string parsing, and format conversions between Java and SQL types.
 */
public class DateUtil {
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    public static LocalDate toLocalDate(java.sql.Date sqlDate) {
        return sqlDate == null ? null : sqlDate.toLocalDate();
    }

    public static java.sql.Date toSqlDate(LocalDate localDate) {
        return localDate == null ? null : java.sql.Date.valueOf(localDate);
    }

    public static LocalDate parseDate(String dateStr) {
        try {
            return LocalDate.parse(dateStr, FORMATTER);
        } catch (DateTimeParseException e) {
            return null;
        }
    }

    public static String formatDate(LocalDate date) {
        return date == null ? "" : date.format(FORMATTER);
    }
}
