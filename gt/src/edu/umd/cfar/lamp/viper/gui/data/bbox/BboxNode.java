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

package edu.umd.cfar.lamp.viper.gui.data.bbox;

import java.awt.geom.*;
import java.util.logging.*;

import edu.umd.cfar.lamp.viper.geometry.*;
import edu.umd.cfar.lamp.viper.gui.canvas.*;
import edu.umd.cfar.lamp.viper.gui.canvas.datatypes.*;
import edu.umd.cfar.lamp.viper.gui.core.*;
import edu.umd.cfar.lamp.viper.gui.data.obox.*;
import edu.umd.cs.piccolo.nodes.*;

/**
 * Visual representation of aligned bounding boxes
 * 
 * @author clin
 */
public class BboxNode extends PBoxNode implements Attributable {
	private static Logger logger = Logger
			.getLogger("edu.umd.cfar.lamp.viper.gui.canvas.datatypes");

	private BoundingBox localCopy;

	private Point2D[] bboxPts;

	private double bboxWidth;

	private double bboxHeight;

	// For highlighting
	private PPath highlightLine = new PPath();

	private PPath highlightCircle = new PPath();

	private PPath highlightCenter = new PPath();

	// For highlighting interior
	private PPath interior = new PPath();

	/**
	 *  
	 */
	public BboxNode(ViperViewMediator mediator) {
		super(mediator);
		bboxPts = new Point2D[5];

		addChild(highlightLine);
		addChild(highlightCircle);
		addChild(highlightCenter);
		
		resetStyle();
	}

	/** @inheritDoc */
	protected void resetStyle() {
		highlightLine.setStroke(getHighlightDisplayProperties().getStroke());
		highlightCircle.setStroke(getHighlightDisplayProperties().getStroke());
		highlightCenter.setStroke(getHighlightDisplayProperties().getStroke());

		highlightLine.setStrokePaint(getHighlightDisplayProperties().getStrokePaint());
		highlightCircle.setStrokePaint(getHighlightDisplayProperties().getStrokePaint());
		highlightCenter.setStrokePaint(getHighlightDisplayProperties().getStrokePaint());
		
		setStroke(getDisplayProperties().getStroke());
		setStrokePaint(getDisplayProperties().getStrokePaint());
	}

	public Point2D[] getBoxPts() {
		return bboxPts;
	}

	public Point2D getUpperLeftPt() {
		return bboxPts[0];
	}

	/**
	 * Can't use getWidth() since PNode already defines it
	 * 
	 * @return the width of bbox
	 */
	public double getBoxWidth() {
		return bboxWidth;
	}

	/**
	 * Can't use getWidth() since PNode already defines it
	 * 
	 * @return the width of bbox
	 */
	public double getBoxHeight() {
		return bboxHeight;
	}

	public void setWidthAndHeight(double widthIn, double heightIn) {
		bboxWidth = widthIn;
		bboxHeight = heightIn;
		updateBbox();
	}

	public void setBbox(double topLeftX, double topLeftY, double widthIn,
			double heightIn) {
		bboxPts[0].setLocation(topLeftX, topLeftY);
		bboxWidth = widthIn;
		bboxHeight = heightIn;
		updateBbox();
	}

	public void setBbox(Point2D topLeft, double widthIn, double heightIn) {
		bboxPts[0].setLocation(topLeft);
		bboxWidth = widthIn;
		bboxHeight = heightIn;
		updateBbox();
	}

	public void setUpperLeft(Point2D topLeft) {
		bboxPts[0].setLocation(topLeft);
		updateBbox();
	}

	public void setUpperLeft(double x, double y) {
		bboxPts[0].setLocation(x, y);
		updateBbox();
	}

	public void setPath(BoxInformation box) {
		if (box == null) {
			logger.warning("Uhoh");
		}
		logger.fine("==== Init BboxNode BoundingBox: " + box);

		bboxWidth = box.getWidth();
		bboxHeight = box.getHeight();

		// Put enough information so obox can be updated
		double x = box.getX();
		double y = box.getY();

		bboxPts[0] = new Point2D.Double(x, y);

		// Now that enough info is available, update the obox and its handle
		updateBbox();

		localCopy = (BoundingBox) box.clone();
	}

	public void updateBbox() {
		OboxRectangle r = new OboxRectangle((int) bboxPts[0].getX(),
				(int) bboxPts[0].getY(), (int) bboxWidth, (int) bboxHeight, 0);

		bboxPts[0] = r.p[0];
		bboxPts[1] = r.p[1];
		bboxPts[2] = r.p[2];
		bboxPts[3] = r.p[3];
		bboxPts[4] = r.p[0];
		setPathToPolyline(bboxPts);
		rehighlight();
	}

	public Object getUpdatedAttribute() {

		BoundingBox boundBox = new BoundingBox((int) bboxPts[0].getX(),
				(int) bboxPts[0].getY(), (int) bboxWidth, (int) bboxHeight);
		return boundBox;
	}

	private BoundingBox makeCopy(BoundingBox box) {
		return new BoundingBox(box.getX(), box.getY(), box.getWidth(), box
				.getHeight());
	}

	int cornerRadius = 5;

	public void setCornerRadius(int val) {
		cornerRadius = val;
	}

	int getCornerRadius() {
		return cornerRadius;
	}

	boolean boldInDirection = false;

	CanvasDir boldDirection = CanvasDir.NONE;

	public void bold(CanvasDir dir) {
		boldInDirection = true;
		boldDirection = dir;
		rehighlight();
	}

	private void rehighlight() {
		clear();
		if (boldInDirection) {
			Point2D[] lineOne = new Point2D[2];
			if (boldDirection == CanvasDir.INTERIOR) {
				setStroke(getHighlightDisplayProperties().getStroke());
				setStrokePaint(getHighlightDisplayProperties().getStrokePaint());
			} else if (boldDirection == CanvasDir.TOP) {
				lineOne[0] = bboxPts[0];
				lineOne[1] = bboxPts[1];
				highlightLine.setPathToPolyline(lineOne);
			} else if (boldDirection == CanvasDir.RIGHT) {
				lineOne[0] = bboxPts[1];
				lineOne[1] = bboxPts[2];
				highlightLine.setPathToPolyline(lineOne);
			} else if (boldDirection == CanvasDir.BOTTOM) {
				lineOne[0] = bboxPts[2];
				lineOne[1] = bboxPts[3];
				highlightLine.setPathToPolyline(lineOne);
			} else if (boldDirection == CanvasDir.LEFT) {
				lineOne[0] = bboxPts[3];
				lineOne[1] = bboxPts[0];
				highlightLine.setPathToPolyline(lineOne);
			} else if (boldDirection == CanvasDir.TOP_RIGHT) {
				highlightCircle.setPathToEllipse((int) bboxPts[1].getX()
						- getCornerRadius(), (int) bboxPts[1].getY()
						- getCornerRadius(), 2 * getCornerRadius(),
						2 * getCornerRadius());
			} else if (boldDirection == CanvasDir.TOP_LEFT) {
				highlightCircle.setPathToEllipse((int) bboxPts[0].getX()
						- getCornerRadius(), (int) bboxPts[0].getY()
						- getCornerRadius(), 2 * getCornerRadius(),
						2 * getCornerRadius());
			} else if (boldDirection == CanvasDir.BOTTOM_LEFT) {
				highlightCircle.setPathToEllipse((int) bboxPts[3].getX()
						- getCornerRadius(), (int) bboxPts[3].getY()
						- getCornerRadius(), 2 * getCornerRadius(),
						2 * getCornerRadius());
			} else if (boldDirection == CanvasDir.BOTTOM_RIGHT) {
				highlightCircle.setPathToEllipse((int) bboxPts[2].getX()
						- getCornerRadius(), (int) bboxPts[2].getY()
						- getCornerRadius(), 2 * getCornerRadius(),
						2 * getCornerRadius());
			}
		}
		if (centerBolded) {
			Point2D center = getCenterPt();
			highlightCenter.setPathToEllipse((int) center.getX()
					- getCornerRadius(), (int) center.getY()
					- getCornerRadius(), 2 * getCornerRadius(),
					2 * getCornerRadius());
		}
	}

	private boolean centerBolded = false;

	/**
	 * Turns on/off the center highlight, indicating the middle of the box.
	 * 
	 * @param b
	 */
	public void setCenterBolded(boolean b) {
		centerBolded = b;
		rehighlight();
	}

	public void unbold() {
		boldInDirection = false;
		centerBolded = false;
		clear();
	}

	/**
	 * Makes all bolding "blank" so it doesn't highlight
	 */
	public void clear() {
		Point2D[] blank = new Point2D[1];
		blank[0] = new Point2D.Double();
		highlightLine.setPathToPolyline(blank);
		highlightCircle.setPathToPolyline(blank);
		if (centerBolded == false) // Try to avoid flickering
			highlightCenter.setPathToPolyline(blank);
		resetStyle();
	}

	/**
	 * @return
	 */
	public Point2D getCenterPt() {
		Point2D center = new Point2D.Double();
		Point2D upperLeft = bboxPts[0];
		center.setLocation(upperLeft.getX() + (getWidth() / 2), upperLeft
				.getY()
				+ (getHeight() / 2));
		return center;
	}
}