package com.ggktech.utils;

public class ExecutionResult {
	
	private int tcCount;
	private int tcPassCount;
	private int tcFailCount;
	private String failScript;
	private String startTime;
	
	public String getStartTime() {
		return startTime;
	}
	public void setStartTime(String startTime) {
		this.startTime = startTime;
	}
	public int getTcCount() {
		return tcCount;
	}
	public void setTcCount(int tcCount) {
		this.tcCount = tcCount;
	}
	public int getTcPassCount() {
		return tcPassCount;
	}
	public void setTcPassCount(int tcPassCount) {
		this.tcPassCount = tcPassCount;
	}
	public int getTcFailCount() {
		return tcFailCount;
	}
	public void setTcFailCount(int tcFailCount) {
		this.tcFailCount = tcFailCount;
	}
	public String getFailScript() {
		return failScript;
	}
	public void setFailScript(String failScript) {
		this.failScript = failScript;
	}

}
