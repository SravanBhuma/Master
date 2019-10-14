package com.ggktech.dao;

import java.io.IOException;
import java.util.HashMap;
import java.util.Properties;

/**
 * Class which contains the functions to read the property files.
 */
public class PropertiesFileReader {
	private static PropertiesFileReader fileReader;
	private HashMap<String, Properties> propertiesMap = new HashMap<>();

	/**
	 * @param sPropFileName
	 *            : property file name
	 * @return obj.get(sPropFileName): Property file
	 * @throws IOException
	 */
	public Properties getPropFile(String sPropFileName) throws IOException {
		Properties prop;
		if (propertiesMap.get(sPropFileName) == null) {
			prop = new Properties();
			prop.load(this.getClass().getClassLoader().getResourceAsStream(sPropFileName));
			propertiesMap.put(sPropFileName, prop);
		}
		return propertiesMap.get(sPropFileName);
	}

	/**
	 * @return PropertiesFileReader Instance.
	 */

	public static PropertiesFileReader getInstance() {
		if (fileReader != null) {
			return fileReader;
		}
		synchronized (PropertiesFileReader.class) {
			if (fileReader == null) {
				fileReader = new PropertiesFileReader();
			}
			return fileReader;
		}
	}
	
}
