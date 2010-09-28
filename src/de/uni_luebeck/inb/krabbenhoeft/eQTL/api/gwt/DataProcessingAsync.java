package de.uni_luebeck.inb.krabbenhoeft.eQTL.api.gwt;

import com.google.gwt.user.client.rpc.AsyncCallback;

public interface DataProcessingAsync {
	void enumerateProcessors(Integer dataSetLayerKey, AsyncCallback<DataSetProcessorOverview[]> callback);

	void applyProcessor(Integer dataSetKey, Integer sourceDataSetLayerKey, String processorKey, String processorConfiguration, AsyncCallback<DataSetLayerOverview> callback);
}
