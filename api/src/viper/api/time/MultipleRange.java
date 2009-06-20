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

import org.apache.commons.collections.*;

import viper.api.*;
import edu.umd.cfar.lamp.viper.util.*;

/**
 * Acts as an aggregate of several TemporalRange objects.
 * It acts as if it were a list of arrays, where each array at time <i>t</i>
 * contains the value of the respective TemporalRange at time <i>t</i>. 
 * 
 * @author davidm
 * @since May 17, 2003
 */
public class MultipleRange implements TemporalRange {
	private static Integer ONE = new Integer(1);
	/**
	 * The ranges that make up this multiplerange object.
	 */
	public TemporalRange[] subs;
	
	/**
	 * Creates a new MultipleRange using the given array as backing.
	 * Note that it takes the reference to the array; it does not copy it.
	 * 
	 * @param subs The TemporalRanges to act as children to this aggregator
	 */
	public MultipleRange(TemporalRange[] subs) {
		this.subs = subs;
	}

	/**
	 * {@inheritDoc}
	 */
	public void clear () {
		for (int i = 0; i < subs.length; i++) {
			subs[i].clear();
		}
	}

	/**
	 * {@inheritDoc}
	 */
	public boolean isFrameBased() {
		if (subs.length > 0) {
			return subs[0].isFrameBased();
		}
		return false;
	}

	/**
	 * {@inheritDoc}
	 */
	public boolean isTimeBased() {
		if (subs.length > 0) {
			return subs[0].isTimeBased();
		}
		return false;
	}

	/**
	 * {@inheritDoc}
	 */
	public Object clone() {
		TemporalRange[] c = new MultipleRange[subs.length];
		for (int i = 0; i < subs.length; i++) {
			c[i] = (TemporalRange) subs[i].clone();
		}
		return new MultipleRange(c);
	}

	/**
	 * {@inheritDoc}
	 */
	public boolean intersects(TemporalRange other) {
		for (int i = 0; i < subs.length; i++) {
			if (subs[i].intersects(other)) {
				return true;
			}
		}
		return false;
	}

	/**
	 * {@inheritDoc}
	 */
	public Interval getExtrema() {
		if (subs.length == 0) {
			return null;
		}
		Interval ex = null;
		for (int i = 0; i < subs.length; i++) {
			if (subs[i].isEmpty()) {
				continue;
			}
			Interval curr = subs[i].getExtrema();
			if (ex == null) {
				ex = curr;
			} else if (curr != null) {
				Comparable n_s = ex.getStart();
				Comparable n_e = ex.getEnd();
				boolean changed = false;
				if (ex.getStart().compareTo(curr.getStart()) > 0) {
					changed = true;
					n_s = curr.getStart();
				}
				if (ex.getEnd().compareTo(curr.getEnd()) < 0) {
					changed = true;
					n_e = curr.getEnd();
				}
				if (changed) {
					ex = ex.change(n_s, n_e);
				}
			}
		}
		return ex;
	}

	/**
	 * {@inheritDoc}
	 */
	public boolean contains(Object o) {
		for (int i = 0; i < subs.length; i++) {
			if (subs[i].contains(o)) {
				return true;
			}
		}
		return false;
	}

	/**
	 * {@inheritDoc}
	 */
	public Object get(Comparable index) {
		Object[] r = new Object[subs.length];
		for (int i = 0; i < subs.length; i++) {
			r[i] = subs[i].get(index);
		}
		return r;
	}

	/**
	 * {@inheritDoc}
	 */
	public void set(Comparable start, Comparable stop, Object value) {
		Object[] r = (Object[]) value;
		for (int i = 0; i < subs.length; i++) {
			subs[i].set(start, stop, r[i]);
		}
	}

	/**
	 * {@inheritDoc}
	 */
	public boolean remove(Comparable start, Comparable stop) {
		boolean r = false;
		for (int i = 0; i < subs.length; i++) {
			r = r || subs[i].remove(start,stop);
		}
		return r;
	}

	/** @inheritDoc */
	public Iterator iterator(Interval i) {
		return new CropIteratorUtility(i).getIterator(iterator());
	}
	private static class IntersectsThisFilter implements ExceptIterator.ExceptFunctor {
		private Interval i;
		IntersectsThisFilter(Interval i) {
			super();
			this.i = i;
		}
		public boolean check(Object o) {
			Interval toCheck = (Interval) o;
			return i.intersects(toCheck);
		}
		
	}

	/**
	 * {@inheritDoc}
	 */
	public Iterator iterator() {
		return new MyIterator();
	}
	private class MyIterator implements Iterator {
		private Iterator[] iters;
		private Interval[] nexts;
		private Comparable n_s = null;
		private Comparable n_e = null;
		private boolean hn = false;
		MyIterator() {
			iters = new Iterator[subs.length];
			nexts = new Interval[subs.length];
			for (int i = 0; i < subs.length; i++) {
				iters[i] = subs[i].iterator();
				if (iters[i].hasNext()) {
					nexts[i] = (Interval) iters[i].next();
					hn = true;
				}
			}
			Interval r = getExtrema();
			if (r != null) {
				n_s = r.getStart();
				n_e = r.getEnd();
			}
		}
		/**
		 * Gets the next interval in the range.
		 * @return an interval
		 */
		public Object next() {
			if (!hasNext()) {
				throw new NoSuchElementException();
			}
			Object[] c = new Object[nexts.length];
			Comparable e = n_e;
			Comparable s = n_s;
			boolean found = false;
			while (!found && hasNext()) {
				hn = false;
				for (int i = 0; i < nexts.length; i++) {
					// fill c with the values of each iter at frame s
					// while searching for the next end frame e.
					// if they are all null values, set s = e, e = n_e and continue
					if (nexts[i] == null) {
						// Iterator is empty
						continue;
					} else if (nexts[i].getEnd().compareTo(n_s) <= 0) {
						// out of date iterator element. Go to the next one.
						if (iters[i].hasNext()) {
							nexts[i] = (Interval) iters[i].next();
						} else {
							nexts[i] = null;
							continue;
						}
					}

					if (nexts[i].contains(s)) {
						if (nexts[i] instanceof DynamicValue) {
							c[i] = ((DynamicValue) nexts[i]).getValue();
						} else {
							c[i] = ONE;
						}
						found = true;
						if (nexts[i].getEnd().compareTo(e) < 0) {
							e = nexts[i].getEnd();
							hn = true;
						}
					} else {
						// nexts[i] starts after the current frame
						if (nexts[i].getStart().compareTo(e) < 0) {
							// nexts[i] starts during [s,e)
							e = nexts[i].getStart();
							hn = true;
						}
					}
				}
				if (!found) {
					s = e;
					e = n_e;
				}
			}
			DynamicAttributeValue v = new TemporalObject((Instant) s, (Instant) e, c);
			n_s = e;
			return v;
		}
		
		/**
		 * Tests to see if next will fail.
		 * @return if next will succeed
		 */
		public boolean hasNext() {
			return hn;
		}

		/**
		 * Not implemented.
		 */
		public void remove() {
			throw new UnsupportedOperationException();
		}
	}

	/**
	 * {@inheritDoc}
	 */
	public boolean addAll(IntervalIndexList l) {
		Iterator iter = l.iterator();
		while (iter.hasNext()) {
			DynamicAttributeValue curr = (DynamicAttributeValue) iter.next();
			this.set(curr.getStart(), curr.getEnd(), curr.getValue());
		}
		return false;
	}

	/**
	 * {@inheritDoc}
	 */
	public IntervalIndexList subList(Comparable start, Comparable stop) {
		throw new UnsupportedOperationException();
	}
	private static final Comparator NULL_OR_INT_SORT = new Comparator() {
		public int compare(Object o1, Object o2) {
			// null == +inf
			if (o1 == o2) {
				return 0;
			} else if (o1 == null) {
				return Integer.MAX_VALUE;
			} else if (o2 == null) {
				return Integer.MIN_VALUE;
			} else {
				return ((Comparable) o1).compareTo((Comparable) o2);
			}
		}};
	private void fourSort(Comparable[] four) {
		Arrays.sort(four, NULL_OR_INT_SORT);
		int alpha = 0;
		int beta = 1;
		while (alpha < four.length-1) {
			if (four[alpha] == null) {
				return;
			}
			while(four[alpha].equals(nullGet(four, beta))) {
				beta++;
			}
			four[++alpha] = nullGet(four, beta++);
		}
	}
	private Comparable nullGet(Comparable[] C, int index) {
		if (index < 0 || C.length <= index) {
			return null;
		} else {
			return C[index];
		}
	}
	private Comparable[] firstTwoEndsOrStartsBefore(Comparable c) {
		Comparable[] F = new Comparable[4];
		for (int i = 0; i < subs.length; i++) {
			F[2] = subs[i].firstBefore(c);
			if (F[2] == null) {
				continue;
			} else {
				F[3] = subs[i].endOf(F[2]);
				fourSort(F);
				if (F[3] != null && F[3].compareTo(c) < 0) {
					F[0] = F[2];
					F[1] = F[3];
				} else if (F[2] != null && F[2].compareTo(c) < 0) {
					F[0] = F[1];
					F[1] = F[2];
				}
			}
		}
		if (F[1] != null && F[1].compareTo(c) < 0)
			return new Comparable[] {F[0], F[1]};
		else if (F[0] != null && F[0].compareTo(c) < 0)
			return new Comparable[] {F[0]};
		else 
			return new Comparable[0];
	}
	private Comparable[] firstTwoEndsOrStartsAfter(Comparable c) {
		Comparable[] F = new Comparable[4];
		for (int i = 0; i < subs.length; i++) {
			F[2] = subs[i].firstBefore(c);
			if (F[2] != null) {
				F[2] = subs[i].endOf(F[2]);
				if (F[2] != null && F[2].compareTo(c) <= 0) {
					F[2] = null;
				}
			}
			F[3] = subs[i].firstAfter(c);
			fourSort(F);
		}
		if (F[1] != null && F[1].compareTo(c) >= 0)
			return new Comparable[] {F[0], F[1]};
		else if (F[0] != null && F[0].compareTo(c) >= 0)
			return new Comparable[] {F[0]};
		else 
			return new Comparable[0];
	}
	
	/**
	 * {@inheritDoc}
	 */
	public Comparable firstBefore(Comparable c) {
		Comparable[] F = firstTwoEndsOrStartsBefore(c);
		for (int i = 0; i < F.length; i++) {
			if (F[i] != null && this.contains(F[i])) {
				return F[i];
			}
		}
		return null;
	}
	public Comparable firstBeforeOrAt(Comparable c) {
		return firstBefore(((Incrementable) c).next());
	}
	/**
	 * {@inheritDoc}
	 */
	public Comparable firstAfterOrAt(Comparable c) {
		return firstAfter(((Incrementable) c).previous());
	}

	/**
	 * {@inheritDoc}
	 */
	public Comparable firstAfter(Comparable c) {
		Comparable[] F = firstTwoEndsOrStartsAfter(c);
		for (int i = 0; i < F.length; i++) {
			if (F[i] != null && this.contains(F[i])) {
				return F[i];
			}
		}
		return null;
	}

	/**
	 * {@inheritDoc}
	 */
	public Comparable endOf(Comparable c) {
		if (c == null) {
			return null;
		}
		if (subs.length == 0) {
			return null;
		}
		Comparable eo = subs[0].endOf(c);
		for (int i = 1; i < subs.length; i++) {
			Comparable temp = subs[i].endOf(c);
			if ((eo == null) || (temp != null && eo.compareTo(temp) < 0)) {
				eo = temp;
			}
		}
		return eo;
	}

	/**
	 * {@inheritDoc}
	 */
	public boolean isEmpty() {
		for (int i = 0; i < subs.length; i++) {
			if (!subs[i].isEmpty()) {
				return false;
			}
		}
		return true;
	}

	/**
	 * {@inheritDoc}
	 */
	public void map(Transformer c) {
		for (int i = 0; i < subs.length; i++) {
			subs[i].map(c);
		}
	}

	/**
	 * Counts the number of contiguous, homogenous intervals.
	 * @return the number of elements the iterator returns.
	 */
	public int getContiguousIntervalCount() {
		int i = 0;
		Iterator iter = this.iterator();
		while (iter.hasNext()) {
			i++;
			iter.next();
		}
		return i;
	}

	/** @inheritDoc */
	public void shift(Instant amount) {
		for (int i = 0; i < subs.length; i++) {
			subs[i].shift(amount);
		}
	}
}
