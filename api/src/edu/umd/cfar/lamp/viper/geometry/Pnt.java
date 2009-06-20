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

import java.awt.*;
import java.awt.geom.*;
import java.util.*;

import viper.api.*;

/**
 * Represents a 2d point in Rational format.
 */
public class Pnt extends Component implements Moveable, HasCentroid {
	/**
	 * Constructs a new Pnt set to the origin.
	 */
	public Pnt() {
		super(0, 0, 1);
	}

	/**
	 * Constructs a new Pnt at the given location.
	 * 
	 * @param X
	 *            The x coordinate of the point.
	 * @param Y
	 *            The y coordinate of the point.
	 */
	public Pnt(int X, int Y) {
		super(X, Y, 1);
	}

	/**
	 * Constructs a new Pnt at the given location.
	 * 
	 * @param X
	 *            The x coordinate of the point.
	 * @param Y
	 *            The y coordinate of the point.
	 */
	public Pnt(Rational X, Rational Y) {
		super(X, Y, new Rational(1));
	}

	/**
	 * Constructs a new Pnt with the given coordinates.
	 * 
	 * @param C
	 *            A Component to take the x and y coordinates from for a point.
	 */
	public Pnt(Component C) {
		x = new Rational(C.x);
		y = new Rational(C.y);
		t = new Rational(1);
	}

	/**
	 * Creates a new point from the given number pair. E.g. <code>12 13</code>
	 * 
	 * @param S
	 *            the pair of numbers
	 * @return new point
	 * @throws BadAttributeDataException
	 *             if the string isn't formed properly
	 */
	public static Pnt valueOf(String S) {
		try {
			StringTokenizer st = new StringTokenizer(S);
			Integer x = Integer.valueOf(st.nextToken());
			Integer y = Integer.valueOf(st.nextToken());
			return new Pnt(x.intValue(), y.intValue());
		} catch (NoSuchElementException e1) {
			throw new BadAttributeDataException("Not enough numbers: " + S);
		} catch (NumberFormatException e2) {
			throw new BadAttributeDataException("Not a valid point string: "
					+ S);
		}
	}

	/**
	 * Tests the equality of this with another Component.
	 * 
	 * @param o
	 *            The object to test against this Component.
	 * @return <code>true</code> if these are equal.
	 */
	public boolean equals(Object o) {
		if (o instanceof Pnt) {
			Pnt C = (Pnt) o;
			Rational mine = new Rational();
			Rational other = new Rational();
			Rational.divide(x, t, mine);
			Rational.divide(C.x, C.t, other);
			if (!mine.equals(other)) {
				return false;
			}
			Rational.divide(y, t, mine);
			Rational.divide(C.y, C.t, other);
			if (!mine.equals(other)) {
				return false;
			}
			return true;
		} else {
			return super.equals(o);
		}
	}

	/**
	 * Generates a hash code for this object.
	 * 
	 * @return A hash code for use in HashMaps and so on.
	 */
	public int hashCode() {
		Rational X = new Rational();
		Rational Y = new Rational();
		Rational.divide(x, t, X);
		Rational.divide(y, t, Y);
		return X.hashCode() + Y.hashCode();
	}

	/**
	 * Gets the Point2D representation of this point.
	 * 
	 * @return A Point2D.Double that is as close to this Pnt as possible.
	 */
	public Point2D.Double point2DDoubleValue() {
		return new Point2D.Double(x.doubleValue(), y.doubleValue());
	}

	/**
	 * Gets the Point representation of this point.
	 * 
	 * @return The nearest integral Point to this Pnt.
	 */
	public Point pointValue() {
		return new Point(x.intValue(), y.intValue());
	}

	/**
	 * Gets the x-coordinate of the point.
	 * 
	 * @return the x-coordinate of the point
	 */
	public Rational getX() {
		return x;
	}

	/**
	 * Gets the y-coordinate of the point.
	 * 
	 * @return the y-coordinate of the point
	 */
	public Rational getY() {
		return y;
	}

	/**
	 * Moves the box in the given direction. XXX currently only works for 32-bit
	 * integral coordinates
	 * 
	 * @see Moveable
	 * @param direction
	 *            the direction to move, one of Movable.NORTH, etc.
	 * @param distance
	 *            the distance to move
	 * @return a copy of the box with the origin shifted per the instructions
	 */
	public Moveable move(int direction, int distance) {
		switch (direction) {
		case Moveable.NORTH:
			return shift(0, distance);
		case Moveable.NORTHEAST:
			return shift(distance, distance);
		case Moveable.EAST:
			return shift(distance, 0);
		case Moveable.SOUTHEAST:
			return shift(distance, -distance);
		case Moveable.SOUTH:
			return shift(0, -distance);
		case Moveable.SOUTHWEST:
			return shift(-distance, -distance);
		case Moveable.WEST:
			return shift(-distance, 0);
		case Moveable.NORTHWEST:
			return shift(-distance, distance);
		}
		throw new IllegalArgumentException("Not a cardinal direction: "
				+ direction);
	}

	/**
	 * @see edu.umd.cfar.lamp.viper.geometry.Moveable#shift(int, int)
	 */
	public Moveable shift(int x, int y) {
		return new Pnt(getX().intValue() + x, getY().intValue() + y);
	}

	/**
	 * @see edu.umd.cfar.lamp.viper.geometry.HasCentroid#getCentroid()
	 */
	public Pnt getCentroid() {
		return this;
	}

	/**
	 * Tests to see if this point is to the left of the oriented line segment
	 * ab.
	 * 
	 * @param a
	 *            start
	 * @param b
	 *            look from a to this point to see where left is
	 * @return if the point is on the left halfplane defined by ab
	 */
	public boolean isLeftOf(Pnt a, Pnt b) {
		return Util.areaSign(a, b, this).greaterThan(0);
	}

	/**
	 * Tests to see if this point is to the left of or on the oriented line
	 * segment ab.
	 * 
	 * @param a
	 *            start
	 * @param b
	 *            look from a to this point to see where left is
	 * @return if the point is on the left halfplane defined by ab
	 */
	public boolean isLeftOfOrOn(Pnt a, Pnt b) {
		return Util.areaSign(a, b, this).greaterThan(new Rational(0));
	}

	/**
	 * Tests to see if this point is a convex combination of the two given
	 * points. Assumes it is already known that abc are collinear; it actually
	 * tests to see if c is in the bounding box that contains a and b.
	 * 
	 * @param a
	 *            endpoint of the line segment to look inside
	 * @param b
	 *            endpoint of the line segment to look inside
	 * @return <code>true</code> iff point c lies on the closed segement ab.
	 */
	boolean between(Pnt a, Pnt b) {
		assert Util.collinear(a, b, this);
		
		// If ab not vertical, check betweenness on x; else on y.
		if (!a.x.equals(b.x))
			return ((a.x.lessThan(this.x) || a.x.equals(this.x)) && (this.x
					.lessThan(b.x) || this.x.equals(b.x)))
					|| ((a.x.greaterThan(this.x) || a.x.equals(this.x)) && (this.x
							.greaterThan(b.x) || this.x.equals(b.x)));
		else
			return ((a.y.lessThan(this.y) || a.y.equals(this.y)) && (this.y
					.lessThan(b.y) || this.y.equals(b.y)))
					|| ((a.y.greaterThan(this.y) || a.y.equals(this.y)) && (this.y
							.greaterThan(b.y) || this.y.equals(b.y)));
	}
}