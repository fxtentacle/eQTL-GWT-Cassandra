package de.uni_luebeck.inb.krabbenhoeft.eQTL.server.processors;

import java.util.Iterator;
import java.util.List;

import de.uni_luebeck.inb.krabbenhoeft.eQTL.entities.Category;
import de.uni_luebeck.inb.krabbenhoeft.eQTL.entities.ColumnForDataSetLayer;
import de.uni_luebeck.inb.krabbenhoeft.eQTL.entities.HajoEntity;
import de.uni_luebeck.inb.krabbenhoeft.eQTL.entities.ColumnForDataSetLayer.ColumType;
import de.uni_luebeck.inb.krabbenhoeft.eQTL.server.helpers.persistence.CreateAndModifyEntities;

public class CalculateCisDistanceProcessor extends BaseProcessorImplementation {

	@Override
	public void addNewColumns(List<ColumnForDataSetLayer> columns) {
		columns.add(new ColumnForDataSetLayer("cisOrTrans", ColumType.Category));
		columns.add(new ColumnForDataSetLayer("cisDistance", ColumType.Numerical));
	}

	@Override
	public int doWork(CreateAndModifyEntities modifier, Iterator<HajoEntity> iter) {
		int count = 0;
		while (iter.hasNext()) {
			final HajoEntity target = iter.next();
			boolean isCis = target.getCategory("chromosome").equals(target.getCategory("geneChromosome"));
			target.setCategory("cisOrTrans", Category.wrap(isCis ? "cis" : "trans"));
			double distance = Double.POSITIVE_INFINITY;

			if (isCis) {
				double peak = target.getNumerical("positionPeakBP");
				double min = target.getNumerical("geneStartBP");
				double max = target.getNumerical("geneEndBP");

				if (peak < min)
					distance = min - peak;
				else if (peak > max)
					distance = peak - max;
				else
					distance = 0;
			}

			target.setNumerical("cisDistance", distance);
			modifier.put(target);
			count++;
		}
		return count;
	}

}
