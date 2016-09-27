package com.dev.bruno.exception;

public class NoProxyException extends Exception {

	/**
	 * 
	 */
	private static final long serialVersionUID = 4899077642025252139L;

	public NoProxyException() {
		super("Lista de proxy vazia.");
	}
}
