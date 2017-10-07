package com.home.you.bookstore.rest.client;

import java.io.IOException;

@FunctionalInterface
public interface Task<T> {
	void execute(T arg) throws IOException;
}
