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


package edu.umd.cfar.lamp.viper.gui.canvas;

import java.awt.geom.*;

import viper.api.*;
import viper.api.time.*;
import edu.umd.cfar.lamp.viper.geometry.*;
import edu.umd.cfar.lamp.viper.gui.canvas.datatypes.*;
import edu.umd.cfar.lamp.viper.gui.core.*;

/**
 * Common subclass of box attribute display nodes.
 * @author davidm
 */
public abstract class PBoxNode extends AttributablePPathAdapter implements Attributable {
	
	public PBoxNode( ViperViewMediator mediator ) {
		super(mediator);
	}
	public void setAttribute( Attribute attr ) {
		this.attr = attr ;
		Instant now = getInstant();
		// Get the oriented box corresponding to current frame
		BoxInformation box = (BoxInformation) attr.getAttrValueAtInstant( now ) ;
		// Extract information about oriented box for local use
		if ( box != null ) {
			setPath( box ) ;
		}
	}
	
	/**
	 * @return
	 */
	public Point2D getCenterPt() {
		Point2D[] pts = getBoxPts();
		assert pts.length >= 4;
		double minX, maxX, minY, maxY;
		minX = maxX = pts[0].getX();
		minY = maxY = pts[0].getY();
		for (int i = 1; i < 4; i++) {
			double x = pts[i].getX();
			minX = Math.min(minX,x);
			maxX = Math.max(maxX,x);
			
			double y = pts[i].getY();
			minY = Math.min(minY,y);
			maxY = Math.max(maxY,y);
		}
		return new Point2D.Double((minX+maxX)/2, (minY+maxY)/2);
	}
	
	public abstract void setCenterBolded(boolean bold);

	public abstract Point2D[] getBoxPts();
	public abstract double getBoxWidth();
	public abstract double getBoxHeight();
	
	public abstract void setPath(BoxInformation box);
	
	public abstract void bold( CanvasDir dir );
	public abstract void unbold();
}
