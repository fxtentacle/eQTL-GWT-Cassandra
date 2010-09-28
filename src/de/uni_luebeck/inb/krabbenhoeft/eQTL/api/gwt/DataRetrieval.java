package de.uni_luebeck.inb.krabbenhoeft.eQTL.api.gwt;

import java.util.Map;

import com.google.gwt.user.client.rpc.RemoteService;
import com.google.gwt.user.client.rpc.RemoteServiceRelativePath;

@RemoteServiceRelativePath("retrieval")
public interface DataRetrieval extends RemoteService {
	public Map<Integer, String> enumerateDataSets();

	DataSetOverview getOverview(Integer dataSetKey);

	DataSetLayerOverview getLayerAfterCalculationCompletes(Integer dataSetKey, Integer dataSetLayerKey) throws CalculationInProgressException;

	String[][] getLayerRows(Integer dataSetLayerKey, int offset, int numberOfItems);

	String[][] getTopRowsForRange(Integer dataSetLayerKey, String positionColumn, GenomeRange genomeRange);

	ExpressionQtlTrackEntry[] getTopEntriesForRange(Integer dataSetLayerKey, String positionColumn, GenomeRange genomeRange);

	ExpressionQtlTrackEntry2D[] getTopEntriesForArea(Integer dataSetLayerKey, String positionColumnX, GenomeRange genomeRangeX, String positionColumnY, GenomeRange genomeRangeY);
}
