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

/*
 * Pair.java
 *
 * Created on May 30, 2002, 6:49 PM
 */

package edu.umd.cfar.lamp.viper.util;

import java.io.*;

/**
 * Simple object that replicate's c++'s stl pair facility. Often useful to
 * subclass with object specific features. Probably not included in java.util
 * because it tends to encourage instance-heavy programming, while c++'s pair is
 * often optimized out by the compiler.
 * 
 * @author davidm
 */
public class Pair implements Serializable {
	/**
	 * 1
	 */
	private static final long serialVersionUID = 1L;

	private Object first;

	private Object second;

	/**
	 * Creates a new instance of Pair
	 * 
	 * @param first
	 *            the first element of the pair
	 * @param second
	 *            the second element of the pair
	 */
	public Pair(Object first, Object second) {
		this.first = first;
		this.second = second;
	}

	/**
	 * Prints the pair as (first, second).
	 * 
	 * @return String representation of the data.
	 */
	public String toString() {
		return "(" + first + ", " + second + ")";
	}

	/**
	 * Gets a hashcode for the pair. Uses the same xor algorithm as defined for
	 * java.util.Map.Entry.
	 * 
	 * @return <code>first.hash ^ second.hash</code>, basically
	 */
	public int hashCode() {
		return (first == null ? 0 : first.hashCode())
				^ (second == null ? 0 : second.hashCode());
	}

	/**
	 * Uses first and second's .equals methods.
	 * 
	 * @param obj
	 *            the object to compare with
	 * @return <code>true</code> if respective elements in both Pairs are
	 *         reported as equal.
	 */
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		} else if (obj instanceof Pair) {
			Pair other = (Pair) obj;
			return (first == null ? null == other.first : first
					.equals(other.first))
					&& (second == null ? null == other.second : second
							.equals(other.second));
		} else {
			return false;
		}
	}

	/**
	 * Returns the object passed as the first argument to the constructor.
	 * 
	 * @return the first argument to the constructor
	 */
	public Object getFirst() {
		return first;
	}

	/**
	 * Returns the object passed as the second argument to the constructor.
	 * 
	 * @return the second argument to the constructor
	 */
	public Object getSecond() {
		return second;
	}

}