package com.home.you.bookstore.rest.client;

import java.io.IOException;
import java.util.List;

import com.home.you.bookstore.book.Book;
import com.home.you.bookstore.results.PurchaseResult;

public interface BookStoreConnection {
	String command(String command) throws IOException;

	List<Book> readBooks() throws IOException;

	List<Book> readBooks(String searchString) throws IOException;

	List<Book> readBasket() throws IOException;

	void addBooksToStore(Iterable<Book> books) throws IOException;

	void addBooksToBasket(List<Integer> books) throws IOException;

	void removeBooksToBasket(List<Integer> ids) throws IOException;

	PurchaseResult buyBasket() throws IOException;

	static BookStoreConnection create(String address, int port) {
		return new BookStoreConnectionImpl(address, port);
	}
}
