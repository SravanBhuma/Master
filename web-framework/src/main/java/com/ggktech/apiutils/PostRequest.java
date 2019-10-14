package com.ggktech.apiutils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collector;
import java.util.stream.Collectors;

import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.message.BasicNameValuePair;

import com.ggktech.applib.ApplicationLibrary;
import com.ggktech.utils.Screenshot;
import com.ggktech.utils.Status;

public class PostRequest {
	private String baseURL;
	private HttpClient httpClient;
	private HttpPost postRequest;
	private ApplicationLibrary appLib;

	public PostRequest(String baseURL, HttpClient httpClient, ApplicationLibrary appLib) {
		this.baseURL = baseURL;
		this.httpClient = httpClient;
		postRequest = new HttpPost(baseURL);
		this.appLib = appLib;
	}

	public PostRequest setHeaders(Map<String, String> headers) {
		for (Map.Entry<String, String> header : headers.entrySet()) {
			postRequest.setHeader(header.getKey(), header.getValue());
		}
		return this;

	}

	public PostRequest setBody(String requestBody) {
		StringEntity entity = null;
		try {
			entity = new StringEntity(requestBody);
		} catch (UnsupportedEncodingException e) {
			appLib.getRep().reportinCatch(e);
		}
		postRequest.setEntity(entity);
		return this;
	}

	public PostRequest addParameters(Map<String, String> parameters) {
		try {
			List<NameValuePair> lparameters = new ArrayList<>();
			// Convert MAP to List<NameValuePair>
			for (Map.Entry<String, String> entry : parameters.entrySet()) {
				lparameters.add(new BasicNameValuePair(entry.getKey(), entry.getValue()));
			}
			postRequest.setEntity(new UrlEncodedFormEntity(lparameters));

		} catch (Exception e) {
			appLib.getRep().reportinCatch(e);
		}
		return this;
	}

	public RestResponse execute() {
		HttpResponse response = null;
		try {
			reportPostRequestDetails();
			response = httpClient.execute(postRequest);
		} catch (ClientProtocolException e) {
			System.err.println(e.getMessage());
			e.printStackTrace();
		} catch (IOException e) {
			System.err.println(e.getMessage());
			e.printStackTrace();
		}
		// return new RestResponse(response, appLib);
		return null;
	}

	private void reportPostRequestDetails() {
		// Report Headers, URI, Request Line
		Header[] header = postRequest.getAllHeaders();
		Map<String, String> headers = new HashMap<>();
		for (Header header2 : header) {
			headers.put(header2.getName(), header2.getValue());
		}
		appLib.getRep().report(Status.PASS, "Request URI: " + postRequest.getURI(), Screenshot.FALSE);
		appLib.getRep().report(Status.PASS, "Request Line : " + postRequest.getRequestLine(), Screenshot.FALSE);
		appLib.getRep().report(Status.PASS, "Request Headers : " + headers.toString(), Screenshot.FALSE);
		String requestBody = "";
		try {
			InputStream is = postRequest.getEntity().getContent();
			BufferedReader br = new BufferedReader(new InputStreamReader(is));
			requestBody = br.lines().collect(Collectors.joining(System.lineSeparator()));
		} catch (Exception e) {
			appLib.getRep().reportinCatch(e);
		}
		appLib.getRep().report(Status.PASS, "Request Body : " + requestBody, Screenshot.FALSE);
	}

}
