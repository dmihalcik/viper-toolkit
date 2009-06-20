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

import java.awt.*;
import java.io.*;

/**
 */
public class BasicShapeDisplayProperties implements
		ShapeDisplayProperties, Serializable {
	/**
	 * 1
	 */
	private static final long serialVersionUID = 1L;
	private Paint strokePaint;
	private Stroke stroke;
	private Paint paint;
	
	/**
	 * @param strokePaint
	 * @param stroke
	 * @param paint
	 */
	public BasicShapeDisplayProperties(Paint strokePaint, Stroke stroke,
			Paint paint) {
		super();
		this.strokePaint = strokePaint;
		this.stroke = stroke;
		this.paint = paint;
	}
	
	/**
	 * Sets the paint to none
	 * @param strokePaint
	 * @param stroke
	 */
	public BasicShapeDisplayProperties(Paint strokePaint, Stroke stroke) {
		this(strokePaint, stroke, ColorUtilities.translucent);
	}

	/**
	 * Simple 1f black line.
	 */
	public BasicShapeDisplayProperties() {
		this(Color.black, new BasicStroke(1), ColorUtilities.translucent);
	}
	
	public Paint getPaint() {
		return paint;
	}
	public Stroke getStroke() {
		return stroke;
	}
	public Paint getStrokePaint() {
		return strokePaint;
	}
}
