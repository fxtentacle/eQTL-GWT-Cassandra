package de.uni_luebeck.inb.krabbenhoeft.eQTL.api.gwt;

import java.util.Map;

import com.google.gwt.user.client.rpc.AsyncCallback;

public interface DataRetrievalAsync {
	void enumerateDataSets(AsyncCallback<Map<Integer, String>> callback);

	void getOverview(Integer dataSetKey, AsyncCallback<DataSetOverview> callback);

	void getLayerRows(Integer dataSetLayerKey, int offset, int numberOfItems, AsyncCallback<String[][]> callback);

	void getLayerAfterCalculationCompletes(Integer dataSetKey, Integer dataSetLayerKey, AsyncCallback<DataSetLayerOverview> callback);

	void getTopRowsForRange(Integer dataSetLayerKey, String positionColumn, GenomeRange genomeRange, AsyncCallback<String[][]> callback);

	void getTopEntriesForRange(Integer dataSetLayerKey, String positionColumn, GenomeRange genomeRange, AsyncCallback<ExpressionQtlTrackEntry[]> callback);

	void getTopEntriesForArea(Integer dataSetLayerKey, String positionColumnX, GenomeRange genomeRangeX, String positionColumnY, GenomeRange genomeRangeY,
			AsyncCallback<ExpressionQtlTrackEntry2D[]> callback);
}
