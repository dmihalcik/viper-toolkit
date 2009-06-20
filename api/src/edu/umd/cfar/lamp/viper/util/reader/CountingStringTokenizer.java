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

package edu.umd.cfar.lamp.viper.util.reader;

import java.util.*;

/**
 * This class extends the StringTokenizer to also maintain a character index of
 * each new token for error reporting.
 */
public class CountingStringTokenizer extends StringTokenizer {
	private static final String defaultDelim = " \t\n\r\f";

	int offset = -1;
	int end = 0;
	boolean returningTokes = false;
	String delim = defaultDelim;

	String nextToken;
	int nextOffset = -1;
	int nextEnd = 0;
	NoSuchElementException cachedNsex;

	/**
	 * Get the index into the parent string of the last character of the most
	 * recently returned token.
	 * 
	 * @return index of first char, will be 0 is no token has been taken
	 */
	public int getEnd() {
		return end;
	}

	/**
	 * Get the index into the parent string of the first character of the most
	 * recently returned token.
	 * @return index of first char, will be -1 is no token has been taken
	 */
	public int getStart() {
		return offset;
	}

	/**
	 * Constructs a new string tokenizer with the default 
	 * delimiters and does not return them as tokens.
	 * @param str the string to chop up
	 */
	public CountingStringTokenizer(String str) {
		this(str, defaultDelim, false);
	}

	/**
	 * Constructs a new tokenizer with the given 
	 * string and tokens.
	 * @param str the string to chop up
	 * @param delim the characters to use as delimiters
	 */
	public CountingStringTokenizer(String str, String delim) {
		this(str, delim, false);
	}

	/**
	 * Constructs a new tokenizer with the given 
	 * string and tokens, while allowing the delimiters to
	 * be returned as tokens.
	 * @param str the string to chop up
	 * @param delim the characters to use as delimiters
	 * @param returnTokens <code>true</code> if you would
	 * like all characters to be returned, not just non-delims
	 */
	public CountingStringTokenizer(
		String str,
		String delim,
		boolean returnTokens) {
		super(str, delim, true);
		returningTokes = returnTokens;
		this.delim = delim;

		try {
			nextToken = super.nextToken();
			while (!returnTokens
				&& (delim.indexOf(nextToken.charAt(0)) >= 0)) {
				nextToken = super.nextToken();
				nextOffset = nextEnd++;
			}
			nextOffset = nextEnd;
			nextEnd += nextToken.length();
		} catch (NoSuchElementException nsex) {
			cachedNsex = nsex;
		}
	}

	/**
	 * Unsupported operation.
	 * @return 
	 * @throws UnsupportedOperationException
	 */
	public int countTokens() {
		throw new UnsupportedOperationException();
	}

	/**
	 * Tests to see if more tokens remain.
	 * @return <code>true</code> if next returns 
	 * an element, instead of throwing an exception.
	 */
	public boolean hasMoreElements() {
		return hasMoreTokens();
	}

	/**
	 * Tests to see if more tokens remain.
	 * @return <code>true</code> if next returns 
	 * an element, instead of throwing an exception.
	 */
	public boolean hasMoreTokens() {
		return cachedNsex == null;
	}

	/**
	 * Gets the next token
	 * @return the next token, if it exists
	 * @throws NoSuchElementException
	 */
	public Object nextElement() {
		return nextToken();
	}

	/**
	 * Gets the next token
	 * @return the next token, if it exists
	 * @throws NoSuchElementException
	 */
	public String nextToken() {
		if (cachedNsex != null) {
			throw (NoSuchElementException) cachedNsex.fillInStackTrace();
		}

		String currToken = nextToken;
		offset = nextOffset;
		end = nextEnd;
		try {
			nextToken = super.nextToken();
			if (returningTokes) {
				nextOffset = nextEnd;
				nextEnd += nextToken.length();
			} else {
				while (delim.indexOf(nextToken.charAt(0)) >= 0) {
					nextToken = super.nextToken();
					nextOffset = nextEnd++;
				}
				nextOffset = nextEnd;
				nextEnd += nextToken.length();
			}
		} catch (NoSuchElementException nsex) {
			cachedNsex = nsex;
		}
		return currToken;
	}

	/**
	 * Unsupported operation
	 * @param delim the delimiters to switch to
	 * @return the next token, if it exists
	 * @throws NoSuchElementException
	 * @throws UnsupportedOperationException
	 */
	public String nextToken(String delim) {
		throw new UnsupportedOperationException();
	}
}
