package de.uni_luebeck.inb.krabbenhoeft.eQTL.server.helpers.persistence;

import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import org.apache.cassandra.service.Column;
import org.apache.cassandra.service.SuperColumn;

import de.uni_luebeck.inb.krabbenhoeft.eQTL.entities.Category;
import de.uni_luebeck.inb.krabbenhoeft.eQTL.entities.ColumnForDataSetLayer;
import de.uni_luebeck.inb.krabbenhoeft.eQTL.entities.HajoEntity;
import de.uni_luebeck.inb.krabbenhoeft.eQTL.entities.InvalidColumnException;
import de.uni_luebeck.inb.krabbenhoeft.eQTL.entities.InvalidTypeException;
import de.uni_luebeck.inb.krabbenhoeft.eQTL.entities.ColumnForDataSetLayer.ColumType;

public class CassandraEntityContainer {
	public static int getRandomBlock() {
		return new Random().nextInt(HajoEntity.NUMBER_OF_PARALLEL_BLOCK_IDS);
	}

	protected CassandraSession cassandra;
	protected Map<String, ColumnForDataSetLayer> columnName2columnDescriptionRead = new HashMap<String, ColumnForDataSetLayer>();
	protected Map<String, ColumnForDataSetLayer> columnName2columnDescriptionWrite = new HashMap<String, ColumnForDataSetLayer>();

	CassandraEntityContainer(CassandraSession cassandra) {
		this.cassandra = cassandra;
	}

	protected static byte[] t2b(final String text) {
		return text.getBytes(CassandraSession.charset);
	}

	protected static String b2t(byte[] data) {
		return new String(data, CassandraSession.charset);
	}

	public CassandraEntityContainer() {
		super();
	}

	protected ColumnForDataSetLayer ensureColumnExistsAndHasType(Map<String, ColumnForDataSetLayer> name2column, String propertyName, ColumType columType) {
		if (!name2column.containsKey(propertyName))
			throw new InvalidColumnException(propertyName);
		final ColumnForDataSetLayer column = name2column.get(propertyName);
		if (column.getType() != columType)
			throw new InvalidTypeException(propertyName, columType);
		return column;
	}

	public class CassandraHajoEntity implements HajoEntity {

		SuperColumn superColumn;

		CassandraHajoEntity(SuperColumn superColumn) {
			this.superColumn = superColumn;
		}

		byte[] getColumn(byte[] name) {
			for (Column col : superColumn.columns) {
				if (Arrays.equals(col.name, name))
					return col.value;
			}
			return null;
		}

		private void setColumn(byte[] name, byte[] value) {
			for (Column col : superColumn.columns) {
				if (col.name.equals(name)) {
					col.setValue(value);
					col.setTimestamp(CassandraSession.ts());
					return;
				}
			}
			superColumn.columns.add(new Column(name, value, CassandraSession.ts()));
		}

		public byte[] getEntityKey() {
			return superColumn.name;
		}

		public Category getCategory(String propertyName) {
			final byte[] name = t2b(propertyName);
			ensureColumnExistsAndHasType(columnName2columnDescriptionRead, propertyName, ColumType.Category);
			return Category.wrap(b2t(getColumn(name)));
		}

		public String getName(String propertyName) {
			final byte[] name = t2b(propertyName);
			ensureColumnExistsAndHasType(columnName2columnDescriptionRead, propertyName, ColumType.Name);
			return b2t(getColumn(name));
		}

		public double getNumerical(String propertyName) {
			final byte[] name = t2b(propertyName);
			ensureColumnExistsAndHasType(columnName2columnDescriptionRead, propertyName, ColumType.Numerical);
			return ByteBuffer.wrap(getColumn(name)).getDouble();
		}

		public long getLocation(String propertyName) {
			final byte[] name = t2b(propertyName);
			ensureColumnExistsAndHasType(columnName2columnDescriptionRead, propertyName, ColumType.Location);
			return ByteBuffer.wrap(getColumn(name)).getLong();
		}

		public void setCategory(String propertyName, Category value) {
			final byte[] name = t2b(propertyName);
			final ColumnForDataSetLayer column = ensureColumnExistsAndHasType(columnName2columnDescriptionWrite, propertyName, ColumType.Category);
			final byte[] bytes = t2b(value.getCategory());
			setColumn(name, bytes);
			column.getValues().add(value.getCategory());
		}

		public void setName(String propertyName, String value) {
			final byte[] name = t2b(propertyName);
			ensureColumnExistsAndHasType(columnName2columnDescriptionWrite, propertyName, ColumType.Name);
			final byte[] bytes = t2b(value);
			setColumn(name, bytes);
		}

		public void setNumerical(String propertyName, double value) {
			final byte[] name = t2b(propertyName);
			final ColumnForDataSetLayer column = ensureColumnExistsAndHasType(columnName2columnDescriptionWrite, propertyName, ColumType.Numerical);
			final byte[] bytes = ByteBuffer.allocate(8).putDouble(value).array();
			setColumn(name, bytes);
			if (value > column.getMax())
				column.setMax(value);
			if (value < column.getMin())
				column.setMin(value);
		}

		public void setLocation(String propertyName, long value) {
			final byte[] name = t2b(propertyName);
			final ColumnForDataSetLayer column = ensureColumnExistsAndHasType(columnName2columnDescriptionWrite, propertyName, ColumType.Location);
			final byte[] bytes = ByteBuffer.allocate(8).putLong(value).array();
			setColumn(name, bytes);
			if (value > column.getLmax())
				column.setLmax(value);
			if (value < column.getLmin())
				column.setLmin(value);
		}

		public String getAsString(String name) {
			switch (columnName2columnDescriptionRead.get(name).getType()) {
			case Category:
				return getCategory(name).getCategory();
			case Name:
				return getName(name);
			case Numerical:
				return Double.toString(getNumerical(name));
			case Location:
				return Long.toString(getLocation(name));
			}
			return null;
		}

		byte[] calculateIndexFor(final byte[] bytes) {
			return ByteBuffer.allocate(bytes.length + 4).put(bytes).put(superColumn.name).array();
		}
	}

}