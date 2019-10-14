package com.ggktech.service;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.StringWriter;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.CopyOption;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.regex.PatternSyntaxException;

import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.codehaus.jackson.JsonGenerationException;
import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.map.ObjectMapper;
import org.openqa.selenium.By;
import org.openqa.selenium.Dimension;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.Keys;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.StaleElementReferenceException;
import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.edge.EdgeDriver;
import org.openqa.selenium.firefox.FirefoxBinary;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxOptions;
import org.openqa.selenium.firefox.FirefoxProfile;
import org.openqa.selenium.ie.InternetExplorerDriver;
import org.openqa.selenium.ie.InternetExplorerOptions;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.logging.LogEntries;
import org.openqa.selenium.logging.LogEntry;
import org.openqa.selenium.logging.LogType;
import org.openqa.selenium.opera.OperaDriver;
import org.openqa.selenium.opera.OperaOptions;
import org.openqa.selenium.remote.DesiredCapabilities;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.openqa.selenium.safari.SafariDriver;
import org.openqa.selenium.support.ui.ExpectedCondition;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.FluentWait;
import org.openqa.selenium.support.ui.Select;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.testng.Assert;

import com.ggktech.dao.ExcelDataHandler;
import com.ggktech.dao.PropertiesFileReader;
import com.ggktech.resultobjects.TestCaseResult;
import com.ggktech.resultobjects.TestStep;
import com.ggktech.utils.ConfigConstants;
import com.ggktech.utils.DateUtils;
import com.ggktech.utils.EncryptData;
import com.ggktech.utils.ExecutionResult;
import com.ggktech.utils.FailedTests;
import com.ggktech.utils.JQuerySelector;
import com.ggktech.utils.LinkStatusCheck;
import com.ggktech.utils.MiscConstants;
import com.ggktech.utils.PropertyFileConstants;
import com.ggktech.utils.Reporter;
import com.ggktech.utils.Screenshot;
import com.ggktech.utils.SheetConstants;
import com.ggktech.utils.Status;

/**
 * Class that contains the methods that can be used across different projects.
 */
public class PublicLibrary {

	private static final Logger LOGGER = Logger.getLogger(PublicLibrary.class.getName());
	public static final String LINE_SEPARATOR = System.getProperty("line.separator");
	public static ConcurrentMap<String, String> configValues = new ConcurrentHashMap<String, String>();
	public static CopyOnWriteArrayList<String> linkStatus = new CopyOnWriteArrayList<String>();
	public static ConcurrentHashMap<String, ExecutionResult> executionResultMap = new ConcurrentHashMap<String, ExecutionResult>();
	private PropertiesFileReader propFileReader;
	private Properties configProperties;
	private ExcelDataHandler excelSheetReadWrite;
	private RemoteWebDriver driver;
	private Reporter rep;
	private String remoteUrl;
	private Sheet sheet;
	private String moduleWiseReport;
	private String scriptName;
	ConcurrentHashMap<String, String> moduleLevelScripts = new ConcurrentHashMap<String, String>();

	/**
	 * Constructor
	 */
	public PublicLibrary() {
		try {
			rep = new Reporter();
			propFileReader = PropertiesFileReader.getInstance();
			configProperties = propFileReader.getPropFile(PropertyFileConstants.CONFIG_PROPERTIES);
			excelSheetReadWrite = new ExcelDataHandler();
			sheet = ExcelDataHandler.getSheetData(SheetConstants.TEST_SUITE_SHEET,
					getPropertyValue(PropertyFileConstants.CONFIG_PROPERTIES, "filePathTestSuite"));
			moduleWiseReport = sheet.getRow(ConfigConstants.MODULE_WISE_REPORT_ROW_INDEX)
					.getCell(ConfigConstants.MODULE_WISE_REPORT_COLUMN_INDEX).getStringCellValue();
			String rUrl = sheet.getRow(ConfigConstants.REMOTE_URL_ROW_INDEX)
					.getCell(ConfigConstants.REMOTE_URL_COLUMN_INDEX).getStringCellValue();
			String browserUserName = sheet.getRow(ConfigConstants.REMOTE_USER_NAME_ROW_INDEX)
					.getCell(ConfigConstants.REMOTE_USER_NAME_COLUMN_INDEX).getStringCellValue();
			String userAutomateKey = sheet.getRow(ConfigConstants.REMOTE_USER_KEY_ROW_INDEX)
					.getCell(ConfigConstants.REMOTE_USER_KEY_COLUMN_INDEX).getStringCellValue();
			remoteUrl = "https://" + browserUserName.trim() + ":" + userAutomateKey.trim() + rUrl;
		} catch (Exception e) {
			LOGGER.error("Exception while initiating properties in Public Library" + e);
		}
	}

	public RemoteWebDriver getDriver() {
		return driver;
	}

	public void setDriver(RemoteWebDriver driver) {
		this.driver = driver;
	}

	public ExcelDataHandler getExcelSheetReadWrite() {
		return excelSheetReadWrite;
	}

	public void setExcelSheetReadWrite(ExcelDataHandler excelSheetReadWrite) {
		this.excelSheetReadWrite = excelSheetReadWrite;
	}

	public Properties getConfigProperties() {
		return configProperties;
	}

	public PropertiesFileReader getPropFileReader() {
		return propFileReader;
	}

	public void setPropFileReader(PropertiesFileReader propFileReader) {
		this.propFileReader = propFileReader;
	}

	public Reporter getRep() {
		return rep;
	}

	public void setRep(Reporter rep) {
		this.rep = rep;
	}

	/**
	 * Method will return locator value
	 * 
	 * @param keyValue
	 *            : value from property file
	 * @param val
	 *            : value in term locator using which split is done
	 * @return sSplPrVal : string value in locator after splitting
	 */
	public String propVal(String keyValue, String val) {

		String[] sPrVal = null;
		String sSplPrVal = null;
		try {
			sPrVal = keyValue.split(val);
			if (sPrVal.length > 1 && sPrVal[1].endsWith(")")) {
				sSplPrVal = sPrVal[1].substring(0, sPrVal[1].length() - ")".length());
			}
		} catch (PatternSyntaxException | IndexOutOfBoundsException e) {
			LOGGER.error("Exception in propVal()" + e);
		}
		return sSplPrVal;
	}

	/**
	 * Method returns By value of element for the given key from the given property
	 * file
	 * 
	 * @param key
	 *            : key for getting the value from mentioned property file
	 * @param sORFileName
	 *            : Property file name from which the data is being read
	 * @return By : by value to recognize the element
	 */
	public By byLocator(String key, String sORFileName) {
		By by = null;
		String sPropVal = null;
		try {
			String keyValue = getPropertyValue(sORFileName, key);
			if (keyValue.startsWith("By.xpath(")) {
				sPropVal = propVal(keyValue, ConfigConstants.XPATH);
				by = By.xpath(sPropVal);
			} else if (keyValue.startsWith("By.id(")) {
				sPropVal = propVal(keyValue, ConfigConstants.ID);
				by = By.id(sPropVal);
			} else if (keyValue.startsWith("By.name(")) {
				sPropVal = propVal(keyValue, ConfigConstants.NAME);
				by = By.name(sPropVal);
			} else if (keyValue.startsWith("By.linkText(")) {
				sPropVal = propVal(keyValue, ConfigConstants.LINK_TEXT);
				by = By.linkText(sPropVal);
			} else if (keyValue.startsWith("By.className(")) {
				sPropVal = propVal(keyValue, ConfigConstants.CLASS_NAME);
				by = By.className(sPropVal);
			} else if (keyValue.startsWith("By.cssSelector(")) {
				sPropVal = propVal(keyValue, ConfigConstants.CSS_SELECTOR);
				by = By.cssSelector(sPropVal);
			} else if (keyValue.startsWith("By.tagName(")) {
				sPropVal = propVal(keyValue, ConfigConstants.TAG_NAME);
				by = By.tagName(sPropVal);
			} else if (keyValue.startsWith("By.partialLinkText(")) {
				sPropVal = propVal(keyValue, ConfigConstants.PARTIAL_LINK_TEXT);
				by = By.partialLinkText(sPropVal);
			} else if (keyValue.startsWith("JQuerySelector.jQuery(")) {
				sPropVal = propVal(keyValue, ConfigConstants.JQUERY);
				by = JQuerySelector.jQuery(sPropVal);
			} else {
				rep.report(Status.FAIL, "Error: Please check the object mentioned in property file", Screenshot.FALSE);
			}
		} catch (Exception e) {
			LOGGER.error("Exception in byLocator()" + e);
			rep.reportinCatch(e);
		}

		return by;
	}

	/**
	 * Getting value from properties file.
	 * 
	 * @param sPropFileName
	 *            : Properties file to be read.
	 * @param key
	 *            : key of the property.
	 * @return : value of the provided key.
	 */
	public String getPropertyValue(String sPropFileName, String key) {
		Properties props = null;
		try {
			props = propFileReader.getPropFile(sPropFileName);
		} catch (Exception e) {
			LOGGER.error("The mentioned properties file '" + sPropFileName + "' is not found : " + 2);
			getRep().report(Status.FAIL, "Reading Properties file: The mentioned properties file '" + sPropFileName
					+ "' is not found : " + e, Screenshot.FALSE);
			return null;
		}
		String value = props.getProperty(key);
		if (value == null) {
			LOGGER.error("Object with key :  '" + key + "' not found in " + sPropFileName + " file : ");
			getRep().report(Status.FAIL, "Object with key :  '" + key + "' not found in " + sPropFileName + " file : ",
					Screenshot.FALSE);
			return null;
		}
		return value.trim();
	}

	/**
	 * Method will launch browser with specified capabilities and URL
	 * 
	 * @param sUrl
	 *            : url of the web applications
	 * @param scriptName
	 *            : testscript name
	 * @param browserType
	 *            : browser on which the script is going to be run
	 * @param browserVersion
	 *            : browser version
	 * @param os
	 *            : operating system name where the script should run(windows,
	 *            linux, etc,.)
	 * @param osVersion
	 *            : version of the OS
	 * @param server
	 *            : to specify whether local browser or remote browser should invoke
	 * @throws Exception
	 * @throws IOException
	 */
	public void invokeBrowser(String testEnv, String scriptName, String server, DesiredCapabilities capabilities)
			throws Exception {
		try {
			LOGGER.info(scriptName + " is started.");
			sheet = ExcelDataHandler.getSheetData(SheetConstants.TEST_SUITE_SHEET,
					getPropertyValue(PropertyFileConstants.CONFIG_PROPERTIES, "filePathTestSuite"));
			this.scriptName = scriptName;
			switch (server.toUpperCase()) {
			case ConfigConstants.CLOUD:
				createRemoteDriver(remoteUrl, capabilities);
				break;
			case ConfigConstants.GRID:
				createRemoteDriver(sheet.getRow(ConfigConstants.HUB_URL_ROW_INDEX)
						.getCell(ConfigConstants.HUB_URL_COLUMN_INDEX).getStringCellValue(), capabilities);
				break;
			default:
				createLocalDriver(capabilities.getCapability("browser").toString(), capabilities);
				break;
			}
			if (driver != null) {
				rep.setServer(server);
				rep.setDriver(driver);
				rep.stepCounter = 0;
				rep.setResultObject(new TestCaseResult());
				rep.getResultObject().setTestSteps(new ArrayList<TestStep>());
				rep.getResultObject().setName(scriptName);
				rep.getResultObject().setServer(StringUtils.capitalize(server.toLowerCase()));
				rep.getResultObject().setTestEnv(testEnv);
				rep.getResultObject().setStartTime(DateUtils.getCurrentDate());

				//rep.report(Status.PASS, "Test Script Name : " + scriptName, Screenshot.FALSE);
				String appURL = getPropertyValue(PropertyFileConstants.CONFIG_PROPERTIES, testEnv.toUpperCase());

				driver.get(appURL);
				delay(MiscConstants.S_DELAY);
				driver.manage().window().maximize();
				rep.setsScreenName(scriptName + "-" + capabilities.getCapability("browser").toString().substring(0, 3)
						+ capabilities.getCapability("browser_version").toString()
						+ capabilities.getCapability("os").toString().substring(0, 3)
						+ capabilities.getCapability("os_version").toString() + "_");
				rep.report(Status.PASS, "Invoked browser : " + capabilities.getCapability("browser").toString(),
						Screenshot.FALSE);
			} else {
				LOGGER.error("driver is null");
				rep.report(Status.FAIL, "Driver intialization : driver is null", Screenshot.FALSE);
			}
		} catch (Exception e) {
			LOGGER.error("Exception in invokeBrowser()" + e);
			throw new Exception("Exception in invokeBrowser() method" + e);
		}
	}

	/**
	 * Method will create remote web driver with given capabilities and sets to
	 * public variable
	 * 
	 * @param url
	 *            : url of the webapplication
	 * @param capabilities
	 *            : capabilites set for the browser
	 */
	private void createRemoteDriver(String url, DesiredCapabilities capabilities) {
		try {
			driver = new RemoteWebDriver(new URL(url), capabilities);
		} catch (MalformedURLException e) {
			LOGGER.error("Exception in invokeBrowser()" + e);
		}
	}

	/**
	 * Method will create local web driver with given capabilities and sets to
	 * public variable
	 * 
	 * @param browserType
	 *            : browser name where the script is going to run
	 * @param capabilities
	 *            : capabilities of the browser
	 */
	private void createLocalDriver(String browserType, DesiredCapabilities capabilities) {
		String iePath = ConfigConstants.PARENTFOLDER_PATH
				+ getPropertyValue(PropertyFileConstants.CONFIG_PROPERTIES, "iePath");
		String chromePath = ConfigConstants.PARENTFOLDER_PATH
				+ getPropertyValue(PropertyFileConstants.CONFIG_PROPERTIES, "chromePath");
		String edgePath = ConfigConstants.PARENTFOLDER_PATH
				+ getPropertyValue(PropertyFileConstants.CONFIG_PROPERTIES, "sEdgePath");
		String safaripath = ConfigConstants.PARENTFOLDER_PATH
				+ getPropertyValue(PropertyFileConstants.CONFIG_PROPERTIES, "sSafariPath");
		String firefoxPath = ConfigConstants.PARENTFOLDER_PATH
				+ getPropertyValue(PropertyFileConstants.CONFIG_PROPERTIES, "geckoPath");
		String operaPath = ConfigConstants.PARENTFOLDER_PATH
				+ getPropertyValue(PropertyFileConstants.CONFIG_PROPERTIES, "operaPath");
		String isHeadlessMode = sheet.getRow(ConfigConstants.HEADLESS_EXECUTION_ROW_INDEX)
				.getCell(ConfigConstants.HEADLESS_EXECUTION_COLUMN_INDEX).getStringCellValue();

		if (ConfigConstants.FIREFOX.equalsIgnoreCase(browserType)) {

			System.setProperty("webdriver.gecko.driver", firefoxPath);
			System.setProperty(FirefoxDriver.SystemProperty.DRIVER_USE_MARIONETTE, "true");
			System.setProperty(FirefoxDriver.SystemProperty.BROWSER_LOGFILE, "/dev/null");
			FirefoxProfile ffProfile = new FirefoxProfile();
			ffProfile.setPreference("browser.tabs.remote.autostart.2", false);
			FirefoxOptions options = new FirefoxOptions();
			FirefoxBinary firefoxBinary = new FirefoxBinary();
			if ("y".equalsIgnoreCase(isHeadlessMode)) {
				firefoxBinary.addCommandLineOptions("--headless");
				options.setBinary(firefoxBinary);
			}
			options.setProfile(ffProfile);
			driver = new FirefoxDriver(options);
		} else if (ConfigConstants.CHROME.equalsIgnoreCase(browserType)) {
			System.setProperty("webdriver.chrome.driver", chromePath);
			ChromeOptions chromeOptions = new ChromeOptions();
			if ("y".equalsIgnoreCase(isHeadlessMode)) {
				chromeOptions.addArguments("--headless");
			}
			driver = new ChromeDriver(chromeOptions);
		} else if (ConfigConstants.IE.equalsIgnoreCase(browserType)) {
			System.setProperty("webdriver.ie.driver", iePath);
			driver = new InternetExplorerDriver(new InternetExplorerOptions(capabilities));
		} else if (ConfigConstants.EDGE.equalsIgnoreCase(browserType)) {
			System.setProperty("webdriver.edge.driver", edgePath);
			driver = new EdgeDriver(capabilities);
		} else if (ConfigConstants.SAFARI.equalsIgnoreCase(browserType)) {
			System.setProperty("webdriver.safari.driver", safaripath);
			driver = new SafariDriver();
		} else if (ConfigConstants.OPERA.equalsIgnoreCase(browserType)) {
			System.setProperty("webdriver.opera.driver", operaPath);
			OperaOptions ops = new OperaOptions();
			ops.setBinary("C:\\Program Files\\Opera\\launcher.exe");
			driver = new OperaDriver(ops);
		} else {
			LOGGER.error("Please check browser details" + browserType + "in Excel");
			Assert.fail();
		}
		if (!ConfigConstants.EDGE.equalsIgnoreCase(browserType)) {
			driver.manage().deleteAllCookies();
		}
	}

	/**
	 * Method waits until element becomes visible, if element is not visible in 10
	 * seconds then throws exception
	 * 
	 * @param by
	 *            : Element locator to recognize the element
	 */
	public void waitForElement(By by) {
		try {
			WebDriverWait wait = new WebDriverWait(driver, 10);
			wait.until(ExpectedConditions.visibilityOfElementLocated(by));
			wait.ignoring(NoSuchElementException.class, StaleElementReferenceException.class).pollingEvery(1,
					TimeUnit.SECONDS);
		} catch (Exception e) {
			LOGGER.error("Exception in waitForElement()" + e);
			rep.reportinCatch(e);
		}
	}

	/**
	 * Method checks whether element is enabled and displayed. Fails if any
	 * condition is not satisfied
	 * 
	 * @param byVal
	 *            : Element locator
	 * @param sRepoText
	 *            : report text
	 */

	public void verifyElement(WebElement element, String sRepoText) {
		if (element != null) {
			if (!element.isDisplayed()) {
				rep.report(Status.FAIL, sRepoText + " is not displayed", Screenshot.TRUE);
			}
			if (!element.isEnabled()) {
				rep.report(Status.FAIL, sRepoText + " is not enabled", Screenshot.TRUE);
			}
		} else {
			rep.report(Status.FAIL, sRepoText + " is not exist", Screenshot.TRUE);
		}
	}

	/**
	 * Method sets value to text box after clearing and clicking
	 * 
	 * @param byVal
	 *            : Element locator
	 * @param sValue
	 *            : Value to be be entered in the element
	 * @param iSecs
	 *            : Number of seconds to wait
	 * @param sReportText
	 *            : Report text
	 * @return : webelement used for setting text
	 */
	public WebElement setValue(By byVal, String sValue, int iSecs, String sReportText) {

		WebElement element = null;
		try {
			fluentWait(byVal, iSecs);
			element = verifyElementExist(byVal, sReportText);
			verifyElement(element, sReportText);
			element.click();
			String elementValue = element.getAttribute("value");
			if (!elementValue.isEmpty() && elementValue != null) {
				element.clear();
			}
			element.sendKeys(sValue);
			rep.report(Status.PASS, sReportText + " is set to " + sValue, Screenshot.TRUE);

		} catch (Exception e) {
			LOGGER.error("Exception in setValue()" + e);
			rep.reportinCatch(e);
		}
		return element;
	}

	/**
	 * Method sets password to text box after clearing and clicking
	 * 
	 * @param byVal
	 *            : Element locator
	 * @param encryptedValue
	 *            : value to be decrypted and set to element
	 * @param iSecs
	 *            : Number of seconds to wait
	 * @param sReportText
	 *            : Report text
	 * @return : webelement used for setting text
	 */
	public WebElement setPassword(By byVal, String encryptedValue, int iSecs, String sReportText) {

		WebElement element = null;
		try {
			fluentWait(byVal, iSecs);
			element = verifyElementExist(byVal, sReportText);
			verifyElement(element, sReportText);
			element.click();
			String elementValue = element.getAttribute("value");
			if (!elementValue.isEmpty() && elementValue != null) {
				element.clear();
			}
			element.sendKeys(new EncryptData().getDecryptValue(encryptedValue));
			rep.report(Status.PASS, "Password is entered", Screenshot.TRUE);

		} catch (Exception e) {
			LOGGER.error("Exception in setPassword()" + e);
			rep.reportinCatch(e);
		}
		return element;
	}

	/**
	 * Methods sets value to text box by appending some random value to actual text
	 * after clearing and clicking
	 * 
	 * @param byVal
	 *            : Element locator
	 * @param sValue
	 *            : Value to be be entered in the element
	 * @param iSecs
	 *            : Number of seconds to wait
	 * @param sReportText
	 *            : Report text
	 */
	public WebElement setRandomValue(By byVal, String sValue, int iSecs, String sReportText) {
		WebElement element = null;
		try {
			fluentWait(byVal, iSecs);
			String sVal = sValue + Math.round(Math.random() * 100000);
			element = driver.findElement(byVal);
			element.click();
			if (element.getText() != null) {
				element.clear();
			}
			element.sendKeys(sVal);
			rep.report(Status.PASS, sReportText + " is set to " + sValue, Screenshot.FALSE);
		} catch (Exception e) {
			LOGGER.error("Exception in setRandomvalue()" + e);
			rep.reportinCatch(e);
		}
		return element;
	}

	/**
	 * Method clicks on element for the given By value
	 * 
	 * @param byVal
	 *            : Element locator
	 * @param iSecs
	 *            : Number of seconds to wait
	 * @param sReportText
	 *            : Report tex
	 */
	public void clickElement(By byVal, int iSecs, String sReportText) {
		try {
			fluentWait(byVal, iSecs);
			WebElement element = verifyElementExist(byVal, sReportText);
			verifyElement(element, sReportText);
			new Actions(getDriver()).moveToElement(element).perform();
			element.click();
			rep.report(Status.PASS, sReportText + " is clicked", Screenshot.FALSE);
		} catch (Exception e) {
			LOGGER.error("Exception in clickElement()" + e);
			rep.reportinCatch(e);
		}
	}

	/**
	 * Methods sets value for drop down or select box using given value attribute
	 * 
	 * @param byVal
	 *            : Element locator
	 * @param sValue
	 *            : Value to be be entered in the element
	 * @param iSecs
	 *            : Number of seconds to wait
	 * @param sReportText
	 *            : Report text
	 */
	public WebElement selectValue(By byVal, String sValue, int iSecs, String sReportText) {
		WebElement element = null;
		try {
			fluentWait(byVal, iSecs);
			element = verifyElementExist(byVal, sReportText);
			verifyElement(element, sReportText);
			Select select = new Select(element);
			select.selectByValue(sValue);
			rep.report(Status.PASS, sReportText + " : " + sValue + " is selected ", Screenshot.FALSE);
		} catch (Exception e) {
			LOGGER.error("Exception in selectValue()" + e);
			rep.reportinCatch(e);
		}
		return element;
	}

	/**
	 * Method verifies inner text of an element with expected value
	 * 
	 * @param byVal
	 *            : Element locator
	 * @param sValue
	 *            : Value to be be entered in the element
	 * @param iSecs
	 *            : Number of seconds to wait
	 * @param sReportText
	 *            : Report tex
	 * @param screenshot
	 *            : to specify screenshot is required or not
	 */
	public void verifyText(By byVal, String sVal, String sReportText, int iSecs, Screenshot screenshot) {
		try {
			fluentWait(byVal, iSecs);
			String sWebElVal = driver.findElement(byVal).getText().trim();
			verifyAssertEquals(sWebElVal, sVal, sReportText, screenshot);
		} catch (Exception e) {
			LOGGER.error("Exception in verifyText()" + e);
			rep.reportinCatch(e);
		}
	}

	/**
	 * Method compares actual value with expected value and fails if both are not
	 * matched
	 * 
	 * @param sActual
	 *            : Value actually captured
	 * @param sValue
	 *            : Value to be be entered in the element
	 * @param sReportText
	 *            :Text which needs to be displayed on the Report
	 * @param screenshot
	 *            : to specify screenshot is required or not
	 */
	public void verifyAssertEquals(String sActual, String sValue, String sReportText, Screenshot screenshot) {
		try {
			Assert.assertEquals(sActual, sValue);
			rep.report(Status.PASS, sReportText + " : " + sValue + " is displayed", screenshot);
		} catch (AssertionError | Exception e) {
			rep.report(Status.FAIL,
					sReportText + " : " + "'" + sActual + "'" + " is displayed instead of '" + sValue + "'",
					Screenshot.TRUE);
			LOGGER.error(e);
		}
	}

	/**
	 * Method to select a value from dropdown by index
	 * 
	 * @param byVal
	 *            : By locator of dropdown
	 * @param index
	 *            : index of dropdown selection
	 * @param iSecs
	 *            : waits until this value for an element to visible
	 * @param sReportText
	 *            : text used for reporting about element
	 * @return
	 */
	public WebElement setDropdownByIndex(By byVal, int index, int iSecs, String sReportText) {

		WebElement element = null;
		try {

			fluentWait(byVal, iSecs);
			element = verifyElementExist(byVal, sReportText);
			verifyElement(element, sReportText);
			Select select = new Select(element);
			select.selectByIndex(index);
			rep.report(Status.PASS, sReportText + " : " + index + "option is selected", Screenshot.FALSE);

		} catch (Exception e) {
			LOGGER.error("Exception in setDropdownByIndex()" + e);
			rep.reportinCatch(e);
		}
		return element;
	}

	/**
	 * Methods sets value for drop down or select box using text content
	 * 
	 * @param byVal
	 *            : By locator of dropdown
	 * @param sVal:
	 *            Value to be set for dropdown
	 * @param iSecs
	 *            : waits until this value for an element to visible
	 * @param sReportText
	 *            : text used for reporting about element
	 */
	public WebElement setDropdownValue(By byVal, String sVal, int iSecs, String sReportText) {

		WebElement element = null;
		try {
			fluentWait(byVal, iSecs);
			element = verifyElementExist(byVal, sReportText);
			verifyElement(element, sReportText);
			Select select = new Select(element);
			select.selectByVisibleText(sVal);
			rep.report(Status.PASS, sReportText + " : " + sVal + " is selected", Screenshot.FALSE);
		} catch (Exception e) {
			LOGGER.error("Exception in setDropdownValue()" + e);
			rep.reportinCatch(e);
		}
		return element;
	}

	/**
	 * Method to getText from WebElement
	 * 
	 * @param byVal
	 *            : Element locator
	 * @param iSecs
	 *            :Number of seconds to wait
	 * @param sReportText
	 *            : Report Text
	 * @return sCaptVal : returns captured text
	 */
	public String getText(By byVal, int iSecs, String sReportText) {
		String sCaptVal = null;
		try {
			fluentWait(byVal, iSecs);
			WebElement element = verifyElementExist(byVal, sReportText);
			verifyElement(element, sReportText);
			sCaptVal = element.getText().trim();
			rep.report(Status.PASS, sReportText + " : " + sCaptVal + " is captured", Screenshot.FALSE);
		} catch (Exception e) {
			LOGGER.error("Exception in getText()" + e);
			rep.reportinCatch(e);
		}
		return sCaptVal;

	}

	/**
	 * Method to clear text from given webelement
	 * 
	 * @param byVal
	 *            : Element Locator
	 * @param iSecs
	 *            : Number of seconds to wait
	 * @param sReportText
	 *            : Report text
	 */
	public WebElement clearText(By byVal, int iSecs, String sReportText) {
		WebElement element = null;
		try {
			fluentWait(byVal, iSecs);
			element = verifyElementExist(byVal, sReportText);
			verifyElement(element, sReportText);
			delay(MiscConstants.S_DELAY);
			element.click();
			delay(MiscConstants.S_DELAY);
			element.sendKeys(Keys.CONTROL, "a");
			delay(MiscConstants.M_DELAY);
			element.sendKeys(Keys.DELETE);
			delay(MiscConstants.M_DELAY);
			rep.report(Status.PASS, sReportText + " is cleared", Screenshot.FALSE);
		} catch (Exception e) {
			LOGGER.error("Exception in clearText()" + e);
			rep.reportinCatch(e);
		}
		return element;

	}

	/**
	 * Method to Check or un check checkbox
	 * 
	 * @param byVal
	 *            : Element locator
	 * @param sVal
	 *            : Value to be be entered in the element(YES, NO)
	 * @param sReportText
	 *            : Report Text
	 */
	public WebElement selectCheckBox(By byVal, String sVal, String sReportText) {
		WebElement element = verifyElementExist(byVal, sReportText);
		try {
			verifyElement(element, sReportText);
			if ("YES".equals(sVal.trim())) {
				if (!element.isSelected()) {
					element.click();
					LOGGER.info(sReportText + " is checked");
					rep.report(Status.PASS, sReportText + " is checked", Screenshot.FALSE);
				} else {
					rep.report(Status.PASS, sReportText + " is already checked", Screenshot.FALSE);
				}
			} else if ("NO".equals(sVal.trim())) {
				if (element.isSelected()) {
					element.click();
					rep.report(Status.PASS, sReportText + " is unchecked", Screenshot.FALSE);
				} else {
					rep.report(Status.PASS, sReportText + " is already unchecked", Screenshot.FALSE);
				}
			} else {
				rep.report(Status.FAIL, "Wrong value is passed as parameter for performing action on " + sReportText,
						Screenshot.FALSE);
			}
		} catch (Exception e) {
			LOGGER.error("Exception in selectCheckBox()" + e);
			rep.reportinCatch(e);
		}
		return element;
	}

	/**
	 * Method to get dimensions of table
	 * 
	 * @param byVal
	 *            : Element Locator
	 * @param getVal
	 *            : height or width
	 * @return int sVal : height or width of webTable
	 */
	public int getDimensionOfTable(By byVal, String getVal) {
		int sVal = 0;
		try {
			Dimension mDim = driver.findElement(byVal).getSize();

			if (getVal == "height") {
				sVal = mDim.height;
			} else if (getVal == "width") {
				sVal = mDim.width;
			}
		} catch (Exception e) {
			LOGGER.error("Exception in getDimensionOfTable()" + e);
			rep.reportinCatch(e);
		}
		return sVal;

	}

	/**
	 * Method to get Attribute Value from given locator
	 * 
	 * @param byVal
	 *            : Element locator
	 * @param iSecs
	 *            :Number of seconds to wait
	 * @param attributeValue
	 *            : Attribute of the element
	 * @return capturedAttributeValue : Captured value of the attribute
	 */
	public String getAttributeElement(By byVal, int iSecs, String attributeValue) {
		String capturedAttributeValue = null;
		try {
			fluentWait(byVal, iSecs);
			capturedAttributeValue = driver.findElement(byVal).getAttribute(attributeValue);
		} catch (Exception e) {
			LOGGER.error("Exception in getAttibuteElement()" + e);
			rep.reportinCatch(e);
		}
		return capturedAttributeValue;

	}

	/**
	 * Method to check given text is exist in web table
	 * 
	 * @param byVal
	 *            : Element locator
	 * @param sTxt
	 *            : WebTable header name
	 * @param iSecs
	 *            :Number of seconds to wait
	 * @param sVal
	 *            : Data to be checked in the table
	 * @param sReportText
	 *            : Report text
	 */
	public void getCellTextfromTable(By byVal, String sTxt, int iSecs, String sVal, String sReportText) {

		int iCounter;
		try {
			WebElement table = driver.findElement(byVal);
			iCounter = 0;
			fluentWait(byVal, iSecs);
			List<WebElement> th = table.findElements(By.tagName("th"));
			int colPosition = 0;
			for (int i = 0; i < th.size(); i++) {
				if (sTxt.equalsIgnoreCase(th.get(i).getText())) {
					colPosition = i + 1;
					break;
				}
			}
			List<WebElement> firstColumns = table.findElements(By.xpath("//tr//td[" + colPosition + "]"));

			for (WebElement e : firstColumns) {
				if (e.getText().contains(sVal)) {
					rep.report(Status.PASS, sReportText + " : " + sVal + " in table is present", Screenshot.FALSE);
					iCounter = 1;
					break;
				}
			}
			if (iCounter == 0) {
				rep.report(Status.FAIL, sReportText + " : " + sVal + " in table is not displayed", Screenshot.FALSE);
			}
		} catch (Exception e) {
			LOGGER.error("Exception in getCellTextFromTable()" + e);
			rep.reportinCatch(e);
		}
	}

	/**
	 * Method to click on given link
	 * 
	 * @param sHref
	 *            : href of web element
	 * @param sReportText
	 *            : Report text
	 */
	public void clickLinkByHref(String sHref, String sReportText) {

		try {
			List<WebElement> anchors = driver.findElements(By.tagName("a"));
			Iterator<WebElement> i = anchors.iterator();
			int flg = 0;
			while (i.hasNext()) {
				WebElement anchor = i.next();
				if (anchor.getAttribute("href").contains(sHref)) {
					anchor.click();
					rep.report(Status.PASS, sReportText + " is clicked using href", Screenshot.FALSE);
					flg = 1;
					break;
				}
			}
			if (flg == 0) {
				rep.report(Status.FAIL, sReportText + " is not displayed", Screenshot.TRUE);
			}
		} catch (Exception e) {
			LOGGER.error("Exception in clickLinkByHref()" + e);
			rep.reportinCatch(e);
		}
	}

	/**
	 * Method to get text from web element
	 * 
	 * @param byVal
	 *            : Element locator
	 * @param iSecs
	 *            : Number of seconds to wait
	 * @param sReportText
	 *            : Report Text
	 * @return String content : content of selected value of webelement
	 */
	public String getSelectedtext(By byVal, int iSecs, String sReportText) {
		String content = null;
		try {
			fluentWait(byVal, iSecs);
			WebElement element = verifyElementExist(byVal, sReportText);
			verifyElement(element, sReportText);
			Select se = new Select(element);
			WebElement option = se.getFirstSelectedOption();
			content = option.getText().trim();
			if (content == null) {
				rep.report(Status.FAIL, "No value is captured from " + sReportText, Screenshot.TRUE);
			} else {
				rep.report(Status.PASS, content + " is captured from " + sReportText, Screenshot.FALSE);
			}
		} catch (Exception e) {
			LOGGER.error("Exception in getSelectedtext()" + e);
			rep.reportinCatch(e);
		}
		return content;

	}

	/**
	 * Method perform Keyboard actions like Down, Enter, Space, BackSpace, Shift,
	 * Control, Alt, Home, End,Insert, Delete, Page_up, Page_down
	 * 
	 * @param byVal
	 *            : Element locator
	 * @param sVal
	 *            : Value to be be entered in the element
	 * @param sReportText
	 *            : Report text
	 */
	public void keyPress(By byVal, String sVal, String sReportText) {
		WebElement webEl = verifyElementExist(byVal, sReportText);
		try {
			verifyElement(webEl, sReportText);
			webEl.click();
			delay(MiscConstants.S_DELAY);
			switch (sVal) {
			case "Down":
				webEl.sendKeys(Keys.DOWN);
				break;
			case "Enter":
				webEl.sendKeys(Keys.ENTER);
				break;
			case "Space":
				webEl.sendKeys(Keys.SPACE);
				break;
			case "BackSpace":
				webEl.sendKeys(Keys.BACK_SPACE);
				break;
			case "Shift":
				webEl.sendKeys(Keys.SHIFT);
				break;
			case "Control":
				webEl.sendKeys(Keys.CONTROL);
				break;
			case "Alt":
				webEl.sendKeys(Keys.ALT);
				break;
			case "Home":
				webEl.sendKeys(Keys.HOME);
				break;
			case "End":
				webEl.sendKeys(Keys.END);
				break;
			case "Insert":
				webEl.sendKeys(Keys.INSERT);
				break;
			case "Delete":
				webEl.sendKeys(Keys.DELETE);
				break;
			case "PageUp":
				webEl.sendKeys(Keys.PAGE_UP);
				break;
			case "PageDown":
				webEl.sendKeys(Keys.PAGE_DOWN);
				break;
			default:
				break;
			}
			delay(MiscConstants.S_DELAY);
			rep.report(Status.PASS, sReportText + " is set to " + sVal, Screenshot.FALSE);
		} catch (Exception e) {
			LOGGER.error("Exception in keyPress()" + e);
			rep.reportinCatch(e);
		}
	}

	/**
	 * Method to verify whether element present on page
	 * 
	 * @param by
	 *            : Element Locator
	 * @param iSecs
	 *            : Number of seconds to wait
	 * @return boolean isPresent : true or false
	 */
	public boolean isElementPresent(By by, int iSecs) {
		boolean isPresent = true;
		try {
			if (driver.findElements(by).isEmpty()) {
				isPresent = false;
			}
		} catch (Exception e) {
			LOGGER.error("Exception in isElementPresent()" + e);
			rep.reportinCatch(e);
		}
		return isPresent;
	}

	/**
	 * Method to close driver
	 */
	public void tearDown() {
		try {
			driver.close();
			driver.quit();
		} catch (Exception e) {
			LOGGER.error("Exception in tearDown()" + e);
			rep.reportinCatch(e);
		}
	}

	/**
	 * Method to write browserComponent details as header in input excel and also
	 * update the execution result
	 * 
	 * @param sSheetName
	 *            : name of the excel sheet
	 * @param sClassName
	 *            : Script Name
	 * @param browserComponent
	 *            : Environment i.e. browser,browser version, OS ,osVersion
	 * @param status
	 *            : pass or fail
	 */
	public void updateResults(String sSheetName, String sClassName, String browserComponent, String status) {
		Sheet sheet;
		Cell cell;
		Row row;
		try {

			sheet = ExcelDataHandler.getSheetData(sSheetName,
					getPropertyValue(PropertyFileConstants.CONFIG_PROPERTIES, "filePathTestSuite"));
			for (int i = 1; i <= sheet.getLastRowNum(); i++) {
				row = sheet.getRow(i);
				cell = row.getCell(ConfigConstants.SCRIPT_NAME_COLUMN_INDEX);
				if (cell.getStringCellValue().equals(sClassName)) {
					cell = excelSheetReadWrite.getCellFromRow(row, SheetConstants.RUNSTATUS_TESTSUITE, sheet);
					if ("y".equalsIgnoreCase(cell.getStringCellValue())) {
						Row headerRow = sheet.getRow(ConfigConstants.HEADER_ROW_INDEX);
						int columnIndex = headerRow.getPhysicalNumberOfCells();
						Boolean isColumnPresent = true;
						Iterator<Cell> cellIterator = headerRow.cellIterator();
						while (cellIterator.hasNext()) {
							Cell headerCell = cellIterator.next();
							if (browserComponent.equalsIgnoreCase(headerCell.getStringCellValue())) {
								columnIndex = headerCell.getColumnIndex();
								if (row.getCell(columnIndex) == null) {
									isColumnPresent = true;
									break;
								} else {
									String cellValue = row.getCell(columnIndex).getStringCellValue();
									if ("Pass".equals(cellValue) || "Fail".equals(cellValue)) {
										isColumnPresent = false;
									}
								}

							}
						}

						if (!isColumnPresent) {
							columnIndex = headerRow.getLastCellNum();
							ExcelDataHandler.setSheetData(sSheetName, ConfigConstants.HEADER_ROW_INDEX,
									headerRow.getLastCellNum(), browserComponent,
									getPropertyValue(PropertyFileConstants.CONFIG_PROPERTIES, "filePathTestSuite"));
						}
						ExcelDataHandler.setSheetData(sSheetName, i, columnIndex, status,
								getPropertyValue(PropertyFileConstants.CONFIG_PROPERTIES, "filePathTestSuite"));
						break;
					}
				}
			}
		} catch (Exception e) {
			LOGGER.error("Exception in updateResults()" + e);
		}
	}

	/**
	 * Method to wait for given amount of time, if Parameter is >0 then it will be
	 * wait for given time by using hard wait (thread.sleep), in other case it will
	 * be implicit wait by taking time from config properties
	 * 
	 * @param iSecs
	 *            : Number of seconds to wait
	 */
	public void delay(int iSecs) {
		try {
			if (iSecs == 0) {
				driver.manage().timeouts().implicitlyWait(
						Integer.parseInt(getPropertyValue(PropertyFileConstants.CONFIG_PROPERTIES, "secsToWait")),
						TimeUnit.SECONDS);
			}
			Thread.sleep(iSecs);
		} catch (Exception e) {
			LOGGER.error("Exception in delay()" + e);
			rep.reportinCatch(e);
		}
	}

	/**
	 * Method for Fluentwait, Ignore NoSuchElementException and
	 * StaleElementReferenceException
	 * 
	 * @param by
	 *            : Element locator
	 * @param iSecs
	 *            : Number of seconds to wait
	 */
	public void fluentWait(final By locator, int time) {
		try {
			new FluentWait<WebDriver>(getDriver()).withTimeout(time, TimeUnit.SECONDS)
					.pollingEvery(100, TimeUnit.MILLISECONDS).ignoring(NoSuchElementException.class)
					.ignoring(StaleElementReferenceException.class).until(new ExpectedCondition<WebElement>() {
						public WebElement apply(WebDriver driver) {
							return driver.findElement(locator);
						}
					});
			waitForElement(locator);
		} catch (TimeoutException e) {
			LOGGER.error("Exception in fluentWait()" + e);
			rep.report(Status.FAIL, "Element locator '" + locator.toString() + "' did not match any elements after "
					+ time + " seconds.", Screenshot.TRUE);
		} catch (Exception e) {
			LOGGER.error("Exception in fluentWait()" + e);
			rep.reportinCatch(e);
		}
	}

	/**
	 * 
	 * Method to verify element present on Page, verify whether duplicate elements
	 * exists and reports the passed text
	 * 
	 * @param byVal
	 *            : Element locator
	 * @param elementName
	 *            : Name of the element to be verified
	 * @return : Element
	 */
	public WebElement verifyElementExist(By byVal, String elementName) {
		WebElement element = null;
		try {
			List<WebElement> allElements = getDriver().findElements(byVal);
			int size = allElements.size();
			if (size != 0) {
				if (size == 1) {
					element = allElements.get(0);
				} else {
					getRep().report(Status.FAIL, "Found duplicate elements", Screenshot.TRUE);
				}
			} else {
				getRep().report(Status.FAIL, elementName + "is not exist", Screenshot.TRUE);
			}
		} catch (Exception e) {
			LOGGER.error("Exception in verifyElementExist()" + e);
			getRep().report(Status.FAIL, "Element is not present", Screenshot.TRUE);
		}
		return element;
	}

	/**
	 * Method to verify Page Title,Page Load Time, Broken links and java script
	 * errors in page based on excel configurations
	 * 
	 * @param sValue
	 *            : Value to be be entered in the element
	 * @param sReportText
	 *            : Text which needs to be displayed on the Report
	 */
	public void verifyPage(String sValue, String sReportText) {

		String sActual = driver.getTitle().trim();
		verifyAssertEquals(sActual, sValue, sReportText, Screenshot.FALSE);
		verifyPage(sReportText);
	}

	/**
	 * Method to verify Page Load Time, Broken links and java script errors in page
	 * based on excel configurations
	 * 
	 * @param sReportText
	 *            : Text which needs to be displayed on the Report
	 */
	public void verifyPage(String sReportText) {
		int count = 0;
		int brokenLinkCount = 0;
		try {
			if (("y").equalsIgnoreCase(sheet.getRow(ConfigConstants.PAGE_LOAD_TIME_ROW_INDEX)
					.getCell(ConfigConstants.PAGE_LOAD_TIME_COLUMN_INDEX).getStringCellValue())) {
				pageLoadTime();
			}
			if (("y").equalsIgnoreCase(sheet.getRow(ConfigConstants.REPORT_JAVA_SCRIPT_ERRORS_ROW_INDEX)
					.getCell(ConfigConstants.REPORT_JAVA_SCRIPT_ERRORS_COLUMN_INDEX).getStringCellValue())) {
				javaScriptErrors(sReportText);
			}

			if (("y").equalsIgnoreCase(sheet.getRow(ConfigConstants.REPORT_BROKEN_LINKS_ROW_INDEX)
					.getCell(ConfigConstants.REPORT_BROKEN_LINKS_COLUMN_INDEX).getStringCellValue())) {
				findBrokenLinks(scriptName);
				BufferedWriter bw = getBufferedWriter(
						getPropertyValue(PropertyFileConstants.CONFIG_PROPERTIES, "consolidatedResultPath")
								+ "\\linkStatuses.txt");
				bw.write(LINE_SEPARATOR);
				bw.write(
						"Script Name: " + scriptName + " || Browser : " + getDriver().getCapabilities().getBrowserName()
								+ " || Page Name: " + driver.getTitle().trim() + LINE_SEPARATOR);
				bw.write(LINE_SEPARATOR);
				for (String link : linkStatus) {
					if (link.contains(scriptName) && link.contains(getDriver().getCapabilities().getBrowserName())) {
						if (link.contains("Malformed")) {
							count = count + 1;
						}
						bw.write(link + LINE_SEPARATOR);
						linkStatus.remove(linkStatus.indexOf(link));
					}
					if (link.contains("||| 404 |||") || link.contains("||| 500 |||")) {
						brokenLinkCount = brokenLinkCount + 1;
						if (brokenLinkCount == 1) {
							getRep().report(Status.WARNING, "Broken Links are present in the page", Screenshot.FALSE);
						}
						getRep().report(Status.WARNING, "BrokenLink" + link, Screenshot.FALSE);
					}
				}
				if (brokenLinkCount == 0) {
					getRep().report(Status.PASS, "Broken Links are not present in the page", Screenshot.FALSE);
				}
				if (count > 0) {
					getRep().report(Status.WARNING, "Number of Malformed urls present in the page : " + count,
							Screenshot.FALSE);
				}
				bw.close();
			}
		} catch (Exception e) {
			LOGGER.error("Exception in verifyPage()" + e);
			rep.reportinCatch(e);
		}
	}

	/**
	 * Method is to get all .java files from given directory.
	 * 
	 * @param directoryName
	 *            : Name of the directory
	 * 
	 * @return list of all files which contains .java in given directory
	 */
	public static List<File> listScripts(String directoryName) {
		List<File> resultList = new ArrayList<File>();
		File directory = new File(directoryName);
		if (!(directory.isHidden())) {
			File[] fList = directory.listFiles();
			for (File file : fList) {
				if (file.isDirectory()) {
					resultList.addAll(listScripts(file.getAbsolutePath()));
				} else if ((!file.isHidden()) && file.getName().contains(".java")) {
					resultList.add(file);
				}
			}
		}
		return resultList;
	}

	/**
	 * Method to verify javaScriptErrors in a page
	 * 
	 * @param sReportText
	 *            : Reporting text
	 */
	private void javaScriptErrors(String sReportText) {
		try {
			LogEntries logEntries = driver.manage().logs().get(LogType.BROWSER);

			String[] errorTypeList = { "EvalError", "RangeError", "ReferenceError", "SyntaxError", "TypeError",
					"URIError", "OtherError" };

			for (LogEntry entry : logEntries) {
				for (String errorType : errorTypeList) {
					if (entry.getLevel().getName().contains("SEVERE") && entry.getMessage().contains(errorType)) {
						getRep().report(Status.WARNING, "Javascript Error in " + sReportText + entry.getMessage(),
								Screenshot.FALSE);
						break;
					}
				}
			}

		} catch (Exception e) {
			rep.reportinCatch(e);
			LOGGER.error("Exception in JavaSCriptErrors() method" + e);
		}
	}

	/**
	 * This method to write data to a file in given path
	 * 
	 * @param path
	 *            : Path of the file
	 * 
	 * @return bufferwriter
	 */
	public BufferedWriter getBufferedWriter(String path) {
		BufferedWriter bw = null;
		try {
			File file = new File(ConfigConstants.PARENTFOLDER_PATH + path);
			if (!file.exists()) {
				file.createNewFile();
			}
			FileWriter fw = new FileWriter(file.getAbsoluteFile(), true);
			bw = new BufferedWriter(fw);
		} catch (Exception e) {
			rep.report(Status.FAIL, "Write To textFile is Failed", Screenshot.FALSE);
			LOGGER.error("Exception in getBufferedWriter()" + e);
		}
		return bw;
	}

	/**
	 * This method is to find broken links in a page
	 * 
	 * @param scriptName
	 *            : Name of the script
	 */
	private void findBrokenLinks(String scriptName) {
		try {
			if (configValues.size() == 0) {
				configValues.put("user-agent", ConfigConstants.USERAGENT);
				configValues.put("statuses",
						getPropertyValue(PropertyFileConstants.CONFIG_PROPERTIES, "ignoreStatuses"));
			}
			ThreadPoolExecutor executor = (ThreadPoolExecutor) Executors.newCachedThreadPool();
			List<WebElement> links = getDriver().findElements(By.tagName("a"));
			for (int i = 0; i < links.size(); i++) {
				LinkStatusCheck zz = new LinkStatusCheck(links.get(i).getAttribute("href"), links.get(i).getText(),
						getDriver().getCapabilities().getBrowserName(), scriptName);
				executor.execute(zz);
			}
			links.clear();
			executor.shutdown();
			while (!executor.isTerminated()) {
				if (executor.getActiveCount() == 0) {
					break;
				}
			}
		} catch (Exception e) {
			getRep().report(Status.WARNING, "Exception in findBrokenLinks()" + e, Screenshot.FALSE);
		}
	}

	/**
	 * This method to capture page load time of a page
	 */
	private void pageLoadTime() {

		Long pageLoadTime = (Long) ((JavascriptExecutor) getDriver()).executeScript(
				"var performance1 = window.performance.timing.loadEventEnd-window.performance.timing.navigationStart || {};"
						+ "return performance1;");
		int timeout = Integer.parseInt(getPropertyValue(PropertyFileConstants.CONFIG_PROPERTIES, "sPageTimeOut"));
		if (pageLoadTime / 1000 > timeout) {
			getRep().report(Status.WARNING,
					getDriver().getTitle() + " : Page is taking more than " + timeout + " seconds to load",
					Screenshot.FALSE);
		} else {
			getRep().report(Status.PASS,
					getDriver().getTitle() + " : Page Load Time is : " + pageLoadTime / 1000 + "secs",
					Screenshot.FALSE);
		}

	}

	/**
	 * Method to create execution result XML for each script individually
	 * 
	 * @param browserType
	 *            : Browser name
	 * @param browserVersion
	 *            : Browser version
	 * @param os
	 *            : Windows or Linux
	 * @param osVersion
	 *            : OS Version
	 * @param scriptName
	 *            : TestScript name
	 * @param sheetName
	 *            : Sheet name
	 * @throws TransformerException
	 */
	public void createReportAndUpdateExcel(String browserType, String browserVersion, String os, String osVersion,
			String testEnv, String scriptName, String sheetName) throws TransformerException {
		TransformerFactory transformerFactory = TransformerFactory.newInstance();
		Transformer transformer = transformerFactory.newTransformer();

		/** Transforming into String to identify Pass and Fail counts */
		StringWriter writer = new StringWriter();
		StreamResult stringResult = new StreamResult(writer);
		DOMSource source = new DOMSource(getRep().getXmlDoc());

		transformer.transform(source, stringResult);

		String browserComponent = browserType + ',' + browserVersion + ',' + os + ',' + osVersion;

		String moduleName = sheetName;

		/**
		 * Reducing count by one as first step is default and it always in pass status
		 */

		int passStepCount = 0;
		int failStepCount = 0;

		for (TestStep step : rep.getResultObject().getTestSteps()) {
			if ("pass".equalsIgnoreCase(step.getStepStatus())) {
				passStepCount++;
			} else if("fail".equalsIgnoreCase(step.getStepStatus())) {
				failStepCount++;
			}
		}

		/** Update script wise results back to excel based on counts */
		if (failStepCount > 0 || passStepCount <= 0) {
			updateResults(sheetName, scriptName, browserComponent, "Fail");
			Map<String, String> failedTest = new HashMap<>();
			failedTest.put("moduleName", moduleName);
			failedTest.put("browserConfig", browserComponent);
			failedTest.put("server", rep.getServer());
			failedTest.put("testenv", testEnv);
			failedTest.put("testScriptName", scriptName);
			FailedTests.setFailedScripts(failedTest);
		} else {
			updateResults(sheetName, scriptName, browserComponent, "Pass");
			LOGGER.info(scriptName + " is passed.");
		}

		ExecutionResult exeResultObj = null;
		Boolean isScriptPresent = false;
		/** Logic to generate map with email content */
		if (!executionResultMap.containsKey(moduleName + '|' + browserComponent + '|' + testEnv)) {
			exeResultObj = new ExecutionResult();
			exeResultObj.setStartTime(rep.getResultObject().getStartTime());
			if (failStepCount > 0 || passStepCount <= 0) {
				exeResultObj.setTcPassCount(0);
				exeResultObj.setTcFailCount(1);
				exeResultObj.setFailScript(scriptName + ",");
			} else {
				exeResultObj.setTcPassCount(1);
				exeResultObj.setTcFailCount(0);
			}
			exeResultObj.setTcCount(1);
			executionResultMap.put(moduleName + '|' + browserComponent + '|' + testEnv, exeResultObj);

		} else {
			exeResultObj = executionResultMap.get(moduleName + '|' + browserComponent + '|' + testEnv);
			if (failStepCount > 0 || passStepCount <= 0) {
				int failCount = exeResultObj.getTcFailCount();
				String failScripts = exeResultObj.getFailScript() != null
						? exeResultObj.getFailScript() + scriptName + ","
						: scriptName + ",";
				String failScripts2 = exeResultObj.getFailScript();

				if (failScripts2 != null) {
					String[] arrFailScripts2 = failScripts2.split(",");
					isScriptPresent = Arrays.asList(arrFailScripts2).contains(scriptName);
					if (!isScriptPresent) {
						exeResultObj.setTcFailCount(failCount + 1);
					}
				} else {
					exeResultObj.setTcFailCount(failCount + 1);
				}

				exeResultObj.setFailScript(failScripts);
			} else {
				int passCount = exeResultObj.getTcPassCount();
				int failCount = exeResultObj.getTcFailCount();
				String failScripts2 = exeResultObj.getFailScript();
				if (failScripts2 != null) {
					String[] arrFailScripts2 = failScripts2.split(",");
					isScriptPresent = Arrays.asList(arrFailScripts2).contains(scriptName);
					if (isScriptPresent) {
						exeResultObj.setTcFailCount(failCount - 1);
					}
				}

				exeResultObj.setTcPassCount(passCount + 1);
			}
			int totalCount = exeResultObj.getTcCount();
			if (!isScriptPresent) {
				exeResultObj.setTcCount(totalCount + 1);
			}
		}

		/** Creating end tags for each script based on pass and fail counts */
		createEndTags(passStepCount, failStepCount);

		ObjectMapper mapper = new ObjectMapper();

		if (moduleWiseReport.equalsIgnoreCase("y")) {
			File emailableFolder = new File(
					ConfigConstants.PARENTFOLDER_PATH
							+ getPropertyValue(PropertyFileConstants.CONFIG_PROPERTIES, "tempTestResultsPath"),
					sheetName + "-" + browserType + browserVersion + os + osVersion + "-" + testEnv);
			if (!emailableFolder.exists()) {
				emailableFolder.mkdir();
			}

			try {
				mapper.writeValue(
						new File(emailableFolder, scriptName + browserType + System.currentTimeMillis() + ".json"),
						rep.getResultObject());
			} catch (JsonGenerationException e) {
				e.printStackTrace();
			} catch (JsonMappingException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

		File consolidatedFolder = new File(
				ConfigConstants.PARENTFOLDER_PATH
						+ getPropertyValue(PropertyFileConstants.CONFIG_PROPERTIES, "consolidatedResultPath"),
				sheetName + "-" + browserType + browserVersion + os + osVersion + "-" + testEnv);

		if (!consolidatedFolder.exists()) {
			consolidatedFolder.mkdir();
		}
		try {
			mapper.writeValue(
					new File(consolidatedFolder, scriptName + browserType + System.currentTimeMillis() + ".json"),
					rep.getResultObject());
		} catch (JsonGenerationException e) {
			e.printStackTrace();
		} catch (JsonMappingException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Method to create Passed, Failed count and Start and end time tags for script
	 * in XML
	 * 
	 * @param passStepCount
	 *            : Number of Test steps passed
	 * 
	 * @param failStepCount
	 *            : Number of Test steps passed
	 */
	private void createEndTags(int passStepCount, int failStepCount) {

		if (failStepCount > 0) {
			rep.getResultObject().setStatus("Failed");
		} else {
			rep.getResultObject().setStatus("Passed");
		}

		rep.getResultObject().setPassed(String.valueOf(passStepCount));
		rep.getResultObject().setFailed(String.valueOf(failStepCount));
		rep.getResultObject().setEndTime(DateUtils.getCurrentDate());
		rep.getResultObject().setTotalTime(
				DateUtils.calcExec(rep.getResultObject().getStartTime(), rep.getResultObject().getEndTime()));
	}

	/**
	 * Method to get CSS AttributeValue from given locator
	 * 
	 * @param byVal
	 *            : Element locator
	 * @param iSecs
	 *            :Number of seconds to wait
	 * @param attributeValue
	 *            : CSS Attribute of the element
	 * @return capturedAttributeValue : Captured value of the CSS attribute
	 */
	public String getCssValue(By byVal, int iSecs, String attributeValue) {
		String capturedAttributeValue = null;
		try {
			fluentWait(byVal, iSecs);
			capturedAttributeValue = driver.findElement(byVal).getCssValue(attributeValue);
		} catch (Exception e) {
			LOGGER.error("Exception in getCssValue()" + e);
			rep.reportinCatch(e);
		}
		return capturedAttributeValue;
	}

	/**
	 * Method copies file from source to destination. Based on boolean value given
	 * to this method it will override existing copy in destination folder
	 * 
	 * @param sFROM
	 * @param sTO
	 * @param replaceExistingFile
	 */
	public static void copyFile(String sFROM, String sTO, boolean replaceExistingFile) {
		try {
			Path FROM = Paths.get(ConfigConstants.PARENTFOLDER_PATH + sFROM);
			String fromFile = FilenameUtils.removeExtension(sFROM);
			if (replaceExistingFile) {
				String toFileName = sFROM.replace(fromFile, sTO);
				Path TO = Paths.get(ConfigConstants.PARENTFOLDER_PATH + toFileName);
				// overwrite existing file, if exists
				CopyOption[] options = new CopyOption[] { StandardCopyOption.REPLACE_EXISTING,
						StandardCopyOption.COPY_ATTRIBUTES };
				Files.copy(FROM, TO, options);
			} else {
				DateFormat dateFormat = new SimpleDateFormat("YYYYMMddHHmmss");
				String toFileName = sFROM.replace(fromFile, sTO + (dateFormat.format(new Date())));
				Path TO = Paths.get(ConfigConstants.PARENTFOLDER_PATH + toFileName);
				Files.copy(FROM, TO);
			}
		} catch (IOException e) {
			LOGGER.error("Exception in copyFile()" + e);
		}
	}

	/**
	 * Method to Encrypt String
	 * 
	 * @param str
	 * @return : string by replacing each character with corresponding ASCII value
	 */
	public StringBuilder encryption(String str) {
		StringBuilder encryptedString = new StringBuilder();
		try {
			for (int i = 0; i < str.length(); i++) {
				char temp = str.charAt(i);
				int ascii = (int) temp;
				encryptedString.append(String.valueOf(ascii) + " ");
			}
		} catch (Exception e) {
			LOGGER.error("Exception in encryption()" + e);
			rep.reportinCatch(e);
		}
		return encryptedString;
	}

	/**
	 * Method to decrypt String
	 * 
	 * @param str
	 * @return : String by retrieving an exact character from each ASCII value
	 */
	public StringBuilder decryption(String str) {
		StringBuilder decryptedValue = new StringBuilder();
		try {
			String[] myStringArray = str.split(" ");
			for (int i = 0; i < myStringArray.length; i++) {
				int a = Integer.parseInt(myStringArray[i]);
				char ex = (char) a;
				decryptedValue.append(ex);
			}

		} catch (Exception e) {
			LOGGER.error("Exception in decryption()" + e);
			rep.reportinCatch(e);
		}
		return decryptedValue;
	}

	/**
	 * This method injects jquery to the application.
	 */
	public void injectJQuery() {
		JavascriptExecutor js = getDriver();

		getDriver().manage().timeouts().setScriptTimeout(20, TimeUnit.SECONDS);

		for (int i = 0; i < 4; i++) {

			if (isJqueryActive(getDriver()) == false) {
				delay(MiscConstants.S_DELAY);
				if (i < 2) { // 0,1
					js.executeScript(jQueryLoader(getPropertyValue(PropertyFileConstants.CONFIG_PROPERTIES, "sCdn")));
				} else { // 2,3
					js.executeScript(jQueryLoader(getPropertyValue(PropertyFileConstants.CONFIG_PROPERTIES, "sMiLib")));
				}

			} else {

				break;
			}

			if (i == 3 && isJqueryActive(getDriver()) == false) {

				LOGGER.error("Exception in Inject jQuery.");
				getRep().report(Status.PASS, "Inject jQuery : Please check hosted jQuery library/ version!",
						Screenshot.FALSE);

			}
		}
	}

	/**
	 * @param driver
	 * @return Boolean
	 */
	public static Boolean isJqueryActive(WebDriver driver) {
		String jscript = "return window.jQuery != undefined && jQuery.active == 0";
		return (Boolean) ((JavascriptExecutor) driver).executeScript(jscript);
	}

	/**
	 * This method loads jquery from the given source url.
	 * 
	 * @param jQryLibUrl
	 *            : jquery source url
	 * @return
	 */
	public String jQueryLoader(String jQryLibUrl) {
		String jQueryLoader = "(function(jqueryUrl, callback) {    if (typeof jqueryUrl != 'string') {"
				+ "        jqueryUrl = '" + jQryLibUrl + "';" + "    }" + "    if (typeof jQuery == 'undefined') {"
				+ "        var script = document.createElement('script');     "
				+ "        var head = document.getElementsByTagName('head')[0];" + "        var done = false;"
				+ "        script.onload = script.onreadystatechange = (function() {"
				+ "            if (!done && (!this.readyState || this.readyState == 'loaded' || this.readyState == 'complete')) {"
				+ "                done = true;" + "                script.onload = script.onreadystatechange = null;"
				+ "                head.removeChild(script);" + "                callback();" + "            }"
				+ "        });        " + "    script.src = jqueryUrl;    " + "    script.type='text/javascript';"
				+ "    head.appendChild(script);" + "    }" + "})(arguments[0], arguments[arguments.length - 1]);";

		return jQueryLoader;

	}

	/**
	 * This method will wait for alert until wait time.
	 * 
	 * @param waitTime
	 *            : Time in seconds for which waiting for an alert
	 */
	public void waitForAlert(int waitTime) {
		try {
			WebDriverWait alertWait = new WebDriverWait(getDriver(), 10);
			alertWait.until(ExpectedConditions.alertIsPresent());
		} catch (Exception e) {
			getRep().report(Status.FAIL, "Alert did not appear in " + waitTime + " seconds", Screenshot.TRUE);
		}
	}

	/**
	 * This method accepts alert after waiting for mentioned waitTime
	 * 
	 * @param waitTime
	 *            : Time in seconds for which waiting for an alert
	 * @param sReportText
	 *            : Report text.
	 */
	public void acceptAlert(int waitTime, String sReportText) {
		waitForAlert(waitTime);
		try {
			getDriver().switchTo().alert().accept();
			getRep().report(Status.PASS, "Accepted alert : " + sReportText, Screenshot.FALSE);
		} catch (Exception e) {
			getRep().reportinCatch(e);
		}
	}

	/**
	 * This method dismisses alert after waiting for mentioned waitTime
	 * 
	 * @param waitTime
	 *            : Time in seconds for which waiting for an alert
	 * @param sReportText
	 *            : Report text.
	 */
	public void dismissAlert(int waitTime, String sReportText) {
		waitForAlert(waitTime);
		try {
			getDriver().switchTo().alert().dismiss();
			getRep().report(Status.PASS, "Dissmissed alert" + sReportText, Screenshot.FALSE);
		} catch (Exception e) {
			getRep().reportinCatch(e);
		}
	}

	/**
	 * This method verifies alert text after waiting for mentioned waitTime
	 * 
	 * @param alertText
	 *            : alert text to be verified
	 * @param waitTime
	 *            : Time in seconds for which waiting for an alert
	 */
	public void verifyAlertMessage(String alertText, int waitTime) {
		waitForAlert(waitTime);
		try {
			System.out.println(getDriver().switchTo().alert().getText().trim());
			if (getDriver().switchTo().alert().getText().trim().equals(alertText)) {
				getRep().report(Status.PASS, "Alert with text '" + alertText + "' is displayed", Screenshot.FALSE);
			} else {
				getRep().report(Status.FAIL, "Alert message did not match with '" + alertText
						+ "'\nDisplayed message is '" + getDriver().switchTo().alert().getText().trim() + "'",
						Screenshot.TRUE);
			}
		} catch (Exception e) {
			getRep().reportinCatch(e);
		}
	}
}
