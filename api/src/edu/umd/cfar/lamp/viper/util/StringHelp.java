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

import java.io.*;
import java.text.*;
import java.util.*;

import org.apache.commons.lang.*;

/**
 * This class contains generic static string manipulation and 
 * generation functions.
 */
public class StringHelp {
	/**
	 * This is useful for generating jikes-style underlines for compiler
	 * errors and warnings. For example, if there is an unknown identifier
	 * foo in a statement, pass the underliner (true, true, first char index,
	 * last char index), and then print out the underline after printing
	 * the offending line. eg:
	 * <pre>
	 *    i += foo;
	 *         <->
	 * </pre>
	 * The first two arguments allow for multiline underlines. eg:
	 * <pre>
	 *        <-----------------------------------------
	 *    i = ((Descriptor) vec.elementAt (g).sameTypeAs
	 *          (other);
	 *          ------^
	 * </pre>
	 * @param starts Does the thing to be underlined start on this line?
	 * @param ends Does it end on this line?
	 * @param start The character offset of the first offending mark on this line
	 * @param end The last character in the error on this line.
	 * @return a string, such as "<-->", to be used to underline errors
	 */
	public static String underliner(
		boolean starts,
		boolean ends,
		int start,
		int end) {
		StringBuffer line = new StringBuffer(end);
		char current = ' ';
		if (!starts)
			current = '-';
		for (int i = 0; i < (starts ? start : end); i++)
			line.append(current);
		if (start - end == 0) {
			line.append('-');
		} else {
			if (starts) {
				line.append('<');
				for (int i = start + 1; i < end - 1; i++)
					line.append('-');
				if (ends)
					line.append('>');
				else
					line.append('-');
			} else {
				line.append('^');
			}
		}
		return line.toString();
	}

	/**
	 * This function returns a string equal to <code>amount</code>
	 * number of ' ' characters added to the front of the string
	 * <code>S</code>.
	 * @param amount Number of spaces to pad.
	 * @param S The string that requires padding.
	 * @return " "*<code>amount</code> + <code>S</code>
	 */
	public static String padLeft(int amount, String S) {
		if (0 > (amount = amount - S.length()))
			return (S);
		char[] pad = new char[amount];
		for (int i = 0; i < amount; i++)
			pad[i] = ' ';
		return ((new String(pad)) + S);
	}

	/**
	 * Divides a String by its whitespace.
	 *
	 * @param line The String to divide.
	 * @return An Array of Strings taken from between the whitespace characters.
	 */
	public static String[] splitSpaces(String line) {
		if (line == null)
			return (new String[0]);

		int start, end;

		Vector vec = new Vector();

		end = 0;
		while (true) {
			start = findNonBlank(line, end);
			if (start == -1)
				break;
			end = findBlank(line, start);
			if (end == -1) {
				vec.addElement(line.substring(start));
				break;
			} else {
				vec.addElement(line.substring(start, end));
			}
		}

		String[] result = new String[vec.size()];
		for (int j = 0; j < vec.size(); j++) {
			result[j] = (String) vec.elementAt(j);
		}

		return (result);
	}

	/**
	 * Get the index of the first character that isn't a space.
	 * 
	 * @param line The String to search.
	 * @param startIndex Where to start looking.
	 * @return The index of the first non-space character.
	 *    If there is none, it returns -1.
	 */
	private static int findNonBlank(String line, int startIndex) {
		int length = line.length();
		for (int i = startIndex; i < length; i++) {
			char c = line.charAt(i);
			if (c != ' ')
				return (i);
		}
		return (-1);
	}

	/**
	 * Gets the index of the first space.
	 * 
	 * @param line The String to check.
	 * @param startIndex Where to start looking.
	 * @return The index of the space character. If there is none, it returns -1.
	 *
	 * @deprecated You'd be better off using {@link
	 *    java.lang.String#indexOf(String str, int fromIndex) String.indexOf(String, int)}.
	 */
	private static int findBlank(String line, int startIndex) {
		int length = line.length();
		for (int i = startIndex; i < length; i++) {
			char c = line.charAt(i);
			if (c == ' ')
				return (i);
		}
		return (-1);
	}

	/**
	 * Removes the first and last character of the string.
	 *
	 * @param line The String to be shortened
	 * @return The substring.
	 */
	public static String shorten(String line) {
		line = line.trim();

		return (line.substring(1, line.length() - 1).trim());
	}

	/**
	 * Get the characters from a bracketed list that are not in brackets.
	 * Returns a string if the input contains characters 
	 * between brackets
	 * such as the ... in the following:
	 * <pre>
	 *   ... [ foo ] ... [1.2 1.2] ... [hi] ...
	 * </pre>
	 * If there isn't anything outside the brackets, it returns null.
	 *
	 * @param line The String to check.
	 * @return The text outside brackets, or <code>null</code> if there
	 *    is no such text.
	 */
	public static String getExtraTextOutsideBrackets(String line) {
		Vector stringList = new Vector();
		boolean justEntered = false;
		int startPos = 0;

		int bracketCount = 0;
		for (int i = 0; i < line.length(); i++) {
			char c = line.charAt(i);
			if (c == '[') {
				bracketCount++;
				if (bracketCount != 0) {
					String str = line.substring(startPos, i);
					str = str.trim();
					if (str.length() > 0)
						stringList.addElement(str);
				}
			} else if (c == ']') {
				bracketCount--;
				if (bracketCount == 0)
					justEntered = true;
			} else if (bracketCount == 0) {
				if (justEntered) {
					justEntered = false;
					startPos = i;
				}
			}
		}
		if (!justEntered) {
			String str = line.substring(startPos);
			str = str.trim();
			if (str.length() > 0)
				stringList.addElement(str);
		}
		if (stringList.size() == 0)
			return (null);
		else {
			int size = stringList.size();
			String temp = (String) stringList.elementAt(0);
			for (int i = 1; i < size; i++) {
				String temp2 = (String) stringList.elementAt(i);
				temp += ", " + temp2;
			}
			return (temp);
		}
	}

	/**
	 * Prints a warning for test that should all be inside brackets
	 * but has some outside. It gets the text from line and, if there
	 * is any, prints it out as a {@link
	 * ErrorWriter#printWarning(String) warning}.
	 *
	 * @param line The line that might have a problem.
	 * @param err The ErrorWriter that gets the warning, if there is one.
	 * @see #splitByBrackets(String line)
	 */
	public static void handleExtraTextOutsideBrackets(
		String line,
		ErrorWriter err) {
		String extra = StringHelp.getExtraTextOutsideBrackets(line);
		if (extra != null)
			err.printWarning("Ignoring unrecognized text : (" + extra + ")");
	}

	/**
	 * Extracts a list of all strings contained within brackets.
	 * Assumes list contains bracketed elements, as in:
	 * <pre>
	 *   [ foo ]  [1.2 1.2] [hi]
	 * </pre>
	 * This will return {" foo ", "1.2 1.2", "hi"}.
	 *
	 * @param line The data to be split.
	 * @throws IllegalArgumentException if brackets are unbalanced
	 * @return An Array containing the Strings inside the brackets.
	 */
	public static String[] splitByBrackets(String line) {
		if (line == null) {
			return (new String[0]);
		}
		int start = 0;
		int state = 0;
		List L = new LinkedList();
		for (int i = 0; i < line.length(); i++) {
			char c = line.charAt(i);
			if (c == '[') {
				if (0 == state++) {
					start = i + 1;
				}
			} else if (c == ']') {
				if (0 == --state) {
					L.add(line.substring(start, i));
				}
				if (state < 0) {
					throw new IllegalArgumentException(
						"Found unexpected end bracket in string: " + line);
				}
			}
		}
		if (state > 0) {
			throw new IllegalArgumentException(
				"String requires balanced brackets: " + line);
		}
		return (String[]) L.toArray(new String[L.size()]);
	}

	/**
	 * Checks to see if the String has marching '[' and ']' characters.
	 * 
	 * @param str The String in which to check the brackets.
	 * @return <ul><li><code>0</code>: Brackets match up.</li>
	 *             <li><code>-1</code>: Too many left brackets.</li>
	 *             <li><code>1</code>: Missing right brackets</li></ul>
	 */
	public static int hasUnmatchedBrackets(String str) {
		int bracketCount = 0;
		for (int i = 0; i < str.length(); i++) {
			char c = str.charAt(i);
			if (c == '[')
				bracketCount++;
			else if (c == ']')
				bracketCount--;

			if (bracketCount < 0)
				return (1); // too many right brackets
		}

		if (bracketCount > 0)
			return (-1); // missing right brackets

		return (0);
	}

	/**
	 * Converts parenthesized list of comma seperated items into
	 * an array of Strings.
	 * @param line A String of the format <code>(alpha, beta, gamma)</code>
	 * @return An array {"alpha", "beta", "gamma"}
	 * @deprecated If you must, use this line: <code>splitBySeparator (removeBrackets (line), ",")</code>
	 */
	public static String[] splitByCommas(String line) {
		return splitBySeparator(shorten(line), ',');
	}

	/**
	 * Split using a seperator.
	 *
	 * @param line The String to be seperated.
	 * @param sep The seperator character, eg a comma
	 * @return An array of Strings containing the seperated data.
	 * @see #splitBySeparatorAndParen(String line, char sep)
	 */
	public static String[] splitBySeparator(String line, char sep) {
		String newLine = line;
		Vector temp = new Vector();
		while (true) {
			int separatorIndex = newLine.indexOf(sep);
			if (separatorIndex == -1) {
				String lastNum = newLine.substring(separatorIndex + 1);
				temp.addElement(lastNum.trim());
				break;
			} else {
				String newNum = newLine.substring(0, separatorIndex);
				temp.addElement(newNum.trim());
			}
			newLine = newLine.substring(separatorIndex + 1);
		}
		String[] result = new String[temp.size()];
		for (int i = 0; i < result.length; i++) {
			result[i] = (String) temp.elementAt(i);
		}
		return (result);
	}

	/**
	 * Split using a separator, but allow for the separator to occur
	 * in nested parentheses without splitting.  
	 *   E.g.   
	 * <pre>
	 *     1, (2,3), 4
	 * </pre>
	 *  would split into 
	 * <UL>
	 *   <li> 1 </li>
	 *   <li> (2,3)  (Doesn't get split, even though it has comma) </li>
	 *   <li> 4 </li>
	 * </ul>
	 *
	 * @param line The String to be seperated.
	 * @param sep The seperator character, eg a comma
	 * @return An array of Strings containing the seperated data.
	 * @see #splitBySeparator(String line, char c)
	 */
	public static String[] splitBySeparatorAndParen(String line, char sep) {
		boolean withinQuotes = false;
		String newLine = new String(line);
		Vector temp = new Vector();

		int startIndex = 0;
		int nesting = 0;
		for (int i = 0; i < newLine.length(); i++) {
			char c = newLine.charAt(i);
			if (c == '"') {
				withinQuotes = !withinQuotes;
			} else if (!withinQuotes) {
				if (c == '(') {
					nesting++;
				} else if (c == ')') {
					nesting--;
				} else {
					if ((nesting == 0) && (c == sep)) {
						String s = newLine.substring(startIndex, i);
						temp.addElement(s);

						startIndex = i + 1;
					}
				}
			}
		}
		String s = newLine.substring(startIndex);
		temp.addElement(s);

		String[] result = new String[temp.size()];
		for (int i = 0; i < result.length; i++) {
			result[i] = (String) temp.elementAt(i);
		}
		return (result);
	}

	/**
	 * Split using a separator, but allow for the separator to occur
	 * in nested parentheses without splitting.
	 * <PRE>
	 *   E.g.   "1", 2*("2,3"), "4"
	 *      would split into
	 *        -- "1"
	 *        -- 2*("2,3")
	 *        -- "4"
	 * </PRE>
	 * If the data has an odd number of "s, it will append a " character
	 * to the end. In order to include a quote character without delimiting
	 * a string, use the \". For a \, use \\.
	 * @param line the string to split
	 * @param sep the seperator character, e.g. a comma
	 * @return the split string
	 */
	public static String[] splitBySeparatorQuoteAndParen(
		String line,
		char sep) {
		boolean withinQuotes = false;
		String newLine = new String(line);
		Vector temp = new Vector();
		StringBuffer nextString = new StringBuffer();
		int nesting = 0;

		for (int i = 0; i < newLine.length(); i++) {
			char c = newLine.charAt(i);

			if (c == '\\') {
				if ((++i >= newLine.length()) && (nextString.length() > 0)) {
					temp.addElement(nextString.toString());
					break;
				} else {
					switch (newLine.charAt(i)) {
						case 'n' :
							nextString.append('\n');
							break;

						case '"' :
							nextString.append('"');
							break;

						default :
							nextString.append(newLine.charAt(i));
					}
				}
			} else if (c == '"') {
				withinQuotes = !withinQuotes;
				nextString.append('"');
			} else if (!withinQuotes) {
				if (c == '(') {
					nesting++;
					nextString.append('(');
				} else if (c == ')') {
					nesting--;
					nextString.append(')');
				} else {
					if ((nesting == 0) && (c == sep)) {
						temp.addElement(nextString.toString());
						nextString.delete(0, nextString.length());
					} else {
						nextString.append(newLine.charAt(i));
					}
				}
			} else {
				nextString.append(newLine.charAt(i));
			}
		}

		if (withinQuotes) {
			nextString.append('"');
		}

		temp.addElement(nextString.toString());

		String[] result = new String[temp.size()];

		for (int i = 0; i < result.length; i++) {
			result[i] = (String) temp.elementAt(i);
		}
		return (result);
	}

	/**
	 * Checks to see if a String is a legal identifier.
	 * Uses java/c++ standards, execept it cannot start with
	 * an underscore.
	 *
	 * @param str The String to check.
	 * @return <code>true</code> if the String uses valid characters.
	 */
	public static boolean isLegalIdentifier(String str) {
		if (str == null)
			return (false);

		char c = str.charAt(0);
		if (!Character.isLetter(c)) {
			return (false);
		}
		int size = str.length();
		for (int i = 1; i < size; i++) {
			c = str.charAt(i);
			if (!(Character.isLetterOrDigit(c) || c == '_' || c == '-'))
				return (false);
		}
		return (true);
	}

	/**
	 * Checks if there are commas that aren't within quote marks.
	 * Thus, this following is a comma separated list,
	 * <pre>
	 *     HELLO, THERE, FINE
	 * </pre>
	 * But the following isn't
	 * <pre>
	 *   "hello, world"  "fine, thank you"
	 * </pre>
	 * since the commas are between quotes.
	 * 
	 * <em>Note:</em> This doesn't do checking for control characters,
	 *     so it won't work on data that contains them,
	 *     eg. <code>"Say \"Hello,\" World."</code>
	
	 * @param s The String to check.
	 * @return <code>true</code> if this looks like a comma seperated list.
	 * 
	 */
	public static boolean containsCommaSeparatedList(String s) {
		boolean inQuotes = false;
		for (int i = 0; i < s.length(); i++) {
			char c = s.charAt(i);
			if (c == '"')
				inQuotes = !inQuotes;
			else {
				if (!inQuotes) {
					if (c == ',')
						return (true);
				}
			}
		}
		return (false);
	}

	/**
	 * Will return true if the string passed to it is one of the defined
	 * relational operators. Right now those operators are <code>&lt;</code>,
	 * <code>&gt;</code>, <code>&lt;=</code>, <code>&gt;=</code>,
	 * <code>==</code>, and <code>!=</code>.
	 * @param rel_op the string that has to be tested if it is a rel op
	 * @return true if rel_op was a relational operator.
	 */
	public static boolean isRelationalOperator(String rel_op) {
		return (
			rel_op.equals("<")
				|| rel_op.equals(">")
				|| rel_op.equals("<=")
				|| rel_op.equals(">=")
				|| rel_op.equals("==")
				|| rel_op.equals("!="));
	}

	/** Equality relation */
	public static final int REL_EQ = 0;
	/** Less than relation */
	public static final int REL_LT = 1;
	/** Greater than relation */
	public static final int REL_GT = 2;
	/** Less than or equals relation */
	public static final int REL_LTEQ = 3;
	/** Greater than or equals relation */
	public static final int REL_GTEQ = 4;
	/** Inequality relation */
	public static final int REL_NEQ = 5;
	private static final String[] relOpMap =
		{ "==", "<", ">", "<=", ">=", "!=" };

	/**
	 * Converts the string, e.g. <q>==</q> or 
	 * <q>&lt;=</q>, to the constant. 
	 * @param rel_op the relational operator
	 * @return the static final int for the operator,
	 * e.g. {@link #REL_EQ}
	 */
	public static int getRelationalOperatorEnum(String rel_op) {
		if (rel_op.length() == 1) {
			if (rel_op.equals("<")) {
				return StringHelp.REL_LT;
			} else if (rel_op.equals(">")) {
				return StringHelp.REL_GT;
			}
		} else if (rel_op.length() == 2) {
			if (rel_op.equals("<=")) {
				return StringHelp.REL_LTEQ;
			} else if (rel_op.equals(">=")) {
				return StringHelp.REL_GTEQ;
			} else if (rel_op.equals("==")) {
				return StringHelp.REL_EQ;
			} else if (rel_op.equals("!=")) {
				return StringHelp.REL_NEQ;
			}
		}
		return -1;
	}

	/**
	 * Gets the string for the relational operator.
	 * @param rel_op_enum the constant int for the operator
	 * @return the corresponding operator string
	 */
	public static String getRelationalOperatorString(int rel_op_enum) {
		return relOpMap[rel_op_enum];
	}

	/**
	 * Will return true if the string passed to it is one of the defined
	 * logical operators. So far, these are <code>&&</code> and <code>||</code>.
	 * @param log_op the string that has to be tested if it is a log op
	 * @return true if log_op was a logical operator.
	 */
	public static boolean isLogicalOperator(String log_op) {
		return (log_op.equals("&") || log_op.equals("||"));
	}

	/**
	 * Will remove whitespace chars from the string.
	 * @param candidate String with whitespace characters.
	 * @return The same String as passed in only w/o the white spaces.
	 */
	public static String removeSpacesFrom(String candidate) {
		StringTokenizer tokenizer = new StringTokenizer(candidate);
		String return_value = "";
		while (tokenizer.hasMoreTokens())
			return_value = return_value + tokenizer.nextToken();
		return return_value;
	}

	/**
	 * Converts special characters to escpe key sequences.
	 * eg a new line becomes <code>\n</code>.
	 * 
	 * @param str The string containing control characters that
	 *            should be expanded
	 * @return The expanded String.
	 * @see #debackslashify(String str)
	 */
	public static String backslashify(String str) {
		StringBuffer buf = new StringBuffer(str.length());
		for (int i = 0; i < str.length(); i++) {
			switch (str.charAt(i)) {
				case '\\' :
					buf.append("\\\\");
					break;
				case '\n' :
					buf.append("\\n");
					break;
				case '"' :
					buf.append("\\\"");
					break;
				default :
					buf.append(str.charAt(i));
			}
		}
		return buf.toString();
	}
	
	/**
	 * Doubles every " mark in the string.
	 * @param str the string to encode
	 * @return the string, with each mark doubled
	 */
	public static String doublequoteEncode(String str) {
		StringBuffer buf = new StringBuffer(str.length());
		for (int i = 0; i < str.length(); i++) {
			if (str.charAt(i) == '"') {
				buf.append("\"\"");
			} else {
				buf.append(str.charAt(i));
			}
		}
		return buf.toString();
	}
	
	/**
	 * Decodes a comma seperated list of quoted strings.
	 * Quotes are encoded by doubling them.
	 * @param list
	 * @return
	 */
	public static Iterator decodeDoublequotedList(final String list) {
		return new Iterator() {
			String next = null;
			int cursor = 0;
			private void cacheNext() {
				StringBuffer sb = new StringBuffer();
				char next = list.charAt(cursor);
				if (next == '"') {
					// in a quoted thing
					while (cursor < list.length()) {
						next = list.charAt(cursor);
						if (next == '"') {
							cursor++;
							next = list.charAt(cursor);
							if (next == '"') {
								sb.append('"');
								cursor++;
								continue;
							} else {
								cursor += 2;
								break;
							}
						}
						sb.append(next);
						cursor++;
					}
				} else {
					// in a bare string
					while (cursor < list.length()) {
						next = list.charAt(cursor);
						if (next == ',') {
							cursor++;
							break;
						}
						sb.append(next);
						cursor++;
					}
				}
				this.next = sb.toString();
			}
			
			public boolean hasNext() {
				if (next == null) {
					cacheNext();
				}
				return cursor < list.length();
			}

			public Object next() {
				if (!hasNext()) {
					throw new IndexOutOfBoundsException();
				}
				String r = next;
				next = null;
				return r;
			}

			public void remove() {
				throw new UnsupportedOperationException();
			}
		};
	}

	/**
	 * Tokenizes like the bash command line.
	 * @param str the string to tokenize
	 * @return an iterator over all tokens in the string
	 */
	public static Iterator debackslashedTokenizer(String str) {
		return new DebackslashedIterator(str);
	}
	private static class DebackslashedIterator implements Iterator {
		private String content;
		private int cursor;

		/**
		 * 
		 * @param str the string to parse
		 */
		public DebackslashedIterator(String str) {
			content = str;
			cursor = 0;
			advancePastWS();
		}

		/**
		 * @see java.util.Iterator#hasNext()
		 */
		public boolean hasNext() {
			return cursor < content.length();
		}

		/**
		 * @see java.util.Iterator#next()
		 */
		public Object next() {
			if (!hasNext()) {
				throw new NoSuchElementException();
			}
			int start = cursor;
			char curr = content.charAt(cursor);
			int end = cursor + 1;
			if (curr == '"') {
				start = ++cursor;
				while (hasNext()) {
					curr = content.charAt(cursor);
					if (curr == '"') {
						break;
					} else if (curr == '\\') {
						cursor++;
					}
					cursor++;
				}
				end = cursor++;
			} else {
				boolean done = !hasNext();
				while (!done) {
					if (curr == '\\') {
						cursor++;
					}
					cursor++;
					done = !hasNext();
					if (!done) {
						curr = content.charAt(cursor);
						done = Character.isWhitespace(curr);
					}
				}
				end = cursor;
			}
			advancePastWS();
			String sub = content.substring(start, end);
			return debackslashify(sub);
		}

		/**
		 * Unsupported.
		 * @see java.util.Iterator#remove()
		 */
		public void remove() {
			throw new UnsupportedOperationException();
		}

		private void advancePastWS() {
			while (hasNext()) {
				char curr = content.charAt(cursor);
				if (Character.isWhitespace(curr)) {
					cursor++;
				} else {
					return;
				}
			}
		}
	}

	/**
	 * Returns the list as a comma sepatated list
	 * of quoted and escaped strings.
	 * @param list the list to print as a string
	 * @return a string of quoted, escaped, comma delimited items
	 */
	public static String getQuotedListString(List list) {
		if (list == null || list.size() == 0) {
			return "";
		}

		Iterator iter = list.iterator();
		StringBuffer buff =
			new StringBuffer("\"").append(
				backslashify(iter.next().toString())).append(
				"\"");
		while (iter.hasNext()) {
			buff.append(", \"").append(
				backslashify(iter.next().toString())).append(
				"\"");
		}
		return buff.toString();
	}

	/**
	 * Converts a comma seperated list of quoted strings 
	 * to a list of debackslashified strings.
	 * For example: <code>"one", "two", "three"</code> will
	 * convert to a list containing three strings, each
	 * stripped of quotes.
	 *
	 * @param str The string containing a list.
	 * @return A list of strings
	 * @see #debackslashify(String str)
	 * @throws BadDataException
	 */
	public static List getQuotedList(String str) throws BadDataException {
		LinkedList strings = new LinkedList();
		StringBuffer buff = new StringBuffer();
		boolean inside = false;
		for (int i = 0; i < str.length(); i++) {
			char c = str.charAt(i);
			if (!inside && c != '"') {
				if ((c != ' ') && (c != '\t') && (c != ',')) {
					throw new BadDataException(
						"Missing quotes in list: '" + c + "'");
				}
			} else {
				switch (c) {
					case '\\' :
						if (++i >= str.length())
							throw new BadDataException(
								"Error in escape sequence: " + str);
						switch (str.charAt(i)) {
							case 'n' :
								buff.append('\n');
								break;
							case '"' :
								buff.append('"');
								break;
							default :
								buff.append(c);
						}
						break;
					case '"' :
						if (inside == true) {
							strings.add(buff.toString());
							buff.setLength(0);
						}
						inside = !inside;
						break;
					default :
						buff.append(c);
				}
			}
		}
		return strings;
	}

	/**
	 * Converts escape sequences to their real characters,
	 * like new lines. For example, the string:
	 * <pre>
	 * I went to \"http:\\\\java.sun.com\\\",\nbut they didn't sell coffee.
	 * </pre>
	 * becomes:
	 * <pre>
	 * I went to "http:\\java.sun.com\",
	 * but they didn't sell coffee.
	 * </pre>
	 *
	 * @param str The string containing C-style control characters.
	 * @return The string with the control characters 
	 *    turned into actual characters.
	 * @throws IllegalArgumentException if the last character is a 
	 *    backslash (e.g. <code>"some\"</code>)
	 * @see #backslashify(String str)
	 */
	public static String debackslashify(String str)
		throws IllegalArgumentException {
		StringBuffer buff = new StringBuffer();
		for (int i = 0; i < str.length(); i++) {
			switch (str.charAt(i)) {
				case '\\' :
					if (++i >= str.length())
						throw new IllegalArgumentException(
							"Error in escape sequence: '" + str + "'");
					switch (str.charAt(i)) {
						case 'n' :
							buff.append('\n');
							break;
						case '"' :
							buff.append('"');
							break;
						default :
							buff.append(str.charAt(i));
					}
					break;
				default :
					buff.append(str.charAt(i));
			}
		}
		return buff.toString();
	}

	/**
	 * Convert the plain text string into something that
	 * is a valid HTML/XML text string (i.e. escaping angle brackets,
	 * etc.).
	 * I owe a lot to the apache project for their source code
	 * was an assistance. It is annoying that they don't have
	 * this as a public method somewhere, though.
	 * @param str the string to escape 
	 * @return the string, with some characters converted to XML character entity references
	 */
	public static String webify(String str) {
		StringBuffer buff = new StringBuffer();
		StringCharacterIterator iter = new StringCharacterIterator(str);

		for (char c = iter.first();
			c != CharacterIterator.DONE;
			c = iter.next()) {
			switch (c) {
				case '<' :
					buff.append("&lt;");
					break;

				case '>' :
					buff.append("&gt;");
					break;

				case '"' :
					buff.append("&quot;");
					break;

				case '\'' :
					buff.append("&apos;");
					break;

				case '&' :
					buff.append("&amp;");
					break;

				default :
					if ((c >= ' ' && c < 0xF7)
						|| c == '\n'
						|| c == '\r'
						|| c == '\t') {
						buff.append(c);
					} else {
						buff.append("&#x").append(
							Integer.toHexString(c)).append(
							';');
					}
			}
		}
		return buff.toString();
	}

	/**
	 * Generates an asterisk-surrounded banner containing the 
	 * given text. For example, <code>banner("Hello, world!",12)</code>
	 * generates:
	 * <pre>
	 * ***********
	 * *  Hello, *
	 * *  world! *
	 * ***********
	 * </pre>
	 * @param text
	 * @param width
	 * @return a banner describing the text, with a newline at the end
	 */
	public static String banner(String text, int width) {
		StringBuffer s = new StringBuffer();
		int innerWidth = width - 4;
		for (int i = 0; i < width; i++)
			s.append('*');
		s.append('\n');
		StringTokenizer st =
			new StringTokenizer(WordUtils.wrap(text, innerWidth), "\n\r\f");
		while (st.hasMoreTokens()) {
			s.append("* ");
			s.append(StringUtils.center(st.nextToken(), innerWidth));
			s.append(" *\n");
		}
		for (int i = 0; i < width; i++)
			s.append('*');
		s.append('\n');
		return s.toString();
	}

	/**
	 * Determines by file content if the data is in XML format.
	 * Specialized version for gtf data.
	 * It looks for the <?xml?> processing directive.
	 *
	 * @param configFileName The config file name. XML files only
	 *            have the data file, so if this is not null, this
	 *            method returns false.
	 * @param dataFileName The name of the data file, the file that
	 *            is checked for the processing directive
	 * @throws IOException if there was a problem with the data file.
	 * @return true if the data is xml. Does not validate, or anything.
	 */
	public static boolean isXMLFormat(
		String configFileName,
		String dataFileName)
		throws IOException {
		if (configFileName != null) {
			return false;
		} else {
			return isXMLFormat(dataFileName);
		}
	}

	/** 
	 * Checks to see if the file begins with an xml processing directive, eg
	 * <code>&lt;?xml?&gt;</code>. This method does not check to see that the 
	 * file is well-formed, or even if the processing directive is good, just that
	 * the first non-whitespace characters are "&lt;?xml".
	 *
	 * @param fileName The file to check for xml processing directive
	 * @throws IOException if there is an error while reading the file, eg FileNotFoundException
	 * @return <code>true</code> if the directive was found. 
	 */
	public static boolean isXMLFormat(String fileName) throws IOException {
		return isXMLFormat(new File(fileName));
	}

	/** 
	 * Checks to see if the file begins with an xml processing directive, eg
	 * <code>&lt;?xml?&gt;</code>. This method does not check to see that the 
	 * file is well-formed, or even if the processing directive is good, just that
	 * the first non-whitespace characters are "&lt;?xml".
	 *
	 * @param f The file to check for xml processing directive
	 * @throws IOException if there is an error while reading the file, eg FileNotFoundException
	 * @return <code>true</code> if the directive was found. 
	 */
	public static boolean isXMLFormat(File f) throws IOException {
		StreamTokenizer st = new StreamTokenizer(new FileReader(f));
		st.wordChars('<', '<');
		st.wordChars('>', '>');
		st.wordChars('?', '?');
		st.nextToken();
		return st.toString().startsWith("Token[<?xml");
	}
	
	/** 
	 * Checks to see if the stream begins with an xml processing directive, eg
	 * <code>&lt;?xml?&gt;</code>. This method does not check to see that the 
	 * stream is well-formed, or even if the processing directive is good, just that
	 * the first non-whitespace characters are "&lt;?xml".
	 *
	 * @param f The file to check for xml processing directive
	 * @throws IOException if there is an error while reading the file, eg FileNotFoundException
	 * @return <code>true</code> if the directive was found. 
	 */
	public static boolean isXMLFormat(InputStream f) throws IOException {
		final int LIMIT = 4024;
		f.mark(LIMIT);
		InputStreamReader isr = new InputStreamReader(f);
		char n;
		do {
			n = (char) isr.read();
		} while(Character.isWhitespace(n));
		boolean xml = (n == '<' && isr.read() == '?' && (char) isr.read() == 'x' && (char) isr.read() == 'm' && (char) isr.read() == 'l');
		f.reset();
		return xml;
	}

	final static String[] hex = {
	  "%00", "%01", "%02", "%03", "%04", "%05", "%06", "%07",
	  "%08", "%09", "%0a", "%0b", "%0c", "%0d", "%0e", "%0f",
	  "%10", "%11", "%12", "%13", "%14", "%15", "%16", "%17",
	  "%18", "%19", "%1a", "%1b", "%1c", "%1d", "%1e", "%1f",
	  "%20", "%21", "%22", "%23", "%24", "%25", "%26", "%27",
	  "%28", "%29", "%2a", "%2b", "%2c", "%2d", "%2e", "%2f",
	  "%30", "%31", "%32", "%33", "%34", "%35", "%36", "%37",
	  "%38", "%39", "%3a", "%3b", "%3c", "%3d", "%3e", "%3f",
	  "%40", "%41", "%42", "%43", "%44", "%45", "%46", "%47",
	  "%48", "%49", "%4a", "%4b", "%4c", "%4d", "%4e", "%4f",
	  "%50", "%51", "%52", "%53", "%54", "%55", "%56", "%57",
	  "%58", "%59", "%5a", "%5b", "%5c", "%5d", "%5e", "%5f",
	  "%60", "%61", "%62", "%63", "%64", "%65", "%66", "%67",
	  "%68", "%69", "%6a", "%6b", "%6c", "%6d", "%6e", "%6f",
	  "%70", "%71", "%72", "%73", "%74", "%75", "%76", "%77",
	  "%78", "%79", "%7a", "%7b", "%7c", "%7d", "%7e", "%7f",
	  "%80", "%81", "%82", "%83", "%84", "%85", "%86", "%87",
	  "%88", "%89", "%8a", "%8b", "%8c", "%8d", "%8e", "%8f",
	  "%90", "%91", "%92", "%93", "%94", "%95", "%96", "%97",
	  "%98", "%99", "%9a", "%9b", "%9c", "%9d", "%9e", "%9f",
	  "%a0", "%a1", "%a2", "%a3", "%a4", "%a5", "%a6", "%a7",
	  "%a8", "%a9", "%aa", "%ab", "%ac", "%ad", "%ae", "%af",
	  "%b0", "%b1", "%b2", "%b3", "%b4", "%b5", "%b6", "%b7",
	  "%b8", "%b9", "%ba", "%bb", "%bc", "%bd", "%be", "%bf",
	  "%c0", "%c1", "%c2", "%c3", "%c4", "%c5", "%c6", "%c7",
	  "%c8", "%c9", "%ca", "%cb", "%cc", "%cd", "%ce", "%cf",
	  "%d0", "%d1", "%d2", "%d3", "%d4", "%d5", "%d6", "%d7",
	  "%d8", "%d9", "%da", "%db", "%dc", "%dd", "%de", "%df",
	  "%e0", "%e1", "%e2", "%e3", "%e4", "%e5", "%e6", "%e7",
	  "%e8", "%e9", "%ea", "%eb", "%ec", "%ed", "%ee", "%ef",
	  "%f0", "%f1", "%f2", "%f3", "%f4", "%f5", "%f6", "%f7",
	  "%f8", "%f9", "%fa", "%fb", "%fc", "%fd", "%fe", "%ff"
	};
	
	/**
	 * Encode a string to the "x-www-form-urlencoded" form, enhanced
	 * with the UTF-8-in-URL proposal. This is modified from the
	 * w3c's code, at <a href="http://www.w3.org/International/O-URL-code.html"
	 * >http://www.w3.org/International/O-URL-code.html</a>, in that it 
	 * is more conservative in its encoding, including path seperators,
	 * tildes, parens, bangs and single quotes in its encoding.
	 * 
	 *
	 * <ul>
	 * <li><p>The ASCII characters 'a' through 'z', 'A' through 'Z',
	 *        and '0' through '9' remain the same.
	 *
	 * <li><p>The unreserved characters - _ . remain the same.
	 *
	 * <li><p>The space character ' ' is converted into a plus sign '+'.
	 *
	 * <li><p>All other ASCII characters are converted into the
	 *        3-character string "%xy", where xy is
	 *        the two-digit hexadecimal representation of the character
	 *        code
	 *
	 * <li><p>All non-ASCII characters are encoded in two steps: first
	 *        to a sequence of 2 or 3 bytes, using the UTF-8 algorithm;
	 *        secondly each of these bytes is encoded as "%xx".
	 * </ul>
	 *
	 * @param s The string to be encoded
	 * @return The encoded string
	 */
	public static String encodeAsAnAcceptableFileName(String s) {
		StringBuffer sbuf = new StringBuffer();
		int len = s.length();
		for (int i = 0; i < len; i++) {
			int ch = s.charAt(i);
			if ('A' <= ch && ch <= 'Z') { // 'A'..'Z'
				sbuf.append((char) ch);
			} else if ('a' <= ch && ch <= 'z') { // 'a'..'z'
				sbuf.append((char) ch);
			} else if ('0' <= ch && ch <= '9') { // '0'..'9'
				sbuf.append((char) ch);
			} else if (ch == ' ') { // space
				sbuf.append('+');
			} else if (ch == '-' || ch == '_' // unreserved
					   || ch == '.') {
				sbuf.append((char) ch);
			} else if (ch <= 0x007f) { // other ASCII
				sbuf.append(hex[ch]);
			} else if (ch <= 0x07FF) { // non-ASCII <= 0x7FF
				sbuf.append(hex[0xc0 | (ch >> 6)]);
				sbuf.append(hex[0x80 | (ch & 0x3F)]);
			} else { // 0x7FF < ch <= 0xFFFF
				sbuf.append(hex[0xe0 | (ch >> 12)]);
				sbuf.append(hex[0x80 | ((ch >> 6) & 0x3F)]);
				sbuf.append(hex[0x80 | (ch & 0x3F)]);
			}
		}
		return sbuf.toString();
	}

	/**
	 * Decodes a string encoded with {@link #encodeAsAnAcceptableFileName(String)}, 
	 * which is to say it decodes any URL encoded string.
	 * @param s an URL encoded string
	 * @return the decoded string
	 */
	public static String unescapeAnAcceptableFileName(String s) {
		StringBuffer sbuf = new StringBuffer();
		int l = s.length();
		int ch = -1;
		int b, sumb = 0;
		for (int i = 0, more = -1; i < l; i++) {
			/* Get next byte b from URL segment s */
			switch (ch = s.charAt(i)) {
				case '%' :
					ch = s.charAt(++i);
					int hb =
						(Character.isDigit((char) ch)
							? ch - '0'
							: 10 + Character.toLowerCase((char) ch) - 'a')
							& 0xF;
					ch = s.charAt(++i);
					int lb =
						(Character.isDigit((char) ch)
							? ch - '0'
							: 10 + Character.toLowerCase((char) ch) - 'a')
							& 0xF;
					b = (hb << 4) | lb;
					break;
				case '+' :
					b = ' ';
					break;
				default :
					b = ch;
			}
			/* Decode byte b as UTF-8, sumb collects incomplete chars */
			if ((b & 0xc0) == 0x80) { // 10xxxxxx (continuation byte)
				sumb = (sumb << 6) | (b & 0x3f); // Add 6 bits to sumb
				if (--more == 0)
					sbuf.append((char) sumb); // Add char to sbuf
			} else if ((b & 0x80) == 0x00) { // 0xxxxxxx (yields 7 bits)
				sbuf.append((char) b); // Store in sbuf
			} else if ((b & 0xe0) == 0xc0) { // 110xxxxx (yields 5 bits)
				sumb = b & 0x1f;
				more = 1; // Expect 1 more byte
			} else if ((b & 0xf0) == 0xe0) { // 1110xxxx (yields 4 bits)
				sumb = b & 0x0f;
				more = 2; // Expect 2 more bytes
			} else if ((b & 0xf8) == 0xf0) { // 11110xxx (yields 3 bits)
				sumb = b & 0x07;
				more = 3; // Expect 3 more bytes
			} else if ((b & 0xfc) == 0xf8) { // 111110xx (yields 2 bits)
				sumb = b & 0x03;
				more = 4; // Expect 4 more bytes
			} else /*if ((b & 0xfe) == 0xfc)*/ { // 1111110x (yields 1 bit)
				sumb = b & 0x01;
				more = 5; // Expect 5 more bytes
			}
			/* We don't test if the UTF-8 encoding is well-formed */
		}
		return sbuf.toString();
	}
}
