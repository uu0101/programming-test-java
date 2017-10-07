package com.home.you.bookstore.utils;

import static com.home.you.bookstore.Constants.NEW_LINE;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;

public class ReaderUtils {
	
	private ReaderUtils() {}
	
	public static String readAll(InputStream stream) throws IOException {
		return readAll(new InputStreamReader(stream));
	}

	public static String readAll(Reader reader) throws IOException {
		final BufferedReader bufferedReader = new BufferedReader(reader);
		final StringBuilder builder = new StringBuilder();
		String line = bufferedReader.readLine();
		while (line != null) {
			builder.append(line);
			builder.append(NEW_LINE);
			line = bufferedReader.readLine();
		}
		return builder.toString();
	}
}
