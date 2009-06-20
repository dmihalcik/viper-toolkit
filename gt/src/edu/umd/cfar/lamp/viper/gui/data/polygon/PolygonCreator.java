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

package edu.umd.cfar.lamp.viper.gui.data.polygon;

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

public class PolygonCreator extends CanvasCreator {
	private static Logger logger = Logger.getLogger("edu.umd.cfar.lamp.viper.gui.canvas.datatypes");

	PPath poly = new PPath() ;
	PPath moveLine = new PPath() ;

	Point2D selectPt = null ;
	PPath rect ;
	Point2D [] polyPnts = new Point2D[ 0 ] ;
	private static int LEFT_MOUSE_MASK = InputEvent.BUTTON1_MASK ;
	private static int MIDDLE_MOUSE_MASK = InputEvent.BUTTON2_MASK ;
	
	public PolygonCreator( CreatorAssistant a, Attribute attr ) {
		super( a, attr );

//		getAssistant().addShape( poly ) ;
//		poly.addChild( moveLine ) ;
//		displaySelected() ;
	}

	public void displaySelected()
	{
		Highlightable colorTable = getColorTable() ;

		poly.setStroke( colorTable.getSelectedStroke() ) ;
		poly.setStrokePaint( colorTable.getSelectedColor() ) ;
		moveLine.setStroke( colorTable.getMediumHighlightStroke() ) ;
		moveLine.setStrokePaint( colorTable.getHighlightColor() ) ;
	}
	
	private void addPoint( Point2D newPoint )
	{
		if ( polyPnts.length == 0 )
		{
			getAssistant().addShape( poly ) ;
			poly.addChild( moveLine ) ;
			displaySelected() ;
		}
		Point2D [] tempPolyPnts = new Point2D[ polyPnts.length + 1 ] ;
//		System.out.println( "num points = " + tempPolyPnts.length ) ;
		for ( int i = 0 ; i < polyPnts.length; i++ )
		{
			tempPolyPnts[ i ] = polyPnts[ i ] ;
		}
		tempPolyPnts[ tempPolyPnts.length - 1 ] = newPoint ;
		polyPnts = tempPolyPnts ;
		updatePolygon() ;
	}

	
	private void updatePoint( Point2D newPoint )
	{
		polyPnts[ polyPnts.length - 1 ] = newPoint ;
	}
	
	public void keyPressed( PInputEvent e )
	{
		super.keyPressed( e ) ;
		if ( e.getKeyCode() == KeyEvent.VK_ENTER )
		{
			getAssistant().switchListener() ; // switch from creator to editor
			Polygon poly = new Polygon( polyPnts ) ;
								   
			// Set attribute to have orientedBox as current value
			setAttrValueInMediator( poly ) ;
		}
	}
	
	public void mousePressed(PInputEvent e) {
		super.mousePressed(e);
		// middle mouse button finishes the polygon and exits
		selectPt = e.getPosition(); 
//		System.out.println( "Polygon creator: mouse pressed" ) ;
		addPoint( selectPt ) ;
		updatePolygon();
	}

	public void mouseDragged(PInputEvent e) {
		super.mouseDragged(e);
		selectPt = e.getPosition();
		updatePoint( selectPt ) ;
		updatePolygon();
	}

	Point2D [] local = new Point2D[ 2 ] ;
	
	public void mouseMoved(PInputEvent e) {
		super.mouseDragged(e);
		if ( selectPt != null )
		{
			Point2D dragPt = e.getPosition() ;
			local[ 0 ] = selectPt ;
			local[ 1 ] = dragPt ;
			moveLine.setPathToPolyline( local ) ;
		}
	}
	
	public void mouseReleased(PInputEvent e) {
		super.mouseReleased(e);
		selectPt = e.getPosition();
		updatePoint( selectPt ) ;
		updatePolygon();
	}

	private void updatePolygon() {
		// TODO This isn't going to be very efficient, but it's quick to write
		poly.setPathToPolyline( polyPnts ) ;
	}

	/* (non-Javadoc)
	 * @see edu.umd.cfar.lamp.viper.gui.canvas.ShapeCreator#getName()
	 */
	public String getName() {
		return "POLYGON CREATOR";
	}
}
