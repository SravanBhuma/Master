package com.ggktech.resultobjects;

import java.util.List;

import org.codehaus.jackson.annotate.JsonProperty;

public class Modules {
	
	private String projectName;
	private List<Module> modules;
	private PieChart pie;
	
	@JsonProperty("ProjectName")
	public String getProjectName() {
		return projectName;
	}
	@JsonProperty("ProjectName")
	public void setProjectName(String projectName) {
		this.projectName = projectName;
	}
	
	@JsonProperty("Modules")
	public List<Module> getModules() {
		return modules;
	}
	@JsonProperty("Modules")
	public void setModules(List<Module> modules) {
		this.modules = modules;
	}
	
	@JsonProperty("PieChart")
	public PieChart getPie() {
		return pie;
	}
	@JsonProperty("PieChart")
	public void setPie(PieChart pie) {
		this.pie = pie;
	}
	
	
	
}
