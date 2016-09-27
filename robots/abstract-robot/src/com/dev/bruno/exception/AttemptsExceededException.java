package com.dev.bruno.exception;

public class AttemptsExceededException extends Exception {

	private static final long serialVersionUID = 3614878323377780298L;

	public AttemptsExceededException() {
		super("Número de tentativas de acesso ao link excedida.");
	}
}
