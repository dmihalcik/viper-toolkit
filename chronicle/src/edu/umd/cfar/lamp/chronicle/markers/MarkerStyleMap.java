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

package edu.umd.cfar.lamp.chronicle.markers;

import java.util.*;

/**
 * Simple MarkerStyles class that maps labels to styles by 
 * matching the names directly.
 */
public class MarkerStyleMap implements MarkerStyles {
	private MarkerStyles.StyleForLabel defaultStyle;
	private Map styleMap;
	
	/**
	 * Creates a new, empty map.
	 */
	public MarkerStyleMap() {
		styleMap = new HashMap();
	}

	/**
	 * Adds the given style for the specified
	 * marker label
	 * @param label the label for nodes to style
	 * @param sfl the style
	 */
	public void putStyleForLabel(String label, MarkerStyles.StyleForLabel sfl) {
		styleMap.put(label, sfl);
	}
	
	/**
	 * @inheritDoc
	 */
	public MarkerStyles.StyleForLabel getStyleForLabel(String label) {
		if (styleMap.containsKey(label)) {
			return (MarkerStyles.StyleForLabel) styleMap.get(label);
		} else {
			return defaultStyle;
		}
	}
	
	/**
	 * Removes the given style. This is necessary to call
	 * when you are generating markers with new names, or the
	 * style nodes will never be collected.
	 * @param label the label to remove
	 * @return the removed style, or <code>null</code>
	 * if none was found for the label
	 */
	public MarkerStyles.StyleForLabel removeStyleForLabel(String label) {
		return (MarkerStyles.StyleForLabel) styleMap.remove(label);
	}


	/**
	 * Gets the default marker style type
	 * @return StyleForLabel the default style
	 */
	public MarkerStyles.StyleForLabel getDefaultStyle() {
		return defaultStyle;
	}

	/**
	 * Sets the default style for marker nodes.
	 * @param defaultStyle the new default style
	 */
	public void setDefaultStyle(MarkerStyles.StyleForLabel defaultStyle) {
		this.defaultStyle = defaultStyle;
	}
}
