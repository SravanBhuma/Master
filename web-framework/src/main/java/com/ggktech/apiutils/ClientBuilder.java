package com.ggktech.apiutils;

import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.client.HttpClient;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.client.HttpClientBuilder;

public class ClientBuilder {
	private HttpClient httpClient;
	private HttpClientBuilder httpClientBuilder;

	public ClientBuilder setAuthentication(String userName, String password) {
		CredentialsProvider credentials = new BasicCredentialsProvider();
		UsernamePasswordCredentials creds = new UsernamePasswordCredentials(userName, password);
		credentials.setCredentials(AuthScope.ANY, creds);
		httpClientBuilder = httpClientBuilder.setDefaultCredentialsProvider(credentials);
		return this;
	}

	public ClientBuilder() {
		httpClientBuilder = httpClientBuilder.create();
	}

	public static ClientBuilder build() {
		return new ClientBuilder();
	}

	public HttpClient complete() {
		httpClient = httpClientBuilder.build();
		return httpClient;
	}

}
