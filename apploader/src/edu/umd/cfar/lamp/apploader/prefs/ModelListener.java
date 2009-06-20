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
 * Listens for changes to an RDF triplestore. 
 * It can be filtered based on elements of the triple,
 * with null values acting as wild cards.
 */
public interface ModelListener {
	/**
	 * Indicates that a change occurred.
	 * @param event description of the change in the model
	 */
	public void changeEvent(ModelEvent event);
	
	/**
	 * Will only be notified if the selector matches a statement
	 * that has added or removed.
	 * @return
	 */
	public Selector getSelector();
}
