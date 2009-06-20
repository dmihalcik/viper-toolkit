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

package viper.comparison.distances;

/**
 * Thrown when a distance cannot be determined, since it is completely within an
 * ignored region. This is useful, for example, when a candidate box falls
 * within an ignored target box, such as illegible text in a text detection
 * algorithm evaluation.
 */
public class IgnoredValueException extends Exception {
	
	/**
	 * Constructs a new exception with no detail message.
	 */
	public IgnoredValueException() {
		super();
	}

	/**
	 * Constructs a new exception with the given detail message.
	 * @param s the detail message. Perhaps it should explain why 
	 * there was no value found to care about
	 */
	public IgnoredValueException(String s) {
		super(s);
	}
}