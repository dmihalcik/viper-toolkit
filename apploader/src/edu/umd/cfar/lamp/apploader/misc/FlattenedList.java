/***************************************
 *:// L A M P . cfar . umd . edu       *
 *      AppLoader                      *
 *                                     *
 *      A tool for loading java apps   *
 *             from RDF descriptions.  *
 *                                     *
 * Distributed under the GPL license   *
 *        Terms available at gnu.org.  *
 *                                     *
 *  Copyright University of Maryland,  *
 *                      College Park.  *
 ***************************************/

package edu.umd.cfar.lamp.apploader.misc;

import java.util.*;

/**
 * A list of lists that acts as a single list. Note 
 * that this only flattens one layer, not completely.
 * For example, if I pass this a list containing two lists,
 * and one list has a list as an element, that list will
 * be returned as a list. Also, it does expect a list of lists.
 */
public class FlattenedList extends AbstractList {
	private List L;
	/**
	 * Create a new list from the given list of lists.
	 * Note that this is live, so changes to the member lists
	 * will have effects.
	 * @param inside The list of lists.
	 */
	public FlattenedList(List inside) {
		L = inside;
	}
	
	/**
	 * Iterate through all elements of the lists
	 * in order.
	 * @return An iterator for all elements
	 */
	public Iterator iterator() {
		List iterList = new LinkedList();
		Iterator[] iterArray;

		for (Iterator cols = L.iterator(); cols.hasNext();) {
			Collection c = (Collection) cols.next();
			iterList.add(c.iterator());
		}

		iterArray = new Iterator[iterList.size()];
		iterArray = (Iterator[]) iterList.toArray(iterArray);
		return new MultiIterator(iterArray);
	}

	/**
	 * Finds the <i>i</i>th element in the flattened list.
	 * @see java.util.AbstractList#get(int)
	 */
	public Object get(int i) {
		int count = 0;
		Iterator cols = L.iterator();
		while (count <= i && cols.hasNext()) {
			Collection curr = (Collection) cols.next();
			if (i < count + curr.size()) {
				int localIndex = i - count;
				if (curr instanceof List) {
					return ((List) curr).get(localIndex);
				} else {
					Iterator iter = curr.iterator();
					while (iter.hasNext()) {
						Object n = iter.next();
						if (localIndex-- == 0) {
							return n;
						}
					}
				}
			}
			count += curr.size();
		}
		throw new IndexOutOfBoundsException(String.valueOf(i));
	}

	/**
	 * Gets the sum of the sizes of the individual lists.
	 * @see java.util.List#size()
	 */
	public int size() {
		int count = 0;
		Iterator cols = L.iterator();
		while (cols.hasNext()) {
			Collection curr = (Collection) cols.next();
			count += curr.size();
		}
		return count;
	}

	/**
	 * Tests to see that all the component lists are empty.
	 * @see java.util.List#isEmpty()
	 */
	public boolean isEmpty() {
		Iterator cols = L.iterator();
		while (cols.hasNext()) {
			Collection curr = (Collection) cols.next();
			if (!curr.isEmpty()) {
				return false;
			}
		}
		return true;
	}
	
	/**
	 * Doesn't clear each list; instead, clears the list of
	 * lists.
	 * @see java.util.AbstractList#clear()
	 */
	public void clear() {
		L.clear();
	}
	
	/**
	 * Gets the backing list.
	 * @return Returns the backing list.
	 */
	public List getInnerList() {
		return L;
	}
	/**
	 * Set the list backing this flattened list
	 * to something else.
	 * @param L the new backing list
	 */
	public void setInnerList(List L) {
		this.L = L;
	}
}