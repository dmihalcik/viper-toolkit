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

package edu.umd.cfar.lamp.viper.gui.data;

import java.util.*;

import viper.api.*;

/**
 * Contains comparators for the different data types.
 */
public class ViperSorters {
	private static HashMap comps;
	private static final Comparator BVAL_CMP = new Comparator() {
		public int compare(Object o1, Object o2) {
			boolean b1 = ((Boolean) o1).booleanValue();
			boolean b2 = ((Boolean) o2).booleanValue();
			return (b1 == b2) ? 0 : (b1 ? 1 : -1);
		}
	};
	static {
		comps = new HashMap();
		comps.put(ViperData.ViPER_DATA_URI + "bvalue", BVAL_CMP);
	}

	public static Comparator getCmpFor(String type) {
		return (Comparator) comps.get(type);
	}
}
