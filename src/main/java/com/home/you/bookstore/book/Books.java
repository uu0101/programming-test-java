package com.home.you.bookstore.book;

import java.util.List;

public interface Books extends Iterable<Book> {
    int size();

    static Books of(List<Book> books) {
        return new BooksImpl(books);
    }
}
