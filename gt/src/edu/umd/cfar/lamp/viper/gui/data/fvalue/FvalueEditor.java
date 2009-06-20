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

package edu.umd.cfar.lamp.viper.gui.data.fvalue;

import edu.umd.cfar.lamp.apploader.misc.*;
import edu.umd.cfar.lamp.viper.gui.data.*;

/**
 * @author davidm@cfar.umd.edu
 * @since Jul 8, 2003
 */
public class FvalueEditor extends ViperDataFsmTextEditor {
	public FvalueEditor() {
		super(new DoubleFSM());
	}

	public Object parse(String t) {
		if (t == null || "".equals(t)) {
			return null;
		} else {
			try {
				return Double.valueOf(t);
			} catch (NumberFormatException nfx) {
				nfx.printStackTrace();
				return null;
			}
		}
	}

	private static class DoubleFSM extends StringParserFSM {
		private StringBuffer sb;
		private int state;
		private boolean hasMantissa;
		public DoubleFSM() {
			reset();
		}
		
		private static final int START = 0;
		private static final int BEFORE_DOT = 1;
		private static final int AFTER_DOT = 2;
		private static final int AFTER_EXP = 3;
		private static final int IN_EXP = 4;

		public boolean pushDown(char c) {
			switch (state) {
				case START :
					if ('.' == c) {
						sb.append(c);
						state = AFTER_DOT;
						return true;
					} else if (Character.isDigit(c)) {
						sb.append(c);
						state = BEFORE_DOT;
						hasMantissa = true;
						return true;
					} else if (c == '+' || c == '-') {
						sb.append(c);
						state = BEFORE_DOT;
						return true;
					} // Need a mantissa for an exponent
					break;
				case BEFORE_DOT :
					if ('.' == c) {
						sb.append(c);
						state = AFTER_DOT;
						return true;
					} else if (Character.isDigit(c)) {
						sb.append(c);
						hasMantissa = true;
						return true;
					} else if (c == 'e' || c == 'E' && hasMantissa) {
						sb.append('E');
						state = AFTER_EXP;
						return true;
					}
					break;
				case AFTER_DOT :
					if (Character.isDigit(c)) {
						sb.append(c);
						hasMantissa = true;
						return true;
					} else if (c == 'e' || c == 'E' && hasMantissa) {
						sb.append('E');
						state = AFTER_EXP;
						return true;
					}
					break;
				case AFTER_EXP :
					if (c == '+' || c == '-' || Character.isDigit(c)) {
						sb.append(c);
						state = IN_EXP;
						return true;
					}
					break; 
				case IN_EXP :
					if (Character.isDigit(c)) {
						sb.append(c);
						return true;
					}
					break;
				default :
					return false;
			}
			return false;
		}
		
		public void reset() {
			sb = new StringBuffer();
			state = START;
			hasMantissa = false;
		}
		
		public String toString() {
			return sb.toString();
		}
		
		public String getValidString() {
			if (!hasMantissa || sb.length() == 0) {
				return "";
			}
			if (state == AFTER_EXP && 'E' == sb.charAt(sb.length()-1)) {
				return sb.substring(0, sb.length()-1);
			}
			return sb.toString();
		}

	}
}
