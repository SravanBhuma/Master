package com.ggktech.resultobjects;

import org.codehaus.jackson.annotate.JsonProperty;

public class PieChart {
	
	private String failureTitle;
	private String successTitle;
	private String successCount;
	private String failureCount;
	private String piechartTitle;
	
	@JsonProperty("failureTitle")
	public String getFailureTitle() {
		return failureTitle;
	}
	@JsonProperty("failureTitle")
	public void setFailureTitle(String failureTitle) {
		this.failureTitle = failureTitle;
	}
	
	@JsonProperty("successTitle")
	public String getSuccessTitle() {
		return successTitle;
	}
	@JsonProperty("successTitle")
	public void setSuccessTitle(String successTitle) {
		this.successTitle = successTitle;
	}
	
	@JsonProperty("successCount")
	public String getSuccessCount() {
		return successCount;
	}
	@JsonProperty("successCount")
	public void setSuccessCount(String successCount) {
		this.successCount = successCount;
	}
	
	@JsonProperty("failureCount")
	public String getFailureCount() {
		return failureCount;
	}
	@JsonProperty("failureCount")
	public void setFailureCount(String failureCount) {
		this.failureCount = failureCount;
	}
	
	@JsonProperty("piechartTitle")
	public String getPiechartTitle() {
		return piechartTitle;
	}
	@JsonProperty("piechartTitle")
	public void setPiechartTitle(String piechartTitle) {
		this.piechartTitle = piechartTitle;
	}
}
