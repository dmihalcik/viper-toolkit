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

package edu.umd.cfar.lamp.apploader.propertysheets;

import java.util.*;

/**
 * Extended properties are properties that an object may
 * or may not have, depending on other features
 * of the object. This is basically an interface to 
 * a string-keyed java map, but with an event listener
 * for when key pairs are added or removed.
 */
public interface InstancePropertyList extends List {
	/**
	 * Set the backing object for the list of properties.
	 * @param o The new object.
	 */
	public void setObject(Object o);
	
	/**
	 * Gets the object to which the properties are attached.
	 * @return
	 */
	public Object getObject();
	
	/**
	 * Indicates that the object's value has changed,
	 * although its reference has not.
	 */
	public void refresh();
}
