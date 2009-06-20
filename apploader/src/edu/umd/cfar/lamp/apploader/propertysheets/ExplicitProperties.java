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

package edu.umd.cfar.lamp.apploader.propertysheets;

import java.util.*;

import edu.umd.cfar.lamp.apploader.prefs.*;

/**
 * A property sheet, allowing the user to edit different 
 * properties. The default implementation uses the javabean
 * standard naming conventions to extract property names and
 * types. The user/system can override this with a the preferences.
 * 
 * The explicit properties are all the ones mentioned in the 
 * hasProperties or propertyOrder links. They are instances of 
 * the {@link InstancePropertyDescriptor} interface.
 */
public class ExplicitProperties extends AbstractList implements InstancePropertyList {
	private PrefsManager prefs;
	private List props;
	private Object bean;

	/**
	 * Create a new, empty property sheet.
	 */
	public ExplicitProperties() {
		props = new LinkedList();
	}

	/**
	 * Gets the associated application preferences.
	 * @return The application preferences
	 */
	public PrefsManager getPrefs() {
		return prefs;
	}
	/**
	 * Sets the associated application preferences.
	 * @param manager the application preferences
	 */
	public void setPrefs(PrefsManager manager) {
		prefs = manager;
	}

	/**
	 * Sets the properties to the given list
	 * of properties.
	 * @param props All of the properties.
	 */
	public void setProps(List props) {
		this.props = props;
	}

	/**
	 * Set the instance object that this is looking at.
	 * @see InstancePropertyList#setObject(Object)
	 */
	public void setObject(Object o) {
		bean = o;
	}

	/**
	 * Get the current instance object.
	 * @return The instance that this is currently wrapping.
	 */
	public Object getObject() {
		return bean;
	}

	/**
	 * @see edu.umd.cfar.lamp.apploader.propertysheets.InstancePropertyList#refresh()
	 */
	public void refresh() {
		// Since the attributes are dependent on the class, 
		// not the instance, nothing changes when the instance
		// changes
	}

	/**
	 * Removes all properties.
	 * @see java.util.List#clear()
	 */
	public void clear() {
		props.clear();
	}

	/**
	 * @see java.util.List#contains(java.lang.Object)
	 */
	public boolean contains(Object o) {
		return props.contains(o);
	}
	/**
	 * @see java.util.List#containsAll(java.util.Collection)
	 */
	public boolean containsAll(Collection c) {
		return props.containsAll(c);
	}
	/**
	 * Tests to see that the property lists are equal.
	 * @see java.util.List#equals(java.lang.Object)
	 */
	public boolean equals(Object obj) {
		return props.equals(obj);
	}

	private static boolean objEq (Object a, Object b) {
		return null==a?b==null:a.equals(b);
	}
	private static int objHash (Object a) {
		return null==a?0:a.hashCode();
	}

	/**
	 * @see java.util.List#get(int)
	 */
	public Object get(int index) {
		return props.get(index);
	}
	/**
	 * @see java.util.List#hashCode()
	 */
	public int hashCode() {
		return props.hashCode() ^ objHash(bean);
	}

	/**
	 * @see java.util.List#indexOf(java.lang.Object)
	 */
	public int indexOf(Object o) {
		return props.indexOf(o);
	}

	/**
	 * @see java.util.List#isEmpty()
	 */
	public boolean isEmpty() {
		return props.isEmpty();
	}

	/**
	 * @see java.util.List#iterator()
	 */
	public Iterator iterator() {
		return props.iterator();
	}

	/**
	 * @see java.util.List#lastIndexOf(java.lang.Object)
	 */
	public int lastIndexOf(Object o) {
		return props.lastIndexOf(o);
	}

	/**
	 * @see java.util.List#listIterator()
	 */
	public ListIterator listIterator() {
		return props.listIterator();
	}

	/**
	 * @see java.util.List#listIterator(int)
	 */
	public ListIterator listIterator(int index) {
		return props.listIterator(index);
	}

	/**
	 * @see java.util.List#remove(int)
	 */
	public Object remove(int index) {
		return props.remove(index);
	}

	/**
	 * @see java.util.List#remove(java.lang.Object)
	 */
	public boolean remove(Object o) {
		return props.remove(o);
	}

	/**
	 * @see java.util.List#removeAll(java.util.Collection)
	 */
	public boolean removeAll(Collection c) {
		return props.removeAll(c);
	}

	/**
	 * @see java.util.List#retainAll(java.util.Collection)
	 */
	public boolean retainAll(Collection c) {
		return props.retainAll(c);
	}

	/**
	 * @see java.util.List#set(int, java.lang.Object)
	 */
	public Object set(int index, Object element) {
		return props.set(index, element);
	}

	/**
	 * @see java.util.List#size()
	 */
	public int size() {
		return props.size();
	}

	/**
	 * @see java.util.List#subList(int, int)
	 */
	public List subList(int fromIndex, int toIndex) {
		return props.subList(fromIndex, toIndex);
	}

	/**
	 * @see java.util.List#toArray()
	 */
	public Object[] toArray() {
		return props.toArray();
	}

	/**
	 * @see java.util.List#toArray(java.lang.Object[])
	 */
	public Object[] toArray(Object[] a) {
		return props.toArray(a);
	}

	/**
	 * @see java.util.AbstractCollection#toString()
	 */
	public String toString() {
		return props.toString();
	}

	/**
	 * @see java.util.List#add(int, java.lang.Object)
	 */
	public void add(int index, Object element) {
		props.add(index, element);
	}

	/**
	 * @see java.util.List#add(java.lang.Object)
	 */
	public boolean add(Object o) {
		return props.add(o);
	}

	/**
	 * @see java.util.List#addAll(int, java.util.Collection)
	 */
	public boolean addAll(int index, Collection c) {
		return props.addAll(index, c);
	}

	/**
	 * @see java.util.List#addAll(java.util.Collection)
	 */
	public boolean addAll(Collection c) {
		return props.addAll(c);
	}
}
