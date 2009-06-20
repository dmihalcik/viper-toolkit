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

/**
 * Generic functions for interacting with a property of
 * a java object.
 */
public interface PropertyInterfacer {
	/**
	 * Get the display name of the property.
	 * @return The (possibly localized) display name.
	 */
	public String getName();
	
	/**
	 * Gets the Class associated with the property
	 * @return The java class of the property
	 */
	public Class getPropertyClass();
	
	/**
	 * Checks to see if the property may be readable.
	 * @return <code>true</code> if there exists some instances
	 * that allow this property to be read.
	 */
	public boolean isReadable();

	/**
	 * Checks to see if the property may be settable.
	 * @return <code>true</code> if there exists some instances
	 * that allow this property to be set.
	 */
	public boolean isWritable();
	
	/**
	 * Sets a specific property of the passed bean to 
	 * the given value.
	 * @param bean The java object to set the property specified by this
	 *     PropertyInterfacer instance of. 
	 * @param value The new value for the property
	 * @throws InapplicablePropertyException
	 * @throws PropertyAccessException
	 */
	public void setValue(Object bean, Object value);
	
	/**
	 * Gets the current value of the property on the given bean.
	 * @param bean
	 * @return
	 * @throws InapplicablePropertyException
	 * @throws PropertyAccessException
	 */
	public Object getValue(Object bean);

	/**
	 * Checks to see if the property is writable
	 * on the given bean.
	 * @param bean The bean to check
	 * @return <code>true</code> if the bean's property may be written
	 * @throws InapplicablePropertyException
	 */
	public boolean isWritableOn(Object bean);
	
	/**
	 * Checks to see if the property is readable
	 * on the given bean.
	 * @param bean The bean to check
	 * @return <code>true</code> if the bean's property may be read
	 * @throws InapplicablePropertyException
	 */
	public boolean isReadableOn(Object bean);
}
