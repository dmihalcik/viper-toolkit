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

package edu.umd.cfar.lamp.apploader.prefs;

/**
 * Thrown by a preference handler to indicate errors loading pref files,
 * errors in pref file format, or incorrect preference access.
 * 
 * @author davidm
 */
public class PreferenceException extends Exception {
	/**
	 * Encapsulates the throwable with a PreferenceException
	 * to avoid giving unnecessary information to callers.
	 * @param cause The thrown thing that this exception wraps.
	 */
	public PreferenceException(Throwable cause) {
		super(cause);
	}

	/**
	 * Encapsulates the throwable with a PreferenceException
	 * and some additional text. Avoids giving unnecessary information 
	 * to callers.
	 * @param message New detail message to display instead of cause's
	 * @param cause The thrown thing that this exception wraps.
	 */
	public PreferenceException(String message, Throwable cause) {
		super(message, cause);
	}
	
	/**
	 * Throws a pref exception with the given detail message.
	 * @param message New error detail message to display
	 */
	public PreferenceException(String message) {
		super(message);
	}
	
	/**
	 * Default constructor, with null message.
	 */
	public PreferenceException() {
		super();
	}
}
