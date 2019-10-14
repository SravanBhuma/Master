package com.ggktech.accountManagement;

import javax.xml.transform.TransformerException;

import com.ggktech.utils.PropertyFileConstants;
import com.ggktech.utils.TestCaseTemplate;

public class TS_SelectSearchLink extends TestCaseTemplate {

	public void testScript() {
		// Robot rb=new Robot();
		// rb.keyPress(1);
		// rb.keyRelease(1);
		try {
			appLib.setValue(appLib.byLocator("sSearchTab", PropertyFileConstants.OR_PROPERTIES), "ggk technologies",
					appLib.getWaitTime(), "Search Tab");
			// appLib.delay(MiscConstants.S_DELAY);
			appLib.clickElement(appLib.byLocator("sSelectValueFromList", PropertyFileConstants.OR_PROPERTIES),
					appLib.getWaitTime(), "Search Button");
			appLib.verifyPage("ggk technologies - Google Search", "");
			// appLib.delay(MiscConstants.S_DELAY);
			// List<WebElement> resultLinks =
			// appLib.getDriver().findElements(appLib.byLocator("sSearchResultLinks",
			// PropertyFileConstants.OR_PROPERTIES));
			// appLib.getRep().report("Count of result links", Status.PASS, "The total
			// number of result links are : "+resultLinks.size(), Screenshot.FALSE);
			//
			// for (WebElement webElement : resultLinks) {
			// if(webElement.getAttribute("href").contains("ggktech")) {
			// webElement.click();
			// break;
			// };
			// }
			// appLib.delay(MiscConstants.S_DELAY);

			appLib.setValue(appLib.byLocator("sSearchTab", PropertyFileConstants.OR_PROPERTIES), "ggk technologies",
					appLib.getWaitTime(), "Search Tab");
			// appLib.delay(MiscConstants.S_DELAY);
			appLib.clickElement(appLib.byLocator("sSelectValueFromList", PropertyFileConstants.OR_PROPERTIES),
					appLib.getWaitTime(), "Search Button");
			appLib.verifyPage("ggk technologies - Google Search", "");
			// appLib.delay(MiscConstants.S_DELAY);
			// List<WebElement> resultLinks =
			// appLib.getDriver().findElements(appLib.byLocator("sSearchResultLinks",
			// PropertyFileConstants.OR_PROPERTIES));
			// appLib.getRep().report("Count of result links", Status.PASS, "The total
			// number of result links are : "+resultLinks.size(), Screenshot.FALSE);
			//
			// for (WebElement webElement : resultLinks) {
			// if(webElement.getAttribute("href").contains("ggktech")) {
			// webElement.click();
			// break;
			// };
			// }
			// appLib.delay(MiscConstants.S_DELAY);
		} catch (Exception e) {
			LOGGER.info(e);
			appLib.getRep().reportinCatch(e);
		} finally {
			/** Logging out of application */
			try {
				appLib.createReportAndUpdateExcel(browserType, browserVersion, os, osVersion, testEnv, scriptName,
						sheetName);
			} catch (TransformerException e) {
				appLib.getRep().reportinCatch(e);
			}
			appLib.getDriver().quit();
		}
	}
}
