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

package edu.umd.cfar.lamp.apploader.propertysheets;

/**
 * Generated when trying to write a read-only property or reading a write-only
 * property.
 */
public class PropertyAccessException extends PropertyException {
	/**
	 * Constructs an <code>PropertyAccessException</code> with no detail
	 * message.
	 */
	public PropertyAccessException() {
		super();
	}

	/**
	 * Constructs an <code>PropertyAccessException</code> with the given
	 * error string
	 * @param s The error string
	 */
	public PropertyAccessException(String s) {
		super(s);
	}
}
