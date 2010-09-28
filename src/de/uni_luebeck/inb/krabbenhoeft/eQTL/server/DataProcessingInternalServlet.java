package de.uni_luebeck.inb.krabbenhoeft.eQTL.server;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.hibernate.Session;
import org.hibernate.Transaction;

import de.uni_luebeck.inb.krabbenhoeft.eQTL.entities.ColumnForDataSetLayer;
import de.uni_luebeck.inb.krabbenhoeft.eQTL.entities.DataSetLayer;
import de.uni_luebeck.inb.krabbenhoeft.eQTL.entities.ProcessingParameters;
import de.uni_luebeck.inb.krabbenhoeft.eQTL.server.DataSetProcessor.ProcessingResult;
import de.uni_luebeck.inb.krabbenhoeft.eQTL.server.helpers.persistence.RunWithHibernate;

public class DataProcessingInternalServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;
	private final Logger log = Logger.getLogger(getClass().getName());

	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		resp.setStatus(200);

		final String basePath = getBasePath(req);
		final String key = req.getParameter("key");
		if (key != null) {
			invokeProcessor(key);
			return;
		}

		final String layer = req.getParameter("layer");
		final String workingSetSize = req.getParameter("workingSetSize");

		if (workingSetSize != null) {
			invokeWorkingSet(basePath, Integer.parseInt(layer), Integer.parseInt(workingSetSize));
			return;
		}

		final String cleanup = req.getParameter("cleanup");
		if (cleanup != null) {
			invokeCleanup(basePath, Integer.parseInt(layer), Integer.parseInt(cleanup));
			return;
		}
	}

	@SuppressWarnings("unchecked")
	public void invokeWorkingSet(final String basePath, final int targetDataSetLayerKey, final int workingSetSize) {
		log.info("Invoking a working set of size " + workingSetSize + " for target layer " + targetDataSetLayerKey);

		final ExecutorService threadPool = Executors.newCachedThreadPool();

		boolean hasMore = new RunWithHibernate<Boolean>() {
			public Boolean work(Transaction transaction, Session session) throws Exception {
				final org.hibernate.Query query = session.createQuery("select key from ProcessingParameters where targetDataSetLayerKey = :a and processingResult is null");
				final List<Integer> callThese = query.setParameter("a", targetDataSetLayerKey).setMaxResults(workingSetSize + 1).list();
				boolean hasMore = callThese.size() > workingSetSize;
				boolean skipOne = hasMore;
				for (final Integer key : callThese) {
					if (skipOne) {
						skipOne = false;
						continue;
					}

					final String command = basePath + "/data_set_processor?key=" + Integer.toString(key);
					threadPool.execute(DataProcessingService.runnableForCommand(command));
				}
				return hasMore;
			}
		}.run();

		threadPool.shutdown();
		try {
			threadPool.awaitTermination(5, TimeUnit.MINUTES);
		} catch (InterruptedException e) {
		}

		if (hasMore) {
			final String command = "/data_set_processor?layer=" + targetDataSetLayerKey + "&workingSetSize=" + workingSetSize;
			DataProcessingService.invokeSelfAsync(basePath + command);
		} else
			DataProcessingService.invokeSelfAsync(basePath + "/data_set_processor?layer=" + targetDataSetLayerKey + "&cleanup=1");
	}

	public void invokeProcessor(String key) {
		log.info("Invoking processor for key " + key);
		final Integer keyInt = Integer.valueOf(key);

		new RunWithHibernate<Void>() {
			public Void work(Transaction transaction, Session session) throws Exception {
				ProcessingParameters parameters = (ProcessingParameters) session.load(ProcessingParameters.class, keyInt);
				final DataSetProcessorFactory factory = DataProcessingService.getFacoryByKey(parameters.getProcessorKey());
				final DataSetProcessor processor = factory.configure(parameters.getProcessorConfiguration());
				final ProcessingResult processingResult = processor.process(parameters);
				parameters.setProcessingResult(processingResult);
				session.flush();
				return null;
			}
		}.run();
	}

	@SuppressWarnings("unchecked")
	private void invokeCleanup(final String basePath, final int targetDataSetLayerKey, final int cleanupsLeft) {
		log.info("Invoking cleanup for layer " + targetDataSetLayerKey);

		boolean retry = new RunWithHibernate<Boolean>() {
			public Boolean work(Transaction transaction, Session session) throws Exception {
				final DataSetLayer dataSetLayer = (DataSetLayer) session.load(DataSetLayer.class, targetDataSetLayerKey);

				Map<String, ColumnForDataSetLayer> name2column = new HashMap<String, ColumnForDataSetLayer>();
				for (ColumnForDataSetLayer column : dataSetLayer.getColumns()) {
					name2column.put(column.getName(), column);
				}

				final int workingSetSize = 990;

				final org.hibernate.Query query = session.createQuery("from ProcessingParameters where targetDataSetLayerKey = :a");
				final List<ProcessingParameters> processingWorkers = query.setParameter("a", targetDataSetLayerKey).setMaxResults(workingSetSize + 1).list();
				boolean hasMore = processingWorkers.size() > workingSetSize;
				boolean skipOne = hasMore;
				boolean missingResult = false;

				final List<ProcessingParameters> processingWorkersToDelete = new ArrayList<ProcessingParameters>();
				for (ProcessingParameters worker : processingWorkers) {
					if (skipOne) {
						skipOne = false;
						continue;
					}

					final ProcessingResult result = worker.getProcessingResult();
					if (result == null) {
						missingResult = true;
						continue;
					}

					dataSetLayer.setNumberOfItems(dataSetLayer.getNumberOfItems() + result.numberOfItemsEmitted);
					for (ColumnForDataSetLayer column : result.columnDefinitions) {
						switch (column.getType()) {
						case Category:
							name2column.get(column.getName()).getValues().addAll(column.getValues());
							break;
						case Numerical: {
							final ColumnForDataSetLayer ocol = name2column.get(column.getName());
							ocol.setMin(Math.min(ocol.getMin(), column.getMin()));
							ocol.setMax(Math.max(ocol.getMax(), column.getMax()));
							break;
						}
						default:
							break;
						}
					}
					processingWorkersToDelete.add(worker);
				}

				if (processingWorkersToDelete.size() > 0) {
					// remove all the workes we just collected
					for (ProcessingParameters processingParameters : processingWorkersToDelete)
						session.delete(processingParameters);
				}

				final boolean retry = (hasMore || missingResult) && cleanupsLeft > 0;
				if (!retry) {
					dataSetLayer.setCalculationComplete(true);
					session.flush();
				}

				return retry;
			}
		}.run();

		if (retry)
			DataProcessingService.invokeSelfAsync(basePath + "/data_set_processor?layer=" + targetDataSetLayerKey + "&cleanup=" + (cleanupsLeft - 1));
	}

	private String getBasePath(HttpServletRequest request) {
		String path = request.getRequestURL().toString();
		path = path.substring(0, path.indexOf('/', 9));
		return path;
	}

}
