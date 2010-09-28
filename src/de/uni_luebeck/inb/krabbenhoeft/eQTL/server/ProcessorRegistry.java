package de.uni_luebeck.inb.krabbenhoeft.eQTL.server;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import de.uni_luebeck.inb.krabbenhoeft.eQTL.entities.ColumnForDataSetLayer;
import de.uni_luebeck.inb.krabbenhoeft.eQTL.server.processors.AnnotateGenesFromEnsemblBiomart;
import de.uni_luebeck.inb.krabbenhoeft.eQTL.server.processors.CalculateCisDistance;
import de.uni_luebeck.inb.krabbenhoeft.eQTL.server.processors.ConvertCMorganToBP_MMus;
import de.uni_luebeck.inb.krabbenhoeft.eQTL.server.processors.FilterByCategory;

public class ProcessorRegistry {
	private static Set<DataSetProcessorFactory> factories;

	public static void initFactories() {
		factories = new HashSet<DataSetProcessorFactory>();
		factories.add(new AnnotateGenesFromEnsemblBiomart());
		factories.add(new ConvertCMorganToBP_MMus());
		factories.add(new CalculateCisDistance());
		factories.add(new FilterByCategory());
	}

	public static Set<DataSetProcessorFactory> getFactoriesFor(List<ColumnForDataSetLayer> columns) {
		if (factories == null)
			initFactories();

		Set<DataSetProcessorFactory> returnme = new HashSet<DataSetProcessorFactory>();
		for (DataSetProcessorFactory factory : factories) {
			if (factory.mightWorkWith(columns))
				returnme.add(factory);
		}
		return returnme;
	}
}
