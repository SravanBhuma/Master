package com.ggktech.apiutils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.http.Header;
import org.apache.http.HttpResponse;

import com.ggktech.applib.ApplicationLibrary;
import com.ggktech.utils.Screenshot;
import com.ggktech.utils.Status;

public class RestResponse {

	private ApplicationLibrary appLib;
	private String responseBody = "";
	private String responseStatusLine = "";
	private int responseStatusCode;
	private Map<String, String> responseHeaders = new HashMap<>();

	public RestResponse(HttpResponse response, ApplicationLibrary appLib) {
		this.appLib = appLib;
		// Get response Headers
		Header[] headers = response.getAllHeaders();
		for (Header header : headers) {
			responseHeaders.put(header.getName(), header.getValue());
		}

		// Get response status code
		this.responseStatusCode = response.getStatusLine().getStatusCode();
		this.responseStatusLine = response.getStatusLine().toString();

		// Get response body

		InputStream is = null;
		try {
			is = response.getEntity().getContent();
			BufferedReader br = new BufferedReader(new InputStreamReader(is));
			this.responseBody = br.lines().collect(Collectors.joining(System.lineSeparator()));
			br.close();
		} catch (UnsupportedOperationException e) {
			appLib.getRep().reportinCatch(e);
		} catch (IOException e) {
			appLib.getRep().reportinCatch(e);
		} finally {
			try {
				is.close();
			} catch (IOException e) {
				appLib.getRep().reportinCatch(e);
			}
		}

		appLib.getRep().report(Status.PASS, "Response Status Line : " + this.responseStatusLine, Screenshot.FALSE);
		appLib.getRep().report(Status.PASS, "Response Headers : " + this.responseHeaders.toString(), Screenshot.FALSE);
		appLib.getRep().report(Status.PASS, "Response Body : " + this.responseBody, Screenshot.FALSE);
	}

	public Integer getStatus() {
		return this.responseStatusCode;
	}

	public String getResponseBody() {
		return this.responseBody;
	}

	public Map<String, String> getHeaders() {
		return this.responseHeaders;
	}

	public String getHeader(String headerName) {
		return this.responseHeaders.get(headerName);

	}

}
