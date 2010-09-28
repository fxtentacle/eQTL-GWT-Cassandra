package de.uni_luebeck.inb.krabbenhoeft.eQTL.server.processors;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import de.uni_luebeck.inb.krabbenhoeft.eQTL.entities.ColumnForDataSetLayer;
import de.uni_luebeck.inb.krabbenhoeft.eQTL.entities.HajoEntity;
import de.uni_luebeck.inb.krabbenhoeft.eQTL.entities.ColumnForDataSetLayer.ColumType;
import de.uni_luebeck.inb.krabbenhoeft.eQTL.server.helpers.persistence.CreateAndModifyEntities;

public class ConvertCMorganToBPProcessor_MMus extends BaseProcessorImplementation {

	@Override
	public void addNewColumns(List<ColumnForDataSetLayer> columns) {
		columns.add(new ColumnForDataSetLayer("positionMinBP", ColumType.Location));
		final ColumnForDataSetLayer peakBpColumn = new ColumnForDataSetLayer("positionPeakBP", ColumType.Location);
		peakBpColumn.setIndexme(true);
		peakBpColumn.setIndexChromosomeField("chromosome");
		columns.add(peakBpColumn);
		columns.add(new ColumnForDataSetLayer("positionMaxBP", ColumType.Location));
	}

	@Override
	public int doWork(CreateAndModifyEntities modifier, Iterator<HajoEntity> iter) {
		int count = 0;
		while (iter.hasNext()) {
			final HajoEntity target = iter.next();
			final String chr = target.getCategory("chromosome").getCategory();
			target.setLocation("positionMinBP", cM2bp(chr, target.getNumerical("positionMin")));
			target.setLocation("positionPeakBP", cM2bp(chr, target.getNumerical("positionPeak")));
			target.setLocation("positionMaxBP", cM2bp(chr, target.getNumerical("positionMax")));
			modifier.put(target);
			count++;
		}
		return count;
	}

	Map<String, Map<Double, Integer>> map = new HashMap<String, Map<Double, Integer>>();

	public ConvertCMorganToBPProcessor_MMus() {
		Map<Double, Integer> tmp;

		INSERT YOUR MARKER DATA HERE
	}

	public long cM2bp(String chromosome, double cm) {
		Map<Double, Integer> chrConv = map.get(chromosome);
		Map.Entry<Double, Integer> less = null, more = null;
		for (Map.Entry<Double, Integer> cur : chrConv.entrySet()) {
			if (cur.getKey() <= cm && (less == null || less.getKey() < cur.getKey()))
				less = cur;
			if (cur.getKey() >= cm && (more == null || more.getKey() > cur.getKey()))
				more = cur;
		}
		if (more == null) {
			more = less;
			less = null;
			for (Map.Entry<Double, Integer> cur : chrConv.entrySet()) {
				if (cur.getKey() < more.getKey() && (less == null || less.getKey() < cur.getKey()))
					less = cur;
			}
		} else if (less == null) {
			less = more;
			more = null;
			for (Map.Entry<Double, Integer> cur : chrConv.entrySet()) {
				if (cur.getKey() > less.getKey() && (more == null || more.getKey() > cur.getKey()))
					more = cur;
			}
		}
		if (more == less)
			return more.getValue();
		double lerp = (cm - less.getKey()) / (more.getKey() - less.getKey());
		return Math.round(more.getValue() * lerp + less.getValue() * (1 - lerp));
	}
}
