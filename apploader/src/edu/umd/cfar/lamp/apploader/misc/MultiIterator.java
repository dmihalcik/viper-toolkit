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

import java.util.*;

/**
 * Given an array of iterators, iterates through them.
 */
public class MultiIterator implements Iterator {
	private Iterator[] internal;
	private Iterator curr;
	private Iterator last;
	private int k;

	/**
	 * Constructs a new iterator given the 
	 * array of iterators to traverse.
	 * @param iters The iterators to exhaust in order
	 */
	public MultiIterator(Iterator[] iters) {
		internal = iters;
		k = 0;
		last = null;
		do {
			curr = internal[k];
			k++;
		} while (k<internal.length && !curr.hasNext());
	}

	/**
	 * Tests to see if there is another item in this or the next iterator.
	 * @see java.util.Iterator#hasNext()
	 */
	public boolean hasNext() {
		return internal.length > 0 && curr.hasNext();
	}
	
	/**
	 * Gets the next element in the flattened list of iterators.
	 * @see java.util.Iterator#next()
	 */
	public Object next() {
		if (curr == null) { // no iterators
			throw new NoSuchElementException();
		}
		Object temp = curr.next();
		last = curr;
		if (!curr.hasNext()) {
			while (k<internal.length && !curr.hasNext()) {
				curr = internal[k];
				k++;
			}
		}
		return temp;
	}
	
	/**
	 * Tries to call the appropriate remove method.
	 * @see java.util.Iterator#remove()
	 */
	public void remove() {
		last.remove();
	}
}
