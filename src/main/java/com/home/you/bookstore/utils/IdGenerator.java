package com.home.you.bookstore.utils;

import java.util.concurrent.atomic.AtomicInteger;

public class IdGenerator {
	private static final int INITIAL_VALUE = 1000;
	private static final IdGenerator INSTANCE = new IdGenerator();
	
	private final AtomicInteger counter = new AtomicInteger(INITIAL_VALUE);
	
	private IdGenerator() {}
			
	public static int generateId() {
		return INSTANCE.counter.getAndIncrement();
	}
}
