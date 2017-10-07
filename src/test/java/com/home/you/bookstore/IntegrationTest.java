package com.home.you.bookstore;


import com.home.you.bookstore.basket.Basket;
import com.home.you.bookstore.book.Book;
import com.home.you.bookstore.book.Books;
import com.home.you.bookstore.errors.BookStoreErrorReporter;
import com.home.you.bookstore.rest.client.BookStoreClient;
import com.home.you.bookstore.rest.client.BookStoreConnection;
import com.home.you.bookstore.rest.server.BookStoreServer;
import com.home.you.bookstore.results.PurchaseResult;
import com.home.you.bookstore.results.Status;
import com.home.you.bookstore.results.Statuses;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

import static com.home.you.bookstore.Constants.FIRST;
import static com.home.you.bookstore.Constants.SECOND;
import static com.home.you.bookstore.booklist.TestBookListImpl.createBookList;
import static com.home.you.bookstore.results.Status.*;
import static com.home.you.bookstore.utils.ParseUtils.parseBigDecimal;
import static java.util.Collections.singletonList;
import static org.fest.assertions.Assertions.assertThat;

public class IntegrationTest implements BookStoreErrorReporter {
    private static final String NEW_TITILE = "Black Horse";
    private static final String NEW_AUTHOR = "Carl Svensson";
    private static final BigDecimal NEW_PRICE = BigDecimal.valueOf(499.99);
    private static final int NEW_QUANTITY = 10;
    private static final Book NEW_BOOK = new Book(NEW_TITILE, NEW_AUTHOR, NEW_PRICE, NEW_QUANTITY);
    private static final String EXISTING_TITLE_1 = "Random Sales";
    private static final String EXISTING_AUTHOR_1 = "Cunning Bastard";
    private static final BigDecimal EXISTING_PRICE_1 = parseBigDecimal("999.00");
    private static final int EXISTING_QUANTITY_1 = 10;
    private static final String EXISTING_TITLE_2 = "Mastering åäö";
    private static final String EXISTING_AUTHOR_2 = "Average Swede";
    private static final BigDecimal EXISTING_PRICE_2 = parseBigDecimal("762.00");
    private static final String LOCAL_ADDRESS = "localhost";
    private static final int DEFAULT_PORT = 5542;
    private static final AssertionTask<Book> NO_EXTRA_ASSERTION = book -> {};

    private static final AtomicBoolean ourServerErrorFlag = new AtomicBoolean(false);

    private final Basket serverBasket = Basket.create();
    private final List<Book> serverBookList = new ArrayList<>();
    private BookStoreServer server;
    private BookStoreClient client;

    @Before
    public void setup() throws Exception {
        server = BookStoreServer.builder()
                .withPort(DEFAULT_PORT)
                .withBookList(createBookList(serverBookList))
                .withBasket(serverBasket)
                .withBookStoreErrorReporter(this)
                .build();
        server.start();
        final BookStoreConnection connection = BookStoreConnection.create(LOCAL_ADDRESS, DEFAULT_PORT);
        client = BookStoreClient.create(this, connection);
    }

    @After
    public void tearDown() throws Exception {
//        Thread.sleep(100);
        server.stop();
        if(ourServerErrorFlag.getAndSet(false)) {
            throw new AssertionError("Server error");
        }
    }

    @Test
    public void testGetAllBooks() throws Exception {
        final Books books = client.getBooks();
        assertThat(books)
                .hasSize(7);
        assertBooks(books);
        assertThat(books.equals(serverBookList))
                .isTrue();
    }

    @Test
    public void testSearchForBookByTitle() {
        final Books books = client.findBooks(EXISTING_TITLE_1);
        assertThat(books)
                .hasSize(2);
        assertBooks(books, book ->
                assertThat(book.getTitle())
                        .isEqualTo(EXISTING_TITLE_1));
    }

    @Test
    public void testSearchForBookByAuthor() throws Exception {
        final Books books = client.findBooks(EXISTING_AUTHOR_1);
        assertThat(books)
                .hasSize(2);
        assertBooks(books, book ->
                assertThat(book.getAuthor())
                        .isEqualTo(EXISTING_AUTHOR_1));
    }

    @Test
    public void testSearchForBookBySingleAuthor() throws Exception {
        final Books books = client.findBooks(EXISTING_AUTHOR_2);
        assertThat(books)
                .hasSize(1);
        assertBooks(books, book ->
                assertThat(book.getAuthor())
                        .isEqualTo(EXISTING_AUTHOR_2));
    }

    @Test
    public void testAddNewBook() throws Exception {
        final List<Book> originalList = new ArrayList<>(serverBookList);
        final Book book = new Book(NEW_TITILE, NEW_AUTHOR, NEW_PRICE, NEW_QUANTITY);
        final List<Book> bookList = singletonList(book);
        final Books books = Books.of(bookList);
        client.addBooksToStore(books);
        assertThat(serverBookList)
                .hasSize(8);
        final Book actualBook = findBookFromStorageOrThrow(NEW_TITILE, NEW_AUTHOR, NEW_PRICE);
        assertThat(serverBookList.contains(actualBook))
                .isTrue();
        originalList.forEach(b -> {
            if(b.getId() == actualBook.getId()) {
                throw new AssertionError("Id \"" + actualBook.getId() +"\" must be unique, in list \"" + originalList + "\"");
            }
        });
    }

    @Test
    public void testAddExistingBook() throws Exception {
        final Book original = findBookFromStorageOrThrow(EXISTING_TITLE_1, EXISTING_AUTHOR_1, EXISTING_PRICE_1);
        final int originalQuantity = original.getQuantity();
        final Book book = new Book(EXISTING_TITLE_1, EXISTING_AUTHOR_1, EXISTING_PRICE_1, EXISTING_QUANTITY_1);
        final List<Book> bookList = singletonList(book);
        final Books books = Books.of(bookList);
        client.addBooksToStore(books);
        assertThat(serverBookList)
                .hasSize(7);
        assertThat(original.getQuantity())
                .isEqualTo(originalQuantity + EXISTING_QUANTITY_1);
    }

    @Test
    public void testAddSameTwiceExistingBook() throws Exception {
        final List<Book> bookList = Arrays.asList(NEW_BOOK, NEW_BOOK);
        final Books books = Books.of(bookList);
        client.addBooksToStore(books);
        assertThat(serverBookList)
                .hasSize(8);
        final Book storedBook = findBookFromStorageOrThrow(NEW_TITILE, NEW_AUTHOR, NEW_PRICE);
        assertThat(storedBook.getQuantity())
                .isEqualTo(NEW_QUANTITY + NEW_QUANTITY);
    }

    @Test
    public void testAddToBasket() throws Exception {
        final Book book = findBookFromStorageOrThrow(EXISTING_TITLE_1, EXISTING_AUTHOR_1, EXISTING_PRICE_1);
        final List<Integer> books = Arrays.asList(book.getId(), book.getId());
        client.addBooksToBasket(books);
        assertThat(serverBasket.getBooks())
                .hasSize(2);
        final Book firstStoredBook = serverBasket.getBooks().get(FIRST);
        assertThat(firstStoredBook.isSame(book))
                .isTrue();
        final Book secondStoreBucket = serverBasket.getBooks().get(SECOND);
        assertThat(secondStoreBucket.isSame(book))
                .isTrue();
    }

    @Test
    public void testAddNoneExsitingToBasket() throws Exception {
        final List<Integer> books = singletonList(0);
        client.addBooksToBasket(books);
        assertThat(serverBasket.getBooks())
                .isEmpty();
    }

    @Test
    public void testRemoveFromBucket() throws Exception {
        final Book book = findBookFromStorageOrThrow(EXISTING_TITLE_1, EXISTING_AUTHOR_1, EXISTING_PRICE_1);
        serverBasket.add(singletonList(book));
        final List<Integer> books = singletonList(book.getId());
        assertThat(serverBasket.getBooks())
                .hasSize(1);
        client.removeBooksToBasket(books);
        assertThat(serverBasket.getBooks())
                .isEmpty();
    }

    @Test
    public void testRemoveNoneExistingFromBucket() throws Exception {
        final Book book = findBookFromStorageOrThrow(EXISTING_TITLE_1, EXISTING_AUTHOR_1, EXISTING_PRICE_1);
        serverBasket.add(singletonList(book));
        final List<Integer> books = singletonList(0);
        assertThat(serverBasket.getBooks())
                .hasSize(1);
        client.removeBooksToBasket(books);
        assertThat(serverBasket.getBooks())
                .hasSize(1);
    }

    @Test
    public void testGetAllBooksFromBasket() throws Exception {
        final Book book = findBookFromStorageOrThrow(EXISTING_TITLE_1, EXISTING_AUTHOR_1, EXISTING_PRICE_1);
        serverBasket.add(Arrays.asList(book, book));
        final Books books = client.getAllBooksFromBasket();
        final Iterator<Book> it = books.iterator();
        assertThat(books)
                .hasSize(2);
        final Book firstStoredBook = it.next();
        assertThat(firstStoredBook.isSame(book))
                .isTrue();
        final Book secondStoreBucket = it.next();
        assertThat(secondStoreBucket.isSame(book))
                .isTrue();
    }

    @Test
    public void testBuyingBasket() throws Exception {
        final Book book = findBookFromStorageOrThrow(EXISTING_TITLE_1, EXISTING_AUTHOR_1, EXISTING_PRICE_1);
        final int originalQuantity = book.getQuantity();
        serverBasket.add(Arrays.asList(book, book));
        final PurchaseResult result = client.buyBasket();
        assertThat(result.getTotalPrice().doubleValue())
                .isEqualTo(EXISTING_PRICE_1.doubleValue() * 2);
        final Statuses statuses = result.getStatuses();
        final Iterator<Status> it = statuses.iterator();
        assertThat(statuses)
                .hasSize(2);
        assertThat(book.getQuantity())
                .isEqualTo(originalQuantity -  2);
        final Status firstStatus = it.next();
        assertThat(firstStatus)
                .isEqualTo(OK);
        final Status secondStatus = it.next();
        assertThat(secondStatus)
                .isEqualTo(OK);
    }

    @Test
    public void testNoneExitingBuyingBasket() throws Exception {
        serverBasket.add(singletonList(NEW_BOOK));
        final PurchaseResult result = client.buyBasket();
        assertThat(result.getTotalPrice().doubleValue())
                .isEqualTo(0.0);
        final Statuses statuses = result.getStatuses();
        final Iterator<Status> it = statuses.iterator();
        assertThat(statuses)
                .hasSize(1);
        final Status firstStatus = it.next();
        assertThat(firstStatus)
                .isEqualTo(DOES_NOT_EXIST);
    }

    @Test
    public void testBuyBooksOutOfStock() throws Exception {
        final Book book = findBookFromStorageOrThrow(EXISTING_TITLE_1, EXISTING_AUTHOR_1, EXISTING_PRICE_1);
        book.addQuantity(- book.getQuantity());
        serverBasket.add(singletonList(book));
        final PurchaseResult result = client.buyBasket();
        assertThat(result.getTotalPrice().doubleValue())
                .isEqualTo(0.0);
        final Statuses statuses = result.getStatuses();
        final Iterator<Status> it = statuses.iterator();
        assertThat(statuses)
                .hasSize(1);
        final Status firstStatus = it.next();
        assertThat(firstStatus)
                .isEqualTo(NOT_IN_STOCK);
    }


    @Test
    public void testBuyMixedBooks() throws Exception {
        final Book book = findBookFromStorageOrThrow(EXISTING_TITLE_1, EXISTING_AUTHOR_1, EXISTING_PRICE_1);
        book.addQuantity(- book.getQuantity());
        final Book book2 = findBookFromStorageOrThrow(EXISTING_TITLE_2, EXISTING_AUTHOR_2, EXISTING_PRICE_2);
        serverBasket.add(Arrays.asList(NEW_BOOK, book2, book));
        final PurchaseResult result = client.buyBasket();
        assertThat(result.getTotalPrice().doubleValue())
                .isEqualTo(book2.getPrice().doubleValue());
        final Statuses statuses = result.getStatuses();
        assertThat(statuses)
                .hasSize(3);
        assertThat(statuses)
                .containsOnly(DOES_NOT_EXIST, OK, NOT_IN_STOCK);
    }

    @Override
    public void report(String message) {
        System.err.println(message);
        ourServerErrorFlag.set(true);
    }

    @Override
    public void report(String message, Throwable throwable) {
        System.err.println(message);
        throwable.printStackTrace();
        ourServerErrorFlag.set(true);
    }

    private Book findBookFromStorageOrThrow(String title, String author, BigDecimal price) {
        Book result = null;
        for (Book book : serverBookList) {
            if (Book.isSame(book, title, author, price)) {
                result = book;
            }
        }
        if (result == null) {
            throw new IllegalStateException("Fail to find book by title \"" + title + "\", author \"" + author + "\" and price \"" + price + "\"");
        }
        return result;
    }

    private static void assertBooks(Books books) throws IOException {
        assertBooks(books, NO_EXTRA_ASSERTION);
    }

    private static void assertBooks(Books books, AssertionTask<Book> extraAssertionTask) {
        for (Book book : books) {
            assertBook(book);
            extraAssertionTask.execute(book);
        }
    }

    private static void assertBook(Book book) {
        assertThat(book.getTitle())
                .isNotEmpty();
        assertThat(book.getAuthor())
                .isNotEmpty();
        assertThat(book.getPrice())
                .isPositive();
        assertThat(book.getId())
                .isPositive();
        assertThat(book.getQuantity())
                .isGreaterThanOrEqualTo(0);
    }
}
