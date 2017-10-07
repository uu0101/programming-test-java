package com.home.you.bookstore.rest.server;

import org.junit.Test;

import static com.home.you.bookstore.rest.server.BookStoreServerImpl.constructServletContext;
import static com.home.you.bookstore.rest.server.BookStoreServerImpl.extractParameter;
import static org.junit.Assert.assertEquals;

public class TestBookStoreServer {
	@Test
	public void testCreateServletContext() {
		final String application = "bookstore";
		final String context = "hello";
		final String result = constructServletContext(application, context);
		assertEquals("/bookstore/hello", result);
	}

	@Test
	public void testExtractParameter() {
		final String parameter = "search";
		final String value = "Cunning Bastard";
		final String quary = "search=\"Cunning Bastard\"";
		final String result = extractParameter(quary, parameter);
		assertEquals(result, value);
	}

	@Test
	public void testExtractFirstParameterWithTwoParams() {
		final String parameter = "search";
		final String value = "Cunning Bastard";
		final String quary = "search=\"Cunning Bastard\"&index=8";
		final String result = extractParameter(quary, parameter);
		assertEquals(result, value);
	}

	@Test
	public void testExtractSecondParameterWithTwoParams() {
		final String parameter = "index";
		final String value = "8";
		final String quary = "search=\"Cunning Bastard\"&index=8";
		final String result = extractParameter(quary, parameter);
		assertEquals(result, value);
	}
}
