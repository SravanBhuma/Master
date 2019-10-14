package com.ggktech.utils;

/**
 * Class to run the test suite.
 */
public class TestSuiteExecutor {
	public static void main(String[] args) {
		if (args.length > 0) {
			TestNgXml.executorMethod(args[0]);
		} else {
			TestNgXml.executorMethod("filePathTestSuite");
		}
	}
}