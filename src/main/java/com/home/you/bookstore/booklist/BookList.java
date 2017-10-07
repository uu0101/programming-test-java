package com.home.you.bookstore.booklist;

import java.util.Optional;
import java.util.function.Predicate;

import com.home.you.bookstore.book.Book;

public interface BookList {
	Book[] list(String searchString);

	boolean add(Book book, int quantity);

	int[] buy(Book... books);

	Optional<Book> find(Predicate<Book> predicate);

	static BookList create() {
		return new BookListImpl();
	}
}
