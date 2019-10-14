package com.ggktech.apiutils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Serializable;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.commons.lang.SerializationUtils;
import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.entity.StringEntity;
import org.apache.http.message.BasicNameValuePair;

import com.ggktech.applib.ApplicationLibrary;
import com.ggktech.utils.Screenshot;
import com.ggktech.utils.Status;

public class PutRequest {

	private String baseURL;
	private HttpClient httpClient;
	private HttpPut putRequest;
	private ApplicationLibrary appLib;

	public PutRequest(String baseURL, HttpClient httpClient, ApplicationLibrary appLib) {
		this.baseURL = baseURL;
		this.httpClient = httpClient;
		putRequest = new HttpPut(baseURL);
		this.appLib = appLib;
	}

	public PutRequest setHeaders(Map<String, String> headers) {
		for (Map.Entry<String, String> header : headers.entrySet()) {
			putRequest.setHeader(header.getKey(), header.getValue());
		}
		return this;

	}

	public PutRequest setBody(String requestBody) {
		StringEntity entity = null;
		try {
			entity = new StringEntity(requestBody);
		} catch (UnsupportedEncodingException e) {
			appLib.getRep().reportinCatch(e);
		}
		putRequest.setEntity(entity);
		return this;
	}

	public PutRequest addParameters(Map<String, String> parameters) {
		try {
			List<NameValuePair> lparameters = new ArrayList<>();
			// Convert MAP to List<NameValuePair>
			for (Map.Entry<String, String> entry : parameters.entrySet()) {
				lparameters.add(new BasicNameValuePair(entry.getKey(), entry.getValue()));
			}
			putRequest.setEntity(new UrlEncodedFormEntity(lparameters));

		} catch (Exception e) {
			appLib.getRep().reportinCatch(e);
		}
		return this;
	}

	public RestResponse execute() {
		HttpResponse response = null;
		try {
			reportPutRequestDetails();
			response = httpClient.execute(putRequest);

		} catch (ClientProtocolException e) {
			appLib.getRep().reportinCatch(e);
		} catch (IOException e) {
			appLib.getRep().reportinCatch(e);
		}
		 return new RestResponse(response, appLib);
		
	}

	private void reportPutRequestDetails() {
		// Report Headers, URI, Request Line
		Header[] header = putRequest.getAllHeaders();
		Map<String, String> headers = new HashMap<>();
		for (Header header2 : header) {
			headers.put(header2.getName(), header2.getValue());
		}
		appLib.getRep().report(Status.PASS, "Request URI: " + putRequest.getURI(), Screenshot.FALSE);
		appLib.getRep().report(Status.PASS, "Request Line : " + putRequest.getRequestLine(), Screenshot.FALSE);
		appLib.getRep().report(Status.PASS, "Request Headers : " + headers.toString(), Screenshot.FALSE);
		String requestBody = "";
		try {
			InputStream is = putRequest.getEntity().getContent();
			BufferedReader br = new BufferedReader(new InputStreamReader(is));
			requestBody = br.lines().collect(Collectors.joining(System.lineSeparator()));
		} catch (Exception e) {
			// If request body is not there
		}

		appLib.getRep().report(Status.PASS, "Request Body : " + requestBody, Screenshot.FALSE);
	}

}
