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
 * An abstract base class for interval pairs
 * of Comparable objects, such as BigIntegers
 * or Instants.
 */
public abstract class AbstractInterval implements Interval {
	/**
	 * Gets a string version of the interval
	 * @return <q>[start, end)</q>
	 */
	public String toString() {
		return "[" + this.getStart() + ", "+ this.getEnd() + ")";
	}
	
	/**
	 * Tests to see if this refers to the same interval
	 * as the other. 
	 * @param o the interval to test against
	 * @return true if the intervals are the same
	 */
	public boolean equals (Object o) {
		if (this == o) {
			return true;
		} else if (o instanceof Interval) {
			Interval that = (Interval) o;
			return this.getStart().equals(that.getStart()) && this.getEnd().equals(that.getEnd());
		} else {
			return false;
		}
	}

	/**
	 * Gets a hash code.
	 * @return <code>getStart().hashCode() ^ getEnd().hashCode()</code>
	 */
	public int hashCode() {
		return getStart().hashCode() ^ getEnd().hashCode();
	}

	/**
	 * Tests to see if any time is taken in the Span.
	 * @return <code>true</code> if at least one Comparable is taken
	 */
	public boolean isEmpty() {
		return getStart().compareTo(getEnd()) >= 0;
	}

	/**
	 * Assuming s contains instants or spans of the same type (Time or Frame),
	 * this will work. Otherwise may throw a ClassCastException.
	 * 
	 * @param s set of Spans or Instants
	 * @return boolean
	 */
	public boolean intersects (Set s) {
		if (s instanceof Interval) {
			return intersects ((Interval) s);
		} else if (s instanceof Range) {
			return ((Range) s).intersects(this);
		} else {
			for (Iterator iter = s.iterator(); iter.hasNext(); ) {
				Object curr = iter.next();
				if ((curr instanceof Interval) && (this.intersects((Interval) curr)))  {
					return true;
				} else if ((curr instanceof Comparable) && (this.contains((Comparable) curr))) {
					return true;
				}
			}
			return false;
		}
	}
	
	/**
	 * Tests to see if this interval intersects
	 * the other one.
	 * @param other the other interval to test
	 * @return <code>true</code> if there exists some 
	 * object such that it is contained within this and the other
	 * interval.
	 */
	public boolean intersects (Interval other) {
		double diffStartEnd = this.getStart().compareTo(other.getEnd());
		if (diffStartEnd >= 0) {
			return false;
		}
		double diffEndStart = this.getEnd().compareTo(other.getStart());
		if (diffEndStart <= 0) {
			return false;
		}
		return true;
	}

	/**
	 * Tests to see if the given object is contained within
	 * the interval. If it isn't a type that can be contained
	 * within the interval, returns false. Otherwise,
	 * it invokes one of the more specific versions of 
	 * <code>contains()</code>.
	 * @param o the object to test
	 * @return true if it is greater than or equal to start and strictly
	 * less than end
	 */
	public boolean contains(Object o) {
		// Hideous code necessary to handle multiple cases.
		// I really miss multimethods, or whatever those things 
		// are called that handle this sort of thing.
		if (o instanceof Comparable) {
			return contains ((Comparable) o);
		} else if (o instanceof Interval){
			return contains((Interval) o);
		} else if (o instanceof Set) {
			return contains ((Set) o);
		} else {
			return false;
		}
	}

	/**
	 * Tests to see if the given item is within the interval.
	 * @param i the comparable object to look for
	 * @return true if it is greater than or equal to start and strictly
	 * less than end
	 */
	public boolean contains (Comparable i) {
		return this.getStart().compareTo(i) <= 0 && this.getEnd().compareTo(i) > 0;
	}
	/**
	 * Tests to see if the other interval is a subset
	 * of this interval.
	 * @param other the interval to test
	 * @return true if other is a subset of this
	 */
	public boolean contains (Interval other) {
		return this.contains(other.getStart()) && (this.getEnd().compareTo(other.getEnd()) >= 0);
	}

	/**
	 * Tests to see that all elements of the set are contained
	 * within this interval.
	 * @param s the items to check for
	 * @return true if this contains every element of s
	 */
	public boolean contains (Set s) {
		for (Iterator iter = s.iterator(); iter.hasNext(); ) {
			Object curr = iter.next();
			if (curr instanceof Comparable) {
				if (!this.contains((Comparable) curr)) {
					return false;
				}
			} else if (curr instanceof Interval) {
				if (!this.contains((Interval) curr)) {
					return false;
				}
			} else {
				return false;
			}
		}
		return true;
	}

	/**
	 * Not implemented; throws unsupported operation exception.
	 * @param start the start (inclusive)
	 * @param stop the end (exclusive)
	 * @return the new interval
	 * @throws UnsupportedOperationException
	 */
	public Interval change(Comparable start, Comparable stop) {
		throw new UnsupportedOperationException();
	}
}
