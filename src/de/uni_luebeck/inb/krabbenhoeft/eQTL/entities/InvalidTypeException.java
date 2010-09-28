package de.uni_luebeck.inb.krabbenhoeft.eQTL.entities;

import de.uni_luebeck.inb.krabbenhoeft.eQTL.entities.ColumnForDataSetLayer.ColumType;

public class InvalidTypeException extends RuntimeException {
	private static final long serialVersionUID = 1L;

	public InvalidTypeException(String propertyName, ColumType columType) {
		super("Column " + propertyName + " is not of type " + columType.name());
	}
}
