package com.home.you.bookstore.basket;

import com.home.you.bookstore.book.Book;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

class BasketImpl implements Basket {
	private List<Book> basket = new ArrayList<>();

	BasketImpl() {}
	
	@Override
	public void add(List<Book> books) {
		basket.addAll(books);
	}

	@Override
	public void remove(List<Book> books) {
		books.forEach(book -> {
			final int index = basket.indexOf(book);
			if(index >= 0) {
				basket.remove(index);
			}
		});
	}
	
	@Override
	public List<Book> getBooks() {
		return Collections.unmodifiableList(basket);
	}
	
	@Override
	public void clear() {
		basket.clear();
	}
}
