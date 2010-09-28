package de.uni_luebeck.inb.krabbenhoeft.eQTL.entities;

public class MissingValueException extends RuntimeException {
	private static final long serialVersionUID = 1L;

	public MissingValueException(String property) {
		super("Missing value for column: " + property);
	}

}
