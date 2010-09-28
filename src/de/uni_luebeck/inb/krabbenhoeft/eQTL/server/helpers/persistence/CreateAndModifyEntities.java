package de.uni_luebeck.inb.krabbenhoeft.eQTL.server.helpers.persistence;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.cassandra.service.Column;
import org.apache.cassandra.service.ColumnOrSuperColumn;
import org.apache.cassandra.service.SuperColumn;

import de.uni_luebeck.inb.krabbenhoeft.eQTL.entities.ColumnForDataSetLayer;
import de.uni_luebeck.inb.krabbenhoeft.eQTL.entities.DataSetLayer;
import de.uni_luebeck.inb.krabbenhoeft.eQTL.entities.HajoEntity;
import de.uni_luebeck.inb.krabbenhoeft.eQTL.entities.ColumnForDataSetLayer.ColumType;

public class CreateAndModifyEntities extends StreamingEntityRead {
	private List<String> columnNamesToDelete = new ArrayList<String>();
	private Map<String, ColumnForDataSetLayer> columnsToIndex = new HashMap<String, ColumnForDataSetLayer>();
	private String targetLayerKet;

	public CreateAndModifyEntities(CassandraSession cassandra, DataSetLayer sourceLayer, DataSetLayer targetLayer, List<ColumnForDataSetLayer> targetColumnDefinitions) {
		super(cassandra);

		if (targetLayer != null) {
			targetLayerKet = "L" + targetLayer.getKey();
			for (ColumnForDataSetLayer targetColumn : targetColumnDefinitions) {
				columnName2columnDescriptionWrite.put(targetColumn.getName(), targetColumn);
				if (targetColumn.isIndexme())
					columnsToIndex.put(targetColumn.getName(), targetColumn);
			}
		}

		if (sourceLayer != null) {
			sourceLayerKey = "L" + sourceLayer.getKey();
			for (ColumnForDataSetLayer sourceColumn : sourceLayer.getColumns()) {
				columnName2columnDescriptionRead.put(sourceColumn.getName(), sourceColumn);

				if (!columnName2columnDescriptionWrite.containsKey(sourceColumn.getName()))
					columnNamesToDelete.add(sourceColumn.getName());
			}
		}
	}

	public Iterator<HajoEntity> getNewEntityIterator(final int startingEntityId) {
		return new Iterator<HajoEntity>() {
			int id = startingEntityId;

			public boolean hasNext() {
				return true;
			}

			public HajoEntity next() {
				byte[] entityKey = ByteBuffer.allocate(4).putInt(id++).array();
				SuperColumn superColumn = new SuperColumn(entityKey, new ArrayList<Column>());
				return new CassandraHajoEntity(superColumn);
			}

			public void remove() {
			}
		};
	}

	public static class LocationForGeobox {
		public LocationForGeobox(String columnName, String chromosome, long value) {
			this.name = columnName;
			this.chromosome = chromosome;
			this.value = value;
			this.valueEnd = value;
		}

		public String name;
		public String chromosome;
		public long value;
		public long valueEnd;
	}

	public void put(HajoEntity inputEntity) {
		final CassandraHajoEntity entity = (CassandraHajoEntity) inputEntity;
		final String entityRowKey = targetLayerKet + "B" + getRandomBlock();

		final List<Column> indexSuperColumn = new ArrayList<Column>();
		indexSuperColumn.add(new Column(t2b("row"), t2b(entityRowKey), CassandraSession.ts()));
		indexSuperColumn.add(new Column(t2b("entity"), entity.getEntityKey(), CassandraSession.ts()));

		final List<LocationForGeobox> locations = new ArrayList<LocationForGeobox>();
		SuperColumn lodScoreSuperColumn = null;

		final Iterator<Column> iterator = entity.superColumn.columns.iterator();
		while (iterator.hasNext()) {
			final Column column = iterator.next();
			final String columnName = b2t(column.name);
			if (columnNamesToDelete.contains(columnName)) {
				iterator.remove();
				continue;
			}

			final ColumnForDataSetLayer indexColumn = columnsToIndex.get(columnName);
			if (indexColumn != null) {
				final SuperColumn superColumn = new SuperColumn(entity.calculateIndexFor(column.value), indexSuperColumn);
				if (indexColumn.getType() == ColumType.Location) {
					final byte[] indexChrName = t2b(indexColumn.getIndexChromosomeField());
					final LocationForGeobox loc = new LocationForGeobox(columnName, b2t(entity.getColumn(indexChrName)), ByteBuffer.wrap(column.value).getLong());
					final String indexRangeEndField = indexColumn.getIndexRangeEndField();
					if (indexRangeEndField != null)
						loc.valueEnd = ByteBuffer.wrap(entity.getColumn(t2b(indexRangeEndField))).getLong();
					locations.add(loc);
				} else {
					cassandra.addToStoreQueue(targetLayerKet + "#" + columnName, "indices", new ColumnOrSuperColumn(null, superColumn));
					if (columnName.equals("lodScore"))
						lodScoreSuperColumn = superColumn;
				}
			}
		}
		cassandra.addToStoreQueue(entityRowKey, "entities", new ColumnOrSuperColumn(null, entity.superColumn));

		for (LocationForGeobox location1 : locations) {
			for (int shift = GeoBoxHelper.minShift; shift <= GeoBoxHelper.maxShift; shift++) {
				final long boxA = GeoBoxHelper.getBoxForValue(shift, location1.value);
				final long boxB = GeoBoxHelper.getBoxForValue(shift, location1.valueEnd);
				for (long box = boxA; box <= boxB; box++) {
					final String rowKey = GeoBoxHelper.geobox1D(targetLayerKet, location1.name, shift, location1.chromosome, box);
					cassandra.addToStoreQueue(rowKey, "indices", new ColumnOrSuperColumn(null, lodScoreSuperColumn));
				}
			}
			for (LocationForGeobox location2 : locations) {
				// note: wir erzwingen hiermit eine sortierung aus loc1 und loc2
				if (location1.name.compareTo(location2.name) <= 0)
					continue;

				if (!location1.chromosome.equals(location2.chromosome))
					continue;

				for (int shift = GeoBoxHelper.minShift; shift <= GeoBoxHelper.maxShift; shift++) {
					final long box1A = GeoBoxHelper.getBoxForValue(shift, location1.value);
					final long box1B = GeoBoxHelper.getBoxForValue(shift, location1.valueEnd);
					final long box2A = GeoBoxHelper.getBoxForValue(shift, location2.value);
					final long box2B = GeoBoxHelper.getBoxForValue(shift, location2.valueEnd);
					for (long box1 = box1A; box1 <= box1B; box1++) {
						for (long box2 = box2A; box2 <= box2B; box2++) {
							final String rowKey = GeoBoxHelper.geobox2D(targetLayerKet, location1.name, location2.name, shift, location1.chromosome, box1, box2);
							cassandra.addToStoreQueue(rowKey, "indices", new ColumnOrSuperColumn(null, lodScoreSuperColumn));
						}
					}
				}
			}
		}
	}
}
