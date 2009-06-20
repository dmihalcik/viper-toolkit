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

package edu.umd.cfar.lamp.viper.geometry;

/**
 * An Oriented Box is a rectangle that is rotated about its bottom right
 * corner. The BoxInformation class presents a unified interface to both
 * BoundingBoxes and OrientedBoxes, as well as any other shape that fits the
 * definition.
 */
public interface BoxInformation extends Cloneable {
	/**
	 * Gets the x-coordinate of the box's origin.
	 * @return the x-coordinate of the box's origin.
	 */
	public int getX();

	/**
	 * Gets the y-coordinate of the box's origin.
	 * @return the y-coordinate of the box's origin.
	 */
	public int getY();

	/**
	 * Gets the width of the box.
	 * @return the width of the box.
	 */
	public int getWidth();

	/**
	 * Gets the height of the box.
	 * @return the height of the box.
	 */
	public int getHeight();

	/**
	 * Gets the number of degrees the box is rotated counterclockwise
	 * about its origin.
	 * @return the rotation, in degrees.
	 */
	public int getRotation();

	/**
	 * A rational approximation of the box's centroid.
	 * @return the center point of the box
	 */
	public Pnt getCentroid();

	/**
	 * Tests to see if the specified point is within
	 * this rectangle
	 * @param point the point to test
	 * @return <code>true</code> iff the point is within
	 * this rectangle
	 */
	public boolean contains(Pnt point);

	/**
	 * Get the point of intersection between the ray from the centroid of this
	 * box through q1 that is closest to q1.
	 * 
	 * @param q1
	 *            A point that is not the centroid.
	 * @return A point on the perimeter of the box on the ray from the centroid
	 *         through q1.
	 */
	public Pnt getNearIntersection(Pnt q1);
	
	public Object clone();
}
