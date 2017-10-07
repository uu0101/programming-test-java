package com.home.you.bookstore.rest.client;

import java.io.IOException;

public class ClientException extends IOException {
	private static final long serialVersionUID = -6413648353761986138L;

	public ClientException(String message) {
		super(message);
	}
}
