package com.home.you.bookstore.rest.client;

import com.home.you.bookstore.book.Books;
import com.home.you.bookstore.errors.BookStoreErrorReporter;
import com.home.you.bookstore.results.PurchaseResult;

import java.util.List;

public interface BookStoreClient {
    String command(String command);

    Books getBooks();

    Books findBooks(String searchString);

    Books getAllBooksFromBasket();

    void addBooksToStore(Books books);

    void addBooksToBasket(List<Integer> bookIds);

    void removeBooksToBasket(List<Integer> bookIds);

    PurchaseResult buyBasket();

    static BookStoreClient create(BookStoreErrorReporter reporter, BookStoreConnection connection) {
        return new BookStoreClientImpl(reporter, connection);
    }
}
