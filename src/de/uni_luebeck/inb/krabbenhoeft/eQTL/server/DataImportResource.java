package de.uni_luebeck.inb.krabbenhoeft.eQTL.server;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.hibernate.Session;
import org.hibernate.Transaction;
import org.restlet.resource.ServerResource;

import de.uni_luebeck.inb.krabbenhoeft.eQTL.api.rest.CreateNewDataSetParameter;
import de.uni_luebeck.inb.krabbenhoeft.eQTL.api.rest.DataImporter;
import de.uni_luebeck.inb.krabbenhoeft.eQTL.api.rest.DataSetLine;
import de.uni_luebeck.inb.krabbenhoeft.eQTL.api.rest.InsertIntoDataSetParameter;
import de.uni_luebeck.inb.krabbenhoeft.eQTL.entities.Category;
import de.uni_luebeck.inb.krabbenhoeft.eQTL.entities.ColumnForDataSetLayer;
import de.uni_luebeck.inb.krabbenhoeft.eQTL.entities.DataSet;
import de.uni_luebeck.inb.krabbenhoeft.eQTL.entities.DataSetLayer;
import de.uni_luebeck.inb.krabbenhoeft.eQTL.entities.HajoEntity;
import de.uni_luebeck.inb.krabbenhoeft.eQTL.entities.ColumnForDataSetLayer.ColumType;
import de.uni_luebeck.inb.krabbenhoeft.eQTL.server.helpers.persistence.CassandraSession;
import de.uni_luebeck.inb.krabbenhoeft.eQTL.server.helpers.persistence.CreateAndModifyEntities;
import de.uni_luebeck.inb.krabbenhoeft.eQTL.server.helpers.persistence.RunWithHibernate;

public class DataImportResource extends ServerResource implements DataImporter {
	public String[] getDataSetNames() {
		return new RunWithHibernate<String[]>() {
			public String[] work(Transaction transaction, Session session) throws Exception {

				final List<DataSet> dataSets = DataSetHelpers.listMyDataSets(session, getCurrentUser());
				final ArrayList<String> ret = new ArrayList<String>();
				for (DataSet dataSet : dataSets) {
					ret.add(dataSet.getName());
				}
				return ret.toArray(new String[0]);
			}
		}.run();
	}

	public Integer createNewDataSet(final CreateNewDataSetParameter parameterObject) {
		return new RunWithHibernate<Integer>() {
			public Integer work(Transaction transaction, Session session) throws Exception {
				DataSet dataSet = DataSetHelpers.createDataSet(session, parameterObject.name, getCurrentUser());

				List<ColumnForDataSetLayer> columns = new ArrayList<ColumnForDataSetLayer>();
				columns.add(new ColumnForDataSetLayer("locusId", ColumType.Name));
				columns.add(new ColumnForDataSetLayer("traitId", ColumType.Name));

				final ColumnForDataSetLayer col = new ColumnForDataSetLayer("lodScore", ColumType.Numerical);
				col.setIndexme(true);
				columns.add(col);

				for (String covariate : parameterObject.covariateNames) {
					columns.add(new ColumnForDataSetLayer(covariate, ColumType.Category));
				}

				columns.add(new ColumnForDataSetLayer("chromosome", ColumType.Category));
				columns.add(new ColumnForDataSetLayer("positionMin", ColumType.Numerical));
				columns.add(new ColumnForDataSetLayer("positionPeak", ColumType.Numerical));
				columns.add(new ColumnForDataSetLayer("positionMax", ColumType.Numerical));

				columns.add(new ColumnForDataSetLayer("geneBankDnaId", ColumType.Name));

				final DataSetLayer layer = DataSetHelpers.addLayerToDataSet(session, dataSet, columns, "Initial import");

				return layer.getKey();
			}
		}.run();
	}

	public void insertIntoDataSet(final InsertIntoDataSetParameter parameterObject) {
		new RunWithHibernate<Void>() {
			public Void work(Transaction transaction, Session session) throws Exception {
				final DataSetLayer dataSetLayer = (DataSetLayer) session.load(DataSetLayer.class, parameterObject.dataSetLayerKey);

				CassandraSession cassandra = new CassandraSession();
				CreateAndModifyEntities creator = new CreateAndModifyEntities(cassandra, null, dataSetLayer, dataSetLayer.getColumns());
				final Iterator<HajoEntity> newEntityIterator = creator.getNewEntityIterator(parameterObject.linesIdStart);

				for (DataSetLine dataSetLine : parameterObject.lines) {
					HajoEntity addme = newEntityIterator.next();

					addme.setName("locusId", dataSetLine.locusId);
					addme.setName("traitId", dataSetLine.traitId);
					addme.setNumerical("lodScore", dataSetLine.lodScore);

					for (String cov : dataSetLine.covariates) {
						String[] keyData = cov.split("=");
						addme.setCategory(keyData[0], Category.wrap(keyData[1]));
					}

					addme.setCategory("chromosome", Category.wrap(dataSetLine.chromosome));
					addme.setNumerical("positionMin", dataSetLine.positionMin);
					addme.setNumerical("positionPeak", dataSetLine.positionPeak);
					addme.setNumerical("positionMax", dataSetLine.positionMax);

					addme.setName("geneBankDnaId", dataSetLine.geneBankDnaId);

					creator.put(addme);
				}

				cassandra.close();

				dataSetLayer.setNumberOfItems(dataSetLayer.getNumberOfItems() + parameterObject.lines.length);
				if (parameterObject.lastUpload)
					dataSetLayer.setCalculationComplete(true);

				return null;
			}
		}.run();
	}
}
