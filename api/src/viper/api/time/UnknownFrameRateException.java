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

package viper.api.time;

/**
 * Thrown to indicate that the descriptor or attribute is being 
 * accessed by frame/time when it is stored in the other format
 * without access to a conversion routine.
 */
public class UnknownFrameRateException extends UnsupportedOperationException {
	/**
	 * Constructs an <code>UnknownFrameRateException</code> with no
	 * detail message.
	 */
	public UnknownFrameRateException() {
		super();
	}

	/**
	 * Constructs an <code>UnknownFrameRateException</code> with the
	 * given detail message.
	 * @param message the detail message
	 */
	public UnknownFrameRateException(String message) {
		super(message);
	}
}
