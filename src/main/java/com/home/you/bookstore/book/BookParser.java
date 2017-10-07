package com.home.you.bookstore.book;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import static com.home.you.bookstore.Constants.*;
import static com.home.you.bookstore.utils.IdGenerator.generateId;
import static com.home.you.bookstore.utils.ParseUtils.parseBigDecimal;
import static java.util.Collections.unmodifiableList;

public class BookParser {
    private static final int TITLE_INDEX = 0;
    private static final int AUTHOR_INDEX = 1;
    private static final int PRICE_INDEX = 2;
    private static final int QUANTITY_INDEX = 3;
    private static final int ID_INDEX = 4;
    private static final int NO_ID = -1;
    
    private BookParser(){}

    public static List<Book> decode(InputStream stream) throws IOException {
        final List<Book> books = new ArrayList<>();
        try (final BufferedReader input = new BufferedReader(new InputStreamReader(stream));) {
            String line = input.readLine();
            while (line != null) {
                parseLineAndPopulate(line, books);
                line = input.readLine();
            }
        } catch (IOException e) {
            throw new IllegalStateException("Unable to read data from stream.", e);
        }
        return unmodifiableList(books);
    }

    public static byte[] encode(Iterable<Book> result) throws IOException {
        final StringBuilder messageBuilder = new StringBuilder();
        for (Book book : result) {
            messageBuilder.append(book.getTitle());
            messageBuilder.append(SEMICOLON);
            messageBuilder.append(book.getAuthor());
            messageBuilder.append(SEMICOLON);
            messageBuilder.append(book.getPrice());
            messageBuilder.append(SEMICOLON);
            messageBuilder.append(book.getQuantity());
            if (book.hasId()) {
                messageBuilder.append(SEMICOLON);
                messageBuilder.append(book.getId());
            }
            messageBuilder.append(NEW_LINE);
        }
        final String message = messageBuilder.toString();
        final byte[] blob = message.getBytes(UTF8);
        return blob;
    }

    private static void parseLineAndPopulate(String line, List<Book> books) {
        final String[] fields = line.split(SEMICOLON);
        final String title = fields[TITLE_INDEX];
        final String author = fields[AUTHOR_INDEX];
        final BigDecimal price = parseBigDecimal(fields[PRICE_INDEX]);
        final int quantity = Integer.parseInt(fields[QUANTITY_INDEX]);
        final int id = fields.length > ID_INDEX ? Integer.parseInt(fields[ID_INDEX]) : NO_ID;
        final Book book = id != NO_ID ?
                new Book(id, title, author, price, quantity) :
                new Book(generateId(), title, author, price, quantity);
        books.add(book);
    }
}
