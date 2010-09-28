package de.uni_luebeck.inb.krabbenhoeft.eQTL.api.rest;

import java.io.Serializable;

public class DataSetLine implements Serializable {
	private static final long serialVersionUID = 1L;

	public String locusId;
	public String traitId;
	public double lodScore;

	// format as key=value. covariate keys need to match covariateNames
	// specified when creating data set.
	public String[] covariates;
	
	public String chromosome;

	public double positionMin;
	public double positionPeak;
	public double positionMax;
	
	public String geneBankDnaId;
}