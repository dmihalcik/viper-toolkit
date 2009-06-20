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

package edu.umd.cfar.lamp.viper.util;

import java.util.*;

/**
 * Enhanced ListIterator with methods for dealing with 
 * lists without ends.
 */
public interface CircularIterator extends ListIterator {
	/**
	 * Gets the last object returned by the last
	 * call to next or previous. If the object has been
	 * removed, it should return null, with the pointer now
	 * between the adjacent elements.
	 * @return 
	 */
	public Object current();

	/**
	 * Tests to see if the collection the iterator refers to has no elements.
	 * Note that either hasNext() and hasPrevious() will return the same thing.
	 * @return <code>true</code> when the iterator is iterating
	 * over an empty collection
	 */
	public boolean isEmpty();
}
