package de.uni_luebeck.inb.krabbenhoeft.eQTL.api.rest;

import java.io.Serializable;

public class InsertIntoDataSetParameter implements Serializable {
	private static final long serialVersionUID = 1L;

	public Integer dataSetLayerKey;
	public DataSetLine[] lines;
	public Integer linesIdStart;
	public boolean lastUpload;

	public InsertIntoDataSetParameter(Integer dataSetLayerKey, DataSetLine[] lines, Integer linesIdStart, boolean lastUpload) {
		this.dataSetLayerKey = dataSetLayerKey;
		this.lines = lines;
		this.linesIdStart = linesIdStart;
		this.lastUpload = lastUpload;
	}
}