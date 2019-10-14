package com.ggktech.utils;

import java.net.HttpURLConnection;
import java.net.URL;

import com.ggktech.service.PublicLibrary;

/**
 * Class contains runnable method for finding the status of the link.
 */
public class LinkStatusCheck extends PublicLibrary implements Runnable  {

	private String linkText;
	private String linkUrl;
	private String browser;
	private String name;
	

	/**
	 * @param link : Link to be checked.
	 * @param text : Text of the link.
	 * @param browserName : Name of the browser.
	 * @param script : Name of the script.
	 */
	public LinkStatusCheck(String link,String text,String browserName, String script){

		this.linkText=text;
		this.linkUrl=link;
		this.browser=browserName;
		this.name=script;
		
	}
	
	/* (non-Javadoc)
	 * @see java.lang.Runnable#run()
	 */
	
	@Override
	public void run(){
		try{
			URL url = new URL(linkUrl);
			HttpURLConnection http = (HttpURLConnection) url.openConnection();
			http.setRequestProperty("User-Agent", configValues.get("user-agent"));
			http.setConnectTimeout(10000);
			http.setReadTimeout(20000);
			int statusCode=http.getResponseCode();
			String status = http.getResponseMessage();
			if(!(configValues.get("statuses").contains(Integer.toString(statusCode)))){
				linkStatus.add(browser+" ||| "+name+" ||| "+"Link text : "+linkText+" ||| URL : "+linkUrl+" ||| "+statusCode+" ||| "+status);
			}
		}
		catch(Exception e){
			linkStatus.add(browser+" ||| "+name+" ||| "+"Exception : "+e.getClass()+" ||| Link text : "+linkText+" ||| URL : "+linkUrl);
		}
	}

}
