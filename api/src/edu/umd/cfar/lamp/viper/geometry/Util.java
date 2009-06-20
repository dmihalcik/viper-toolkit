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

/*
 * Util.java
 *
 * Created on June 4, 2002, 2:13 PM
 */

package edu.umd.cfar.lamp.viper.geometry;

/**
 * A set of utility static methods for dealing with the viper geometry package.
 */
public final class Util {

	/**
	 * Snaps the angle (in radians) to a multiple of PI/4.
	 * @param angle
	 * @return
	 */
	static public double snapAngleToMajorDirection(double angle) {
		return Math.round(4 * angle / Math.PI) * Math.PI / 4;
	}
	
	// @see O'Rourke: computational geometry in c
	static boolean intersectsProperly(Pnt a, Pnt b, Pnt c, Pnt d) {
		// Eliminate improper cases.
		if (collinear(a, b, c) || collinear(a, b, d) || collinear(c, d, a)
				|| collinear(c, d, b))
			return false;

		boolean cdAlternatesAb = c.isLeftOf(a, b) ^ d.isLeftOf(a, b);
		boolean abAlternatesCd = a.isLeftOf(c, d) ^ b.isLeftOf(c, d);
		return cdAlternatesAb && abAlternatesCd;
	}

	/**
	 * Tests to see if two line segments share points.
	 * Taken from O'Rourke: computational geometry in c.
	 * @param a endpoint of first segment
	 * @param b endpoint of first segment
	 * @param c endpoint of second segment
	 * @param d endpoint of second segment
	 * @return if the two segments intersect or overlap
	 */
	static boolean intersects(Pnt a, Pnt b, Pnt c, Pnt d) {
		if (intersectsProperly(a, b, c, d)) {
			return true;
		}
		if (Util.collinear(a, b, c) && c.between(a, b) ||
				Util.collinear(a, b, d) && d.between(a, b) || 
				Util.collinear(c, d, a) && a.between(c, d) || 
				Util.collinear(c, d, b) && b.between(c, d)) { 
			return true;
		}
		return false;
	}

	/**
	 * Sets point p to the intersection of segments ab and cd.
	 * 
	 * @param a
	 *            endpoint of the first line
	 * @param b
	 *            endpoint of the first line
	 * @param c
	 *            endpoint of the second line
	 * @param d
	 *            endpoint of the second line
	 * @param p
	 *            the point that will be modified to refer to the point of
	 *            intersection. It is only modified if the two lines intersect.
	 * @return <dl>
	 *         <dt>'e'</dt>
	 *         <dd>The segments collinearly overlap, sharing a point.</dd>
	 *         <dt>'v'</dt>
	 *         <dd>An endpoint (vertex) of one segment is on the other segment,
	 *         but 'e' doesn't hold.</dd>
	 *         <dt>'1'</dt>
	 *         <dd>The segments intersect properly (i.e., they share a point
	 *         and neither 'v' nor 'e' holds).</dd>
	 *         <dt>'0'</dt>
	 *         <dd>The segments do not intersect (i.e., they share no points).
	 *         </dd>
	 *         </dl>
	 *         Note that two collinear segments that share just one point, an
	 *         endpoint of each, returns 'e' rather than 'v' as one might
	 *         expect.
	 */
	public static char lineIntersection(Pnt a, Pnt b, Pnt c, Pnt d, Pnt p) {
		/*
		 * This works by placing the line segments in parametric form and then
		 * by solving the two equations. Parametric form p(s) of a line segment
		 * ab is p(s) = a + s(b-a). The line segment is all values of s in
		 * [0,1]. Here, we find the parameters s for ab and t for cd that
		 * indicate the point of intersection. s = [a.x * (d.y - c.y) + c.x *
		 * (a.y - d.y) + d.x * (c.y - a.y) / D t = -[a.x * (c.y - b.y) + b.x *
		 * (a.y - c.y) + c.x * (b.y - a.y) / D D = a.x * (d.y - c.y) + b.x *
		 * (c.y - d.y) + c.x * (a.y - b.y) p = a + s(b-a)
		 */

		Rational temp1 = new Rational();
		Rational temp2 = new Rational();

		// denominator = a.x * (d.y - c.y)
		//             + b.x * (c.y - d.y)
		//             + c.x * (b.y - a.y)
		//             + d.x * (a.y - b.y)
		Rational denominator = new Rational(0);
		aPlusBTimesCMinusD(denominator, a.x, d.y, c.y, denominator, temp1,
				temp2);
		aPlusBTimesCMinusD(denominator, b.x, c.y, d.y, denominator, temp1,
				temp2);
		aPlusBTimesCMinusD(denominator, c.x, a.y, b.y, denominator, temp1,
				temp2);
		aPlusBTimesCMinusD(denominator, d.x, b.y, a.y, denominator, temp1,
				temp2);

		// If the segments ab and cd are parallel:
		if (denominator.equals(0)) {
			return collinearAndOverlap(a, b, c, d) ? 'e' : '0';
		}

		char code = '?';

		// Calculate the parametric form, first s then t

		//numerator = a.x * (d.y - c.y)
		//            + c.x * (a.y - d.y)
		//            + d.x * (c.y - a.y)
		Rational numerator = new Rational(0);
		aPlusBTimesCMinusD(numerator, a.x, d.y, c.y, numerator, temp1, temp2);
		aPlusBTimesCMinusD(numerator, c.x, a.y, d.y, numerator, temp1, temp2);
		aPlusBTimesCMinusD(numerator, d.x, c.y, a.y, numerator, temp1, temp2);

		// If an endpoint of one segment is on the other, but they aren't
		// colinear
		if (numerator.equals(0) || numerator.equals(denominator)) {
			code = 'v';
		}

		Rational s = new Rational();
		Rational.divide(numerator, denominator, s);

		//numerator = -(a.x * (c.y - b.y)
		//              + b.x * (a.y - c.y)
		//              + c.x * (b.y - a.y))
		numerator.setTo(0);
		aPlusBTimesCMinusD(numerator, a.x, c.y, b.y, numerator, temp1, temp2);
		aPlusBTimesCMinusD(numerator, b.x, a.y, c.y, numerator, temp1, temp2);
		aPlusBTimesCMinusD(numerator, c.x, b.y, a.y, numerator, temp1, temp2);
		numerator.negate();

		// If an endpoint of one segment is on the other, but they aren't
		// colinear
		if (numerator.equals(0) || numerator.equals(denominator)) {
			code = 'v';
		}

		Rational t = new Rational();
		Rational.divide(numerator, denominator, t);

		if (s.greaterThan(0) && s.lessThan(1) && t.greaterThan(0)
				&& t.lessThan(1)) {
			// it intersects properly
			code = '1';
		} else if (s.greaterThan(1) || s.lessThan(0) || t.greaterThan(1)
				|| t.lessThan(0)) {
			// they do not intersect
			code = '0';
		}

		// Set the point of intersection
		aPlusBTimesCMinusD(a.x, s, b.x, a.x, p.x, temp1, temp2);
		aPlusBTimesCMinusD(a.y, s, b.y, a.y, p.y, temp1, temp2);

		return code;
	}

	static private void aPlusBTimesCMinusD(Rational A, Rational B, Rational C,
			Rational D, Rational result, Rational tempDiff, Rational tempProduct) {
		Rational.minus(C, D, tempDiff);
		Rational.multiply(B, tempDiff, tempProduct);
		Rational.plus(A, tempProduct, result);
	}

	/**
	 * Tests to see if two line segments are collinear and overlap.
	 * 
	 * @param a
	 *            endpoint of the first line
	 * @param b
	 *            endpoint of the first line
	 * @param c
	 *            endpoint of the second line
	 * @param d
	 *            endpoint of the second line
	 * @return <code>true</code> iff the segments ab and cd share at least one
	 *         point and are collinear.
	 */
	static public boolean collinearAndOverlap(Pnt a, Pnt b, Pnt c, Pnt d) {
		if (!collinear(a, b, c)) {
			return false;
		} else {
			return c.between(a, b) || d.between(a, b);
		}
	}

	/**
	 * Checks to see if three points are collinear.
	 * 
	 * @param a
	 *            point to check
	 * @param b
	 *            point to check
	 * @param c
	 *            point to check
	 * @return <code>true</code> iff the three points lie on the same line.
	 */
	public static boolean collinear(Pnt a, Pnt b, Pnt c) {
		return areaSign(a, b, c).equals(0);
	}

	/**
	 * Calculates a minus b cross a minus c, or <code>(a-b)x(a-c)<code>.
	 * By the right hand rule, this returns positive values when a is to the
	 * left of bc and negative when it is to the right. The absolute value is
	 * the area of the parallelogram with points a, b, and c.
	 * @param a The origin of the parallelogram
	 * @param b The second point on the line
	 * @param c The third point.
	 * @return negative value if the three points are oriented clockwise, 
	 *         positive if CCW, and 0 if collinear.
	 */
	public static Rational areaSign(Component a, Component b, Component c) {
		Rational result = new Rational();
		Rational diff1 = new Rational();
		Rational diff2 = new Rational();

		// (b.x - a.x) * (c.y - a.y)
		// - (c.x - a.x) * (b.y - a.y)
		Rational.minus(b.x, a.x, diff1);
		Rational.minus(c.y, a.y, diff2);
		Rational.multiply(diff1, diff2, result);

		Rational.minus(c.x, a.x, diff1);
		Rational.minus(b.y, a.y, diff2);
		Rational.multiply(diff1, diff2, diff1);

		Rational.minus(result, diff1, result);
		return result;
	}

	/**
	 * Computes the 1-norm of the distance between two points.
	 * 
	 * @param a
	 *            A point
	 * @param b
	 *            A point
	 * @return The sum of the x and y differences between points a and b
	 */
	public static Rational manhattanDistance(Pnt a, Pnt b) {
		Component dist = a.minus(b);
		Rational result = new Rational();
		Rational.plus(dist.x.abs(), dist.y.abs(), result);
		return result;
	}

	/**
	 * Computes the 2-norm of the distance between two points.
	 * 
	 * @param a
	 *            A point
	 * @param b
	 *            A point
	 * @return The length of the line connecting a and b
	 */
	public static double euclideanDistance(Pnt a, Pnt b) {
		return a.minus(b).length();
	}

}