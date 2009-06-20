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
 * A list, with the elements run length encoded. It offers log n access and edit
 * time, on average. Note that it uses the <tt>equals</tt> method to check
 * equality, not <tt>==</tt>, so <code>null</code> elements will not be
 * accepted. Setting a range to <code>null</code> is the same as removing that
 * range. It does not implement java.util.List, as it takes java.util.Comparable
 * indexes instead of ints.
 */
public class LengthwiseEncodedList implements Cloneable, ArbitraryIndexList {
	/**
	 * This is stored as a java.util.TreeMap (red/black tree, I think), with the
	 * keys being the first frame, and the values containing the object value
	 * and the last frame, exclusive.
	 */
	private TreeMap values;

	/**
	 * Creates a new instance of LengthwiseEncodedList.
	 */
	public LengthwiseEncodedList() {
		values = new TreeMap();
	}

	/**
	 * Copies the list; does not copy the references.
	 * 
	 * @see java.lang.Object#clone()
	 */
	public Object clone() {
		LengthwiseEncodedList nl = new LengthwiseEncodedList();
		nl.values.putAll(values);
		return nl;
	}

	/**
	 * Get the value at the specified index in the list.
	 * 
	 * @param index
	 *            the index into the list
	 * @return the value at the specified index
	 */
	public Object get(Comparable index) {
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
	 * Sets the value at the given range. Note that, like <code>SortedMap</code>,
	 * this means that value is set in the range from start, inclusive, to stop,
	 * exclusive.
	 * 
	 * @param start
	 *            the first index to set
	 * @param stop
	 *            the first index that is not set
	 * @param value
	 *            all elements in the list in the range [start, stop) will take
	 *            this value
	 * @throws IllegalArgumentException
	 *             if start is not less than stop
	 */
	public void set(Comparable start, Comparable stop, Object value) {
		if (start.compareTo(stop) >= 0) {
			throw new IllegalArgumentException(
					"Start not strictly less than stop: " + start + " !< "
							+ stop);
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
					start = (Comparable) head.lastKey();
				}
			} else if (n.getEnd().compareTo(start) > 0) {
				head.put(head.lastKey(), new LelNode(start, n.getValue()));
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
		values.put(start, new LengthwiseEncodedList.LelNode(stop, value));
	}

	/**
	 * Removes all values at the given range. Note that, like
	 * <code>SortedMap</code>, this means that value is set in the range from
	 * start, inclusive, to stop, exclusive.
	 * 
	 * @param start
	 *            the first index to remove
	 * @param stop
	 *            the first index that is not removed
	 * @return <code>true</code> if any elements were removed
	 */
	public boolean remove(Comparable start, Comparable stop) {
		boolean someFound = false;
		SortedMap head = values.headMap(start);
		if (!head.isEmpty()) {
			LelNode n = (LelNode) head.get(head.lastKey());
			if (n.getEnd().compareTo(start) > 0) {
				someFound = true;
				head.put(head.lastKey(), new LelNode(start, n.getValue()));
				if (n.getEnd().compareTo(stop) > 0) {
					// this object spans the whole removed range
					values.put(stop, new LengthwiseEncodedList.LelNode(n
							.getEnd(), n.getValue()));
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
	 * Gets a list in the form <code>n*("item"), n*("item")... </code>
	 * 
	 * @return a String version of the list
	 */
	public String toString() {
		StringBuffer sb = new StringBuffer();
		for (Iterator iter = values.entrySet().iterator(); iter.hasNext();) {
			Map.Entry currEntry = (Map.Entry) iter.next();
			Comparable currStart = (Comparable) currEntry.getKey();
			Comparable currEnd = ((LelNode) currEntry.getValue()).getEnd();
			Object currValue = ((LelNode) currEntry.getValue()).getValue();
			sb.append(currStart).append(':').append(currEnd);
			sb.append("*(\"").append(
					StringHelp.backslashify(currValue.toString()))
					.append("\")");
			if (iter.hasNext()) {
				sb.append(", ");
			}
		}
		return sb.toString();
	}

	/**
	 * An iterator over all the {@link DynamicValue} elements of the list.
	 * 
	 * @return the elements of the list, in order
	 */
	public Iterator iterator() {
		return new LengthwiseEncodedList.DynamicValueIterator(this);
	}

	/**
	 * Represents an object over a span of time/frames.
	 */
	private class LelNode extends Pair {
		/**
		 * 1
		 */
		private static final long serialVersionUID = 1L;

		/**
		 * Constructs a new end node
		 * 
		 * @param e
		 *            the end of the interval
		 * @param v
		 *            the value over the interval
		 */
		public LelNode(Comparable e, Object v) {
			super(e, v);
		}

		/**
		 * Returns the end instant.
		 * 
		 * @return the end instant
		 */
		public Comparable getEnd() {
			return (Comparable) super.getFirst();
		}

		/**
		 * Gets the value at the node
		 * 
		 * @return the value
		 */
		public Object getValue() {
			return super.getSecond();
		}
	}

	private static final class DynamicValueIterator implements Iterator {
		private static final class DynamicValueImpl extends AbstractInterval
				implements DynamicValue {
			private Map.Entry v;

			/**
			 * Constructs a new dynamic value
			 * 
			 * @param o
			 *            the value.
			 */
			public DynamicValueImpl(Object o) {
				v = (Map.Entry) o;
				assert getEnd() != null;
				assert getStart() != null;
			}

			/**
			 * @see edu.umd.cfar.lamp.viper.util.DynamicValue#getValue()
			 */
			public Object getValue() {
				return ((LengthwiseEncodedList.LelNode) v.getValue())
						.getValue();
			}

			/**
			 * @see edu.umd.cfar.lamp.viper.util.Interval#getStart()
			 */
			public Comparable getStart() {
				return (Comparable) v.getKey();
			}

			/**
			 * @see edu.umd.cfar.lamp.viper.util.Interval#getEnd()
			 */
			public Comparable getEnd() {
				return ((LengthwiseEncodedList.LelNode) v.getValue()).getEnd();
			}
		}

		private Iterator t;

		private DynamicValue cachedNext;

		private DynamicValue cache() {
			DynamicValue old = cachedNext;
			if (t.hasNext()) {
				cachedNext = new DynamicValueImpl(t.next());
			} else {
				cachedNext = null;
			}
			return old;
		}

		/**
		 * Constructs a new iterator over all the values of the specified list.
		 * 
		 * @param lel
		 *            the list to iterate over
		 */
		public DynamicValueIterator(LengthwiseEncodedList lel) {
			t = lel.values.entrySet().iterator();
			cache();
		}

		/**
		 * @see java.util.Iterator#next()
		 */
		public Object next() {
			if (hasNext()) {
				return cache();
			}
			throw new NoSuchElementException();
		}

		/**
		 * @see java.util.Iterator#hasNext()
		 */
		public boolean hasNext() {
			return cachedNext != null;

		}

		/**
		 * Unsupported.
		 * 
		 * @see java.util.Iterator#remove()
		 */
		public void remove() {
			throw new UnsupportedOperationException();
		}
	}
}