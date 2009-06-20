package edu.umd.cfar.lamp.viper.util;

import java.util.*;

import org.apache.commons.collections.*;


import viper.api.time.*;

/**
 * Utility methods for dealing with intervals, ranges
 * and the like.
 */
public class Intervals {
	private static class IntervalSingletonList implements IntervalIndexList {
		protected Interval i;

		IntervalSingletonList(Interval i) {
			if (i == null) {
				throw new NullPointerException();
			}
			this.i = i;
		}
		/** @inheritDoc */
		public Object get(Comparable index) {
			if (i.contains(index)) {
				return Boolean.TRUE;
			} else {
				return Boolean.FALSE;
			}
		}
		/** @inheritDoc */
		public void set(Comparable start, Comparable stop, Object value) {
			throw new UnsupportedOperationException();
		}
		/** @inheritDoc */
		public boolean remove(Comparable start, Comparable stop) {
			throw new UnsupportedOperationException();
		}
		/** @inheritDoc */
		public Iterator iterator() {
			return Collections.singleton(i).iterator();
		}
		/** @inheritDoc */
		public Iterator iterator(Interval i) {
			if (this.i.intersects(i)) {
				i = Intervals.intersection(this.i,i);
				return Collections.singleton(i).iterator();
			}
			return Collections.EMPTY_SET.iterator();
		}
		/** @inheritDoc */
		public boolean addAll(IntervalIndexList l) {
			throw new UnsupportedOperationException();
		}
		/** @inheritDoc */
		public IntervalIndexList subList(Comparable start, Comparable stop) {
			return null;
		}
		/** @inheritDoc */
		public Comparable firstBefore(Comparable c) {
			if (i.getStart().compareTo(c) < 0) {
				return i.getStart();
			} else {
				return null;
			}
		}
		/** @inheritDoc */
		public Comparable firstBeforeOrAt(Comparable c) {
			if (i.getStart().compareTo(c) <= 0) {
				return i.getStart();
			} else {
				return null;
			}
		}
		/** @inheritDoc */
		public Comparable firstAfterOrAt(Comparable c) {
			if (i.getStart().compareTo(c) < 0) {
				return null;
			} else {
				return i.getStart();
			}
		}
		/** @inheritDoc */
		public Comparable firstAfter(Comparable c) {
			if (i.getStart().compareTo(c) <= 0) {
				return null;
			} else {
				return i.getStart();
			}
		}
		/** @inheritDoc */
		public Comparable endOf(Comparable c) {
			if (i.contains(c)) {
				return i.getEnd();
			}
			return null;
		}
		/** @inheritDoc */
		public boolean isEmpty() {
			return i.isEmpty();
		}
		/** @inheritDoc */
		public void clear() {
			throw new UnsupportedOperationException();
		}
		/** @inheritDoc */
		public void map(Transformer c) {
			throw new UnsupportedOperationException();
		}
		/** @inheritDoc */
		public int getContiguousIntervalCount() {
			return 1;
		}
	}

	
	private static final class WrapList implements IntervalIndexList {
		private List toWrap;
		private int offset = 0;

		/**
		 * @param toWrap
		 * @param offset
		 */
		public WrapList(List toWrap, int offset) {
			super();
			this.toWrap = toWrap;
			this.offset = offset;
		}
		public Object get(Comparable index) {
			int i = ((Integer) index).intValue();
			return toWrap.get(i - offset);
		}

		public void set(Comparable start, Comparable stop, Object value) {
			int s = ((Integer) start).intValue() - offset;
			int e = ((Integer) stop).intValue() - offset;
			for (int i = s; i < e; i++) {
				toWrap.set(i, value);
			}
		}

		public boolean remove(Comparable start, Comparable stop) {
			throw new UnsupportedOperationException();
		}
		
		private static class IntDynamicValue extends SimpleInterval implements DynamicValue{
			private Object value;
			public IntDynamicValue(int start, int end, Object value) {
				super(new Integer(start), new Integer(end));
				this.value = value;
			}
			public Object getValue() {
				return this.value;
			}
		}
		private class IIterator implements Iterator {
			private Iterator outer;
			private int i = 0;
			public IIterator(Iterator outer) {
				this.outer = outer;
			}
			public boolean hasNext() {
				return outer.hasNext();
			}
			public Object next() {
				if (!hasNext()) {
					throw new NoSuchElementException();
				}
				int s = i++;
				return new IntDynamicValue(s-offset, i-offset, outer.next());
			}

			public void remove() {
				throw new IndexOutOfBoundsException();
			}
		}
		public Iterator iterator() {
			return new IIterator(toWrap.iterator());
		}
		public Iterator iterator(Interval i) {
			return subList(i.getStart(), i.getEnd()).iterator();
		}

		public boolean addAll(IntervalIndexList l) {
			throw new UnsupportedOperationException();
		}

		public IntervalIndexList subList(Comparable start, Comparable stop) {
			int s = ((Integer) start).intValue() - offset;
			int e = ((Integer) stop).intValue() - offset;
			return new WrapList(toWrap.subList(s,e), s + offset);
		}

		public Comparable firstBefore(Comparable c) {
			int r = firstBeforeOrAt(((Integer) c).intValue() - 1);
			if (r < offset) {
				return null;
			}
			return new Integer(r);
		}

		public Comparable firstBeforeOrAt(Comparable c) {
			int r = firstBeforeOrAt(((Integer) c).intValue());
			if (r < offset) {
				return null;
			}
			return new Integer(r);
		}

		private int firstBeforeOrAt(int s) {
			s -= offset;
			if (isEmpty() || s < 0) {
				return offset-1;
			}
			if (s >= toWrap.size()) {
				return (toWrap.size()-1) + offset;
			}
			return s + offset;
		}
		private int firstAfterOrAt(int s) {
			s -= offset;
			if (isEmpty()) {
				return offset-1;
			}
			if (s < 0) {
				return offset;
			}
			if (s < toWrap.size()) {
				return s + offset;
			}
			return offset-1;
		}

		public Comparable firstAfterOrAt(Comparable c) {
			int r = firstAfterOrAt(((Integer) c).intValue());
			if (r < offset) {
				return null;
			}
			return new Integer(r);
		}

		public Comparable firstAfter(Comparable c) {
			int r = firstAfterOrAt(((Integer) c).intValue() + 1);
			if (r < offset) {
				return null;
			}
			return new Integer(r);
		}
		public Comparable endOf(Comparable c) {
			return new Integer(toWrap.size());
		}
		public void clear() {
			toWrap.clear();
		}
		public boolean isEmpty() {
			return toWrap.isEmpty();
		}
		public void map(Transformer c) {
			toWrap = ListUtils.transformedList(toWrap, c);
		}
		public int getContiguousIntervalCount() {
			return toWrap.size();
		}
	}
	
	/**
	 * Wraps an ordinary list with an IntervalIndexList with Integer
	 * indexes
	 * @param toWrap
	 */
	public static IntervalIndexList toIntervalIndexList(final List toWrap) {
		return new WrapList(toWrap, 0);
	}
	
	/**
	 * Immutable range object that refers
	 * to a single continguous interval.
	 */
	private static class TemporalSingleton
		extends IntervalSingletonList
		implements TemporalRange {
		/**
		 * Constructs the range containing just the given span.
		 * @param i the span
		 */
		public TemporalSingleton(InstantInterval i) {
			super(i);
		}

		/**
		 * @see viper.api.time.TemporalRange#isFrameBased()
		 */
		public boolean isFrameBased() {
			return ((InstantInterval) i).isFrameBased();
		}
		/**
		 * @see viper.api.time.TemporalRange#isTimeBased()
		 */
		public boolean isTimeBased() {
			return ((InstantInterval) i).isTimeBased();
		}
		/**
		 * @see viper.api.time.TemporalRange#clone()
		 */
		public Object clone() {
			return new TemporalSingleton((InstantInterval) i);
		}
		/**
		 * @see viper.api.time.TemporalRange#intersects(viper.api.time.TemporalRange)
		 */
		public boolean intersects(TemporalRange other) {
			return other.firstAfterOrAt(i.getStart()) == null
				&& other.endOf(i.getStart()) == null;
		}
		/**
		 * @see viper.api.time.TemporalRange#getExtrema()
		 */
		public Interval getExtrema() {
			return i;
		}
		/**
		 * @see viper.api.time.TemporalRange#contains(java.lang.Object)
		 */
		public boolean contains(Object o) {
			return i.contains((Comparable) o);
		}

		/** @inheritDoc */
		public void shift(Instant amount) {
			throw new UnsupportedOperationException();
		}
	}

	/**
	 * Gets an IntervalIndexList that refers
	 * to a single interval.
	 * @param i the interval to contain
	 * @return an immutable list of intervals containing
	 * the specified interval
	 */
	public static IntervalIndexList singleton(Interval i) {
		return new IntervalSingletonList(i);
	}

	/**
	 * Gets an Temporal that refers
	 * to a single interval.
	 * @param i the interval to contain
	 * @return an immutable range containing
	 * the specified interval
	 */
	public static TemporalRange singletonRange(InstantInterval i) {
		return new TemporalSingleton(i);
	}
	
	private static class ComplementIterator implements Iterator {
		private Iterator i;
		private Instant alpha;
		private Instant omega;
		
		private Interval prev;
		private Interval next;
		/**
		 * Creates a new iterator that returns all the intervals not
		 * included in the interval iterator.
		 * @param i the iterator to invert
		 * @param alpha the start time
		 * @param omega the end time
		 */
		public ComplementIterator(Iterator i, Instant alpha, Instant omega) {
			this.i = i;
			this.alpha = alpha;
			this.omega = omega;
			this.prev = null;
			if (this.i.hasNext()) {
				this.next = (Interval) this.i.next();
			}
		}
		/**
		 * @see java.util.Iterator#hasNext()
		 */
		public boolean hasNext() {
			return i != null;
		}
		/**
		 * Gets the next Instant in the interval.
		 * @see java.util.Iterator#next()
		 */
		public Object next() {
			if (!hasNext()) {
				throw new NoSuchElementException();
			}
			Instant s, e;
			if (prev == null) {
				s = alpha;
			} else {
				s = (Instant) prev.getEnd();
			}
			if (next == null) {
				e = omega;
			} else {
				e = (Instant) next.getStart();
			}
			prev = next;
			if (i.hasNext()) {
				next = (Interval) i.next();
			} else {
				next = null;
			}
			
			if (prev == null) {
				i = null;
			}
			
			return new Span(s, e);
		}
		/**
		 * Unsupported.
		 * @see java.util.Iterator#remove()
		 */
		public void remove() {
			throw new UnsupportedOperationException();
		}
	}
	
	/**
	 * Iterates through the complement intervals,
	 * that is, the intervals that are not in the range.
	 * @param tr
	 * @return
	 */
	public static Iterator complement(TemporalRange tr) {
		Instant alpha;
		Instant omega;
		Iterator i;
		i = tr.iterator();
		if (tr.isFrameBased()) {
			alpha = Frame.ALPHA;
			omega = Frame.OMEGA;
		} else {
			alpha = Time.ALPHA;
			omega = Time.OMEGA;
		}
		return new ComplementIterator(i, alpha, omega);
	}
	
	public static Interval intersection(Interval a, Interval b) {
		Comparable s = a.getStart().compareTo(b.getStart()) > 0 ? a.getStart() : b.getStart();
		Comparable e = a.getEnd().compareTo(b.getEnd()) < 0 ? a.getEnd() : b.getEnd();
		return a.change(s,e);
	}
}
