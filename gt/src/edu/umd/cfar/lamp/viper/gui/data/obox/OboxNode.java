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

package edu.umd.cfar.lamp.viper.gui.data.obox;

import java.awt.geom.*;
import java.util.logging.*;

import viper.api.time.*;
import edu.umd.cfar.lamp.chronicle.*;
import edu.umd.cfar.lamp.viper.geometry.*;
import edu.umd.cfar.lamp.viper.gui.canvas.*;
import edu.umd.cfar.lamp.viper.gui.canvas.datatypes.*;
import edu.umd.cfar.lamp.viper.gui.core.*;
import edu.umd.cs.piccolo.*;
import edu.umd.cs.piccolo.nodes.*;

/**
 * @author clin
 * 
 * To change the template for this generated type comment go to
 * Window>Preferences>Java>Code Generation>Code and Comments
 */
public class OboxNode extends PBoxNode implements Attributable {
	private static Logger logger = Logger
			.getLogger("edu.umd.cfar.lamp.viper.gui.canvas.datatypes");
	private static final double HANDLE_LENGTH = 20;

	OrientedBox localCopy;
	Point2D[] oboxPts;
	Point2D[] rightHandlePts;
	Point2D[] northHandlePts;
	double oboxWidth;
	double oboxHeight;
	double angle; // in radians
	
	private PPath rightHandle;
	private PPath northHandle;
	private PNode upLabel;

	
	// For highlighting
	PPath highlightLine = new PPath();
	PPath highlightCircle = new PPath();
	PPath highlightInterior = new PPath();
	PPath highlightCenterPoint = new PPath();


	/**
	 *  
	 */
	public OboxNode(ViperViewMediator mediator) {
		super(mediator);
		rightHandle = new PPath();
		northHandle = new PPath();

		oboxPts = new Point2D[5];

		// Create handle
		rightHandlePts = new Point2D[2];
		northHandlePts = new Point2D[2];
		upLabel = new PTextLabel(" u ");

		// Set handle as obox child
		addChild(rightHandle);
		addChild(northHandle);

		addChild(highlightCenterPoint);
		addChild(highlightLine);
		addChild(highlightCircle);
		addChild(highlightInterior);

		resetStyle();
	}

	/**
	 * 
	 */
	protected void resetStyle() {
		rightHandle.setStrokePaint(getHandleDisplayProperties().getStrokePaint());
		rightHandle.setStroke(getHandleDisplayProperties().getStroke());
		northHandle.setStrokePaint(getHandleDisplayProperties().getStrokePaint());
		northHandle.setStroke(getHandleDisplayProperties().getStroke());
		highlightCenterPoint.setStroke(getHighlightDisplayProperties().getStroke());
		highlightLine.setStroke(getHighlightDisplayProperties().getStroke());
		highlightCircle.setStroke(getHighlightDisplayProperties().getStroke());
		highlightInterior.setStroke(getHighlightDisplayProperties().getStroke());

		highlightCenterPoint.setStrokePaint(getHighlightDisplayProperties().getStrokePaint());
		highlightLine.setStrokePaint(getHighlightDisplayProperties().getStrokePaint());
		highlightCircle.setStrokePaint(getHighlightDisplayProperties().getStrokePaint());
		highlightInterior.setStrokePaint(getHighlightDisplayProperties().getStrokePaint());
		
		this.setStroke(getDisplayProperties().getStroke());
		this.setStrokePaint(getDisplayProperties().getStrokePaint());
	}

	public Point2D[] getBoxPts() {
		return oboxPts;
	}

	public Point2D[] getRightHandlePts() {
		return rightHandlePts;
	}

	public Point2D[] getNorthHandlePts() {
		return northHandlePts;
	}

	/**
	 * Can't use getWidth() since PNode already defines it
	 * 
	 * @return the width of obox
	 */
	public double getBoxWidth() {
		return oboxWidth;
	}

	/**
	 * Can't use getWidth() since PNode already defines it
	 * 
	 * @return the width of obox
	 */
	public double getBoxHeight() {
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

	public void setPath(BoxInformation box) {
		if (box == null) {
			logger.warning("Uhoh");
			return;
		}
		logger.fine("==== Init OboxNode OrientedBox: " + box);

		oboxWidth = box.getWidth();
		oboxHeight = box.getHeight();
		angle = Math.toRadians(box.getRotation());

		// Put enough information so obox can be updated
		double x = box.getX();
		double y = box.getY();

		oboxPts[0] = new Point2D.Double(x, y);

		// Now that enough info is available, update the obox and its handle
		updateObox();

		localCopy = (OrientedBox) box.clone();
	}

	public void updateObox() {
		updateOboxPart();
		updateHandles();
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

	private void updateOboxPart() {
		OboxRectangle r = new OboxRectangle((int) oboxPts[0].getX(),
				(int) oboxPts[0].getY(), (int) oboxWidth, (int) oboxHeight,
				angle);
		oboxPts[0] = r.p[0];
		oboxPts[1] = r.p[1];
		oboxPts[2] = r.p[2];
		oboxPts[3] = r.p[3];
		oboxPts[4] = r.p[0];
		setPathToPolyline(oboxPts);
	}

	private void updateHandles() {
		// To get the right handle, this makes a slightly bigger box
		// with the same origin and angle as the obox, and connects
		// the corners of the two boxes with a line segment
		OboxRectangle r = new OboxRectangle((int) oboxPts[0].getX(),
				(int) oboxPts[0].getY(), oboxWidth + HANDLE_LENGTH, HANDLE_LENGTH, angle);
		rightHandlePts[0] = oboxPts[1];
		rightHandlePts[1] = r.p[1];
		rightHandle.setPathToPolyline(rightHandlePts);
		
		// To get the north handle, take the point halfway between
		// the first and second points and go in the direction of 
		// rotation by handle_length
		
		northHandlePts[0] = new Point2D.Double((oboxPts[0].getX() + oboxPts[1].getX()) / 2, (oboxPts[0].getY() + oboxPts[1].getY()) / 2);
		northHandlePts[1] = new Point2D.Double(northHandlePts[0].getX() - r.p[3].getX() + r.p[0].getX(), northHandlePts[0].getY() - r.p[3].getY() + r.p[0].getY());
		northHandle.setPathToPolyline(northHandlePts);
	}

	public Object getUpdatedAttribute() {
		int degrees = (int) Math.toDegrees(angle);
		Instant now = mediator.getMajorMoment();
		// Get the oriented box corresponding to current frame
		OrientedBox origBox = (OrientedBox) attr.getAttrValueAtInstant(now);

		// To make sure tiny changes don't affect result
		if (origBox != null) {
			int setDegrees = origBox.getRotation();
			if (Math.abs(degrees - setDegrees) < 1.2)
				degrees = origBox.getRotation();
		}

		OrientedBox orientedBox = new OrientedBox((int) oboxPts[0].getX(),
				(int) oboxPts[0].getY(), (int) oboxWidth, (int) oboxHeight,
				degrees);
		return orientedBox;
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
		if (centerBolded) {
			Point2D center = getCenterPt();
			highlightCenterPoint.setPathToEllipse((int) center.getX()
					- getCornerRadius(), (int) center.getY()
					- getCornerRadius(), 2 * getCornerRadius(),
					2 * getCornerRadius());
		}
	}

	public void boldRightHandle() {
		setHandleDisplayProperties(HighlightSingleton.STYLE_HOVER_HANDLE);
	}

	public void boldNorthHandle() {
		setHandleDisplayProperties(HighlightSingleton.STYLE_HOVER_HANDLE);
		AffineTransform recenter = AffineTransform.getTranslateInstance(northHandlePts[1].getX(), northHandlePts[1].getY());
		recenter.concatenate(AffineTransform.getRotateInstance(-angle));
		recenter.concatenate(AffineTransform.getTranslateInstance(-upLabel.getWidth()/2, -upLabel.getHeight() + OboxCanvasEditor.HANDLE_RADIUS));
		upLabel.setTransform(recenter);
		if (!northHandle.getChildrenReference().contains(upLabel)) {
			((PTextLabel) upLabel).setTextInset(0);
			((PTextLabel) upLabel).setBorderCurveRadius(8);
			((PTextLabel) upLabel).recomputeLayout();
			northHandle.addChild(upLabel);
		}
	}

	private void unboldHandle() {
		setHandleDisplayProperties(HighlightSingleton.STYLE_HANDLE);
		rightHandle.removeAllChildren();
		northHandle.removeAllChildren();
		resetStyle();
	}

	/**
	 * Makes all bolding "blank" so it doesn't highlight
	 */
	public void unbold() {
		highlightLine.setPathToPolyline(blank);
		highlightCircle.setPathToPolyline(blank);
		highlightInterior.setPathToPolyline(blank);
		unboldHandle();
	}

	private Point2D[] blank = new Point2D[] {new Point2D.Double()};
	private boolean centerBolded = false;
	public void setCenterBolded(boolean bold) {
		if (centerBolded != bold) {
			centerBolded = bold;
			if (bold) {
				Point2D center = getCenterPt();
				highlightCenterPoint.setPathToEllipse((int) center.getX()
						- getCornerRadius(), (int) center.getY()
						- getCornerRadius(), 2 * getCornerRadius(),
						2 * getCornerRadius());
			} else {
				highlightCenterPoint.setPathToPolyline(blank);
			}
		}
	}

}