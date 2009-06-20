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

import org.apache.commons.collections.*;


/**
 * This class represents a set of Intervals. Note that 
 * all indexes must all be Comparable.
 */
public class Range implements Cloneable, Set, IntervalIndexList {
	/**
	 * This is stored as a java.util.TreeMap (red/black tree, I think), 
	 * with the keys being the first frame, and the values containing
	 * the Span. The code is careful to make sure that no two Spans
	 * within the spans Map are contiguous.
	 */
	private SortedMap spans;
	
	private Object positiveValue = Boolean.TRUE;
	private Object negativeValue = null;

	/**
	 * Creates a new, empty Range.
	 */
	public Range() {
		spans = new TreeMap();
	}

	/**
	 * Creates a new, empty Range.
	 * @param c The comparison to use while adding items to the range.
	 */
	public Range(Comparator c) {
		spans = new TreeMap(c);
	}

	/**
	 * Creates a new Range from a Collection of Span objects.
	 * @param c the intervals to add
	 */
	public Range(Collection c) {
		spans = new TreeMap();
		for (Iterator iter = c.iterator(); iter.hasNext();) {
			this.add((Interval) iter.next());
		}
	}

	/**
	 * Gets the number of disconnected ranges of 
	 * Instants this Range contains. Note this is 
	 * not the number of total Instants included 
	 * in the Range.
	 * @return number of discrete sections of time
	 * 	covered by this Range.
	 */
	public int size() {
		return spans.size();
	}

	/**
	 * Checks that this is 
	 * 
	 * @param s
	 * @return
	 */
	public boolean withinRange(Interval s) {
		return withinRange(s.getStart(), s.getEnd());
	}

	/**
	 * Checks to see if the interval defined as [s,e)
	 * is entirely contained within this Range object.
	 * 
	 * @param s
	 * @param e
	 * @return boolean
	 */
	public boolean withinRange(Comparable s, Comparable e) {
		SortedMap m = spans.headMap(e);
		if (!m.isEmpty()) {
			// thankfully Range keeps contiguous spans merged,
			// so this is easy
			Comparable start = (Comparable) m.lastKey();
			Comparable end = (Comparable) m.get(start);
			if (end.compareTo(s) >= 0) {
				return start.compareTo(s) <= 0
					&& end.compareTo(e) >= 0;
			}
		}
		m = spans.tailMap(s);
		if (!m.isEmpty()) {
			Comparable sPrime = (Comparable) m.firstKey();
			if (sPrime.compareTo(s) == 0) {
				return e.compareTo((Comparable) m.get(sPrime)) <= 0;
			}
		}
		return false;
	}

	/**
	 * Returns null if the moment is in the span, otherwise returns 
	 * Boolean.TRUE. This is pretty hackish. 
	 * @param index the index to see if is in range
	 * @return <code>null</code> or <code>Boolean.TRUE</code>
	 */
	public Object get(Comparable index) {
		if (this.contains(index)) {
			return positiveValue;
		} else {
			return negativeValue;
		}
	}

	/**
	 * Since this is just a range, this ignores the value object except
	 * to check if it is "null". Setting to Null, or Boolean.FALSE, is the
	 * same as removing an Interval.
	 * @param start the start point, inclusive
	 * @param stop the end point, exclusive
	 * @param value the value to set over the given range
	 */
	public void set(Comparable start, Comparable stop, Object value) {
		if (value == null 
			|| ((value instanceof Boolean) && value.equals(Boolean.FALSE))) {
			this.remove(start, stop);
		} else {
			this.add(start, stop);
		}
	}

	/**
	 * Subsumes the Instants in the Span into this Range. 
	 * @param start the first instant to add
	 * @param stop the stop instant, exclusive
	 * @return <code>true</code> iff the operation modified this Range
	 */
	public boolean add(Comparable start, Comparable stop) {
		Comparable old = (Comparable) spans.get(start);
		if (old != null && old.compareTo(stop) >= 0) {
			return false;
		}
		SortedMap head = spans.headMap(start);
		if (!head.isEmpty()) {
			Comparable oldStart = (Comparable) head.lastKey();
			Comparable oldEnd = (Comparable) head.get(oldStart);
			if (oldEnd.compareTo(stop) >= 0) {
				return false;
			} else {
				if (oldEnd.compareTo(start) >= 0) {
					start = oldStart;
					spans.remove(oldStart);
				}
			}
		}
		SortedMap sub = spans.subMap(start, stop);
		if (!sub.isEmpty()) {
			Comparable oldStart = (Comparable) sub.lastKey();
			Comparable oldEnd = (Comparable) sub.get(oldStart);
			if (oldStart.compareTo(start) == 0
				&& oldEnd.compareTo(stop) >= 0) {
				return false;
			} else if (oldEnd.compareTo(stop) > 0) {
				stop = oldEnd;
			}
			sub.clear();
		}
		if (spans.containsKey(stop)){
			stop = (Comparable) spans.remove(stop);
		}
		spans.put(start, stop);
		return true;
	}

	/**
	 * Gets the Span that starts at the beginning
	 * of the first Span and ends at the end of the 
	 * last Span.
	 * @return Span a Span that has all the others
	 *    as a subset. Null if empty.
	 */
	public Interval getExtrema() {
		if (spans.isEmpty()) {
			return null;
		} else if (spans.size() == 1) {
			Comparable start = (Comparable) spans.firstKey();
			Comparable end = (Comparable) spans.get(start);
			return createInterval(start, end);
		} else {
			Comparable start = (Comparable) spans.firstKey();
			Comparable end = (Comparable) spans.lastKey();
			end = (Comparable) spans.get(end);
			return createInterval (start, end);
		}
	}

	/**
	 * @see edu.umd.cfar.lamp.viper.util.IntervalIndexList#remove(java.lang.Comparable, java.lang.Comparable)
	 */
	public boolean remove(Comparable start, Comparable end) {
		boolean someFound = false;
		SortedMap head = spans.headMap(start);
		if (!head.isEmpty()) {
			Comparable oldStart = (Comparable) head.lastKey(); 
			Comparable oldEnd = (Comparable) head.get(oldStart);
			if (oldEnd.compareTo(start) > 0) {
				// if there is a span that goes into the span to
				// be removed, replace it.
				head.put(oldStart, start);
				someFound = true;
				double toCheck = oldEnd.compareTo(end);
				if (toCheck > 0) {
					// if the span to be removed is a strict subset 
					// of some existing span, you also have
					// to add back the end.
					spans.put(end, oldEnd);
					return true;
				} else if (toCheck == 0) {
					return true;
				}
			}
		}
		SortedMap sub = spans.subMap(start, end);
		if (!sub.isEmpty()) {
			someFound = true;
			Comparable oldStart = (Comparable) sub.lastKey(); 
			Comparable oldEnd = (Comparable) sub.get(oldStart);
			if (oldEnd.compareTo(end) > 0) {
				// if there is a span that starts during the
				// span to removed that goes past the end,
				// have to add back the difference.
				spans.put(end, oldEnd);
			}
			sub.clear();
		}
		return someFound;
	}

	/**
	 * Adds all of the Span objects in the Collection s 
	 * to this one. It won't remove any of the current
	 * Instants.
	 * @param s a set of homogenous type Span objects
	 * @return <code>true</code> if any new Instants are now
	 * 	part of this set.
	 */
	public boolean setSpans(Collection s) {
		boolean changed = false;
		for (Iterator iter = s.iterator(); iter.hasNext();) {
			changed = changed || add(iter.next());
		}
		return changed;
	}

	protected void cloneInto(Range r) {
		r.clear();
		r.spans.putAll(spans);
	}
	/**
	 * @see java.lang.Object#clone()
	 */
	public Object clone() {
		Range r = new Range();
		cloneInto(r);
		return r;
	}

	/**
	 * @see java.util.Set#add(java.lang.Object)
	 */
	public boolean add(Object o) {
		if (o instanceof Interval) {
			Interval t = (Interval) o;
			return add(t.getStart(), t.getEnd());
		} else if (o instanceof Incrementable) {
			Incrementable i = (Incrementable) o;
			return add(i, i.next());
		} else {
			throw new IllegalArgumentException("Cannot add " + o);
		}
	}

	/**
	 * Adds all the elements described by the iterator.
	 * @param iter the elements to add
	 * @return  if <code>this</code> changed
	 */
	public boolean addAll(Iterator iter) {
		boolean changed = false;
		while (iter.hasNext()) {
			changed = add(iter.next()) || changed;
		}
		return changed;
	}


	/**
	 * @see java.util.Set#addAll(java.util.Collection)
	 */
	public boolean addAll(Collection c) {
		if (this.isEmpty() && (c instanceof Range)) {
			if (!c.isEmpty()) {
				spans.putAll(((Range) c).spans);
				return true;
			} else {
				return false;
			}
		} else if (c instanceof Interval) {
			return add(c);
		} else {
			return addAll(c.iterator());
		}
	}

	/**
	 * @see java.util.Set#clear()
	 */
	public void clear() {
		spans.clear();
	}

	/**
	 * @see java.util.Set#contains(java.lang.Object)
	 */
	public boolean contains(Object o) {
		if (spans.size() == 0) {
			return false;
		} else if (o instanceof Comparable) {
			SortedMap m = spans.headMap(o);
			if (m.size() > 0) {
				Comparable e = (Comparable) m.get(m.lastKey());
				if (e.compareTo(o) > 0) {
					return true;
				} 
			}
			m = spans.tailMap(o);
			if (m.size() > 0) {
				Comparable s = (Comparable) m.firstKey();
				return s.compareTo(o) == 0;
			}
			return false;
		} else {
			return withinRange((Interval) o);
		}
	}

	/**
	 * @see java.util.Set#containsAll(java.util.Collection)
	 */
	public boolean containsAll(Collection c) {
		boolean foundAll = true;
		for (Iterator iter = c.iterator(); iter.hasNext();) {
			foundAll = contains(iter.next()) && foundAll;
		}
		return foundAll;
	}

	/**
	 * @see java.util.Set#isEmpty()
	 */
	public boolean isEmpty() {
		return spans.isEmpty();
	}

	class IntervalIterator implements Iterator {
		private Iterator internal;
		IntervalIterator () {
			internal = spans.entrySet().iterator();
		}
		/**
		 * @see java.util.Iterator#hasNext()
		 */
		public boolean hasNext() {
			return internal.hasNext();
		}
		/**
		 * @see java.util.Iterator#next()
		 */
		public Object next() {
			Map.Entry c = (Map.Entry) internal.next();
			Comparable start = (Comparable) c.getKey();
			Comparable end = (Comparable) c.getValue();
			return createInterval(start, end);
		}
		/**
		 * @see java.util.Iterator#remove()
		 */
		public void remove() {
			throw new UnsupportedOperationException();
		}
	}

	/** @inheritDoc */
	public Iterator iterator() {
		return new IntervalIterator();
	}
	/** @inheritDoc */
	public Iterator iterator(Interval i) {
		return new CropIteratorUtility(i).getIterator(iterator());
	}
	/**
	 * @see java.util.Set#remove(java.lang.Object)
	 */
	public boolean remove(Object o) {
		if (o instanceof Interval) {
			Interval toRemove = (Interval) o;
			return remove(toRemove.getStart(), toRemove.getEnd());
		} else if (o instanceof Incrementable) {
			Incrementable toRemove = (Incrementable) o;
			return remove(toRemove, toRemove.next());
		} else {
			throw new IllegalArgumentException("Cannot remove: " + o);
		}
	}
	/**
	 * @see java.util.Set#removeAll(java.util.Collection)
	 */
	public boolean removeAll(Collection c) {
		boolean changed = false;
		for (Iterator iter = c.iterator(); iter.hasNext();) {
			changed = changed || remove(iter.next());
		}
		return changed;
	}
	/**
	 * TODO: currently unsupported
	 * @see java.util.Set#retainAll(java.util.Collection)
	 */
	public boolean retainAll(Collection c) {
		// This is tough. Need a state machine of some sort.
		throw new UnsupportedOperationException();
	}
	/**
	 * @see java.util.Set#toArray()
	 */
	public Object[] toArray() {
		return toArray(new Interval[spans.size()]);
	}
	/**
	 * @see java.util.Set#toArray(java.lang.Object[])
	 */
	public Object[] toArray(Object[] A) {
		if (A.length < spans.size()) {
			A = new Interval[spans.size()];
		}
		int i = 0;
		for (Iterator iter = iterator(); iter.hasNext(); ) {
			A[i++] = iter.next();
		}
		return A;
	}

	/**
	 * Returns the list as a space delimited of [s e) pairs.
	 * @see java.lang.Object#toString()
	 */
	public String toString() {
		StringBuffer sb = new StringBuffer();
		for (Iterator iter = spans.entrySet().iterator(); iter.hasNext();) {
			Map.Entry curr = (Map.Entry) iter.next();
			sb.append('[').append((curr.getKey())).append(',').append(curr.getValue()).append(')');
			if (iter.hasNext()) {
				sb.append(" ");
			}
		}
		return sb.toString();
	}
	/**
	 * @see java.util.Set#equals(java.lang.Object)
	 */
	public boolean equals(Object o) {
		if (this == o) {
			return true;
		} else if (o instanceof Range) {
			SortedMap oSpans = ((Range) o).spans;
			if (oSpans.size() != spans.size()) {
				return false;
			} else {
				Iterator myIter = spans.entrySet().iterator();
				Iterator oIter = oSpans.entrySet().iterator();
				while (myIter.hasNext()) {
					if (!myIter.next().equals(oIter.next())) {
						return false;
					}
				}
				return true;
			}
		} else {
			return false;
		}
	}
	/**
	 * @see java.util.Set#hashCode()
	 */
	public int hashCode() {
		return spans.hashCode();
	}

	/**
	 * Tests to see if this range contains any element
	 * or intersects any element of the specified set.
	 * @param s the set to test against
	 * @return if there is an intersection/shared item
	 */
	public boolean intersects(Set s) {
		if (s instanceof Interval) {
			return intersects((Interval) s);
		} else if (s instanceof Range) {
			return intersects((Range) s);
		} else {
			for (Iterator iter = s.iterator(); iter.hasNext();) {
				Object curr = iter.next();
				if (curr instanceof Comparable) {
					Comparable i = (Comparable) curr;
					if (this.withinRange(i, i)) {
						return true;
					}
				} else if (this.intersects((Interval) curr)) {
					return true;
				}
			}
			return false;
		}
	}
	/**
	 * Tests to see if the this intersects the other range.
	 * @param r the range to test against
	 * @return <code>true</code> if there is some shared interval
	 */
	public boolean intersects(Range r) {
		if (!this.getExtrema().intersects(r.getExtrema())) {
			return false;
		} else {
			return this.intersect(r).size() > 0;
		}
	}
	/**
	 * Checks to see if some interval of this
	 * intersects the specified interval.
	 * @param s the interval to test against
	 * @return <code>true</code> if there is some overlap
	 */
	public boolean intersects(Interval s) {
		if (this.isEmpty() || s.isEmpty()) {
			return false;
		}
		SortedMap m = spans.subMap(s.getStart(), s.getEnd());
		if (!m.isEmpty()) {
			return true;
		} else {
			m = spans.headMap(s.getStart());
			Interval last = (Interval) m.get(m.lastKey());
			return last.intersects(s);
		}
	}


	/**
	 * Gets the range shared by this and the specified 
	 * range.
	 * @param list the list to intersect with
	 * @return the shared range
	 */
	public Range intersect(IntervalIndexList list) {
		// Note that each span in a range must
		// have a gap before it. this means it is 
		// ease enough to just follow the heuristic:
		// get span to add, advance to latest pointer,
		// get span to add.
		Range newRange = new Range();
		if (this.isEmpty() || list.isEmpty()) {
			return newRange;
		}
		SortedMap A = this.spans;
		Comparable aNextStart = (Comparable) A.firstKey();
		Comparable aNextEnd = (Comparable) A.get(aNextStart);
		Iterator B = list.iterator();
		if (!B.hasNext()) {
			return newRange;
		}
		Interval b = (Interval) B.next();
		if (b.isEmpty()) {
			throw new AssertionError();
		}
		Comparable bNextStart = b.getStart();
		Comparable bNextEnd = b.getEnd();

		while (aNextStart != null && bNextStart != null) {
			double diffStartStart = aNextStart.compareTo(bNextStart);
			double diffEndEnd = aNextEnd.compareTo(bNextEnd);
			if (diffStartStart == 0) {
				// both start at the same time
				if (diffEndEnd == 0) {
					newRange.add(aNextStart, aNextEnd);
					aNextStart = this.firstAfterOrAt(aNextEnd);
					aNextEnd = this.endOf(aNextStart);
					bNextStart = list.firstAfterOrAt(bNextEnd);
					bNextEnd = list.endOf(bNextStart);
				} else if (diffEndEnd < 0) {
					// a stops before b
					newRange.add(aNextStart, aNextEnd);
					aNextStart = this.firstAfterOrAt(aNextEnd);
					aNextEnd = this.endOf(aNextStart);
				} else {
					// b stops before a
					newRange.add(bNextStart, bNextEnd);
					bNextStart = list.firstAfterOrAt(bNextEnd);
					bNextEnd = list.endOf(bNextStart);
				}
			} else if (diffStartStart < 0) {
				// a starts before b
				double diffEndStart = aNextEnd.compareTo(bNextStart);
				if (diffEndStart <= 0) {
					// skip ahead, since there is no
					// chance of overlap here
					aNextStart = this.firstAfterOrAt(aNextEnd);
					aNextEnd = this.endOf(aNextStart);
				} else if (diffEndEnd == 0) {
					newRange.add(bNextStart, bNextEnd);
					aNextStart = this.firstAfterOrAt(aNextEnd);
					aNextEnd = this.endOf(aNextStart);
					bNextStart = list.firstAfterOrAt(bNextEnd);
					bNextEnd = list.endOf(bNextStart);
				} else if (diffEndEnd < 0) {
					// a ends before b does, but after b starts
					newRange.add(bNextStart, aNextEnd);
					aNextStart = this.firstAfterOrAt(aNextEnd);
					aNextEnd = this.endOf(aNextStart);
				} else {
					// a ends after b does
					newRange.add(bNextStart, bNextEnd);
					bNextStart = list.firstAfterOrAt(bNextEnd);
					bNextEnd = list.endOf(bNextStart);
				}
			} else {
				// a starts after b does
				double diffStartEnd = aNextStart.compareTo(bNextEnd);
				if (diffStartEnd >= 0) {
					// skip ahead, since there is no
					// chance of overlap here
					bNextStart = list.firstAfterOrAt(bNextEnd);
					bNextEnd = list.endOf(bNextStart);
				} else if (diffEndEnd == 0) {
					// both end at the same moment
					newRange.add(aNextStart, aNextEnd);
					aNextStart = this.firstAfterOrAt(aNextEnd);
					aNextEnd = this.endOf(aNextStart);
					bNextStart = list.firstAfterOrAt(bNextEnd);
					bNextEnd = list.endOf(bNextStart);
				} else if (diffEndEnd < 0) {
					// a is a subset of b
					newRange.add(aNextStart, aNextEnd);
					aNextStart = this.firstAfterOrAt(aNextEnd);
					aNextEnd = this.endOf(aNextStart);
				} else {
					// a ends after b does
					newRange.add(aNextStart, bNextEnd);
					bNextStart = list.firstAfterOrAt(bNextEnd);
					bNextEnd = list.endOf(bNextStart);
				}
			}
			if ((aNextStart != null && bNextStart != null) && (aNextEnd == null || bNextEnd == null)) {
				throw new AssertionError();
			}
		}
		return newRange;
	}

	/**
	 * @see edu.umd.cfar.lamp.viper.util.IntervalIndexList#firstBefore(java.lang.Comparable)
	 */
	public Comparable firstBefore(Comparable oldStart) {
		SortedMap head = spans.headMap(oldStart);
		if (head.size() > 0) {
			return (Comparable) head.lastKey();
		}
		return null;
	}
	public Comparable firstBeforeOrAt(Comparable oldStart) {
		if (spans.containsKey(oldStart)) {
			return oldStart;
		}
		SortedMap head = spans.headMap(oldStart);
		if (head.size() > 0) {
			return (Comparable) head.lastKey();
		}
		return null;
	}
	/**
	 * @see edu.umd.cfar.lamp.viper.util.IntervalIndexList#firstAfter(java.lang.Comparable)
	 */
	public Comparable firstAfter(Comparable oldStart) {
		Collection tail = spans.tailMap(oldStart).keySet();
		if (!tail.isEmpty()) {
			Iterator iter = tail.iterator();
			if (spans.containsKey(oldStart)) {
				iter.next();
				if (!iter.hasNext()) {
					return null;
				}
			}
			return (Comparable) iter.next();
		}
		return null;
	}
	/**
	 * @see edu.umd.cfar.lamp.viper.util.IntervalIndexList#firstAfterOrAt(java.lang.Comparable)
	 */
	public Comparable firstAfterOrAt(Comparable oldStart) {
		if (spans.containsKey(oldStart)) {
			return oldStart;
		} else {
			return firstAfter(oldStart);
		}
	}
	/**
	 * @see edu.umd.cfar.lamp.viper.util.IntervalIndexList#endOf(java.lang.Comparable)
	 */
	public Comparable endOf(Comparable c) {
		if (c == null) {
			return null;
		}
		Object o = spans.get(c);
		if (o == null) {
			SortedMap head = spans.headMap(c);
			if (head != null && !head.isEmpty()) {
				Comparable last = (Comparable) spans.get(head.lastKey());
				if (last.compareTo(c) >= 0) {
					return last;
				}
			}
			return null;
		} else {
			return (Comparable) o;
		}
	}

	/**
	 * Gets the union of this and the specified range.
	 * @param o the range to union with 
	 * @return a new range containing all items in either this
	 * or the specified range
	 */
	public Range union(Range o) {
		Range u = new Range();
		u.addAll(this.iterator());
		u.addAll(o.iterator());
		return u;
	}
	/**
	 * Gets all elements from this that are not elements
	 * of the specified range.
	 * @param o the range to complement
	 * @return this - that
	 */
	public Range intersectComplement(Range o) {
		Range r = new Range();
		r.addAll(this.iterator());
		r.removeAll(o);
		return r;
	}

	/**
	 * @see edu.umd.cfar.lamp.viper.util.IntervalIndexList#addAll(edu.umd.cfar.lamp.viper.util.IntervalIndexList)
	 */
	public boolean addAll(IntervalIndexList l) {
		return addAll(l.iterator());
	}

	/**
	 * @see edu.umd.cfar.lamp.viper.util.IntervalIndexList#subList(java.lang.Comparable, java.lang.Comparable)
	 */
	public IntervalIndexList subList(Comparable start, Comparable stop) {
		return intersect(Intervals.singleton(new SimpleInterval(start, stop)));
	}

	/**
	 * @see edu.umd.cfar.lamp.viper.util.IntervalIndexList#map(edu.umd.cfar.lamp.viper.util.IntervalIndexList.Change)
	 */
	public void map(Transformer c) {
		throw new UnsupportedOperationException();
	}
	
	/**
	 * Creates a new interval. This is used instead of a constructor, so
	 * subclasses of this range which wish to return subclasses of interval 
	 * should override this method.
	 * @param s
	 * @param e
	 * @return
	 */
	public Interval createInterval(Comparable s, Comparable e) {
		return new SimpleInterval(s, e);
	}

	/**
	 * @see edu.umd.cfar.lamp.viper.util.IntervalIndexList#getContiguousIntervalCount()
	 */
	public int getContiguousIntervalCount() {
		return spans.size();
	}

	public Object getPositiveValue() {
		return positiveValue;
	}

	public void setPositiveValue(Object positiveValue) {
		this.positiveValue = positiveValue;
	}
}
