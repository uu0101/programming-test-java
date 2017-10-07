package com.home.you.bookstore.book;

import com.home.you.bookstore.utils.ListWrapper;

import java.util.List;

class BooksImpl extends ListWrapper<Book> implements Books {
    BooksImpl(List<Book> books) {
        super(books);
    }
}
