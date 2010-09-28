package de.uni_luebeck.inb.krabbenhoeft.eQTL.server.processors;

import java.util.List;

import de.uni_luebeck.inb.krabbenhoeft.eQTL.entities.ColumnForDataSetLayer;
import de.uni_luebeck.inb.krabbenhoeft.eQTL.entities.ColumnForDataSetLayer.ColumType;
import de.uni_luebeck.inb.krabbenhoeft.eQTL.server.DataSetProcessor;
import de.uni_luebeck.inb.krabbenhoeft.eQTL.server.DataSetProcessorFactory;

public class AnnotateGenesFromEnsemblBiomart implements DataSetProcessorFactory {
	public DataSetProcessor configure(String parameters) {
		return new AnnotateGenesFromEnsemblBiomartProcessor();
	}

	public String getName() {
		return "Annotate Genes using Ensembl BioMart";
	}

	public String getParameterDescription(List<ColumnForDataSetLayer> dataTypeBeforeTransformation) {
		return "";
	}

	public boolean mightWorkWith(List<ColumnForDataSetLayer> dataTypeBeforeTransformation) {
		boolean hasGeneBankDnaId = false;
		boolean hasNoEnsemblGeneId = true;
		for (ColumnForDataSetLayer columnForDataSetLayer : dataTypeBeforeTransformation) {
			if (columnForDataSetLayer.getName().equals("geneBankDnaId") && columnForDataSetLayer.getType() == ColumType.Name)
				hasGeneBankDnaId = true;
			if (columnForDataSetLayer.getName().equals("ensemblGeneId") && columnForDataSetLayer.getType() == ColumType.Name)
				hasNoEnsemblGeneId = false;
		}
		return hasGeneBankDnaId && hasNoEnsemblGeneId;
	}
}
