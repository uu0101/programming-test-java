package com.home.you.bookstore.rest.server;

import com.home.you.bookstore.book.Book;
import com.home.you.bookstore.basket.Basket;
import com.home.you.bookstore.booklist.BookList;
import com.home.you.bookstore.errors.BookStoreErrorReporter;
import com.home.you.bookstore.book.BookParser;
import com.home.you.bookstore.IdParser;
import com.home.you.bookstore.results.PurchaseResult;
import com.home.you.bookstore.results.PurchaseResultParser;
import com.home.you.bookstore.results.Status;
import com.home.you.bookstore.results.Statuses;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;

import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.net.InetSocketAddress;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.logging.Logger;

import static com.home.you.bookstore.Constants.*;
import static com.home.you.bookstore.rest.RestConstants.*;
import static com.home.you.bookstore.utils.ArgumentUtils.extractIntegerOrDefault;
import static java.util.concurrent.TimeUnit.MILLISECONDS;
import static java.util.logging.Level.SEVERE;

class BookStoreServerImpl implements BookStoreServer {
	private static final Logger LOG = Logger.getLogger(BookStoreServerImpl.class.getName());
	
    private static final String ADDRESS = "https://raw.githubusercontent.com/contribe/contribe/dev/bookstoredata/bookstoredata.txt";
    private static final int DEFAULT_BACKLOG = -1;
    private static final String DEFAULT_APPLICATION_CONTEXT = "bookstore";
    private static final int SIZE_OF_EQUAL_SIGN = 1;
    private static final String PARAMETER_DELIMIT = "&";
    private static final int NO_DELAY = 0;
    private static final int SHORT_DELAY = 1000;
    private static final long NO_DATA = 0;
    private static final String DO_NOT_SUPPORT = "Do not support \"";
    private static final String ERROR_ON_SERVER = "Error occur on server side";

    static final int DEFAULT_PORT = 8080;
    

    private final BookList bookList;
    private final Basket basket;
    private final int port;
    private final HttpServer httpServer;
    private final BookStoreErrorReporter reporter;
    private final ScheduledExecutorService executor = Executors.newScheduledThreadPool(1);

    private BookStoreServerImpl(BuilderImpl builder) {
        this.port = builder.port;
        this.bookList = builder.bookList;
        this.basket = builder.basket;
        this.reporter = builder.reporter;
        this.httpServer = createHttpServerQuietly();
    }

    @Override
    public void start() throws IOException {
        startHttpServer();
        loadBookDataFromAddress();
    }

    @Override
    public void stop() throws IOException {
        httpServer.stop(NO_DELAY);
        executor.shutdownNow();
    }

    static Builder newBuilder() {
        return new BuilderImpl();
    }

    private static class BuilderImpl implements BookStoreServer.Builder {
        private BookList bookList = BookList.create();
        private Basket basket = Basket.create();
        private int port = DEFAULT_PORT;
        private BookStoreErrorReporter reporter = BookStoreErrorReporter.none();

        private BuilderImpl() {
        }

        @Override
        public Builder withPort(int port) {
            this.port = port;
            return this;
        }

        @Override
        public Builder withBookList(BookList bookList) {
            this.bookList = bookList;
            return this;
        }

        @Override
        public Builder withBasket(Basket basket) {
            this.basket = basket;
            return this;
        }

        @Override
        public Builder withBookStoreErrorReporter(BookStoreErrorReporter reporter) {
            this.reporter = reporter;
            return this;
        }

        @Override
        public BookStoreServer build() {
            return new BookStoreServerImpl(this);
        }
    }

    private void startHttpServer() throws IOException {
        httpServer.bind(new InetSocketAddress(port), DEFAULT_BACKLOG);
        final String basketServletContextUrl = constructServletContext(DEFAULT_APPLICATION_CONTEXT,
                BasketServlet.CONTEXT);
        httpServer.createContext(basketServletContextUrl, new BasketServlet());
        final String bookServletContextUrl = constructServletContext(DEFAULT_APPLICATION_CONTEXT, BookServlet.CONTEXT);
        httpServer.createContext(bookServletContextUrl, new BookServlet());
        final String adminServletContextUrl = constructServletContext(DEFAULT_APPLICATION_CONTEXT, AdminServlet.CONTEXT);
        httpServer.createContext(adminServletContextUrl, new AdminServlet());
        httpServer.start();
    }

    private void loadBookDataFromAddress() {
    	try {
            final URL dataUrl = new URL(ADDRESS);
            final InputStream stream = dataUrl.openStream();
            final List<Book> books = BookParser.decode(stream);
            addBooksToList(books);
        } catch (IOException e) {
            throw new IllegalStateException("Unable to read data from address \"" + ADDRESS + "\"", e);
        }
    }

    private class AdminServlet implements HttpHandler {
        static final String CONTEXT = "admin";
        private static final String COMMAND_PARAMETER = "command";

        @Override
		public void handle(HttpExchange exchange) throws IOException {
			try {
				if (POST.equals(exchange.getRequestMethod())) {
					final String query = exchange.getRequestURI().getQuery();
					final String command = query != null ? extractParameter(query, COMMAND_PARAMETER) : null;
					if ("quit".equalsIgnoreCase(command)) {
						executor.schedule(() -> {stopServer();}, SHORT_DELAY, MILLISECONDS);
					}
					sendResponce(exchange);
				} else {
					throw new IllegalStateException(DO_NOT_SUPPORT + exchange.getRequestMethod() + "\"");
				}
			} catch (Throwable t) {
				reporter.report(ERROR_ON_SERVER, t);
				throw t;
			}
		}

		private void stopServer() {
			try {
				stop();
			} catch (IOException e) {
				reporter.report("Failed to stop server", e);
			}
		}
    }

    private class BookServlet implements HttpHandler {
        static final String CONTEXT = "book";
        private static final String SEARCH_PARAMETER = "search";

        @Override
        public void handle(HttpExchange exchange) throws IOException {
            try {
                switch (exchange.getRequestMethod()) {
                    case POST:
                        doPost(exchange);
                        break;
                    case GET:
                        doGet(exchange);
                        break;
                    default:
                        throw new IllegalStateException(DO_NOT_SUPPORT + exchange.getRequestMethod() + "\"");
                }
            } catch (Throwable t) {
                reporter.report(ERROR_ON_SERVER, t);
                throw t;
            }
        }

        private void doPost(HttpExchange exchange) throws IOException {
            final InputStream stream = exchange.getRequestBody();
            final List<Book> result = BookParser.decode(stream);
            addBooksToList(result);
            final byte[] data = BookParser.encode(result);
            sendResponce(exchange, data);
        }

        private void doGet(HttpExchange exchange) throws IOException {
            final String query = exchange.getRequestURI().getQuery();
            final String searchingString = query != null ? extractParameter(query, SEARCH_PARAMETER) : null;
            final List<Book> result = Arrays.asList(bookList.list(searchingString));
            final byte[] data = BookParser.encode(result);
            sendResponce(exchange, data);
        }
    }

    private class BasketServlet implements HttpHandler {
        static final String CONTEXT = "basket";

        @Override
        public void handle(HttpExchange exchange) throws IOException {
            try {
                switch (exchange.getRequestMethod()) {
                    case POST:
                        doPost(exchange);
                        break;
                    case GET:
                        doGet(exchange);
                        break;
                    case DELETE:
                        doDelete(exchange);
                        break;
                    case PUT:
                        doPut(exchange);
                        break;
                    default:
                        throw new IllegalStateException(DO_NOT_SUPPORT + exchange.getRequestMethod() + "\"");
                }
            } catch (Throwable t) {
                reporter.report(ERROR_ON_SERVER, t);
                throw t;
            }
        }

        private void doPost(HttpExchange exchange) throws IOException {
            final List<Book> books = basket.getBooks();
            final int[] statusCodes = bookList.buy(books.toArray(new Book[0]));
            final List<Status> statusList = Status.asList(statusCodes);
            final BigDecimal totalPrice = calculateTotalPrice(books, statusCodes);
            final Statuses statuses = Statuses.of(statusList);
            final PurchaseResult result = PurchaseResult.builder()
                    .withTotalPrice(totalPrice)
                    .withStatuses(statuses)
                    .build();
            final byte[] data = PurchaseResultParser.encode(result);
            sendResponce(exchange, data);
            basket.clear();
        }

        private void doGet(HttpExchange exchange) throws IOException {
            final List<Book> books = basket.getBooks();
            final byte[] data = BookParser.encode(books);
            sendResponce(exchange, data);
        }

        private void doDelete(HttpExchange exchange) throws IOException {
            final List<Book> books = findBooksFromBookList(exchange);
            basket.remove(books);
            sendResponce(exchange);
        }

        private void doPut(HttpExchange exchange) throws IOException {
            final List<Book> books = findBooksFromBookList(exchange);
            basket.add(books);
            sendResponce(exchange);
        }
        
        private BigDecimal calculateTotalPrice(List<Book> books, int[] statusCodes) {
        	double price = 0.0;
        	for (int i = 0; i < books.size(); i++) {
        		final Book book = books.get(i);
        		if(statusCodes[i] == 0) {
        			price += book.getPrice().doubleValue();
        		}
        	}
        	return BigDecimal.valueOf(price);
        }
        
        private List<Book> findBooksFromBookList(HttpExchange exchange) throws IOException {
        	final InputStream stream = exchange.getRequestBody();
        	final List<Integer> ids = IdParser.decode(stream);
        	final List<Book> books = findAllBooksByIds(ids);
        	return books;
        }

        private List<Book> findAllBooksByIds(List<Integer> ids) {
            final List<Book> books = new ArrayList<>();
            ids.forEach(id -> {
                final Optional<Book> bookOpt = bookList.find(b -> id.equals(b.getId()));
                bookOpt.ifPresent(books::add);
            });
            return books;
        }
    }

    private void addBooksToList(List<Book> books) {
        for (Book book : books) {
            if (!bookList.add(book, book.getQuantity())) {
            	LOG.log(SEVERE, "Failed to add " + book + " to list");
            }
        }
    }

    private static void sendResponce(HttpExchange exchange) throws IOException {
        exchange.sendResponseHeaders(HTTP_OK, NO_DATA);
        exchange.close();
    }

    private static void sendResponce(HttpExchange exchange, final byte[] data) throws IOException {
        exchange.sendResponseHeaders(HTTP_OK, data.length);
        exchange.getResponseBody().write(data);
        exchange.close();
    }

    public static void main(String[] args) throws IOException, InterruptedException {
        final int port = extractIntegerOrDefault(args, FIRST, DEFAULT_PORT);
        final BookStoreServer server = BookStoreServer.builder()
                .withPort(port)
                .build();
        server.start();
    }

    private static HttpServer createHttpServerQuietly() {
        final HttpServer server;
        try {
            server = HttpServer.create();
        } catch (IOException e) {
            throw new IllegalStateException("Failed to create http server", e);
        }
        return server;
    }

    static String extractParameter(String query, String parameter) {
        final int parameterStartIndex = query.indexOf(parameter);
        String parameterValue = null;
        if (parameterStartIndex >= 0) {
            final int parameterEndIndex = parameterStartIndex + parameter.length();
            final int parameterValueStartIndex = parameterEndIndex + SIZE_OF_EQUAL_SIGN;
            final int parameterValueEndIndex = indexOfOrDefault(query, PARAMETER_DELIMIT, parameterValueStartIndex,
                    query.length());
            parameterValue = query.substring(parameterValueStartIndex, parameterValueEndIndex);
            parameterValue = parameterValue.replaceAll(DOUBLE_QUOTE, EMPTY_STRING);
        }
        return parameterValue;
    }

    private static int indexOfOrDefault(String src, String arg, int fromIndex, int defaultIndex) {
        final int index = src.indexOf(arg, fromIndex);
        return index > 0 ? index : defaultIndex;
    }

    static String constructServletContext(String applicationContext, String servletContext) {
        return '/' + applicationContext + '/' + servletContext;
    }
}
