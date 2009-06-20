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

package edu.umd.cfar.lamp.viper.gui.data.point;

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
public class PointCreator extends CanvasCreator {
	private static Logger logger = Logger.getLogger("edu.umd.cfar.lamp.viper.gui.canvas.datatypes");
	
	public PointCreator( CreatorAssistant a, Attribute attr ) {
		super( a, attr );
	}
	
	Point2D pressPoint, dragPoint;
	PPath base, horiz, vert ;
	Point2D [] horizPts = new Point2D[ 2 ];
	Point2D [] vertPts = new Point2D[ 2 ];
	public static final int WIDTH = 3 ;
	
	public void mousePressed(PInputEvent e) {
		super.mousePressed(e);

		pressPoint = e.getPosition();
		base = new PPath() ;
		horiz = new PPath() ;
		vert = new PPath() ;
		base.addChild( horiz ) ;
		base.addChild( vert ) ;
		displaySelected() ;
		
		setCrosshair( pressPoint ) ;
		
		dragPoint = pressPoint;
		getAssistant().addShape( base ) ;
	}
	public void displaySelected()
	{
		Highlightable colorTable = getColorTable() ;

		horiz.setStroke( colorTable.getSelectedStroke() ) ;
		horiz.setStrokePaint( colorTable.getSelectedColor() ) ;
		vert.setStroke( colorTable.getSelectedStroke() ) ;
		vert.setStrokePaint( colorTable.getSelectedColor() ) ;
	}
	public void setCrosshair( Point2D center )
	{
		horizPts[ 0 ] 
			= new Point2D.Double( center.getX() - WIDTH, center.getY() ) ;
		horizPts[ 1 ] 
			= new Point2D.Double( center.getX() + WIDTH, center.getY() ) ;
		horiz.setPathToPolyline( horizPts ) ;
		
		vertPts[ 0 ] 
			= new Point2D.Double( center.getX(), center.getY() - WIDTH ) ;
		vertPts[ 1 ] 
			= new Point2D.Double( center.getX(), center.getY() + WIDTH ) ;
		vert.setPathToPolyline( vertPts ) ;
		
	}

	public void mouseDragged(PInputEvent e) {
		super.mouseDragged(e);
		setCrosshair( e.getPosition() ) ;
	}

	public void mouseReleased(PInputEvent e) {
		super.mouseReleased(e);
		Point2D center = e.getPosition() ;
		setCrosshair( center ) ;
		
		// Switch to select listener, before updating in mediator
		getAssistant().switchListener() ;
		
		// Update the value in the mediator
		Pnt pnt = new Pnt( (int)center.getX(), (int) center.getY() ) ;
		setAttrValueInMediator( pnt ) ;
	}


	/* (non-Javadoc)
	 * @see edu.umd.cfar.lamp.viper.gui.canvas.ShapeCreator#getName()
	 */
	public String getName() {
		return "PNT CREATOR";
	}
}

