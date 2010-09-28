package de.uni_luebeck.inb.krabbenhoeft.eQTL.server;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.hibernate.Session;
import org.hibernate.Transaction;

import com.google.gwt.user.server.rpc.RemoteServiceServlet;

import de.uni_luebeck.inb.krabbenhoeft.eQTL.api.gwt.DataProcessing;
import de.uni_luebeck.inb.krabbenhoeft.eQTL.api.gwt.DataSetLayerOverview;
import de.uni_luebeck.inb.krabbenhoeft.eQTL.api.gwt.DataSetProcessorOverview;
import de.uni_luebeck.inb.krabbenhoeft.eQTL.entities.ColumnForDataSetLayer;
import de.uni_luebeck.inb.krabbenhoeft.eQTL.entities.DataSet;
import de.uni_luebeck.inb.krabbenhoeft.eQTL.entities.DataSetLayer;
import de.uni_luebeck.inb.krabbenhoeft.eQTL.entities.HajoEntity;
import de.uni_luebeck.inb.krabbenhoeft.eQTL.entities.ProcessingParameters;
import de.uni_luebeck.inb.krabbenhoeft.eQTL.server.helpers.persistence.RunWithHibernate;

public class DataProcessingService extends RemoteServiceServlet implements DataProcessing {
	private static final long serialVersionUID = 1L;

	public DataSetProcessorOverview[] enumerateProcessors(final Integer dataSetLayerKey) {
		return new RunWithHibernate<DataSetProcessorOverview[]>() {
			public DataSetProcessorOverview[] work(Transaction transaction, Session session) throws Exception {

				DataSetLayer dataSetLayer = (DataSetLayer) session.load(DataSetLayer.class, dataSetLayerKey);
				final Set<DataSetProcessorFactory> factories = ProcessorRegistry.getFactoriesFor(dataSetLayer.getColumns());

				ArrayList<DataSetProcessorOverview> processorOverviews = new ArrayList<DataSetProcessorOverview>();
				for (DataSetProcessorFactory factory : factories) {
					DataSetProcessorOverview overview = new DataSetProcessorOverview();
					overview.key = factory.getClass().getName();
					overview.name = factory.getName();
					overview.parameterDescription = factory.getParameterDescription(dataSetLayer.getColumns());
					processorOverviews.add(overview);
				}
				return processorOverviews.toArray(new DataSetProcessorOverview[0]);
			}
		}.run();
	}

	public DataSetLayerOverview applyProcessor(final Integer dataSetKey, final Integer sourceDataSetLayerKey, final String processorKey, final String processorConfiguration) {
		final String[] command = new String[1];

		final DataSetLayerOverview dataSetLayerOverview = new RunWithHibernate<DataSetLayerOverview>() {
			public DataSetLayerOverview work(Transaction transaction, Session session) throws Exception {

				DataSetProcessorFactory factory = getFacoryByKey(processorKey);
				final DataSetProcessor processor = factory.configure(processorConfiguration);

				final DataSetLayer sourceDataSetLayer = (DataSetLayer) session.load(DataSetLayer.class, sourceDataSetLayerKey);
				final List<ColumnForDataSetLayer> columns = new ArrayList<ColumnForDataSetLayer>();
				for (ColumnForDataSetLayer column : sourceDataSetLayer.getColumns()) {
					columns.add(new ColumnForDataSetLayer(column));
				}
				final List<ColumnForDataSetLayer> dataTypeAfterTransformation = processor.getDataTypeAfterTransformation(columns);
				final String params;
				if (processorConfiguration.length() > 0)
					params = " (" + processorConfiguration + ")";
				else
					params = "";
				final String operationFromLastLayer = sourceDataSetLayer.getKey() + ": " + factory.getName() + params;
				final DataSet sourceDataSet = (DataSet) session.load(DataSet.class, dataSetKey);
				final DataSetLayer targetDataSetLayer = DataSetHelpers.addLayerToDataSet(session, sourceDataSet, dataTypeAfterTransformation, operationFromLastLayer);
				final Integer targetDataSetLayerKey = targetDataSetLayer.getKey();

				final int preferredItemsPerProcessor = processor.getPreferredItemsPerProcessor();
				final int preferredNumberOfParallelRunningProcessors = processor.getPreferredNumberOfParallelRunningProcessors();

				double blocksPerProcessor = (double) preferredItemsPerProcessor / (double) sourceDataSetLayer.getNumberOfItems() * (double) HajoEntity.NUMBER_OF_PARALLEL_BLOCK_IDS;
				blocksPerProcessor = Math.max(1.0f, blocksPerProcessor);

				double currentParallelBlock = 0.99;
				// to help in case we get rounding problems
				int lastBlockId = 0;

				while (lastBlockId <= HajoEntity.NUMBER_OF_PARALLEL_BLOCK_IDS) {
					ProcessingParameters parameters = new ProcessingParameters();
					parameters.setProcessorConfiguration(processorConfiguration);
					parameters.setProcessorKey(processorKey);

					parameters.setSourceDataSetLayerKey(sourceDataSetLayerKey);
					parameters.setSourceParallelBlockIdMin(lastBlockId);
					parameters.setTargetDataSetLayerKey(targetDataSetLayerKey);

					currentParallelBlock += blocksPerProcessor;
					lastBlockId = (int) Math.ceil(currentParallelBlock);
					parameters.setSourceParallelBlockIdMax(lastBlockId);

					session.persist(parameters);
				}

				command[0] = "/data_set_processor?layer=" + targetDataSetLayerKey + "&workingSetSize=" + preferredNumberOfParallelRunningProcessors;
				return DataSetHelpers.generateOverviewForDataSetLayer(dataSetKey, targetDataSetLayer);
			}
		}.run();

		invokeSelfAsync(getBasePath() + command[0]);
		return dataSetLayerOverview;
	}

	private String getBasePath() {
		System.out.println("getBasePath()");
		String path = getThreadLocalRequest().getRequestURL().toString();
		path = path.substring(0, path.indexOf('/', 9));
		System.out.println("getBasePath() = " + path);
		return path;
	}

	private static final ExecutorService threadPool = Executors.newCachedThreadPool();

	public static void invokeSelfAsync(final String command) {
		System.out.println("invokeSelfAsync: " + command);
		threadPool.execute(runnableForCommand(command));
	}

	public static Runnable runnableForCommand(final String command) {
		return new Runnable() {
			public void run() {
				try {
					new URL(command).openStream().close();
				} catch (MalformedURLException e) {
					e.printStackTrace();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		};
	}

	public static DataSetProcessorFactory getFacoryByKey(String processorKey) {
		DataSetProcessorFactory factory;
		try {
			factory = (DataSetProcessorFactory) Class.forName(processorKey).newInstance();
		} catch (InstantiationException e) {
			throw new RuntimeException("Could not create processor factory", e);
		} catch (IllegalAccessException e) {
			throw new RuntimeException("Could not create processor factory", e);
		} catch (ClassNotFoundException e) {
			throw new RuntimeException("Could not create processor factory. Maybe the processorKey is invalid?", e);
		}
		return factory;
	}
}
