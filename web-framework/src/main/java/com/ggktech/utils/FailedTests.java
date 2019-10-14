package com.ggktech.utils;

import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;

public class FailedTests {

	private static final CopyOnWriteArrayList<Map<String, String>> failedScripts = new CopyOnWriteArrayList<>();

	public static void setFailedScripts(Map<String, String> testScript) {
		failedScripts.add(testScript);
	}

	public static CopyOnWriteArrayList<Map<String, String>> getFailedScripts() {
		return failedScripts;
	}

	public static void clearFailedScripts() {
		failedScripts.clear();
	}

}
