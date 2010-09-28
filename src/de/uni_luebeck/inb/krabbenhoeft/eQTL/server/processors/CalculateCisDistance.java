package de.uni_luebeck.inb.krabbenhoeft.eQTL.server.processors;

import java.util.List;

import de.uni_luebeck.inb.krabbenhoeft.eQTL.entities.ColumnForDataSetLayer;
import de.uni_luebeck.inb.krabbenhoeft.eQTL.entities.ColumnForDataSetLayer.ColumType;
import de.uni_luebeck.inb.krabbenhoeft.eQTL.server.DataSetProcessor;
import de.uni_luebeck.inb.krabbenhoeft.eQTL.server.DataSetProcessorFactory;

public class CalculateCisDistance implements DataSetProcessorFactory {
	public String getName() {
		return "Calculate cis/trans and distance from locus to gene";
	}

	public String getParameterDescription(List<ColumnForDataSetLayer> dataTypeBeforeTransformation) {
		return "";
	}

	public DataSetProcessor configure(String parameters) {
		return new CalculateCisDistanceProcessor();
	}

	public boolean mightWorkWith(List<ColumnForDataSetLayer> dataTypeBeforeTransformation) {
		boolean hasPositionPeakBP = false;
		boolean hasGeneStartBP = false;
		boolean hasGeneEndBP = false;
		boolean hasNoCisOrTrans = true;
		for (ColumnForDataSetLayer columnForDataSetLayer : dataTypeBeforeTransformation) {
			if (columnForDataSetLayer.getName().equals("positionPeakBP") && columnForDataSetLayer.getType() == ColumType.Location)
				hasPositionPeakBP = true;
			if (columnForDataSetLayer.getName().equals("geneStartBP") && columnForDataSetLayer.getType() == ColumType.Location)
				hasGeneStartBP = true;
			if (columnForDataSetLayer.getName().equals("geneEndBP") && columnForDataSetLayer.getType() == ColumType.Location)
				hasGeneEndBP = true;
			if (columnForDataSetLayer.getName().equals("cisOrTrans") && columnForDataSetLayer.getType() == ColumType.Category)
				hasNoCisOrTrans = false;
		}
		return hasPositionPeakBP && hasGeneStartBP && hasGeneEndBP && hasNoCisOrTrans;
	}
}
