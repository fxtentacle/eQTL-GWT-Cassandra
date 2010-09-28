package de.uni_luebeck.inb.krabbenhoeft.eQTL.api.gwt;

import java.util.Date;

import com.google.gwt.user.client.rpc.IsSerializable;

public class DataSetOverview implements IsSerializable {
	public int key;
	public String name;
	public Date dateCreated;
	public DataSetLayerOverview[] layers;
}
