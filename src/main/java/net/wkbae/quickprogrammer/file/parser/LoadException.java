package net.wkbae.quickprogrammer.file.parser;

import java.io.IOException;

public class LoadException extends IOException {
	private static final long serialVersionUID = -741637607298297375L;
	
	public LoadException(String message) {
		super(message);
	}
	public LoadException(String message, Throwable cause) {
		super(message, cause);
	}
}