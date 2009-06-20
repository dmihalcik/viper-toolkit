package edu.umd.cfar.lamp.viper.gui.chronology;

import java.awt.*;
import java.util.*;

import edu.umd.cfar.lamp.chronicle.markers.*;
import edu.umd.cs.piccolo.*;

/**
 * @author davidm
 *
 * To change the template for this generated type comment go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
public class Markers {
	private Alpharator alpha = new Alpharator();
	private ViperChronicleView chronicle;

	private final class MarkerStyle implements MarkerStyles.StyleForLabel {
		private String label;
		private int level;
		public MarkerStyle(String label, int level) {
			this.label = label;
			this.level = level;
		}
		public Paint getLineStyle() {
			return Color.black;
		}
		public PNode createHeaderNode() {
			return new CircleSignHeader(label, level/100.0);
		}
		public PNode createFooterNode() {
			return null;
		}
		public Paint getFillStyle() {
			return Color.white;
		}
	}
	private static class Alpharator implements Iterator {
		private static final char[] latinLetters = "abcdefghijklmnopqrstuvwxyz".toCharArray();
		private static final char[] greekLetters = 
			{
				'\u03b1',
				'\u03b2',
				'\u03b3',
				'\u03b4',
				'\u03b5',
				'\u03b6',
				'\u03b7',
				'\u03b8',
				'\u03b9',
				'\u03ba',
				'\u03bb',
				'\u03bc',
				'\u03bd',
				'\u03be',
				'\u03bf',
				'\u03c0',
				'\u03c1',
				'\u03c2',
				'\u03c3',
				'\u03c4',
				'\u03c5',
				'\u03c6',
				'\u03c7',
				'\u03c8',
				'\u03c9' };

		public static char[] letters = latinLetters;
		public static void setUsingGreek(boolean greek) {
			letters = greek ? greekLetters : latinLetters;
		}

		public int offset;
		public int level;

		public Alpharator() {
			reset();
		}

		public void reset() {
			this.offset = 0;
			this.level = 1;
		}

		public boolean hasNext() {
			return true;
		}

		public Object next() {
			StringBuffer sb = new StringBuffer();
			for (int i = 0; i < level; i++) {
				sb.append(letters[offset]);
			}
			offset++;
			if (offset >= letters.length) {
				offset = 0;
				level++;
			}
			return sb.toString();
		}

		public void remove() {
			throw new UnsupportedOperationException();
		}
	}

	public void addMarker() {
		alpha.reset();
		ChronicleMarkerModel m = chronicle.getMarkerModel();
		String newLabel;
		do {
			newLabel = (String) alpha.next();
		} while (m.getMarkersWithLabel(newLabel).hasNext());
		MarkerStyle sfl = new MarkerStyle(newLabel, 20);
		((MarkerStyleMap) chronicle.getMarkersNode().getStyles()).putStyleForLabel(newLabel, sfl);
		ChronicleMarker cm = m.createMarker();
		cm.setWhen(chronicle.getMediator().getMajorMoment());
		cm.setLabel(newLabel);
	}
	
	/**
	 * @return
	 */
	public ViperChronicleView getChronicle() {
		return chronicle;
	}

	/**
	 * @param view
	 */
	public void setChronicle(ViperChronicleView view) {
		chronicle = view;
	}

	public ChronicleMarkerModel getRelatedMarkerModel() {
		if (chronicle == null) {
			return null;
		}
		return chronicle.getMarkerModel();
	}
}
