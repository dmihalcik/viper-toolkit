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

/**
 * An abstract iterator that does not have an end, instead returning to the beginning.
 * This class implements the empty circular iterator, and throws 
 * <code>UnsupportedOperationException</code>s when the change methods are invoked.
 */
public abstract class AbstractCircularIterator implements CircularIterator {
	/**
	 * Tests to see if there is another element.
	 * @return <code>true</code>
	 */
	public boolean hasNext() {
		return isEmpty();
	}

	/**
	 * Tests to see if there is another element.
	 * @return <code>true</code>
	 */
	public boolean hasPrevious() {
		return isEmpty();
	}

	/**
	 * Removes the current element; unimplemented.
	 * @throws UnsupportedOperationException
	 */
	public void remove() {
		throw new UnsupportedOperationException();
	}

	/**
	 * Sets the current element; unimplemented.
	 * @param o the value to set to current
	 * @throws UnsupportedOperationException
	 */
	public void set(Object o) {
		throw new UnsupportedOperationException();
	}

	/**
	 * Adds the value between current and next; unimplemented.
	 * @param o the value to add.
	 * @throws UnsupportedOperationException
	 */
	public void add(Object o) {
		throw new UnsupportedOperationException();
	}
}
