package de.uni_luebeck.inb.krabbenhoeft.eQTL.api.gwt;

import com.google.gwt.user.client.rpc.RemoteService;
import com.google.gwt.user.client.rpc.RemoteServiceRelativePath;

@RemoteServiceRelativePath("processWithR")
public interface ProcessWithR extends RemoteService {
	public String callR(int dataSetLayerKey, String input);
}
