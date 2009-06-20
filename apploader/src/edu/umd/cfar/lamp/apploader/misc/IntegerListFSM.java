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
 * @author davidm@cfar.umd.edu
 * @since Jul 10, 2003
 */
public class IntegerListFSM extends StringParserFSM {
	// The semi-valid input so far
	private StringBuffer sb;
	
	// the index of the current number
	private int onNumber;
	
	private static interface STATE {
		/**
		 * The state while inside typing a number
		 */
		int DEF = 0;
		
		/**
		 * indicates a new number should begin
		 */
		int AFTER_SPACE = 1;
		
		/**
		 * a negative sign was encountered
		 */
		int AFTER_NEGATIVE_SIGN = 2;
	}
	private int state;

	// required integers in the list
	private int goal;
	public IntegerListFSM(int goal) {
		this.goal = goal;
		reset();
	}
		
	public boolean pushDown(char c) {
		switch (state) {
			case STATE.DEF:
				if (Character.isDigit(c)) {
					sb.append(c);
					// state stays in def
					return true;
				} else if (onNumber < goal && c==' ') {
					sb.append(' ');
					state = STATE.AFTER_SPACE;
					return true;
				}
				return false;
			case STATE.AFTER_SPACE:
				if (c == '-') {
					sb.append(c);
					onNumber++;
					state = STATE.AFTER_NEGATIVE_SIGN;
					return true;
				}
				if (Character.isDigit(c)) {
					sb.append(c);
					onNumber++;
					state = STATE.DEF;
					return true;
				}
				// keep waiting for '-' or digit
				return false;
			case STATE.AFTER_NEGATIVE_SIGN:
				if (Character.isDigit(c)) {
					state = STATE.DEF;
					sb.append(c);
					return true;
				}
				return false;
			default:
				assert false: "Invalid FSM state: " + state;
				return false;
		}
	}

	public void reset() {
		onNumber = 0;
		state = STATE.AFTER_SPACE;
		sb = new StringBuffer();
	}
		
	public String toString() {
		return sb.toString();
	}
		
		
	public String getValidString() {
		StringBuffer nsb = new StringBuffer(sb.toString());
		if (nsb.length() == 0) {
			return "";
		}
		int numCount = onNumber;
		if (onNumber < goal) {
			// Get into default-state, the goal state
			switch (state) {
			case STATE.AFTER_SPACE:
				numCount++;
			case STATE.AFTER_NEGATIVE_SIGN:
				nsb.append("0");
				break;
			}
		}
		// bump up the onNumber count to meet the goal
		while (numCount < goal) {
			nsb.append(" 0");
			numCount++;
		}
		return nsb.toString();
	}
	
	public boolean isFinished () {
		return onNumber == goal;
	}
}
