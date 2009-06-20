/*
 * Created on Jan 20, 2004
 *
 * To change the template for this generated file go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
package edu.umd.cfar.lamp.viper.gui.data.circle;

import java.awt.geom.*;
import java.util.logging.*;

import viper.api.*;
import viper.api.time.*;
import edu.umd.cfar.lamp.viper.geometry.*;
import edu.umd.cfar.lamp.viper.gui.canvas.*;
import edu.umd.cfar.lamp.viper.gui.canvas.datatypes.*;
import edu.umd.cfar.lamp.viper.gui.core.*;
import edu.umd.cs.piccolo.nodes.*;

/**
 * @author clin
 * 
 * To change the template for this generated type comment go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */

public class CircleNode extends AttributablePPathAdapter implements Attributable {
	private static Logger logger = Logger
			.getLogger("edu.umd.cfar.lamp.viper.gui.canvas.datatypes");

	Circle circ;

	PPath radialLine = new PPath();

	Point2D[] radialPts = new Point2D[2];

	PPath highlightCircle = new PPath();

	/**
	 *  
	 */
	public CircleNode(ViperViewMediator mediator) {
		super(mediator);
		circ = new Circle();

		addChild(radialLine);
		addChild(highlightCircle);
		nonCenter = circ.getCenter().pointValue();

		resetStyle();
	}

	/**
	 * Updates the strokes to the latest version of the display property 
	 * decorators.
	 */
	protected void resetStyle() {
		highlightCircle.setStroke(getHighlightDisplayProperties().getStroke());
		highlightCircle.setStrokePaint(getHighlightDisplayProperties()
				.getStrokePaint());
		
		this.setStroke(getDisplayProperties().getStroke());
		this.setStrokePaint(getDisplayProperties().getStrokePaint());
	}

	private void setPath(Circle circle) {
		if (circle == null) {
			logger.warning("CircleNode: Uhoh");
		}
		logger.fine("==== Init CircleNode: " + circle);

		circ = new Circle(circle);
		// Now that enough info is available, update the obox and its handle
		rehighlight();
	}

	boolean showRadius = false;

	boolean showBoldCircle = false;

	Point2D nonCenter;

	public void boldCircle(boolean b) {
		showBoldCircle = b;
	}

	public void bold() {
		showBoldCircle = true;
		rehighlight();
	}

	public void unbold() {
		showRadius = false;
		showBoldCircle = false;
		rehighlight();
	}

	private void rehighlight() {
		int x = circ.getCenter().pointValue().x;
		int y = circ.getCenter().pointValue().y;
		int radius = circ.getRadius();
		int diameter = 2 * radius;
		setPathToEllipse(x - radius, y - radius, diameter, diameter);
		if (showRadius) {
			radialPts[0] = circ.getCenter().pointValue();
			radialPts[1] = nonCenter;
		} else // Show center dot
		{
			radialPts[0] = radialPts[1] = circ.getCenter().pointValue();
		}
		radialLine.setPathToPolyline(radialPts);

		// Highlight circle
		if (showBoldCircle) {
			highlightCircle.setPathToEllipse(x - radius, y - radius, diameter,
					diameter);
		} else {
			Point2D[] blank = new Point2D[1];
			blank[0] = new Point2D.Double();
			highlightCircle.setPathToPolyline(blank);
		}
	}

	private void clear() {
		Point2D[] blank = new Point2D[1];
		blank[0] = new Point2D.Double();
		highlightCircle.setPathToPolyline(blank);
		radialLine.setPathToPolyline(blank);
	}

	public void showRadius(boolean show) {
		showRadius = show;
		rehighlight();
	}

	public int getRadius() {
		return circ.getRadius();
	}

	public Point2D getCenter() {
		return circ.getCenter().point2DDoubleValue();
	}

	public void setCenter(Point2D center) {
		circ = new Circle((int) center.getX(), (int) center.getY(), circ
				.getRadius());
		rehighlight();
	}

	public void setRadius(int dist) {
		circ = new Circle(circ.getCenter().pointValue().x, circ.getCenter()
				.pointValue().y, dist);
		rehighlight();
	}

	public void updateRadialLine(Point2D nonCenter) {
		this.nonCenter = nonCenter;
		rehighlight();
	}

	public void setAttribute(Attribute attr) {
		this.attr = attr;
		Instant now = getInstant();
		// Get the oriented box corresponding to current frame
		Circle circle = (Circle) attr.getAttrValueAtInstant(now);
		// Extract information about circle for local use
		if (circle != null)
			setPath(circle);
	}

	public Object getUpdatedAttribute() {
		Circle newCircle = new Circle(circ);
		return newCircle;
	}

	private Polygon makeCopy(Polygon box) {
		return new Polygon(box);
	}
}