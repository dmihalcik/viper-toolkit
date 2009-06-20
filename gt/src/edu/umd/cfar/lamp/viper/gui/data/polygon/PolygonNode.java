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

import java.awt.geom.*;
import java.util.*;
import java.util.logging.*;

import viper.api.*;
import viper.api.time.*;
import edu.umd.cfar.lamp.viper.geometry.*;
import edu.umd.cfar.lamp.viper.gui.canvas.*;
import edu.umd.cfar.lamp.viper.gui.canvas.datatypes.*;
import edu.umd.cfar.lamp.viper.gui.core.*;
import edu.umd.cs.piccolo.nodes.*;

/**
 * @author clin
 *
 * To change the template for this generated type comment go to
 * Window>Preferences>Java>Code Generation>Code and Comments
 */

public class PolygonNode extends AttributablePPathAdapter implements Attributable {
	private static Logger logger = Logger.getLogger("edu.umd.cfar.lamp.viper.gui.canvas.datatypes");
	Point2D [] polyPts ;
	double bboxWidth ;
	double bboxHeight ;
	
	PPath highlightLine = new PPath() ;
	PPath highlightCircle = new PPath() ;
	PPath highlightRect = new PPath() ;
	
	/**
	 * 
	 */
	public PolygonNode( ViperViewMediator mediator ) {
		super(mediator);
		polyPts = new Point2D[ 1 ] ;	
		
		resetStyle();
		
		addChild( highlightLine ) ;
		addChild( highlightCircle ) ;
		addChild( highlightRect ) ;
	}
	
	/**
	 * 
	 */
	protected void resetStyle() {
		highlightLine.setStroke( getHighlightDisplayProperties().getStroke() ) ;
		highlightLine.setStrokePaint( getHighlightDisplayProperties().getStrokePaint() ) ;
			
		highlightCircle.setStroke( getHighlightDisplayProperties().getStroke() ) ;
		highlightCircle.setStrokePaint( getHighlightDisplayProperties().getStrokePaint() ) ;
		
		highlightRect.setStroke( getHighlightDisplayProperties().getStroke() ) ;
		highlightRect.setStrokePaint( getHighlightDisplayProperties().getStrokePaint() ) ;
		
		this.setStroke(getDisplayProperties().getStroke());
		this.setStrokePaint(getDisplayProperties().getStrokePaint());
	}

	public Point2D [] getPolyPts() {
		return polyPts ;
	}

	
	private void setPath(Polygon poly) {
		if ( poly == null )
		{
			logger.warning( "Uhoh" ) ;
		}
		logger.fine( "==== Init PolygonNode BoundingBox: " + poly ) ;

		ArrayList list = new ArrayList() ;
		Iterator iter = poly.getPoints() ;
		while( iter.hasNext() )
		{
			Pnt pnt = (Pnt) iter.next() ;
			list.add( pnt ) ;
		}
		Object [] holder = list.toArray() ;
		
		polyPts = new Point2D[ holder.length + 1 ] ;
		for ( int i = 0 ; i < polyPts.length - 1 ; i++ )
		{
			polyPts[ i ] = ( (Pnt) holder[ i ] ).point2DDoubleValue() ;
		}
		// Original polygon does not repeat last point as first point
		// So must repeat it since PPath does not close polylines
		polyPts[ polyPts.length - 1 ] = polyPts[ 0 ] ;
		
		// Now that enough info is available, update the obox and its handle
		updatePolygon() ;

	}

	public void updatePolygon() {
		setPathToPolyline(polyPts);
	}
	
	public void setAttribute( Attribute attr ) {
		this.attr = attr ;
		Instant now = getInstant();
		// Get the oriented box corresponding to current frame
		Polygon poly = (Polygon) attr.getAttrValueAtInstant( now ) ;
		// Extract information about oriented box for local use
		if ( poly != null )
			setPath( poly ) ;
	}
	
	public Object getUpdatedAttribute() {
		Point2D [] polyPtsShort = new Point2D[ polyPts.length - 1 ] ;
		for ( int i = 0 ; i < polyPtsShort.length ; i++ )
		{
			polyPtsShort [ i ] = polyPts[ i ] ;
		}
		Polygon newPoly = new Polygon( polyPtsShort ) ;
		return newPoly ;
	}
	
	private Polygon makeCopy( Polygon box )
	{
		return new Polygon( box ) ;
	}
	
	boolean validIndex( int index )
	{
		if ( polyPts == null )
			return false ;
			
		return index >= 0 && index < polyPts.length ;
	}

	int cornerRadius = 5 ;
	int getCornerRadius()
	{
		return cornerRadius ;
	}
	
	public void setCornerRadius( int val )
	{
		cornerRadius = val ;
	}
	
	public void boldVertex( int index )
	{
		unbold() ;
		if ( ! validIndex( index ) )
			return ;
			
		highlightCircle.setPathToEllipse( (int) polyPts[ index ].getX() - getCornerRadius(),
			(int) polyPts[ index ].getY() - getCornerRadius(), 2 * getCornerRadius(),
			2 * getCornerRadius() ) ;
	}
	
	public void boldEdge( int index )
	{
		unbold() ;
		if ( ! validIndex( index ) )
			return ;
			
		Point2D [] line = new Point2D[ 2 ] ;
		line[ 0 ] = polyPts[ index ] ;
		line[ 1 ] = polyPts[ ( index + 1 ) % polyPts.length ] ;
		highlightLine.setPathToPolyline( line ) ;
	}
	public void boldCrossEdgeAndVertex( int index )
	{
		if ( ! validIndex( index ) )
			return ;
		boldCrossEdge( index ) ;
		
		// And highlight vertex
		highlightCircle.setPathToEllipse( (int) polyPts[ index ].getX() - getCornerRadius(),
			(int) polyPts[ index ].getY() - getCornerRadius(), 2 * getCornerRadius(),
			2 * getCornerRadius() ) ;
	}
	
	public void boldCrossEdge( int index )
	{
		unbold() ;
		if ( ! validIndex( index ) )
			return ;
			
		// Draw cross edge (from index - 1 to index + 1)
		Point2D [] line = new Point2D[ 2 ] ;
		int prevIndex = index - 1 ;
		if ( prevIndex == -1 )
			prevIndex = polyPts.length - 2 ;
		// mod may not be necessary
		int nextIndex = ( index + 1 ) % polyPts.length ;
		line[ 0 ] = polyPts[ prevIndex ] ;
		line[ 1 ] = polyPts[ nextIndex ] ;
		highlightLine.setPathToPolyline( line ) ;
		
		// And highlight vertex
		highlightCircle.setPathToEllipse( (int) polyPts[ index ].getX() - getCornerRadius(),
			(int) polyPts[ index ].getY() - getCornerRadius(), 2 * getCornerRadius(),
			2 * getCornerRadius() ) ;
	}
	
	public void unbold()
	{
		Point2D [] blank = new Point2D[ 1 ] ;
		blank[ 0 ] = new Point2D.Double() ;
		highlightLine.setPathToPolyline( blank ) ;
		highlightCircle.setPathToPolyline( blank ) ;
		highlightRect.setPathToPolyline( blank ) ;
	}

	/**
	 * @param polyPts
	 */
	public void setPolyPts(Point2D[] polyPtsIn) {
		polyPts = polyPtsIn ;
		
	}
	
	public int getNumVertices()
	{
		if ( polyPts == null )
			return 0 ;
		else
			return polyPts.length - 1 ;
	}

	/**
	 * @param point2D
	 */
	public void boldCircle(Point2D select) {
		unbold() ;
		highlightCircle.setPathToEllipse( (int) select.getX() - getCornerRadius(),
			(int) select.getY() - getCornerRadius(), 2 * getCornerRadius(),
			2 * getCornerRadius() ) ;
	}

	/**
	 * @param rect
	 */
	public void boldRect(Rectangle2D rect) {
		unbold() ;
		highlightRect.setPathToRectangle( (float) rect.getX(), 
										  (float) rect.getY(),
									      (float) rect.getWidth(), 
									      (float) rect.getHeight()) ;
		
	}

	/**
	 * @param direction
	 */
	public void boldRectAndCircle(Rectangle2D rect, Point2D select) {
		unbold() ;
		highlightRect.setPathToRectangle( (float) rect.getX(), 
										  (float) rect.getY(),
										  (float) rect.getWidth(), 
										  (float) rect.getHeight()) ;
		highlightCircle.setPathToEllipse( (int) select.getX() - getCornerRadius(),
			(int) select.getY() - getCornerRadius(), 2 * getCornerRadius(),
			2 * getCornerRadius() ) ;
		
	}

}