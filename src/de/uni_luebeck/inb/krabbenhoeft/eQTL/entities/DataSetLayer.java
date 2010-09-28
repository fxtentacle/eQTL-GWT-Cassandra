package de.uni_luebeck.inb.krabbenhoeft.eQTL.entities;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.OrderBy;

@Entity
public class DataSetLayer {

	@Id
	@GeneratedValue
	@Column(unique = true, nullable = false)
	private Integer key;

	@Column(nullable = false)
	private String operationFromLastLayer = "UNDEFINED";

	@OneToMany
	@OrderBy("key")
	private List<ColumnForDataSetLayer> columns = new ArrayList<ColumnForDataSetLayer>();

	@Column(nullable = false)
	private Date dateCreated = new Date();

	@Column(nullable = false)
	private long numberOfItems = 0;

	@Column(nullable = false)
	private boolean calculationComplete = false;

	public Integer getKey() {
		return key;
	}

	public void setKey(Integer key) {
		this.key = key;
	}

	public String getOperationFromLastLayer() {
		return operationFromLastLayer;
	}

	public void setOperationFromLastLayer(String operationFromLastLayer) {
		this.operationFromLastLayer = operationFromLastLayer;
	}

	public List<ColumnForDataSetLayer> getColumns() {
		return columns;
	}

	public void setColumns(List<ColumnForDataSetLayer> columns) {
		this.columns = columns;
	}

	public Date getDateCreated() {
		return dateCreated;
	}

	public void setDateCreated(Date dateCreated) {
		this.dateCreated = dateCreated;
	}

	public long getNumberOfItems() {
		return numberOfItems;
	}

	public void setNumberOfItems(long numberOfItems) {
		this.numberOfItems = numberOfItems;
	}

	public boolean isCalculationComplete() {
		return calculationComplete;
	}

	public void setCalculationComplete(boolean calculationComplete) {
		this.calculationComplete = calculationComplete;
	}
}