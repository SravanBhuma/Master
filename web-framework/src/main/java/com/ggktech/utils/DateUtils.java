package com.ggktech.utils;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

import org.apache.log4j.Logger;


/**
 * Class that contains the functions for date operations.
 */
public class DateUtils {
	private static final Logger LOGGER = Logger.getLogger(DateUtils.class.getName());
	private static final int ONE_DAY = 1000 * 60 * 60 * 24;
	private static final int ONE_HOUR = ONE_DAY / 24;
	private static final int ONE_MINUTE = ONE_HOUR / 60;
	private static final int ONE_SECOND = ONE_MINUTE / 60;
	private static final DateFormat dateFormat = new SimpleDateFormat(ConfigConstants.DATE_TIME_FORMAT);

	private DateUtils() {

	}
	/**
	 * @return  currentDate : Return current date in "dd-MM-yyyy HH:mm:ss" format as string.
	 */
	public static String getCurrentDate() {
		return dateFormat.format(new Date());
	}

	/**
	 * @param sStrtTime : Execution start time
	 * @param sEndTime : Execution End time
	 * @return exec : execution time displayed in following format: 0days:0hh:11min:33secs
	 */
	public static String calcExec(String sStrtTime, String sEndTime) {

		String exec = null;
		long[] result = new long[5];
		try {
			Date d1 = dateFormat.parse(sStrtTime);
			Date d2 = dateFormat.parse(sEndTime);

			Calendar cal = Calendar.getInstance();
			cal.setTimeZone(TimeZone.getTimeZone(ConfigConstants.TIME_ZONE_ID));
			cal.setTime(d1);

			long t1 = cal.getTimeInMillis();
			cal.setTime(d2);

			long diff = Math.abs(cal.getTimeInMillis() - t1);

			long d = diff / ONE_DAY;
			diff %= ONE_DAY;

			long h = diff / ONE_HOUR;
			diff %= ONE_HOUR;

			long m = diff / ONE_MINUTE;
			diff %= ONE_MINUTE;

			long s = diff / ONE_SECOND;
			result[0] = d;
			result[1] = h;
			result[2] = m;
			result[3] = s;

			exec = result[0] + "days:" + result[1] + "hh:" + result[2] + "min:" + result[3]
					+ "secs";
		} catch (Exception e) {
			LOGGER.error("Exception in calcExec" + e);
		}
		return exec;

	}

}
