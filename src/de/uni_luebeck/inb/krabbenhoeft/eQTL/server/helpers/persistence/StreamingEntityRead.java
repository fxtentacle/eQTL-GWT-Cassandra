package de.uni_luebeck.inb.krabbenhoeft.eQTL.server.helpers.persistence;

import java.util.Iterator;
import java.util.List;
import java.util.NavigableMap;
import java.util.TreeMap;
import java.util.Map.Entry;

import org.apache.cassandra.service.ColumnOrSuperColumn;
import org.apache.cassandra.service.SuperColumn;

import de.uni_luebeck.inb.krabbenhoeft.eQTL.entities.ColumnForDataSetLayer;
import de.uni_luebeck.inb.krabbenhoeft.eQTL.entities.DataSetLayer;
import de.uni_luebeck.inb.krabbenhoeft.eQTL.entities.HajoEntity;

public class StreamingEntityRead extends CassandraEntityContainer {

	protected String sourceLayerKey;

	StreamingEntityRead(CassandraSession cassandra) {
		super(cassandra);
	}

	public StreamingEntityRead(CassandraSession cassandra, DataSetLayer sourceLayer) {
		super(cassandra);

		sourceLayerKey = "L" + sourceLayer.getKey();
		for (ColumnForDataSetLayer sourceColumn : sourceLayer.getColumns()) {
			columnName2columnDescriptionRead.put(sourceColumn.getName(), sourceColumn);
		}
	}

	private final class MergeBlocksIterator implements Iterator<HajoEntity> {
		private final int sourceParallelBlockIdMax;
		int currentSourceParallelBlockId;
		Iterator<HajoEntity> currentIterator;

		private MergeBlocksIterator(int sourceParallelBlockIdMin, int sourceParallelBlockIdMax) {
			this.sourceParallelBlockIdMax = sourceParallelBlockIdMax;
			currentSourceParallelBlockId = sourceParallelBlockIdMin;
			currentIterator = getEntitiesForOneBlock(sourceParallelBlockIdMin);
		}

		public void remove() {
			throw new UnsupportedOperationException();
		}

		public HajoEntity next() {
			return currentIterator.next();
		}

		public boolean hasNext() {
			while (!currentIterator.hasNext()) {
				currentSourceParallelBlockId++;
				if (currentSourceParallelBlockId >= sourceParallelBlockIdMax)
					return false;
				currentIterator = getEntitiesForOneBlock(currentSourceParallelBlockId);
			}
			return true;
		}
	}

	public Iterator<HajoEntity> getEntitiesForProcessor(final int sourceParallelBlockIdMin, final int sourceParallelBlockIdMax) {
		return new MergeBlocksIterator(sourceParallelBlockIdMin, sourceParallelBlockIdMax);
	}

	public class SliceIterator implements Iterator<HajoEntity> {
		int currentItem = 0;
		ColumnOrSuperColumn[] slice;

		public SliceIterator(ColumnOrSuperColumn[] slice) {
			this.slice = slice;
		}

		public void remove() {
			throw new UnsupportedOperationException();
		}

		public HajoEntity next() {
			return new CassandraHajoEntity(slice[currentItem++].super_column);
		}

		public boolean hasNext() {
			return currentItem < slice.length;
		}
	}

	public Iterator<HajoEntity> getEntitiesForOneBlock(int sourceParallelBlockId) {
		final String rowKey = sourceLayerKey + "B" + sourceParallelBlockId;
		final ColumnOrSuperColumn[] slice = cassandra.getCompleteRow("entities", rowKey).toArray(new ColumnOrSuperColumn[0]);
		return new SliceIterator(slice);
	}

	public class IndexIterator implements Iterator<HajoEntity> {
		int currentItem = 0;
		ColumnOrSuperColumn[] slice;

		public IndexIterator(ColumnOrSuperColumn[] slice) {
			this.slice = slice;
		}

		public void remove() {
			throw new UnsupportedOperationException();
		}

		public HajoEntity next() {
			final SuperColumn indexEntry = slice[currentItem++].super_column;
			final ColumnOrSuperColumn entityObject = cassandra.getSuperColumn("entities", b2t(indexEntry.columns.get(1).value), indexEntry.columns.get(0).value);
			return new CassandraHajoEntity(entityObject.super_column);
		}

		public boolean hasNext() {
			return currentItem < slice.length;
		}
	}

	public Iterator<HajoEntity> getEntitiesFromSearchIndex(String sortField, boolean sortAscending, int offset, int numberOfItems) {
		final List<ColumnOrSuperColumn> index = cassandra.getIndex(sourceLayerKey + "#" + sortField, !sortAscending, 2000);
		final int start = Math.min(offset, index.size());
		final int end = Math.min(offset + numberOfItems, index.size());
		final ColumnOrSuperColumn[] fetchThese = index.subList(start, end).toArray(new ColumnOrSuperColumn[0]);
		return new IndexIterator(fetchThese);
	}

	// min inclusive, max exclusive
	public static class FilteringIteratorWithLodScorePeek {
		private final Iterator<HajoEntity> iter;
		private final String locName;
		private final long filterMin, filterMax;

		private HajoEntity next;
		private double nextVal;

		public FilteringIteratorWithLodScorePeek(Iterator<HajoEntity> sourceIterator, String locationColumnName, long filterMin, long filterMax) {
			this.iter = sourceIterator;
			this.locName = locationColumnName;
			this.filterMin = filterMin;
			this.filterMax = filterMax;
		}

		public boolean hasNext() {
			while (true) {
				if (!iter.hasNext())
					return false;

				next = iter.next();
				final long location = next.getLocation(locName);
				if (location < filterMin || location >= filterMax)
					continue;

				nextVal = next.getNumerical("lodScore");
				return true;
			}
		}

		public double lodScore() {
			return nextVal;
		}

		public HajoEntity next() {
			return next;
		}
	}

	public class MergeGeoboxIterator implements Iterator<HajoEntity> {
		private final NavigableMap<Double, FilteringIteratorWithLodScorePeek> lod2iterator = new TreeMap<Double, FilteringIteratorWithLodScorePeek>();

		public MergeGeoboxIterator(String columnName, int shift, String chromosome, long geoboxMin, long geoboxMax, long filterMin, long filterMax) {
			for (long box = geoboxMin; box <= geoboxMax; box++) {
				final Iterator<HajoEntity> sourceIterator = getEntitiesFromGeobox(columnName, shift, chromosome, box);
				FilteringIteratorWithLodScorePeek iter = new FilteringIteratorWithLodScorePeek(sourceIterator, columnName, filterMin, filterMax);
				if (!iter.hasNext())
					continue;
				lod2iterator.put(iter.lodScore(), iter);
			}
		}

		public void remove() {
			throw new UnsupportedOperationException();
		}

		public HajoEntity next() {
			final Entry<Double, FilteringIteratorWithLodScorePeek> greatestEntry = lod2iterator.pollLastEntry();
			final FilteringIteratorWithLodScorePeek iterator = greatestEntry.getValue();
			final HajoEntity returnme = iterator.next();
			if (iterator.hasNext())
				lod2iterator.put(iterator.lodScore(), iterator);
			return returnme;
		}

		public boolean hasNext() {
			return lod2iterator.size() > 0;
		}
	}

	public Iterator<HajoEntity> getEntitiesFromGeobox(String columnName, int shift, String chromosome, long box) {
		final String rowKey = GeoBoxHelper.geobox1D(sourceLayerKey, columnName, shift, chromosome, box);
		final List<ColumnOrSuperColumn> index = cassandra.getIndex(rowKey, true, 25);
		return new IndexIterator(index.toArray(new ColumnOrSuperColumn[0]));
	}

	public Iterator<HajoEntity> getEntitiesFromGeobox2D(String columnName1, String columnName2, int shift, String chromosome, long box1, long box2) {
		final String rowKey = GeoBoxHelper.geobox2D(sourceLayerKey, columnName1, columnName2, shift, chromosome, box1, box2);
		final List<ColumnOrSuperColumn> index = cassandra.getIndex(rowKey, true, 100);
		return new IndexIterator(index.toArray(new ColumnOrSuperColumn[0]));
	}

	// NOTE: min is inclusive, max is exclusive
	public Iterator<HajoEntity> getEntitiesFromGeoboxRange(String columnName, String chromosome, long filterMin, long filterMax) {
		final long rangeLen = filterMax - filterMin;

		int shiftToUse;
		for (shiftToUse = GeoBoxHelper.minShift; shiftToUse < GeoBoxHelper.maxShift; shiftToUse++) {
			final long boxSize = GeoBoxHelper.getSizeForBox(shiftToUse);
			if (boxSize >= rangeLen)
				break;
		}

		final long boxFrom = GeoBoxHelper.getBoxForValue(shiftToUse, filterMin);
		final long boxTo = GeoBoxHelper.getBoxForValue(shiftToUse, filterMax - 1);

		if (boxFrom == boxTo) {
			long boxStart = GeoBoxHelper.getStartForBox(shiftToUse, boxFrom);
			long boxLen = GeoBoxHelper.getSizeForBox(shiftToUse);
			if (filterMin == boxStart && rangeLen == boxLen)
				return getEntitiesFromGeobox(columnName, shiftToUse, chromosome, boxFrom);
		}

		return new MergeGeoboxIterator(columnName, shiftToUse, chromosome, boxFrom, boxTo, filterMin, filterMax);
	}
}