package de.uni_luebeck.inb.krabbenhoeft.eQTL.server.processors;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Logger;

import org.hibernate.Session;
import org.hibernate.Transaction;

import de.uni_luebeck.inb.krabbenhoeft.eQTL.entities.ColumnForDataSetLayer;
import de.uni_luebeck.inb.krabbenhoeft.eQTL.entities.DataSetLayer;
import de.uni_luebeck.inb.krabbenhoeft.eQTL.entities.HajoEntity;
import de.uni_luebeck.inb.krabbenhoeft.eQTL.entities.ProcessingParameters;
import de.uni_luebeck.inb.krabbenhoeft.eQTL.server.DataSetProcessor;
import de.uni_luebeck.inb.krabbenhoeft.eQTL.server.helpers.persistence.CassandraSession;
import de.uni_luebeck.inb.krabbenhoeft.eQTL.server.helpers.persistence.CreateAndModifyEntities;
import de.uni_luebeck.inb.krabbenhoeft.eQTL.server.helpers.persistence.RunWithHibernate;

public abstract class BaseProcessorImplementation implements DataSetProcessor {
	public List<ColumnForDataSetLayer> getDataTypeAfterTransformation(List<ColumnForDataSetLayer> dataTypeBeforeTransformation) {
		List<ColumnForDataSetLayer> columns = new ArrayList<ColumnForDataSetLayer>();
		columns.addAll(dataTypeBeforeTransformation);
		addNewColumns(columns);
		return columns;
	}

	public abstract void addNewColumns(List<ColumnForDataSetLayer> columns);

	public int getPreferredItemsPerProcessor() {
		return 5 * 1000;
	}

	public int getPreferredNumberOfParallelRunningProcessors() {
		return 20;
	}

	public DataSetProcessor.ProcessingResult process(final ProcessingParameters parameterObject) {
		return new RunWithHibernate<DataSetProcessor.ProcessingResult>() {
			public DataSetProcessor.ProcessingResult work(Transaction transaction, Session session) throws Exception {
				final DataSetLayer sourceDataSetLayer = (DataSetLayer) session.load(DataSetLayer.class, parameterObject.getSourceDataSetLayerKey());
				final DataSetLayer targetDataSetLayer = (DataSetLayer) session.load(DataSetLayer.class, parameterObject.getTargetDataSetLayerKey());

				CassandraSession cassandra = new CassandraSession();
				DataSetProcessor.ProcessingResult result = new DataSetProcessor.ProcessingResult();

				final List<ColumnForDataSetLayer> origColumns = targetDataSetLayer.getColumns();
				result.columnDefinitions = new ArrayList<ColumnForDataSetLayer>();
				for (ColumnForDataSetLayer col : origColumns) {
					result.columnDefinitions.add(new ColumnForDataSetLayer(col));
				}

				CreateAndModifyEntities modifier = new CreateAndModifyEntities(cassandra, sourceDataSetLayer, targetDataSetLayer, result.columnDefinitions);
				final Iterator<HajoEntity> iterator = modifier.getEntitiesForProcessor(parameterObject.getSourceParallelBlockIdMin(), parameterObject.getSourceParallelBlockIdMax());
				result.numberOfItemsEmitted = doWork(modifier, iterator);

				final Logger log = Logger.getLogger(getClass().getName());
				log.info("process: " + result.numberOfItemsEmitted);
				cassandra.close();
				return result;
			}
		}.run();
	}

	public abstract int doWork(CreateAndModifyEntities modifier, Iterator<HajoEntity> iterator);
}