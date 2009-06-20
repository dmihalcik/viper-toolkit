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
 * Thrown to indicate that the application has tried to set an 
 * attribute to a value that is not allowed. While it was chosen to
 * subclass IllegalArgumentException, another choice could have been
 * ClassCastException.
 */
public class BadAttributeDataException extends IllegalArgumentException {
	/**
	 * Constructs a <code>BadAttributeDataException</code> with no
	 * detail message.
	 */
	public BadAttributeDataException() {
		super();
	}

	/**
	 * Constructs a <code>BadAttributeDataException</code> with the
	 * specified detail message.
	 * @param message the exception detail string
	 */
	public BadAttributeDataException(String message) {
		super(message);
	}
}
