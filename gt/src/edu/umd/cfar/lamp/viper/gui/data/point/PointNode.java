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

package edu.umd.cfar.lamp.viper.gui.data.point;

import java.awt.*;
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
 */
public class PointNode extends AttributablePPathAdapter implements Attributable {
	private static Logger logger = Logger
			.getLogger("edu.umd.cfar.lamp.viper.gui.canvas.datatypes");

	Pnt localCopy;

	Point2D center;

	Point2D[] horizPts = new Point2D[2];

	Point2D[] vertPts = new Point2D[2];

	PPath horizLine = new PPath();

	PPath vertLine = new PPath();

	/**
	 *  
	 */
	public PointNode(ViperViewMediator mediator) {
		super(mediator);
		center = new Point2D.Double();

		addChild(horizLine);
		addChild(vertLine);
	}

	public Point2D getPoint() {
		return center;
	}

	public void setPoint(double widthIn, double heightIn) {
		center.setLocation(widthIn, heightIn);
		updatePoint();
	}

	public void setPoint(Point2D pt) {
		center.setLocation(pt);
		updatePoint();
	}

	private void setPoint(Pnt pnt) {
		assert pnt != null;
		logger.fine("==== Init PointNode Pnt: " + pnt);

		// Put enough information so obox can be updated
		Rational x = pnt.getX();
		Rational y = pnt.getY();
		center.setLocation(x.doubleValue(), y.doubleValue());
		// Now that enough info is available, update the point
		updatePoint();

		localCopy = makeCopy(pnt);
	}

	public static final int CROSSHAIR_LENGTH = 4;

	public void updatePoint() {
		double x = center.getX();
		double y = center.getY();
		horizPts[0] = new Point2D.Double(x - CROSSHAIR_LENGTH, y);
		horizPts[1] = new Point2D.Double(x + CROSSHAIR_LENGTH, y);
		vertPts[0] = new Point2D.Double(x, y - CROSSHAIR_LENGTH);
		vertPts[1] = new Point2D.Double(x, y + CROSSHAIR_LENGTH);
		horizLine.setPathToPolyline(horizPts);
		vertLine.setPathToPolyline(vertPts);
	}

	public void setAttribute(Attribute attr) {
		this.attr = attr;
		Instant now = getInstant();
		// Get point corresponding to current frame
		Pnt pnt = (Pnt) attr.getAttrValueAtInstant(now);
		// Extract information about oriented box for local use
		if (pnt != null)
			setPoint(pnt);
	}

	public Point2D[] getHorizPts() {
		return horizPts;
	}

	public Point2D[] getVertPts() {
		return vertPts;
	}

	public Object getUpdatedAttribute() {
		Rational ratX = new Rational((long) center.getX());
		Rational ratY = new Rational((long) center.getY());

		Pnt pnt = new Pnt(ratX, ratY);
		return pnt;
	}

	private Pnt makeCopy(Pnt pnt) {
		return new Pnt(pnt.getX(), pnt.getY());
	}

	public void resetStyle() {
		Stroke aStroke = getDisplayProperties().getStroke();
		super.setStroke(aStroke);
		horizLine.setStroke(aStroke);
		vertLine.setStroke(aStroke);

		Paint aPaint = getDisplayProperties().getStrokePaint();
		super.setStrokePaint(aPaint);
		horizLine.setStrokePaint(aPaint);
		vertLine.setStrokePaint(aPaint);
	}
}

