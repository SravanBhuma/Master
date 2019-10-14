package com.ggktech.utils;

public class ConfigConstants {

	// Locators
	public static final String XPATH = "By.xpath\\(";
	public static final String ID = "By.id\\(";
	public static final String NAME = "By.name\\(";
	public static final String LINK_TEXT = "By.linkText\\(";
	public static final String CLASS_NAME = "By.className\\(";
	public static final String CSS_SELECTOR = "By.cssSelector\\(";
	public static final String TAG_NAME = "By.tagName\\(";
	public static final String PARTIAL_LINK_TEXT = "By.partialLinkText\\(";
	public static final String JQUERY = "JQuerySelector.jQuery\\(";

	public static final String XPATH_LOC = "By.xpath(";
	public static final String ID_LOC = "By.id(";
	public static final String NAME_LOC = "By.name(";
	public static final String LINK_TEXT_LOC = "By.linkText(";
	public static final String CLASS_NAME_LOC = "By.className(";
	public static final String CSS_SELECTOR_LOC = "By.cssSelector(";
	public static final String TAG_NAME_LOC = "By.tagName(";
	public static final String PARTIAL_LINK_TEXT_LOC = "By.partialLinkText(";

	// XML Tags
	public static final String TEST_SCRIPT_LXML = "TestScript";
	public static final String NAME_LXML = "Name";
	public static final String TEST_STEP_LXML = "TestStep";
	public static final String DESC_LXML = "Description";
	public static final String SCREENSHOT_LXML = "ScreenShot";
	public static final String DATE_TIME_LXML = "DateTime";
	public static final String TEST_STEPS_LXML = "TestSteps";
	public static final String STATUS_LXML = "Status";
	public static final String NUM_LXML = "No";
	public static final String ID_LXML = "Id";
	public static final String ID_LNODE = "TS_";
	public static final String FAILED_LXML = "Failed";
	public static final String PASSED_LXML = "Passed";
	public static final String STEPS_LXML = "# Steps";
	public static final String END_TIME_LXML = "EndTime";
	public static final String TOTAL_TIME_LXML = "TotalTime";
	public static final String START_TIME_LXML = "StartTime";
	public static final String MODULES_LXML = "Modules";
	public static final String PROJ_NAME_LXML = "ProjectName";
	public static final String MODULE_LXML = "Module";
	public static final String TEST_SCRIPTS_LXML = "TestScripts";
	public static final String TESTS_LXML = "Tests";
	public static final String EXE_TIME_LXML = "ExecutionTime";
	public static final String SERVER_LXML = "Server";
	public static final String TESTENVIRONMENT_LXML = "testEnv";
	public static final String PIE_CHART_LXML = "PieChart";
	
	// Public library constants
	public static final String BUILD_INFO = "Build info:";
	public static final String WEBTABLEDIM_HEIGHT = "height";
	public static final String WEBTABLEDIM_WIDTH = "width";

	// Date and Time constants
	public static final String DATE_TIME_FORMAT = "dd-MM-yyyy HH:mm:ss";
	public static final String WGL_DATE_TIME_FORMAT = "YYYY-MM-dd";
	public static final String WGLCHORME_DATE_TIME_FORMAT = "MM/dd/YYYY";
	public static final String TIME_ZONE_ID = "UTC";
	public static final String DATE_TIME_FORMAT_WITHOUTSPACES = "dd_MM_yyyy_HH_mm_ss_uuuu";

	// Browser and Server Constants
	public static final String CHROME = "chrome";
	public static final String FIREFOX = "firefox";
	public static final String IE = "internet explorer";
	public static final String EDGE = "microsoft edge";
	public static final String SAFARI = "safari";
	public static final String OPERA = "opera";

	public static final String CLOUD = "CLOUD";
	public static final String GRID = "GRID";
	public static final String LOCAL = "LOCAL";
	// Statuses
	public static final int PASSED = 3;
	public static final int FAILED = 4;
	public static final int IN_PROGRESS = 2;
	public static final int NOT_STARTED = 1;
	public static final int COMPLETED = 5;
	public static final String YES = "Yes";
	public static final String NO = "No";

	// Absolute Path
	public static final String PARENTFOLDER_PATH = System.getProperty("user.dir");
	public static final String SCRIPTS_PATH = "/src/test/java/";
	public static final String SCRIPTS_PACKAGE = "com/ggktech/";
	public static final String RESOURCE_PATH = "/src/main/resources/";
	public static final String All_SCREENSHOT = "ALL";
	public static final String FAIL_SCREENSHOT = "FAIL";

	// User Agent for http connection
	public static final String USERAGENT = "Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/537.11 (KHTML, like Gecko) Chrome/23.0.1271.95 Safari/537.11";

	// Browser and Platform Details
	public static final int BROWSER_DETAILS_COLUMN_NUMBER = 8;
	public static final int EXECUTION_START_ROW_NUMBER = 11;
	public static final int BROWSER_DETAILS_COL_NUMBER_IN_MODULES = 4;

	public static final int EXEC_THREAD_COUNT_ROW_INDEX = 7;
	public static final int EXEC_THREAD_COUNT_COLUMN_INDEX = 3;
	public static final int SEND_MAIL_ROW_INDEX = 2;
	public static final int SEND_MAIL_COLUMN_INDEX = 1;
	public static final int TO_MAIL_ROW_INDEX = 4;
	public static final int TO_MAIL_COLUMN_INDEX = 1;
	public static final int MODULE_WISE_EMAIL_ROW_INDEX = 3;
	public static final int MODULE_WISE_EMAIL_COLUMN_INDEX = 1;
	public static final int TAKE_SCREEN_SHOT_ROW_INDEX = 8;
	public static final int TAKE_SCREEN_SHOT_COLUMN_INDEX = 3;

	public static final int RUN_STATUS_COLUMN_INDEX = 2;
	public static final int HEADER_ROW_INDEX = 0;
	public static final int SCRIPT_RUN_STATUS_COLUM_INDEX = 3;
	public static final int SCRIPT_NAME_COLUMN_INDEX = 1;
	public static final int MODULE_NAME_COLUMN_INDEX = 1;
	public static final int SERVER_COLUMN_INDEX = 3;
	public static final int PARALLEL_MODE_COLUMN_INDEX = 4;
	public static final int EXECUTION_TIME_COLUMN_INDEX = 5;
	public static final int TEST_ENV_COLUMN_INDEX = 7;
	public static final int TO_MODULE_EMAIL_COLUMN_INDEX = 6;

	public static final int PROJECT_NAME_ROW_INDEX = 0;
	public static final int PROJECT_NAME_COLUMN_INDEX = 1;
	public static final int PROJECT_VERSION_ROW_INDEX = 0;
	public static final int PROJECT_VERSION_COLUMN_INDEX = 5;
	public static final int SUITE_NAME_ROW_INDEX = 0;
	public static final int SUITE_NAME_COLUMN_INDEX = 3;

	// publiclibrary
	public static final int MODULE_WISE_REPORT_ROW_INDEX = 3;
	public static final int MODULE_WISE_REPORT_COLUMN_INDEX = 1;
	public static final int REMOTE_URL_ROW_INDEX = 2;
	public static final int REMOTE_URL_COLUMN_INDEX = 3;
	public static final int REMOTE_USER_NAME_ROW_INDEX = 3;
	public static final int REMOTE_USER_NAME_COLUMN_INDEX = 3;
	public static final int REMOTE_USER_KEY_ROW_INDEX = 4;
	public static final int REMOTE_USER_KEY_COLUMN_INDEX = 3;
	public static final int HUB_URL_ROW_INDEX = 5;
	public static final int HUB_URL_COLUMN_INDEX = 3;
	public static final int REEXECUTE_FAILED_SCRIPTS_ROW_INDEX = 6;
	public static final int REEXECUTE_FAILED_SCRIPTS_COLUMN_INDEX = 3;
	public static final int PAGE_LOAD_TIME_ROW_INDEX = 5;
	public static final int PAGE_LOAD_TIME_COLUMN_INDEX = 1;
	public static final int REPORT_BROKEN_LINKS_ROW_INDEX = 6;
	public static final int REPORT_BROKEN_LINKS_COLUMN_INDEX = 1;
	public static final int REPORT_JAVA_SCRIPT_ERRORS_ROW_INDEX = 7;
	public static final int REPORT_JAVA_SCRIPT_ERRORS_COLUMN_INDEX = 1;
	public static final int HEADLESS_EXECUTION_ROW_INDEX = 8;
	public static final int HEADLESS_EXECUTION_COLUMN_INDEX = 1;

	private ConfigConstants() {
	}

}
