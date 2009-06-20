/*
 * Created on Jan 20, 2004
 *
 * To change the template for this generated file go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
package edu.umd.cfar.lamp.viper.gui.data.circle;

import java.awt.event.*;
import java.awt.geom.*;
import java.util.logging.*;

import edu.umd.cfar.lamp.viper.gui.canvas.*;
import edu.umd.cfar.lamp.viper.gui.canvas.datatypes.*;
import edu.umd.cs.piccolo.event.*;
import edu.umd.cs.piccolo.nodes.*;

/**
 * @author clin
 *
 * To change the template for this generated type comment go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */

public class CircleCanvasEditor extends CanvasEditor {
	private static Logger logger = Logger.getLogger("edu.umd.cfar.lamp.viper.gui.canvas.datatypes");
	PPath base; // local reference to base class
	CircleNode circleNode ;
	
	public CircleCanvasEditor(
		Attributable attrIn ) {
			// *********************************
		super( attrIn ) ;
		circleNode = (CircleNode) attrIn ;
		logger.fine("Created CIRCLE EDITOR");
	}
	
	public String getName() {
		return "CircleEditor";
	}

	Point2D pressPoint, dragPoint, origCenter ;

	public double minDist(Point2D select) {
		Point2D location = circleNode.getCenter() ;
		double distFromCenter = location.distance( select ) ;
		double diff = Math.abs( distFromCenter - circleNode.getRadius() ) ;
		return diff ;
	}
	
	public boolean contains(Point2D select) {
		Point2D location = circleNode.getCenter() ;
		double distFromCenter = location.distance( select ) ;
		return distFromCenter < circleNode.getRadius() ;
	}

	public String toString()
	{
		return "CircleCanvasEditor: center = " + circleNode.getCenter()
				+ " radius = " + circleNode.getRadius() ;
	}
	public void keyPressed(PInputEvent e) {

	}

	private static int LEFT_MOUSE_MASK = InputEvent.BUTTON1_MASK ;

	public void doWhenUnselected()
	{
		if ( circleNode != null )
			circleNode.unbold() ;
		// nothing needs to happen
	}
	
	static int MOVE_MODE = 0 ;
	static int RESIZE_MODE = 1 ;
	static int currMode = MOVE_MODE ;
	boolean hasAdded = false ;

	public void mouseMoved(PInputEvent e) {
		Point2D localPoint = e.getPosition() ;
		origCenter = circleNode.getCenter() ;
		if ( inResizeRange( localPoint ) )
		{
			circleNode.showRadius( true ) ;
			circleNode.updateRadialLine( localPoint ) ;
		}
		else
		{
			circleNode.showRadius( false ) ;			
			if ( contains( localPoint ) )
				circleNode.bold() ;
			else
				circleNode.unbold() ;
		}
	}
	
	public void mousePressed(PInputEvent e) {
		boolean leftClick = 0 != (e.getModifiersEx() & InputEvent.BUTTON1_DOWN_MASK);
		if (!leftClick) return;
		logger.fine( "CircleCanvasEditor: mousePressed" ) ;
		pressPoint = e.getPosition() ;
		origCenter = circleNode.getCenter() ;
		if ( inResizeRange( pressPoint ) )
		{
			currMode = RESIZE_MODE ;
			circleNode.showRadius( true ) ;
			circleNode.updateRadialLine( pressPoint ) ;
		}
		else
		{
			currMode = MOVE_MODE ;
			circleNode.showRadius( false ) ;	
			circleNode.bold() ;		
		}
	}

	public void mouseDragged(PInputEvent e) {
		boolean leftClick = 0 != (e.getModifiersEx() & InputEvent.BUTTON1_DOWN_MASK);
		if (!leftClick) return;
		
		dragPoint = e.getPosition() ;
		int diffX = (int) (dragPoint.getX() - pressPoint.getX()) ;
		int diffY = (int) (dragPoint.getY() - pressPoint.getY()) ;
		Point2D newCenter = new Point2D.Double( origCenter.getX() + diffX, 
											  origCenter.getY() + diffY ) ;
											  
		if ( currMode == MOVE_MODE )
			mouseCommon( newCenter ) ;							
		else
		{
			int dist = (int) dragPoint.distance( origCenter ) ;
			circleNode.setRadius( dist ) ;
			circleNode.updateRadialLine( dragPoint ) ;
		}
	}

	public void mouseReleased(PInputEvent e) {
		if (!e.isLeftMouseButton()) {
			return;
		}
		
		dragPoint = e.getPosition() ;
		int diffX = (int) (dragPoint.getX() - pressPoint.getX()) ;
		int diffY = (int) (dragPoint.getY() - pressPoint.getY()) ;
		Point2D newCenter = new Point2D.Double( origCenter.getX() + diffX, 
											  origCenter.getY() + diffY ) ;
		
		if ( currMode == MOVE_MODE )
			mouseCommon( newCenter ) ;		
		else
		{
			int dist = (int) dragPoint.distance( origCenter ) ;
			circleNode.setRadius( dist ) ;
			circleNode.updateRadialLine( dragPoint ) ;
		}	
	}
	
	void mouseCommon( Point2D newCenter )
	{
		circleNode.setCenter( newCenter ) ;	
		circleNode.bold() ;
	}
	
	// The radius in the circleNode---may not be the current active radius
	public int getRadius()
	{
		return circleNode.getRadius() ;
	}
	
	public boolean inResizeRange( Point2D select )
	{
		int dist = (int) select.distance( origCenter ) ;
		return ( dist > getRadius() && dist < getRadius() + 5 ) ;
	}

	/* (non-Javadoc)
	 * @see edu.umd.cfar.lamp.viper.gui.canvas.CanvasEditor#inRangeOfInterest(java.awt.geom.Point2D)
	 */
	public boolean inRangeOfInterest(Point2D point) {
		return inResizeRange( point ) || contains( point ) ;
	}
}
