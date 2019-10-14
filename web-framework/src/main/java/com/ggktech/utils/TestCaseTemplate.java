package com.ggktech.utils;

import javax.xml.transform.TransformerException;
import org.apache.log4j.Logger;
import org.openqa.selenium.remote.DesiredCapabilities;
import org.testng.annotations.AfterTest;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Parameters;
import org.testng.annotations.Test;
import com.ggktech.applib.ApplicationLibrary;

public abstract class TestCaseTemplate {
	protected Logger LOGGER;
	protected String scriptName;
	protected ApplicationLibrary appLib;
	protected String browserType, browserVersion, os, osVersion, sheetName, testEnv;

	/**
	 * Consutructor
	 */
	public TestCaseTemplate() 
	{
		appLib = new ApplicationLibrary();
		LOGGER = Logger.getLogger(this.getClass().getName());
		scriptName = this.getClass().getSimpleName().trim();
	}

	/**
	 * This method need to be implemented to write script
	 */
	@Test
	public abstract void testScript();

	/**
	 * This method executes Before each script, invokes browser and initializes all
	 * the variables
	 * 
	 * @param browserType
	 *            : Name of browser
	 * @param browserVersion
	 *            : Version number of browser
	 * @param os
	 *            : Os name of browser
	 * @param osVersion
	 *            : OS version number
	 * @param sheetName
	 *            : sheet Name (or) module Name
	 * @param testEnv
	 *            : test Environment in which the script need to be run
	 */
	@Parameters({ "browserType", "browserVersion", "os", "osVersion", "sheetName", "testEnv", "server" })
	@BeforeTest
	protected final void testSetUp(String browserType, String browserVersion, String os, String osVersion,
			String sheetName, String testEnv, String server) {
		this.browserType = browserType;
		this.browserVersion = browserVersion;
		this.os = os;
		this.osVersion = osVersion;
		this.sheetName = sheetName;
		this.testEnv = testEnv;
		try {
			DesiredCapabilities capabilities = appLib.createDesiredCapabilities(server, browserType, browserVersion, os,
					osVersion, scriptName);
			appLib.invokeBrowser(testEnv, scriptName, server, capabilities);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * This method executes after the script is run and closes the browser
	 */
	@AfterTest
	protected final void testTearDown() {
		/** Generating xml and closing the browser */
		try {
			appLib.createReportAndUpdateExcel(browserType, browserVersion, os, osVersion, testEnv, scriptName,
					sheetName);
		} catch (TransformerException e) {
			appLib.getRep().reportinCatch(e);
		}
		appLib.getDriver().quit();
	}
}
