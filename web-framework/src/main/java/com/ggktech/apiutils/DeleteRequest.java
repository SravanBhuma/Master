package com.ggktech.apiutils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URI;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.message.BasicNameValuePair;

import com.ggktech.applib.ApplicationLibrary;
import com.ggktech.utils.Screenshot;
import com.ggktech.utils.Status;

public class DeleteRequest {

	private String baseURL;
	private HttpClient httpClient;
	private HttpDelete deleteRequest;
	private ApplicationLibrary appLib;

	public DeleteRequest(String baseURL, HttpClient httpClient, ApplicationLibrary appLib) {
		this.baseURL = baseURL;
		this.httpClient = httpClient;
		deleteRequest = new HttpDelete(baseURL);
		this.appLib = appLib;
	}

	public DeleteRequest setHeaders(Map<String, String> headers) {
		for (Map.Entry<String, String> header : headers.entrySet()) {
			deleteRequest.setHeader(header.getKey(), header.getValue());
		}
		return this;

	}

	public DeleteRequest addParameters(Map<String, String> parameters) {
		try {
			List<NameValuePair> lparameters = new ArrayList<>();
			// Convert MAP to List<NameValuePair>
			for (Map.Entry<String, String> entry : parameters.entrySet()) {
				lparameters.add(new BasicNameValuePair(entry.getKey(), entry.getValue()));
			}
			URI uri = new URIBuilder(baseURL).addParameters(lparameters).build();
			deleteRequest.setURI(uri);

		} catch (Exception e) {
			appLib.getRep().reportinCatch(e);
		}
		return this;
	}

	public RestResponse execute() {
		HttpResponse response = null;
		try {
			reportDeleteRequestDetails();
			response = httpClient.execute(deleteRequest);

		} catch (ClientProtocolException e) {
			appLib.getRep().reportinCatch(e);
		} catch (IOException e) {
			appLib.getRep().reportinCatch(e);
		}
		 return new RestResponse(response, appLib);
		
	}

	private void reportDeleteRequestDetails() {
		// Report Headers, URI, Request Line
		Header[] header = deleteRequest.getAllHeaders();
		Map<String, String> headers = new HashMap<>();
		for (Header header2 : header) {
			headers.put(header2.getName(), header2.getValue());
		}
		appLib.getRep().report(Status.PASS, "Request URI: " + deleteRequest.getURI(), Screenshot.FALSE);
		appLib.getRep().report(Status.PASS, "Request Line : " + deleteRequest.getRequestLine(), Screenshot.FALSE);
		appLib.getRep().report(Status.PASS, "Request Headers : " + headers.toString(), Screenshot.FALSE);
	}

}
