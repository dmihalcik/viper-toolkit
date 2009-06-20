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

import java.io.*;
import java.util.*;

import edu.umd.cfar.lamp.viper.util.*;

/**
 * A span specifies either a span of frames or of time.
 * Note that, while it implements <code>java.util.Set</code>,
 * it is immutable. It will throw Immutable exceptions if
 * you try to alter it through the Set methods. However,
 * you can use the Set query methods (contains, etc).
 * Right now, toArray fails. Really, you shouldn't call it
 * anyway, since it could create really huge arrays for tiny 
 * slices of time. Just use the iterator or something (and even
 * that creates a bunch of unnecessary temporaries).
 * Since a Span is contiguous, the real benefit from 
 * using it as a set is the 'contains' method, so you might
 * want to constrain yourself to that.
 */
public class Span extends AbstractInstantInterval implements Set, Serializable {
	public static Span EMPTY_FRAME_SPAN = new Span(new Frame(0), new Frame(0));
	public static Span EMPTY_TIME_SPAN = new Span(new Time(0), new Time(0));
	
	private Instant start;
	private Instant end;

	/**
	 * Creates a new span with the specified start and end (exclusive).
	 * Note that a Span must be homogenous, ie only use Frames or Times,
	 * but not both.
	 * @param s the first frame/nanosecond
	 * @param e the last frame/nanosecond
	 * @throws IllegalArgumentException if non homogenous, either is null, or end is less than start
	 */
	public Span(Instant s, Instant e) {
		start = s;
		end = e;
		if (s == null || e == null) {
			throw new IllegalArgumentException("Cannot have a span with a null endpoint: " + s + " to " + e);
		}
		if (((s instanceof Time) && (e instanceof Frame)) || ((s instanceof Frame) && (e instanceof Time))) {
			throw new IllegalArgumentException("Cannot have an mix frames with time in a single span: " + s + " to " + e);
		}
		if (getStart().compareTo(getEnd()) > 0) {
			throw new IllegalArgumentException("Invalid frame span: " + s + " to " + e);
		}
	}

	/**
	 * Parses a Span of Frame Instants in the form
	 * "11:20". Note the lack of white space. If 
	 * it is a single number, returns a span that
	 * covers just that instant.
	 * @param val the string value to parse
	 * @throws IllegalArgumentException if 
	 * 	the String val isn't in the proper form
	 * @return Span represenation of val
	 */
	public static Span parseFrameSpan(String val) {
		int i = val.indexOf(":");
		try {
			if (i < 0) {
				Frame f = Frame.parseFrame(val);
				return new Span(f, (Frame) f.next());
			} else {
				Frame s = Frame.parseFrame(val.substring(0, i));
				Frame e = Frame.parseFrame(val.substring(i + 1));
				return new Span(s, (Frame) e.next());
			}
		} catch (NumberFormatException nfx) {
			throw new IllegalArgumentException(
				"Invalid frame number in span "
					+ val
					+ "\n\t"
					+ nfx.getMessage());
		}
	}
	/**
	 * Parses a Span of Time Instants in the form
	 * "1107:1609". Note the lack of white space. If 
	 * it is a single number, returns a span that
	 * covers just that instant.
	 * @param val the string value to parse
	 * @throws IllegalArgumentException if 
	 * 	the String val isn't in the proper form
	 * @return Span represenation of val
	 */
	public static Span parseTimeSpan(String val) {
		int i = val.indexOf(":");
		try {
			if (i < 0) {
				Time f = Time.parseTime(val);
				return new Span(f, (Time) f.next());
			} else {
				Time s = Time.parseTime(val.substring(0, i));
				Time e = Time.parseTime(val.substring(i + 1));
				return new Span(s, (Time) e.next());
			}
		} catch (NumberFormatException nfx) {
			throw new IllegalArgumentException(
					"Invalid time in span "
						+ val
						+ "\n\t"
						+ nfx.getMessage());
		}
	}

	/**
	 * Intervals are immutable, so this operation is unimplemented.
	 * @see Set#add(java.lang.Object)
	 * @throws UnsupportedOperationException
	 */
	public boolean add(Object o) {
		throw new UnsupportedOperationException();
	}
	/**
	 * Intervals are immutable, so this operation is unimplemented.
	 * @see Set#addAll(java.util.Collection)
	 * @throws UnsupportedOperationException
	 */
	public boolean addAll(Collection c) {
		throw new UnsupportedOperationException();
	}
	/**
	 * Intervals are immutable, so this operation is unimplemented.
	 * @see Set#clear()
	 * @throws UnsupportedOperationException
	 */
	public void clear() {
		throw new UnsupportedOperationException();
	}
	/**
	 * Intervals are immutable, so this operation is unimplemented.
	 * @see Set#remove(java.lang.Object)
	 * @throws UnsupportedOperationException
	 */
	public boolean remove(Object o) {
		throw new UnsupportedOperationException();
	}
	/**
	 * Intervals are immutable, so this operation is unimplemented.
	 * @see Set#removeAll(java.util.Collection)
	 * @throws UnsupportedOperationException
	 */
	public boolean removeAll(Collection c) {
		throw new UnsupportedOperationException();
	}
	/**
	 * Intervals are immutable, so this operation is unimplemented.
	 * @see Set#retainAll(java.util.Collection)
	 * @throws UnsupportedOperationException
	 */
	public boolean retainAll(Collection c) {
		throw new UnsupportedOperationException();
	}
	/**
	 * This isn't implemented, because it is a bad idea.
	 * @see Set#toArray()
	 * @throws UnsupportedOperationException
	 */
	public Object[] toArray() {
		throw new UnsupportedOperationException();
	}
	/**
	 * This isn't implemented, because it is a bad idea.
	 * @see Set#toArray(java.lang.Object[])
	 * @throws UnsupportedOperationException
	 */
	public Object[] toArray(Object[] oa) {
		throw new UnsupportedOperationException();
	}

	/**
	 * Gets the start of the span.
	 * @return first Instant in the Span
	 */
	public Instant getStartInstant() {
		return start;
	}

	/**
	 * Gets the last instant of the span.
	 * @return last Instant in the Span
	 */
	public Instant getEndInstant() {
		return end;
	}
	
	/**
	 * Gets the last instant, meaning
	 * the instant that is within the interval
	 * that is greater than or equal to all elements
	 * of the interval.
	 * @return the last instant within the interval
	 */
	public Instant getLastInstant() {
		return (Instant) end.previous();
	}

	/**
	 * Checks to see that every element in the Collection of Instants
	 * c is within this Span.
	 * @param c the collection to check
	 * @return <code>true</code> iff all elements of c are within this Span
	 * @throws ClassCastException if some element of c is not the same type 
	 *    of Instant as the Span.
	 */
	public boolean containsAll(Collection c) {
		for (Iterator iter = c.iterator(); iter.hasNext();) {
			if (!contains(iter.next())) {
				return false;
			}
		}
		return true;
	}

	/**
	 * Gets an iterator that enumerates all the Instants
	 * in this Span. Not a good idea to use this.
	 * @return an iterator of Instants
	 */
	public Iterator iterator() {
		return new InstantIterator(getStartInstant(), getEndInstant());
	}

	/**
	 * Gets the span in old school, inclusive format.
	 * @return start:last, e.g. 1:1 for the first frame
	 */
	public String toString() {
		return getStartInstant() + ":" + getLastInstant();
	}

	private static class InstantIterator implements Iterator {
		private Instant next;
		private Instant last;
		InstantIterator(Instant start, Instant end) {
			this.next = start;
			this.last = end;
		}
		
		/**
		 * {@inheritDoc}
		 */
		public boolean hasNext() {
			return next.compareTo(last) <= 0;
		}
		
		/**
		 * {@inheritDoc}
		 */
		public Object next() {
			if (next.compareTo(last) > 0) {
				throw new NoSuchElementException(
					"Instant out of bounds: " + next);
			}
			Instant t = next;
			next = (Instant) next.next();
			return t;
		}
		
		/**
		 * Not implemented, as spans are both contiguous and 
		 * immutable.
		 * {@inheritDoc}
		 */
		public void remove() {
			throw new UnsupportedOperationException("Cannot remove Instants from Spans");
		}
	}
	
	/**
	 * {@inheritDoc}
	 */
	public Interval change(Comparable start, Comparable stop) {
		return new Span((Instant) start, (Instant) stop);
	}

	/**
	 * Dangerous - use width instead.
	 * @deprecated
	 */
	public int size() {
		long w = width();
		if (w > Integer.MAX_VALUE) {
			throw new IllegalStateException("Cannot use size on long-valued instant spans");
		}
		return (int) w;
	}
}
