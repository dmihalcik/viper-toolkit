/*
 * Created on Feb 9, 2004
 *
 * To change the template for this generated file go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
package edu.umd.cfar.lamp.viper.gui.data.ellipse;

import java.awt.*;
import java.awt.geom.*;
import java.util.logging.*;

import viper.api.*;
import viper.api.time.*;
import edu.umd.cfar.lamp.viper.geometry.*;
import edu.umd.cfar.lamp.viper.gui.canvas.*;
import edu.umd.cfar.lamp.viper.gui.canvas.datatypes.*;
import edu.umd.cfar.lamp.viper.gui.core.*;
import edu.umd.cfar.lamp.viper.gui.data.obox.*;
import edu.umd.cs.piccolo.nodes.*;

/**
 * @author clin
 */
public class EllipseNode extends AttributablePPathAdapter implements Attributable {
	private static Logger logger = Logger
			.getLogger("edu.umd.cfar.lamp.viper.gui.canvas.datatypes");

	Ellipse localCopy;

	Point2D[] oboxPts;

	Point2D[] handlePts;

	double oboxWidth;

	double oboxHeight;

	double angle; // in radians

	PPath handle;

	// For highlighting
	PPath highlightLine = new PPath();

	PPath highlightCircle = new PPath();

	PPath highlightInterior = new PPath();

	PPath boundingBox = new PPath();

	Ellipse2D ellipse = new Ellipse2D.Double();

	
	/**
	 *  
	 */
	public EllipseNode(ViperViewMediator mediator) {
		super(mediator);
		handle = new PPath();
		oboxPts = new Point2D[5];
		// Create handle
		handlePts = new Point2D[2];

		// Set handle as obox child
		boundingBox.addChild(handle);

		boundingBox.addChild(highlightLine);
		boundingBox.addChild(highlightCircle);
		boundingBox.addChild(highlightInterior);

		// Bounding box
		addChild(boundingBox);
		resetStyle();
	}

	/**
	 * 
	 */
	protected void resetStyle() {
		handle.setStrokePaint(getHandleDisplayProperties().getStrokePaint());
		handle.setStroke(getHandleDisplayProperties().getStroke());
		
		boundingBox.setStroke(getHandleDisplayProperties().getStroke());
		boundingBox.setStrokePaint(getHandleDisplayProperties().getStrokePaint());

		highlightLine.setStroke(getHighlightDisplayProperties().getStroke());
		highlightCircle.setStroke(getHighlightDisplayProperties().getStroke());
		highlightInterior.setStroke(getHighlightDisplayProperties().getStroke());

		highlightLine.setStrokePaint(getHighlightDisplayProperties().getStrokePaint());
		highlightCircle.setStrokePaint(getHighlightDisplayProperties().getStrokePaint());
		highlightInterior.setStrokePaint(getHighlightDisplayProperties().getStrokePaint());
		
		setStroke(getDisplayProperties().getStroke());
		setStrokePaint(getDisplayProperties().getStrokePaint());
	}

	public Point2D[] getOboxPts() {
		return oboxPts;
	}

	public Point2D[] getHandlePts() {
		return handlePts;
	}

	/**
	 * Can't use getWidth() since PNode already defines it
	 * 
	 * @return the width of obox
	 */
	public double getOboxWidth() {
		return oboxWidth;
	}

	/**
	 * Can't use getWidth() since PNode already defines it
	 * 
	 * @return the width of obox
	 */
	public double getOboxHeight() {
		return oboxHeight;
	}

	public void setAngleInRadians(double angleIn) {
		angle = angleIn;
		updateObox();
	}

	public double getAngleInRadians() {
		return angle;
	}

	public void setWidthAndHeight(double widthIn, double heightIn) {
		oboxWidth = widthIn;
		oboxHeight = heightIn;
		updateObox();
	}

	private void setPath(Ellipse box) {
		if (box == null) {
			logger.warning("Uhoh");
		}
		logger.fine("==== Init OboxNode OrientedBox: " + box);

		oboxWidth = box.getWidth().doubleValue();
		oboxHeight = box.getHeight().doubleValue();
		angle = Math.toRadians(box.getRotation());

		// Put enough information so obox can be updated
		double x = box.getX().doubleValue();
		double y = box.getY().doubleValue();

		oboxPts[0] = new Point2D.Double(x, y);

		// Now that enough info is available, update the obox and its handle
		updateObox();

		localCopy = makeCopy(box);
	}

	public void updateObox() {
		updateOboxPart();
		updateHandlePart();
		updateEllipse();
	}

	private AffineTransform getTransformForRotate() {
		double x = oboxPts[0].getX();
		double y = oboxPts[0].getY();
		AffineTransform transFrom = AffineTransform
				.getTranslateInstance(-x, -y);
		AffineTransform rotate = AffineTransform.getRotateInstance(-angle);
		AffineTransform transBack = AffineTransform.getTranslateInstance(x, y);
		rotate.concatenate(transFrom);
		transBack.concatenate(rotate);
		return transBack;
	}

	private void updateEllipse() {
		ellipse.setFrame(oboxPts[0].getX(), oboxPts[0].getY(), oboxWidth,
				oboxHeight);
		Ellipse2D copy = (Ellipse2D) ellipse.clone();
		AffineTransform trans = getTransformForRotate();
		Shape s = trans.createTransformedShape(copy);
		setPathTo(s);
	}

	private void updateOboxPart() {
		OboxRectangle r = new OboxRectangle((int) oboxPts[0].getX(),
				(int) oboxPts[0].getY(), (int) oboxWidth, (int) oboxHeight,
				angle);

		oboxPts[0] = r.p[0];
		oboxPts[1] = r.p[1];
		oboxPts[2] = r.p[2];
		oboxPts[3] = r.p[3];
		oboxPts[4] = r.p[0];
		boundingBox.setPathToPolyline(oboxPts);
	}

	private void updateHandlePart() {
		OboxRectangle r = new OboxRectangle((int) oboxPts[1].getX(),
				(int) oboxPts[1].getY(), 20, 15, angle);
		handlePts[0] = r.p[0];
		handlePts[1] = r.p[1];
		handle.setPathToPolyline(handlePts);
	}

	public void setAttribute(Attribute attr) {
		this.attr = attr;
		Instant now = getInstant();
		// Get the oriented box corresponding to current frame
		Ellipse ellipse = (Ellipse) attr.getAttrValueAtInstant(now);
		// Extract information about oriented box for local use
		if (ellipse != null)
			setPath(ellipse);
	}

	public Object getUpdatedAttribute() {
		Instant now = mediator.getMajorMoment();
		// Get the oriented box corresponding to current frame
		Ellipse orig = (Ellipse) attr.getAttrValueAtInstant(now);
		int degrees = orig.getRotation();

		// To make sure tiny changes don't affect result
		int setDegrees = (int) Math.toDegrees(angle);
		if (orig != null) {
			if (Math.abs(degrees - setDegrees) < 1.2)
				setDegrees = orig.getRotation();
		}

		Ellipse updated = new Ellipse((int) oboxPts[0].getX(), (int) oboxPts[0]
				.getY(), (int) oboxWidth, (int) oboxHeight, setDegrees);
		return updated;
	}

	private Ellipse makeCopy(Ellipse ellipse) {
		return new Ellipse(ellipse);
	}

	int cornerRadius = 5;

	public void setCornerRadius(int val) {
		cornerRadius = val;
	}

	int getCornerRadius() {
		return cornerRadius;
	}

	public void bold(CanvasDir dir) {
		unbold();
		if (dir == CanvasDir.NONE)
			return;

		Point2D[] line = new Point2D[2];
		if (dir == CanvasDir.TOP) {
			line[0] = oboxPts[0];
			line[1] = oboxPts[1];
			highlightLine.setPathToPolyline(line);
		} else if (dir == CanvasDir.RIGHT) {
			line[0] = oboxPts[1];
			line[1] = oboxPts[2];
			highlightLine.setPathToPolyline(line);
		} else if (dir == CanvasDir.BOTTOM) {
			line[0] = oboxPts[2];
			line[1] = oboxPts[3];
			highlightLine.setPathToPolyline(line);
		} else if (dir == CanvasDir.LEFT) {
			line[0] = oboxPts[3];
			line[1] = oboxPts[0];
			highlightLine.setPathToPolyline(line);
		} else if (dir == CanvasDir.TOP_RIGHT) {
			highlightCircle.setPathToEllipse((int) oboxPts[1].getX()
					- getCornerRadius(), (int) oboxPts[1].getY()
					- getCornerRadius(), 2 * getCornerRadius(),
					2 * getCornerRadius());
		} else if (dir == CanvasDir.TOP_LEFT) {
			highlightCircle.setPathToEllipse((int) oboxPts[0].getX()
					- getCornerRadius(), (int) oboxPts[0].getY()
					- getCornerRadius(), 2 * getCornerRadius(),
					2 * getCornerRadius());
		} else if (dir == CanvasDir.BOTTOM_LEFT) {
			highlightCircle.setPathToEllipse((int) oboxPts[3].getX()
					- getCornerRadius(), (int) oboxPts[3].getY()
					- getCornerRadius(), 2 * getCornerRadius(),
					2 * getCornerRadius());
		} else if (dir == CanvasDir.BOTTOM_RIGHT) {
			highlightCircle.setPathToEllipse((int) oboxPts[2].getX()
					- getCornerRadius(), (int) oboxPts[2].getY()
					- getCornerRadius(), 2 * getCornerRadius(),
					2 * getCornerRadius());
		} else if (dir == CanvasDir.INTERIOR) {
			highlightInterior.setPathToPolyline(oboxPts);
		}
	}

	public void boldHandle() {
		setHandleDisplayProperties(HighlightSingleton.STYLE_HOVER_HANDLE);
	}

	private void unboldHandle() {
		setHandleDisplayProperties(HighlightSingleton.STYLE_HANDLE);
	}

	/**
	 * Makes all bolding "blank" so it doesn't highlight
	 */
	public void unbold() {
		Point2D[] blank = new Point2D[1];
		blank[0] = new Point2D.Double();
		highlightLine.setPathToPolyline(blank);
		highlightCircle.setPathToPolyline(blank);
		highlightInterior.setPathToPolyline(blank);
	}

	/**
	 * Used when making box inactive so only ellipse shows
	 */
	public void removeBoundingBox() {
		Point2D[] blank = new Point2D[1];
		blank[0] = new Point2D.Double();
		boundingBox.setPathToPolyline(blank);
		handle.setPathToPolyline(blank);
	}

	/**
	 * Used when making box active so only ellipse shows
	 */
	public void addBoundingBox() {
		updateOboxPart();
		updateHandlePart();
	}
}

