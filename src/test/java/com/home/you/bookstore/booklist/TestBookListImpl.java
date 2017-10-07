package com.home.you.bookstore.booklist;

import com.home.you.bookstore.book.Book;
import com.home.you.bookstore.book.BookParser;
import com.home.you.bookstore.Constants;
import com.home.you.bookstore.results.Status;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import static org.junit.Assert.*;

public class TestBookListImpl {
    private static final String EXIST_AUTHOR = "CunninG BastArd";
    private static final String NON_EXIST_AUTHOR = "You You";
    private static final String EXIST_TITLE = "RandoM Sales";
    private static final String NON_EXIST_TITLE = "Awesome";
    private static final BigDecimal CORRECT_PRICE = new BigDecimal("499.50");
    private static final int QUANTITY = 10;
    private static final Book NON_EXIST_BOOK = new Book(EXIST_TITLE, NON_EXIST_AUTHOR, CORRECT_PRICE, QUANTITY);
    private static final Book EXIST_BOOK = new Book(EXIST_TITLE, EXIST_AUTHOR, CORRECT_PRICE, QUANTITY);

    private BookList bookList;
    private List<Book> storeBookList;

    @Before
    public void makeBookStore() throws IOException {
        final InputStream stream = getClass().getResourceAsStream("/books.txt");
        storeBookList = new ArrayList<>(BookParser.decode(stream));
        bookList = new BookListImpl(storeBookList);
    }

    @Test
    public void testEmptySearchingList() {
        final List<Book> expectedBooks = storeBookList;
        final List<Book> actualBooks = Arrays.asList(bookList.list(Constants.EMPTY_STRING));
        assertFalse(actualBooks.isEmpty());
        assertEquals(expectedBooks, actualBooks);
    }

    @Test
    public void testExistAuthorSearchingList() {
        final List<Book> expectedBooks = storeBookList.stream()
                .filter(book -> EXIST_AUTHOR.equalsIgnoreCase(book.getAuthor())).collect(Collectors.toList());
        final List<Book> actualBooks = Arrays.asList(bookList.list(EXIST_AUTHOR));
        assertFalse(actualBooks.isEmpty());
        assertEquals(expectedBooks, actualBooks);
    }

    @Test
    public void testNonExistAuthorSearchingList() {
        final List<Book> expectedBooks = storeBookList.stream()
                .filter(book -> NON_EXIST_AUTHOR.equalsIgnoreCase(book.getAuthor())).collect(Collectors.toList());
        final List<Book> actualBooks = Arrays.asList(bookList.list(NON_EXIST_AUTHOR));
        assertTrue(actualBooks.isEmpty());
        assertEquals(expectedBooks, actualBooks);
    }

    @Test
    public void testExistTitleSearchingList() {
        final List<Book> expectedBooks = storeBookList.stream()
                .filter(book -> EXIST_TITLE.equalsIgnoreCase(book.getTitle())).collect(Collectors.toList());
        final List<Book> actualBooks = Arrays.asList(bookList.list(EXIST_TITLE));
        assertFalse(actualBooks.isEmpty());
        assertEquals(expectedBooks, actualBooks);
    }

    @Test
    public void testNonExistTitleSearchingList() {
        final List<Book> expectedBooks = storeBookList.stream()
                .filter(book -> NON_EXIST_TITLE.equalsIgnoreCase(book.getAuthor())).collect(Collectors.toList());
        final List<Book> actualBooks = Arrays.asList(bookList.list(NON_EXIST_TITLE));
        assertTrue(actualBooks.isEmpty());
        assertEquals(expectedBooks, actualBooks);
    }

    @Test
    public void testAddExistBook() {
        final boolean actual = bookList.add(EXIST_BOOK, QUANTITY);
        assertTrue(actual);
    }

    @Test
    public void testBuy() {
        final int[] expected = {Status.OK.value(), Status.DOES_NOT_EXIST.value(), Status.OK.value(),
                Status.OK.value(), Status.NOT_IN_STOCK.value()};
        final int[] actual = bookList.buy(EXIST_BOOK, NON_EXIST_BOOK, EXIST_BOOK, EXIST_BOOK, EXIST_BOOK);
        assertEquals(Arrays.toString(expected), Arrays.toString(actual));
    }

    public static BookList createBookList(List<Book> books) {
        return new BookListImpl(books);
    }
}
