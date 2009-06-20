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

import java.lang.reflect.*;

/**
 * Simple implementation of the PropertInterfacer interface
 * that uses java Method objects to interact with the property.
 */
public abstract class PropertyAdapter implements PropertyInterfacer {
	protected String name;
	protected Method getter;
	protected Method setter;

	private int hashObj(Object o) {
		return (o == null) ? 0 : o.hashCode();
	}
	private boolean sameObj(Object a, Object b) {
		return a == b || (a != null && a.equals(b));
	}
	/**
	 * Hashes based on the name, getter and setter.
	 * @return a hash code
	 */
	public int hashCode() {
		return hashObj(name) ^ hashObj(getter) ^ hashObj(setter);
	}
	/**
	 * Tests to see that the other property has the same
	 * name getter and setter.
	 * @param o the other property adapter to compare with
	 * @return <code>true</code> iff both this and the parameter
	 * are property adapters that refer the the same name, getter 
	 * and setter
	 */
	public boolean equals(Object o) {
		if (this == o) {
			return true;
		} else if (o instanceof PropertyAdapter) {
			PropertyAdapter that = (PropertyAdapter) o;
			return sameObj(name, that.name) && sameObj(getter, that.getter) && sameObj(setter, that.setter);
		} else {
			return false;
		}
	}

	/**
	 * @see edu.umd.cfar.lamp.apploader.propertysheets.PropertyInterfacer#setValue(java.lang.Object, java.lang.Object)
	 */
	public void setValue(Object bean, Object value) {
		if (getter != null && setter == null) {
			throw new PropertyAccessException("Read-Only Error: (" + bean + ")." + name + " = " + value);
		}
		try {
			setter.invoke(bean, new Object[]{value});
		} catch (NullPointerException npx) {
			throw new InapplicablePropertyException("No set method found for property " + name);
		} catch (IllegalArgumentException e) {
			throw new RuntimeException(e);
		} catch (IllegalAccessException e) {
			throw new RuntimeException(e);
		} catch (InvocationTargetException e) {
			throw new RuntimeException(e);
		}
	}

	/**
	 * @see edu.umd.cfar.lamp.apploader.propertysheets.PropertyInterfacer#getValue(java.lang.Object)
	 */
	public Object getValue(Object bean) {
		if (getter == null && setter != null) {
			throw new PropertyAccessException("Write-Only Error: (" + bean + ")." + name);
		}
		try {
			return getter.invoke(bean, new Object[0]);
		} catch (NullPointerException npx) {
			throw new InapplicablePropertyException("No get/is method found on " + bean + " for property " + name);
		} catch (IllegalArgumentException e) {
			throw new RuntimeException(e);
		} catch (IllegalAccessException e) {
			throw new RuntimeException(e);
		} catch (InvocationTargetException e) {
			throw new RuntimeException(e);
		}
	}

	/**
	 * @see edu.umd.cfar.lamp.apploader.propertysheets.PropertyInterfacer#isWritableOn(java.lang.Object)
	 */
	public boolean isWritableOn(Object bean) {
		return isWritable();
	}
	/**
	 * @see edu.umd.cfar.lamp.apploader.propertysheets.PropertyInterfacer#isReadableOn(java.lang.Object)
	 */
	public boolean isReadableOn(Object bean) {
		return isReadable();
	}

	/**
	 * @see edu.umd.cfar.lamp.apploader.propertysheets.PropertyInterfacer#isReadable()
	 */
	public boolean isReadable() {
		return getter != null;
	}
	/**
	 * @see edu.umd.cfar.lamp.apploader.propertysheets.PropertyInterfacer#isWritable()
	 */
	public boolean isWritable() {
		return setter != null;
	}

	/**
	 * @see edu.umd.cfar.lamp.apploader.propertysheets.PropertyInterfacer#getName()
	 */
	public String getName() {
		return name;
	}
}
