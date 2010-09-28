package de.uni_luebeck.inb.krabbenhoeft.eQTL.api.rest;

import org.restlet.resource.Get;
import org.restlet.resource.Post;
import org.restlet.resource.Put;

public interface DataImporter {
	@Get
	public String[] getDataSetNames();

	@Post
	// create a new data set. returns the key
	public Integer createNewDataSet(CreateNewDataSetParameter parameterObject);

	@Put
	// insert entries into the data Set
	public void insertIntoDataSet(InsertIntoDataSetParameter parameterObject);
}
