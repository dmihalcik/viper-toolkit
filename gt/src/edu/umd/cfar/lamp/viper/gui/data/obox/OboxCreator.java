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

import java.awt.*;
import java.awt.event.*;
import java.awt.geom.*;
import java.util.logging.*;

import viper.api.*;
import edu.umd.cfar.lamp.viper.geometry.*;
import edu.umd.cfar.lamp.viper.gui.canvas.*;
import edu.umd.cs.piccolo.event.*;
import edu.umd.cs.piccolo.nodes.*;

/**
 * @author clin
 *
 * To change the template for this generated type comment go to
 * Window>Preferences>Java>Code Generation>Code and Comments
 */
public class OboxCreator extends CanvasCreator {
	private static Logger logger = Logger.getLogger("edu.umd.cfar.lamp.viper.gui.canvas.datatypes");
	PPath obox; // local reference to base class
	PPath handle;

	Point2D pressPoint, dragPoint;
	Point2D[] polyline = null;

	static final int OBOX_INIT_DRAW = 0 ;
	static final int OBOX_FINISH_DRAW = 1 ;

	int currentState = OBOX_INIT_DRAW;
	Point2D[] oboxPts = new Point2D[ 5 ];
	Point2D[] handlePts = new Point2D[ 2 ];
	double angle = 0;
	int oboxWidth = 0, oboxHeight = 0;
	
	public OboxCreator( CreatorAssistant a, Attribute attr ) {
		super( a, attr );
		obox = new PPath() ;
		handle = new PPath() ;
		handle.setStrokePaint( Color.ORANGE ) ;
		handle.setStroke( new BasicStroke( 1.4f ) ) ;
		// This allows the obox and handle to stay together
		// If the obox is removed, so is the handle
		obox.addChild( handle ) ;
		
		displaySelected() ;
	}

	public void displaySelected()
	{
		Highlightable colorTable = getColorTable() ;

		obox.setStroke( colorTable.getSelectedStroke() ) ;
		obox.setStrokePaint( colorTable.getSelectedColor() ) ;
	}
	public String getName()
	{
		return "OBOX CREATOR" ;
	}

	public void reset() {
		currentState = OBOX_INIT_DRAW;
	}

	public void keyPressed(PInputEvent e) {
		super.keyPressed(e);
	}

	public void mousePressed(PInputEvent e) {
		super.mousePressed(e);
		pressPoint = e.getPosition();
		dragPoint = pressPoint;
		// This adds the obox to the canvas
		if ( currentState == OBOX_INIT_DRAW ) {
			getAssistant().addShape( obox ) ;
		}
		updateOboxDrawing((e.getModifiersEx() & MouseEvent.SHIFT_DOWN_MASK) != 0, false);
	}

	private void updateOboxDrawing(boolean constrain, boolean center) {
		if (currentState == OBOX_INIT_DRAW) {
			updateLine(constrain);
		} else {
			finishObox();
		}
	}

	public void mouseDragged(PInputEvent e) {
		super.mouseDragged(e);
		dragPoint = e.getPosition();
		updateOboxDrawing((e.getModifiersEx() & MouseEvent.SHIFT_DOWN_MASK) != 0, false);
	}

	public void mouseReleased(PInputEvent e) {
		super.mouseReleased(e);
		dragPoint = e.getPosition();
		if (currentState == OBOX_INIT_DRAW) {
			initObox(0 != (e.getModifiersEx() & MouseEvent.SHIFT_DOWN_MASK));
		} else {
			finishObox();
		}
		if ( currentState == OBOX_INIT_DRAW )
			currentState = OBOX_FINISH_DRAW ;
		else // currentState == OBOX_FINISH_DRAW
		{
			getAssistant().switchListener() ; // switch from creator to editor
			OrientedBox orientedBox
				= new OrientedBox( (int) oboxPts[ 0 ].getX(),
								   (int) oboxPts[ 0 ].getY(),
								   (int) oboxWidth, (int) oboxHeight, 
								   (int) Math.toDegrees(angle) ) ;
								   
			// Set attribute to have orientedBox as current value
			setAttrValueInMediator( orientedBox ) ;
			// Just in case we get back to this creator---shouldn't happen
			// though.
			currentState = OBOX_INIT_DRAW ;
		}
	}
	
	public void mouseMoved(PInputEvent e) {
		super.mouseMoved(e) ;
		if ( currentState == OBOX_INIT_DRAW )
			return ;
		dragPoint = e.getPosition();
		updateOboxDrawing((e.getModifiersEx() & MouseEvent.SHIFT_DOWN_MASK) != 0, false);
	}

   /**
    * The first phase of drawing is to draw the top edge of the obox.
    * This method is called to draw the top edge
    */
	private void updateLine(boolean constrain) {
		assert pressPoint != null;
		assert dragPoint != null;
		Point2D[] line = new Point2D.Double[2];
		line[0] = pressPoint;
		line[1] = dragPoint;
		if (constrain) {
			Point2D d = new Point2D.Double(line[1].getX() - line[0].getX(), line[1].getY()-line[0].getY());
			double angle = Util.snapAngleToMajorDirection(Math.atan2(d.getX(), d.getY()));
			angle += Math.PI / 2;
			AffineTransform.getRotateInstance(angle).transform(d, d);
			d.setLocation(d.getX(), 0);
			AffineTransform.getRotateInstance(-angle).transform(d, d);
			d.setLocation(d.getX() + line[0].getX(), d.getY() + line[0].getY());
			line[1] = d;
		}

		obox.setPathToPolyline(line);
	}

	/**
	 * Once the mouse has been released, this draws the top edge
	 * and a small part of the left edge.  After this method, we switch
	 * to the second phase of drawing, where the user sets the height
	 * of the obox.
	 */
	public void initObox(boolean constrain) {
		// effectively multiplying diffY by -1, because as y increases
		// in pixels it goes down, but in normal graphs y increases go "up"
		// This compensates for it.
		assert pressPoint != null;
		assert dragPoint != null;
		if (constrain) {
			Point2D d = new Point2D.Double(dragPoint.getX() - pressPoint.getX(), dragPoint.getY()-pressPoint.getY());
			angle = Util.snapAngleToMajorDirection(MathVector.computeAngle( pressPoint, dragPoint ));
			AffineTransform.getRotateInstance(angle).transform(d, d);
			oboxWidth = (int) d.getX();
		} else {
			angle = MathVector.computeAngle( pressPoint, dragPoint );
			oboxWidth = (int) pressPoint.distance(dragPoint);
		}
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

		obox.setPathToPolyline(localOboxPts);
		
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
			obox.setPathToPolyline(oboxPts);
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



