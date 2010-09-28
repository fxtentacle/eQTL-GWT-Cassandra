package de.uni_luebeck.inb.krabbenhoeft.eQTL.server;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.hibernate.Query;
import org.hibernate.Session;

import de.uni_luebeck.inb.krabbenhoeft.eQTL.api.gwt.DataSetLayerOverview;
import de.uni_luebeck.inb.krabbenhoeft.eQTL.entities.ColumnForDataSetLayer;
import de.uni_luebeck.inb.krabbenhoeft.eQTL.entities.DataSet;
import de.uni_luebeck.inb.krabbenhoeft.eQTL.entities.DataSetLayer;

public class DataSetHelpers {

	@SuppressWarnings("unchecked")
	public static List<DataSet> listMyDataSets(final Session session, String ownerMailAddress) {
		final Query query = session.createQuery("from DataSet where ownerMailAddress = :a order by dateCreated ASC").setParameter("a", ownerMailAddress);
		final List<DataSet> dataSets = (List<DataSet>) query.list();
		return dataSets;
	}

	public static DataSet createDataSet(final Session session, String name, String ownerMailAddress) {
		DataSet dataSet = new DataSet();
		dataSet.setOwnerMailAddress(ownerMailAddress);
		dataSet.setName(name);
		dataSet.setDateCreated(new Date());
		dataSet.setDateAccessed(new Date());
		session.persist(dataSet);
		return dataSet;
	}

	public static DataSetLayer addLayerToDataSet(final Session session, DataSet dataSet, List<ColumnForDataSetLayer> columns, String operationFromLastLayer) {
		DataSetLayer dataSetLayer = new DataSetLayer();
		dataSetLayer.getColumns().addAll(columns);
		for (ColumnForDataSetLayer columnForDataSetLayer : dataSetLayer.getColumns()) {
			session.persist(columnForDataSetLayer);
		}
		dataSetLayer.setOperationFromLastLayer(operationFromLastLayer);
		session.persist(dataSetLayer);
		dataSet.getLayers().add(dataSetLayer);
		session.persist(dataSet);
		return dataSetLayer;
	}

	public static DataSetLayerOverview generateOverviewForDataSetLayer(Integer dataSetKey, DataSetLayer dataSetLayer) {
		ArrayList<String> columnNames = new ArrayList<String>();
		for (ColumnForDataSetLayer column : dataSetLayer.getColumns()) {
			columnNames.add(column.getName());
		}

		DataSetLayerOverview layerOverview = new DataSetLayerOverview();
		layerOverview.columns = columnNames.toArray(new String[0]);
		if (dataSetLayer.isCalculationComplete())
			layerOverview.numberOfDataRows = (int) dataSetLayer.getNumberOfItems();
		else
			layerOverview.numberOfDataRows = -1;
		layerOverview.dateCreated = dataSetLayer.getDateCreated();
		layerOverview.dataSetKey = dataSetKey;
		layerOverview.layerKey = dataSetLayer.getKey();
		layerOverview.operationFromLastLayer = dataSetLayer.getOperationFromLastLayer();
		return layerOverview;
	}
}
