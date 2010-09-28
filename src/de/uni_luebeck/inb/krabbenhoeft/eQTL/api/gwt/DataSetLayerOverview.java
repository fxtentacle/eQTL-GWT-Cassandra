package de.uni_luebeck.inb.krabbenhoeft.eQTL.api.gwt;

import java.util.Date;

import com.google.gwt.user.client.rpc.IsSerializable;

public class DataSetLayerOverview implements IsSerializable {
	public int dataSetKey;
	public int layerKey;
	
	public String operationFromLastLayer;
	public Date dateCreated;

	public String[] columns;
	public int numberOfDataRows;
}
