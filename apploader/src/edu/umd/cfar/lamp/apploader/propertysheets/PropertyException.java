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
 * Thrown when a property has problems.
 */
public class PropertyException extends IllegalArgumentException {
	/**
	 * Constructs an <code>PropertyException</code> with no detail
	 * message.
	 */
	public PropertyException() {
		super();
	}

	/**
	 * Constructs an <code>PropertyException</code> with the given
	 * error string
	 * @param s The error string
	 */
	public PropertyException(String s) {
		super(s);
	}
}
