package edu.umd.cfar.lamp.apploader.prefs;

import java.text.*;
import java.util.*;

/**
 * Class for dealing with the ISO-8601 date format commonly
 * found in XML files. I downloaded most of this code from 
 * the net, where it had this attached information:
 * <blockquote>
 * ISO 8601 date manipulation in Java 1.2.
 * This was sent to me by Simon Brooke <simon@jasmine.org.uk>
 * 2000-11-22T15:20:52Z for inclusion into my material on ISO 8601.
 * I don't understand much of it, but I include it in the hope
 * that some people might find it useful. <br /> 
 *  - Jukka K. Korpela, jkorpela@cs.tut.fi 
 * </blockquote>
 * I'm not sure, but I think that means it is in the public domain.
 */
public class Iso8601Calendar extends GregorianCalendar {
	/**
	 * Return an ISO8601 string representing the date/time
	 * represented by this Calendar
	 * @return an ISO8601 string representing the date/time
	 */
	public String toString() {
		String timef = "'T'hh:mm:ss.S";
		String datef = "yyyy-MM-dd";
		String bothf = "yyyy-MM-dd'T'hh:mm:ss.S";
		boolean dotimezone = true;

		String format = bothf; // initially assume this is a date/time

		if ((isSet(DAY_OF_MONTH) == false || get(DAY_OF_MONTH) == 1)
			&& (isSet(MONTH) == false || get(MONTH) == 0)
			&& (isSet(YEAR) == false || get(YEAR) == 1970))
			// it's highly probable that we're
			// looking at a time-of-day.
			format = timef;
		else if (
			(isSet(HOUR) == false || get(HOUR) == 0)
				&& (isSet(MINUTE) == false || get(MINUTE) == 0)
				&& (isSet(SECOND) == false || get(SECOND) == 0))
			// it's highly probable that we're
			// looking at a date.
			{
			format = datef;
			dotimezone = false;
		}

		DateFormat df = new SimpleDateFormat(format);
		StringBuffer result = new StringBuffer(df.format(getTime()));

		if (dotimezone) // we don't need to worry about timezone in
			{ // date only strings but otherwise we do...

			int offset = get(java.util.Calendar.ZONE_OFFSET);

			if (offset == 0)
				result.append("Z");
			// zero offset -- excellent, easy.
			else { // horrible, horrible, oh most horrible.
				NumberFormat nf = new DecimalFormat("00");

				offset = offset / 60000;
				int om = Math.abs(offset % 60), oh = Math.abs(offset / 60);
				String os = "+";

				if (offset < 0)
					os = "-";

				result.append(os).append(nf.format(oh));

//				if (om > 0) // XML Schema date time requires the minutes
					result.append(':').append(nf.format(om));
			}
		}

		return result.toString();
	}
}
