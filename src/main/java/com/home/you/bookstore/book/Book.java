package com.home.you.bookstore.book;

import java.math.BigDecimal;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;

public class Book {
    private static final int NO_ID = -1;

    private final int id;
    private final String title;
    private final String author;
    private final BigDecimal price;
    private final AtomicInteger quantity;

    public Book(int id, String title, String author, BigDecimal price, int quantity) {
        this.id = id;
        this.title = title;
        this.author = author;
        this.price = price;
        this.quantity = new AtomicInteger(quantity);
    }
    public Book(String title, String author, BigDecimal price, int quantity) {
        this.id = NO_ID;
        this.title = title;
        this.author = author;
        this.price = price;
        this.quantity = new AtomicInteger(quantity);
    }

    public boolean hasId() {
        return id != NO_ID;
    }

    public int getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public String getAuthor() {
        return author;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public boolean inStock() {
        return quantity.get() > 0;
    }

    public void decreaseQuantity() { quantity.decrementAndGet(); }

    public int getQuantity() {
        return quantity.get();
    }

    public void addQuantity(int value) {
        quantity.addAndGet(value);
    }

    public boolean matchTitleOrAuthor(String searchString) {
        return title.toLowerCase().contains(searchString.toLowerCase()) || author.toLowerCase().contains(searchString.toLowerCase());
    }

    public boolean isSame(Book book) {
        return Book.isSame(this, book.title, book.author, book.price);
    }

    public static boolean isSame(Book book, String title, String author, BigDecimal price) {
        return title.equalsIgnoreCase(book.title) && author.equalsIgnoreCase(book.author) && price.equals(book.price);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        final Book other = (Book) o;
        final boolean result = Objects.equals(id, other.id) &&
                Objects.equals(title, other.title) &&
                Objects.equals(author, other.author) &&
                Objects.equals(price, other.price) &&
                Objects.equals(quantity.get(), other.quantity.get());
        return result;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, title, author, price, quantity.get());
    }

    @Override
    public String toString() {
        return "Book [id=" + id + ", title=" + title + ", author=" + author + ", price=" + price + ", quantity="
                + quantity + "]";
    }
}
