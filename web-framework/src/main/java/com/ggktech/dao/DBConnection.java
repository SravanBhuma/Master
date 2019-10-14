package com.ggktech.dao;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import org.apache.log4j.Logger;
import org.apache.poi.ss.usermodel.Sheet;

import com.ggktech.utils.ConfigConstants;
import com.ggktech.utils.PropertyFileConstants;
import com.ggktech.utils.Reporter;
import com.ggktech.utils.SheetConstants;

/**
 * Class that contains function for establishing DBConnection.
 *
 */
public class DBConnection {

	private static final Logger LOGGER = Logger.getLogger(DBConnection.class);
	private String filePathTestSuite;
	private Connection con = null;
	private Statement stmt;
	private String dbClass;
	private ResultSet rs;
	private PropertiesFileReader prop;
	private Properties configProperties;
	private Sheet mainSheet;
	private String sDBType;
	private String sDBURL;
	private String sDBUserName;
	Reporter rep = new Reporter();
	private String sDBPaswd;

	/**
	 * Constructor
	 */
	public DBConnection() {
		try {
			prop = PropertiesFileReader.getInstance();
			configProperties = prop.getPropFile(PropertyFileConstants.CONFIG_PROPERTIES);
			filePathTestSuite = configProperties.getProperty("filePathTestSuite");
			mainSheet = ExcelDataHandler.getSheetData(SheetConstants.TEST_SUITE_SHEET, filePathTestSuite);
			
			sDBType = configProperties.getProperty("dbType");
			sDBURL = configProperties.getProperty("dbUrl");
			sDBUserName = configProperties.getProperty("dbUsername");
			sDBPaswd = configProperties.getProperty("dPassword");

			switch (DBType.valueOf(sDBType.toUpperCase())) {
			case MYSQL:
				dbClass = configProperties.getProperty("mysqlDriverClass");
				Class.forName(dbClass).newInstance();
				break;
			case ORACLE:
				dbClass = configProperties.getProperty("oracleDriverClass");
				Class.forName(dbClass).newInstance();
				break;
			case SQLSERVER:
				dbClass = configProperties.getProperty("sqlserDriverClass");
				Class.forName(dbClass).newInstance();
				break;
			}

		} catch (Exception e) {
			LOGGER.error(e);
			rep.reportinCatch(e);
		}

	}

	/**
	 * @return Established connection object
	 */
	private Connection getDBConnection() {
		try {
			con = DriverManager.getConnection(sDBURL, sDBUserName, sDBPaswd);

		} catch (SQLException e) {
			LOGGER.error("Exception while establishing DBConnection.");
		}
		return con;

	}

	/**
	 * @returns : Map<String,Map<String, String>> in which key is the unique column value for each row.This method will return multiple rows
	 * 							from the database.
	 * @parameter : query : Query passed in script
	 * @parameter : uniqueColumnName : Which column to be considered to be unique and to be added as key to the result.
	 */
	public Map<String,Map<String, String>> getDataFromDB(String query,String uniqueColumnName) {
		Map<String,Map<String, String>> record = new HashMap<>();
		try {
			con = getDBConnection();
			Map<String,String> temp;
			// Statement object to send the SQL statement to the Database
			stmt = con.createStatement();
			rs = stmt.executeQuery(query);
			int rowNumber = 1;
			while (rs.next()) {
				String uniqueName = null;
				temp = new HashMap<String, String>();
				ResultSetMetaData rsmd = rs.getMetaData();
				int column = rsmd.getColumnCount();
				for (int i = 1; i <= column; i++) {
					temp.put(rsmd.getColumnName(i), rs.getString(rsmd.getColumnName(i)));
					if(rsmd.getColumnName(i).equals(uniqueColumnName)) {
						uniqueName = rs.getString(rsmd.getColumnName(i));
					}
				}
				record.put(uniqueName, temp);
			}
		} catch (Exception e) {
			LOGGER.error(e);
			rep.reportinCatch(e);
		} finally {
			try {
				con.close();
			} catch (SQLException e) {
				LOGGER.error(e);
				rep.reportinCatch(e);
			}
		}
		return record;
	}
	
	
	/**
     * @returns : List<Map<String, String>> records : records : data from database
     *          is stored in this using record set.
     * @parameter : query : Query passed in script
     */
    public Map<String, String> getSingleRowDataFromDB(String query) {
        Map<String, String> record = new HashMap<>();
        try {
            con = getDBConnection();
            // Statement object to send the SQL statement to the Database
            stmt = con.createStatement();
            rs = stmt.executeQuery(query);
            while (rs.next()) {
                ResultSetMetaData rsmd = rs.getMetaData();
                int column = rsmd.getColumnCount();
                for (int i = 1; i <= column; i++) {
                    record.put(rsmd.getColumnName(i), rs.getString(rsmd.getColumnName(i)));
                }
            }
        } catch (Exception e) {
            LOGGER.error(e);
            rep.reportinCatch(e);
        } finally {
            try {
                con.close();
            } catch (SQLException e) {
                LOGGER.error(e);
                rep.reportinCatch(e);
            }
        }
        return record;
    }

	/**
	 * Enum for DB types
	 */
	public enum DBType {

		MYSQL("mysql"), ORACLE("oracle"), SQLSERVER("sqlserver");
		private final String text;
		private DBType(final String text) {
			this.text = text;
		}
		@Override
		public String toString() {
			return text;
		}
	}
}