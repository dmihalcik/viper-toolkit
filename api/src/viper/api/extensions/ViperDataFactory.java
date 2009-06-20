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

package viper.api.extensions;

import java.util.*;

import viper.api.*;

/**
 * This is the interface that must be implemented in order for 
 * a parser to know how to convert data from a serialized
 * representation. 
 * 
 * @author davidm
 */
public interface ViperDataFactory {
	/**
	 * Gets all currently available data type identifiers.
	 * @return
	 */
	public Iterator getTypes();

	/**
	 * Gets a type directly from a concatenated URI, usually
	 * of the form (namespace)#(typename) or (namespace)/(typename).
	 * @param uri The locator/indicator for the type
	 * @return parameter, if it exists. Otherwise, returns null
	 */
	public AttrValueWrapper getAttribute(String uri);
}
