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


package edu.umd.cfar.lamp.viper.gui.chronology;

import java.awt.*;

import viper.api.time.*;
import edu.umd.cfar.lamp.viper.util.*;
import edu.umd.cs.piccolo.*;

/**
 */
public class VDescriptorTimeLineRenderer extends
		AbstractAttributeSegmentRenderer {
	private double endpointRadius;
	private Paint color;
	
	/**
	 * Creates a new descriptor with the default
	 * endpoint radius of 4 and the default color of black.
	 *
	 */
	public VDescriptorTimeLineRenderer() {
		this(4, Color.black);
	}
	/**
	 * @param endpointRadius
	 * @param color
	 */
	public VDescriptorTimeLineRenderer(double endpointRadius, Paint color) {
		super();
		this.endpointRadius = endpointRadius;
		this.color = color;
	}
	
	public PNode makeSegment(Interval i, double width, double height) {
		return new TQESegment(
			endpointRadius,
			color,
			(InstantInterval) i);
	}
	
	public PNode makeSegment(Interval i, double width, double height, InstantRange interpRange){
		return new TQESegment(
				endpointRadius,
				color,
				(InstantInterval) i,
				interpRange);			
	}
	public Paint getColor() {
		return color;
	}
	public void setColor(Paint color) {
		this.color = color;
	}
	public double getEndpointRadius() {
		return endpointRadius;
	}
	public void setEndpointRadius(double endpointRadius) {
		this.endpointRadius = endpointRadius;
	}
}
