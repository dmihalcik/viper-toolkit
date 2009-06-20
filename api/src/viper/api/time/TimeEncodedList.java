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

import org.apache.commons.collections.*;

import viper.api.*;
import edu.umd.cfar.lamp.viper.util.*;

/**
 * A list, with the elements run length encoded. It offers log n 
 * access and edit time, on average. Note that it uses the 
 * <tt>equals</tt> method to check equality, not <tt>==</tt>, so 
 * <code>null</code> elements will not be accepted. 
 * Setting a range to <code>null</code> is the same as removing that range.
 * It does not implement java.util.List, as it takes 
 * {@link viper.api.time.Instant} indexes instead of ints. 
 * This is a modified version of viper.util.LengthwiseEncodedList 
 * that uses Instants instead of the more general Comparable.
 */
public class TimeEncodedList implements Cloneable, TemporalRange, Serializable {
	/**
	 * This is stored as a java.util.TreeMap (red/black tree, I think), 
	 * with the keys being the first frame, and the values containing
	 * the object value and the last frame, exclusive. 
	 */
	private TreeMap values;

	/** 
	 * Creates a new instance of LengthwiseEncodedList.
	 */
	public TimeEncodedList() {
		values = new TreeMap();
	}
	

	/**
	 * {@inheritDoc}
	 */
	public void clear () {
		values.clear();
	}

	/**
	 * Tests to see if the list is encoded by Time (instead 
	 * of some Frame).
	 * @return <code>true</code> iff the List contains values
	 *  and they are indexed by Time.
	 */
	public boolean isTimeBased() {
		return (values.size() > 0) && (values.firstKey() instanceof Time);
	}

	/**
	 * Tests to see if the list is encoded by frame (instead 
	 * of some Time).
	 * @return <code>true</code> iff the List contains values
	 *  and they are indexed by Frame.
	 */
	public boolean isFrameBased() {
		return (values.size() > 0) && (values.firstKey() instanceof Frame);
	}

	/**
	 * {@inheritDoc}
	 */
	public boolean equals(Object o) {
		if (o == this) {
			return true;
		} else if (o instanceof TimeEncodedList) {
			return this.values.equals(((TimeEncodedList) o).values);
		} else {
			return false;
		}
	}

	/**
	 * {@inheritDoc}
	 */
	public int hashCode() {
		return values.hashCode();
	}

	/**
	 * {@inheritDoc}
	 */
	public Object clone() {
		TimeEncodedList nl = new TimeEncodedList();
		nl.values.putAll(values);
		return nl;
	}

	/**
	 * Get the value at the specified index in the list.
	 * @param index the index into the list
	 * @return the value at the specified index
	 */
	public Object get(Instant index) {
		assert index != null;
		SortedMap m = values.tailMap(index);
		if (!m.isEmpty() && m.firstKey().equals(index)) {
			return ((LelNode) m.get(index)).getValue();
		}
		m = values.headMap(index);
		if (!m.isEmpty()) {
			LelNode v = (LelNode) m.get(m.lastKey());
			if (v.getEnd().compareTo(index) > 0) {
				return v.getValue();
			}
		}
		return null;
	}

	/**
	 * Sets the value at the given interval. 
	 * @param span the interval to modify
	 * @param value the new value to take on for the interval
	 */
	public void set(Interval span, Object value) {
		set(span.getStart(), span.getEnd(), value);
	}

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
	public void set(Comparable start, Comparable stop, Object value) {
		if (start.compareTo(stop) >= 0) {
			throw new IllegalArgumentException(
				"Start not strictly less than stop: " + start + " !< " + stop);
		}
		if (value == null) {
			remove(start, stop);
			return;
		}
		SortedMap head = values.headMap(start);
		if (!head.isEmpty()) {
			LelNode n = (LelNode) head.get(head.lastKey());
			if (n.getValue().equals(value)) {
				if (n.getEnd().compareTo(start) >= 0) {
					start = (Instant) head.lastKey();
				}
			} else if (n.getEnd().compareTo(start) > 0) {
				head.put(
					head.lastKey(),
					new LelNode((Instant) start, n.getValue()));
				if (n.getEnd().compareTo(stop) > 0) {
					values.put(start, new LelNode((Instant) stop, value));
					values.put(stop, new LelNode(n.getEnd(), n.getValue()));
					return;
				}
			}
		}
		SortedMap sub = values.subMap(start, stop);
		if (!sub.isEmpty()) {
			LelNode n = (LelNode) sub.get(sub.lastKey());
			if (n.getValue().equals(value)) {
				if (n.getEnd().compareTo(stop) > 0) {
					stop = n.getEnd();
				}
			} else if (n.getEnd().compareTo(stop) > 0) {
				values.put(stop, new LelNode(n.getEnd(), n.getValue()));
			}
		}
		values.subMap(start, stop).clear();
		values.put(start, new TimeEncodedList.LelNode((Instant) stop, value));
	}

	/**
	 * Removes all values at the given range. Note that, like 
	 * <code>SortedMap</code>, this means that value is set in the
	 * range from start, inclusive, to stop, exclusive.
	 * @param start the first index to remove
	 * @param stop the first index that is not removed
	 * @return <code>true</code> if any elements were removed
	 */
	public boolean remove(Comparable start, Comparable stop) {
		boolean someFound = false;
		SortedMap head = values.headMap(start);
		if (!head.isEmpty()) {
			LelNode n = (LelNode) head.get(head.lastKey());
			if (n.getEnd().compareTo(start) > 0) {
				someFound = true;
				head.put(
					head.lastKey(),
					new LelNode((Instant) start, n.getValue()));
				if (n.getEnd().compareTo(stop) > 0) {
					// this object spans the whole removed range
					values.put(
						stop,
						new TimeEncodedList.LelNode(n.getEnd(), n.getValue()));
					return true;
				}
			}
		}

		// By now, will have removed anything coming in from the head
		// Just remove the stuff in the subMap, making sure to put back
		// the leftovers.
		SortedMap sub = values.subMap(start, stop);
		if (!sub.isEmpty()) {
			LelNode n = (LelNode) sub.get(sub.lastKey());
			if (n.getEnd().compareTo(stop) > 0) {
				values.put(stop, new LelNode(n.getEnd(), n.getValue()));
			}
			values.subMap(start, stop).clear();
			return true;
		}
		return someFound;
	}

	/**
	 * {@inheritDoc}
	 */
	public String toString() {
		StringBuffer sb = new StringBuffer();
		for (Iterator iter = values.entrySet().iterator(); iter.hasNext();) {
			Map.Entry currEntry = (Map.Entry) iter.next();
			Instant currStart = (Instant) currEntry.getKey();
			Instant currEnd = ((LelNode) currEntry.getValue()).getEnd();
			Object currValue = ((LelNode) currEntry.getValue()).getValue();
			sb.append(currStart).append(':').append(currEnd);
			sb.append("*(\"").append(
				StringHelp.backslashify(currValue.toString())).append(
				"\")");
			if (iter.hasNext()) {
				sb.append(", ");
			}
		}
		return sb.toString();
	}

	/**
	 * Gets an iterator over the given interval.
	 * This is an intersection operation.
	 * @param i the interval to intersect with
	 * @return an iterator of {@link Interval} objects 
	 * over the given interval
	 */
	public Iterator iterator(Interval i) {
		return new TimeEncodedList.CroppedIterator(this, i);
	}

	/**
	 * {@inheritDoc}
	 */
	public Iterator iterator() {
		return new TimeEncodedList.DynamicValueIterator(this);
	}

	/**
	 * Represents an object over a span of time/frames.
	 */
	private class LelNode extends Pair implements Serializable {
		LelNode(Instant e, Object v) {
			super(e, v);
			if (v == null) {
				throw new IllegalStateException();
			}
		}
		Instant getEnd() {
			return (Instant) super.getFirst();
		}
		Object getValue() {
			return super.getSecond();
		}
	}

	/**
	 * A useful class for representing an object bound to an
	 * interval.
	 */
	public static final class DynamicAttributeValueImpl
		extends AbstractInstantInterval
		implements DynamicAttributeValue {
		private InstantInterval s;
		private Object v;
		/**
		 * Creates a new DynamicAttributeValueImpl backed by the 
		 * Map.Entry o.
		 * @param o A Map.Entry mapping start time to a LelNode
		 */
		DynamicAttributeValueImpl(Object o) {
			Map.Entry v = (Map.Entry) o;
			LelNode ln = (LelNode) v.getValue();
			this.s = new Span((Instant) v.getKey(), ln.getEnd());
			this.v = ln.getValue();
			assert getValue() != null;
			assert getEnd() != null;
			assert getStart() != null;
		}
		/**
		 * Gets a new instance with the given span and value.
		 * 
		 * @param s The span in which the time encoded list/whatever has the given value
		 * @param v The value
		 */
		public DynamicAttributeValueImpl(InstantInterval s, Object v) {
			this.s = s;
			this.v = v;
		}
		/**
		 * Gets a new DynamicAttributeValue with the given span and 
		 * value.
		 * @param s The first Instant
		 * @param e The last Instant
		 * @param v The value that is holds over the given set of Instants
		 */
		public DynamicAttributeValueImpl(Instant s, Instant e, Object v) {
			this.s = new Span(s, e);
			this.v = v;
		}

		/**
		 * {@inheritDoc}
		 */
		public Object getValue() {
			return v;
		}

		/**
		 * {@inheritDoc}
		 */
		public Instant getStartInstant() {
			return s.getStartInstant();
		}

		/**
		 * {@inheritDoc}
		 */
		public Instant getEndInstant() {
			return s.getEndInstant();
		}

		/**
		 * Hashes based on the interval and the value.
		 * {@inheritDoc}
		 */
		public int hashCode() {
			int hash = super.hashCode();
			if (getValue() != null) {
				hash ^= getValue().hashCode();
			}
			return hash;
		}

		/**
		 * {@inheritDoc}
		 */
		public boolean equals(Object o) {
			if ((o instanceof DynamicAttributeValue) && super.equals(o)) {
				DynamicAttributeValue that = (DynamicAttributeValue) o;
				if (this.getValue() == that.getValue()) {
					return true;
				} else if (
					this.getValue() == null || that.getValue() == null) {
					return false;
				} else {
					return getValue().equals(that.getValue());
				}
			} else {
				return false;
			}
		}

		/**
		 * {@inheritDoc}
		 * @return <code>"value"@[start,end)</code>
		 */
		public String toString() {
			return "\""
				+ StringHelp.backslashify(getValue().toString())
				+ "\"@"
				+ super.toString();
		}

		/**
		 * {@inheritDoc}
		 */
		public Interval change(Comparable start, Comparable stop) {
			return new DynamicAttributeValueImpl(
				new Span((Instant) start, (Instant) stop),
				getValue());
		}
	}

	private static final class DynamicValueIterator implements Iterator {

		private Iterator t;
		private DynamicAttributeValue cachedNext;
		protected DynamicAttributeValue cache() {
			DynamicAttributeValue old = cachedNext;
			if (t.hasNext()) {
				cachedNext = new DynamicAttributeValueImpl(t.next());
			} else {
				cachedNext = null;
			}
			return old;
		}
		DynamicValueIterator(TimeEncodedList lel) {
			t = lel.values.entrySet().iterator();
			cache();
		}
		DynamicValueIterator(Map valuesmap) {
			t = valuesmap.entrySet().iterator();
			cache();
		}

		/**
		 * {@inheritDoc}
		 */
		public Object next() {
			if (hasNext()) {
				return cache();
			}
			throw new NoSuchElementException();
		}

		/**
		 * {@inheritDoc}
		 */
		public boolean hasNext() {
			return cachedNext != null;
		}

		/**
		 * Not implemented.
		 * {@inheritDoc}
		 */
		public void remove() {
			throw new UnsupportedOperationException();
		}
	}

	private static final class CroppedIterator implements Iterator {
		private Iterator t;
		private DynamicAttributeValue cachedNext;
		private Interval sub;
		protected DynamicAttributeValue cache() {
			DynamicAttributeValue old = cachedNext;
			if (t.hasNext()) {
				cachedNext = new DynamicAttributeValueImpl(t.next());
				if (!t.hasNext() && cachedNext.getEnd().compareTo(sub.getEnd()) > 0) {
					// the first call is to avoid unnecessary comparisons
					// this line is only ever called on the last lel node whose key 
					// is within the interval 'sub', so we know that it intersects 
					// 'sub', as it starts within it and ends after it. 
					cachedNext = (DynamicAttributeValue) Intervals.intersection(cachedNext, sub);
				}
			} else {
				cachedNext = null;
			}
			return old;
		}
		CroppedIterator(TimeEncodedList lel, Interval sub) {
			this.sub = sub;
			Instant subStart = (Instant) sub.getStart();
			Instant subEnd = (Instant) sub.getEnd();
			SortedMap head = lel.values.headMap(subStart);
			t = lel.values.subMap(sub.getStart(), sub.getEnd()).entrySet().iterator();
			if (head != null && head.size() > 0) {
				Instant firstStart = (Instant) head.lastKey();
				LelNode ln = (LelNode) head.get(firstStart);
				Instant firstEnd = ln.getEnd();
				if (firstEnd.compareTo(subStart) <= 0) {
					cache();
				} else {
					Instant end = firstEnd;
					if (firstEnd.compareTo(sub.getEnd()) > 0) {
						end = subEnd;
					}
					cachedNext = new DynamicAttributeValueImpl(subStart, end, ln.getValue());
				}
			} else {
				cache();
			}
		}

		/**
		 * {@inheritDoc}
		 */
		public Object next() {
			if (hasNext()) {
				return cache();
			}
			throw new NoSuchElementException();
		}

		/**
		 * {@inheritDoc}
		 */
		public boolean hasNext() {
			return cachedNext != null;
		}

		/**
		 * Not implemented.
		 * {@inheritDoc}
		 */
		public void remove() {
			throw new UnsupportedOperationException();
		}
	}

	/**
	 * {@inheritDoc}
	 */
	public Object get(Comparable index) {
		return get((Instant) index);
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
	public Interval getExtrema() {
		if (values.size() > 0) {
			Instant first = (Instant) values.firstKey();
			LelNode lastPair = (LelNode) values.get(values.lastKey());
			return new Span(first, lastPair.getEnd());
		} else {
			return new Span(Frame.ALPHA, Frame.ALPHA);
		}
	}

	/**
	 * {@inheritDoc}
	 */
	public boolean addAll(IntervalIndexList l) {
		Iterator iter = l.iterator();
		boolean changed = false; 
		while (iter.hasNext()) {
			changed = true;// XXX addAll returns true even if nothing changes
			Interval c = (Interval) iter.next();
			if (c instanceof DynamicValue) {
				this.set(c, ((DynamicValue) c).getValue());
			} else {
				this.set(c, Boolean.TRUE);
			}
		}
		return changed;
	}

	/**
	 * Looks for the time, not the object.
	 * @param o the comparable object to check for
	 * @return if the specified object is within the range
	 * @throws IllegalArgumentException if o isn't comparable
	 */
	public boolean contains(Object o) {
		if (o == this) {
			return true;
		} else if (o instanceof Comparable) {
			return this.get((Comparable) o) != null;
		} else {
			throw new IllegalArgumentException(o.toString());
		}
	}

	/**
	 * {@inheritDoc}
	 */
	public IntervalIndexList subList(Comparable start, Comparable stop) {
		throw new UnsupportedOperationException();
	}

	/**
	 * {@inheritDoc}
	 */
	public Comparable firstBefore(Comparable c) {
		SortedMap head = values.headMap(c);
		if (!head.isEmpty()) {
			return (Comparable) head.lastKey();
		}
		return null;
	}
	public Comparable firstBeforeOrAt(Comparable c) {
		if (values.containsKey(c)) {
			return c;
		}
		return firstBefore(c);
	}

	/**
	 * {@inheritDoc}
	 */
	public Comparable firstAfterOrAt(Comparable c) {
		SortedMap tail = values.tailMap(c);
		if (!tail.isEmpty()) {
			return (Comparable) tail.firstKey();
		}
		return null;
	}

	/**
	 * {@inheritDoc}
	 */
	public Comparable firstAfter(Comparable c) {
		SortedMap tail = values.tailMap(c);
		if (!tail.isEmpty()) {
			Iterator iter = tail.keySet().iterator();
			Comparable a = (Comparable) iter.next();
			if (a.compareTo(c) == 0) {
				if (iter.hasNext()) {
					a = (Comparable) iter.next();
				} else {
					return null;
				}
			}
			return c;
		}
		return null;
	}

	/**
	 * {@inheritDoc}
	 */
	public Comparable endOf(Comparable c) {
		LelNode l = (LelNode) values.get(c);
		if (l == null) {
			Comparable start = firstBefore(c);
			l = (LelNode) values.get(start);
			if (l.getEnd().compareTo(c) <= 0) {
				return null;
			}
		}
		return l.getEnd();
	}

	/**
	 * {@inheritDoc}
	 */
	public boolean isEmpty() {
		return values.isEmpty();
	}

	/**
	 * {@inheritDoc}
	 */
	public void map(Transformer c) {
		Iterator entries = values.entrySet().iterator();
		values = new TreeMap();
		Object lastValue = null;
		Instant lastStart = null;
		Instant lastEnd = null;
		while (entries.hasNext()) {
			Map.Entry currEntry = (Map.Entry) entries.next();
			Instant start = (Instant) currEntry.getKey();
			LelNode node = (LelNode) currEntry.getValue();
			Object newValue = c.transform(node.getValue());
			if (newValue != null) {
				Instant newEnd = node.getEnd();
				if (start.equals(lastEnd) && newValue.equals(lastValue)) {
					start = lastStart;
				}
				node = new LelNode(newEnd, newValue);
				values.put(start, node);
				lastStart = start; lastEnd = newEnd; lastValue = newValue;
			}
		}
	}

	/**
	 * {@inheritDoc}
	 */
	public int getContiguousIntervalCount() {
		return values.size();
	}


	/** @inheritDoc */
	public void shift(Instant amount) {
		if ((this.isFrameBased() && !(amount instanceof Frame)) ||
				(this.isTimeBased() && !(amount instanceof Time))) {
			throw new IllegalArgumentException("Shifting by wrong unit type");
		}
		long shift = amount.longValue();
		if (shift < 0) {
			Instant curr = (Instant) getExtrema().getStart();
			while (curr!= null) {
				Instant end = (Instant) endOf(curr);
				Object o = get(curr);
				remove(curr, end);
				this.set(curr.go(shift), end.go(shift), o);
				curr = (Instant) firstAfterOrAt(end);
			}
		} else if (shift > 0) {
			Instant curr = (Instant) getExtrema().getEnd();
			Instant start = (Instant) firstBefore(curr);
			while (start != null) {
				curr = (Instant) endOf(start);
				Object o = get(start);
				remove(start, curr);
				set(start.go(shift), curr.go(shift), o);
				start = (Instant) firstBefore(start);
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
		Interval extrema = getExtrema();
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