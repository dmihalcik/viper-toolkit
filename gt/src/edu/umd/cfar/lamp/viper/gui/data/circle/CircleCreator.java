/*
 * Created on Jan 22, 2004
 *
 * To change the template for this generated file go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
package edu.umd.cfar.lamp.viper.gui.data.circle;

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
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
public class CircleCreator extends CanvasCreator {
	private static Logger logger = Logger.getLogger("edu.umd.cfar.lamp.viper.gui.canvas.datatypes");
	
	Point2D dragPoint;
	PPath circle ;
	PPath refLine ;
	Point2D centerPoint ;		
	Point2D [] endPoints = new Point2D[ 2 ] ;
	int radius = 0 ;
	
	public CircleCreator( CreatorAssistant a, Attribute attr ) {
		super( a, attr );
		circle = new PPath() ;
		refLine = new PPath() ;
		circle.addChild( refLine ) ;
		displaySelected() ;
	}
	
	public void mousePressed(PInputEvent e) {
		super.mousePressed(e);

		centerPoint = e.getPosition();
		dragPoint = centerPoint;
		updateCircle() ;
		getAssistant().addShape( circle ) ;
	}

	public void displaySelected()
	{
		Highlightable colorTable = getColorTable() ;

		circle.setStroke( colorTable.getSelectedStroke() ) ;
		circle.setStrokePaint( colorTable.getSelectedColor() ) ;
		refLine.setStroke( colorTable.getSelectedStroke() ) ;
		refLine.setStrokePaint( colorTable.getSelectedColor() ) ;
	}
	
	public void updateCircle()
	{
		int x = (int)( centerPoint.getX() - radius ) ;
		int y = (int)( centerPoint.getY() - radius ) ;
		circle.setPathToEllipse( x, y, 2 * radius, 2 * radius ) ;
		endPoints[ 0 ] = centerPoint ;
		endPoints[ 1 ] = dragPoint ;
		if ( refLine == null )
			logger.warning( "NULL refline" ) ;
		if ( endPoints[ 1 ] == null )
			logger.warning( "endPoints NULL" ) ;	
		refLine.setPathToPolyline( endPoints ) ;
	}
	
	public void mouseDragged(PInputEvent e) {
		super.mouseDragged(e);
		dragPoint = e.getPosition();
		radius = (int) dragPoint.distance( centerPoint ) ;
		updateCircle();
	}

	public void mouseReleased(PInputEvent e) {
		super.mouseReleased(e);
		dragPoint = e.getPosition();
		updateCircle();

		// Switch to select listener, before updating in mediator
		getAssistant().switchListener() ;
		
		int x = (int)( centerPoint.getX() - radius ) ;
		int y = (int)( centerPoint.getY() - radius ) ;
		
		// Update the value in the mediator
		Circle circle = new Circle( (int) centerPoint.getX(), 
									(int) centerPoint.getY(), radius ) ;
		setAttrValueInMediator( circle ) ;
	}


	/* (non-Javadoc)
	 * @see edu.umd.cfar.lamp.viper.gui.canvas.ShapeCreator#getName()
	 */
	public String getName() {
		return "CIRCLE CREATOR";
	}

}
