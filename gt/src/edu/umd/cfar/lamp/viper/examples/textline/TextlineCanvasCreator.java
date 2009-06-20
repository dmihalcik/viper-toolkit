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

/**
 * Heavily based on @{link edu.umd.cfar.lamp.viper.gui.data.obox.OboxCanvasCreator}.
 * 
 * @author spikes51@umiacs.umd.edu
 * @since Feb 25, 2005
 *
 */

import java.awt.*;
import java.awt.geom.*;
import java.util.logging.*;

import viper.api.*;
import edu.umd.cfar.lamp.viper.gui.canvas.*;
import edu.umd.cfar.lamp.viper.gui.data.obox.*;
import edu.umd.cs.piccolo.event.*;
import edu.umd.cs.piccolo.nodes.*;

public class TextlineCanvasCreator extends CanvasCreator {
	private static Logger logger = Logger.getLogger("edu.umd.cfar.lamp.viper.gui.canvas.datatypes");
	PPath textline; // local reference to base class
	PPath handle;

	Point2D pressPoint, dragPoint;
	Point2D[] polyline = null;

	// available states
	static final int TEXTLINE_INIT_DRAW = 0;
	static final int TEXTLINE_FINISH_DRAW = 1;
	static final int TEXTLINE_ALL_FINISHED = 3;

	int currentState = TEXTLINE_INIT_DRAW;
	Point2D[] oboxPts = new Point2D[ 5 ];
	Point2D[] handlePts = new Point2D[ 2 ];
	double angle = 0;
	int oboxWidth = 0, oboxHeight = 0;
	
	public TextlineCanvasCreator( CreatorAssistant a, Attribute attr ) {
		super( a, attr );
		textline = new PPath() ;
		handle = new PPath() ;
		handle.setStrokePaint( Color.ORANGE ) ;
		handle.setStroke( new BasicStroke( 1.4f ) ) ;
		// This allows the obox and handle to stay together
		// If the obox is removed, so is the handle
		textline.addChild( handle ) ;
		
		displaySelected() ;
	}

	public void displaySelected()
	{
		Highlightable colorTable = getColorTable() ;

		textline.setStroke( colorTable.getSelectedStroke() ) ;
		textline.setStrokePaint( colorTable.getSelectedColor() ) ;
	}
	public String getName()
	{
		return "TextlineCanvasCreator" ;
	}

	public void reset() {
		currentState = TEXTLINE_INIT_DRAW;
	}

	public void keyPressed(PInputEvent e) {
		super.keyPressed(e);
	}

	public void mousePressed(PInputEvent e) {
		super.mousePressed(e);
		pressPoint = e.getPosition();
		dragPoint = pressPoint;
		// This adds the obox to the canvas
		if ( currentState == TEXTLINE_INIT_DRAW ) {
			getAssistant().addShape( textline ) ;
		}
		updateOboxDrawing();
	}

	private void updateOboxDrawing() {
		if (currentState == TEXTLINE_INIT_DRAW)
			updateLine();
		else
			finishObox();
	}

	public void mouseDragged(PInputEvent e) {
		super.mouseDragged(e);
		dragPoint = e.getPosition();
		updateOboxDrawing() ;
	}

	public void mouseReleased(PInputEvent e) {
		super.mouseReleased(e);
		dragPoint = e.getPosition();
		if (currentState == TEXTLINE_INIT_DRAW) {
			initObox();
			currentState = TEXTLINE_FINISH_DRAW ;
		} else if(currentState == TEXTLINE_FINISH_DRAW) {
			finishObox();
			currentState = TEXTLINE_ALL_FINISHED;
		}
		
		// IMPORTANT: NOT if else, because then it would only be executed on the next mouseReleased
		if(currentState == TEXTLINE_ALL_FINISHED) {
			
			Attribute at = getAttribute();
			TextlineModel tlm = (TextlineModel) ((AttributeWrapperTextline) at.getAttrConfig().getParams()).getMetaDefault(at);
			getAssistant().switchListener() ; // switch from creator to editor
			tlm = new TextlineModel( (int) oboxPts[ 0 ].getX(),
					   (int) oboxPts[ 0 ].getY(),
					   (int) oboxWidth, (int) oboxHeight, 
					   (int) Math.toDegrees(angle), "");
			tlm.setTextPointer(at);

			// Set attribute to have tlm as current value
			setAttrValueInMediator(tlm);
			// Just in case we get back to this creator---shouldn't happen though.
			currentState = TEXTLINE_INIT_DRAW ;
		}
	}
	
	public void mouseMoved(PInputEvent e) {
		super.mouseMoved(e) ;
		if ( currentState == TEXTLINE_INIT_DRAW )
			return ;
		dragPoint = e.getPosition();
		updateOboxDrawing() ;
	}

   /**
    * The first phase of drawing is to draw the top edge of the obox.
    * This method is called to draw the top edge
    */
	private void updateLine() {
		assert pressPoint != null;
		assert dragPoint != null;
		Point2D[] line = new Point2D.Double[2];
		line[0] = pressPoint;
		line[1] = dragPoint;
		textline.setPathToPolyline(line);
	}

	/**
	 * Once the mouse has been released, this draws the top edge
	 * and a small part of the left edge.  After this method, we switch
	 * to the second phase of drawing, where the user sets the height
	 * of the obox.
	 */
	public void initObox() {
		// effectively multiplying diffY by -1, because as y increases
		// in pixels it goes down, but in normal graphs y increases go "up"
		// This compensates for it.
		assert pressPoint != null;
		assert dragPoint != null;
		angle = MathVector.computeAngle( pressPoint, dragPoint );
		oboxWidth = (int) pressPoint.distance(dragPoint);
		OboxRectangle r =
			new OboxRectangle(
				(int) pressPoint.getX(),
				(int) pressPoint.getY(),
				oboxWidth,
				20,
				angle);
		// Only draw top edge and 20 pixels of the left edge
		Point2D [] localOboxPts = new Point2D[3];
		localOboxPts[0] = r.p[3];
		localOboxPts[1] = r.p[0];
		localOboxPts[2] = r.p[1];

		textline.setPathToPolyline(localOboxPts);
		
		// Set obox points so far, which is basically just the top edge
		oboxPts[ 0 ] = r.p[ 0 ] ;
		oboxPts[ 1 ] = r.p[ 1 ] ;
		oboxHeight = 20 ; // arbitrary choice for now
	}

	
	int THRESHOLD = 3;
	private void finishObox() {
		if (isBeneathTopEdgeOfObox(dragPoint)) {
			Line2D top = new Line2D.Double(oboxPts[0], oboxPts[1]);
			int newHeight = (int) top.ptLineDist(dragPoint);
			if (newHeight < THRESHOLD)
				newHeight = THRESHOLD;

			// Draw obox
			OboxRectangle r =
				new OboxRectangle(
					oboxPts[0].getX(),
					oboxPts[0].getY(),
					oboxWidth,
					newHeight,
					angle);
			oboxPts = new Point2D[5];
			oboxPts[0] = r.p[0];
			oboxPts[1] = r.p[1];
			oboxPts[2] = r.p[2];
			oboxPts[3] = r.p[3];
			oboxPts[4] = r.p[0];
			textline.setPathToPolyline(oboxPts);
			oboxHeight = newHeight ;
			
			// Draw handle
			OboxRectangle r2 =
				new OboxRectangle(
					oboxPts[1].getX(),
					oboxPts[1].getY(),
					20.0,
					15.0,
					angle);
			handlePts = new Point2D[2];
			handlePts[0] = r2.p[0];
			handlePts[1] = r2.p[1];
			handle.setPathToPolyline(handlePts);
		} 
	}

	/**
	 * Is the drag point beneath the top edge of the obox?
	 * @param dragPoint 
	 * @return
	 */
	private boolean isBeneathTopEdgeOfObox(Point2D dragPoint) {
		Line2D line = new Line2D.Double(oboxPts[0], oboxPts[1]);
		int dir =
			Line2D.relativeCCW(
				oboxPts[0].getX(),
				oboxPts[0].getY(),
				oboxPts[1].getX(),
				oboxPts[1].getY(),
				dragPoint.getX(),
				dragPoint.getY());
		return dir == -1 ? true : false;
	}
}
