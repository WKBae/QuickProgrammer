package net.wkbae.quickprogrammer.file.parser;

import org.xml.sax.Locator;
import org.xml.sax.SAXParseException;

public class IllegalPluginDescriptionException extends SAXParseException {
	private static final long serialVersionUID = 4058181816041043312L;

	IllegalPluginDescriptionException(String message, Locator locator) {
		super(message, locator);
	}
}