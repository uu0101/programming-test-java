package com.home.you.bookstore.rest.client;

import com.home.you.bookstore.IdParser;
import com.home.you.bookstore.book.Book;
import com.home.you.bookstore.book.BookParser;
import com.home.you.bookstore.results.PurchaseResult;
import com.home.you.bookstore.results.PurchaseResultParser;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;

import static com.home.you.bookstore.BookStoreCli.ADMIN_SERVICE;
import static com.home.you.bookstore.Constants.WHITESPACE;
import static com.home.you.bookstore.rest.RestConstants.*;
import static com.home.you.bookstore.BookStoreCli.BASKET_SERVICE;
import static com.home.you.bookstore.BookStoreCli.BOOK_SERVICE;
import static com.home.you.bookstore.utils.ReaderUtils.readAll;

class BookStoreConnectionImpl implements BookStoreConnection {
    private static final String APPLICATION = "bookstore";
    private final String DEFAULT_USER_AGENT_VALUE = "Mozilla/5.0";

    private final String address;
    private final int port;

    BookStoreConnectionImpl(String address, int port) {
        this.address = address;
        this.port = port;
    }

    @Override
    public String command(String command) throws IOException {
        final String quary = "command=" + command;
        final URL url = createUrl(ADMIN_SERVICE, quary);
        final InputStream stream = doPost(url, out -> {});
        final String message = readAll(stream);
        return message;
    }

    @Override
    public List<Book> readBooks() throws IOException {
        final URL url = createUrl(BOOK_SERVICE);
        final InputStream stream = doGet(url);
        final List<Book> books = BookParser.decode(stream);
        return books;
    }

    @Override
    public List<Book> readBooks(String searchString) throws IOException {
        final String transformedSearchString = searchString.replaceAll(String.valueOf(WHITESPACE), URL_WHITESPACE);
        final String searchQuary = "search=" + transformedSearchString;
        final URL url = createUrl(BOOK_SERVICE, searchQuary);
        final InputStream stream = doGet(url);
        final List<Book> books = BookParser.decode(stream);
        return books;
    }

    @Override
    public void addBooksToStore(Iterable<Book> books) throws IOException {
        final byte[] blob = BookParser.encode(books);
        final URL url = createUrl(BOOK_SERVICE);
        doPost(url, out -> out.write(blob));
    }

    @Override
    public List<Book> readBasket() throws IOException {
        final URL url = createUrl(BASKET_SERVICE);
        final InputStream stream = doGet(url);
        final List<Book> books = BookParser.decode(stream);
        return books;
    }

    @Override
    public void addBooksToBasket(List<Integer> ids) throws IOException {
        final byte[] blob = IdParser.encode(ids);
        final URL url = createUrl(BASKET_SERVICE);
        doPut(url, out -> out.write(blob));
    }

    @Override
    public void removeBooksToBasket(List<Integer> ids) throws IOException {
        final byte[] blob = IdParser.encode(ids);
        final URL url = createUrl(BASKET_SERVICE);
        doDelete(url, out -> out.write(blob));
    }

    @Override
    public PurchaseResult buyBasket() throws IOException {
        final URL url = createUrl(BASKET_SERVICE);
        final InputStream stream = doPost(url, out -> {});
        final PurchaseResult result = PurchaseResultParser.decode(stream);
        return result;
    }

    private InputStream doGet(URL url) throws IOException {
        final HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod(GET);
        connection.setRequestProperty(HTTP_HEADER_USER_AGENT, DEFAULT_USER_AGENT_VALUE);
        final int responseCode = connection.getResponseCode();
        final InputStream body;
        if (responseCode == 200) {
            body = connection.getInputStream();
        } else {
            throw new ClientException("Failed to send request to url \"" + url + "\". Got HTTP responce code \"" + responseCode + "\"");
        }
        return body;
    }

    private InputStream doPost(URL url, Task<OutputStream> task) throws IOException {
        return doUpdate(POST, url, task);
    }

    private InputStream doPut(URL url, Task<OutputStream> task) throws IOException {
        return doUpdate(PUT, url, task);
    }

    private InputStream doDelete(URL url, Task<OutputStream> task) throws IOException {
        return doUpdate(DELETE, url, task);
    }

    private InputStream doUpdate(String methodType, URL url, Task<OutputStream> task) throws IOException {
        final HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod(methodType);
        connection.setRequestProperty(HTTP_HEADER_USER_AGENT, DEFAULT_USER_AGENT_VALUE);
        connection.setDoOutput(true);
        final OutputStream stream = connection.getOutputStream();
        task.execute(stream);
        final int responseCode = connection.getResponseCode();
        final InputStream body;
        if (responseCode == HTTP_OK) {
            body = connection.getInputStream();
        } else {
            throw new ClientException("Failed to send request to url \"" + url + "\". Got HTTP responce code \"" + responseCode + "\"");
        }
        return body;
    }

    private URL createUrl(String service, String... quaryArgs) {
        URL url;
        try {
            final StringBuilder builder = new StringBuilder("http://");
            builder.append(address);
            builder.append(":");
            builder.append(port);
            builder.append('/');
            builder.append(APPLICATION);
            builder.append('/');
            builder.append(service);
            if (quaryArgs != null && quaryArgs.length > 0) {
                builder.append('?').append(quaryArgs[0]);
                for (int i = 1; i < quaryArgs.length; i++) {
                    builder.append('&').append(quaryArgs[i]);
                }
            }
            url = new URL(builder.toString());
        } catch (MalformedURLException e) {
            throw new IllegalStateException(e);
        }
        return url;
    }
}
