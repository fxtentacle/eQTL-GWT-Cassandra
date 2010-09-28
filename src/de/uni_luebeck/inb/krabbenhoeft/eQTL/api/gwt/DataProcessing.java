package de.uni_luebeck.inb.krabbenhoeft.eQTL.api.gwt;

import com.google.gwt.user.client.rpc.RemoteService;
import com.google.gwt.user.client.rpc.RemoteServiceRelativePath;

@RemoteServiceRelativePath("processing")
public interface DataProcessing extends RemoteService {
	public DataSetProcessorOverview[] enumerateProcessors(Integer dataSetLayerKey);

	public DataSetLayerOverview applyProcessor(Integer dataSetKey, Integer sourceDataSetLayerKey, String processorKey, String processorConfiguration);
}
