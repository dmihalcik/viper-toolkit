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

/**
 * Thrown when attempting to create or use an unknown 
 * attribute type.
 */
public class UnknownAttributeTypeException extends IllegalArgumentException {
	/**
	 * Constructs an <code>UnknownAttributeTypeException</code> with no
	 * detail message.
	 */
	public UnknownAttributeTypeException() {
		super();
	}

	/**
	 * Constructs an <code>UnknownAttributeTypeException</code> with the
	 * given detail message.
	 * @param message the detail message
	 */
	public UnknownAttributeTypeException(String message) {
		super(message);
	}
}
