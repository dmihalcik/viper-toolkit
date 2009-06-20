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
 * A range of Instants - basically, a set of 
 * non-overlapping Intervals, or a set of Instant
 * objects.
 */
public class InstantRange extends Range implements TemporalRange, Serializable {
	/**
	 * Parses a Range of Frames in the form of a white-space
	 * delimited sequence of Span tokens. For example,
	 * "12:19 24 30:100" would be three Spans of Frames:
	 * one from 12 to 19, another that represents just Frame 24, 
	 * and finally the Span of Frames from 30 to 100, inclusive.
	 * Also parses half open ranges, where the above is 
	 * "[12,20) [24,25) [30,101)".
	 * @param val white space delimited sequence of numbers
	 * 	or two increasing numbers seperated by a colon.
	 * @return A new Range object representative of the value
	 */
	public static InstantRange parseFrameRange(String val) {
		if(val.startsWith("[")){
			StringTokenizer st = new StringTokenizer(val, " ,;\t[]()");
			InstantRange range = new InstantRange();
			while(st.hasMoreTokens()){
				String begin = st.nextToken();
				String end = st.nextToken();
				Frame beginFrame, endFrame;
				beginFrame = new Frame(Integer.parseInt(begin));
				endFrame = new Frame(Integer.parseInt(end));
				range.add(beginFrame, endFrame);
			}
			return range;
		}else{
			StringTokenizer st = new StringTokenizer(val, " ,;\t");
			InstantRange ret = new InstantRange();
			while (st.hasMoreTokens()) {
				String curr = st.nextToken();
				if (curr.length() > 0) {
					ret.add(Span.parseFrameSpan(curr));
				}
			}
			return ret;
		}
	}

	/**
	 * Parses a Range of Times in the form of a white-space
	 * delimited sequence of Span tokens. For example,
	 * "12:19 24 30:100" would be three Spans of Times:
	 * one from 12 to 19, another that represents just the 
	 * 24th microsecond, and finally the Span of Times from 
	 * the 30th microsecond to 100th, inclusive.
	 * @param val white space delimited sequence of numbers
	 * 	or two increasing numbers seperated by a colon.
	 * @return A new Range object representative of the value
	 */
	public static InstantRange parseTimeRange(String val) {
		StringTokenizer st = new StringTokenizer(val);
		InstantRange ret = new InstantRange();
		while (st.hasMoreTokens()) {
			String curr = st.nextToken();
			ret.add(Span.parseTimeSpan(curr));
		}
		return ret;
	}

	/**
	 * Checks to see if the span is inside the Range.
	 * @param s The first Instant to check for.
	 * @param e The last Instant to check.
	 * @return <code>true</code> iff all Instants in [s,e] are in this
	 */
	public boolean withinRangeInclusive(Instant s, Instant e) {
		return withinRange(s, e.next());
	}

	/**
	 * {@inheritDoc}
	 */
	public boolean add(Object o) {
		if (o instanceof Instant) {
			return add((Instant) o, ((Instant) o).next());
		} else { 
			return add(((Interval) o).getStart(), ((Interval) o).getEnd());
		}
	}

	/**
	 * {@inheritDoc}
	 */
	public boolean remove(Object o) {
		if (o instanceof Instant) {
			Instant i = (Instant) o;
			return remove(i, i.next());
		} else {
			Interval i = (Interval) o;
			return remove(i.getStart(), i.getEnd());
		}
	}

	/**
	 * {@inheritDoc}
	 */
	public Interval getExtrema() {
		Interval i = super.getExtrema();
		if (i == null) {
			return Span.EMPTY_FRAME_SPAN;
		}
		return new Span ((Instant) i.getStart(), (Instant) i.getEnd());
	}

	/**
	 * {@inheritDoc}
	 */
	public boolean isFrameBased() {
		if (size()>0) {
			return ((Instant) super.getExtrema().getStart()) instanceof Frame;
		}
		return false;
	}

	/**
	 * {@inheritDoc}
	 */
	public boolean isTimeBased() {
		if (size()>0) {
			return ((Instant) super.getExtrema().getStart()) instanceof Time;
		}
		return false;
	}

	/**
	 * Tests to see if the two ranges intersect somewhere.
	 * @param alpha a range to test
	 * @param beta another range
	 * @return if they intersect
	 */
	public static boolean intersect(TemporalRange alpha, TemporalRange beta) {
		if (alpha.getExtrema().intersects(beta.getExtrema())) {
			Iterator a = alpha.iterator();
			Iterator b = beta.iterator();
			Interval i_a, i_b;
			if (a.hasNext() && b.hasNext()) {
				i_a = (Interval) a.next();
				i_b = (Interval) b.next();
				do {
					double f = i_a.getEnd().compareTo(i_b.getEnd());
					if (f == 0) {
						return true;
					} else if (f > 0) { // a ends after b
						Iterator t = b; Interval i_t = i_b;
						b = a; i_b = i_a;
						a = t; i_a = i_t;
					} 
					// b ends after a ends 
					if (i_b.getStart().compareTo(i_a.getEnd()) < 0) {
						// b starts before a ends
						return true;
					} else if (a.hasNext()) {
						i_a = (Interval) a.next();
					} else {
						// a ends before b starts, and there is nothing left
						return false;
					}
				} while (a.hasNext() && b.hasNext());
			}
		}
		return false;
	}

	/**
	 * {@inheritDoc}
	 */
	public boolean intersects(TemporalRange other) {
		return InstantRange.intersect(this, other);
	}

	/**
	 * {@inheritDoc}
	 */
	public Object clone() {
		InstantRange nir = new InstantRange();
		super.cloneInto(nir);
		return nir;
	}


	/**
	 * {@inheritDoc}
	 */
	public Interval createInterval(Comparable s, Comparable e) {
		return new Span((Instant) s, (Instant) e);
	}
	
	/** @inheritDoc */
	public void shift(Instant amount) {
		if ((this.isFrameBased() && !(amount instanceof Frame)) ||
				(this.isTimeBased() && !(amount instanceof Time))) {
			throw new IllegalArgumentException("Shifting by wrong unit type");
		}
		long shift = amount.longValue();
		if (shift < 0) {
			Instant curr = (Instant) super.getExtrema().getStart();
			while (curr!= null) {
				Instant end = (Instant) super.endOf(curr);
				super.remove(curr, end);
				super.add(curr.go(shift), end.go(shift));
				curr = (Instant) super.firstAfterOrAt(end);
			}
		} else if (shift > 0) {
			Instant curr = (Instant) super.getExtrema().getEnd();
			Instant start = (Instant) super.firstBefore(curr);
			while (start != null) {
				curr = (Instant) super.endOf(start);
				super.remove(start, curr);
				super.add(start.go(shift), curr.go(shift));
				start = (Instant) super.firstBefore(start);
			}
		}
	}

	/**
	 * Crops the range so that it is down to this size.
	 * @param validSpan
	 */
	public void crop(InstantInterval validSpan) {
		if ((this.isFrameBased() && !validSpan.isFrameBased()) ||
				(this.isTimeBased() && !validSpan.isTimeBased())) {
			throw new IllegalArgumentException("Shifting by wrong unit type");
		}
		Interval extrema = super.getExtrema();
		if (extrema.contains(validSpan)) {
			return;
		}
		Comparable before = firstBefore(validSpan.getStart());
		if (before != null) {
			remove(extrema.getStart(), validSpan.getStart());
		}
		Comparable after = firstAfter(validSpan.getEnd());
		if (after == null) {
			after = endOf(validSpan.getEnd());
		}
		if (after != null) {
			remove(validSpan.getEnd(), extrema.getEnd());
		}
	}
}
