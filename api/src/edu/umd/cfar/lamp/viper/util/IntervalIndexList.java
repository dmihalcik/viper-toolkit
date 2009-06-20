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

import org.apache.commons.collections.*;

/**
 * A list of items that is indexed not by integers but by 
 * Comparable objects.
 */
public interface IntervalIndexList {
	/**
	 * Get the value at the specified index in the list.
	 * @param index the index into the list
	 * @return the value at the specified index
	 */
	public abstract Object get(Comparable index);

	/**
	 * Sets the value at the given range. Note that, like 
	 * <code>SortedMap</code>, this means that value is set in the
	 * range from start, inclusive, to stop, exclusive.
	 * @param start the first index to set
	 * @param stop the first index that is not set
	 * @param value all elements in the list in the range [start, stop)
	 *   will take this value
	 * @throws IllegalArgumentException if start is not less than stop
	 */
	public abstract void set(Comparable start, Comparable stop, Object value);

	/**
	 * Removes all values at the given range. Note that, like 
	 * <code>SortedMap</code>, this means that value is set in the
	 * range from start, inclusive, to stop, exclusive.
	 * @param start the first index to remove
	 * @param stop the first index that is not removed
	 * @return <code>true</code> if any elements were removed
	 */
	public abstract boolean remove(Comparable start, Comparable stop);
	
	/**
	 * Gets an iterator over all the Interval objects
	 * here. Note that, if the 
	 * @return
	 */
	public Iterator iterator();
	
	/**
	 * Gets an iterator over all the Interval objects
	 * that intersect the given Interval.
	 * @return
	 */
	public Iterator iterator(Interval i);

	/**
	 * Adds all the elements from the given list
	 * at their specified locations
	 * @param l the list to add
	 * @return true if this changed
	 */
	public boolean addAll(IntervalIndexList l);
	
	/**
	 * Gets the sublist in the given interval
	 * @param start the start of the chop, inclusive
	 * @param stop the end of the chop, exclusive
	 * @return a sublist. It may be wired to the containing list,
	 * or may be a copy or immutable, depending on the implementor
	 */
	public IntervalIndexList subList(Comparable start, Comparable stop);
	
	/**
	 * Gets the start of the first element before c, exclusive.
	 * @param c Where to start looking backwards from
	 * @return the start of the last element that starts 
	 * before c; <code>null</code> if none found
	 */
	public Comparable firstBefore(Comparable c);
	
	/**
	 * aka startOf, gets the start of the first element before or at c.
	 * @param c Where to start looking backwards from
	 * @return the start of the last element that starts 
	 * before c; <code>null</code> if none found
	 */
	public Comparable firstBeforeOrAt(Comparable c);

	/**
	 * Gets the start of the first element that 
	 * begins at c or after it.
	 * @param c the place to start looking
	 * @return the start of the first element
	 * after or at c; <code>null</code> if none found
	 */
	public Comparable firstAfterOrAt(Comparable c);
	
	/**
	 * Gets the start of the first element that 
	 * begins strictly after c.
	 * @param c the place to start looking
	 * @return the start of the first element
	 * after c; <code>null</code> if none found
	 */
	public Comparable firstAfter(Comparable c);
	
	/**
	 * Gets the end of the element in which 
	 * c is contained.
	 * @param c the element to check
	 * @return the end, if found; <code>null</code> if c isn't within an interval
	 * described by this list
	 */
	public Comparable endOf(Comparable c);

	/**
	 * Removes all elements from the list.
	 */
	public void clear();

	/**
	 * True if no elements are described in the list.
	 * @return <code>!iterator().hasNext()</code>
	 */
	public boolean isEmpty();
	
	/**
	 * Changes all the values of the elements by 
	 * the given change function object.
	 * @param c the method to apply to all elements
	 */
	public abstract void map(Transformer c);

	/**
	 * Gets a count of the contiguous (and 
	 * homogenous, for those sort of things) intervals
	 * in the list.
	 * @return
	 */
	public int getContiguousIntervalCount();
}
