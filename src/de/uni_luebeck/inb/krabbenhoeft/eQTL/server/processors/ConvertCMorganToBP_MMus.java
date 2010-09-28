package de.uni_luebeck.inb.krabbenhoeft.eQTL.server.processors;

import java.util.List;

import de.uni_luebeck.inb.krabbenhoeft.eQTL.entities.ColumnForDataSetLayer;
import de.uni_luebeck.inb.krabbenhoeft.eQTL.entities.ColumnForDataSetLayer.ColumType;
import de.uni_luebeck.inb.krabbenhoeft.eQTL.server.DataSetProcessor;
import de.uni_luebeck.inb.krabbenhoeft.eQTL.server.DataSetProcessorFactory;

public class ConvertCMorganToBP_MMus implements DataSetProcessorFactory {
	public String getName() {
		return "Convert positions from cMorgan to BP";
	}

	public String getParameterDescription(List<ColumnForDataSetLayer> dataTypeBeforeTransformation) {
		return "";
	}

	public DataSetProcessor configure(String parameters) {
		return new ConvertCMorganToBPProcessor_MMus();
	}

	public boolean mightWorkWith(List<ColumnForDataSetLayer> dataTypeBeforeTransformation) {
		boolean hasPositionpeak = false;
		boolean hasNoConvertedPosition = true;
		for (ColumnForDataSetLayer columnForDataSetLayer : dataTypeBeforeTransformation) {
			if (columnForDataSetLayer.getName().equals("positionPeak") && columnForDataSetLayer.getType() == ColumType.Numerical)
				hasPositionpeak = true;
			if (columnForDataSetLayer.getName().equals("positionPeakBP") && columnForDataSetLayer.getType() == ColumType.Location)
				hasNoConvertedPosition = false;
		}
		return hasPositionpeak && hasNoConvertedPosition;
	}
}
