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

import java.util.*;

import viper.api.*;
import edu.umd.cfar.lamp.viper.util.*;

/**
 * A list of integers encoded by time.
 */
public class TimeEncodedIntegerVector extends TimeEncodedList {
	/**
	 * Constructs an empty set of integers.
	 */
	public TimeEncodedIntegerVector() {
		super();
	}
	
	/**
	 * Sets the given span to take on the given integer value.
	 * @param start the start of the span, inclusive
	 * @param end the end of the span, exclusive
	 * @param i the value for the interval
	 * @see #set(Comparable, Comparable, int)
	 */
	public void set(Comparable start, Comparable end, int i) {
		super.set(start, end, new Integer(i));
	}

	/**
	 * Adds the given values to the current vector.
	 * Regions with no integer are assumed to have the
	 * value zero.
	 * @param o the numbers to add
	 */
	public void plus(TemporalRange o) {
		addThings(o.iterator());
	}
	private void addThings(Iterator iter) {
		while (iter.hasNext()) {
			Interval i_c = (Interval) iter.next();
			int addend = 1;
			if (i_c instanceof DynamicAttributeValue) {
				Object v = ((DynamicAttributeValue) i_c).getValue();
				if (v instanceof Numeric) {
					addend = ((Numeric) v).intValue();
				} else if (v instanceof Number) {
					addend = ((Number) v).intValue();
				}
			}
			Iterator me = this.iterator(i_c);
			if (me.hasNext()) {
				Instant lastEndInM = (Instant) i_c.getStart();
				List toSet = new LinkedList();
				do {
					DynamicAttributeValue i_m =
						(DynamicAttributeValue) me.next();
					int currVal = ((Integer) i_m.getValue()).intValue() + addend;
					if (lastEndInM.compareTo(i_m.getStart()) < 0) {
						// there was a break in this
						toSet.add(new TemporalObject(lastEndInM, (Instant) i_m.getStart(), new Integer(addend))); 
					}
					if (i_c.getEnd().compareTo(lastEndInM) <= 0) {
						// i_c ends before or at the end of i_m
						Instant t_e = (Instant) i_c.getEnd();
						toSet.add(new TemporalObject(lastEndInM, t_e, new Integer(currVal)));
						break;
					}
					lastEndInM = (Instant) i_m.getEnd();
					if (i_m.getStart().compareTo(i_c.getStart()) < 0) {
						// i_c starts after i_m starts; only happens on first one
						toSet.add (new TemporalObject((Instant) i_c.getStart(), (Instant) i_m.getEnd(),
							new Integer(currVal)));
					} else {
						// i_m is inside i_c
						toSet.add (new TemporalObject((Instant) i_m.getStart(), (Instant) i_m.getEnd(),
							new Integer(currVal)));
					}
				} while (me.hasNext());
				if (lastEndInM.compareTo(i_c.getEnd()) < 0) {
					toSet.add(new TemporalObject(lastEndInM, (Instant) i_c.getEnd(), new Integer(addend)));
				} 
				for(Iterator neos = toSet.iterator(); neos.hasNext(); ) {
					DynamicValue curr = (DynamicValue) neos.next();
					this.set(curr, curr.getValue());
				}
			} else {
				this.set(i_c, new Integer(addend));
			}
		}
	}
}
