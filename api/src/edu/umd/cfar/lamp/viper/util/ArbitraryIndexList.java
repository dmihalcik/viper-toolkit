package edu.umd.cfar.lamp.viper.util;

import java.util.*;

/**
 * A list that is indexed with 'Comparable' instead of ints.
 */
public interface ArbitraryIndexList {
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
     * An iterator over all the {@link DynamicValue} elements
     * of the list.
     * @return the elements of the list, in order
     */
    public Iterator iterator ();
}