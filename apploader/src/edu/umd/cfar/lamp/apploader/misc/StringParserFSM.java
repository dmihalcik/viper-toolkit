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

package edu.umd.cfar.lamp.apploader.misc;

/**
 * Provides an interface for my kind of text field - the kind that won't even
 * let you type something that isn't valid. Although I usually use FSMs to
 * implement this, thus the name, it is by no means a constraint on the 
 * implementation. Feel free to consult whatever psychic necessary.
 * 
 * @author davidm@cfar.umd.edu
 * @since Jul 10, 2003
 */
public abstract class StringParserFSM {
	/**
	 * Parse the next character. This isn't terribly Unicode safe, now, is it?
	 * @param c the character to push onto the stack
	 * @return <code>true</code> if the character was added to the stack
	 */
	public abstract boolean pushDown(char c);
	
	/**
	 * Reset to the start state.
	 */
	public abstract void reset();

	/**
	 * Gets the current parsed characters. It should be a valid
	 * string of the language the FSM represents, or an empty string.
	 * It is up to the implementor to decide what to do with a partial
	 * string.
	 * @return the current parsed characters
	 */
	public abstract String toString();
	
	/**
	 * Gets a valid version from the current state.
	 * For example, a floating point with toString
	 * as "12e" would return the valid part "12",
	 * or a bounding box parsed as "12 13 4" might
	 * return "12 13 4 0" or "".
	 * 
	 * @return Default is to return the value of 'toString' method
	 */
	public String getValidString() {
		return toString();
	}
	
	/**
	 * 
	 * @param s the characters to try to push down
	 * @return <code>false</code> if any characters were not added
	 */
	public boolean addString(String s) {
		boolean perfect = true;
		for (int i = 0; i < s.length(); i++) {
			perfect = pushDown(s.charAt(i)) && perfect;
		}
		return perfect;
	}
}
