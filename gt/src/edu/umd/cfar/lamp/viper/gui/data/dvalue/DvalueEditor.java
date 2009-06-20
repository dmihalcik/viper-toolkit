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

package edu.umd.cfar.lamp.viper.gui.data.dvalue;

import edu.umd.cfar.lamp.apploader.misc.*;
import edu.umd.cfar.lamp.viper.gui.data.*;

/**
 * @author davidm@cfar.umd.edu
 * @since Jul 8, 2003
 */
public class DvalueEditor extends ViperDataFsmTextEditor {
	public DvalueEditor() {
		super(new IntegerFSM());
	}

	public Object parse(String s) {
		if (s == null || "".equals(s)) {
				return null;
		} else {
			try {
				return Integer.valueOf(s);
			} catch (NumberFormatException nfx) {
				nfx.printStackTrace();
				return null;
			}
		}
	}
}
