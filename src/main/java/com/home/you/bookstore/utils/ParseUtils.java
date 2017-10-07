package com.home.you.bookstore.utils;

import java.math.BigDecimal;

import static com.home.you.bookstore.Constants.COMMA;
import static com.home.you.bookstore.Constants.EMPTY_STRING;

public class ParseUtils {

    private ParseUtils() {}

    public static int parseIntOrDefault(String value, int defaultValue) {
        int result = defaultValue;
        try {
            result = Integer.parseInt(value);
        } catch (NumberFormatException e) {
            // ignore
        }
        return result;
    }

    public static BigDecimal parseBigDecimal(String value) {
        final String trimmedValue = value.replaceAll(COMMA, EMPTY_STRING);
        return new BigDecimal(trimmedValue);
    }
}
