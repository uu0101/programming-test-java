package com.home.you.bookstore;

import com.home.you.bookstore.book.Book;
import com.home.you.bookstore.book.Books;
import com.home.you.bookstore.errors.BookStoreErrorReporter;
import com.home.you.bookstore.rest.client.BookStoreClient;
import com.home.you.bookstore.rest.client.BookStoreConnection;
import com.home.you.bookstore.rest.client.ClientException;
import com.home.you.bookstore.results.PurchaseResult;
import com.home.you.bookstore.results.Status;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.*;

import static com.home.you.bookstore.Constants.NEW_LINE;
import static com.home.you.bookstore.Constants.WHITESPACE;
import static com.home.you.bookstore.rest.RestConstants.*;
import static com.home.you.bookstore.utils.ArgumentUtils.*;
import static java.util.Collections.singletonList;

public class BookStoreCli implements BookStoreErrorReporter {
    public static final String BOOK_SERVICE = "book";
    public static final String BASKET_SERVICE = "basket";
    public static final String ADMIN_SERVICE = "admin";
    private static final String LOCAL_ADDRESS = "localhost";
    private static final int DEFAULT_PORT = 8080;
    private static final String SEARCH_FOR_ALL = null;
    private static final String START_OF_LINE_MARGIN = "--  ";
    private static final String NO_DATA = "--";
    private static final String END_OF_LINE_MARGIN = "--";
    private static final String FILLED_LINE = "--------------------------------------------------------------------------------------------------------------";
    private static final int ID_SPACE_SIZE = 8;
    private static final int TITLE_SPACE_SIZE = 32;
    private static final int AUTHOR_SPACE_SIZE = 32;
    private static final int PRICE_SPACE_SIZE = 16;
    private static final int QUANTITY_SPACE_SIZE = 16;
    private static final String UNKNOWN_SERVICE = "Unknown service \"";

    public static void main(String[] args) throws IOException, InterruptedException {
        BookStoreCli cli = new BookStoreCli();
        cli.start(args);
    }

    private void start(String[] args) throws IOException {
        final BookStoreConnection connection = BookStoreConnection.create(LOCAL_ADDRESS, DEFAULT_PORT);
        final BookStoreClient client = BookStoreClient.create(this, connection);
        final String method = args[0];
        final String service = args[1];

        final String message;
        try {
            switch (method) {
                case GET:
                    message = handleGet(client, service, args);
                    break;
                case POST:
                    message = handlePost(client, service, args);
                    break;
                case PUT:
                    message = handlePut(client, service, args);
                    break;
                case DELETE:
                    message = handleDelete(client, service, args);
                    break;
                default:
                    message = "";
                    break;
            }
            System.out.println(message);
        } catch (ClientException e) {
            System.err.println(e.getMessage());
        }
    }

    private static String handleGet(BookStoreClient client, String service, String[] args) throws IOException {
        final String message;
        switch (service) {
            case BOOK_SERVICE:
                message = handleGetForBook(client, args);
                break;
            case BASKET_SERVICE:
                message = handleGetForBasket(client);
                break;
            default:
                throw new ClientException(UNKNOWN_SERVICE + service + "\"");
        }
        return message;
    }

    private static String handleGetForBook(BookStoreClient client, String[] args) throws ClientException {
        final String message;
        try {
            final String searchString = extractStringOrDefault(args, 2, SEARCH_FOR_ALL);
            final Books books;
            if (!Objects.equals(searchString, SEARCH_FOR_ALL)) {
                books = client.findBooks(searchString);
            } else {
                books = client.getBooks();
            }
            message = printBooks(books).toString();
        } catch (Throwable t) {
            throw new ClientException("Failed to get books.\n" + t.getMessage() + ".\n" + "browse [empty] - Display all\n browse [string] - Display filtered books by author or title");
        }
        return message;
    }

    private static String handleGetForBasket(BookStoreClient client) throws ClientException {
        final String message;
        try {
            final Books books = client.getAllBooksFromBasket();
            message = printBookAndSummary(books)
                    .toString();
        } catch (Throwable t) {
            throw new ClientException("Failed to get basket.\n" + t.getMessage() + ".\n" + "display-basket - Display basket");
        }
        return message;
    }

    private static String handlePost(BookStoreClient client, String service, String[] args) throws IOException {
        final String message;
        switch (service) {
            case BOOK_SERVICE:
                message = handlePostForBook(client, args);
                break;
            case BASKET_SERVICE:
                message = handlePostForBasket(client);
                break;
            case ADMIN_SERVICE:
                message = handlePostForAdmin(client, args);
                break;
            default:
                throw new ClientException(UNKNOWN_SERVICE + service + "\"");
        }
        return message;
    }

    private static String handlePostForBook(BookStoreClient client, String[] args) throws ClientException {
        final String message;
        try {
            final String[] cmd = createCommand(args);
            final String title = extractStringOrThrow(cmd, 0, "Title need to be given");
            final String author = extractStringOrThrow(cmd, 1, "Author need to be given");
            final BigDecimal price = extractBigDecimalOrThrow(cmd, 2, "Price need to be given");
            final int quantity = extractIntegerOrThrow(cmd, 3, "Quantity need to be given");
            final Book book = new Book(title, author, price, quantity);
            final Books books = Books.of(singletonList(book));
            client.addBooksToStore(books);
            final Books b = client.getBooks();
            message = printBooks(b).toString();
        } catch (Throwable t) {
            throw new ClientException("Failed to add book to store.\n" + t.getMessage() + ".\n" + "add-book [title] [author] [price] [quantity]");
        }
        return message;
    }

    private static String handlePostForBasket(BookStoreClient client) throws ClientException {
        final String message;
        try {
            final Books basketBooks = client.getAllBooksFromBasket();
            final PurchaseResult result = client.buyBasket();
            message = printBookAndResult(basketBooks, result)
                    .toString();
        } catch (Throwable t) {
            throw new ClientException("Failed to buy basket.\n" + t.getMessage() + ".\n" + "buy - buy everything in basket and display the result");
        }
        return message;
    }

    private static String handlePostForAdmin(BookStoreClient client, String[] args) throws ClientException {
        final String message;
        try {
            final String command = extractStringOrThrow(args, 2, "Missing command");
            message = client.command(command);
        } catch (Throwable t) {
            throw new ClientException("Failed to send command to server.\n" + t.getMessage() + ".\n");
        }
        return message;
    }

    private static String handlePut(BookStoreClient client, String service, String[] args) throws ClientException {
        final String message;
        switch (service) {
            case BASKET_SERVICE:
                message = handlePutForBasket(client, args);
                break;
            default:
                throw new ClientException(UNKNOWN_SERVICE + service + "\"");
        }
        return message;
    }

    private static String handlePutForBasket(BookStoreClient client, String[] args) throws ClientException {
        final String message;
        try {
            final String[] cmd = createCommand(args);
            final List<Integer> ids = convertToInteger(cmd);
            client.addBooksToBasket(ids);
            final Books books = client.getAllBooksFromBasket();
            message = printBookAndSummary(books)
                    .toString();
        } catch (Throwable t) {
            throw new ClientException("Failed to put books into basket.\n" + t.getMessage() + ".\n" + "add-to-basket [id, id, ...]");
        }
        return message;
    }

    private static String handleDelete(BookStoreClient client, String service, String[] args) throws ClientException {
        final String message;
        switch (service) {
            case BASKET_SERVICE:
                message = handleDeleteForBasket(client, args);
                break;
            default:
                throw new ClientException(UNKNOWN_SERVICE + service + "\"");
        }
        return message;
    }

    private static String handleDeleteForBasket(BookStoreClient client, String[] args) throws ClientException {
        final String message;
        try {
            final String[] cmd = createCommand(args);
            final List<Integer> ids = convertToInteger(cmd);
            client.removeBooksToBasket(ids);
            final Books books = client.getAllBooksFromBasket();
            message = printBookAndSummary(books)
                    .toString();
        } catch (Throwable t) {
            throw new ClientException("Failed to execute remove from basket.\n" + t.getMessage() + ".\n" + "remove-from-basket [id, id, ...]");
        }
        return message;
    }

    private static String[] createCommand(String[] args) {
        final StringBuilder builder = new StringBuilder();
        for (int i = 2; args != null && i < args.length; i++) {
            builder.append(args[i]);
            builder.append(WHITESPACE);
        }
        final String raw = builder.toString();
        final String[] cmd = raw.split(",");
        for (int i = 0; i < cmd.length; i++) {
            cmd[i] = cmd[i].trim();
        }
        return cmd;
    }

    private static List<Integer> convertToInteger(String[] values) {
        final List<Integer> integers = new ArrayList<>();
        for (String value : values) {
            integers.add(Integer.parseInt(value));
        }
        return Collections.unmodifiableList(integers);
    }

    private static StringBuilder printBooks(Books books) {
        final StringBuilder builder = new StringBuilder();
        builder.append(FILLED_LINE);
        builder.append(NEW_LINE);
        builder.append(START_OF_LINE_MARGIN);
        builder.append(fill("Id", ID_SPACE_SIZE));
        builder.append(fill("Title", TITLE_SPACE_SIZE));
        builder.append(fill("Author", AUTHOR_SPACE_SIZE));
        builder.append(fill("Price", PRICE_SPACE_SIZE));
        builder.append(fill("Quantity", QUANTITY_SPACE_SIZE));
        builder.append(END_OF_LINE_MARGIN);
        builder.append(NEW_LINE);
        builder.append(FILLED_LINE);
        builder.append(NEW_LINE);
        for (Book book : books) {
            builder.append(START_OF_LINE_MARGIN);
            builder.append(fill(book.getId(), ID_SPACE_SIZE));
            builder.append(fill(book.getTitle(), TITLE_SPACE_SIZE));
            builder.append(fill(book.getAuthor(), AUTHOR_SPACE_SIZE));
            builder.append(fill(book.getPrice().toString(), PRICE_SPACE_SIZE));
            builder.append(fill(book.getQuantity(), QUANTITY_SPACE_SIZE));
            builder.append(END_OF_LINE_MARGIN);
            builder.append(NEW_LINE);
        }
        builder.append(FILLED_LINE);
        builder.append(NEW_LINE);
        return builder;
    }

    private static StringBuilder printBookAndSummary(Books books) {
        final StringBuilder builder = printBooks(books);
        builder.append(START_OF_LINE_MARGIN);
        builder.append(fill(NO_DATA, ID_SPACE_SIZE));
        builder.append(fill(NO_DATA, TITLE_SPACE_SIZE));
        builder.append(fill(NO_DATA, AUTHOR_SPACE_SIZE));
        BigDecimal totalPrice = BigDecimal.valueOf(0);
        for (Book book : books) {
            totalPrice = totalPrice.add(book.getPrice());
        }
        builder.append(fill(totalPrice.toString(), PRICE_SPACE_SIZE));
        builder.append(fill(books.size(), QUANTITY_SPACE_SIZE));
        builder.append(END_OF_LINE_MARGIN);
        builder.append(NEW_LINE);
        builder.append(FILLED_LINE);
        return builder;
    }

    private static StringBuilder printBookAndResult(Books books, PurchaseResult result) {
        final StringBuilder builder = new StringBuilder();
        builder.append(FILLED_LINE);
        builder.append(NEW_LINE);
        builder.append(START_OF_LINE_MARGIN);
        builder.append(fill("Id", ID_SPACE_SIZE));
        builder.append(fill("Title", TITLE_SPACE_SIZE));
        builder.append(fill("Author", AUTHOR_SPACE_SIZE));
        builder.append(fill("Price", PRICE_SPACE_SIZE));
        builder.append(fill("Status", QUANTITY_SPACE_SIZE));
        builder.append(END_OF_LINE_MARGIN);
        builder.append(NEW_LINE);
        builder.append(FILLED_LINE);
        builder.append(NEW_LINE);
        final Iterator<Status> statusIt = result.getStatuses().iterator();
        for (Book book : books) {
            builder.append(START_OF_LINE_MARGIN);
            builder.append(fill(book.getId(), ID_SPACE_SIZE));
            builder.append(fill(book.getTitle(), TITLE_SPACE_SIZE));
            builder.append(fill(book.getAuthor(), AUTHOR_SPACE_SIZE));
            builder.append(fill(book.getPrice().toString(), PRICE_SPACE_SIZE));
            builder.append(fill(statusIt.next().name(), QUANTITY_SPACE_SIZE));
            builder.append(END_OF_LINE_MARGIN);
            builder.append(NEW_LINE);
        }
        builder.append(FILLED_LINE);
        builder.append(NEW_LINE);
        builder.append(START_OF_LINE_MARGIN);
        builder.append(fill(NO_DATA, ID_SPACE_SIZE));
        builder.append(fill(NO_DATA, TITLE_SPACE_SIZE));
        builder.append(fill(NO_DATA, AUTHOR_SPACE_SIZE));
        builder.append(fill(result.getTotalPrice().toString(), PRICE_SPACE_SIZE));
        builder.append(fill(books.size(), QUANTITY_SPACE_SIZE));
        builder.append(END_OF_LINE_MARGIN);
        builder.append(NEW_LINE);
        builder.append(FILLED_LINE);
        return builder;
    }

    private static char[] fill(int value, int length) {
        return fill(String.valueOf(value), length);
    }

    private static char[] fill(String value, int length) {
        final char[] buffer = new char[length];
        Arrays.fill(buffer, WHITESPACE);
        for (int i = 0; i < length && i < value.length(); i++) {
            buffer[i] = value.charAt(i);
        }
        return buffer;
    }

    @Override
    public void report(String message) {
        System.err.println(message);
    }

    @Override
    public void report(String message, Throwable throwable) {
        if (throwable instanceof ClientException) {
            System.err.println(throwable.getMessage());
        } else {
            System.err.println(message);
            throwable.printStackTrace();
        }
    }
}
