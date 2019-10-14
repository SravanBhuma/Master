package com.ggktech.utils;

import java.io.File;
import java.io.IOException;
import java.util.Properties;

import org.apache.commons.io.FileUtils;
import org.apache.log4j.Logger;
import org.apache.poi.ss.usermodel.Sheet;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.remote.Augmenter;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.testng.Assert;
import org.w3c.dom.Document;
import com.ggktech.dao.ExcelDataHandler;
import com.ggktech.dao.PropertiesFileReader;
import com.ggktech.resultobjects.TestCaseResult;
import com.ggktech.resultobjects.TestStep;

/**
 * Class for reporting functionality.
 */
public class Reporter {

	private static final Logger LOGGER = Logger.getLogger(Reporter.class.getName());
	private RemoteWebDriver driver;
	private Document xmlDoc;
	private String sScreenName;
	private String server;
	private String filePathTestSuite;
	private PropertiesFileReader propFileReader;
	private Properties configProperties;
	private String takeScreenshot;

	private TestCaseResult resultObject;
	public int stepCounter = 0;

	public TestCaseResult getResultObject() {
		return resultObject;
	}

	public void setResultObject(TestCaseResult resultObject) {
		this.resultObject = resultObject;
	}

	public RemoteWebDriver getDriver() {
		return driver;
	}

	public void setDriver(RemoteWebDriver driver) {
		this.driver = driver;
	}

	public Document getXmlDoc() {
		return xmlDoc;
	}

	public void setXmlDoc(Document xmlDoc) {
		this.xmlDoc = xmlDoc;
	}

	public String getsScreenName() {
		return sScreenName;
	}

	public void setsScreenName(String sScreenName) {
		this.sScreenName = sScreenName;
	}

	public String getServer() {
		return server;
	}

	public void setServer(String server) {
		this.server = server;
	}

	/**
	 * @param sStepName
	 *            : Teststep name
	 * @param sStatus
	 *            : 0 is for pass or 1 for fail
	 * @param sDesc
	 *            : Exception description
	 * @param iScreenshot
	 *            : 0 is for required and 1 is for not required
	 */
	public void report(Status sStatus, String sDesc, Screenshot iScreenshot) {
		String description = sDesc;
		try {
			propFileReader = PropertiesFileReader.getInstance();
			configProperties = propFileReader.getPropFile(PropertyFileConstants.CONFIG_PROPERTIES);
			filePathTestSuite = configProperties.getProperty("filePathTestSuite");
			Sheet mainSheet = ExcelDataHandler.getSheetData(SheetConstants.TEST_SUITE_SHEET, filePathTestSuite);
			takeScreenshot = mainSheet.getRow(ConfigConstants.TAKE_SCREEN_SHOT_ROW_INDEX)
					.getCell(ConfigConstants.TAKE_SCREEN_SHOT_COLUMN_INDEX).getStringCellValue();
			if (sDesc.contains("|")) {
				description = sDesc.replace("|", "-");
			}

			if (sStatus == Status.PASS || sStatus == Status.INFO || sStatus == Status.WARNING) {
				if ((iScreenshot == Screenshot.TRUE)
						&& (takeScreenshot.equalsIgnoreCase(ConfigConstants.All_SCREENSHOT))) {
					addTestStep(description, Screenshot.TRUE, sStatus);
				} else {

					addTestStep(description, Screenshot.FALSE, sStatus);

				}
			} else {
				failedreport(description, iScreenshot, sStatus);
			}
		} catch (Exception e) {
			LOGGER.error("Exception in report method " + e);
		}
	}

	/**
	 * @param sStepName
	 *            : Teststep name i.e. Error Captured
	 * @param description
	 *            : Exception description
	 * @param iScreenshot
	 *            : 0 or 1
	 * @throws IOException
	 */
	private void failedreport(String description, Screenshot iScreenshot, Status sStatus) throws IOException {
		LOGGER.info(sScreenName.split("-")[0] + " is failed due to reason : " + description);
		String desc = description;
		if (desc.contains("org.openqa.selenium.TimeoutException:")) {
			String[] sDescSplit = desc.split("org.openqa.selenium.TimeoutException:");
			desc = sDescSplit[1];
		}
		if (desc.contains("org.openqa.selenium.UnhandledAlertException:")) {
			String[] sDescSplit = desc.split("org.openqa.selenium.UnhandledAlertException:");
			desc = sDescSplit[1];
			reportAndStopExe(desc, sStatus);
		}
		if (desc.contains("org.openqa.selenium.WebDriverException:")) {
			String[] sDescSplit = desc.split("org.openqa.selenium.WebDriverException:");
			desc = sDescSplit[1];
			reportAndStopExe(desc, sStatus);
		}
		if (desc.contains("org.openqa.selenium.remote.SessionNotFoundException:")) {
			String[] sDescSplit = desc.split("org.openqa.selenium.remote.SessionNotFoundException:");
			desc = sDescSplit[1];
			reportAndStopExe(desc, sStatus);
		}
		if (desc.contains("org.openqa.selenium.NoSuchElementException:")) {
			String[] sDescSplit = desc.split("org.openqa.selenium.NoSuchElementException:");
			desc = sDescSplit[1];
			if (desc.contains("For documentation on this error")) {
				String[] sDescSplit2 = desc.split(
						"For documentation on this error, please visit: http://seleniumhq.org/exceptions/no_such_element.html");
				desc = sDescSplit2[0];
			}
		}
		if (desc.contains("org.openqa.selenium.remote.UnreachableBrowserException:")) {
			String[] sDescSplit = desc.split("org.openqa.selenium.remote.UnreachableBrowserException:");
			desc = sDescSplit[1];
			reportAndStopExe(desc, sStatus);
		}
		if (desc.contains("org.openqa.selenium.NoSuchWindowException:")) {
			String[] sDescSplit = desc.split("org.openqa.selenium.NoSuchWindowException:");
			desc = sDescSplit[1];
			reportAndStopExe(desc, sStatus);
		}
		if (iScreenshot == Screenshot.TRUE) {
			if (takeScreenshot.equalsIgnoreCase(ConfigConstants.All_SCREENSHOT)
					|| takeScreenshot.equalsIgnoreCase(ConfigConstants.FAIL_SCREENSHOT)) {

				addTestStep(desc, Screenshot.TRUE, Status.FAIL);
				if (sStatus == Status.FAIL) {
					Assert.fail();
				}
			} else {

				addTestStep(desc, Screenshot.FALSE, Status.FAIL);
				if (sStatus == Status.FAIL) {
					Assert.fail();
				}
			}
		} else {
			reportAndStopExe(desc, sStatus);

		}

	}

	/**
	 * @param e
	 *            : Exception description
	 */
	public void reportinCatch(Exception e) {
		try {
			String[] sBuilInfo = e.toString().split(ConfigConstants.BUILD_INFO);
			report(Status.FAIL, "ErrorCaptured" + sBuilInfo[0], Screenshot.TRUE);
		} catch (Exception ee) {
			LOGGER.error("Exception in reportinCatch method " + ee);
		}
	}

	/**
	 * @param desc
	 * @param sStatus
	 */
	public void reportAndStopExe(String desc, Status sStatus) {
		addTestStep(desc, Screenshot.FALSE, Status.FAIL);
		if (sStatus == Status.FAIL) {
			Assert.fail();
		}
	}

	/**
	 * @param sStat
	 *            : 0(pass) or 1(fail)
	 * @return sVal : returns screenshot name
	 * @throws IOException
	 */
	public String getScreen(String sStat) throws IOException {
		try {
			String screenPath = ConfigConstants.PARENTFOLDER_PATH + PropertiesFileReader.getInstance()
					.getPropFile(PropertyFileConstants.CONFIG_PROPERTIES).getProperty("screenshot");

			File directory = new File(screenPath);
			if (!directory.exists()) {
				directory.mkdir();
			}

			int ScreenCount = new File(screenPath).list().length;
			String totafilepath = null;
			String sVal = null;
			File screenshotFile;

			if ("pass".equalsIgnoreCase(sStat)) {
				sVal = PropertiesFileReader.getInstance().getPropFile(PropertyFileConstants.CONFIG_PROPERTIES)
						.getProperty("passedScreenshotName") + sScreenName + (ScreenCount) + ".png";
				totafilepath = screenPath + "" + sVal;
			} else if ("fail".equalsIgnoreCase(sStat)) {
				sVal = PropertiesFileReader.getInstance().getPropFile(PropertyFileConstants.CONFIG_PROPERTIES)
						.getProperty("failedScreenshotName") + sScreenName + (ScreenCount) + ".png";
				totafilepath = screenPath + "" + sVal;
			}
			if (ConfigConstants.CLOUD.equalsIgnoreCase(server)) {
				WebDriver augmentedDriver = new Augmenter().augment(driver);
				screenshotFile = ((TakesScreenshot) augmentedDriver).getScreenshotAs(OutputType.FILE);
				FileUtils.copyFile(screenshotFile, new File(totafilepath));
			} else {
				screenshotFile = ((TakesScreenshot) driver).getScreenshotAs(OutputType.FILE);
				FileUtils.copyFile(screenshotFile, new File(totafilepath));
			}
			return sVal;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return sStat;
	}

	public void addTestStep(String description, Screenshot iScreenshot, Status sStatus) {

		TestStep a = new TestStep();
		switch (sStatus) {
		case PASS:
			a.setStepStatus("Pass");
			break;
		case INFO:
			a.setStepStatus("Info");
			break;
		case WARNING:
			a.setStepStatus("Warning");
			break;
		case FAIL:
			a.setStepStatus("Fail");
			break;
		default:
			a.setStepStatus("Fail");
			break;
		}

		a.setNo(String.valueOf(stepCounter++));
		a.setDateTime(DateUtils.getCurrentDate());
		a.setDescription(description);
		if (iScreenshot.equals(Screenshot.TRUE)) {
			try {
				if (sStatus.equals(Status.PASS) || sStatus.equals(Status.INFO) || sStatus.equals(Status.WARNING)) {
					a.setScreenshot(getScreen("pass"));
				} else {
					a.setScreenshot(getScreen("fail"));
				}
			} catch (Exception e) {
				LOGGER.error("Exception in test step : " + stepCounter++ + ",Message : " + e.getMessage());
			}

		}
		resultObject.getTestSteps().add(a);
	}

}
