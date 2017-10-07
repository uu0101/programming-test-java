package com.home.you.bookstore.rest.server;

import com.home.you.bookstore.basket.Basket;
import com.home.you.bookstore.booklist.BookList;
import com.home.you.bookstore.errors.BookStoreErrorReporter;

import java.io.IOException;

public interface BookStoreServer {
	void start() throws IOException;

	void stop() throws IOException;

	static BookStoreServer.Builder builder() {
		return BookStoreServerImpl.newBuilder();
	}

	interface Builder {
		Builder withPort(int port);

		Builder withBookList(BookList bookList);

		Builder withBasket(Basket basket);

		Builder withBookStoreErrorReporter(BookStoreErrorReporter reporter);

		BookStoreServer build();
	}
}
