package com.home.you.bookstore;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import static com.home.you.bookstore.Constants.SEMICOLON;
import static com.home.you.bookstore.Constants.UTF8;
import static java.util.Collections.unmodifiableList;

public class IdParser {
	
	private IdParser() {}
	
    public static List<Integer> decode(InputStream stream) throws IOException {
        final List<Integer> books = new ArrayList<>();
        try {
            final BufferedReader input = new BufferedReader(new InputStreamReader(stream));
            String line = input.readLine();
            while (line != null) {
                parseLineAndPopulate(line, books);
                line = input.readLine();
            }
            input.close();
        } catch (IOException e) {
            throw new IllegalStateException("Unable to read data from stream.", e);
        }
        return unmodifiableList(books);
    }

    public static byte[] encode(List<Integer> result) throws IOException {
        StringBuilder messageBuilder = new StringBuilder();
        for (Integer integer : result) {
            messageBuilder.append(integer);
            messageBuilder.append(SEMICOLON);
        }
        final String message = messageBuilder.toString();
        final byte[] blob = message.getBytes(UTF8);
        return blob;
    }

    private static void parseLineAndPopulate(String line, List<Integer> list) {
        final String[] values = line.split(Constants.SEMICOLON);
        for (String value : values) {
            final int id = Integer.parseInt(value);
            list.add(id);
        }
    }
}
