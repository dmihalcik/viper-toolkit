/*
 * Created on Mar 2, 2004
 *
 * To change the template for this generated file go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
package edu.umd.cfar.lamp.viper.gui.data.ellipse;

import java.awt.event.*;
import java.awt.geom.*;
import java.util.logging.*;

import edu.umd.cfar.lamp.viper.gui.canvas.*;
import edu.umd.cfar.lamp.viper.gui.canvas.datatypes.*;
import edu.umd.cfar.lamp.viper.gui.data.obox.*;
import edu.umd.cs.piccolo.event.*;
import edu.umd.cs.piccolo.nodes.*;

/**
 * @author clin
 *
 * To change the template for this generated type comment go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
public class EllipseCanvasEditor extends CanvasEditor {
	private static Logger logger = Logger.getLogger("edu.umd.cfar.lamp.viper.gui.canvas.datatypes");
	PPath obox; // local reference to base class
	PPath handle;
	Point2D[] oboxPts;
	Point2D[] handlePts;
	double angle ;
	double oboxWidth, oboxHeight ;
	EllipseNode ellipseNode ;
	PPath debugLine = new PPath() ;
	PPath debugObox = new PPath() ;
	public EllipseCanvasEditor(
		Attributable attrIn ) {
			// *********************************
		super( attrIn ) ;
		ellipseNode = (EllipseNode) attrIn ;
		logger.fine("Created ELLIPSE EDITOR");

		// Local copy of information stored in oboxNode
		oboxPts = ellipseNode.getOboxPts() ;
		handlePts = ellipseNode.getHandlePts() ;
		oboxWidth = ellipseNode.getOboxWidth() ;
		oboxHeight = ellipseNode.getOboxHeight() ;
		angle = ellipseNode.getAngleInRadians() ;
		// for debugging purposes
//		debugLine.setStrokePaint( Color.MAGENTA ) ;
//		debugLine.setStroke( new BasicStroke( 3.0f )) ;
//		oboxNode.addChild( debugLine ) ;
//		
//		debugObox.setStrokePaint( Color.MAGENTA ) ;
//		debugObox.setStroke( new BasicStroke( 3.0f )) ;
//		ellipseNode.addChild( debugObox ) ;

		// Make sure the bounding box for the ellipse does NOT appear
		doWhenUnselected() ;
		
		// Just initializing the obox
		for ( int i = 0 ; i < 4 ; i++ )
			origOboxPts[ i ] = new Point2D.Double() ;
			

	}
	
	private void resetDebugLine()
	{
//		Point2D [] list = new Point2D[ 1 ] ;
//		list[ 0 ] = oboxPts[ 0 ] ;
//		// This line is basically empty
//		debugLine.setPathToPolyline( list ) ;
	}
	
	private void setDebugLine( Point2D newPoint )
	{
//		Point2D [] list = new Point2D[ 2 ] ;
//		list[ 0 ] = origTopLeft ;
//		list[ 1 ] = newPoint ;
//		debugLine.setPathToPolyline( list ) ;
	}
	
	public String getName() {
		return "EllipseEditor";
	}

	Point2D pressPoint, dragPoint;

	public double minDist(Point2D select) {
		Point2D upperLeft = oboxPts[ 0 ];
		Point2D upperRight = oboxPts[ 1 ];
		Point2D lowerRight = oboxPts[ 2 ];
		Point2D lowerLeft = oboxPts[ 3 ];

		// Create lines
		Line2D top = new Line2D.Double(upperLeft, upperRight);
		Line2D left = new Line2D.Double(upperLeft, lowerLeft);
		Line2D right = new Line2D.Double(upperRight, lowerRight);
		Line2D bottom = new Line2D.Double(lowerLeft, lowerRight);
		Line2D handle = new Line2D.Double( handlePts[ 0 ], handlePts[ 1 ] ) ;
		double min = top.ptSegDist(select);
		if (left.ptSegDist(select) < min)
			min = left.ptSegDist(select);
		if (right.ptSegDist(select) < min)
			min = right.ptSegDist(select);
		if (bottom.ptSegDist(select) < min)
			min = bottom.ptSegDist(select);
		if ( handle.ptSegDist(select) < min)
			min = handle.ptSegDist( select ) ;

		return min;
	}
	// Should be fixed
	public boolean contains(Point2D select) {
		boolean result = true ;
		for ( int i = 0 ; i < 4 ; i ++ )
			result = result && isRightOf( oboxPts[ i ], oboxPts[ (i + 1) % 4 ], select ) ;
		return result ;
	}

	/**
	 * 
	 * @param first    Tail of a vector
	 * @param second   Head of a vector
	 * @param select   Point is right of it?
	 * @return true if it's right, false if not
	 */
	boolean isRightOf( Point2D first, Point2D second, Point2D select )
	{
		Line2D edge = new Line2D.Double( first, second ) ;
		return edge.relativeCCW( select ) == -1 ;
	}
	public void keyPressed(PInputEvent e) {

	}

	boolean isNearHandle = false ;
	CanvasDir currDir = CanvasDir.NONE ;

	private static int LEFT_MOUSE_MASK = InputEvent.BUTTON1_MASK ;
	
	public void mouseMoved(PInputEvent e)
	{
		isNearHandle = nearHandle(e.getPosition()) ;
		if (isNearHandle)
		{
			ellipseNode.boldHandle() ;
		}
		else
		{
			CanvasDir localDir = findDirection( e.getPosition() ) ;
//			System.out.println( localDir ) ;
			ellipseNode.bold( localDir ) ;
		}
		
	}
	
	public void doWhenUnselected()
	{
		// XXX Hack fix for npe. not sure why this code is being called in the first place
		if (ellipseNode != null) {
			ellipseNode.unbold() ;
			ellipseNode.removeBoundingBox() ;
		}
	}
	// Overrides base class method
	public void doWhenSelected()
	{
		if (ellipseNode != null) {
			ellipseNode.addBoundingBox() ;
		}
	}
	// Records the orignal obox at time of mouse press
	// Used for computing the adjusted box
	Point2D origTopLeft = new Point2D.Double() ;
	Point2D [] origOboxPts = new Point2D.Double[ 4 ] ;
	
	public void mousePressed(PInputEvent e) {
		boolean leftClick = 0 != (e.getModifiersEx() & InputEvent.BUTTON1_DOWN_MASK);
		if (!leftClick) return;
		
		logger.fine( "EllipseCanvasEditor: mousePressed" ) ;
		pressPoint = e.getPosition();
		dragPoint = pressPoint ; // need this for computeAndSetAngle()
		
		
		// Record if press point is near the handle
		
		// debugging
//		OboxRectangle shortHeight
//		  = new OboxRectangle( oboxPts[ 0 ].getX(), oboxPts[ 0 ].getY(),
//							   oboxWidth, oboxHeight - MIN_HEIGHT, angle ) ;
//		OboxRectangle shortWidth 
//		  = new OboxRectangle( oboxPts[ 0 ].getX(), oboxPts[ 0 ].getY(),
//							   oboxWidth - MIN_WIDTH, oboxHeight, angle ) ;
//		
//		OboxRectangle testBox
//		  = new OboxRectangle( oboxPts[ 3 ].getX(), oboxPts[ 3 ].getY(),
//							   oboxWidth, -(oboxHeight - MIN_HEIGHT), angle ) ;
//							   
//		OboxRectangle foo = testBox ;
//		Point2D [] testArr = new Point2D[ 3 ] ;
//		for ( int i = 0 ; i < 3 ; i++ )
//			testArr[ i ] = foo.p[ i ] ;
//		
//		debugObox.setPathToPolyline( testArr ) ;
//		
//		topLeftMinHeight = shortHeight.p[ 3 ] ;		
//		topLeftMinWidth = shortWidth.p[ 1 ] ;

		isNearHandle = nearHandle(pressPoint) ;
		if (isNearHandle)
		{
			logger.fine("Near handle");
			computeAndSetAngle();
		}
		else
		{
			currDir = findDirection( pressPoint ) ;
			ellipseNode.bold( currDir ) ;
			logger.fine( "Direction is " + currDir ) ;
			
			// Record original top left
			origTopLeft = new Point2D.Double() ;
			origTopLeft.setLocation( oboxPts[ 0 ] ) ;

			// Record original top left
			for ( int i = 0 ; i < 4 ; i++ )
			{
				origOboxPts[ i ].setLocation( oboxPts[ i ] ) ;
			}
			origWidth = oboxWidth ;
			origHeight = oboxHeight ;
			origPressPoint.setLocation( pressPoint ) ;
		}
	}

	public boolean nearHandle(Point2D pt) {
		double dist = pt.distance(handlePts[1]);
//		System.out.println(
//			"press point = " + pt + "  handle = " + handlePts[1]);
		return (dist < 5);
	}

	public void mouseDragged(PInputEvent e) {
		boolean leftClick = 0 != (e.getModifiersEx() & InputEvent.BUTTON1_DOWN_MASK);
		if (!leftClick) return;
		
//		oboxNode.unbold() ;
		dragPoint = e.getPosition();
		handleMouseCommon();
	}

	private void handleMouseCommon() {
		if (isNearHandle)
		{
			ellipseNode.boldHandle() ;
			computeAndSetAngle();
		}
		else 
		{
			ellipseNode.bold( currDir ) ;
			if ( ! ( currDir == CanvasDir.NONE || 
					 currDir == CanvasDir.INTERIOR ) )
				resizeObox( currDir ) ;
			else
				shift() ;
		}
	}

	public void mouseReleased(PInputEvent e) {
		boolean leftClick = 0 != (e.getModifiersEx() & InputEvent.BUTTON1_DOWN_MASK);
		if (!e.isLeftMouseButton()) {
			return;
		}
		
		dragPoint = e.getPosition();
		handleMouseCommon() ;
	}
	
	private int CORNER_THRESHOLD = 7 ;
	private int SIDE_THRESHOLD = 7 ;
	private CanvasDir findDirection( Point2D refPoint )
	{
		if ( refPoint.distance( oboxPts[0] ) < CORNER_THRESHOLD )
			return CanvasDir.TOP_LEFT ;
		else if ( refPoint.distance( oboxPts[1] ) < CORNER_THRESHOLD )
			return CanvasDir.TOP_RIGHT ;
		else if ( refPoint.distance( oboxPts[2] ) < CORNER_THRESHOLD )
			return CanvasDir.BOTTOM_RIGHT ;
		else if ( refPoint.distance( oboxPts[3] ) < CORNER_THRESHOLD )
			return CanvasDir.BOTTOM_LEFT ;
		else
		{
			Line2D top = new Line2D.Double( oboxPts[0], oboxPts[1] ) ;
			Line2D right = new Line2D.Double( oboxPts[1], oboxPts[2] ) ;
			Line2D bottom = new Line2D.Double( oboxPts[2], oboxPts[3] ) ;
			Line2D left = new Line2D.Double( oboxPts[3], oboxPts[0] ) ;
			if ( top.ptSegDist( refPoint ) < SIDE_THRESHOLD )
				return CanvasDir.TOP ;
			else if ( right.ptSegDist( refPoint ) < SIDE_THRESHOLD )
				return CanvasDir.RIGHT ;
			else if ( left.ptSegDist( refPoint ) < SIDE_THRESHOLD )
				return CanvasDir.LEFT ;
			else if ( bottom.ptSegDist( refPoint ) < SIDE_THRESHOLD )
				return CanvasDir.BOTTOM ;
			if ( contains( refPoint ) )
				return CanvasDir.INTERIOR ;
		}

		return CanvasDir.NONE ;		
	}
	
	public static double MIN_HEIGHT = 8.0 ;
	public static double MIN_WIDTH = 8.0 ;
	public void shift()
	{
		MathVector shift = new MathVector( origPressPoint, dragPoint ) ;
		oboxPts[ 0 ].setLocation( origOboxPts[ 0 ].getX() + shift.getX(),
								  origOboxPts[ 0 ].getY() + shift.getY() ) ;
		ellipseNode.updateObox() ;
	}

	Point2D origPressPoint = new Point2D.Double() ;
	double origWidth, origHeight ;

	// Right edge must have first point "above" second point
	// Thus, if you draw from the first point to the second, it should point "down"
	private boolean rightOf( Line2D edge )
	{
		return edge.relativeCCW( dragPoint ) == 1 ;
	}
	
	// Right edge must have first point "right of" second point
	// Thus, if you draw from the first point to the second, it should go "left"
	private boolean below( Line2D edge )
	{
		return edge.relativeCCW( dragPoint ) == 1 ;
	}
	/**
	 * Computes the angle from the upper left hand corner of the obox
	 * and the drag point, and sets the orientation to that angle
	 *
	 */
	private void computeAndSetAngle() {
		angle = MathVector.computeAngle( oboxPts[ 0 ], dragPoint ) ;
		ellipseNode.setAngleInRadians( angle ) ;
	}
	
	public void resizeObox( CanvasDir dir )
	{
		MathVector diff = new MathVector( origPressPoint, dragPoint ) ;
		// debugging
		resetDebugLine() ;
		
		Point2D saveTopLeft = oboxPts[ 0 ] ;
		
		// Compute change to height and width
		double diffWidth = MathVector.computeWidth( diff, angle ) ;
		double diffHeight = -MathVector.computeHeight( diff, angle ) ;

		CanvasDir fixedPoint ;
		// Zero out height and width, if they don't change
		if ( dir == CanvasDir.BOTTOM_RIGHT || dir == CanvasDir.BOTTOM )
		{
			fixedPoint = CanvasDir.TOP_LEFT ;
			if ( dir == CanvasDir.BOTTOM )
				diffWidth = 0 ;
		}
		else if ( dir == CanvasDir.TOP_RIGHT || dir == CanvasDir.RIGHT )
		{
			fixedPoint = CanvasDir.BOTTOM_LEFT ;
			if ( dir == CanvasDir.RIGHT )
				diffHeight = 0 ;
			else // compensates for change
				diffHeight = -diffHeight ;
		}
		else if ( dir == CanvasDir.TOP_LEFT || dir == CanvasDir.TOP )
		{
			fixedPoint = CanvasDir.BOTTOM_RIGHT ;
			if ( dir == CanvasDir.TOP )
				diffWidth = 0 ;
			else
				diffWidth = - diffWidth ;
			diffHeight = -diffHeight ;
		}
		else
		{
			fixedPoint = CanvasDir.TOP_RIGHT ;
			if ( dir == CanvasDir.LEFT )
				diffHeight = 0 ;
			diffWidth = -diffWidth ;				
		}
		// Update height
		oboxHeight = origHeight + diffHeight ;	
		if ( oboxHeight < MIN_HEIGHT )
			oboxHeight = MIN_HEIGHT ;
		// Update width
		oboxWidth = origWidth + diffWidth ;
		if ( oboxWidth < MIN_WIDTH )
			oboxWidth = MIN_WIDTH ;
						
		fixTopLeftCorner( fixedPoint, debugObox ) ;
		
		// Now update info in the oboxNode
		ellipseNode.setWidthAndHeight( oboxWidth, oboxHeight ) ;
	}

	private void fixTopLeftCorner( CanvasDir fixedPoint, PPath debugObox )
	{
		OboxRectangle referenceBox ;
		if ( fixedPoint == CanvasDir.BOTTOM_RIGHT )
		{
			referenceBox	
			= new OboxRectangle( origOboxPts[ 2 ].getX(), origOboxPts[ 2 ].getY(),
								 -oboxWidth, -oboxHeight, angle ) ;
			oboxPts[ 0 ] = referenceBox.p[ 2 ] ;
		}
		else if ( fixedPoint == CanvasDir.BOTTOM_LEFT )
		{
			referenceBox	
			= new OboxRectangle( origOboxPts[ 3 ].getX(), origOboxPts[ 3 ].getY(),
								 oboxWidth, -oboxHeight, angle ) ;
			oboxPts[ 0 ] = referenceBox.p[ 3 ] ;
		}
		else if ( fixedPoint == CanvasDir.TOP_RIGHT )
		{
			referenceBox	
			= new OboxRectangle( origOboxPts[ 1 ].getX(), origOboxPts[ 1 ].getY(),
								 -oboxWidth, oboxHeight, angle ) ;
			oboxPts[ 0 ] = referenceBox.p[ 1 ] ;
		}
	}

	/* (non-Javadoc)
	 * @see edu.umd.cfar.lamp.viper.gui.canvas.CanvasEditor#inRangeOfInterest(java.awt.geom.Point2D)
	 */
	public boolean inRangeOfInterest(Point2D point) {
		return findDirection( point ) != CanvasDir.NONE ;
	}

}
