/*
 * Created on Feb 5, 2004
 *
 * To change the template for this generated file go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
package edu.umd.cfar.lamp.chronicle.markers;

import java.awt.*;

import edu.umd.cs.piccolo.*;

/**
 * A default style type for markers, this just
 * has a line and fill style associated with it.
 */
public class DefaultStyleForLabel implements MarkerStyles.StyleForLabel {
	private Paint lineStyle;
	private Paint fillStyle;
	
	/**
	 * Construct a new style from the given line color 
	 * and fill color.
	 * @param line the line color
	 * @param fill the fill color
	 */
	public DefaultStyleForLabel(Paint line, Paint fill) {
		this.lineStyle = line;
		this.fillStyle = fill;
	}

	/**
	 * Gets the default fill color.
	 * @return the fill paint
	 */
	public Paint getFillStyle() {
		return fillStyle;
	}

	/**
	 * Gets the default line color.
	 * @return the line paint
	 */
	public Paint getLineStyle() {
		return lineStyle;
	}

	/**
	 * Creaate a marker header node of the current style.
	 * @return a header node with the given style
	 */
	public PNode createHeaderNode() {
		return new FrameFlagHeader();
	}

	/**
	 * Creaate a marker footer node of this style.
	 * @return a footer node with the given style
	 */
	public PNode createFooterNode() {
		return null;
	}

}