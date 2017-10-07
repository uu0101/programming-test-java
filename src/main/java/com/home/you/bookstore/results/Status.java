package com.home.you.bookstore.results;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public enum Status {
    OK(0), NOT_IN_STOCK(1), DOES_NOT_EXIST(2);

    private final int statusCode;

    Status(int quantity) {
        this.statusCode = quantity;
    }

    public int value() {
        return statusCode;
    }

    public static Status valueOf(int value) {
        Status result = null;
        for (Status status : values()) {
            if (status.value() == value) {
                result = status;
                break;
            }
        }
        return result;
    }

    public static List<Status> asList(int[] statusCodes) {
        final List<Status> statuses = new ArrayList<>();
        for (int statusCode : statusCodes) {
            statuses.add(valueOf(statusCode));
        }
        return Collections.unmodifiableList(statuses);
    }
}
