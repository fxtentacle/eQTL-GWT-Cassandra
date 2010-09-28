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
public class DataSet {

	@Id
	@GeneratedValue
	@Column(unique = true, nullable = false)
	private Integer key;

	@Column(nullable = false)
	private String ownerMailAddress;

	@Column(nullable = false)
	private String name;

	@Column(nullable = false)
	private Date dateCreated;

	@Column(nullable = false)
	private Date dateAccessed;

	@OneToMany
	@OrderBy("dateCreated")
	private List<DataSetLayer> layers = new ArrayList<DataSetLayer>();

	public Integer getKey() {
		return key;
	}

	public void setKey(Integer key) {
		this.key = key;
	}

	public String getOwnerMailAddress() {
		return ownerMailAddress;
	}

	public void setOwnerMailAddress(String owner) {
		this.ownerMailAddress = owner;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public Date getDateCreated() {
		return dateCreated;
	}

	public void setDateCreated(Date dateCreated) {
		this.dateCreated = dateCreated;
	}

	public Date getDateAccessed() {
		return dateAccessed;
	}

	public void setDateAccessed(Date dateAccessed) {
		this.dateAccessed = dateAccessed;
	}

	public List<DataSetLayer> getLayers() {
		return layers;
	}

	public void setLayers(List<DataSetLayer> layers) {
		this.layers = layers;
	}

}