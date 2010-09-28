package de.uni_luebeck.inb.krabbenhoeft.eQTL.server;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import de.uni_luebeck.inb.krabbenhoeft.eQTL.entities.ColumnForDataSetLayer;
import de.uni_luebeck.inb.krabbenhoeft.eQTL.entities.ProcessingParameters;

public interface DataSetProcessor {
	public List<ColumnForDataSetLayer> getDataTypeAfterTransformation(List<ColumnForDataSetLayer> dataTypeBeforeTransformation);

	public int getPreferredItemsPerProcessor();

	public int getPreferredNumberOfParallelRunningProcessors();

	public static class ProcessingResult implements Serializable {
		private static final long serialVersionUID = 1L;

		public int numberOfItemsEmitted = -1;
		public List<ColumnForDataSetLayer> columnDefinitions = new ArrayList<ColumnForDataSetLayer>();
	}

	// sourceParallelBlockIdMin inclusive, -Max exclusive
	public ProcessingResult process(ProcessingParameters parameterObject);
}
