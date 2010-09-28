package de.uni_luebeck.inb.krabbenhoeft.eQTL.server;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.hibernate.Session;
import org.hibernate.Transaction;

import com.google.gwt.user.server.rpc.RemoteServiceServlet;

import de.uni_luebeck.inb.krabbenhoeft.eQTL.api.gwt.CalculationInProgressException;
import de.uni_luebeck.inb.krabbenhoeft.eQTL.api.gwt.DataRetrieval;
import de.uni_luebeck.inb.krabbenhoeft.eQTL.api.gwt.DataSetLayerOverview;
import de.uni_luebeck.inb.krabbenhoeft.eQTL.api.gwt.DataSetOverview;
import de.uni_luebeck.inb.krabbenhoeft.eQTL.api.gwt.ExpressionQtlTrackEntry;
import de.uni_luebeck.inb.krabbenhoeft.eQTL.api.gwt.ExpressionQtlTrackEntry2D;
import de.uni_luebeck.inb.krabbenhoeft.eQTL.api.gwt.GenomeRange;
import de.uni_luebeck.inb.krabbenhoeft.eQTL.entities.ColumnForDataSetLayer;
import de.uni_luebeck.inb.krabbenhoeft.eQTL.entities.DataSet;
import de.uni_luebeck.inb.krabbenhoeft.eQTL.entities.DataSetLayer;
import de.uni_luebeck.inb.krabbenhoeft.eQTL.entities.HajoEntity;
import de.uni_luebeck.inb.krabbenhoeft.eQTL.server.helpers.persistence.CassandraSession;
import de.uni_luebeck.inb.krabbenhoeft.eQTL.server.helpers.persistence.GeoBoxHelper;
import de.uni_luebeck.inb.krabbenhoeft.eQTL.server.helpers.persistence.RunWithHibernate;
import de.uni_luebeck.inb.krabbenhoeft.eQTL.server.helpers.persistence.StreamingEntityRead;

public class DataRetrievalService extends RemoteServiceServlet implements DataRetrieval {
	private static final long serialVersionUID = 1L;

	public Map<Integer, String> enumerateDataSets() {
		return new RunWithHibernate<Map<Integer, String>>() {
			public Map<Integer, String> work(Transaction transaction, Session session) throws Exception {
				final List<DataSet> dataSets = DataSetHelpers.listMyDataSets(session, getCurrentUser());
				Map<Integer, String> map = new HashMap<Integer, String>();
				for (DataSet dataSet : dataSets) {
					map.put(dataSet.getKey(), dataSet.getName() + SimpleDateFormat.getDateTimeInstance().format(dataSet.getDateCreated()));
				}
				return map;
			}
		}.run();
	}

	public DataSetOverview getOverview(final Integer dataSetKey) {
		return new RunWithHibernate<DataSetOverview>() {
			public DataSetOverview work(Transaction transaction, Session session) throws Exception {
				DataSet dataSet = (DataSet) session.load(DataSet.class, dataSetKey);

				DataSetOverview overview = new DataSetOverview();
				overview.dateCreated = dataSet.getDateCreated();
				overview.key = dataSet.getKey();
				final List<DataSetLayer> layers = dataSet.getLayers();
				overview.layers = new DataSetLayerOverview[layers.size()];
				for (int i = 0; i < layers.size(); i++) {
					overview.layers[i] = DataSetHelpers.generateOverviewForDataSetLayer(dataSetKey, layers.get(i));
				}
				overview.name = dataSet.getName();

				return overview;
			}
		}.run();
	}

	public String[][] getLayerRows(final Integer dataSetLayerKey, final int offset, final int numberOfItems) {
		return new ClientRowsFromIterator(dataSetLayerKey) {
			public Iterator<HajoEntity> prepareIterator(StreamingEntityRead read) {
				return read.getEntitiesFromSearchIndex("lodScore", false, offset, numberOfItems);
			}
		}.run();
	}

	public DataSetLayerOverview getLayerAfterCalculationCompletes(final Integer dataSetKey, final Integer dataSetLayerKey) {
		final DataSetLayerOverview dataSetLayerOverview = new RunWithHibernate<DataSetLayerOverview>() {
			public DataSetLayerOverview work(Transaction transaction, Session session) throws Exception {
				DataSetLayer dataSetLayer = (DataSetLayer) session.load(DataSetLayer.class, dataSetLayerKey);
				if (!dataSetLayer.isCalculationComplete())
					return null;
				return DataSetHelpers.generateOverviewForDataSetLayer(dataSetKey, dataSetLayer);
			}
		}.run();

		if (dataSetLayerOverview == null)
			throw new CalculationInProgressException();
		return dataSetLayerOverview;
	}

	public String[][] getTopRowsForRange(Integer dataSetLayerKey, final String locationColumnName, final GenomeRange tableRange) {
		return new ClientRowsFromIterator(dataSetLayerKey) {
			public Iterator<HajoEntity> prepareIterator(StreamingEntityRead read) {
				return read.getEntitiesFromGeoboxRange(locationColumnName, tableRange.chromosome, tableRange.fromBP, tableRange.toBP);
			}
		}.run();
	}

	public ExpressionQtlTrackEntry[] getTopEntriesForRange(final Integer dataSetLayerKey, final String locationColumnName, final GenomeRange genomeRange) {
		return new RunWithHibernate<ExpressionQtlTrackEntry[]>() {
			public ExpressionQtlTrackEntry[] work(Transaction transaction, Session session) throws Exception {
				DataSetLayer dsl = (DataSetLayer) session.load(DataSetLayer.class, dataSetLayerKey);
				ColumnForDataSetLayer[] columns = dsl.getColumns().toArray(new ColumnForDataSetLayer[0]);

				ColumnForDataSetLayer lodColumn = null;
				ColumnForDataSetLayer locationColumn = null;
				for (ColumnForDataSetLayer col : columns) {
					if (col.getName().equals("lodScore"))
						lodColumn = col;
					if (col.getName().equals(locationColumnName))
						locationColumn = col;
				}

				CassandraSession cassandra = new CassandraSession();
				StreamingEntityRead read = new StreamingEntityRead(cassandra, dsl);

				final List<ExpressionQtlTrackEntry> output = new ArrayList<ExpressionQtlTrackEntry>();
				final Iterator<HajoEntity> reader = read.getEntitiesFromGeoboxRange(locationColumnName, genomeRange.chromosome, genomeRange.fromBP, genomeRange.toBP);
				while (reader.hasNext()) {
					final HajoEntity entity = reader.next();
					final ExpressionQtlTrackEntry addme = new ExpressionQtlTrackEntry();

					addme.locusId = entity.getName("locusId");
					addme.traitId = entity.getName("traitId");
					addme.lodScore = entity.getNumerical("lodScore");
					addme.lodScoreInMinMaxRange = (addme.lodScore - lodColumn.getMin()) / (lodColumn.getMax() - lodColumn.getMin());
					addme.positionStart = entity.getLocation(locationColumnName);

					final String indexRangeEndField = locationColumn.getIndexRangeEndField();
					if (indexRangeEndField == null)
						addme.positionEnd = addme.positionStart;
					else
						addme.positionEnd = entity.getLocation(indexRangeEndField);

					output.add(addme);
					if (output.size() >= 25)
						break;
				}
				cassandra.close();
				return output.toArray(new ExpressionQtlTrackEntry[0]);
			}
		}.run();
	}

	public ExpressionQtlTrackEntry2D[] getTopEntriesForArea(final Integer dataSetLayerKey, final String positionColumnX, final GenomeRange genomeRangeX, final String positionColumnY,
			final GenomeRange genomeRangeY) {
		return new RunWithHibernate<ExpressionQtlTrackEntry2D[]>() {
			public ExpressionQtlTrackEntry2D[] work(Transaction transaction, Session session) throws Exception {
				DataSetLayer dsl = (DataSetLayer) session.load(DataSetLayer.class, dataSetLayerKey);
				ColumnForDataSetLayer[] columns = dsl.getColumns().toArray(new ColumnForDataSetLayer[0]);

				ColumnForDataSetLayer lodColumn = null;
				ColumnForDataSetLayer locationColumnX = null;
				ColumnForDataSetLayer locationColumnY = null;
				for (ColumnForDataSetLayer col : columns) {
					if (col.getName().equals("lodScore"))
						lodColumn = col;
					if (col.getName().equals(positionColumnX))
						locationColumnX = col;
					if (col.getName().equals(positionColumnY))
						locationColumnY = col;
				}

				long rangeLen = Math.max(genomeRangeX.toBP - genomeRangeX.fromBP, genomeRangeY.toBP - genomeRangeY.fromBP);
				int shiftToUse;
				for (shiftToUse = GeoBoxHelper.minShift; shiftToUse < GeoBoxHelper.maxShift; shiftToUse++) {
					final long boxSize = GeoBoxHelper.getSizeForBox(shiftToUse);
					if (boxSize >= rangeLen)
						break;
				}

				CassandraSession cassandra = new CassandraSession();
				StreamingEntityRead read = new StreamingEntityRead(cassandra, dsl);

				final List<ExpressionQtlTrackEntry2D> output = new ArrayList<ExpressionQtlTrackEntry2D>();
				final long boxX = GeoBoxHelper.getBoxForValue(shiftToUse, genomeRangeX.fromBP);
				final long boxY = GeoBoxHelper.getBoxForValue(shiftToUse, genomeRangeY.fromBP);
				final Iterator<HajoEntity> reader = read.getEntitiesFromGeobox2D(positionColumnX, positionColumnY, shiftToUse, genomeRangeX.chromosome, boxX, boxY);
				while (reader.hasNext()) {
					final HajoEntity entity = reader.next();
					final ExpressionQtlTrackEntry2D addme = new ExpressionQtlTrackEntry2D();

					addme.positionXStart = entity.getLocation(positionColumnX);
					if (addme.positionXStart > genomeRangeX.toBP)
						continue;

					final String indexRangeEndField = locationColumnX.getIndexRangeEndField();
					if (indexRangeEndField == null)
						addme.positionXEnd = addme.positionXStart;
					else
						addme.positionXEnd = entity.getLocation(indexRangeEndField);
					if (addme.positionXEnd < genomeRangeX.fromBP)
						continue;

					addme.positionYStart = entity.getLocation(positionColumnY);
					if (addme.positionYStart > genomeRangeY.toBP)
						continue;

					final String indexRangeEndField2 = locationColumnY.getIndexRangeEndField();
					if (indexRangeEndField2 == null)
						addme.positionYEnd = addme.positionYStart;
					else
						addme.positionYEnd = entity.getLocation(indexRangeEndField2);
					if (addme.positionYEnd < genomeRangeY.fromBP)
						continue;

					addme.locusId = entity.getName("locusId");
					addme.traitId = entity.getName("traitId");
					addme.lodScore = entity.getNumerical("lodScore");
					addme.lodScoreInMinMaxRange = (addme.lodScore - lodColumn.getMin()) / (lodColumn.getMax() - lodColumn.getMin());

					output.add(addme);
				}
				cassandra.close();
				return output.toArray(new ExpressionQtlTrackEntry2D[0]);
			}
		}.run();
	}

	// little helper class

	private static abstract class ClientRowsFromIterator extends RunWithHibernate<String[][]> {
		private final Integer dataSetLayerKey;

		private ClientRowsFromIterator(Integer dataSetLayerKey) {
			this.dataSetLayerKey = dataSetLayerKey;
		}

		public String[][] work(Transaction transaction, Session session) throws Exception {
			DataSetLayer dsl = (DataSetLayer) session.load(DataSetLayer.class, dataSetLayerKey);
			ColumnForDataSetLayer[] columns = dsl.getColumns().toArray(new ColumnForDataSetLayer[0]);

			CassandraSession cassandra = new CassandraSession();
			StreamingEntityRead read = new StreamingEntityRead(cassandra, dsl);

			final List<String[]> output = new ArrayList<String[]>();
			final Iterator<HajoEntity> reader = prepareIterator(read);
			while (reader.hasNext()) {
				final HajoEntity entity = reader.next();

				String[] tmp = new String[columns.length];
				for (int j = 0; j < columns.length; j++) {
					tmp[j] = entity.getAsString(columns[j].getName());
				}
				output.add(tmp);

				if (output.size() >= 25)
					break;
			}
			cassandra.close();
			return output.toArray(new String[0][]);
		}

		public abstract Iterator<HajoEntity> prepareIterator(StreamingEntityRead read);
	}

}
