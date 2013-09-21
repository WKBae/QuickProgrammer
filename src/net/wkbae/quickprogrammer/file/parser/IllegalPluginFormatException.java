package net.wkbae.quickprogrammer.file.parser;

import org.xml.sax.SAXException;

public class IllegalPluginFormatException extends SAXException {
	private static final long serialVersionUID = 7169589499033932712L;

	IllegalPluginFormatException(String message){
		super(message);
	}
	IllegalPluginFormatException(String message, Exception e) {
		super(message, e);
	}
}