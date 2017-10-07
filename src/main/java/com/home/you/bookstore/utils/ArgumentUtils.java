package com.home.you.bookstore.utils;

import java.math.BigDecimal;

import static com.home.you.bookstore.utils.ParseUtils.parseIntOrDefault;

public class ArgumentUtils {

    private ArgumentUtils() {}

    public static int extractIntegerOrDefault(String[] args, int index, int defaultValue) {
        int value = defaultValue;
        if(args != null && args.length > index) {
            final String valueAsString = args[index];
            value = parseIntOrDefault(valueAsString, defaultValue);
        }
        return value;
    }

    public static int extractIntegerOrThrow(String[] args, int index, String message) {
        final int value;
        if(args != null && args.length > index) {
            final String valueAsString = args[index];
            value = Integer.parseInt(valueAsString);
        } else {
            throw new IllegalArgumentException(message);
        }
        return value;
    }

    public static BigDecimal extractBigDecimalOrThrow(String[] args, int index, String message) {
        final BigDecimal value;
        if(args != null && args.length > index) {
            final String valueAsString = args[index];
            value = ParseUtils.parseBigDecimal(valueAsString);
        } else {
            throw new IllegalArgumentException(message);
        }
        return value;
    }

    public static String extractStringOrDefault(String[] args, int index, String defaultValue) {
        String value = defaultValue;
        if(args != null && args.length > index) {
            value = args[index];
        }
        return value;
    }

    public static String extractStringOrThrow(String[] args, int index, String message) {
        final String value;
        if(args != null && args.length > index) {
            value = args[index];
        } else {
            throw new IllegalArgumentException(message);
        }
        return value;
    }
}