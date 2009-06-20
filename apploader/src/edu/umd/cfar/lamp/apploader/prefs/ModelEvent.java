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
 * Describes a change in an RDF model.
 * @author davidm
 */
public class ModelEvent {
	private Model removed;
	private Model added;
	/**
	 * Creates a new model event, with the
	 * given additions and removals.
	 * @param del these triples are removed
	 * @param add these triples are added
	 */
	public ModelEvent(Model del, Model add) {
		added = add;
		removed = del;
	}
	/**
	 * Gets all the RDF Statements related to the query since the
	 * Listener was last invoked.
	 * @return Collection of Statement objects
	 */
	public Model getAdded() {
		return added;
	}

	/**
	 * Gets all of the RDF Statements that were removed, related to
	 * the listening filter.
	 * @return Collection of Statements
	 */
	public Model getRemoved() {
		return removed;
	}
}
