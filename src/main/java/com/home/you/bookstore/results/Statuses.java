package com.home.you.bookstore.results;

import java.util.List;

public interface Statuses extends Iterable<Status> {
    static Statuses of(List<Status> statuses) {
        return new StatusesImpl(statuses);
    }
}
