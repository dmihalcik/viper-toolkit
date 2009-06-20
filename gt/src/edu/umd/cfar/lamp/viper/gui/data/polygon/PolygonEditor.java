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

package edu.umd.cfar.lamp.viper.gui.data.polygon;

import java.util.*;

import edu.umd.cfar.lamp.apploader.misc.*;
import edu.umd.cfar.lamp.viper.geometry.*;
import edu.umd.cfar.lamp.viper.gui.data.*;

/**
 * JTable cell editor for viper's <code>polygon</code> 
 * data type.
 * @author davidm@cfar.umd.edu
 * @since Jul 8, 2003
 */
public class PolygonEditor extends ViperDataFsmTextEditor {
	public PolygonEditor() {
		super(new PolygonFSM());
	}

	public Object parse(String t) {
		if (t == null || "".equals(t)) {
			return null;
		} else {
			try {
				return Polygon.valueOf(t);
			} catch (NumberFormatException nfx) {
				nfx.printStackTrace();
				return null;
			}
		}
	}
	

	private static class PolygonFSM extends StringParserFSM {
		private StringBuffer sb;
		private StringBuffer csb;
		private int state;
		private List points;
		public PolygonFSM() {
			reset();
		}
		
		private static final int START            = 0;
		private static final int AFTER_LEFT_PAREN = 1;
		private static final int IN_X             = 2;
		private static final int AFTER_X          = 3;
		private static final int IN_Y             = 4;

		public boolean pushDown(char c) {
			switch (state) {
				case START :
					if ('(' == c) {
						state = AFTER_LEFT_PAREN;
						sb.append(c);
						return true;
					} else if (Character.isDigit(c)) {
						state = IN_X;
						csb.append(c);
						sb.append('(').append(c);
						return true;
					}
					break;
				case AFTER_LEFT_PAREN :
					if (Character.isDigit(c)) {
						csb.append(c);
						sb.append(c);
						state = IN_X;
						return true;
					}
					break;
				case IN_X :
					if (Character.isWhitespace(c)) {
						csb.append(' ');
						sb.append(' ');
						state = AFTER_X;
						return true;
					} else if (Character.isDigit(c)) {
						csb.append(c);
						sb.append(c);
						return true;
					}
					break;
				case AFTER_X :
					if (Character.isDigit(c)) {
						csb.append(c);
						sb.append(c);
						state = IN_Y;
						return true;
					}
					break;
				case IN_Y :
					if (Character.isWhitespace(c) || c == ')') {
						sb.append(')');
						points.add(Pnt.valueOf(csb.toString()));
						csb.delete(0, csb.length());
						state = START;
						return true;
					} else if (Character.isDigit(c)) {
						csb.append(c);
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
			csb = new StringBuffer();
			if (points == null) {
				points = new LinkedList();
			} else {
				points.clear();
			}
			state = START;
		}
		
		public String toString() {
			return sb.toString();
		}
		
		public String getValidString() {
			if (points.size() > 1) {
				Polygon p = new Polygon();
				Iterator ptIter = points.iterator();
				while (ptIter.hasNext()) {
					p.addVertex((Pnt) ptIter.next());
				}
				if (state == IN_Y) {
					p.addVertex(Pnt.valueOf(csb.toString()));
				}
				return p.toString();
			}
			return null;
		}
	}
}
