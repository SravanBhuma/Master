package com.ggktech.apitesting;

import java.util.List;
import java.util.Map;

import org.apache.http.client.HttpClient;

import com.ggktech.apiutils.ClientBuilder;
import com.ggktech.apiutils.GetRequest;
import com.ggktech.apiutils.RestResponse;
import com.ggktech.utils.Screenshot;
import com.ggktech.utils.Status;
import com.ggktech.utils.TestCaseTemplate;
import com.jayway.jsonpath.JsonPath;

public class TS_SampleAPITesting extends TestCaseTemplate {

	@Override
	public void testScript() {
		getCityWeather();
	}

	public void getCityWeather() {
		String cityName = "London";
		String countryName = "uk";
		String baseURL = "https://samples.openweathermap.org/data/2.5/weather?q=" + cityName + "," + countryName
				+ "&appid=b6907d289e10d714a6e88b30761fae22";

		HttpClient httpClient = ClientBuilder.build().complete();
		GetRequest getReq = new GetRequest(baseURL, httpClient, appLib);
		RestResponse response = getReq.execute();

		if (200 != response.getStatus()) {
			appLib.getRep().report(Status.FAIL, "Expected code = 200; Actual code = " + response.getStatus(),
					Screenshot.FALSE);
		}
		String responseBody = response.getResponseBody();
		String nameAttribute = JsonPath.read(responseBody, "$.name");
		appLib.verifyAssertEquals(cityName, nameAttribute, "Respones name", Screenshot.FALSE);
		try {

			List<Map<String, String>> weatherDescription = JsonPath.read(responseBody, "$.weather");
			System.out.println(weatherDescription.get(0).get("main"));
			System.out.println(weatherDescription);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}
