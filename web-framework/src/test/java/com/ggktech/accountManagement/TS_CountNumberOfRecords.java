
package com.ggktech.accountManagement;

import com.ggktech.utils.TestCaseTemplate;

/**
 * Class containing test method for counting number of records.
 */
public class TS_CountNumberOfRecords extends TestCaseTemplate {

	public void testScript() {
		appLib.loginCRM(testEnv, scriptName);
		appLib.navigatingSearchLead();
		appLib.verifyPagination();
		appLib.logoutSalesCrm();
	}
}
