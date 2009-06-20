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

import javax.swing.*;

import edu.umd.cfar.lamp.apploader.*;
import edu.umd.cfar.lamp.apploader.prefs.*;

/**
 * Describes a property of a given instance of a class.
 */
public interface InstancePropertyDescriptor {
	/**
	 * Get the value from the bean instance.
	 * @param bean The instance to get this property from 
	 * @return The value of the bean
	 * @throws PropertyException When the value can't be extracted.
	 */
	public abstract Object applyGetter(Object bean);
	/**
	 * Set the value of this property on the given bean
	 * @param bean The instance to set this property of
	 * @param toValue The new value for the property
	 * @throws PropertyException When the value can't be set.
	 */
	public abstract void applySetter(Object bean, Object toValue);
	/**
	 * Checks to see if the property is currently settable on the 
	 * given instance bean.
	 * @param bean the instance to check
	 * @return <code>false</code> if the bean may not be set
	 */
	public abstract boolean isSettable(Object bean);
	
	/**
	 * Gets the associated cell editor for the property
	 * @param bean
	 * @param core
	 * @return
	 * @throws PreferenceException
	 */
	public abstract JComponent getEditor(Object bean, AppLoader core)
		throws PreferenceException;

	/**
	 * Gets the associated cell renderer for the property.
	 * @param bean
	 * @param core
	 * @return
	 * @throws PreferenceException
	 */
	public abstract JComponent getRenderer(Object bean, AppLoader core)
		throws PreferenceException;
		
	/**
	 * Get a property interfacer for the bean.
	 * @return
	 */
	public abstract PropertyInterfacer getInterfacer();

	/**
	 * Set the interfacer for the property.
	 * @param functor
	 */
	public abstract void setInterfacer(PropertyInterfacer functor);

	/**
	 * Gets the java class of the bean.
	 * @return
	 */
	public abstract Class getBeanType();

	/**
	 * Gets the display name of the property.
	 * @return
	 */
	public abstract String getName();
}