package com.home.you.bookstore.rest.client;

import com.home.you.bookstore.book.Book;
import com.home.you.bookstore.book.Books;
import com.home.you.bookstore.errors.BookStoreErrorReporter;
import com.home.you.bookstore.results.PurchaseResult;

import java.util.Collections;
import java.util.List;

class BookStoreClientImpl implements BookStoreClient {
	private final BookStoreErrorReporter handler;
	private final BookStoreConnection connection;
	
	BookStoreClientImpl(BookStoreErrorReporter handler, BookStoreConnection connection) {
		this.handler = handler;
		this.connection = connection;
	}

	@Override
	public String command(String command) {
		String message = "";
		try {
			message = connection.command(command);
		} catch (Throwable e) {
			handler.report("Failed to execute command \"" + command + "\"", e);
		}
		return message;
	}

	@Override
	public Books getBooks() {
		List<Book> books = Collections.emptyList();
		try {
			books = connection.readBooks();
		} catch (Throwable e) {
			handler.report("Failed to get all books from store", e);
		}
		return Books.of(books);
	}

	@Override
	public Books findBooks(String searchString)  {
		List<Book> books = Collections.emptyList();
		try {
			books = connection.readBooks(searchString);
		} catch (Throwable e) {
			handler.report("Failed to get book by search \"" + searchString + "\" from store", e);
		}
		return Books.of(books);
	}

	@Override
	public Books getAllBooksFromBasket() {
		List<Book> books = Collections.emptyList();
		try {
			books = connection.readBasket();
		} catch (Throwable e) {
			handler.report("Failed to get all books from basket", e);
		}
		return Books.of(books);
	}

	@Override
	public void addBooksToStore(Books books) {
		try {
			connection.addBooksToStore(books);
		} catch (Throwable e) {
			handler.report("Failed to add books \"" + books + "\" to store", e);
		}
	}

	@Override
	public void addBooksToBasket(List<Integer> ids) {
		try {
			connection.addBooksToBasket(ids);
		} catch (Throwable e) {
			handler.report("Failed to add books by ids \"" + ids + "\" to basket", e);
		}
	}

	@Override
	public void removeBooksToBasket(List<Integer> ids) {
		try {
			connection.removeBooksToBasket(ids);
		} catch (Throwable e) {
			handler.report("Failed to remove books by ids \"" + ids + "\" from basket", e);
		}
	}

	@Override
	public PurchaseResult buyBasket() {
		PurchaseResult result = null;
		try {
			result = connection.buyBasket();
		} catch (Throwable e) {
			handler.report("Failed to buy basket", e);
		}
		return result;
	}
}
