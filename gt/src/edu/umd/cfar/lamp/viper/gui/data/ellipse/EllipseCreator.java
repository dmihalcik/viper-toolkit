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
import edu.umd.cfar.lamp.viper.geometry.*;
import edu.umd.cfar.lamp.viper.gui.canvas.*;
import edu.umd.cfar.lamp.viper.gui.data.obox.*;
import edu.umd.cs.piccolo.event.*;
import edu.umd.cs.piccolo.nodes.*;

/**
 * @author clin
 *
 * To change the template for this generated type comment go to
 * Window>Preferences>Java>Code Generation>Code and Comments
 */
public class EllipseCreator extends CanvasCreator {
	private static Logger logger = Logger.getLogger("edu.umd.cfar.lamp.viper.gui.canvas.datatypes");
	PPath ellipse; // local reference to base class
	PPath handle;
	Attribute attr ;

	Point2D pressPoint, dragPoint;
	Point2D[] polyline = null;

	static final int ELLIPSE_INIT_DRAW = 0 ;
	static final int ELLIPSE_FINISH_DRAW = 1 ;

	int currentState = ELLIPSE_INIT_DRAW;
	Point2D[] ellipsePts = new Point2D[ 5 ];
	Point2D[] handlePts = new Point2D[ 2 ];
	double angle = 0;
	int ellipseWidth = 0, ellipseHeight = 0;
	
	public EllipseCreator( CreatorAssistant a, Attribute attr ) {
		super( a, attr );
		ellipse = new PPath() ;
		handle = new PPath() ;
		handle.setStrokePaint( Color.ORANGE ) ;
		handle.setStroke( new BasicStroke( 1.4f ) ) ;
		// This allows the obox and handle to stay together
		// If the obox is removed, so is the handle
		ellipse.addChild( handle ) ;
		displaySelected() ;
	}

	public String getName()
	{
		return "ELIIPSE CREATOR" ;
	}

	public void displaySelected()
	{
		Highlightable colorTable = getColorTable() ;

		ellipse.setStroke( colorTable.getSelectedStroke() ) ;
		ellipse.setStrokePaint( colorTable.getSelectedColor() ) ;
	}
	
	public void reset() {
		currentState = ELLIPSE_INIT_DRAW;
	}
	
	public void keyPressed(PInputEvent e) {
		super.keyPressed(e);
	}

	public void mousePressed(PInputEvent e) {
		super.mousePressed(e);
		pressPoint = e.getPosition();
		dragPoint = pressPoint;
		// This adds the obox to the canvas
		if ( currentState == ELLIPSE_INIT_DRAW )
			getAssistant().addShape( ellipse ) ;
		updateOboxDrawing();
	}

	private void updateOboxDrawing() {
		if (currentState == ELLIPSE_INIT_DRAW)
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
		if (currentState == ELLIPSE_INIT_DRAW)
			initEllipse();
		else
			finishObox();
		if ( currentState == ELLIPSE_INIT_DRAW )
		{
			currentState = ELLIPSE_FINISH_DRAW ;
			// Draws ellipse
			ellipse.addChild( actualEllipse ) ;
		}
		else // currentState == OBOX_FINISH_DRAW
		{
			getAssistant().switchListener() ; // switch from creator to editor
			Ellipse ellipse
				= new Ellipse( (int) ellipsePts[ 0 ].getX(),
								(int) ellipsePts[ 0 ].getY(),
								(int) ellipseWidth, (int) ellipseHeight, 
								(int) Math.toDegrees(angle) ) ;
								   
			// Set attribute to have orientedBox as current value
			setAttrValueInMediator( ellipse ) ;
			// Just in case we get back to this creator---shouldn't happen
			// though.
			currentState = ELLIPSE_INIT_DRAW ;
		}
	}
	
	public void mouseMoved(PInputEvent e) {
		super.mouseMoved(e) ;
		if ( currentState == ELLIPSE_INIT_DRAW )
			return ;
		dragPoint = e.getPosition();
		updateOboxDrawing() ;
	}
   /**
	* The first phase of drawing is to draw the top edge of the obox.
	* This method is called to draw the top edge
	*/
	private void updateLine() {
		Point2D[] line = new Point2D.Double[2];
		line[0] = pressPoint;
		line[1] = dragPoint;
		ellipse.setPathToPolyline(line);
	}


	/**
	 * Once the mouse has been released, this draws the top edge
	 * and a small part of the left edge.  After this method, we switch
	 * to the second phase of drawing, where the user sets the height
	 * of the obox.
	 */
	public void initEllipse() {
		// effectively multiplying diffY by -1, because as y increases
		// in pixels it goes down, but in normal graphs y increases go "up"
		// This compensates for it.
		angle = MathVector.computeAngle( pressPoint, dragPoint );
		ellipseWidth = (int) pressPoint.distance(dragPoint);
		OboxRectangle r =
			new OboxRectangle(
				(int) pressPoint.getX(),
				(int) pressPoint.getY(),
				ellipseWidth,
				20,
				angle);
		// Only draw top edge and 20 pixels of the left edge
		Point2D [] localOboxPts = new Point2D[3];
		localOboxPts[0] = r.p[3];
		localOboxPts[1] = r.p[0];
		localOboxPts[2] = r.p[1];

		ellipse.setPathToPolyline(localOboxPts);
		
		// Set obox points so far, which is basically just the top edge
		ellipsePts[ 0 ] = r.p[ 0 ] ;
		ellipsePts[ 1 ] = r.p[ 1 ] ;
		ellipseHeight = 20 ; // arbitrary choice for now
	}

	
	int THRESHOLD = 3;
	private void finishObox() {
		if (isBeneathTopEdgeOfObox(dragPoint)) {
			Line2D top = new Line2D.Double(ellipsePts[0], ellipsePts[1]);
			int newHeight = (int) top.ptLineDist(dragPoint);
			if (newHeight < THRESHOLD)
				newHeight = THRESHOLD;

			// Draw obox
			OboxRectangle r =
				new OboxRectangle(
					ellipsePts[0].getX(),
					ellipsePts[0].getY(),
					ellipseWidth,
					newHeight,
					angle);
			ellipsePts = new Point2D[5];
			ellipsePts[0] = r.p[0];
			ellipsePts[1] = r.p[1];
			ellipsePts[2] = r.p[2];
			ellipsePts[3] = r.p[3];
			ellipsePts[4] = r.p[0];
			ellipse.setPathToPolyline(ellipsePts);
			ellipseHeight = newHeight ;
			
			// Draw handle
			OboxRectangle r2 =
				new OboxRectangle(
					ellipsePts[1].getX(),
					ellipsePts[1].getY(),
					20.0,
					15.0,
					angle);
			handlePts = new Point2D[2];
			handlePts[0] = r2.p[0];
			handlePts[1] = r2.p[1];
			handle.setPathToPolyline(handlePts);
			// Update the interior ellipse
			updateEllipse() ;
		} 
	}

	/**
	 * Is the drag point beneath the top edge of the obox?
	 * @param dragPoint 
	 * @return
	 */
	private boolean isBeneathTopEdgeOfObox(Point2D dragPoint) {
		Line2D line = new Line2D.Double(ellipsePts[0], ellipsePts[1]);
		int dir =
			Line2D.relativeCCW(
				ellipsePts[0].getX(),
				ellipsePts[0].getY(),
				ellipsePts[1].getX(),
				ellipsePts[1].getY(),
				dragPoint.getX(),
				dragPoint.getY());
		return dir == -1 ? true : false;
	}
	private AffineTransform getTransformForRotate() 
	{
		double x = ellipsePts[ 0 ].getX() ;
		double y = ellipsePts[ 0 ].getY() ;
		AffineTransform transFrom = AffineTransform.getTranslateInstance(-x, -y) ;
		AffineTransform rotate = AffineTransform.getRotateInstance( -angle ) ;
		AffineTransform transBack = AffineTransform.getTranslateInstance(x, y) ;
		rotate.concatenate( transFrom ) ;
		transBack.concatenate( rotate ) ;
	  return transBack ;
	}

	Ellipse2D localEllipse = new Ellipse2D.Double() ;
	PPath actualEllipse = new PPath() ;
	private void updateEllipse()
	{
		localEllipse.setFrame( ellipsePts[ 0 ].getX(), ellipsePts[0].getY(),
							  ellipseWidth, ellipseHeight ) ;
		Ellipse2D copy = (Ellipse2D) localEllipse.clone() ;
		AffineTransform trans = getTransformForRotate() ;
		Shape s = trans.createTransformedShape( copy ) ;
		actualEllipse.setPathTo( s ) ;
	}

}



