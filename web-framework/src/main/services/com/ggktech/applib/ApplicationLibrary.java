package com.ggktech.applib;

import io.selendroid.server.common.exceptions.ElementNotVisibleException;

import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.TimeUnit;

import org.apache.log4j.Logger;
import org.apache.poi.ss.usermodel.Cell;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.Keys;
import org.openqa.selenium.StaleElementReferenceException;
import org.openqa.selenium.UnexpectedAlertBehaviour;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.ie.InternetExplorerDriver;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.remote.CapabilityType;
import org.openqa.selenium.remote.DesiredCapabilities;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.Select;
import org.openqa.selenium.support.ui.WebDriverWait;

import com.codoid.products.fillo.Connection;
import com.codoid.products.fillo.Fillo;
import com.codoid.products.fillo.Recordset;
import com.ggktech.service.PublicLibrary;
import com.ggktech.utils.ConfigConstants;
import com.ggktech.utils.JQuerySelector;
import com.ggktech.utils.MiscConstants;
import com.ggktech.utils.PropertyFileConstants;
import com.ggktech.utils.Screenshot;
import com.ggktech.utils.SheetConstants;
import com.ggktech.utils.Status;

/**
 * Class that contains the functions related to particular application.
 */

public class ApplicationLibrary extends PublicLibrary {
	private static final Logger LOGGER = Logger.getLogger(ApplicationLibrary.class);
	private String filePathExcel = "";
	private Map<String, String> excelValues;
	private Properties miscValue;
	private String randonValue;
	private int waitTime = Integer.parseInt(getPropertyValue(PropertyFileConstants.CONFIG_PROPERTIES, "fluentTime"));

	public ApplicationLibrary() {
		super();
		try {
			miscValue = getPropFileReader().getPropFile(PropertyFileConstants.MISC_PROPERTIES);
		} catch (IOException e) {
			getRep().reportinCatch(e);
		}
	}

	public Map<String, String> getExcelValues() {
		return excelValues;
	}

	public void setExcelValues(Map<String, String> excelValues) {
		this.excelValues = excelValues;
	}

	public Properties getMiscValue() {
		return miscValue;
	}

	public void setMiscValue(Properties miscValue) {
		this.miscValue = miscValue;
	}

	public int getWaitTime() {
		return waitTime;
	}

	public void setWaitTime(int waitTime) {
		this.waitTime = waitTime;
	}

	public String getRandonValue() {
		return randonValue;
	}

	public void setRandonValue(String ranVal) {
		this.randonValue = ranVal;
	}

	private List<WebElement> getElements(By byLocater) {
		return getDriver().findElements(byLocater);
	}

	private void setFilePathExcel(String testEnv) {
		this.filePathExcel = getPropertyValue(PropertyFileConstants.CONFIG_PROPERTIES, testEnv + "_filePath");
	}

	/**
	 * @param browserType
	 *            : browser on which the script is going to be run
	 * @param browserVersion
	 *            : browser version
	 * @param os
	 *            : operating system name where the script should run(windows,
	 *            linux, etc,.)
	 * @param osVersion
	 *            : version of the OS
	 * @return capabilities : capabilities set for the browser
	 * @throws IOException
	 */
	public DesiredCapabilities createDesiredCapabilities(String server, String browserType, String browserVersion,
			String os, String osVersion, String scriptName) throws IOException {

		DesiredCapabilities capabilities = new DesiredCapabilities();
		if (ConfigConstants.CHROME.equalsIgnoreCase(browserType)) {
			capabilities = DesiredCapabilities.chrome();
		} else if (ConfigConstants.FIREFOX.equalsIgnoreCase(browserType)) {
			capabilities = DesiredCapabilities.firefox();
		} else if (ConfigConstants.IE.equalsIgnoreCase(browserType)) {
			capabilities = DesiredCapabilities.internetExplorer();
			capabilities.setCapability(InternetExplorerDriver.INTRODUCE_FLAKINESS_BY_IGNORING_SECURITY_DOMAINS, true);
			capabilities.setCapability(InternetExplorerDriver.NATIVE_EVENTS, false);
			capabilities.setCapability(InternetExplorerDriver.IE_ENSURE_CLEAN_SESSION, true);
			capabilities.setCapability(CapabilityType.ACCEPT_SSL_CERTS, true);

		} else if (ConfigConstants.EDGE.equalsIgnoreCase(browserType)) {
			capabilities = DesiredCapabilities.edge();
		}
		capabilities.setCapability("browser", browserType);
		capabilities.setCapability("browser_version", browserVersion);
		capabilities.setCapability("os", os);
		capabilities.setCapability("os_version", osVersion);
		capabilities.setCapability(CapabilityType.UNEXPECTED_ALERT_BEHAVIOUR, UnexpectedAlertBehaviour.IGNORE);
		if (server.equalsIgnoreCase(ConfigConstants.CLOUD)) {
			capabilities.setCapability("name", scriptName + "-" + browserType);
			capabilities.setCapability("version", browserVersion);
			capabilities.setCapability("platform", os + " " + osVersion);
			capabilities.setCapability("browserstack.debug", "true");
			/** Getting capabilities from capabilities.properties file */
			// Properties caps =
			// getPropFileReader().getPropFile(PropertyFileConstants.CAPABILITIES_PROPERTIES);
			// Set<Object> capKeys = caps.keySet();
			// for (Object eachCapability : capKeys) {
			// System.out.println("Key : " + eachCapability + ", Value : " +
			// caps.getProperty((String) eachCapability));
			// capabilities.setCapability((String) eachCapability, caps.getProperty((String)
			// eachCapability));
			// }
		}
		return capabilities;
	}

	/**
	 * Function to login to sales crm application.
	 * 
	 * @param scriptName
	 */
	public void loginCRM(String testEnv, String scriptName) {
		delay(MiscConstants.S_DELAY);
		excelValues = excelValues(testEnv, scriptName, SheetConstants.LOGIN_SHEET);
		// setValue(byLocator("sUserName", PropertyFileConstants.OR_PROPERTIES),
		// excelValues.get("UserName"), waitTime,
		// "Username");
		setValue(byLocator("sUserName", PropertyFileConstants.OR_PROPERTIES), excelValues.get("UserName") + Keys.ENTER,
				waitTime, "Username");
		// clickElement(byLocator("sNext", PropertyFileConstants.OR_PROPERTIES),
		// waitTime, "Next");
		delay(MiscConstants.S_DELAY);

		setPassword(byLocator("sPasswd", PropertyFileConstants.OR_PROPERTIES),

				excelValues.get("Password"), waitTime, "Password");
		clickElement(byLocator("sPasswdNext", PropertyFileConstants.OR_PROPERTIES), waitTime, "Login");
		delay(MiscConstants.M_DELAY);
		verifyPage(getPropertyValue(PropertyFileConstants.MISC_PROPERTIES, "CRMHomePage"), "CRM Home page");
	}

	/**
	 * Function to login to sales crm application.
	 * 
	 * @param scriptName
	 */
	public void loginHrms(String testEnv, String scriptName) {
		delay(MiscConstants.S_DELAY);
		excelValues = excelValues(testEnv, scriptName, SheetConstants.LOGIN_SHEET);
		// setValue(byLocator("sUserName", PropertyFileConstants.OR_PROPERTIES),
		// excelValues.get("UserName"), waitTime,
		// "Username");
		setValue(byLocator("sUserName", PropertyFileConstants.OR_PROPERTIES), excelValues.get("UserName") + Keys.ENTER,
				waitTime, "Username");
		// clickElement(byLocator("sNext", PropertyFileConstants.OR_PROPERTIES),
		// waitTime, "Next");
		delay(MiscConstants.S_DELAY);

		setPassword(byLocator("sPasswd", PropertyFileConstants.OR_PROPERTIES),

				excelValues.get("Password"), waitTime, "Password");
		clickElement(byLocator("sPasswdNext", PropertyFileConstants.OR_PROPERTIES), waitTime, "Login");
		delay(MiscConstants.M_DELAY);
	}

	/**
	 * Function to login into gmail and verify the inbox count.
	 * 
	 * @param scriptName
	 * @throws IOException
	 */
	public void loginGmailGetInboxCount(String testEnv, String scriptName) throws IOException {

		delay(MiscConstants.S_DELAY);
		excelValues = excelValues(testEnv, scriptName, SheetConstants.LOGIN_SHEET);
		setValue(byLocator("sUserName", PropertyFileConstants.OR_PROPERTIES), excelValues.get("UserName"), waitTime,
				"Username");
		clickElement(byLocator("sNext", PropertyFileConstants.OR_PROPERTIES), waitTime, "Next");
		delay(MiscConstants.S_DELAY);
		setValue(byLocator("sPasswd", PropertyFileConstants.OR_PROPERTIES), excelValues.get("Password"), waitTime,
				"Password");
		clickElement(byLocator("sPasswdNext", PropertyFileConstants.OR_PROPERTIES), waitTime, "Login");
		delay(MiscConstants.L_DELAY);
		getDriver().navigate().to("https://mail.google.com/mail/");
		delay(MiscConstants.S_DELAY);
		String inboxText = getText(byLocator("lnkInbox", PropertyFileConstants.OR_PROPERTIES), waitTime, "Inbox");
		String pageTitle = inboxText + " - " + excelValues.get("UserName") + " - GGK Tech Mail";
		verifyPage(pageTitle, "Gmail Home page");
		getDriver().navigate().to("http://salescrmtest.ggktech.com");
		verifyPage(getPropertyValue(PropertyFileConstants.MISC_PROPERTIES, "CRMHomePage"), "CRM Home page");

	}

	/**
	 * Function to navigate to creating lead.
	 */
	public void navigationToLead() {
		delay(MiscConstants.S_DELAY);
		waitForElemenetToDisappear(byLocator("sCRMLoader", PropertyFileConstants.OR_PROPERTIES));
		waitForElement(byLocator("crmSidebar", PropertyFileConstants.OR_PROPERTIES));
		WebElement element1 = getDriver().findElement(byLocator("crmSidebar", PropertyFileConstants.OR_PROPERTIES));
		Actions userAction = new Actions(getDriver());
		userAction.moveToElement(element1).perform();
		clickElement(byLocator("leadMangement", PropertyFileConstants.OR_PROPERTIES), waitTime, "Leadmangement button");
		delay(MiscConstants.M_DELAY);
		clickElement(byLocator("createLeadButton", PropertyFileConstants.OR_PROPERTIES), waitTime,
				"Create lead button");
		delay(MiscConstants.M_DELAY);
		verifyPage(getPropertyValue(PropertyFileConstants.MISC_PROPERTIES, "LeadPage"), "Create Lead Page");
	}

	/**
	 * This method waits for a particular element to disappear.
	 * 
	 * @param bylocator
	 */
	private void waitForElemenetToDisappear(By bylocator) {
		WebDriverWait wait = new WebDriverWait(getDriver(), 5);
		wait.ignoring(ElementNotVisibleException.class, StaleElementReferenceException.class)
				.until(ExpectedConditions.invisibilityOfElementLocated(bylocator));
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
				getRep().report(Status.PASS, "Inject jQuery: Please check hosted jQuery library/ version!",
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
	 * Function to create lead.
	 * 
	 * @param scriptName
	 */
	public void createLead(String testEnv, String scriptName) {
		try {
			excelValues = excelValues(testEnv, scriptName, SheetConstants.LOGIN_SHEET);
			setValue(byLocator("organisation", PropertyFileConstants.OR_PROPERTIES),
					excelValues.get("OrganizationName"), waitTime, "Orginisation");
			setValue(byLocator("website", PropertyFileConstants.OR_PROPERTIES),
					"http:\\www.google" + (Math.random() * (1000 + 1 - 1)) + 1 + ".co.in", waitTime, "Website");

			JavascriptExecutor jse = (JavascriptExecutor) getDriver();
			WebElement element = getDriver().findElement(byLocator("country", PropertyFileConstants.OR_PROPERTIES));
			jse.executeScript("window.scrollBy(0," + (element.getLocation().getY() - 80) + ");");

			setValue(byLocator("country", PropertyFileConstants.OR_PROPERTIES), excelValues.get("Country"), waitTime,
					"Country");
			WebDriverWait wait = new WebDriverWait(getDriver(), waitTime);
			wait.until(ExpectedConditions
					.visibilityOfElementLocated(byLocator("countryOptions", PropertyFileConstants.OR_PROPERTIES)));
			for (WebElement webEl : getDriver()
					.findElements(byLocator("countryOptions", PropertyFileConstants.OR_PROPERTIES))) {
				if (webEl.getText().equals(excelValues.get("Country"))) {
					webEl.click();
				}
			}
			delay(MiscConstants.S_DELAY);
			setValue(byLocator("state", PropertyFileConstants.OR_PROPERTIES), excelValues.get("State"), waitTime,
					"State");
			delay(MiscConstants.S_DELAY);
			waitForElemenetToDisappear(byLocator("sCRMLoader", PropertyFileConstants.OR_PROPERTIES));

			addPrimaryConatact(excelValues.get("FirstName"), excelValues.get("LastName"),
					excelValues.get("Designation"), excelValues.get("EmailId"));

			jse.executeScript("arguments[0].scrollIntoView(true);",
					getDriver().findElement(byLocator("saveButton", PropertyFileConstants.OR_PROPERTIES)));

			clickElement(byLocator("saveButton", PropertyFileConstants.OR_PROPERTIES), waitTime, "Save");
			waitForElemenetToDisappear(byLocator("sCRMLoader", PropertyFileConstants.OR_PROPERTIES));
			waitForElement(byLocator("sCrmSuccessMsg", PropertyFileConstants.OR_PROPERTIES));
			verifyText(byLocator("sCrmSuccessMsg", PropertyFileConstants.OR_PROPERTIES),
					miscValue.getProperty("successMessage"), "Verifying success message", waitTime, Screenshot.TRUE);
			delay(MiscConstants.M_DELAY);
		} catch (Exception e) {
			LOGGER.error("Exception Occured" + e);
			getRep().reportinCatch(e);
		}

	}

	/**
	 * Function to create lead.
	 * 
	 * @param scriptName
	 */
	public void createAndSaveLead(String testEnv, String scriptName) {
		try {
			excelValues = excelValues(testEnv, scriptName, SheetConstants.LOGIN_SHEET);
			setValue(byLocator("organisation", PropertyFileConstants.OR_PROPERTIES),
					excelValues.get("OrganizationName"), waitTime, "Orginisation");
			setValue(byLocator("website", PropertyFileConstants.OR_PROPERTIES),
					"http:\\www.google" + (Math.random() * (1000 + 1 - 1)) + 1 + ".co.in", waitTime, "Website");

			JavascriptExecutor jse = (JavascriptExecutor) getDriver();
			WebElement element = getDriver().findElement(byLocator("country", PropertyFileConstants.OR_PROPERTIES));
			jse.executeScript("window.scrollBy(0," + (element.getLocation().getY() - 80) + ");");

			setValue(byLocator("country", PropertyFileConstants.OR_PROPERTIES), excelValues.get("Country"), waitTime,
					"Country");

			WebDriverWait wait = new WebDriverWait(getDriver(), waitTime);
			wait.until(ExpectedConditions
					.visibilityOfElementLocated(byLocator("countryOptions", PropertyFileConstants.OR_PROPERTIES)));
			for (WebElement webEl : getDriver()
					.findElements(byLocator("countryOptions", PropertyFileConstants.OR_PROPERTIES))) {
				if (webEl.getText().equals(excelValues.get("Country"))) {
					webEl.click();
				}
			}
			delay(MiscConstants.S_DELAY);
			setValue(byLocator("state", PropertyFileConstants.OR_PROPERTIES), excelValues.get("State"), waitTime,
					"State");
			delay(MiscConstants.S_DELAY);
			waitForElemenetToDisappear(byLocator("sCRMLoader", PropertyFileConstants.OR_PROPERTIES));

			addPrimaryConatact(excelValues.get("FirstName"), excelValues.get("LastName"),
					excelValues.get("Designation"), excelValues.get("EmailId"));

			jse.executeScript("arguments[0].scrollIntoView(true);",
					getDriver().findElement(byLocator("saveButton", PropertyFileConstants.OR_PROPERTIES)));

			clickElement(byLocator("saveButton", PropertyFileConstants.OR_PROPERTIES), waitTime, "Save button");
			waitForElemenetToDisappear(byLocator("sCRMLoader", PropertyFileConstants.OR_PROPERTIES));
			waitForElement(byLocator("sCrmSuccessMsg", PropertyFileConstants.OR_PROPERTIES));
			verifyText(byLocator("sCrmSuccessMsg", PropertyFileConstants.OR_PROPERTIES),
					miscValue.getProperty("fSuccessMessage"), "Verifying success message", waitTime, Screenshot.TRUE);
			delay(MiscConstants.M_DELAY);
		} catch (Exception e) {
			LOGGER.error("Exception Occured" + e);
			getRep().reportinCatch(e);
		}

	}

	/**
	 * This method adds contact to the lead.
	 * 
	 * @param firstName
	 * @param lastName
	 * @param designation
	 * @param email
	 * @return
	 */
	private void addPrimaryConatact(String firstName, String lastName, String designation, String email) {
		try {

			scrollToElement(byLocator("country", PropertyFileConstants.OR_PROPERTIES));
			Thread.sleep(1000);
			waitForElemenetToDisappear(byLocator("sCRMLoader", PropertyFileConstants.OR_PROPERTIES));
			waitForElemenetToDisappear(byLocator("sCRMLoader", PropertyFileConstants.OR_PROPERTIES));
			setValue(byLocator("firstName", PropertyFileConstants.OR_PROPERTIES), firstName, MiscConstants.M_DELAY,
					"Contact First Name");
			setValue(byLocator("lastname", PropertyFileConstants.OR_PROPERTIES), lastName, MiscConstants.M_DELAY,
					"Contact Last Name");

			setValue(byLocator("designation", PropertyFileConstants.OR_PROPERTIES), designation, MiscConstants.M_DELAY,
					"Contact Designation");

			setValue(byLocator("emailAddress", PropertyFileConstants.OR_PROPERTIES), email, MiscConstants.M_DELAY,
					"Contact Email");

			JavascriptExecutor jse = (JavascriptExecutor) getDriver();
			jse.executeScript("arguments[0].scrollIntoView(true);",
					getDriver().findElement(byLocator("contactStatusID", PropertyFileConstants.OR_PROPERTIES)));

			selectValueByIndex(byLocator("contactStatusID", PropertyFileConstants.OR_PROPERTIES), 1);
			clickElement(byLocator("addContactButton", PropertyFileConstants.OR_PROPERTIES), MiscConstants.M_DELAY,
					"Add Contact");

			delay(MiscConstants.M_DELAY);
		} catch (Exception e) {
			getRep().reportinCatch(e);
		}
	}

	/**
	 * This method selects the value by index in the dropdown by given by object
	 * 
	 * @param byLocator
	 *            : bylocator of the dropdwon
	 * @param index
	 *            : index of dropdown value
	 */
	private void selectValueByIndex(By byLocator, int index) {
		try {
			new Select(getElements(byLocator).get(0)).selectByIndex(index);
		} catch (Exception e) {
			getRep().reportinCatch(e);
		}
	}

	/**
	 * Function to scroll to particular element.
	 * 
	 * @param byLocater
	 */
	private void scrollToElement(By byLocator) {
		try {
			getDriver().executeScript("arguments[0].scrollIntoView(true);", getElements(byLocator).get(0));
		} catch (Exception e) {
			getRep().reportinCatch(e);
		}
	}

	/**
	 * Function to navigate to search leads page.
	 */
	public void navigatingSearchLead() {

		waitForElement(byLocator("crmSidebar", PropertyFileConstants.OR_PROPERTIES));
		WebElement element1 = getDriver().findElement(byLocator("crmSidebar", PropertyFileConstants.OR_PROPERTIES));

		Actions userAction = new Actions(getDriver());
		userAction.moveToElement(element1).perform();
		delay(MiscConstants.M_DELAY);
		if (getDriver().findElement(byLocator("searchLeadsButton", PropertyFileConstants.OR_PROPERTIES))
				.isDisplayed()) {
			clickElement(byLocator("searchLeadsButton", PropertyFileConstants.OR_PROPERTIES), waitTime,
					"Search lead button");
		} else {
			clickElement(byLocator("leadMangement", PropertyFileConstants.OR_PROPERTIES), waitTime,
					"Leadmangement button");
			delay(MiscConstants.M_DELAY);
			clickElement(byLocator("searchLeadsButton", PropertyFileConstants.OR_PROPERTIES), waitTime,
					"Search lead button");
		}

		delay(MiscConstants.M_DELAY);
		verifyPage(getPropertyValue(PropertyFileConstants.MISC_PROPERTIES, "LeadDetailsPage"), "Searchleads Page");
		setDropdownValue(byLocator("showDropDown", PropertyFileConstants.OR_PROPERTIES), "10", waitTime,
				"Records count");
		delay(MiscConstants.M_DELAY);
	}

	/**
	 * Function to search for lead.
	 */
	public void searchLeadDetails() {
		setValue(byLocator("globalsearch", PropertyFileConstants.OR_PROPERTIES), excelValues.get("OrganizationName"),
				waitTime, "Orginasation name");
		clickElement(byLocator("searchButton", PropertyFileConstants.OR_PROPERTIES), waitTime, "Search  button");
		delay(MiscConstants.M_DELAY);
		verifyText(byLocator("organisationRecord", PropertyFileConstants.OR_PROPERTIES),
				excelValues.get("OrganizationName"), "Organisation name", waitTime, Screenshot.TRUE);
	}

	/**
	 * Function to delete lead.
	 */
	public void deleteLead() {
		clickElement(byLocator("organisationRecord", PropertyFileConstants.OR_PROPERTIES), waitTime, "Lead record");
		waitForElemenetToDisappear(byLocator("sCRMLoader", PropertyFileConstants.OR_PROPERTIES));

		JavascriptExecutor jse = (JavascriptExecutor) getDriver();
		jse.executeScript("arguments[0].scrollIntoView(true);",
				getDriver().findElement(byLocator("saveButton", PropertyFileConstants.OR_PROPERTIES)));

		clickElement(byLocator("deleteLeadRecord", PropertyFileConstants.OR_PROPERTIES), waitTime, "Delete button");
		acceptAlert(waitTime, "Lead Delete Confirmation");
		verifyAlertMessage("Successfully deleted record !!", waitTime);
		acceptAlert(waitTime, "Lead Deleted Success alert");
		delay(MiscConstants.M_DELAY);
	}

	/**
	 * Verifying pagination in search lead page.
	 */
	public void verifyPagination() {
		try {
			List<WebElement> pages = getDriver()
					.findElements(byLocator("pagination", PropertyFileConstants.OR_PROPERTIES));
			int size = pages.size() - 3;
			int count = 0;
			if (size > 0) {
				for (int i = 1; i <= 5; i++) {
					count = count + verifyNumberOfRecords();

					JavascriptExecutor jse = (JavascriptExecutor) getDriver();
					jse.executeScript("arguments[0].scrollIntoView(true);",
							getDriver().findElement(byLocator("navigationArrow", PropertyFileConstants.OR_PROPERTIES)));

					clickElement(byLocator("navigationArrow", PropertyFileConstants.OR_PROPERTIES), waitTime,
							"Clicked on Delete button");
					delay(MiscConstants.M_DELAY);
				}
			}

			else {
				LOGGER.info("Pagination does not exist");
			}
		} catch (Exception e) {
			LOGGER.error(e);
		}

	}

	/**
	 * Function to logout from salescrm application.
	 */
	public void logoutSalesCrm() {
		clickElement(byLocator("userName", PropertyFileConstants.OR_PROPERTIES), waitTime, "Username");
		clickElement(byLocator("logout", PropertyFileConstants.OR_PROPERTIES), waitTime, "Logout button");
	}

	/**
	 * @return return the number of rows in the pagination table.
	 */
	private int verifyNumberOfRecords() {
		return getDriver().findElement(byLocator("tableBody", PropertyFileConstants.OR_PROPERTIES))
				.findElements(By.tagName("tr")).size();

	}

	/**
	 * Getting map object of excel data based on test environment.
	 * 
	 * @param testEnv
	 *            : Environment for which excel should be picked up.
	 * @param scriptName
	 *            : Name of the script.
	 * @param sheetName
	 *            : Name of the excel sheet.
	 * @return : return map object of data
	 */
	public Map<String, String> excelValues(String testEnv, String scriptName, String sheetName) {
		Map<String, String> excelValues = new LinkedHashMap<String, String>();
		try {
			setFilePathExcel(testEnv);
			Fillo fillo = new Fillo();
			Connection connection = fillo.getConnection(ConfigConstants.PARENTFOLDER_PATH + filePathExcel);
			String strQuery = "Select * from " + sheetName + " where TestScriptName='" + scriptName + "'";
			Recordset recordset = connection.executeQuery(strQuery);
			recordset.moveNext();
			for (String eachField : recordset.getFieldNames()) {
				excelValues.put(eachField, recordset.getField(eachField));
			}
			connection.close();
			recordset.close();
		} catch (Exception e) {
			getRep().reportinCatch(e);
		}
		return excelValues;
	}

	/**
	 * Logging into Sphere mvc application
	 * 
	 * @param testEnv:
	 *            Test data file to be picked up.
	 * @param scriptName:
	 *            Script name of the test case.
	 */
	public void loginSphereMvc(String testEnv, String scriptName) {
		excelValues = excelValues(testEnv, scriptName, SheetConstants.LOGIN_SHEET);
		injectJQuery();
		getDriver().findElement(byLocator("sEmailLogin", PropertyFileConstants.OR_PROPERTIES))
				.sendKeys(excelValues.get(SheetConstants.USER_NAME));

		clickElement(byLocator("sEmailNxtBtn", PropertyFileConstants.OR_PROPERTIES), MiscConstants.M_DELAY,
				"Clicking on next button after entering email");

		waitForElement(byLocator("sPaswd", PropertyFileConstants.OR_PROPERTIES));

		setPassword(byLocator("sPaswd", PropertyFileConstants.OR_PROPERTIES), excelValues.get(SheetConstants.PASS_WORD),
				MiscConstants.M_DELAY, "Setting text for login email field");

		clickElement(byLocator("sSbmtBtn", PropertyFileConstants.OR_PROPERTIES), MiscConstants.M_DELAY,
				"Clicking on next button after entering email");

		waitForElement(byLocator("sStaySignInNoBtn", PropertyFileConstants.OR_PROPERTIES));
		clickElement(byLocator("sStaySignInNoBtn", PropertyFileConstants.OR_PROPERTIES), MiscConstants.M_DELAY,
				"Clicking on next button after entering email");

		verifyPage("SPHEREboard", "Verifing Sphere dashboard title");
	}

	/**
	 * Navigate to sub-menu items in sphere.
	 * 
	 * @param menuItem:
	 *            menuitem to be clicked.
	 */
	public void navigateInSphere(String menuItem) {
		waitForElement(byLocator("sMenuBtn", PropertyFileConstants.OR_PROPERTIES));
		clickElement(byLocator("sMenuBtn", PropertyFileConstants.OR_PROPERTIES), MiscConstants.M_DELAY,
				"Clicking on left menu button");
		By locater = JQuerySelector.jQuery("#left-nav-menu>ul>li>a:contains('" + menuItem + "')");
		waitForElement(locater);
		clickElement(locater, MiscConstants.M_DELAY, "Clicking on menu item " + menuItem);
	}

	/**
	 * Verifying File Assessment page.
	 */
	public void verifyFileAssessmentPage() {
		waitForElement(byLocator("sInventotyPanel", PropertyFileConstants.OR_PROPERTIES));
		verifyPage("File Assessment", "Verifying File assessment page");
	}

	/**
	 * Verifying ACtive Directory page.
	 */
	public void verifyActiveDirPage() {
		try {
			verifyActiveDirCounts(byLocator("sDomainCompsCnt", PropertyFileConstants.OR_PROPERTIES));

			verifyActiveDirCounts(byLocator("sDomainUserCnt", PropertyFileConstants.OR_PROPERTIES));

			verifyActiveDirCounts(byLocator("sDomainGroupsCnt", PropertyFileConstants.OR_PROPERTIES));

			getRep().report(Status.PASS, "Verifying Active DirPage", Screenshot.FALSE);
		} catch (Exception e) {
			getRep().reportinCatch(e);
		}
	}

	/**
	 * Verify Active Dir stat values.
	 * 
	 * @param byLocater
	 *            : locater of stat groups.
	 */
	public void verifyActiveDirCounts(By byLocater) {
		hoverOnElement(byLocater);
		waitForElemenetToDisappear(byLocator("sCircleStat", PropertyFileConstants.OR_PROPERTIES));
		waitForElement(byLocator("sCircleStat", PropertyFileConstants.OR_PROPERTIES));
		if (!getText(byLocater, MiscConstants.M_DELAY, "").equals(
				getText(byLocator("sCircleStat", PropertyFileConstants.OR_PROPERTIES), MiscConstants.M_DELAY, ""))) {
			getRep().report(Status.FAIL, "Verifying Groups count doesn't match", Screenshot.TRUE);
		}
	}

	/**
	 * This method generates a mouse hover effect on the specified element.
	 * 
	 * @param byLocater:
	 *            Element to be hovered.
	 */
	public void hoverOnElement(By byLocater) {
		try {
			waitForElement(byLocater);
			new Actions(getDriver()).moveToElement(getElements(byLocater).get(0)).perform();
		} catch (Exception e) {
			getRep().reportinCatch(e);
		}
	}
	
	public void FillAutomationForm(String testEnv, String scriptName)
	{
		try
		{
			clickElement(byLocator("CookieAccept_a", PropertyFileConstants.OR_PROPERTIES), MiscConstants.M_DELAY,"Accepting the cookies popup");
			excelValues = excelValues(testEnv, scriptName, SheetConstants.AUTOMATIONFORM_SHEET);
			setValue(byLocator("FirstName_input", PropertyFileConstants.OR_PROPERTIES),excelValues.get(SheetConstants.FIRSTNAME), MiscConstants.M_DELAY, "Set the value to the first name input box");
			setValue(byLocator("LastName_input", PropertyFileConstants.OR_PROPERTIES),excelValues.get(SheetConstants.LASTNAME), MiscConstants.M_DELAY, "Set the value to the last name input box");
			setValue(byLocator("DatePicker_input", PropertyFileConstants.OR_PROPERTIES),excelValues.get(SheetConstants.DATE), MiscConstants.M_DELAY, "Set the date value to the date picket inbox");
			setDropdownValue(byLocator("Continents_select", PropertyFileConstants.OR_PROPERTIES), excelValues.get(SheetConstants.CONTINENT), MiscConstants.M_DELAY, "Select a continent by visble txt");
		}
		catch(Exception e)
		{
			getRep().reportinCatch(e);
		}
		
	}

}
