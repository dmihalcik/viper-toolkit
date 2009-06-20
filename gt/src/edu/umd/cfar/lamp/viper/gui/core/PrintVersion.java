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

package edu.umd.cfar.lamp.viper.gui.core;

import com.hp.hpl.jena.rdf.model.*;

import edu.umd.cfar.lamp.apploader.prefs.*;

/**
 * Print the version number to the command line.
 */
public class PrintVersion implements TriggerHandler {
	/**
	 * @inheritDoc
	 */
	public void invoke(PrefsManager prefs, Resource def, String value) {
		System.err.println("ViPER Version 4");
	}
}
