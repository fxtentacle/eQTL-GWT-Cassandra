package de.uni_luebeck.inb.krabbenhoeft.eQTL.server.processors;

import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import de.uni_luebeck.inb.krabbenhoeft.eQTL.entities.Category;
import de.uni_luebeck.inb.krabbenhoeft.eQTL.entities.ColumnForDataSetLayer;
import de.uni_luebeck.inb.krabbenhoeft.eQTL.entities.HajoEntity;
import de.uni_luebeck.inb.krabbenhoeft.eQTL.server.helpers.persistence.CreateAndModifyEntities;

public class FilterByCategoryProcessor extends BaseProcessorImplementation {
	private final String column;
	private final Category value;

	public FilterByCategoryProcessor(String column, String value) {
		this.column = column;
		this.value = Category.wrap(value);
	}

	@Override
	public void addNewColumns(List<ColumnForDataSetLayer> columns) {
		Set<String> retain = new HashSet<String>();
		retain.add(value.getCategory());

		for (ColumnForDataSetLayer cur : columns) {
			if (cur.getName().equals(column))
				cur.getValues().retainAll(retain);
		}
	}

	@Override
	public int doWork(CreateAndModifyEntities modifier, Iterator<HajoEntity> iter) {
		int count = 0;
		while (iter.hasNext()) {
			final HajoEntity target = iter.next();
			if (!target.getCategory(column).equals(value))
				continue;

			modifier.put(target);
			count++;
		}
		return count;
	}

}
