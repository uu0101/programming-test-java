package com.home.you.bookstore.results;

import com.home.you.bookstore.utils.ListWrapper;

import java.util.List;

class StatusesImpl extends ListWrapper<Status> implements Statuses {
    StatusesImpl(List<Status> list) {
        super(list);
    }
}
