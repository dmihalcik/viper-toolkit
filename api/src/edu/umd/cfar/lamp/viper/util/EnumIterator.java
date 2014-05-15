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
 * An Iterator wrapper for a java Enumeration.
 * @author davidm@cfar.umd.edu
 * @since Jun 20, 2003
 */
public class EnumIterator<X> implements Iterator<X> {
	private Enumeration<X> enumeration;
	/**
	 * Constructs a new Iterator wrapping the given enumeration.
	 * @param enumeration the enumeration to wrap
	 */
	public EnumIterator (Enumeration<X> enumeration) {
		this.enumeration = enumeration;
	}
	/**
	 * Enumerations do not support removal.
	 * @throws UnsupportedOperationException
	 */
	public void remove() {
		throw new UnsupportedOperationException();
	}
	/**
	 * Tests to see if more elements remain.
	 * @return <code>enum.hasMoreElements()</code>
	 */
	public boolean hasNext() {
		return enumeration.hasMoreElements();
	}

	/**
	 * Gets the next element of the enumeration.
	 * @return <code>enum.nextElement()</code>
	 */
	public X next() {
		return enumeration.nextElement();
	}
}
