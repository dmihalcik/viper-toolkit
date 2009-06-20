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

package viper.api.time;

import java.text.*;

import edu.umd.cfar.lamp.viper.util.*;
/**
 * Represents a unit time, usually a microsecond. Functionally equivalent to the
 * Frame object, this type uses long integers internally to allow larger ranges
 * of values. While it is unlikely a video will have more than 2^31 frames, it
 * is possible that it will contain more than 2^31 nanoseconds, and this may be
 * useful to track.
 */
public class Time extends Number implements Instant {
	private long currTime;

	/**
	 * The first moment in time this object can represent.
	 */
	public static Time ALPHA = new Time(Long.MIN_VALUE);

	/**
	 * The last moment in time this object can represent.
	 */
	public static Time OMEGA = new Time(Long.MAX_VALUE);

	/**
	 * Construct a new Time with the given value.
	 * 
	 * @param i
	 *            the time
	 */
	public Time(long i) {
		currTime = i;
	}

	/**
	 * Parses a string in the form of an integer as a time.
	 * 
	 * @param val
	 *            an integer that represents an instant in time.
	 * @throws IllegalArgumentException
	 *             if val isn't a valid int
	 * @return new Time representation of the value stored in the String
	 */
	public static Time parseTime(String val) {
		return new Time(Long.parseLong(val));
	}
	/**
	 * Gets the value of the time.
	 * 
	 * @return the value of the time as a long
	 */
	public long getTime() {
		return currTime;
	}

	/////// Interface implementation

	/**
	 * {@inheritDoc}
	 */
	public Incrementable next() {
		return new Time(currTime + 1);
	}

	/**
	 * {@inheritDoc}
	 */
	public Incrementable previous() {
		return new Time(currTime - 1);
	}

	/**
	 * {@inheritDoc}
	 */
	public long minus(Instant i) {
		return currTime - ((Time) i).currTime;
	}

	/**
	 * {@inheritDoc}
	 */
	public int compareTo(Object o) {
		Time that = (Time) o;
		long l = this.currTime - that.currTime;
		if (l == 0) {
			return 0;
		} else if (l < 0) {
			return -1;
		} else {
			return 1;
		}
	}

	/**
	 * If i is a Time, tests to see that this is greater (later) than i.
	 * 
	 * @param i
	 *            the Time to test against.
	 * @throws ClassCastException
	 *             if i is not a Time
	 * @return <code>true</code> iff this Time is later than i
	 */
	public boolean isGreater(Instant i) {
		if (currTime > ((Time) i).currTime)
			return true;

		return false;
	}

	/**
	 * If i is a Time, tests to see that this is less (earlier) than i.
	 * 
	 * @param t
	 *            the Time to test against.
	 * @throws ClassCastException
	 *             if i is not a Time
	 * @return <code>true</code> iff this Time is earlier than i
	 */
	public boolean isLess(Instant t) {
		if (currTime < ((Time) t).currTime)
			return true;

		return false;
	}

	// Object

	/**
	 * {@inheritDoc}
	 */
	public boolean equals(Object o) {
		if (this == o || (o instanceof Time && currTime == ((Time) o).currTime)) {
			return true;
		}
		return false;
	}

	/**
	 * {@inheritDoc}
	 */
	public String toString() {
		return NumberFormat.getInstance().format(this.currTime);
	}

	/**
	 * {@inheritDoc}
	 */
	public int hashCode() {
		return (int) (currTime ^ (currTime >>> 32));
	}

	/**
	 * {@inheritDoc}
	 */
	public Instant go(long diff) {
		return new Time(currTime + diff);
	}

	/**
	 * {@inheritDoc}
	 */
	public long longValue() {
		return getTime();
	}

	/**
	 * {@inheritDoc}
	 */
	public int intValue() {
		if (0xFFFFFFFFL != (getTime() | 0xFFFFFFFFL)) {
			throw new ArithmeticException();
		}
		return (int) getTime();
	}

	/**
	 * {@inheritDoc}
	 */
	public double doubleValue() {
		return getTime();
	}

	/**
	 * {@inheritDoc}
	 */
	public float floatValue() {
		return getTime();
	}
}