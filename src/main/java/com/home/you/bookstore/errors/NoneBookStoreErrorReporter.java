package com.home.you.bookstore.errors;

class NoneBookStoreErrorReporter implements BookStoreErrorReporter {
    static final BookStoreErrorReporter INSTANCE = new NoneBookStoreErrorReporter();

    private NoneBookStoreErrorReporter() {}

    @Override
    public void report(String message) {
        // Do nothing
    }

    @Override
    public void report(String message, Throwable throwable) {
        // Do nothing
    }
}
