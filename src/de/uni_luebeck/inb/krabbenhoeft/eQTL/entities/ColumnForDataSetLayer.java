package de.uni_luebeck.inb.krabbenhoeft.eQTL.entities;

import java.io.Serializable;
import java.security.InvalidParameterException;
import java.util.HashSet;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;

import org.hibernate.annotations.Type;

@Entity
public class ColumnForDataSetLayer implements Serializable {
	private static final long serialVersionUID = 1L;

	public ColumnForDataSetLayer() {
	}

	public ColumnForDataSetLayer(String name, ColumType type) {
		super();

		if (name.contains("#"))
			throw new InvalidParameterException("Column name may not contain '#'.");
		if (name.contains(":"))
			throw new InvalidParameterException("Column name may not contain ':'.");

		this.name = name;
		this.type = type;
		resetValues();
	}

	public void resetValues() {
		switch (type) {
		case Name:
			break;
		case Numerical:
			min = Double.POSITIVE_INFINITY;
			max = Double.NEGATIVE_INFINITY;
			break;
		case Location:
			lmin = Long.MAX_VALUE;
			lmax = Long.MIN_VALUE;
			break;
		case Category:
			values = new HashSet<String>();
			break;
		}
	}

	public ColumnForDataSetLayer(ColumnForDataSetLayer column) {
		this.name = column.getName();
		this.type = column.getType();
		this.min = column.getMin();
		this.max = column.getMax();
		this.lmin = column.getLmin();
		this.lmax = column.getLmax();
		if (column.getValues() != null) {
			this.values = new HashSet<String>();
			this.values.addAll(column.getValues());
		}
		this.indexme = column.isIndexme();
		this.indexChromosomeField = column.getIndexChromosomeField();
		this.indexRangeEndField = column.indexRangeEndField;
	}

	public static enum ColumType {
		Name, Numerical, Location, Category
	}

	@Id
	@GeneratedValue
	@Column(unique = true, nullable = false)
	private Integer key;

	@Column(nullable = false)
	private String name;

	@Column(nullable = false)
	@Enumerated(EnumType.STRING)
	private ColumType type;

	// for type numerical

	@Column(nullable = false)
	private double min;

	@Column(nullable = false)
	private double max;

	// for type location

	@Column(nullable = false)
	private long lmin;

	@Column(nullable = false)
	private long lmax;

	// for type category

	@Type(type = "serializable")
	private HashSet<String> values;

	@Column(nullable = false)
	private boolean indexme = false;

	@Column(nullable = true)
	private String indexChromosomeField;

	@Column(nullable = true)
	private String indexRangeEndField;

	public Integer getKey() {
		return key;
	}

	public void setKey(Integer key) {
		this.key = key;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public ColumType getType() {
		return type;
	}

	public void setType(ColumType type) {
		this.type = type;
	}

	public double getMin() {
		return min;
	}

	public void setMin(double min) {
		this.min = min;
	}

	public double getMax() {
		return max;
	}

	public void setMax(double max) {
		this.max = max;
	}

	public long getLmin() {
		return lmin;
	}

	public void setLmin(long lmin) {
		this.lmin = lmin;
	}

	public long getLmax() {
		return lmax;
	}

	public void setLmax(long lmax) {
		this.lmax = lmax;
	}

	public HashSet<String> getValues() {
		return values;
	}

	public void setValues(HashSet<String> values) {
		this.values = values;
	}

	public boolean isIndexme() {
		return indexme;
	}

	public void setIndexme(boolean indexme) {
		this.indexme = indexme;
	}

	public String getIndexChromosomeField() {
		return indexChromosomeField;
	}

	public void setIndexChromosomeField(String indexChromosomeField) {
		this.indexChromosomeField = indexChromosomeField;
	}

	public String getIndexRangeEndField() {
		return indexRangeEndField;
	}

	public void setIndexRangeEndField(String indexRangeEndField) {
		this.indexRangeEndField = indexRangeEndField;
	}

	@Override
	public String toString() {
		return getType().toString() + "Column: " + getName();
	}
}
