package de.uni_luebeck.inb.krabbenhoeft.eQTL.entities;

public interface HajoEntity {
	public static final int NUMBER_OF_PARALLEL_BLOCK_IDS = 500;

	// identification

	public byte[] getEntityKey();

	// read

	public String getName(String propertyName);

	public double getNumerical(String propertyName);

	public long getLocation(String propertyName);

	public Category getCategory(String propertyName);

	// write

	public void setName(String propertyName, String value);

	public void setNumerical(String propertyName, double value);

	public void setLocation(String propertyName, long value);

	public void setCategory(String propertyName, Category value);

	// for row data view

	public String getAsString(String name);

}
