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
package edu.umd.cfar.lamp.viper.gui.remote;

import javax.swing.*;
import javax.swing.text.*;

import viper.api.time.*;
import edu.umd.cfar.lamp.apploader.misc.*;

/**
 */
public class JInstantField extends JTextField {
	private InstantFSM fsm = new InstantFSM();
	private Class unitPreference = Frame.class;
	private class InstantFSM extends StringParserFSM {
		private static final int START = 0;
		private static final int NUMBERS = 1;
		private static final int SPACE = 2;
		private static final int TYPE = 3;
		private int state = 0;
		private StringBuffer buf = new StringBuffer();
		public boolean pushDown(char c) {
			switch (state) {
				case START :
					if (Character.isDigit(c)) {
						buf.append(c);
						state = NUMBERS;
						return true;
					}
					break;
				case NUMBERS :
					if (Character.isDigit(c)) {
						buf.append(c);
						state = NUMBERS;
						return true;
					} else if (Character.isWhitespace(c)) {
						buf.append(" ");
						state = SPACE;
						return true;
					} else if (c == 'f' || c == 'n') {
						buf.append(" ").append(c);
						state = TYPE;
						return true;
					}
					break;
				case SPACE :
					if (c == 'f' || c == 'n') {
						buf.append(c);
						state = TYPE;
						return true;
					}
					break;
				case TYPE :
					break;
				default :
					assert false : "Unexpected state";
			}
			return false;
		}
		public void reset() {
			buf.setLength(0);
			state = START;
		}
		public String toString() {
			switch (state) {
				case START :
					return "";
				case NUMBERS :
					return buf.toString();
				case SPACE :
					return buf.toString()
							+ (unitPreference.equals(Frame.class) ? 'f' : 'n');
				case TYPE :
					return buf.toString();
			}
			return null;
		}
	}
	public FsmDocument getFSMDocument() {
		return (FsmDocument) getDocument();
	}
	public JInstantField() {
		this(null);
	}
	public JInstantField(Instant o) {
		super(new FsmDocument(), null, 0);
		getFSMDocument().setFsm(fsm);
	}
	/**
	 * @return Class
	 */
	public Class getUnitPreference() {
		return unitPreference;
	}
	
	/**
	 * Sets the preferred unit of time.
	 * 
	 * @param unitPreference
	 *            The unitPreference to set
	 */
	public void setUnitPreference(Class unitPreference) {
		this.unitPreference = unitPreference;
	}
	
	private String inst2str(Instant i) {
		if (i == null) {
			return "";
		} else {
			return i.toString() + ' ' + ((i instanceof Frame) ? 'f' : 'n');
		}
	}
	private Instant str2inst(String s) {
		if (s == null || s.length() == 0) {
			return null;
		}
		int x = s.indexOf(" ");
		boolean isFrame = Frame.class.equals(unitPreference);
		if (x >= 0) {
			String t = s.substring(x+1);
			if ("f".equals(t)) {
				isFrame = true;
			} else if ("n".equals(t)) {
				isFrame = false;
			} else {
				throw new IllegalArgumentException("Not a valid instant: '" + s + "'");
			}
			s = s.substring(0, x);
		}
		if (isFrame) {
			return new Frame(Integer.parseInt(s));
		} else {
			return new Time(Long.parseLong(s));
		}
	}
	
	public void setValue(Instant value) {
		try {
			getFSMDocument().remove(0, getFSMDocument().getLength());
			getFSMDocument().insertString(0, inst2str(value), null);
		} catch (BadLocationException e) {
			e.printStackTrace();
		}
		
	}

	public Instant getValue() {
		return str2inst(fsm.toString());
	}
}