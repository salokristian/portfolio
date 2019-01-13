package mcc.aalto.fi.chatapp;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

public class Utils {
	public static String getIsoTimestamp() {
		Date date = new Date(System.currentTimeMillis());

		SimpleDateFormat sdf;
		sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSXXX");
		sdf.setTimeZone(TimeZone.getTimeZone("CET"));

		return sdf.format(date);
	}
}
