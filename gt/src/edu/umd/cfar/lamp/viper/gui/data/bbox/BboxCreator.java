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

package edu.umd.cfar.lamp.viper.gui.data.bbox;

import java.awt.event.*;
import java.awt.geom.*;
import java.util.logging.*;

import viper.api.*;
import edu.umd.cfar.lamp.viper.geometry.*;
import edu.umd.cfar.lamp.viper.gui.canvas.*;
import edu.umd.cs.piccolo.event.*;
import edu.umd.cs.piccolo.nodes.*;
import edu.umd.cs.piccolo.util.*;

/**
 * @author clin
 *
 * To change the template for this generated type comment go to
 * Window>Preferences>Java>Code Generation>Code and Comments
 */
public class BboxCreator extends CanvasCreator {
	private static Logger logger = Logger.getLogger("edu.umd.cfar.lamp.viper.gui.canvas.datatypes");
	
	public BboxCreator( CreatorAssistant a, Attribute attr ) {
		super( a, attr );
	}
	boolean controlPressed ;
	/**
	 * @return
	 */
	public boolean isControlPressed() {
		return controlPressed;
	}

	/**
	 * @param b
	 */
	public void setControlPressed(boolean b) {
		controlPressed = b;
	}
	
	public void keyPressed(PInputEvent e) {
		super.keyPressed(e) ;
		if ( e.getKeyCode() == KeyEvent.VK_CONTROL )
		{
			setControlPressed( true ) ;
			if ( cornerPointsValid() )
				updateRectangle() ;
		}
	}
	
	/**
	 * @return
	 */
	private boolean cornerPointsValid() {
		return pressPoint != null && dragPoint != null ;
	}

	public void keyReleased(PInputEvent e) {
		super.keyReleased(e) ;
		if ( e.getKeyCode() == KeyEvent.VK_CONTROL )
		{
			setControlPressed( false ) ;
			if ( cornerPointsValid() )
				updateRectangle() ;
		}
	}
	
	Point2D pressPoint = null, dragPoint = null ;
	PPath rect ;
	
	public void displaySelected()
	{
		Highlightable colorTable = getColorTable() ;

		rect.setStroke( colorTable.getSelectedStroke() ) ;
		rect.setStrokePaint( colorTable.getSelectedColor() ) ;
	}
	
	public void mousePressed(PInputEvent e) {
		super.mousePressed(e);

		pressPoint = e.getPosition();
		rect = new PPath() ;
		displaySelected() ;
		rect.setBounds( pressPoint.getX(), pressPoint.getY(), 0, 0 ) ;
		dragPoint = pressPoint;
		getAssistant().addShape( rect ) ;
		updateRectangle();
	}

	public void mouseDragged(PInputEvent e) {
		super.mouseDragged(e);
		if (null == pressPoint) {
			return;
		}

		dragPoint = e.getPosition();
		updateRectangle();
	}

	public void mouseReleased(PInputEvent e) {
		super.mouseReleased(e);
		if (null == pressPoint) {
			return;
		}
		dragPoint = e.getPosition();
		updateRectangle();

        // Switch to select listener, before updating in mediator
		getAssistant().switchListener() ;
		
		// Update the value in the mediator
		BoundingBox boundBox 
			= new BoundingBox( rect.getX(), rect.getY(), 
								rect.getWidth(), rect.getHeight() ) ;
		logger.fine( "   BboxCreator: setting value in mediator" ) ;
		setAttrValueInMediator( boundBox ) ;
		logger.fine( "   BboxCreator: leaving mouseReleased" ) ;
	}

	public void updateRectangle() {
		if ( isControlPressed() )
		{
			updateRectFromCenter() ;
		}
		else
		{
			updateRectFromCorner();
		}
	}
		
	// Used to draw rectangle from center
	Point2D tempLowerRight = new Point2D.Double() ;
	Point2D tempUpperLeft = new Point2D.Double() ;
	/**
	 * updateRectFromcenter
	 *    There's an easier way (I think) with AffineTransforms
	 */
	private void updateRectFromCenter() {
		// TODO Auto-generated method stub
		double diffX = dragPoint.getX() - pressPoint.getX() ;
		double diffY = dragPoint.getY() - pressPoint.getY() ;
		double absDiffX = Math.abs( diffX ) ;
		double absDiffY = Math.abs( diffY ) ;
		
		double newX = dragPoint.getX() ;
		if ( diffX < 0 )
		{
			newX = pressPoint.getX() + absDiffX ;
		}
		double newY = dragPoint.getY() ;
		if ( diffY < 0 )
		{
			newY = pressPoint.getY() + absDiffY ;
		}	
		
		tempUpperLeft.setLocation( pressPoint.getX() - absDiffX,
								   pressPoint.getY() - absDiffY ) ;
		tempLowerRight.setLocation( newX, newY ) ;
		
		// Now draw rectangle
		PBounds b = new PBounds();
		assert pressPoint != null;
		assert dragPoint != null;
		b.add(tempUpperLeft);
		b.add(tempLowerRight);
		rect.setPathTo(b);
	}

	private void updateRectFromCorner() {
		PBounds b = new PBounds();
		assert pressPoint != null;
		assert dragPoint != null;
		b.add(pressPoint);
		b.add(dragPoint);
		rect.setPathTo(b);
	}

	private Rectangle2D computeRect() {
		double upperLeftX = pressPoint.getX();
		if (dragPoint.getX() < upperLeftX)
			upperLeftX = dragPoint.getX();

		double upperLeftY = pressPoint.getY();
		if (dragPoint.getY() < upperLeftY)
			upperLeftY = dragPoint.getY();

		double width, height;
		width = pressPoint.getX() - dragPoint.getX();
		if (width < 0)
			width = -width;

		height = pressPoint.getY() - dragPoint.getY();
		if (height < 0)
			height = -height;

		return new Rectangle2D.Double(
			upperLeftX,
			upperLeftY,
			width,
			height);
	}

	/* (non-Javadoc)
	 * @see edu.umd.cfar.lamp.viper.gui.canvas.ShapeCreator#getName()
	 */
	public String getName() {
		return "BBOX CREATOR";
	}
	

}
