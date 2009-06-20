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
 * Thrown when a {@link PropertyInterfacer} is used on an instance
 * for which it is ill-prepared or that does not support the property.
 */
public class InapplicablePropertyException extends PropertyException {

	/**
	 * Constructs an <code>InapplicablePropertyException</code> with no detail
	 * message.
	 */
	public InapplicablePropertyException() {
		super();
	}

	/**
	 * Constructs an <code>InapplicablePropertyException</code> with the given
	 * error string
	 * @param s The error string
	 */
	public InapplicablePropertyException(String s) {
		super(s);
	}

}
