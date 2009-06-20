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

package edu.umd.cfar.lamp.viper.examples.textline;

import java.awt.*;
import java.awt.geom.*;
import java.util.*;
import java.util.logging.*;

import viper.api.*;
import viper.api.time.*;
import edu.umd.cfar.lamp.chronicle.*;
import edu.umd.cfar.lamp.viper.geometry.*;
import edu.umd.cfar.lamp.viper.gui.canvas.*;
import edu.umd.cfar.lamp.viper.gui.canvas.datatypes.*;
import edu.umd.cfar.lamp.viper.gui.core.*;
import edu.umd.cfar.lamp.viper.gui.data.obox.*;
import edu.umd.cs.piccolo.*;
import edu.umd.cs.piccolo.nodes.*;

/**
 * Heavily based on @{link edu.umd.cfar.lamp.viper.gui.data.obox.OboxNode}.
 * 
 * @author spikes51@umiacs.umd.edu
 * @since Feb 24, 2005
 *
 */
public class TextlineNode extends PBoxNode implements Attributable {
	private static Logger logger = Logger
			.getLogger("edu.umd.cfar.lamp.viper.gui.canvas.datatypes");
	private static final double HANDLE_LENGTH = 20;

	TextlineModel origCopy;
	TextlineModel changedCopy;
	Point2D[] oboxPts;
	Point2D[] rightHandlePts;
	Point2D[] northHandlePts;
	double oboxWidth;
	double oboxHeight;
	double angle; // in radians
	
	private PPath rightHandle;
	private PPath northHandle;
	private PNode upLabel;

	private PTextLabel text = new PTextLabel();
	private PPath offsetLines = new PPath();

	private Point2D[] blank = new Point2D[] {new Point2D.Double()};
	private boolean centerBolded = false;
	
	private int offscreenCountL;
	private int offscreenCountR;
	private PPath offscreenHandleL = new PPath();
	private PPath offscreenHandleR = new PPath();

	// available states
	static final int TEXTLINE_DEFAULT = 0; // default state: Textline is on the screen, but not selected
	static final int TEXTLINE_SELECTED = 1; // Textline is selected (highlighted)

	int state;
	
	// For highlighting
	PPath highlightLine = new PPath();
	PPath highlightCircle = new PPath();
	PPath highlightInterior = new PPath();
	PPath highlightCenterPoint = new PPath();
	
	// Ghost boundary while placing
	private PPath ghostBoundary = new PPath();
	
	private double diffWidth; // stores the difference in width during resize events
	private int selectedOffset; // currently selected offset index (-1 if none)

	/**
	 *  
	 */
	public TextlineNode(ViperViewMediator mediator) {
		super(mediator);
		state = TEXTLINE_DEFAULT;
		rightHandle = new PPath();
		northHandle = new PPath();

		oboxPts = new Point2D[5];
		
		diffWidth = 0;
		selectedOffset = -1;

		// Create handle
		rightHandlePts = new Point2D[2];
		northHandlePts = new Point2D[2];
		upLabel = new PTextLabel(" u ");

		// put the word boundaries at the bottom of the pile
		addChild(offsetLines);
		
		// add the next being displayed above the box
		addChild(text);
		
		// Set handle as obox child
		addChild(rightHandle);
		addChild(northHandle);

		addChild(highlightCenterPoint);
		addChild(highlightLine);
		addChild(highlightCircle);
		addChild(highlightInterior);
		
		addChild(ghostBoundary);

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

		//ghostBoundary.setStroke(new BasicStroke(1.4f, 2, 0, 10.0f, new float[]{5.0f}, 0.0f)); // dashed
		ghostBoundary.setStroke(getDisplayProperties().getStroke());
		ghostBoundary.setStrokePaint(Color.BLUE);
		
		// reset styles for all the children of offsetLines
		ListIterator li = offsetLines.getChildrenIterator();
		for(int i = 0; li.hasNext(); i++) {
			WordBoundaryNode curr = ((WordBoundaryNode) li.next());
			if(selectedOffset == i) {
				curr.setStroke(getHighlightDisplayProperties().getStroke());
				curr.setStrokePaint(getHighlightDisplayProperties().getStrokePaint());
			} else {
				curr.setStroke(getDisplayProperties().getStroke());
				curr.setStrokePaint(getDisplayProperties().getStrokePaint());
			}
		}
		
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
	
	public void setSelected() {
		if(state == TEXTLINE_SELECTED) return; // time saver--return early
		state = TEXTLINE_SELECTED;
		//System.out.println("Setting state = SELECTED"); // DEBUG
		// TODO: preference check, return if we are always displaying text
		text.setVisible(true);
		
		// make all children visible
		ListIterator li = offsetLines.getChildrenIterator();
		for(int i = 0; li.hasNext(); i++) {
			WordBoundaryNode curr = ((WordBoundaryNode) li.next());
			curr.setSelected();
		}
	}
	
	public void setUnselected() {
		if(state == TEXTLINE_DEFAULT) return; // time saver--return early
		state = TEXTLINE_DEFAULT;
		//System.out.println("Setting state = DEFAULT"); // DEBUG
		// TODO: preference check, return if we are always displaying text		
		text.setVisible(false);

		// hide the ghost indicator
		hideGhost();
		
		// hide all children
		ListIterator li = offsetLines.getChildrenIterator();
		for(int i = 0; li.hasNext(); i++) {
			WordBoundaryNode curr = ((WordBoundaryNode) li.next());
			curr.setUnselected();
		}
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
	 * Can't use getHeight() since PNode already defines it
	 * 
	 * @return the height of obox
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

	public void setWidthAndHeight(double widthIn, double heightIn, double diffWidthIn) {
		oboxWidth = widthIn;
		oboxHeight = heightIn;
		diffWidth = diffWidthIn;
		updateObox();
	}

	/** 
	 * Overrides setAttribute to make it compatible with TextlineNodes (not just BoxInformation)
	 * 
	 * @see edu.umd.cfar.lamp.viper.gui.canvas.datatypes.Attributable#setAttribute(viper.api.Attribute)
	 */
	public void setAttribute(Attribute attr) {
		this.attr = attr ;
		Instant now = mediator.getMajorMoment();
		// Get the oriented box corresponding to current frame
		TextlineModel box = (TextlineModel) attr.getAttrValueAtInstant( now ) ;
		//System.out.println("setAttribute: Word boundaries: "+box.getWordOffsetsAsStr()); // DEBUG
		// Extract information about oriented box for local use
		if ( box != null )
			setPath( box ) ;
	}

	/**
	 * Dummy wrapper method so that setPath from PBoxNode is implemented
	 */
	public void setPath(BoxInformation box) {
		setPath((TextlineModel) box);
	}

	public void setPath(TextlineModel box) {
		if (box == null) {
			logger.warning("Uhoh");
			return;
		}
		logger.fine("==== Init TextlineNode TextlineModel: " + box);

		// IMPORTANT: must do this FIRST and set BOTH
		changedCopy = origCopy = (TextlineModel) box.clone();

		oboxWidth = box.getWidth();
		oboxHeight = box.getHeight();
		angle = Math.toRadians(box.getRotation());

		text.setText(box.getOccludedWords(null)[0]); // display only the first word

		// Put enough information so obox can be updated
		double x = box.getX();
		double y = box.getY();

		oboxPts[0] = new Point2D.Double(x, y);

		// Now that enough info is available, update the obox and its handle
		updateObox();
	}

	/**
	 */
	private void updateText() {
		double x = oboxPts[0].getX();
		double y = oboxPts[0].getY();
		
		text.setHOffset(-5);
		text.setWOffset(-2);
		
		Point2D p = new Point2D.Double(x, y - text.getHeight() + 1);
		AffineTransform textRotation = AffineTransform.getRotateInstance(-angle, x, y);
		p = textRotation.transform(p,null);
		text.setOffset(p);
		text.setRotation(-angle);
	}
	
	public void updateOffsets() {
		//System.out.println("Calling updateOffsets");

		double x = oboxPts[0].getX();
		double y = oboxPts[0].getY();
		
		// clear out all previous children
		offsetLines.removeAllChildren();

		// create one transform to use on the entire collection of offset objects
		offsetLines.setTransform(AffineTransform.getRotateInstance(-angle, x, y));
		
		// IMPORTANT: work with a copy of the original
		changedCopy = (TextlineModel) origCopy.clone();
		
		// detect discrepancy: if we're in the default state but the text is visible, we need to hide everything
		boolean hideAll = false;
		if(state == TEXTLINE_DEFAULT && text.getVisible()) hideAll = true;
		
		// rebuild the children nodes
		ArrayList wo = changedCopy.getWordOffsets();
		String[] words = changedCopy.getOccludedWords(null);
		offscreenCountL = 0;
		offscreenCountR = 0;
		int stackCount = 0, prevX = 0;
		for(int i = 0; i < wo.size(); i++) {
			Integer off = (Integer) wo.get(i);
			off = new Integer(off.intValue() + (int) diffWidth);
			wo.set(i,off);
			int xval = (int) x + off.intValue();
			int label = i+1;
			String word = "";
			int type = WordBoundaryNode.MIDDLE_BOUNDARY;
			if(words.length > i+1) word = words[i+1]; // start with the second word (first is handled in setPath)
			else logger.log(Level.WARNING, "TextlineNode: Found more word boundaries than words present in string");
			
			// offscreen on the right
			if(xval <= x) {
				label = ++offscreenCountL;
				type = WordBoundaryNode.LEFT_BOUNDARY;
				word = ""; // hide the text
				xval = (int) x;
			}
			// offscreen on the left
			if(xval >= (int) x + oboxWidth) {
				label = ++offscreenCountR;
				type = WordBoundaryNode.RIGHT_BOUNDARY;
				word = ""; // hide the text
				xval = (int) (x + oboxWidth);
			}

			Point2D p1 = new Point2D.Double(xval, y + oboxHeight);
			Point2D p2 = new Point2D.Double(xval, y);
			
			if(i > 0 && xval - prevX < TextlineCanvasEditor.BOUNDARY_PUSH_THRESHOLD) {
				stackCount++;
				word = ""; // hide the text for stacked boundaries
			}
			else stackCount = 0; // IMPORTANT: reset stackCount
			prevX = xval; // store previous x value
			
			WordBoundaryNode curr = new WordBoundaryNode(p1, p2, word, label, type, stackCount, this);
			offsetLines.addChild(curr);
			if(hideAll) curr.setUnselected();
			//System.out.println("Selected offset = "+selectedOffset+", i = "+i);
			if(selectedOffset == i) {
				//System.out.println("Setting color of offset "+i+" to PURPLE"); // DEBUG
				curr.setStroke(getHighlightDisplayProperties().getStroke());
				curr.setStrokePaint(getHighlightDisplayProperties().getStrokePaint());
			} else {
				curr.setStroke(getDisplayProperties().getStroke());
				curr.setStrokePaint(getDisplayProperties().getStrokePaint());
			}
			text.setText(changedCopy.getOccludedWords(null)[offscreenCountL]); // display only the first word not offscren on the left
			if(hideAll) text.setVisible(false);
		}
	}

	public void updateObox() {
		updateOboxPart();
		updateHandles();
		updateText();
		if(changedCopy != null) updateOffsets();
	}
	
	/**
	 * Determines whether the given point is near any boundary handle
	 * 
	 * @param p point
	 * @return index of the boundary's handle that contains the given point
	 */
	public int getNearestBoundaryHandle(Point2D p) {
		int ret = -1;
		ListIterator li = offsetLines.getChildrenIterator();
		for(int i = 0; li.hasNext(); i++) {
			if(((WordBoundaryNode) li.next()).handleContains(p))
				ret = i;
		}
		return ret;
	}
	
	/**
	 * Updates the specified wordOffset
	 * 
	 * @param index index of the wordOffset
	 * @param value new value of the boundary's x-offset
	 */
	public void updateWordOffsetAtIndex(int index, int value) {
		Integer off = new Integer(value);
		// IMPORTANT: must set the index in origCopy, NOT in changedCopy
		origCopy.getWordOffsets().set(index,off);
	}
	
	/**
	 * Displays the ghost boundary at the specified x offset
	 * 
	 * @param xOff x offset at which to show the ghost boundary
	 */
	public void showGhostAtOffset(int xOff) {
		
		// normalize to the valid range
		if(xOff < 0) xOff = 0;
		else if(xOff > oboxWidth) xOff = (int) oboxWidth;
		
		double x = oboxPts[0].getX();
		double newX = x + xOff;
		double y = oboxPts[0].getY();
		AffineTransform trans = AffineTransform.getRotateInstance(-angle, x, y);
		
		Point2D[] ghost = new Point2D.Double[2];
		ghost[0] = trans.transform(new Point2D.Double(newX, y), null);
		ghost[1] = trans.transform(new Point2D.Double(newX, y + oboxHeight), null);
		ghostBoundary.setPathToPolyline(ghost);
	}
	
	/**
	 * Hides the ghost boundary
	 */
	public void hideGhost() {
		ghostBoundary.setPathToPolyline(blank);
	}
	
	/**
	 * Getter for the model of this TextlineNode
	 * 
	 * @return TextlineModel object that this node is based on
	 */
	public TextlineModel getModel() {
		return origCopy;
	}

	/**
	 * Used by the mouse event handler to let us know which offset to highlight
	 * 
	 * @param selectedOffset The offset to select/highlight.
	 */
	public void setSelectedOffset(int newOffset) {
		// reset the previously selected boundary node (only if in range)
		if(selectedOffset >= 0 && selectedOffset < offsetLines.getChildrenCount()) {
			WordBoundaryNode curr = (WordBoundaryNode) offsetLines.getChild(selectedOffset);
			curr.setStroke(getDisplayProperties().getStroke());
			curr.setStrokePaint(getDisplayProperties().getStrokePaint());
		}
		
		selectedOffset = newOffset; // set the new value
		
		// highlight the newly selected boundary node (only if not -1)
		if(selectedOffset >= 0 && selectedOffset < offsetLines.getChildrenCount()) {
			//System.out.println("Selecting offset "+selectedOffset); // DEBUG
			WordBoundaryNode curr = (WordBoundaryNode) offsetLines.getChild(selectedOffset);
			curr.setStroke(getHighlightDisplayProperties().getStroke());
			curr.setStrokePaint(getHighlightDisplayProperties().getStrokePaint());
			
		}
	}
	
	/**
	 * @return Returns the selectedOffset.
	 */
	public int getSelectedOffset() {
		return selectedOffset;
	}
	
	// returns the transform needed to translate mouse clicks
	public AffineTransform getInverseRotateTransform() {
		// IMPORTANT: use the POSITIVE angle, not the negative one
		double a = Math.toRadians(origCopy.getRotation());
		return AffineTransform.getRotateInstance(a, oboxPts[0].getX(), oboxPts[0].getY());
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

	/**
	 * @return Returns the number of boudaries offscreen on the left.
	 */
	public int getOffscreenCountL() {
		return offscreenCountL;
	}
	
	/**
	 * @return Returns the number of boudaries offscreen on the right.
	 */
	public int getOffscreenCountR() {
		return offscreenCountR;
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
		//TextlineModel origBox = (TextlineModel) attr.getAttrValueAtInstant(now);
		TextlineModel origBox = changedCopy;

		// To make sure tiny changes don't affect result
		if (origBox != null) {
			int setDegrees = origBox.getRotation();
			if (Math.abs(degrees - setDegrees) < 1.2)
				degrees = origBox.getRotation();
		}

		TextlineModel textlineModel = new TextlineModel((int) oboxPts[0].getX(),
				(int) oboxPts[0].getY(), (int) oboxWidth, (int) oboxHeight,
				degrees, origBox.getText(null), origBox.getOcclusions(), origBox.getWordOffsets());
		return textlineModel;
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
