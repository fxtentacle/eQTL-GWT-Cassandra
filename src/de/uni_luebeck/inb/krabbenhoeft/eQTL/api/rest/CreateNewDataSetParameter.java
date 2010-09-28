package de.uni_luebeck.inb.krabbenhoeft.eQTL.api.rest;

import java.io.Serializable;

public class CreateNewDataSetParameter implements Serializable{
	private static final long serialVersionUID = 1L;
	
	public String name;
	public String[] covariateNames;

	public CreateNewDataSetParameter(String name, String[] covariateNames) {
		this.name = name;
		this.covariateNames = covariateNames;
	}
}