package com.ggktech.apiutils;

import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.http.Header;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.message.BasicNameValuePair;

import com.ggktech.applib.ApplicationLibrary;
import com.ggktech.service.PublicLibrary;
import com.ggktech.utils.Screenshot;
import com.ggktech.utils.Status;

public class GetRequest{

	private String baseURL;
	private HttpClient httpClient;
	private HttpGet getRequest;
	private ApplicationLibrary appLib;
	

	public GetRequest(String baseURL, HttpClient httpClient, ApplicationLibrary appLib) {
		this.baseURL = baseURL;
		this.httpClient = httpClient;
		getRequest = new HttpGet(baseURL);
		this.appLib = appLib;
	}

	public GetRequest setHeaders(Map<String, String> headers) {
		for (Map.Entry<String, String> header : headers.entrySet()) {
			getRequest.setHeader(header.getKey(), header.getValue());
		}
		return this;

	}

	public GetRequest addParameters(Map<String, String> parameters) {
		try {
			List<NameValuePair> lparameters = new ArrayList<>();
			// Convert MAP to List<NameValuePair>
			for (Map.Entry<String, String> entry : parameters.entrySet()) {
				lparameters.add(new BasicNameValuePair(entry.getKey(), entry.getValue()));
			}
			URI uri = new URIBuilder(baseURL).addParameters(lparameters).build();
			getRequest.setURI(uri);
		} catch (Exception e) {
			appLib.getRep().reportinCatch(e);
		}
		return this;
	}

	public RestResponse execute() {
		HttpResponse response = null;
		try {
			reportGetRequestDetails();
			response = httpClient.execute(getRequest);
		} catch (ClientProtocolException e) {
			appLib.getRep().reportinCatch(e);
		} catch (IOException e) {
			appLib.getRep().reportinCatch(e);
		}
		return new RestResponse(response, appLib);
	}

	private void reportGetRequestDetails() {
		// Report Headers, URI, Request Line
		Header[] header = getRequest.getAllHeaders();
		Map<String, String> headers = new HashMap<>();
		for (Header header2 : header) {
			headers.put(header2.getName(), header2.getValue());
		}
		appLib.getRep().report(Status.PASS, "Request URI: " + getRequest.getURI(), Screenshot.FALSE);
		appLib.getRep().report(Status.PASS, "Request Line : " + getRequest.getRequestLine(), Screenshot.FALSE);
		appLib.getRep().report(Status.PASS, "Request Headers : " + headers.toString(), Screenshot.FALSE);
	}
}
