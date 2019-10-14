package com.ggktech.resultobjects;

import java.util.List;

import org.codehaus.jackson.annotate.JsonProperty;

public class TestCaseResult {

	private String name;
	private String status;
	private String server;
	private String testEnv;
	private String id;
	private String startTime;
	private String endTime;
	private String totalTime;
	private String passed;
	private String failed;
	private List<TestStep> testSteps;

	@JsonProperty("Name")
	public String getName() {
		return name;
	}
	@JsonProperty("Name")
	public void setName(String name) {
		this.name = name;
	}
	
	@JsonProperty("Status")
	public String getStatus() {
		return status;
	}
	@JsonProperty("Status")
	public void setStatus(String status) {
		this.status = status;
	}
	
	@JsonProperty("Server")
	public String getServer() {
		return server;
	}
	@JsonProperty("Server")
	public void setServer(String server) {
		this.server = server;
	}
	
	@JsonProperty("TestEnv")
	public String getTestEnv() {
		return testEnv;
	}
	@JsonProperty("TestEnv")
	public void setTestEnv(String testEnv) {
		this.testEnv = testEnv;
	}
	
	@JsonProperty("Id")
	public String getId() {
		return id;
	}
	@JsonProperty("Id")
	public void setId(String id) {
		this.id = id;
	}
	
	@JsonProperty("StartTime")
	public String getStartTime() {
		return startTime;
	}
	@JsonProperty("StartTime")
	public void setStartTime(String startTime) {
		this.startTime = startTime;
	}
	
	@JsonProperty("EndTime")
	public String getEndTime() {
		return endTime;
	}
	@JsonProperty("EndTime")
	public void setEndTime(String endTime) {
		this.endTime = endTime;
	}
	
	@JsonProperty("TotalTime")
	public String getTotalTime() {
		return totalTime;
	}
	@JsonProperty("TotalTime")
	public void setTotalTime(String totalTime) {
		this.totalTime = totalTime;
	}
	
	@JsonProperty("Passed")
	public String getPassed() {
		return passed;
	}
	@JsonProperty("Passed")
	public void setPassed(String passed) {
		this.passed = passed;
	}
	
	@JsonProperty("Failed")
	public String getFailed() {
		return failed;
	}
	@JsonProperty("Failed")
	public void setFailed(String failed) {
		this.failed = failed;
	}
	
	@JsonProperty("TestSteps")
	public List<TestStep> getTestSteps() {
		return testSteps;
	}
	@JsonProperty("TestSteps")
	public void setTestSteps(List<TestStep> list) {
		this.testSteps = list;
	}
}

