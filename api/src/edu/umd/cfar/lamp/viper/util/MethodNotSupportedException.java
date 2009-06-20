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

package edu.umd.cfar.lamp.viper.util;

/**
 * Thrown by classes that may or may not have a specific function implemented,
 * and this instance does not. For example, an Attribute may or may not be
 * composable, and calling compose on an uncomposable attribute will throw one
 * of these exceptions. (I really should use java's
 * UnsupportedOperationException, but that extends RuntimeException and I want
 * this more hardcore.)
 */
public class MethodNotSupportedException extends Exception {
	/**
	 * 1
	 */
	private static final long serialVersionUID = 1L;

	/**
	 * Constructs an MethodNotSupportedException with no detail message.
	 */
	public MethodNotSupportedException() {
	}

	/**
	 * Constructs an MethodNotSupportedException with the specified detail
	 * message.
	 * 
	 * @param s
	 *            The detail message.
	 */
	public MethodNotSupportedException(String s) {
		super(s);
	}
}
