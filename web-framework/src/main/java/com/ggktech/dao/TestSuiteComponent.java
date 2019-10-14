package com.ggktech.dao;

import java.util.ArrayList;
import java.util.List;

public class TestSuiteComponent {

	private String moduleName;

	private String server;

	private String parallelMode;

	private String exeStartTime;

	private String testEnv;

	private String toEmailAddress;

	public String getToEmailAddress() {
		return toEmailAddress;
	}

	public void setToEmailAddress(String toEmailAddress) {
		this.toEmailAddress = toEmailAddress;
	}

	public String getTestEnv() {
		return testEnv;
	}

	public void setTestEnv(String testEnv) {
		this.testEnv = testEnv;
	}

	private List<String> browsers = new ArrayList<>();

	public String getModuleName() {
		return moduleName;
	}

	public void setModuleName(String moduleName) {
		this.moduleName = moduleName;
	}

	public String getServer() {
		return server;
	}

	public void setServer(String server) {
		this.server = server;
	}

	public List<String> getBrowsers() {
		return browsers;
	}

	public void setBrowsers(List<String> browsers) {
		this.browsers = browsers;
	}

	public String getParallelMode() {
		return parallelMode;
	}

	public void setParallelMode(String parallelMode) {
		this.parallelMode = parallelMode;
	}

	public String getExecutionStartTime() {
		return exeStartTime;
	}

	public void setExecutionStartTime(String exeStartTime) {
		this.exeStartTime = exeStartTime;
	}

}
