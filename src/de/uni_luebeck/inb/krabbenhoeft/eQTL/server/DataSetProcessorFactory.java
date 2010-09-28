package de.uni_luebeck.inb.krabbenhoeft.eQTL.server;

import java.util.List;

import de.uni_luebeck.inb.krabbenhoeft.eQTL.entities.ColumnForDataSetLayer;

public interface DataSetProcessorFactory {
	public String getName();

	public boolean mightWorkWith(List<ColumnForDataSetLayer> dataTypeBeforeTransformation);

	public String getParameterDescription(List<ColumnForDataSetLayer> dataTypeBeforeTransformation);

	public DataSetProcessor configure(String parameters);
}
