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
 * A circle, specified by the center point and the radius
 * in integer format, supporting functions necessary for 
 * simple comparison. It does not support the functions
 * necessary for advanced comparison (union, etc.) yet.
 */
public class Circle implements Cloneable, HasCentroid, Moveable {
	Pnt center;
	private Moveable moveDelegate = new CircleMover();
	private class CircleMover extends AbstractMoveable {
		public Moveable shift(int x, int y) {
			Pnt newCenter = (Pnt) center.shift(x, y);
			Circle shiftedCircle = new Circle();
			shiftedCircle.center = newCenter;
			return shiftedCircle;
		}
	};

	/**
	 * Creates a new <code>Circle</code> from the string.
	 * @param S a string in the form <em>x y radius</em>
	 * @return new <code>Circle</code> from the string
	 * @throws BadAttributeDataException upon a parse error
	 */
	public static Circle valueOf(String S) {
		try {
			StringTokenizer st = new StringTokenizer(S);
			return new Circle(
				Integer.parseInt(st.nextToken()),
				Integer.parseInt(st.nextToken()),
				Integer.parseInt(st.nextToken()));
		} catch (NoSuchElementException e1) {
			throw new BadAttributeDataException(
				"Not enough integers for circle: " + S);
		} catch (NumberFormatException e2) {
			throw new BadAttributeDataException(
				"Malformed circle string: " + S);
		}
	}
	int radius;

	/**
	 * Tests to see if this circle is the same one as the specified
	 * circle.
	 * @param o the circle to test against
	 * @return <code>true</code> iff the this and the specified circle
	 * represent the same origin and radius
	 */
	public boolean equals(Object o) {
		if (this == o) {
			return true;
		} else if (o instanceof Circle) {
			Circle other = (Circle) o;
			return (center.equals(other.center)) && (other.radius == radius);
		} else {
			return false;
		}
	}
	
	public int hashCode() {
		return new HashCodeBuilder().append(center).append(radius).toHashCode();
	}

	/**
	 * creates circle at (0,0) with radius = 0.
	 */
	public Circle() {
		center = new Pnt();
		radius = 0;
	}

	/**
	 * Creates circle with center (x,y) and radius = r
	 * @param x the x-coordinate of the circle's center
	 * @param y the y-coordinate of the circle's center
	 * @param r the radius of the circle
	 */
	public Circle(int x, int y, int r) {
		center = new Pnt(x, y);
		radius = r;
	}

	/**
	 * Copy constructor for the circle.
	 * @param old the circle to copy
	 */
	public Circle(Circle old) {
		center = new Pnt(old.center);
		radius = old.radius;
	}

	/**
	 * Returns the center of the circle.
	 * @return the center point of the circle
	 */
	public Pnt getCenter() {
		return (new Pnt(center));
	}

	/**
	 * Gets the circle's radius.
	 * @return the radius of this circle
	 */
	public int getRadius() {
		return radius;
	}

	/**
	 * Prints out the circle in the ViPER format.
	 * @return a space delimited list of integers, in the form <q>x y r</q>
	 */
	public String toString() {
		return (center.x + " " + center.y + " " + radius);
	}

	/**
	 * Copies the circle.
	 * @return a new copy of the same circle
	 */
	public Object clone() {
		return new Circle(this);
	}

	/**
	 * Tests to see if the given point is within the circle.
	 * @param point the point to test
	 * @return <code>true</code> iff the point is within or onthe 
	 * circle.
	 */
	public boolean contains(Pnt point) {
		Component temp = center.minus(point);
		Rational xDistanceSq = new Rational();
		Rational.multiply(temp.x, temp.x, xDistanceSq);
		Rational distanceSq = new Rational();
		Rational.multiply(temp.y, temp.y, distanceSq);
		Rational.plus(xDistanceSq, distanceSq, distanceSq);
		return distanceSq.lessThan(radius*radius);
	}

	/**
	 * Gets the closest square surrounding the circle.
	 * @return the bounding box for the circle
	 */
	public BoundingBox getBoundingBox() {
		return new BoundingBox(
			center.x.intValue() - radius,
			center.y.intValue() - radius,
			radius * 2,
			radius * 2);
	}

	/**
	 * Approximates the area of the circle.
	 * @return <code>Math.PI</code> times the square of the radius
	 */
	public double area() {
		return Math.PI * radius * radius;
	}

	/**
	 * Approximates the shared area of two circles.
	 * @param other the circle to intersect with
	 * @return the area of the region overlapped by both circles
	 */
	public double intersectArea(Circle other) {
		double centerDistance = center.minus(other.center).length();
		if (centerDistance >= (radius + other.radius))
			return 0;

		// Check for inscription
		if ((centerDistance + other.radius) <= radius)
			return other.area();
		if ((centerDistance + radius) <= other.radius)
			return area();

		// for simplicity
		double cDsq = centerDistance * centerDistance;
		double r1sq = radius * radius;
		double r2sq = other.radius * other.radius;

		// for formula, see http://www.oswego.edu/~baloglou/circles.html
		return Math.PI * ((r1sq + r2sq) / 2)
			+ r2sq
				* Math.asin(
					(r1sq - r2sq - cDsq) / (2 * centerDistance * other.radius))
			- r1sq
				* Math.asin((r1sq - r2sq + cDsq) / (2 * centerDistance * radius))
			- .5
				* Math.sqrt(
					2 * (r1sq * r2sq + cDsq * r1sq + cDsq * r2sq)
						- r1sq * r1sq
						- r2sq * r2sq
						- cDsq * cDsq);
	}

	/**
	 * @see edu.umd.cfar.lamp.viper.geometry.HasCentroid#getCentroid()
	 */
	public Pnt getCentroid() {
		return center;
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
