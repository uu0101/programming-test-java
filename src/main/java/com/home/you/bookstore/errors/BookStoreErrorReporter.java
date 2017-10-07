package com.home.you.bookstore.errors;

public interface BookStoreErrorReporter {
    void report(String message);

    void report(String message, Throwable throwable);

    static BookStoreErrorReporter none() {
        return NoneBookStoreErrorReporter.INSTANCE;
    }
}
