/*
 * Created on Jan 31, 2005
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package edu.umd.cfar.lamp.apploader.misc;


public class IntegerFSM extends StringParserFSM {
	private StringBuffer sb;
	public boolean pushDown(char c) {
		if (sb.length() == 0 && ('+' == c || '-' == c)) {
			sb.append(c);
			return true;
		}
		if (Character.isDigit(c))  {
			sb.append(c);
			return true;
		}
		return false;
	}

	public void reset() {
		sb = new StringBuffer();
	}

	public String toString() {
		return sb.toString();
	}
	
	public String getValidString() {
		if (sb.length() == 1 && !Character.isDigit(sb.charAt(0))) {
			return "";
		} else {
			return toString();
		}
	}

}