package com.home.you.bookstore.basket;

import com.home.you.bookstore.book.Book;

import java.util.List;

public interface Basket {
	void add(List<Book> books);

	void remove(List<Book> books);
	
	List<Book> getBooks();

	void clear();

	static Basket create() {
		return new BasketImpl();
	}
}
