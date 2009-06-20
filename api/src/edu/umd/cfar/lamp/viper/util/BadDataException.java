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
 * This is thrown when the information entered from a file into an attribute's
 * value field does not conform. For example, setting a bbox to "false". If you
 * are passing a parse error, please set the character offset for the
 * ErrorWriter, as it helps it find the error on multiline files.
 * 
 * @see edu.umd.cfar.lamp.viper.util.reader.CountingStringTokenizer
 * @see edu.umd.cfar.lamp.viper.util.reader.VReader
 */
public class BadDataException extends Exception {
	/**
	 * 1
	 */
	private static final long serialVersionUID = 1L;

	/** The first character of the bad data (for ErrorWriter). */
	private int start = -1;

	/** The last character of the error. */
	private int end = -1;

	/** Set to true if the character offsets were set. */
	private boolean charWise = false;

	/**
	 * Constructs a new BadDataException with no message.
	 */
	public BadDataException() {
	}

	/**
	 * Constructs a new BadDataException with the given message.
	 * 
	 * @param s
	 *            The detail message.
	 */
	public BadDataException(String s) {
		super(s);
	}

	/**
	 * Constructs a new BadDataException with the given message and indicates
	 * where in the line is the erroneous text.
	 * 
	 * @param s
	 *            The detail message.
	 * @param startCharacter
	 *            The offset of the first character. If set up properly, the
	 *            theory is that emacs could put your cursor here.
	 * @param endCharacter
	 *            The last character. In theory, a good ErrorWriter could then
	 *            underline the error, like jikes does.
	 */
	public BadDataException(String s, int startCharacter, int endCharacter) {
		super(s);
		start = startCharacter;
		end = endCharacter;
		charWise = true;
	}

	/**
	 * Determines if the error message contains data about where the error
	 * occurred.
	 * 
	 * @return <code>true</code> if there is offset info.
	 */
	public boolean isChar() {
		return charWise;
	}

	/**
	 * Gets the first character offset.
	 * 
	 * @return The offset of the first character.
	 */
	public int getStart() {
		return start;
	}

	/**
	 * Gets the last character offset.
	 * 
	 * @return The offset of the last character.
	 */
	public int getEnd() {
		return end;
	}
}
