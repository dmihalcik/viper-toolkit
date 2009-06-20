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

/**
 * Implements the interval asTime and asFrame methods in terms
 * of the corresponding instant methods. Those are wrapped into
 * asFrame(Time) and asTime(Frame), to make it easy to implement: e.g.
 * asFrame(Instant) automatically returns the argument if it is a Frame
 * or <code>null</code>, throwing a class cast if it is not a Time after that.
 * Compare is implemented in terms of Time.
 */
public abstract class AbstractFrameRate implements FrameRate {
	protected abstract Time asTime(Frame t);
	protected abstract Frame asFrame(Time f);

	/**
	 * Gets the Frame corresponding to the instant.
	 * @param i the instant to cast to frame
	 * @return either the instant, or a copy of it as a frame
	 */
	public Frame asFrame(Instant i) {
		if (i == null) {
			return null;
		} else if (i instanceof Frame) {
			return (Frame) i;
		} else {
			return asFrame((Time) i);
		}
	}
	
	/**
	 * Converts the given instant into a Time object.
	 * @param i the instant to convert. May be Frame or Time based.
	 * @return the instant expressed as times
	 */
	public Time asTime(Instant i) {
		if (i == null) {
			return null;
		} else if (i instanceof Time) {
			return (Time) i;
		} else {
			return asTime((Frame) i);
		}
	}

	/**
	 * Converts the given interval into a Frame interval.
	 * @param s the interval to convert. May be Frame or Time based.
	 * @return the interval expressed as frames
	 */
	public InstantInterval asFrame(InstantInterval s) {
		if (s == null) {
			return null;
		} else if (s.isFrameBased()) {
			return s;
		} else {
			return (InstantInterval) s.change(
				asFrame(s.getStartInstant()),
				asFrame(s.getEndInstant()));
		}
	}
	
	/**
	 * Converts the given interval into a Time interval.
	 * @param s the interval to convert. May be Frame or Time based.
	 * @return the interval expressed as times
	 */
	public InstantInterval asTime(InstantInterval s) {
		if (s == null) {
			return null;
		} else if (s.isTimeBased()) {
			return s;
		} else {
			return (InstantInterval) s.change(
				asTime(s.getStartInstant()),
				asTime(s.getEndInstant()));
		}
	}

	/**
	 * Compares the value of two instant objects, not necessarily of the same
	 * unit.
	 * @param a the first Instant
	 * @param b the second Instant
	 * @return like a-b, returns negative if a is less than b, zero if equal, and positive if more.
	 */
	public int compare(Object a, Object b) {
		if (a instanceof Frame && b instanceof Frame) {
			return ((Frame) a).compareTo(b);
		}
		return asTime((Instant) a).compareTo(asTime((Instant) b)); 
	}
}
