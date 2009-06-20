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

import java.awt.event.*;
import java.awt.geom.*;
import java.util.*;
import java.util.logging.*;

import edu.umd.cfar.lamp.viper.gui.canvas.*;
import edu.umd.cfar.lamp.viper.gui.canvas.datatypes.*;
import edu.umd.cfar.lamp.viper.gui.data.obox.*;
import edu.umd.cs.piccolo.event.*;

/**
 * Heavily based on @{link edu.umd.cfar.lamp.viper.gui.data.obox.OboxCanvasEditor}.
 * 
 * @author spikes51@umiacs.umd.edu
 * @since Feb 25, 2005
 *
 */
public class TextlineCanvasEditor extends GenericBoxEditor {
	public static final int HANDLE_RADIUS = 5;
	private static Logger logger = Logger
			.getLogger("edu.umd.cfar.lamp.viper.gui.canvas.datatypes");

	protected boolean isNearNorthHandle = false;
	protected boolean isNearRightHandle = false;
	Point2D[] rightHandlePts;
	Point2D[] northHandlePts;
	double angle;
	double oboxWidth, oboxHeight;
	Point2D origCenter;

	// available states
	static final int TEXTLINE_DEFAULT = 0; // default state: Textline is selected, but nothing is changing
	static final int TEXTLINE_RESIZING = 1; // Textline is being resized
	static final int TEXTLINE_SHIFTING = 2; // Textline is being moved (shifted)
	static final int TEXTLINE_ADD_BOUNDARY = 3; // word boundaries are being added
	static final int TEXTLINE_MOVE_BOUNDARY = 4; // a word boundary is being moved
	
	// state hierarchy:
	// TEXTLINE_DEFAULT
	//  |--> TEXTLINE_RESIZING
	//  |--> TEXTLINE_SHIFTING
	//  |--> TEXTLINE_ADD_BOUNDARY
	//  |--> TEXTLINE_MOVE_BOUNDARY
	//
	// All child states have to return to TEXTLINE_DEFAULT
	int state; // keeps track of the current state
	
	// spacing constants
	static final int BOUNDARY_RADIUS = 5; // max distance from boundary and still selected
	static final int BOUNDARY_PUSH_THRESHOLD = 3; // threshold for pushing boundaries
	static final int BOUNDARY_SNAP_ZONE = 7; // snap zone at either end for dumping remaining boundaries
	// IMPORTANT: must always be greater than BOUNDARY_MIN_SPACING by at least 1
	
	// used to track boundary drag events
	private int dragStartedPosition;
	private int dragFarLeft; // farthest left that the drag has gone
	private int dragFarRight; // farthest right that the drag has gone
	private int dragStartedIndex;
	private ArrayList pushList = new ArrayList(); // list of all boundaries being "pushed" along

	public TextlineCanvasEditor(Attributable attrIn) {
		// *********************************
		super(attrIn);
		box = (TextlineNode) attrIn;
		
		// IMPORTANT: if all possible word boundaries have not yet been placed, start out in boundary add mode
		TextlineModel tlm = ((TextlineNode) box).getModel();
		if(tlm.getWordOffsets().size() < tlm.getMaxBoundaries(null)) {
			state = TEXTLINE_ADD_BOUNDARY;
		}
		else state = TEXTLINE_DEFAULT;
		//System.out.println("Initializing new CanvasEditor object with state = "+state); // DEBUG
		
		logger.fine("Created TEXTLINE EDITOR");

		// Local copy of information stored in TextlineNode
		currPts = box.getBoxPts();
		rightHandlePts = ((TextlineNode) box).getRightHandlePts();
		northHandlePts = ((TextlineNode) box).getNorthHandlePts();
		oboxWidth = box.getBoxWidth();
		oboxHeight = box.getBoxHeight();
		angle = ((TextlineNode) box).getAngleInRadians();
		// for debugging purposes

		for (int i = 0; i < 4; i++)
			origPts[i] = new Point2D.Double();
	}

	public String getName() {
		return "TextlineCanvasEditor";
	}

	/**
	 * Gets the distance from the selection point to the edge of the box.
	 * 
	 * @param select
	 *            the point to test
	 * @return the shortest distance from select to the edge of the box
	 */
	public double minDist(Point2D select) {
		int startPoint = currPts.length - 1;
		double min = Double.POSITIVE_INFINITY;
		for (int endPoint = 0; endPoint < currPts.length; endPoint++) {
			Line2D test = new Line2D.Double(currPts[startPoint],
					currPts[endPoint]);
			min = Math.min(min, test.ptSegDist(select));
			startPoint = endPoint;
		}
		return min;
	}

	// Should be fixed
	public boolean contains(Point2D select) {
		boolean result = true;
		for (int i = 0; i < 4; i++)
			result = result
					&& isRightOf(currPts[i], currPts[(i + 1) % 4], select);
		return result;
	}

	/**
	 * 
	 * @param first
	 *            Tail of a vector
	 * @param second
	 *            Head of a vector
	 * @param select
	 *            Point is right of it?
	 * @return true if it's right, false if not
	 */
	boolean isRightOf(Point2D first, Point2D second, Point2D select) {
		Line2D edge = new Line2D.Double(first, second);
		return edge.relativeCCW(select) == -1;
	}

	public void mouseMoved(PInputEvent e) {
		isNearRightHandle = nearRightHandle(e.getPosition());
		isNearNorthHandle = nearNorthHandle(e.getPosition());
		handleMouseCommon(e);
	}

	public void doWhenSelected() {
		if(box != null) {
			((TextlineNode) box).setSelected();
		}
	}

	public void doWhenUnselected() {
		// XXX Hack fix for npe. not sure why this code is being called in the
		// first place
		if (box != null) {
			box.unbold();
			((TextlineNode) box).setUnselected();
		}
	}

	public void mousePressed(PInputEvent e) {
		boolean leftClick = (e.getModifiersEx() & InputEvent.BUTTON1_DOWN_MASK) != 0;
		boolean shiftDown = (e.getModifiersEx() & InputEvent.SHIFT_DOWN_MASK) != 0;
		boolean altDown = (e.getModifiersEx() & InputEvent.ALT_DOWN_MASK) != 0;
		boolean ctrlDown = (e.getModifiersEx() & InputEvent.CTRL_DOWN_MASK) != 0;
		
		if (!leftClick) return;
		logger.fine("TextlineCanvasEditor: mousePressed");
		pressPoint = e.getPosition();
		//System.out.println("mouse clicked at "+pressPoint.getX()+", "+pressPoint.getY()); // DEBUG
		dragPoint = pressPoint; // need this for computeAndSetAngle()
		
		TextlineNode textLine = (TextlineNode) box;
		ArrayList offsets = textLine.getModel().getWordOffsets();
		int maxOffsets = textLine.getModel().getMaxBoundaries(null);
		
		// if no boundaries have been added, enter add boundary mode automatically
		if(offsets.size() == 0 && maxOffsets > 0) {
			//System.out.println("Entering add boundary mode"); // DEBUG
			state = TEXTLINE_ADD_BOUNDARY;
		}
		
		// check if we are in add boundary mode
		if(state == TEXTLINE_ADD_BOUNDARY && shiftDown) {
			Point2D pick = textLine.getInverseRotateTransform().transform(e.getPosition(), null);
			int clickOffset = (int) (pick.getX() - box.getBoxPts()[0].getX()); // IMPORTANT: MUST subtract out x
			
			// if Ctrl pressed, add all remaining boundaries
			if(ctrlDown) {
				if(clickOffset <= BOUNDARY_SNAP_ZONE || clickOffset >= oboxWidth - BOUNDARY_SNAP_ZONE) {
					// handle the snap zones
					if(clickOffset <= BOUNDARY_SNAP_ZONE) clickOffset = 0;
					else clickOffset = (int) oboxWidth;
					//System.out.println("Dumping all remaining boundaries"); // DEBUG
					
					int ins = Collections.binarySearch(offsets, new Integer(clickOffset)); // find the right place
					if(ins < 0) ins = -(ins + 1); // compensate for negative (insertion) index
					while(offsets.size() < maxOffsets) { // insert until we have no offsets left
						// all clickOffset values are equal, so we don't have to increment ins
						offsets.add(ins, new Integer(clickOffset));
					}
					state = TEXTLINE_DEFAULT;
					textLine.hideGhost();
				} else {
					// ignore if a Ctrl-click was received outside the snap zones
				}
				
			// if no Ctrl, just add a single boundary
			} else {
				// insert the new boundary in its proper place in the array
				int ins = Collections.binarySearch(offsets, new Integer(clickOffset)); // find the right place
				if(ins < 0) ins = -(ins + 1); // compensate for negative (insertion) index
				offsets.add(ins, new Integer(clickOffset));
				if(offsets.size() >= maxOffsets) {
					state = TEXTLINE_DEFAULT;
					textLine.hideGhost();
				}
			}
			textLine.updateOffsets(); // IMPORTANT: must call updateOffsets here
			return; // IMPORTANT: return early
		}
		
		// check if this click is near a boundary handle
		int index = isNearBoundary(e.getPosition());
		if(index != -1) {
			if(!ctrlDown) { // start dragging if this is a regular click
				state = TEXTLINE_MOVE_BOUNDARY;
				dragStartedIndex = index; // save index of the boundary where the dragging started
				dragStartedPosition = ((Integer) offsets.get(index)).intValue();
				dragFarLeft = dragFarRight = dragStartedPosition; // start with the x-pos of the click
				pushList.clear(); // empty the push list in either case
				//System.out.println("Setting dragStartedNearBoundary TRUE"); // DEBUG
			} else { // if it was a Ctrl click, then delete the boundary that it is near
				dragStartedIndex = index; // save index of the boundary where the dragging started
				offsets.remove(index);
			}
			textLine.updateOffsets(); // IMPORTANT: must update here
			//System.out.println("click received by offset "+index); // DEBUG
			if(!inRangeOfInterest(e.getPosition())) {
				//System.out.println("click outside range of interest, bailing from mouseClicked..."); // DEBUG
				return; // bail if the click was outside the box so it doesn't get de-selected
			}
		}

		isNearNorthHandle = nearNorthHandle(pressPoint);
		isNearRightHandle = nearRightHandle(pressPoint);
		origWidth = oboxWidth;
		origHeight = oboxHeight;
		origCenter = box.getCenterPt();
		origPressPoint.setLocation(pressPoint);
		// Record original top left
		for (int i = 0; i < 4; i++) {
			origPts[i].setLocation(currPts[i]);
		}
		if (isNearNorthHandle || isNearRightHandle) {
			logger.fine("Near handle");
			currDir = CanvasDir.NONE;
			computeAndSetAngle();
		} else {
			currDir = findDirection(pressPoint);
			logger.fine("Direction is " + currDir);
			box.bold(currDir);
		}
	}

	public boolean nearRightHandle(Point2D pt) {
		double dist = pt.distance(rightHandlePts[1]);
		return dist < HANDLE_RADIUS;
	}
	public boolean nearNorthHandle(Point2D pt) {
		double dist = pt.distance(northHandlePts[1]);
		return dist < HANDLE_RADIUS;
	}

	public void mouseDragged(PInputEvent e) {
		boolean leftClick = (e.getModifiersEx() & InputEvent.BUTTON1_DOWN_MASK) != 0;
		
		if (!leftClick) return;
		
		//		TextlineNode.unbold() ;
		dragPoint = e.getPosition();
		handleMouseCommon(e);
	}

	/**
	 * Checks to see if the event has the ctrl mask. Puts the controller in the
	 * appropriate draw state, depending on event.
	 * 
	 * @param e
	 *            the (mouse?) event to check
	 */
	protected boolean verifyCtrlIsPressed(PInputEvent e) {
		boolean ctrlDown = (e.getModifiersEx() & InputEvent.CTRL_DOWN_MASK) != 0;
		
		setDrawnFromCenter((ctrlDown && !DEFAULT_DRAW_FROM_CENTER)
				|| (!ctrlDown && DEFAULT_DRAW_FROM_CENTER));
		return ctrlDown;
	}

	private void handleMouseCommon(PInputEvent e) {
		boolean ctrlPressed = verifyCtrlIsPressed(e);
		boolean shiftPressed = 0 != (e.getModifiersEx() & InputEvent.SHIFT_DOWN_MASK);
		TextlineNode textLine = (TextlineNode) box;
		textLine.unbold();

		// IMPORTANT: enter add boundary state if shift is down and there are boundaries to place
		TextlineModel tlm = textLine.getModel();
		if(shiftPressed && tlm.getWordOffsets().size() < tlm.getMaxBoundaries(null)) {
			state = TEXTLINE_ADD_BOUNDARY;
		}
		
		// IMPORTANT: call updateOffsets if we are in the 
		// handle and consume add boundary events (i.e. shadowing the cursor)
		if(state == TEXTLINE_ADD_BOUNDARY && shiftPressed) {
			// apply inverse transform
			Point2D p = textLine.getInverseRotateTransform().transform(e.getPosition(), null);
			// calculate the offset
			int p1offset = (int) (p.getX() - box.getBoxPts()[0].getX()); // IMPORTANT: MUST subtract out x
			textLine.showGhostAtOffset(p1offset);
			return;
		} else if(!shiftPressed) { // hide the ghost once shift is released
			textLine.hideGhost();
		}
		
		// handle and consume move boundary events
		if(handleBoundaryEvent(e) || state == TEXTLINE_MOVE_BOUNDARY) {
			textLine.updateOffsets();
			return; // TODO: handle the case right after boundary deletion: no longer near a boundary
		}
		
		CanvasDir hoverDir = CanvasDir.NONE;
		if (isNearRightHandle) {
			textLine.boldRightHandle();
		} else if (isNearNorthHandle) {
			textLine.boldNorthHandle();
		} else {
			hoverDir = findDirection(e.getPosition());
		}
		boolean nearAHandle = isNearRightHandle || isNearNorthHandle;
		if (isLeftClicked(e)){
			if (nearAHandle) {
				computeAndSetAngle();
			} else {
				if (!(currDir == CanvasDir.NONE || currDir == CanvasDir.INTERIOR)) {
					//System.out.println("Entering resize state"); // DEBUG
					state = TEXTLINE_RESIZING;
					resizeBox(currDir);
				// XXX: Ctrl is completely disabled for events inside the box
				} else if(currDir == CanvasDir.INTERIOR && !ctrlPressed) {
					//System.out.println("Entering shift state"); // DEBUG
					state = TEXTLINE_SHIFTING;
					shift();
				} else { // currDir == CanvasDir.NONE
					//System.out.println("Resetting state to TEXTLINE_DEFAULT"); // DEBUG
					state = TEXTLINE_DEFAULT;
				}
			}
		}
		if (!nearAHandle) {
			box.bold(hoverDir);
		}
	}
	
	/**
	 * Checks if this mouse event is near an offset handle 
	 * and moves it if necessary.
	 * 
	 * @param e the PInputEvent
	 * @return true if we are indeed near a boundary handle
	 */
	public boolean handleBoundaryEvent(PInputEvent e) {
		TextlineNode textLine = (TextlineNode) box;
		int index = -1;
		if(state == TEXTLINE_MOVE_BOUNDARY) index = dragStartedIndex; // use dragStartIndex if we're already dragging
		else index = isNearBoundary(e.getPosition()); // ONLY call isNearBoundary otherwise
		textLine.setSelectedOffset(index); // select the proper offset (-1 if none)
		
		// if we're dragging, update position
		if(state == TEXTLINE_MOVE_BOUNDARY && isLeftClicked(e)) {
			//System.out.println("Mouse CLICK event received near word boundary " + index); // DEBUG
			MathVector diff = new MathVector(origPressPoint, dragPoint);
			double diffWidth = MathVector.computeWidth(diff, angle); // change in x
			//System.out.println("X change is " + diffWidth); // DEBUG
			int newX = dragStartedPosition + (int) diffWidth;
			
			ArrayList offsets = textLine.getModel().getWordOffsets();
			// add to the push list all of our neighbors that we are passing (built-in range checking)
			// NOTE: only one of these will ever happen at once because we are resetting the extreme
			//    values every time the direction is reversed
			for(int a = 1; index-a >= 0 && newX < ((Integer) offsets.get(index-a)).intValue(); a++)
				pushList.add(new Integer(index-a));
			for(int b = 0; index+b < offsets.size() && newX > ((Integer) offsets.get(index+b)).intValue(); b++)
				pushList.add(new Integer(index+b));
			
			// stop pushing if we have reversed directions by more than BOUNDARY_PUSH_THRESHOLD pixels
			if(newX > dragFarLeft + BOUNDARY_PUSH_THRESHOLD || newX < dragFarRight - BOUNDARY_PUSH_THRESHOLD) {
				dragFarLeft = dragFarRight = newX; // reset extreme values
				pushList.clear(); // empty the list of boundaries being pushed
			}
			
			// handle the snap zones
			// IMPORTANT: MUST be done AFTER checking for neighbors that are too close
			if(newX <= BOUNDARY_SNAP_ZONE) { newX = 0; /*resizeBox(CanvasDir.LEFT);*/ }
			else if(newX >= oboxWidth - BOUNDARY_SNAP_ZONE) newX = (int) oboxWidth;
			
			// update the indices of all boundaries that we are currently pushing
			textLine.updateWordOffsetAtIndex(index, newX); // IMPORTANT: update our own separately
			for(int i = 0; i < pushList.size(); i++) {
				textLine.updateWordOffsetAtIndex(((Integer) pushList.get(i)).intValue(), newX);
			}

			// update the extreme values--IMPORTANT: must be done last
			if(newX < dragFarLeft) dragFarLeft = newX;
			else if(newX > dragFarRight) dragFarRight = newX;
		}
		return (index != -1); // return whether or not we're near a boundary
	}
	
	/**
	 * Checks if this point is near an offset handle 
	 * and highlights it appropriately.
	 * 
	 * @param p the point to check
	 * @return index of boundary if we are near a boundary handle, -1 otherwise
	 */
	public int isNearBoundary(Point2D p) {
		TextlineNode textLine = (TextlineNode) box;
		int boundaryIndex = -1;
		ArrayList offsets = textLine.getModel().getWordOffsets();
		
		// translate the click into local coordinates--MUST do first
		//System.out.println("pre-transform offset = "+(p.getX()-box.getBoxPts()[0].getX())); // DEBUG
		Point2D pick = textLine.getInverseRotateTransform().transform(p, null);
		double p1 = pick.getX();
		double p2 = pick.getY();
		
		// check if we are near one of the handles
		if((boundaryIndex = textLine.getNearestBoundaryHandle(pick)) != -1) return boundaryIndex;
		else if(!inRangeOfInterest(p)) return -1; // return early if we're not AND also outside the box
		
		// the y-coordinates stay the same for all offsets
		// IMPORTANT: must use getBoxPts()[0]
		double y1 = box.getBoxPts()[0].getY();
		double y2 = y1 + box.getHeight();
		
		int index = -1;
		if(offsets.size() == 0) return -1;
		int p1offset = (int) (p1 - box.getBoxPts()[0].getX()); // IMPORTANT: MUST subtract out x
		//System.out.println("mouse at offset = "+p1offset); // DEBUG
		
		// don't select boundaries in the snap zone
		if(p1offset <= BOUNDARY_SNAP_ZONE || p1offset >= oboxWidth - BOUNDARY_SNAP_ZONE) return -1;
		
		// use a binary search to find the closest word boundary in O(log n) time
		int tmp = Collections.binarySearch(offsets, new Integer(p1offset));
		if(tmp >= 0) { // event occurred EXACTLY on one of the offsets (this basically NEVER happens)
			index = tmp;
		} else { // we get the insertion index from binarySearch, so we need to look at both neighbors and find the closer one
			index = -(tmp + 1); // binarySearch returns -insertion_index - 1
			if(index >= offsets.size()) { index--; // correct if we're on the last element
			} else if(index >= 1) { // can ONLY do this if index is at least 1
				if(Math.abs(((Integer) offsets.get(index-1)).intValue() - p1offset) < Math.abs(((Integer) offsets.get(index)).intValue() - p1offset))
					index--; // use the previous one if it's closer to where we clicked
			}
			//System.out.println("final index = "+index+"; offset = "+offsets.get(index)); // DEBUG
		}
		
		// the x-coordinates are both the same
		double x1 = box.getBoxPts()[0].getX() + ((Integer) offsets.get(index)).intValue();
		double x2 = x1;
		// compute the (squared) distance to the line
		double distSq = Line2D.ptSegDistSq(x1,y1,x2,y2,p1,p2);
		
		// if we're within BOUNDARY_RADIUS points of a boundary, then return its index
		if(distSq < Math.pow(BOUNDARY_RADIUS,2)) {
			boundaryIndex = index;
		}
		
		// linear search - deprecated
		/*for(int i = 0; i < offsets.size(); i++) {
			// the x-coordinates are both the same
			double x1 = box.getBoxPts()[0].getX() + ((Integer) offsets.get(i)).intValue();
			double x2 = x1;
			// compute the (squared) distance to the line
			double distSq = Line2D.ptSegDistSq(x1,y1,x2,y2,p1,p2);
			if(distSq < Math.pow(BOUNDARY_RADIUS,2)) {
				//System.out.println("P1: ("+x1+","+y1+")");
				//System.out.println("P2: ("+x2+","+y2+")");
				//System.out.println("Pick: ("+p1+","+p2+")");
				//System.out.println("Dist^2: "+distSq);
				boundaryIndex = i;
			}
		}*/
		return boundaryIndex;
	}

	public void mouseReleased(PInputEvent e) {
		boolean leftClick = (e.getModifiersEx() & InputEvent.BUTTON1_DOWN_MASK) != 0;
		if (!leftClick) return;
		
		dragPoint = e.getPosition();
		handleMouseCommon(e);
		dragStartedIndex = -1; // reset dragStartedIndex
		if(state != TEXTLINE_ADD_BOUNDARY) {
			//System.out.println("Resetting state to TEXTLINE_DEFAULT"); // DEBUG
			state = TEXTLINE_DEFAULT;
		}
	}

	public static double MIN_HEIGHT = 8.0;
	public static double MIN_WIDTH = 8.0;
	public void shift() {
		MathVector shift = new MathVector(origPressPoint, dragPoint);
		currPts[0].setLocation(origPts[0].getX() + shift.getX(), origPts[0]
				.getY()
				+ shift.getY());
		((TextlineNode) box).updateObox();
	}

	Point2D origPressPoint = new Point2D.Double();
	double origWidth, origHeight;

	// Right edge must have first point "above" second point
	// Thus, if you draw from the first point to the second, it should point
	// "down"
	private boolean rightOf(Line2D edge) {
		return edge.relativeCCW(dragPoint) == 1;
	}

	// Right edge must have first point "right of" second point
	// Thus, if you draw from the first point to the second, it should go "left"
	private boolean below(Line2D edge) {
		return edge.relativeCCW(dragPoint) == 1;
	}
	/**
	 * Computes the angle from the upper left hand corner of the obox and the
	 * drag point, and sets the orientation to that angle
	 *  
	 */
	private void computeAndSetAngle() {
		if (isNearRightHandle) {
			angle = MathVector.computeAngle(origPts[0], dragPoint);
			((TextlineNode) box).setAngleInRadians(angle);
		} else if (isNearNorthHandle) {
			angle = MathVector.computeAngle(origCenter, dragPoint) - (Math.PI/2);
			OboxRectangle referenceBox = new OboxRectangle(origCenter.getX(), origCenter.getY(), -oboxWidth/2, -oboxHeight/2, angle);
			currPts[0] = referenceBox.p[2];
			((TextlineNode) box).setAngleInRadians(angle);
		}
	}

	public void resizeBox(CanvasDir dir) {
		MathVector diff = new MathVector(origPressPoint, dragPoint);

		Point2D saveTopLeft = currPts[0];

		// Compute change to height and width
		double diffWidth = MathVector.computeWidth(diff, angle);
		double diffHeight = -MathVector.computeHeight(diff, angle);

		CanvasDir fixedPoint;
		// Zero out height and width, if they don't change
		if (dir == CanvasDir.BOTTOM_RIGHT || dir == CanvasDir.BOTTOM) {
			fixedPoint = CanvasDir.TOP_LEFT;
			if (dir == CanvasDir.BOTTOM)
				diffWidth = 0;
		} else if (dir == CanvasDir.TOP_RIGHT || dir == CanvasDir.RIGHT) {
			fixedPoint = CanvasDir.BOTTOM_LEFT;
			if (dir == CanvasDir.RIGHT)
				diffHeight = 0;
			else
				// compensates for change
				diffHeight = -diffHeight;
		} else if (dir == CanvasDir.TOP_LEFT || dir == CanvasDir.TOP) {
			fixedPoint = CanvasDir.BOTTOM_RIGHT;
			if (dir == CanvasDir.TOP)
				diffWidth = 0;
			else
				diffWidth = -diffWidth;
			diffHeight = -diffHeight;
		} else {
			fixedPoint = CanvasDir.TOP_RIGHT;
			if (dir == CanvasDir.LEFT)
				diffHeight = 0;
			diffWidth = -diffWidth;
		}
		if (isDrawnFromCenter()) {
			diffWidth *= 2;
			diffHeight *= 2;
			fixedPoint = CanvasDir.INTERIOR;
		}
		if(isConstrainingAspectRatio()) {
			double widthRatio = oboxWidth / origWidth;
			double heightRatio = oboxHeight / origHeight;
			if (widthRatio < heightRatio) {
				oboxWidth = origWidth * heightRatio;
			} else {
				oboxHeight = origHeight * widthRatio;
			}
		}
		
		// Update height
		oboxHeight = origHeight + diffHeight;
		if (oboxHeight < MIN_HEIGHT)
			oboxHeight = MIN_HEIGHT;
		// Update width
		oboxWidth = origWidth + diffWidth;
		if (oboxWidth < MIN_WIDTH)
			oboxWidth = MIN_WIDTH;

		fixTopLeftCorner(fixedPoint);

		// Now update info in the TextlineNode
		if(!(dir == CanvasDir.LEFT || dir == CanvasDir.TOP_LEFT || dir == CanvasDir.BOTTOM_LEFT)) diffWidth = 0;
		((TextlineNode) box).setWidthAndHeight(oboxWidth, oboxHeight, diffWidth);
	}

	private void fixTopLeftCorner(CanvasDir fixedPoint) {
		OboxRectangle referenceBox;
		if (fixedPoint == CanvasDir.BOTTOM_RIGHT) {
			referenceBox = new OboxRectangle(origPts[2].getX(), origPts[2]
					.getY(), -oboxWidth, -oboxHeight, angle);
			currPts[0] = referenceBox.p[2];
		} else if (fixedPoint == CanvasDir.BOTTOM_LEFT) {
			referenceBox = new OboxRectangle(origPts[3].getX(), origPts[3]
					.getY(), oboxWidth, -oboxHeight, angle);
			currPts[0] = referenceBox.p[3];
		} else if (fixedPoint == CanvasDir.TOP_RIGHT) {
			referenceBox = new OboxRectangle(origPts[1].getX(), origPts[1]
					.getY(), -oboxWidth, oboxHeight, angle);
			currPts[0] = referenceBox.p[1];
		} else if (fixedPoint == CanvasDir.INTERIOR) {
			referenceBox = new OboxRectangle(origCenter.getX(), origCenter.getY(), -oboxWidth/2, -oboxHeight/2, angle);
			currPts[0] = referenceBox.p[2];
		}
	}

	/**
	 * Checks if point is inside the box or the ROTATED point is on one of the boundary handles
	 */
	public boolean inRangeOfInterest(Point2D point) {
		TextlineNode textLine = (TextlineNode) box;
		Point2D trans = textLine.getInverseRotateTransform().transform(point, null);
		return nearRightHandle(point) || nearNorthHandle(point) || 
		       findDirection(point) != CanvasDir.NONE || textLine.getNearestBoundaryHandle(trans) != -1;
	}
}
