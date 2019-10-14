package com.ggktech.resultobjects;

import java.util.List;

import org.codehaus.jackson.annotate.JsonProperty;

public class Module {

	private String Name;
	private String StartTime;
	private String EndTime;
	private String ExecutionTime;
	private String Passed;
	private String Failed;
	private String Server;
	private String Environment;
	private String Tests;
	private String ModuleStatus;
	private List<TestCaseResult> TestScripts;

	@JsonProperty("Name")
	public String getName() {
		return Name;
	}

	@JsonProperty("Name")
	public void setName(String name) {
		Name = name;
	}

	@JsonProperty("StartTime")
	public String getStartTime() {
		return StartTime;
	}

	@JsonProperty("StartTime")
	public void setStartTime(String startTime) {
		StartTime = startTime;
	}

	@JsonProperty("EndTime")
	public String getEndTime() {
		return EndTime;
	}

	@JsonProperty("EndTime")
	public void setEndTime(String endTime) {
		EndTime = endTime;
	}

	@JsonProperty("ExecutionTime")
	public String getExecutionTime() {
		return ExecutionTime;
	}

	@JsonProperty("ExecutionTime")
	public void setExecutionTime(String executionTime) {
		ExecutionTime = executionTime;
	}

	@JsonProperty("Passed")
	public String getPassed() {
		return Passed;
	}

	@JsonProperty("Passed")
	public void setPassed(String passed) {
		Passed = passed;
	}

	@JsonProperty("Failed")
	public String getFailed() {
		return Failed;
	}

	@JsonProperty("Failed")
	public void setFailed(String failed) {
		Failed = failed;
	}

	@JsonProperty("Server")
	public String getServer() {
		return Server;
	}

	@JsonProperty("Server")
	public void setServer(String server) {
		Server = server;
	}

	@JsonProperty("Environment")
	public String getEnvironment() {
		return Environment;
	}

	@JsonProperty("Environment")
	public void setEnvironment(String environment) {
		Environment = environment;
	}

	@JsonProperty("Tests")
	public String getTests() {
		return Tests;
	}

	@JsonProperty("Tests")
	public void setTests(String tests) {
		Tests = tests;
	}

	@JsonProperty("Status")
	public void setStatus(String status) {
		ModuleStatus = status;
	}

	@JsonProperty("Status")
	public String getStatus() {
		return ModuleStatus;
	}

	@JsonProperty("TestScripts")
	public List<TestCaseResult> getTestScripts() {
		return TestScripts;
	}

	@JsonProperty("TestScripts")
	public void setTestScripts(List<TestCaseResult> testScripts) {
		TestScripts = testScripts;
	}
}
