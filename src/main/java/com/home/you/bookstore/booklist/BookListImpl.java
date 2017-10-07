package com.home.you.bookstore.booklist;

import static com.home.you.bookstore.Constants.*;
import static com.home.you.bookstore.results.Status.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import com.home.you.bookstore.book.Book;
import com.home.you.bookstore.results.Status;

class BookListImpl implements BookList {
	private final List<Book> books;

	BookListImpl() {
		this(new ArrayList<>());
	}

	/**
	 * Used by unit test
	 */
	BookListImpl(List<Book> books) {
		this.books = books;
	}

	@Override
	public Book[] list(String searchString) {
		final List<Book> filteredList;
		if (isBrowsing(searchString)) {
			filteredList = this.books;
		} else {
			filteredList = books.stream().filter(book -> book.matchTitleOrAuthor(searchString))
					.collect(Collectors.toList());
		}

		return filteredList.toArray(new Book[0]);
	}

	@Override
	public boolean add(Book book, int quantity) {
		boolean result = true;
		final Optional<Book> bOpt = books.stream().filter(b -> b.isSame(book)).findFirst();
		if (bOpt.isPresent()) {
			bOpt.get().addQuantity(quantity);
		} else {
			result = books.add(book);
		}
		return result;
	}

	@Override
	public int[] buy(Book... books) {
		final int[] statusCode = new int[books.length];
		for (int i = 0; i < books.length; i++) {
			final Book book = books[i];
			final Optional<Book> stockBook = this.books.stream().filter(b -> Book.isSame(book, b.getTitle(), b.getAuthor(), b.getPrice()))//TODO:book
					.findFirst();
			final AtomicReference<Status> status = new AtomicReference<>(DOES_NOT_EXIST);
			stockBook.ifPresent(b -> {
				if (b.inStock()) {
					status.set(OK);
					b.decreaseQuantity();
				} else {
					status.set(NOT_IN_STOCK);
				}
			});
			statusCode[i] = status.get().value();
		}
		return statusCode;
	}

	@Override
	public Optional<Book> find(Predicate<Book> predicate) {
		return books.stream().filter(predicate).findFirst();
	}

	private static boolean isBrowsing(String searchString) {
		return searchString == null || EMPTY_STRING.equals(searchString);
	}
}
