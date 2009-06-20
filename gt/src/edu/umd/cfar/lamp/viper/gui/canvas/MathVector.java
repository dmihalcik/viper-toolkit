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

/**
 * @author clin
 *
 *   "width" is defined parallel to the angle
 *   "height is defined perpendicular to the angle
 */
public class MathVector {

	/**
	 * [x,y] is a vector
	 */
	double x, y ;

	/**
	 * Create a vector with magnitude sqrt(x^2 + y^2) in the
	 * direction of (0,0) to (x,y)
	 */ 
	public MathVector( double x, double y )
	{
		this.x = x ;
		this.y = y ;
	}
	
	/** 
	 * Creates a vector in the direction of first to second,
	 *  with magnitude equal to the distance between first and
	 *  second
	 * @param first    tail of the vector
	 * @param second   head of the vector
	 */
	public MathVector( Point2D first, Point2D second )
	{
		x = second.getX() - first.getX() ;
		y = second.getY() - first.getY() ;
	}
	
	public double getX()
	{
		return x ;
	}
	
	public void setX( double xIn )
	{
		x = xIn ;
	}
	
	public double getY()
	{
		return y ;
	}
	
	public void setY( double yIn )
	{
		y = yIn ;
	}

	/**
	 * Computes magnitude of vector
	 */
	public double getLength()
	{
		return computeLength( x, y ) ;
	}
	
	/**
	 * Computes magnitude of vector, where [x1, y1] is the vector
	 */
	private static double computeLength( double x1, double y1 )
	{
		return Math.sqrt( (x1 * x1) + (y1 * y1) ) ;
	}
	
	
	/**
	 * Computes a unit vector in same direction as this vector
	 */
	private MathVector computeUnitVect()
	{
		double normX = x / getLength() ;
		double normY = y / getLength() ;
		MathVector unit = new MathVector( normX, normY ) ;
//		System.out.println( "unit vect = " + unit.getLength() ) ;
		return unit ;
	}
	
	/**
	 * Computes the magnitude of the dot product
	 * @param v1 The vector to be projected
	 * @param v2 The vector being projected on
	 * @return The magnitude of the dot product of v1 and v2
	 */
	public static double dotProduct( MathVector v1, MathVector v2 )
	{
		return ( v1.getX() * v2.getX() ) + ( v1.getY() * v2.getY() ) ;
	}

	/**
	 * Projects vectSource onto the normalized vectProject (i.e., a unit
	 * vector in the direction of vectProject)
	 * @param vectSource The vector to be projected
	 * @param vectProject The vector being projected on
	 * @return The magnitude of the component of vectSource in the direction
	 * of vectProject
	 */
	public static double projectUnit( MathVector vectSource, MathVector vectProject )
	{
		// Compute dot product of line onto unitUpDown
		MathVector unitProject = vectProject.computeUnitVect() ;
		return MathVector.dotProduct( vectSource, unitProject ) ;
	}
	/**
	 * Computes the magnitude of vect in the direction of angle
	 * @param vect   The vector to project
	 * @param angle  The angle of the unit vector to be projected on
	 * @return The component of vect in the direction of angle
	 */
	public static double computeWidth( MathVector vect, double angle )
	{
		MathVector v = computeUnitWidthVector( angle );
		return projectUnit( vect, v ) ;
	}

	/**
	 * Computes the magnitude of vect perpendicular to direction of angle
	 * @param vect   The vector to project
	 * @param angle  The angle of the unit vector to be projected on, in radians
	 * @return The component of vect perpendicular direction of angle
	 */
	public static double computeHeight( MathVector vect, double angle )
	{
		MathVector v = computeUnitHeightVector( angle ) ;
		return projectUnit( vect, v ) ;
	}
	
	/**
	 * Computes a unit vector in the direction of angle
	 * @param angle  In radians
	 * @return A unit vector in the direction of angle
	 */
	public static MathVector computeUnitWidthVector( double angle )
	{
		return new MathVector( Math.cos( angle ), -Math.sin( angle ) ) ;		
	}
	
	/**
	 * Computes a unit vector perpendicular to direction of angle
	 * @param angle  In radians
	 * @return A unit vector perpendicular to direction of angle
	 */
	public static MathVector computeUnitHeightVector( double angle )
	{
		return new MathVector( -Math.sin( angle ), -Math.cos( angle ) ) ;
	}
	/**
	 * Computes component of vect in direction of angle
	 * @param vect   The vector to be projected
	 * @param angle  The direction to be projected on, in radians
	 * @return A vector that represents the component of vect in the
	 * direction of angle.
	 */
	public static MathVector computeWidthVector( MathVector vect, double angle )
	{
		MathVector v = computeUnitWidthVector( angle ) ;		
		double length = dotProduct( vect, v ) ;
		v.setX( v.getX() * length ) ;
		v.setY( v.getY() * length ) ;
		return v ;
	}
	
	/**
	 * Computes component of vect perpendicular to direction of angle
	 * @param vect   The vector to be projected
	 * @param angle  The direction to be projected on, in radians
	 * @return A vector that represents the component of vect perpendicular
	 * to direction of angle.
	 */
	public static MathVector computeHeightVector( MathVector vect, double angle )
	{
		MathVector v = computeUnitHeightVector( angle ) ;
		double length = dotProduct( vect, v );
		v.setX( v.getX() * length ) ;
		v.setY( v.getY() * length ) ;
		return v ;
	}
	
	/**
	 * 
	 * @param v1 First vector to be added
	 * @param v2 Second vector to be added
	 * @return Performs vector addition, resulting in a head to tail
	 * addition of v1 and v2
	 */
	public static MathVector addVector( MathVector v1, MathVector v2 )
	{
		return new MathVector( v1.getX() + v2.getX(), v1.getY() + v2.getY() ) ;
	}
	
	/**
	 * Adds a vector to a point, to get a resulting point shifted by the vector
	 * @param v  To "add" to the point
	 * @param pt The point to be added
	 * @return New point which is old point displaced by vector
	 */
	
	public static Point2D addVectorToPoint( MathVector v, Point2D pt )
	{
		Point2D newPt = new Point2D.Double( pt.getX() + v.getX(),
											pt.getY() + v.getY() ) ;
		return newPt ;
	}
	/**
	 * We have a point which we want to move along the line with angle, "angle"
	 * v is a vector, which is projected onto angle, and tells us how much to
	 * move at an angle.   As an analogy, pretend a wind is blowing to the 
	 * northeast, and there's a "train" on a track that's east-west.  This
	 * is the component of the wind that pushes the train in the east direction
	 * @param refPt    The point to be offset
	 * @param v        The vector of movement
	 * @param angle    The angle to project the vector on
	 * @return         The offsetted point
	 */
 
	public static Point2D offsetPointByWidth( Point2D refPt, 
											  MathVector v, double angle )
	{
		MathVector widthVect = computeWidthVector( v, angle ) ;
		Point2D newPoint = new Point2D.Double( refPt.getX() + widthVect.getX(),
											   refPt.getY() + widthVect.getY() ) ;
		return newPoint ;
	}
	
	public static Line2D testLineByWidth( Point2D refPt, 
										   MathVector v, double angle )
	{
		MathVector widthVect = computeWidthVector( v, angle ) ;
		Point2D newPoint = new Point2D.Double( refPt.getX() + widthVect.getX(),
											   refPt.getY() - widthVect.getY() ) ;
		return new Line2D.Double( refPt, newPoint ) ;
	}
	/**
	 * Offsets refPt in the direction of angle, with the magnitude of v
	 * projected on the angle
	 * @param refPt
	 * @param v
	 * @param angle
	 * @return
	 */
	public static Point2D offsetPointByHeight( Point2D refPt, 
											  MathVector v, double angle )
	{
		MathVector heightVect = computeHeightVector( v, angle ) ;
		Point2D newPoint = new Point2D.Double( refPt.getX() + heightVect.getX(),
											   refPt.getY() + heightVect.getY() ) ;
		return newPoint ;
	}
	

	/**
	 * (firstPoint, secondPoint) define a line
	 * This computes the angle of the line relative to the x-axis
	 * @param firstPoint
	 * @param secondPoint
	 * @return angle in radians relative to x-axis: result from 0 up to 2 PI
	 */
	public static double computeAngle( Point2D firstPoint, Point2D secondPoint ) {
		return computeAngle( new MathVector( firstPoint, secondPoint ) ) ;
	}
	
	public static double computeAngle( MathVector v ) {
		double x = v.getX() ;
		double y = - v.getY() ;  // negative sign compensates for coordinates
		                         // system differences (math coordinates have
		                         // y increasing as you go up, while graphics
		                         // coordinates have y increasing as you go down)
		double angle = Math.atan(y / x);
		// Compensates for arctan returning -pi to pi
		// Now gives a value from 0 to 2 pi
		if (x < 0)
			angle += Math.PI;
		else if (y < 0)
			angle += 2 * Math.PI;
		return angle ;
	}
	
	private static void printPoint( String note, Point2D point )
	{
		System.out.println( note + " (" + point.getX() + ", " + point.getY() + ")") ;
	}
	public String toString()
	{
		return "<" + x + ", " + y + ">" ;
	}
	
	public static void main( String [] args )
	{
		MathVector v = new MathVector( 10, 10 ) ;

//		System.out.println( computeWidth( v, 0.0 ) ) ;
//		System.out.println( computeHeight( v, 0.0 ) ) ;
//		System.out.println( "Pi/4 = " + Math.PI / 4 ) ;
//		System.out.println( computeWidth( v, Math.PI / 4 ) ) ;
//		System.out.println( computeHeight( v, Math.PI / 4 ) ) ;

		MathVector width = MathVector.computeWidthVector( v, 4 * Math.PI / 3 ) ;
		MathVector height = MathVector.computeHeightVector( v, 4 * Math.PI / 3 ) ;
		MathVector sum = MathVector.addVector( width, height ) ;
		
		System.err.println( "Orig vector = " + v ) ;
		System.err.println( "Reconstructed vector = " + sum ) ;

	}
}

