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

package viper.descriptors;

/**
 * Thrown if there is a data block overrun in the VReader.
 */
public class EndOfBlockException extends Exception {

	/**
	 * Constructs a new exception with no detail message.
	 */
	public EndOfBlockException() {
	}

	/**
	 * Constructs a new exception with the given
	 * detail message.
	 * @param s the detail message
	 */
	public EndOfBlockException(String s) {
		super(s);
	}
}