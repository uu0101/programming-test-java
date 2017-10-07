package com.home.you.bookstore.utils;

import java.util.Collections;
import java.util.Iterator;
import java.util.List;

public abstract class ListWrapper<T> {
    private final List<T> list;

    protected ListWrapper(List<T> list) {
        this.list = Collections.unmodifiableList(list);
    }

    public Iterator<T> iterator() {
        return list.iterator();
    }

    public int size() {
        return list.size();
    }

    @Override
    public boolean equals(Object o) {
        return list.equals(o);
    }

    @Override
    public int hashCode() {
        return list.hashCode();
    }

    @Override
    public String toString() {
        return list.toString();
    }
}
