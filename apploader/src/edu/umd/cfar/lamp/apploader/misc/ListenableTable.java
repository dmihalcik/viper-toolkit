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

package edu.umd.cfar.lamp.apploader.misc;

/**
 * An interface to provide some higher-level listeners
 * to a standard JTable.
 */
public interface ListenableTable {
	/**
	 * Get the currently attached listeners.
	 * @return All currently attached table listeners.
	 */
	public abstract TableListener[] getTableListeners();
	
	/**
	 * Remove the listener.
	 * @param tl The listener to detach.
	 */
	public abstract void removeTableListener(TableListener tl);
	
	/**
	 * Adds the given listener.
	 * @param tl The listener to add.
	 */
	public abstract void addTableListener(TableListener tl);
}