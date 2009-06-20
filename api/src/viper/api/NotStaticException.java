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
 * Thrown to indicate that the user is attempting to get the value
 * or set the value of a dynamic attribute as if it were a static
 * attribute.
 */
public class NotStaticException extends BadAttributeDataException {
	/**
	 * Constructs a <code>NotStaticException</code> with no
	 * detail message.
	 */
	public NotStaticException() {
		super();
	}

	/**
	 * Constructs a <code>NotStaticException</code> with the given
	 * detail message.
	 * @param message the detail message
	 */
	public NotStaticException(String message) {
		super(message);
	}
}
