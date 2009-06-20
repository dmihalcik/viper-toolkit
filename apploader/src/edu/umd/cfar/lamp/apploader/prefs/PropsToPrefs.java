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

import com.hp.hpl.jena.mem.*;
import com.hp.hpl.jena.rdf.model.*;
import com.hp.hpl.jena.vocabulary.*;

import edu.umd.cfar.lamp.apploader.*;

/**
 * A {@link TriggerHandler} that converts from java properties
 * to AppLoader preferences.
 * 
 * @author davidm
 */
public class PropsToPrefs implements TriggerHandler {

	/**
	 * It isn't clear at what layer these should be added. 
	 * I am putting them in the 'temporary' preferences, so 
	 * they are not serialized (if/when I add serialization of
	 * prefs).
	 * 
	 * @see TriggerHandler#invoke(PrefsManager, Resource, String)
	 */
	public void invoke(PrefsManager prefs, Resource def, String value) {
		Statement inserter = def.getProperty(PREFS.inserts);
		if (inserter != null) {
			Resource trip = inserter.getResource();
			Resource pred = trip.getProperty(RDF.predicate).getResource();
			Resource r;
			Model toAdd = new ModelMem();
			Property p = toAdd.createProperty(pred.getURI());
			if (trip.hasProperty(RDF.subject)) {
				r = toAdd.createResource(trip.getProperty(RDF.subject).getResource());
			} else {
				r = toAdd.createResource();
			}
			toAdd.add(r, p, value);
			prefs.changeTemporary(null, toAdd);
		}
	}

}
