package de.uni_luebeck.inb.krabbenhoeft.eQTL.entities;

public class InvalidColumnException extends RuntimeException {
	private static final long serialVersionUID = 1L;

	public InvalidColumnException(String propertyName) {
		super("There is no column named: " + propertyName);
	}

}
