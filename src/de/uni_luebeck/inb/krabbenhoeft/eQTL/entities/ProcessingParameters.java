package de.uni_luebeck.inb.krabbenhoeft.eQTL.entities;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;

import de.uni_luebeck.inb.krabbenhoeft.eQTL.server.DataSetProcessor;

@Entity
public class ProcessingParameters {

	@Id
	@GeneratedValue
	@Column(unique = true, nullable = false)
	private Integer key;

	// processing parameters
	@Column(nullable = false)
	private Integer sourceDataSetLayerKey;
	@Column(nullable = false)
	private int sourceParallelBlockIdMin;
	@Column(nullable = false)
	private int sourceParallelBlockIdMax;
	@Column(nullable = false)
	private Integer targetDataSetLayerKey;

	@Column(nullable = false)
	private String processorKey;
	@Column(nullable = false)
	private String processorConfiguration;

	@Column(nullable = true)
	private DataSetProcessor.ProcessingResult processingResult;

	public Integer getKey() {
		return key;
	}

	public void setKey(Integer key) {
		this.key = key;
	}

	public Integer getSourceDataSetLayerKey() {
		return sourceDataSetLayerKey;
	}

	public void setSourceDataSetLayerKey(Integer sourceDataSetLayerKey) {
		this.sourceDataSetLayerKey = sourceDataSetLayerKey;
	}

	public int getSourceParallelBlockIdMin() {
		return sourceParallelBlockIdMin;
	}

	public void setSourceParallelBlockIdMin(int sourceParallelBlockIdMin) {
		this.sourceParallelBlockIdMin = sourceParallelBlockIdMin;
	}

	public int getSourceParallelBlockIdMax() {
		return sourceParallelBlockIdMax;
	}

	public void setSourceParallelBlockIdMax(int sourceParallelBlockIdMax) {
		this.sourceParallelBlockIdMax = sourceParallelBlockIdMax;
	}

	public Integer getTargetDataSetLayerKey() {
		return targetDataSetLayerKey;
	}

	public void setTargetDataSetLayerKey(Integer targetDataSetLayerKey) {
		this.targetDataSetLayerKey = targetDataSetLayerKey;
	}

	public String getProcessorKey() {
		return processorKey;
	}

	public void setProcessorKey(String processorKey) {
		this.processorKey = processorKey;
	}

	public String getProcessorConfiguration() {
		return processorConfiguration;
	}

	public void setProcessorConfiguration(String processorConfiguration) {
		this.processorConfiguration = processorConfiguration;
	}

	public DataSetProcessor.ProcessingResult getProcessingResult() {
		return processingResult;
	}

	public void setProcessingResult(DataSetProcessor.ProcessingResult processingResult) {
		this.processingResult = processingResult;
	}
}
