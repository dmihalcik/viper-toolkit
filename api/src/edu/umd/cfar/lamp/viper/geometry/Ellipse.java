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

import java.util.*;

import org.apache.commons.lang.builder.*;

import viper.api.*;

/**
 * An ellipse.
 * It does not support the functions
 * necessary for advanced comparison (union, etc.) yet.
 */
public class Ellipse implements Cloneable, HasCentroid, Moveable {
	private final class EllipseMover extends AbstractMoveable {
		public Moveable shift(int x, int y) {
			Pnt newOrigin = (Pnt) origin.shift(x, y);
			Ellipse shiftedEllipse = new Ellipse();
			shiftedEllipse.origin = newOrigin;
			shiftedEllipse.diagonal = diagonal;
			shiftedEllipse.rotation = rotation;
			return shiftedEllipse;
		}
	}

	Pnt origin;
	Component diagonal;
	int rotation;
	private Moveable moveDelegate = new EllipseMover();


	/**
	 * Tests to see the two objects are equal.
	 * @param o the ellipse to check against
	 * @return <code>true</code> iff o is an Ellipse that represents
	 * the same shape 
	 */
	public boolean equals(Object o) {
		if (o == this) {
			return true;
		} else if (o instanceof Ellipse) {
			Ellipse other = (Ellipse) o;
			return origin.equals(other.origin)
				&& diagonal.equals(other.diagonal) 
				&& rotation == other.rotation;
		} else {
			return false;
		}
	}
	
	public int hashCode() {
		return new HashCodeBuilder().append(origin).append(diagonal).append(rotation).toHashCode();
	}

	/**
	 * Constructs a new null ellipse
	 */
	public Ellipse() {
		origin = new Pnt();
		diagonal = new Component();
	}

	/**
	 * Constructs a new ellipse from the given string
	 * @param S the string to parse - a space delimited list of four integers
	 * @return the Ellipse represented by the string.
	 * @throws BadAttributeDataException
	 */
	public static Ellipse valueOf(String S) {
		try {
			StringTokenizer st = new StringTokenizer(S);
			return new Ellipse(
				Integer.parseInt(st.nextToken()),
				Integer.parseInt(st.nextToken()),
				Integer.parseInt(st.nextToken()),
				Integer.parseInt(st.nextToken()),
				Integer.parseInt(st.nextToken()));
		} catch (NoSuchElementException e1) {
			throw new BadAttributeDataException(
				"Not enough integers for ellipse: " + S);
		} catch (NumberFormatException e2) {
			throw new BadAttributeDataException(
				"Malformed ellipse string: " + S);
		}
	}

	/**
	 * Constructs a new ellipse with the given parameters
	 * @param x the x-coordinate of the ellipse's bounding box's origin
	 * @param y the y-coordinate of the ellipse's bounding box's origin
	 * @param width the width of the ellipse
	 * @param height the height of the ellipse
	 * @param rotation 
	 */
	public Ellipse(int x, int y, int width, int height, int rotation) {
		origin = new Pnt(x, y);
		diagonal = new Component(width, height, 1);
		this.rotation = rotation;
	}

	/**
	 * Copy constructor.
	 * @param old the ellipse to copy
	 */
	public Ellipse(Ellipse old) {
		origin = new Pnt(old.origin);
		diagonal = new Component(old.diagonal);
	}

	/**
	 * Prints out the circle in the ViPER format.
	 * @return a space delimited list of four integers
	 */
	public String toString() {
		return (
			origin.x + " " + origin.y + " " + diagonal.x + " " + diagonal.y + " " + rotation);
	}

	/**
	 * Copies the ellipse.
	 * @return a new copy of this ellipse
	 */
	public Object clone() {
		return new Ellipse(this);
	}

	/**
	 * Tests to see if the point is within this ellipse. At the moment,
	 * just throws an exception.
	 * @param point the point to test for
	 * @return true, if the point is in or on the ellipse
	 */
	public boolean contains(Pnt point) {
		// TODO: test to see if the point is within the ellipse
		throw new UnsupportedOperationException("Ellipse.contains (Pnt) not yet implemented");
	}

	/**
	 * Gets the bounding box around the ellipse
	 * @return the closest box surrounding the ellipse
	 */
	public BoundingBox getBoundingBox() {
		return new BoundingBox(
			origin.x.intValue(),
			origin.y.intValue(),
			diagonal.x.intValue(),
			diagonal.y.intValue());
	}

	/**
	 * Approximates the area of the ellipse.
	 * Currently not implemented.
	 * @return the area of the ellipse
	 */
	public double area() {
		// TODO:calculate the area of the ellipse
		throw new UnsupportedOperationException("Ellipse.area() not yet implemented");
	}

	/**
	 * Computes the area shared between the two ellipses
	 * @param other the area to intersect with
	 * @return the shared area
	 */
	public double intersectArea(Ellipse other) {
		// TODO:calculate the area shared between two ellipses
		throw new UnsupportedOperationException("Ellipse.intersectArea() not yet implemented");
	}

	/**
	 * Gets the x-coordinate of the origin
	 * @return the x-coordinate of the origin
	 */
	public Rational getX() {
		return origin.x;
	}

	/**
	 * Gets the y-coordinate of the origin
	 * @return the y-coordinate of the origin
	 */
	public Rational getY() {
		return origin.y;
	}

	/**
	 * Gets the width of the ellipse
	 * @return the width of the ellipse
	 */
	public Rational getWidth() {
		return diagonal.x;
	}

	/**
	 * Gets the height of the ellipse
	 * @return the height of the ellipse
	 */
	public Rational getHeight() {
		return diagonal.y;
	}

	/**
	 * @return
	 */
	public int getRotation() {
		return rotation;
	}

	/**
	 * @see edu.umd.cfar.lamp.viper.geometry.HasCentroid#getCentroid()
	 */
	public Pnt getCentroid() {
		OrientedBox obox = new OrientedBox(getX().intValue(), getY().intValue(),
		                                   getWidth().intValue(), 
							               getHeight().intValue(), 
							               getRotation());
 		return obox.getCentroid() ;							            
	}

	/**
	 * @see edu.umd.cfar.lamp.viper.geometry.Moveable#move(int, int)
	 */
	public Moveable move(int direction, int distance) {
		return moveDelegate.move(direction, distance);
	}
	
	/**
	 * @see edu.umd.cfar.lamp.viper.geometry.Moveable#shift(int, int)
	 */
	public Moveable shift(int x, int y) {
		return moveDelegate.shift(x, y);
	}
}
