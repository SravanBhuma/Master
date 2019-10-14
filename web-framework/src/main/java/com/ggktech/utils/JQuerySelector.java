package com.ggktech.utils;


import java.io.Serializable;
import java.util.List;

import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.SearchContext;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.remote.RemoteWebElement;

/**
 * Class contains the functionality for finding element using jquery.
 */
public class JQuerySelector extends By implements Serializable {

	private static final long serialVersionUID = 1L;
	private final String jquerySelector;
	public JQuerySelector(String selector) {
		this.jquerySelector = selector;
	}
	@SuppressWarnings("unchecked")
	@Override
	public List<WebElement> findElements(SearchContext context) {
		return (List<WebElement>) findWithJQuery(context, true);
	}
	@Override
	public WebElement findElement(SearchContext context) {

		return (WebElement) findWithJQuery(context, false);
	}
	/**  
	 * @param context - To search DOM element using Web driver reference.
	 * @param returnList - Boolean value for returning list or not.
	 * @return Return a WebElement or List of WebElements based on the value of parameter 'returnList'
	 */
	private Object findWithJQuery(SearchContext context, boolean returnList) {
		String getArgument = returnList ? "" : "0";
		if (context instanceof RemoteWebElement) {
			WebDriver driver = ((RemoteWebElement) context).getWrappedDriver();
			return ((JavascriptExecutor) driver).executeScript("return $(arguments[0]).find('" +escape(jquerySelector) +"').get(" + getArgument + ");" , context);
		}
		return ((JavascriptExecutor) context).executeScript("return $('" + escape(jquerySelector) + "').get(" + getArgument + ");");
	}

	@Override
	public String toString() {
		return "By.jQuerySelector: " + jquerySelector;
	}

	/**
	 * @param s : String in which special character to be escaped.
	 * @return Return a string with escaping special characters.
	 */
	private static String escape(String s) {
		return s.replace("'", "\\'");
	}

	/**
	 * @param selector 
	 * @return
	 */
	public static By jQuery(final String selector) {
		if (selector == null)
			throw new IllegalArgumentException("Cannot find elements when the selector is null");
		return new JQuerySelector(selector);
	}  
}
