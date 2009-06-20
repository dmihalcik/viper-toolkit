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

package viper.api;

import viper.api.time.*;

/**
 * A wrapper class that converts between the parsed form
 * of the data and a user-acceptable form of the data. For
 * example, the parsed form of an LValue is an integer x
 * 0 <= x < |lvalues|. This allows value checking and for conversion
 * between a large public form to a compact private one.
 * 
 * This is probably going to be replaced by a simpler method
 * that just checks the validity of the data without
 * allowing transforms. I may even just use Jena2's datatype 
 * system instead - this supports the default xsd datatypes, 
 * and uses a similar system.
 */
public interface AttrValueWrapper {
	/**
	 * Get the external value of the attribute. Like 
	 * setAttributeValue, but doesn't do any 
	 * value checking. This means that 
	 * <code>getObjectValue(setAttributeValue(o)).equals(o)</code>
	 * should be true. There is nothing preventing 
	 * <code>setAttributeValue(o).equals(o)</code>
	 * from returning <code>true</code>, but this 
	 * is not required. 
	 * <pre>
	 *    a.setAttributeValue(o1).equals(b.setAttributeValue(o2))
	 * 		&& a.equals(b)
	 * 	=> o1.equals(o2)
	 * </pre>
	 * @param encodedFormat
	 * @param container TODO
	 * @param instant TODO
	 * @return the unencoded value
	 * @throws BadAttributeDataException
	 */
	public Object getObjectValue(Object encodedFormat, Node container, Instant instant);

	/**
	 * Converts the object from external/unencoded form
	 * into internal/encoded form, while checking to make sure it
	 * is valid.
	 * @param o the new value to set
	 * @param container TODO
	 * @return The object in internal format
	 * @throws BadAttributeDataException if <code>o</code> doesn't fit with the current attribute type
	 */
	public Object setAttributeValue(Object o, Node container);
}
