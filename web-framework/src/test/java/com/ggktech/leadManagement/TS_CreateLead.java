package com.ggktech.leadManagement;

import com.ggktech.utils.Screenshot;
import com.ggktech.utils.Status;
import com.ggktech.utils.TestCaseTemplate;

/**
 * Class file containing test method for creating lead.
 */
public class TS_CreateLead extends TestCaseTemplate {

	public void testScript() {
		appLib.getRep().report(Status.INFO, "Login into SalesCRM", Screenshot.FALSE);
		appLib.loginCRM(testEnv, scriptName);
		appLib.getRep().report(Status.INFO, "Navigate to Create Lead Page", Screenshot.FALSE);
		appLib.navigationToLead();
		appLib.getRep().report(Status.INFO, "Enter the Lead details and click on Save", Screenshot.FALSE);
		appLib.createAndSaveLead(testEnv, scriptName);
		appLib.getRep().report(Status.INFO, "Navigate to Search Lead Page", Screenshot.FALSE);
		appLib.navigatingSearchLead();
		appLib.getRep().report(Status.INFO, "Search Lead which was created before", Screenshot.FALSE);
		appLib.searchLeadDetails();
		appLib.getRep().report(Status.INFO, "Delete the lead and verify alert message", Screenshot.FALSE);
		appLib.deleteLead();
		appLib.getRep().report(Status.INFO, "Logout of Application", Screenshot.FALSE);
		appLib.logoutSalesCrm();

	}
}
