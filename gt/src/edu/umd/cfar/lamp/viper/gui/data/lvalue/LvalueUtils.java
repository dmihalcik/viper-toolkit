/***************************************
 *            ViPER                    *
 *  The Video Processing               *
 *         Evaluation Resource         *
 *                                     *
 *  Distributed under the GPL license  *
 *        Terms available at gnu.org.  *
 *                                     *
 *  Copyright University of Maryland,  *
 *                      College Park.  *
 ***************************************/

package edu.umd.cfar.lamp.viper.gui.data.lvalue;

import java.util.*;

import viper.api.*;
import viper.api.datatypes.*;

/**
 * @author davidm@cfar.umd.edu
 */
public class LvalueUtils {
	public static String getListAsString(Attribute a) {
		return getListAsString(a.getAttrConfig());
	}
	public static String getListAsString(AttrConfig cfg) {
		return getListAsString((Lvalue) cfg.getParams());
	}
	public static String getListAsString (Lvalue param) { 
		StringBuffer sb = new StringBuffer().append(' ');
		String[] P = param.getPossibles();
		if (P.length == 0) {
			return null;
		}
		for (int i = 0; i < P.length; i++) {
			sb.append(P[i]).append(' ');
		}
		return sb.substring(1, sb.length()-1);
	}

	public static String[] parseString(String s) {
		StringTokenizer st = new StringTokenizer(s);
		List l = new LinkedList();
		while (st.hasMoreTokens()) {
			String n = st.nextToken();
			if (!l.contains(n)) {
				l.add(n);
			}
		}
		return (String[]) l.toArray(new String[l.size()]);
	}
}
