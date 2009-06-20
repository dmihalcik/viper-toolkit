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
 * Thrown when there is an error on the command line. 
 * The message should be one that can be printed to the 
 * end user to indicate that there was an error. 
 * 
 * I'd recommend you get the text from the lal:PreferenceTrigger 
 * node to get a localized message.
 * 
 * @author Watch
 * @since Apr 26, 2003
 */
public class ArgumentException extends PreferenceException {
	/**
	 * Encapsulates the throwable with a ArgumentException
	 * to avoid giving unnecessary information to callers.
	 * @param cause The thrown thing that this exception wraps.
	 */
	public ArgumentException(Throwable cause) {
		super(cause);
	}

	/**
	 * Encapsulates the throwable with a ArgumentException
	 * and some additional text. Avoids giving unnecessary information 
	 * to callers.
	 * @param message New detail message to display instead of cause's
	 * @param cause The thrown thing that this exception wraps.
	 */
	public ArgumentException(String message, Throwable cause) {
		super(message, cause);
	}
	
	/**
	 * Throws a pref exception with the given detail message.
	 * @param message New error detail message to display
	 */
	public ArgumentException(String message) {
		super(message);
	}
	
	/**
	 * Default constructor, with null message.
	 */
	public ArgumentException() {
		super();
	}
}
