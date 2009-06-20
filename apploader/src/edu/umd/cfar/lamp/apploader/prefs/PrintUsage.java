/***************************************
 *:// L A M P . cfar . umd . edu       *
 *      AppLoader                      *
 *                                     *
 *      A tool for loading java apps   *
 *             from RDF descriptions.  *
 *                                     *
 * Distributed under the GPL license   *
 *        Terms available at gnu.org.  *
 *                                     *
 *  Copyright University of Maryland,  *
 *                      College Park.  *
 ***************************************/

package edu.umd.cfar.lamp.apploader.prefs;

import com.hp.hpl.jena.rdf.model.*;

/**
 * Command-line trigger handler that prints out
 * usage information and exits the program. 
 */
public class PrintUsage implements TriggerHandler {
	/**
	 * Prints out usage information and exits the program. 
	 * @see edu.umd.cfar.lamp.apploader.prefs.TriggerHandler#invoke(edu.umd.cfar.lamp.apploader.prefs.PrefsManager, com.hp.hpl.jena.rdf.model.Resource, java.lang.String)
	 */
	public void invoke(PrefsManager prefs, Resource def, String value) {
		try {
			prefs.getOptionsManager().printUsage(10);
		} catch (PreferenceException e) {
			prefs.getLogger().severe ("Print usage failed: " + e.getLocalizedMessage());
		}
		System.exit(0);
	}

}
