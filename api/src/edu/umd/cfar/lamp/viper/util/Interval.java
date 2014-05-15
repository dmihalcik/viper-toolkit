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
 * Stores an ordered pair of comparable objects that
 * together describe a half-open interval. 
 */
public interface Interval<C extends Comparable<?>> {
	/**
	 * Gets the start of the interval.
	 * @return first element in the interval
	 */
	public abstract C getStart();

	/**
	 * Gets the end of the interval, which is not an element
	 * of the interval.
	 * @return the first element after the interval
	 */
	public abstract C getEnd();

	/**
	 * Tests to see if an object exists that this Interval contains.
	 * @return <code>true</code> if the interval is non-empty
	 */
	public abstract boolean isEmpty();

	/**
	 * Tests to see if any elements of the set are elements of the
	 * interval.
	 * @param s set of Comparable objects to the interval, or other 
	 * Intervals.
	 * @return boolean true when some element of the set is contained
	 * within or intersects this interval
	 */
	public abstract boolean intersects(Set<C> s);

	/**
	 * Tests to see that the two intervals have some shared
	 * element(s).
	 * @param other the interval to check against
	 * @return true if the intervals intersect.
	 */
	public abstract boolean intersects(Interval<C> other);

	/**
	 * Checks to see if this interval contains the given element.
	 * @param i
	 * @return
	 */
	public abstract boolean contains(C i);

	/**
	 * Checks to see if the other interval is a subset of this one.
	 * @param other the interval to check
	 * @return true if other contains only elements that are 
	 * elements of this
	 */
	public abstract boolean contains(Interval<C> other);

	/**
	 * Checks to see if this contains all elements of the set.
	 * @param s the set to test
	 * @return true if s contains only elements that are 
	 * elements or subsets of this
	 */
	public abstract boolean contains(Set<C> s);

	/**
	 * Since an Interval is designed to be immutable, you need this 
	 * thing to change the interval.  
	 * @param start the first element
	 * @param stop the element after the last elment; all elements 
	 * of the interval are strictly less than this.
	 * @return A copy of this Interval with the new start and stop
	 */
	public abstract Interval<C> change(C start, C stop);

}