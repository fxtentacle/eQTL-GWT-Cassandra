package de.uni_luebeck.inb.krabbenhoeft.eQTL.server.processors;

import java.util.List;

import de.uni_luebeck.inb.krabbenhoeft.eQTL.entities.ColumnForDataSetLayer;
import de.uni_luebeck.inb.krabbenhoeft.eQTL.entities.ColumnForDataSetLayer.ColumType;
import de.uni_luebeck.inb.krabbenhoeft.eQTL.server.DataSetProcessor;
import de.uni_luebeck.inb.krabbenhoeft.eQTL.server.DataSetProcessorFactory;

public class FilterByCategory implements DataSetProcessorFactory {
	public String getName() {
		return "Filter by category";
	}

	public String getParameterDescription(List<ColumnForDataSetLayer> dataTypeBeforeTransformation) {
		StringBuilder ret = new StringBuilder();
		for (ColumnForDataSetLayer columnForDataSetLayer : dataTypeBeforeTransformation) {
			if (columnForDataSetLayer.getType() != ColumType.Category)
				continue;

			for (String val : columnForDataSetLayer.getValues()) {
				ret.append(columnForDataSetLayer.getName());
				ret.append("=");
				ret.append(val);
				ret.append(",");
			}
		}
		return ret.toString();
	}

	public DataSetProcessor configure(String parameters) {
		final String[] parts = parameters.split("=");
		return new FilterByCategoryProcessor(parts[0], parts[1]);
	}

	public boolean mightWorkWith(List<ColumnForDataSetLayer> dataTypeBeforeTransformation) {
		for (ColumnForDataSetLayer columnForDataSetLayer : dataTypeBeforeTransformation) {
			if (columnForDataSetLayer.getType() == ColumType.Category)
				return true;
		}
		return false;
	}
}
