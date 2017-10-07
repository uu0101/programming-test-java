package com.home.you.bookstore;

@FunctionalInterface
public interface AssertionTask<T> {
    void execute(T arg);
}
