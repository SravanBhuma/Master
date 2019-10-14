package com.ggktech.utils;

import java.io.*;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.commons.io.FileUtils;
import org.apache.log4j.Logger;
import org.apache.poi.ss.usermodel.Sheet;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.map.annotate.JsonSerialize.Inclusion;
import org.testng.Assert;
import com.ggktech.dao.ExcelDataHandler;
import com.ggktech.dao.PropertiesFileReader;
import com.ggktech.resultobjects.Module;
import com.ggktech.resultobjects.Modules;
import com.ggktech.resultobjects.PieChart;
import com.ggktech.resultobjects.TestCaseResult;
import com.ggktech.service.PublicLibrary;
import java.util.Map.Entry;

/**
 * Class which contains the functionality to merge the individual scripts xmls
 * to combined xml.
 */
public class ConsolidateJsonAndSendEmail {

	private static final Logger LOGGER = Logger.getLogger(ConsolidateJsonAndSendEmail.class.getName());
	private Properties configProperties;
	static String filePathTestSuite;
	private String consolidEmailablePath;
	private Sheet mainSheet;
	private static String projectName;
	long diffSeconds;
	long diffMinutes;
	long diffHours;
	long diffDays;
	private String sendMail;
	private static String toEmail;
	SendMail sendEmail = new SendMail();
	public static int PRETTY_FACTOR = 4;
	private int consolPassCount = 0;
	private int consolFailCount = 0;
	private Map<String, Set<String>> modulePassedScripts = new HashMap<>();;

	ConsolidateJsonAndSendEmail() {
	}

	/**
	 * @param args
	 * @throws Exception
	 */

	public static void main(String[] args) {
		ConsolidateJsonAndSendEmail mergeXml = new ConsolidateJsonAndSendEmail();
		mergeXml.generateJsonAndSendEmail(projectName, "", toEmail, true);
	}

	/**
	 * This method is used initializes the values.
	 */
	private void initializeValues() {
		try {
			PropertiesFileReader propFileReader = PropertiesFileReader.getInstance();
			configProperties = propFileReader.getPropFile(PropertyFileConstants.CONFIG_PROPERTIES);
			filePathTestSuite = configProperties.getProperty("filePathTestSuite");
			consolidEmailablePath = ConfigConstants.PARENTFOLDER_PATH
					+ configProperties.getProperty("consolidatedEmailablePath");
			mainSheet = ExcelDataHandler.getSheetData(SheetConstants.TEST_SUITE_SHEET, filePathTestSuite);
			projectName = mainSheet.getRow(ConfigConstants.PROJECT_NAME_ROW_INDEX)
					.getCell(ConfigConstants.PROJECT_NAME_COLUMN_INDEX).getStringCellValue();
			sendMail = mainSheet.getRow(ConfigConstants.SEND_MAIL_ROW_INDEX)
					.getCell(ConfigConstants.SEND_MAIL_COLUMN_INDEX).getStringCellValue();

			if ("y".equalsIgnoreCase(sendMail)) {
				toEmail = mainSheet.getRow(ConfigConstants.TO_MAIL_ROW_INDEX)
						.getCell(ConfigConstants.TO_MAIL_COLUMN_INDEX).getStringCellValue();
			}

		} catch (Exception e) {
			LOGGER.error("Exception in initializeValues method" + e);
		}
	}

	/**
	 * Method to merge scripts for module and send module wise Email
	 * 
	 * @param sModuleName
	 *            : Name of the module
	 * @param toEmailAddress
	 *            : To Email address for Email
	 * @throws Exception
	 */
	public void generateJsonAndSendEmail(String sModuleName, String testEnv, String toEmailAddress,
			Boolean isConsolidated) {
		initializeValues();
		try {
			File dir = null;
			ObjectMapper mapper = new ObjectMapper();
			mapper.setSerializationInclusion(Inclusion.NON_NULL);

			Set<String> set = new HashSet<String>();
			Set<String> mapSet = PublicLibrary.executionResultMap.keySet();
			Iterator<String> it = mapSet.iterator();
			while (it.hasNext()) {
				String key = it.next();
				if ((key.contains(sModuleName) && key.contains(testEnv)) || isConsolidated == true) {
					set.add(key);
				}
			}
			Map<String, ExecutionResult> moduleMap = new LinkedHashMap<String, ExecutionResult>();
			moduleMap.putAll(PublicLibrary.executionResultMap);
			moduleMap.keySet().retainAll(set);

			// sorting with execution start time
			LinkedList<Entry<String, ExecutionResult>> sortedEntryList = new LinkedList<Entry<String, ExecutionResult>>(
					moduleMap.entrySet());
			sortedEntryList.sort(
					(entry1, entry2) -> entry1.getValue().getStartTime().compareTo(entry2.getValue().getStartTime()));
			moduleMap.clear();

			sortedEntryList.forEach((eachEntry) -> moduleMap.put(eachEntry.getKey(), eachEntry.getValue()));

			int moduleLevelPassCount = 0;
			int moduleLevelFailCount = 0;
			for (Entry<String, ExecutionResult> entry : moduleMap.entrySet()) {
				moduleLevelPassCount = entry.getValue().getTcPassCount();
				consolPassCount = consolPassCount + moduleLevelPassCount;
				moduleLevelFailCount = entry.getValue().getTcFailCount();
				consolFailCount = consolFailCount + moduleLevelFailCount;
			}

			PieChart pie = new PieChart();
			pie.setFailureTitle("Failed TestScripts");
			pie.setSuccessTitle("Passed TestScripts");
			pie.setPiechartTitle("TestScripts Summary");

			if (isConsolidated) {
				pie.setFailureCount(String.valueOf(consolFailCount));
				pie.setSuccessCount(String.valueOf(consolPassCount));
				dir = new File(
						ConfigConstants.PARENTFOLDER_PATH + configProperties.getProperty("consolidatedResultPath"));
			} else {
				pie.setFailureCount(String.valueOf(moduleLevelFailCount));
				pie.setSuccessCount(String.valueOf(moduleLevelPassCount));
				dir = new File(ConfigConstants.PARENTFOLDER_PATH + configProperties.getProperty("tempTestResultsPath"));
			}
			List<Module> modules = new ArrayList<Module>();
			List<File> files = Arrays.asList(dir.listFiles());
			if (files.size() >= 1) {

				files = files.stream().filter((eachFile) -> eachFile.isDirectory())
						.sorted((f1, f2) -> Long.compare(f1.lastModified(), f2.lastModified()))
						.collect(Collectors.toList());

				for (File module : files) {
					Module moduleTag = appendModuleLevelTags(module);
					modules.add(moduleTag);
				}

				Modules modulesTag = new Modules();
				modulesTag.setModules(modules);
				modulesTag.setProjectName(projectName);
				modulesTag.setPie(pie);
				try {

					File htmlFile = new File(
							ConfigConstants.PARENTFOLDER_PATH + configProperties.getProperty("htmlFilePath"));
					FileReader fr = new FileReader(htmlFile);
					BufferedReader br = new BufferedReader(fr);
					String line = "", oldtext = "";
					while ((line = br.readLine()) != null) {
						oldtext += line + "\r\n";
					}
					br.close();
					String replacedtext = oldtext.replace("\'###jsondata###\'", mapper.writerWithDefaultPrettyPrinter()
							.writeValueAsString(modulesTag).replace("<", "&lt;").replace(">", "&gt;"));
					FileWriter writer = new FileWriter(
							consolidEmailablePath + configProperties.getProperty("propHTMLName"));
					writer.write(replacedtext);
					writer.close();
				} catch (Exception e) {
					e.printStackTrace();
				}
			} else {
				LOGGER.error("As no individual xml generated, stopping script execution");
				Assert.fail();
			}

			// Remove failed scripts which passed in rerun from module map
			Iterator it2 = moduleMap.entrySet().iterator();
			while (it2.hasNext()) {
				Map.Entry pair = (Entry) it2.next();
				String key = pair.getKey().toString();
				String[] keyVals = key.split("\\|");
				String updatedKey = String.format("%s-%s-%s", keyVals[0], keyVals[1].replaceAll(",", ""), keyVals[2]);

				Set<String> passedScripts = modulePassedScripts.get(updatedKey);
				if (passedScripts == null) {
					passedScripts = new HashSet<>();
				}

				ExecutionResult modRes = (ExecutionResult) pair.getValue();
				String failScripts = modRes.getFailScript();
				if (modRes.getFailScript() == null) {
					failScripts = "";
				}
				String[] failedScripts = failScripts.split(",");
				List<String> lFailedScripts = Arrays.asList(failedScripts);
				List<String> lFailedScriptsCopy = new ArrayList<>(lFailedScripts);
				for (String string : lFailedScripts) {
					if (passedScripts.contains(string)) {
						lFailedScriptsCopy.remove(string);
					}
				}
				String strFailedScripts = lFailedScriptsCopy.stream().collect(Collectors.joining(","));
				modRes.setFailScript(strFailedScripts == "" ? null : strFailedScripts);
				moduleMap.put(pair.getKey().toString(), modRes);
			}

			// Update status of execution in console(Can be used by CI tools to determine if
			// build is passed or failed)
			boolean buildStatus = true;
			for (Entry<String, ExecutionResult> entry : moduleMap.entrySet()) {
				if (entry.getValue().getTcFailCount() > 0) {
					buildStatus = false;
					break;
				}
			}
			if (buildStatus) {
				LOGGER.warn("BuildStatus = PASS");
			} else {
				LOGGER.warn("BuildStatus = FAIL");
			}

			if (isConsolidated) {
				if ("y".equalsIgnoreCase(sendMail)) {
					sendEmail.setIsConsolidated(isConsolidated);
					if (toEmailAddress.isEmpty()) {
						LOGGER.error(
								"\n\nEmail id is not provided for sending consolidated email reports. Please check Test Suite\n");
					} else {
						sendEmail.sendMail(moduleMap, toEmailAddress);
					}
				}
				archieveReports();
			} else {
				if (toEmailAddress.isEmpty()) {
					LOGGER.error("\n\nEmail id is not provided for sending email reports of '" + sModuleName
							+ "' module. Please check Test Suite\n");
				} else {
					sendEmail.sendMail(moduleMap, toEmailAddress);
				}
			}
			File emailTestResult = new File(
					ConfigConstants.PARENTFOLDER_PATH + configProperties.getProperty("tempTestResultsPath"));
			if (emailTestResult.listFiles().length > 0) {
				FileUtils.cleanDirectory(emailTestResult);
			}
		} catch (Exception e1) {
			LOGGER.error("Exception in mergeXmlandSendEmail() " + e1);
		}
	}

	/**
	 * Method to generated tags for module wise execution
	 * 
	 * @param module
	 *            : xml doc for each individual testscript
	 * @param doc
	 *            : XML Document file
	 * @param modulesElement
	 *            : module element in xml file
	 * @param docBuilder
	 *            : to create new document
	 * @throws ParseException
	 */
	private Module appendModuleLevelTags(File module) {
		try {

			ObjectMapper mapper = new ObjectMapper();
			Module moduleTag = new Module();
			moduleTag.setName(module.getName());
			int moduleSuccessCount = 0;
			int moduleFailureCount = 0;
			long diff = 0;
			DateFormat dateFormat = new SimpleDateFormat(ConfigConstants.DATE_TIME_FORMAT);

			if (module.isDirectory()) {
				String server = null;
				String testEnv = null;
				List<TestCaseResult> testCases = new ArrayList<TestCaseResult>();
				List<File> testScriptFiles = Arrays.asList(module.listFiles());
				testScriptFiles = testScriptFiles.stream()
						.sorted((f1, f2) -> Long.compare(f1.lastModified(), f2.lastModified()))
						.collect(Collectors.toList());
				for (File resultFile : testScriptFiles) {
					try {
						TestCaseResult result = mapper.readValue(resultFile, TestCaseResult.class);
						server = result.getServer();
						testEnv = result.getTestEnv();
						testCases.add(result);
					} catch (Exception e) {
						LOGGER.error("Exception in appendModuleLevelTags()" + e);
					}
				}
				List<Date> startTimeList = new ArrayList<Date>();
				List<Date> endTimeList = new ArrayList<Date>();

				Collections.sort(testCases, new Comparator<TestCaseResult>() {
					public int compare(TestCaseResult t1, TestCaseResult t2) {
						try {
							return new SimpleDateFormat(ConfigConstants.DATE_TIME_FORMAT).parse(t1.getStartTime())
									.compareTo(new SimpleDateFormat(ConfigConstants.DATE_TIME_FORMAT)
											.parse(t2.getStartTime()));
						} catch (ParseException e) {
							return 0;
						}
					}
				});

				testCases.forEach(c -> {
					try {
						startTimeList
								.add(new SimpleDateFormat(ConfigConstants.DATE_TIME_FORMAT).parse(c.getStartTime()));
						endTimeList.add(new SimpleDateFormat(ConfigConstants.DATE_TIME_FORMAT).parse(c.getEndTime()));
					} catch (ParseException e) {

					}
				});

				// Logic to get time of execution for parallel scripts
				Set<Long> set = new HashSet<Long>();
				for (int i = 0; i < startTimeList.size(); i++) {
					Long low = startTimeList.get(i).getTime() / 1000;
					Long up = endTimeList.get(i).getTime() / 1000;
					for (Long j = low + 1; j <= up; j++) {
						set.add(j);
					}
				}
				diff = set.size();
				diffSeconds = diff % 60;
				diffMinutes = diff / (60) % 60;
				diffHours = diff / (60 * 60) % 24;
				diffDays = diff / (24 * 60 * 60);
				String totalExecutionTime = diffDays + "days:" + diffHours + "hh:" + diffMinutes + "min:" + diffSeconds
						+ "secs";

				Collections.sort(startTimeList);
				Collections.sort(endTimeList);

				for (int i = 0; i < testCases.size(); i++) {
					int j = i + 1;
					testCases.get(i).setServer(null);
					testCases.get(i).setTestEnv(null);
					testCases.get(i).setId("TS_" + j);
				}

				// Logic to get module level success count and fail count. It will take only
				// distict testscript names for count since they will be rerun if there is
				// failure
				Map<String, Set<String>> passFailScripts = testCases.stream().filter(tc -> tc.getStatus() != null)
						.collect(Collectors.groupingBy(TestCaseResult::getStatus,
								Collectors.mapping(TestCaseResult::getName, Collectors.toSet())));

				modulePassedScripts.put(module.getName(), passFailScripts.get("Passed"));

				if (passFailScripts.get("Passed") == null) {
					passFailScripts.put("Passed", new HashSet<>());
				}
				if (passFailScripts.get("Failed") == null) {
					passFailScripts.put("Failed", new HashSet<>());
				}

				// Module level pass count
				moduleSuccessCount = passFailScripts.get("Passed").size();

				// Scripts that are failed in first run but passed in subsequent runs
				int passedInReruns = passFailScripts.get("Failed").stream()
						.filter(item -> passFailScripts.get("Passed").contains(item)).collect(Collectors.toSet())
						.size();

				// Scripts that are failed in all runs
				moduleFailureCount = passFailScripts.get("Failed").size() - passedInReruns;

				int failedInRuns = passFailScripts.get("Failed").size();

				moduleTag.setServer(server);
				moduleTag.setEnvironment(testEnv);
				moduleTag.setStartTime(dateFormat.format(startTimeList.get(0)));
				moduleTag.setEndTime(dateFormat.format(endTimeList.get(endTimeList.size() - 1)));
				moduleTag.setPassed(String.valueOf(moduleSuccessCount));
				moduleTag.setFailed(String.valueOf(moduleFailureCount));
				moduleTag.setStatus(failedInRuns > 0 ? "Failed" : "Passed");
				moduleTag.setTests(String.valueOf(moduleSuccessCount + moduleFailureCount));
				moduleTag.setExecutionTime(totalExecutionTime);
				moduleTag.setTestScripts(testCases);
				return moduleTag;
			}
		} catch (Exception e) {
			LOGGER.error("Exception in appendModuleLevelTags()" + e);
		}
		return null;
	}

	/**
	 * Method to archieve reports.
	 */
	public void archieveReports() {
		Calendar currentDate = Calendar.getInstance();
		SimpleDateFormat formatter = new SimpleDateFormat(ConfigConstants.DATE_TIME_FORMAT_WITHOUTSPACES);
		String sDateNow = formatter.format(currentDate.getTime());
		String archPath = ConfigConstants.PARENTFOLDER_PATH + configProperties.getProperty("archivedPath");
		File logFldr = new File(archPath + configProperties.getProperty("logsFolder") + "_" + sDateNow);
		File srcFolder = new File(
				ConfigConstants.PARENTFOLDER_PATH + configProperties.getProperty("consolidatedEmailablePath"));
		List<File> fileList = new ArrayList<>();
		getAllFiles(srcFolder, fileList);
		try {
			FileUtils.copyDirectory(srcFolder, logFldr);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Method to get all files in a directory.
	 * 
	 * @param dir
	 * @param fileList
	 */
	private void getAllFiles(File dir, List<File> fileList) {
		File[] files = dir.listFiles();
		for (File file : files) {
			fileList.add(file);
			if (file.isDirectory()) {
				getAllFiles(file, fileList);
			}
		}
	}

}
