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

public class CountingIterator implements Iterator {
	private int max;
	private int current;
	
	/**
	 * Counts from 0 to the max integer.
	 * @param max
	 */
	public CountingIterator() {
		this(0,Integer.MAX_VALUE);
	}

	/**
	 * Constructs a new counter that goes from 
	 * start through max.
	 * @param start the first number to return
	 * @param max the last number to return
	 */
	public CountingIterator(int start, int max) {
		super();
		this.current = start;
		this.max = max;
	}
	public boolean hasNext() {
		return current <= max;
	}
	public Object next() {
		if (!hasNext()) {
			throw new IndexOutOfBoundsException();
		}
		return new Integer(current++);
	}
	public void remove() {
		throw new UnsupportedOperationException();
	}
}
