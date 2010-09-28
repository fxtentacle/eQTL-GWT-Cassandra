package de.uni_luebeck.inb.krabbenhoeft.eQTL.api.gwt;

import com.google.gwt.user.client.rpc.AsyncCallback;

public interface ProcessWithRAsync {
	void callR(int dataSetLayerKey, String input, AsyncCallback<String> callback);
}
