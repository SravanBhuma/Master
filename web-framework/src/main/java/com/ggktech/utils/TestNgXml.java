/** *****************************************************Description**************************************************************
 Script Name						- 	TestNgXml
 Author							- 	GGK
 Purpose / Description     		- 	Suite to run test scripts from excel
 ********************************************************************************************************************************* */

package com.ggktech.utils;

import java.awt.Desktop;
import java.awt.Robot;
import java.awt.event.KeyEvent;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URI;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.Callable;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.Logger;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.testng.Assert;
import org.testng.TestNG;
import org.testng.xml.XmlClass;
import org.testng.xml.XmlSuite;
import org.testng.xml.XmlSuite.ParallelMode;
import org.testng.xml.XmlTest;
import org.yaml.snakeyaml.util.UriEncoder;

import com.ggktech.dao.ExcelDataHandler;
import com.ggktech.dao.PropertiesFileReader;
import com.ggktech.dao.TestSuiteComponent;
import com.ggktech.service.PublicLibrary;

/**
 * Class which contains the executorMethod() for starting the execution of the
 * suite.
 */
public class TestNgXml {

	private static final Logger LOGGER = Logger.getLogger(TestNgXml.class.getName());

	private PropertiesFileReader propFileReader;
	private Properties configProperties;
	private Properties intermediateProperties;
	private String filePathTestSuite;
	private ExcelDataHandler excelSheetReadWrite = new ExcelDataHandler();
	private int browserDetailsColumnNumber;
	private int browserDetailsColNumberInModules;
	private String execThreadCount;
	private String sModuleWiseEmail;
	private int startRow;
	private String projectName;
	private String sendMail;
	private String toEmail;
	private String reexecuteFailedScripts;

	/**
	 * Method in which initialization of all the values will be done and the
	 * execution will start.
	 */
	public static void executorMethod(String suiteName) {
		BasicConfigurator.configure();
		TestNgXml obj = new TestNgXml();
		obj.initializevalues(suiteName);
		obj.clearAndDeleteOldResults();
		ScheduledExecutorService exec = Executors.newSingleThreadScheduledExecutor();
		exec.scheduleAtFixedRate(new Runnable() {
			@Override
			public void run() {
				try {
					Robot robot = new Robot();
					int keyCode = KeyEvent.VK_NUM_LOCK;
					robot.keyPress(keyCode);
					robot.keyRelease(keyCode);
				} catch (Exception e) {
					LOGGER.error(e);
				}
			}
		}, 0, 60, TimeUnit.SECONDS);
		
		
		String filePath = ConfigConstants.PARENTFOLDER_PATH + "/Suite.xml";
		File file = new File(filePath);
		if(file.exists())
		{
			TestNG runner=new TestNG();
			List<String> suitefiles=new ArrayList<String>();
			suitefiles.add(filePath);
			runner.setTestSuites(suitefiles);
			runner.run();
		}
		else
		{
		obj.executeTests();
		}
		exec.shutdown();
		obj.reportandSendEmail();
		obj.showReportInBrowser();
	}

	/**
	 * Method to open html report in default browser after execution
	 */
	public void showReportInBrowser() {
		try {
			if (configProperties.getProperty("openReportInBrowser") != null
					&& configProperties.getProperty("openReportInBrowser").equals("true")) {
				Desktop.getDesktop()
						.browse(new URI(UriEncoder.encode(ConfigConstants.PARENTFOLDER_PATH.replace("\\", "/")
								+ configProperties.getProperty("consolidatedEmailablePath")
								+ configProperties.getProperty("propHTMLName"))));
			}
		} catch (Exception e) {
			e.printStackTrace();
			System.out.println("Unable to open browser");
		}
	}

	/**
	 * 
	 * 
	 */
	private void reportandSendEmail() {
		/** Merging all test Scripts into Single Report */
		ConsolidateJsonAndSendEmail mergeXml = new ConsolidateJsonAndSendEmail();
		mergeXml.generateJsonAndSendEmail(projectName, "", toEmail, true);
	}

	/**
	 * 
	 */
	private void clearAndDeleteOldResults() {
		deleteScreenshotsInXSLT(ConfigConstants.PARENTFOLDER_PATH + configProperties.getProperty("screenshot"));
		clearExcelResults();
		cleanTestScriptsXML();
	}

	/**
	 * Method to initialize required values for execution
	 */
	private void initializevalues(String suiteName) {
		try {
			propFileReader = PropertiesFileReader.getInstance();
			configProperties = propFileReader.getPropFile(PropertyFileConstants.CONFIG_PROPERTIES);
			filePathTestSuite = configProperties.getProperty(suiteName);
			if (filePathTestSuite == null) {
				throw new Exception("\n------------------------------------------\n" + "Invalid suite name : '"
						+ filePathTestSuite + "' : Please Provide a valid suite name"
						+ "\n------------------------------------------\n");
			}
			browserDetailsColumnNumber = ConfigConstants.BROWSER_DETAILS_COLUMN_NUMBER;
			browserDetailsColNumberInModules = ConfigConstants.BROWSER_DETAILS_COL_NUMBER_IN_MODULES;
			Sheet mainSheet = ExcelDataHandler.getSheetData(SheetConstants.TEST_SUITE_SHEET, filePathTestSuite);
			startRow = ConfigConstants.EXECUTION_START_ROW_NUMBER;
			execThreadCount = ExcelDataHandler.getCellValueFromSheet(mainSheet,
					ConfigConstants.EXEC_THREAD_COUNT_ROW_INDEX, ConfigConstants.EXEC_THREAD_COUNT_COLUMN_INDEX);
			sModuleWiseEmail = ExcelDataHandler.getCellValueFromSheet(mainSheet,
					ConfigConstants.MODULE_WISE_EMAIL_ROW_INDEX, ConfigConstants.MODULE_WISE_EMAIL_COLUMN_INDEX);
			projectName = mainSheet.getRow(ConfigConstants.PROJECT_NAME_ROW_INDEX)
					.getCell(ConfigConstants.PROJECT_NAME_COLUMN_INDEX).getStringCellValue();
			sendMail = mainSheet.getRow(ConfigConstants.SEND_MAIL_ROW_INDEX)
					.getCell(ConfigConstants.SEND_MAIL_COLUMN_INDEX).getStringCellValue();
			int cellTypeReExcFailedScripts = mainSheet.getRow(ConfigConstants.REEXECUTE_FAILED_SCRIPTS_ROW_INDEX)
					.getCell(ConfigConstants.REEXECUTE_FAILED_SCRIPTS_COLUMN_INDEX).getCellType();

			if (CellType.STRING.getCode() == cellTypeReExcFailedScripts) {
				reexecuteFailedScripts = mainSheet.getRow(ConfigConstants.REEXECUTE_FAILED_SCRIPTS_ROW_INDEX)
						.getCell(ConfigConstants.REEXECUTE_FAILED_SCRIPTS_COLUMN_INDEX).getStringCellValue();
			} else {
				Double dreexecuteFailedScripts = mainSheet.getRow(ConfigConstants.REEXECUTE_FAILED_SCRIPTS_ROW_INDEX)
						.getCell(ConfigConstants.REEXECUTE_FAILED_SCRIPTS_COLUMN_INDEX).getNumericCellValue();
				reexecuteFailedScripts = String.valueOf(dreexecuteFailedScripts.intValue());
			}
			if ("y".equalsIgnoreCase(sendMail)) {
				toEmail = mainSheet.getRow(ConfigConstants.TO_MAIL_ROW_INDEX)
						.getCell(ConfigConstants.TO_MAIL_COLUMN_INDEX).getStringCellValue();
			}

		} catch (Exception e) {
			LOGGER.error("Exception in initializevalues method,please check data in testsuite excel" + e);
			throw new RuntimeException(e.getMessage());
		}
	}

	/**
	 * Method to clear previous execution results from excel
	 * 
	 * @throws Exception
	 */
	private void clearExcelResults() {
		try {
			String filePath = ConfigConstants.PARENTFOLDER_PATH + filePathTestSuite;
			Workbook workbook = ExcelDataHandler.getWorkBook(filePath);
			for (int i = 0; i < workbook.getNumberOfSheets(); i++) {
				Sheet worksheet = workbook.getSheetAt(i);
				if (worksheet.getSheetName().contains(SheetConstants.TEST_SUITE_SHEET))
					continue;
				Iterator<Row> iterator = worksheet.iterator();
				while (iterator.hasNext()) {
					Row row = iterator.next();
					for (int cellNum = browserDetailsColNumberInModules; cellNum < row.getLastCellNum(); cellNum++) {
						Cell cell = row.getCell(cellNum);
						if (cell != null) {
							row.removeCell(row.getCell(cellNum));
						}
					}
				}
			}
			FileOutputStream fileOuta = new FileOutputStream(filePath);
			workbook.write(fileOuta);
			fileOuta.close();
			workbook.close();
		} catch (Exception e) {
			LOGGER.error("Exception in clearExcelResults method, please check data in testsuite excel" + e);
			throw new RuntimeException();
		}
	}


	/**
	 * Method to start execution
	 */
	private void executeTests() {
		try {
			populateModulesTag();
		} catch (Exception e) {
			System.exit(1);
		}
	}

	/**
	 * Method to delete screenshot in given folder
	 * 
	 * @param path
	 *            : path where screenshots need to be deleted
	 */
	private void deleteScreenshotsInXSLT(String path) {
		try {
			File file = new File(path);
			if (file.isDirectory()) {
				File[] myFiles = file.listFiles();
				for (File image : myFiles) {
					if (image.getName().endsWith(configProperties.getProperty("screenshotFormat"))) {
						image.delete();
					}
				}
			}
		} catch (Exception e) {
			LOGGER.error("Exception in deleteScreenshotsInXSLT() method" + e);
		}
	}

	/**
	 * Method to create local, grid and remote execution lists.
	 * 
	 */
	private void populateModulesTag() {
		try {
			Sheet mainSheet = ExcelDataHandler.getSheetData(SheetConstants.TEST_SUITE_SHEET, filePathTestSuite);
			final List<TestSuiteComponent> localList = new ArrayList<>();
			final List<TestSuiteComponent> cloudList = new ArrayList<>();
			final List<TestSuiteComponent> gridList = new ArrayList<>();
			for (int i = startRow; i <= mainSheet.getLastRowNum(); i++) {
				Row row = mainSheet.getRow(i);
				String sRunStatus = row.getCell(2).getStringCellValue();
				if (("y").equalsIgnoreCase(sRunStatus)) {
					populateListsBasedOnServer(row, localList, cloudList, gridList);
				}
			}
			String moduleWiseParellelCount = configProperties.getProperty("moduleWiseParellelCount");
			ScheduledExecutorService scheduledExecutorServiceRemote = Executors.newScheduledThreadPool(
					Integer.parseInt(moduleWiseParellelCount != null ? moduleWiseParellelCount : "1"));
			ScheduledExecutorService scheduledExecutorServiceLocal = Executors.newSingleThreadScheduledExecutor();

			if (!localList.isEmpty()) {
				scheduleModuleLevelExecution(localList, scheduledExecutorServiceLocal);
			}
			if (!cloudList.isEmpty()) {
				scheduleModuleLevelExecution(cloudList, scheduledExecutorServiceRemote);
			}
			if (!gridList.isEmpty()) {
				scheduleModuleLevelExecution(gridList, scheduledExecutorServiceRemote);
			}

			scheduledExecutorServiceLocal.shutdown();
			scheduledExecutorServiceRemote.shutdown();

			while (!scheduledExecutorServiceLocal.isTerminated() || !scheduledExecutorServiceRemote.isTerminated()) {
				if (scheduledExecutorServiceLocal.isTerminated() && scheduledExecutorServiceRemote.isTerminated()) {
					break;
				}
			}
		} catch (Exception e) {
			LOGGER.error("Exception in populateModulesTag()" + e);
			throw new RuntimeException();
		}
	}

	/**
	 * Method to schedule the execution
	 * 
	 * @param moduleList
	 * @param scheduledExecutorService
	 */
	private void scheduleModuleLevelExecution(List<TestSuiteComponent> moduleList,
			ScheduledExecutorService scheduledExecutorService) {
		ScheduledFuture<Void> scheduledFuture = null;
		try {
			for (TestSuiteComponent testSuiteComponent : moduleList) {
				String exeTime = testSuiteComponent.getExecutionStartTime();
				long diffSeconds = 0;
				if (!("".equals(exeTime) || exeTime == null)) {
					SimpleDateFormat formatter = new SimpleDateFormat(ConfigConstants.DATE_TIME_FORMAT);
					long diff = 0;
					try {
						formatter.setLenient(false);
						diff = formatter.parse(exeTime).getTime() - System.currentTimeMillis();
					} catch (ParseException e) {
						LOGGER.error("Exception in ScheduleModuleLevelExecution() :" + e);
						Assert.fail();
					}
					diffSeconds = diff / 1000;
				}

				scheduledFuture = scheduledExecutorService.schedule(createThreads(testSuiteComponent), diffSeconds,
						TimeUnit.SECONDS);
			}
			scheduledFuture.get();

		} catch (Exception e) {
			LOGGER.error("Exception in scheduleModuleLevelExecution()" + e);
			throw new RuntimeException();
		}
	}

	/**
	 * @param row
	 *            : Row where module Runstatus is Yes
	 * @param localList
	 *            : array list to store the list of testcases scheduled to be run on
	 *            local browser
	 * @param remoteList
	 *            : array list to store the list of testcases scheduled to be run on
	 *            remote browser
	 * @param gridList
	 *            : array list to store the list of testcases scheduled to be run on
	 *            grid
	 */
	private void populateListsBasedOnServer(Row row, List<TestSuiteComponent> localList,
			List<TestSuiteComponent> remoteList, List<TestSuiteComponent> gridList) {
		try {
			String sModuName = row.getCell(ConfigConstants.MODULE_NAME_COLUMN_INDEX).getStringCellValue();
			String server = row.getCell(ConfigConstants.SERVER_COLUMN_INDEX).getStringCellValue().toUpperCase();
			String parallelMode = row.getCell(ConfigConstants.PARALLEL_MODE_COLUMN_INDEX).getStringCellValue();
			String executionTime = row.getCell(ConfigConstants.EXECUTION_TIME_COLUMN_INDEX).getStringCellValue();
			String testEnv = row.getCell(ConfigConstants.TEST_ENV_COLUMN_INDEX).getStringCellValue();
			String toModuleEmail = "";

			if ("y".equalsIgnoreCase(sModuleWiseEmail)) {
				toModuleEmail = row.getCell(ConfigConstants.TO_MODULE_EMAIL_COLUMN_INDEX).getStringCellValue();
			}

			// String appURL = configProperties.getProperty(testEnv.toUpperCase());
			if (testEnv == null) {
				LOGGER.error("Check whether test Environment defined properly in test suite");
				Assert.fail();
			}
			TestSuiteComponent testSuiteComponentObj = new TestSuiteComponent();
			testSuiteComponentObj.setModuleName(sModuName);
			testSuiteComponentObj.setServer(server);
			testSuiteComponentObj.setParallelMode(parallelMode);
			testSuiteComponentObj.setExecutionStartTime(executionTime);
			testSuiteComponentObj.setTestEnv(testEnv);
			testSuiteComponentObj.setToEmailAddress(toModuleEmail);
			if (browserDetailsColumnNumber == row.getLastCellNum()) {
				LOGGER.error("Check Browser details in TestSuite file");
				Assert.fail();
			} else {
				for (int i = browserDetailsColumnNumber; i < row.getLastCellNum(); i++) {
					String browserConfig = row.getCell(i).getStringCellValue();
					if (StringUtils.isNotEmpty(browserConfig)) {
						testSuiteComponentObj.getBrowsers().add(browserConfig);
						Sheet sheet = ExcelDataHandler.getSheetData(sModuName,
								configProperties.getProperty("filePathTestSuite"));
						Cell headerCell = excelSheetReadWrite.getCellFromRow(row, browserConfig, sheet);
						/** Creating heading as same as browser parameters */
						if (headerCell == null) {
							Row headerRow = sheet.getRow(ConfigConstants.HEADER_ROW_INDEX);
							ExcelDataHandler.setSheetData(sModuName, ConfigConstants.HEADER_ROW_INDEX,
									headerRow.getPhysicalNumberOfCells(), browserConfig,
									configProperties.getProperty("filePathTestSuite"));
						}
					}
				}
				switch (server) {
				case ConfigConstants.CLOUD:
					remoteList.add(testSuiteComponentObj);
					break;
				case ConfigConstants.GRID:
					gridList.add(testSuiteComponentObj);
					break;
				default:
					localList.add(testSuiteComponentObj);
				}
			}
		} catch (Exception e) {
			LOGGER.error("Exception in populateListsBasedOnServer" + e);
			throw new RuntimeException();
		}
	}

	/**
	 * Method to create thread
	 * 
	 * @param list
	 *            : testsuiteComponent which is having all test environment details
	 * @return
	 */
	private Callable<Void> createThreads(final TestSuiteComponent testSuiteComponent) {
		return new Callable<Void>() {
			@Override
			public Void call() {
				try {
					executeModuleTests(testSuiteComponent.getModuleName(), testSuiteComponent.getBrowsers(),
							testSuiteComponent.getServer(), testSuiteComponent.getParallelMode(),
							testSuiteComponent.getTestEnv(), testSuiteComponent.getToEmailAddress());
					return null;
				} catch (Exception e) {
					throw new RuntimeException();
				}
			}
		};
	}

	/**
	 * @param suite
	 *            : Suite name
	 * @param testName
	 *            : Testscript name
	 * @param sModuName
	 *            : Module name
	 * @param browserConfig
	 *            : Run Environment i.e. browser, browserversion,os, osversion
	 * @param server
	 *            : remote or local or grid
	 * @param testEnv
	 *            : Test Environment i.e QA, UAT, STAGING
	 * @param classes
	 *            : list of classes adding to test
	 * @return
	 */
	private XmlTest createXmlTest(XmlSuite suite, String testName, String sModuName, String browserConfig,
			String server, String testEnv, List<XmlClass> classes) {
		String[] browser = browserConfig.split(",");
		if (browser.length > 0) {
			XmlTest test1 = new XmlTest(suite);
			test1.setName(testName);
			/** Set Test */
			test1.addParameter("browserType", browser[0]);
			test1.addParameter("browserVersion", browser[1]);
			test1.addParameter("os", browser[2]);
			test1.addParameter("osVersion", browser[3]);
			test1.addParameter("sheetName", sModuName);
			test1.addParameter("server", server);
			test1.addParameter("testEnv", testEnv);
			test1.setClasses(classes);
			/** Add classes to test */
			return test1;
		}
		return null;
	}

	/**
	 * Method to execute Module level test cases
	 * 
	 * @param sModuName
	 *            : Module name
	 * @param browsers
	 *            : Browsers list
	 * @param server
	 *            : remote or local or grid
	 * @param parallelMode
	 *            : yes or no
	 * @param testEnv
	 *            : Test Environment i.e QA, UAT, STAGING
	 * @param toEmailAddress
	 *            : Email addresses for Module wise Report
	 */
	private void executeModuleTests(String sModuName, List<String> browsers, String server, String parallelMode,
			String testEnv, String toEmailAddress) {
		XmlSuite suite = new XmlSuite();
		try {
			int threadCount = execThreadCount.isEmpty() ? 1 : Integer.parseInt(execThreadCount);
			if (threadCount <= 0) {
				suite.setThreadCount(1);
			} else {
				suite.setThreadCount(threadCount);
			}
			if ("y".equalsIgnoreCase(parallelMode)) {
				suite.setParallel(ParallelMode.TESTS);
			}
			Sheet childSheet = ExcelDataHandler.getSheetData(sModuName, filePathTestSuite);
			Iterator<Row> childIterator = childSheet.iterator();
			while (childIterator.hasNext()) {
				List<XmlClass> classes = new ArrayList<>();
				Row row = childIterator.next();
				if (row != null && row.getRowNum() > 0 && ("y").equalsIgnoreCase(row.getCell(3).getStringCellValue())) {

					classes.add(new XmlClass(
							getClass(row.getCell(ConfigConstants.SCRIPT_NAME_COLUMN_INDEX).getStringCellValue())));

					for (String browserConfig : browsers) {
						if (StringUtils.isNotBlank(browserConfig)) {
							createXmlTest(suite,
									sModuName + browserConfig
											+ row.getCell(ConfigConstants.SCRIPT_NAME_COLUMN_INDEX)
													.getStringCellValue(),
									sModuName, browserConfig, server, testEnv, classes);
						}
					}
				}
			}

			childSheet.getWorkbook().close();
		} catch (Exception e) {
			LOGGER.error("executeModuleTests, issue while adding classes to test" + e);
			throw new RuntimeException();
		}
		/** New list for the Suite */
		List<XmlSuite> suites = new ArrayList<>();
		suites.add(suite);
		System.out.println("Here is the suite"+suite);
		/** Add suite to the list */
		TestNG tng = new TestNG();
		System.out.println(suites);
		tng.setXmlSuites(suites);
		/** Starts script execution and creating the xml */
		tng.run();

		/** Rerun failed scripts */

		rerunFailedScripts(reexecuteFailedScripts, parallelMode);

		/** trigger email if module wise reporting is yes */
		if (sModuleWiseEmail.equalsIgnoreCase("y")) {
			ConsolidateJsonAndSendEmail mergexml = new ConsolidateJsonAndSendEmail();
			mergexml.generateJsonAndSendEmail(sModuName, testEnv, toEmailAddress, false);
		}
	}

	/**
	 * Method to clean test result folder in emailable, consolidated folders
	 */
	private void cleanTestScriptsXML() {
		try {
			File dir1 = new File(
					ConfigConstants.PARENTFOLDER_PATH + configProperties.getProperty("tempTestResultsPath"));
			if (!dir1.exists()) {
				dir1.mkdirs();
			} else {
				FileUtils.cleanDirectory(dir1);
			}
			File dir2 = new File(
					ConfigConstants.PARENTFOLDER_PATH + configProperties.getProperty("consolidatedResultPath"));
			FileUtils.cleanDirectory(dir2);
		} catch (Exception e) {
			LOGGER.error(
					"Exception while clearing emailble and consolidated test results folder in cleanTestScriptsXML()"
							+ e);
			throw new RuntimeException();
		}
	}

	/**
	 * @return propFileReader object to read the values from property file
	 */
	public PropertiesFileReader getPropFileReader() {
		return propFileReader;
	}

	/**
	 * Method to get all classes
	 * 
	 * @param className
	 *            : Name of the class
	 * @return
	 */
	public Class<?> getClass(String className) {
		Class<?> class1 = null;
		List<File> scriptFiles = PublicLibrary.listScripts(
				ConfigConstants.PARENTFOLDER_PATH + ConfigConstants.SCRIPTS_PATH + ConfigConstants.SCRIPTS_PACKAGE);
		for (int i = 0; i < scriptFiles.size(); i++) {
			String className1 = scriptFiles.get(i).getName().replaceAll(".java", "");
			if (className.equals(className1)) {
				try {
					System.out.println(ConfigConstants.SCRIPTS_PACKAGE.replace("/", ".")+ scriptFiles.get(i).getParentFile().getName() + "." + className);
					class1 = Class.forName(ConfigConstants.SCRIPTS_PACKAGE.replace("/", ".")
							+ scriptFiles.get(i).getParentFile().getName() + "." + className);
					break;
				} catch (final ClassNotFoundException e) {
					LOGGER.error(e);
				}
			}
		}
		if (class1 != null) {
			return class1;
		} else {
			LOGGER.error(className + " class not found");
			return null;
		}
	}

	/**
	 * This method will re-execute the failed scripts
	 * 
	 * @param retryCount
	 *            : No of times we want to execute the failed scripts
	 * @param parallelMode
	 *            : Parallel mode or not (y/n)
	 */
	public void rerunFailedScripts(String retryCount, String parallelMode) {
		int iRetryCount = 0;
		try {
			iRetryCount = Integer.parseInt(retryCount);
		} catch (NumberFormatException e) {
			LOGGER.error("Please enter a valid integer value for 'Re-execute Failed Scripts' in Suite File.");
			return;
		}

		for (int count = 0; count < iRetryCount; count++) {
			CopyOnWriteArrayList<Map<String, String>> failedScripts = FailedTests.getFailedScripts();
			failedScripts.toString();
			if (failedScripts.isEmpty()) {
				return;
			} else {
				XmlSuite suite = new XmlSuite();
				int threadCount = execThreadCount.isEmpty() ? 1 : Integer.parseInt(execThreadCount);
				if (threadCount <= 0) {
					suite.setThreadCount(1);
				} else {
					suite.setThreadCount(threadCount);
				}
				if ("y".equalsIgnoreCase(parallelMode)) {
					suite.setParallel(ParallelMode.TESTS);
				}
				for (Map<String, String> script : failedScripts) {
					List<XmlClass> classes = new ArrayList<>();
					String scriptName = script.get("testScriptName");
					String moduleName = script.get("moduleName");
					String browserConfig = script.get("browserConfig");
					classes.add(new XmlClass(getClass(scriptName)));
					createXmlTest(suite, moduleName + browserConfig + scriptName, moduleName, browserConfig,
							script.get("server"), script.get("testenv"), classes);
				}

				/** Clear failed scripts */
				FailedTests.clearFailedScripts();

				/** New list for the Suite */
				List<XmlSuite> suites = new ArrayList<>();
				suites.add(suite);
				/** Add suite to the list */
				TestNG tng = new TestNG();

				tng.setXmlSuites(suites);

				/** Starts script execution and creating the xml */
				tng.run();

				if (count == (iRetryCount - 1)) {
					FailedTests.clearFailedScripts();
				}
			}
		}
	}

}
