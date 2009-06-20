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

import edu.umd.cfar.lamp.viper.util.*;
import edu.umd.cfar.lamp.viper.util.reader.*;

/**
 * A class representing a Convex Polygon. Polygon with vertexes listed in
 * counterclockwise order such that all interior angles are less than 180
 * degrees. Unlike polylist or BoundingBox, this is limited to containing a
 * single polygon.
 * 
 * @see "Computational Geometry in C"
 */
public class ConvexPolygon extends PolyList implements Cloneable, HasCentroid {
	/** Polygon "P" is inside. Used for the intersection algorithm. */
	static final private int P_IN = -1;
	/** The first intersection hasn't been found. */
	static final private int UNKNOWN = 0;
	/** Polygon "Q" is inside. Used for the intersection algorithm. */
	static final private int Q_IN = 1;

	private List edgeList = null;

	/**
	 * Creates an empty convex polygon, whatever that means.
	 */
	public ConvexPolygon() {
		composed = false;
	}

	/**
	 * Creates a new convex polygon from the given set of tokens. Useful for
	 * parsing, as it will throw exceptions with the appropriate character
	 * offset information.
	 * 
	 * @param input
	 *            the tokenized string to parse
	 * @throws BadDataException
	 *             if the string isn't formatted properly
	 */
	public ConvexPolygon(CountingStringTokenizer input)
		throws BadDataException {
		composed = false;
		try {
			while (input.hasMoreTokens()) {
				int x = Integer.parseInt(input.nextToken());
				int y = Integer.parseInt(input.nextToken());
				if (!addVertex(new Pnt(x, y))) {
					//err.printWarning point is collinear
				}
			}
		} catch (NumberFormatException nfx) {
			throw (new BadDataException("Bad Polygon - " + input));
		}
	}

	/**
	 * Copy constructor.
	 * 
	 * @param old
	 *            the convex polygon to duplicate.
	 */
	public ConvexPolygon(ConvexPolygon old) {
		composed = false;
		if (old.edgeList != null) {
			edgeList = new ArrayList(old.edgeList.size());
			Iterator iter = old.getVerteces();
			while (iter.hasNext()) {
				try {
					addVertex((Pnt) iter.next());
				} catch (BadDataException bdx) {
					throw new ArithmeticException(bdx.getMessage());
				}
			}
		}
	}

	/**
	 * Initialize a convex polygon from a sequence of points.
	 * 
	 * @param points
	 *            a list of points as (x, y) pairs, such that point k's
	 *            cooridinates are (points[k/2], points[k/2 + 1]).
	 */
	public ConvexPolygon(int[] points) {
		int minX, minY, maxX, maxY;
		try {
			composed = false;
			int i = 2;
			minX = maxX = points[0];
			minY = maxY = points[1];
			addVertex(new Pnt(points[0], points[1]));
			while (i < points.length) {
				addVertex(new Pnt(points[i], points[i + 1]));
				minX = Math.min(minX, points[i]);
				maxX = Math.max(maxX, points[i]);
				i++;

				minY = Math.min(minY, points[i]);
				maxY = Math.max(maxY, points[i]);
				i++;
			}
		} catch (BadDataException bdx) {
			throw new IllegalStateException(
				"Error while making polygon: " + bdx.getMessage());
		}
	}

	/**
	 * Initialize a convex polygon from a sequence of points.
	 * 
	 * @param points
	 *            a list of viper.geometry.Pnt objects
	 */
	public ConvexPolygon(List points) {
		try {
			composed = false;
			for (Iterator iter = points.iterator(); iter.hasNext();) {
				Pnt curr = (Pnt) iter.next();
				addVertex(curr);
			}
		} catch (BadDataException bdx) {
			throw new IllegalStateException(
				"Error while making polygon: " + bdx.getMessage());
		}
	}

	protected void initBbox() {
		if (bbox != null) {
			return;
		}
		super.clearPolyList();
		super.addPoly(this);
		Iterator corners = getVerteces();
		if (corners.hasNext()) {
			Pnt leastPoint = (Pnt) corners.next();
			Pnt greatestPoint = new Pnt(leastPoint);
			Pnt currentPoint;

			while (corners.hasNext()) {
				currentPoint = (Pnt) corners.next();
				if (currentPoint.x.lessThan(leastPoint.x))
					leastPoint.x.setTo(currentPoint.x);
				else if (greatestPoint.x.lessThan(currentPoint.x))
					greatestPoint.x.setTo(currentPoint.x);

				if (currentPoint.y.lessThan(leastPoint.y))
					leastPoint.y.setTo(currentPoint.y);
				else if (greatestPoint.y.lessThan(currentPoint.y))
					greatestPoint.y.setTo(currentPoint.y);
			}
			Rational width = new Rational();
			Rational height = new Rational();
			Rational.minus(greatestPoint.x, leastPoint.x, width);
			Rational.minus(greatestPoint.y, leastPoint.y, height);
			bbox =
				new BoundingBox(
					leastPoint.x.doubleValue(),
					leastPoint.y.doubleValue(),
					width.doubleValue(),
					height.doubleValue());
		} else {
			bbox = new BoundingBox();
		}
	}

	/**
	 * Gets a copy of this polygon.
	 * 
	 * @return a new ConvexPolygon that equals <code>this</code>
	 */
	public Object clone() {
		return new ConvexPolygon(this);
	}

	/**
	 * Gets the region shared between this polygon and all those in the
	 * specified PolyList.
	 * 
	 * @param other
	 *            the region to intersect with
	 * @return a set of polygons that together covers only and all of the
	 *         region shared by this polygon and the specified region.
	 */
	public PolyList getIntersection(PolyList other) {
		if (other.composed || !(other instanceof ConvexPolygon)) {
			return PolyList.intersection(this, other);
		} else if (this.composed) {
			return PolyList.intersection(other, this);
		} else if (getBoundingBox().intersects(other.getBoundingBox())) {
			return ConvexPolygon.intersection(this, (ConvexPolygon) other);
		} else {
			return new ConvexPolygon();
		}
	}

	/**
	 * Get the point of intersection between the ray from the centroid of this
	 * box through q1 that is closest to q1.
	 * 
	 * @param q1
	 *            A point that is not the centroid.
	 * @return A point on the perimeter of the box on the ray from the centroid
	 *         through q1.
	 */
	public Pnt getNearIntersection(Pnt q1) {
		Pnt p1 = this.getCentroid();
		Pnt r1 = new Pnt();
		Pnt prev = getVertex(getNumberOfVerteces());
		boolean isNegative = false;
		Pnt curr = null;
		for (Iterator iter = getVerteces(); iter.hasNext(); prev = curr) {
			curr = (Pnt) iter.next();

			// Since points are CCW, the line we want is the first positivie
			// value.
			// They cannot all be negative, although one or two may be zero.
			Rational signArea = Util.areaSign(curr, p1, q1);
			if (isNegative && signArea.isPositive()) {
				Util.lineIntersection(p1, q1, prev, curr, r1);
				return r1;
			} else if (signArea.isNegative()) {
				isNegative = true;
			} else if (isNegative && signArea.isZero()) {
				isNegative = false;
				return new Pnt(curr);
			}
		}
		if (!isNegative) {
			throw new ArithmeticException("No near intersection with " + q1);
		} else {
			curr = getVertex(0);
			Util.lineIntersection(p1, q1, prev, curr, r1);
			return r1;
		}
	}

	/**
	 * Gets the center of the bounding box of the polygon, for now.
	 * 
	 * @return supposed to be the center of the polygon
	 */
	public Pnt getCentroid() {
		if (this instanceof BoxInformation) {
			return ((BoxInformation) this).getCentroid();
		} else {
			return this.getBoundingBox().getCentroid();
		}
	}

	/**
	 * Cuts the polygon into two slices. If it cuts it, returns a two-element
	 * array, with the first being the area to the left of the segment, and the
	 * second with the region to the right. If it does not segment the polygon,
	 * it returns a one element array containing a copy of the original
	 * polygon.
	 * 
	 * @param P
	 *            the polygon to slice
	 * @param a
	 *            a point along the slicing line
	 * @param b
	 *            another point, != to a, along the slicing line
	 * @return one or two polygons such that the returned polygons union to the
	 *         specified polygon but contain no edges that cross the slicing
	 *         line
	 */
	static public ConvexPolygon[] clip(ConvexPolygon P, Pnt a, Pnt b) {
		// Collect points in two halves - those to the left and right.
		// the ones at the end mark the line segments that are cut.
		// As a point of reference, corners on the line are marked as
		// on both. Then generate the new corners and make the polygons
		List left = new LinkedList();
		List right = new LinkedList();
		List left2 = new LinkedList();
		List right2 = new LinkedList();
		Pnt collinear1 = null;
		Pnt collinear2 = null;
		boolean alreadyright = false;
		boolean alreadyleft = false;
		boolean alreadycollinear = false;
		Rational ZERO = new Rational(0);
		for (Iterator corners = P.getVerteces(); corners.hasNext();) {
			Pnt curr = (Pnt) corners.next();
			Rational side = Util.areaSign(curr, a, b);

			if (side.lessThan(ZERO)) {
				if (alreadyright) {
					right.add(curr);
				} else {
					right2.add(curr);
				}
			} else if (right2.size() > 0) {
				alreadyright = true;
			}

			if (side.greaterThan(ZERO)) {
				if (alreadyleft) {
					left.add(curr);
				} else {
					left2.add(curr);
				}
			} else if (left2.size() > 0) {
				alreadyleft = true;
			}

			if (side.equals(ZERO)) {
				if (alreadycollinear) {
					collinear2 = curr;
				} else {
					collinear1 = curr;
				}
			} else if (collinear1 != null) {
				alreadycollinear = true;
			}
		}

		left.addAll(left2);
		right.addAll(right2);

		if (left.size() == 0 || right.size() == 0) {
			return new ConvexPolygon[] { new ConvexPolygon(P)};
		} else {
			// so the collinear points must be on the polygon
			// between the left and right segments
			// To find out which one goes where, place them where
			// they are convex
			if (collinear1 != null) {
				if (Util
					.areaSign(
						collinear1,
						(Pnt) left.get(0),
						(Pnt) right.get(right.size() - 1))
					.lessThan(ZERO)) {
					left.add(collinear1);
					right.add(0, collinear1);
					if (collinear2 != null) {
						left.add(0, collinear2);
						right.add(collinear2);
					}
				} else {
					left.add(0, collinear1);
					right.add(collinear1);
					if (collinear2 != null) {
						left.add(collinear2);
						right.add(0, collinear2);
					}
				}
			}

			Pnt leftStart = (Pnt) left.get(0);
			Pnt leftEnd = (Pnt) left.get(left.size() - 1);
			Pnt rightStart = (Pnt) right.get(0);
			Pnt rightEnd = (Pnt) right.get(right.size() - 1);

			if (!leftStart.equals(rightEnd)) {
				// The corner does not lie on the line, so segment the line
				// and add the point to both sides
				Pnt neo = new Pnt();
				Util.lineIntersection(a, b, leftStart, rightEnd, neo);
				left.add(0, neo);
				right.add(neo);
			}
			if (!rightStart.equals(leftEnd)) {
				Pnt neo = new Pnt();
				Util.lineIntersection(a, b, rightStart, leftEnd, neo);
				left.add(neo);
				right.add(0, neo);
			}
		}
		return new ConvexPolygon[] {
			new ConvexPolygon(left),
			new ConvexPolygon(right)};
	}

	/**
	 * Subtracts the second shape from the first.
	 * 
	 * @param P
	 *            the shape to subtract from
	 * @param Q
	 *            the shape to subtract
	 * @return P - Q, or P ^ ~Q
	 */
	static public PolyList subtract(ConvexPolygon P, ConvexPolygon Q) {
		ConvexPolygon I = ConvexPolygon.intersection(P, Q);
		if (I.area().greaterThan(0)) {
			ConvexPolygon copy = new ConvexPolygon(P);
			PolyList diff = new PolyList();
			// Will work like this: try to get one convex polygon
			// for each edge of the intersection polygon.
			// Uses the "crop" function, that crops the polygon into
			// two segments on either side of a line. I know this is
			// not efficient, but I am just looking for something
			// that works.
			Iterator corners = I.getVerteces();
			Pnt curr = I.getVertex(I.getNumberOfVerteces() - 1);
			Pnt prev;

			while (corners.hasNext()) {
				prev = curr;
				curr = (Pnt) corners.next();
				// The edgelist goes counterclockwise. As such,
				// the region to the left of the line connecting
				// prev->curr contains the hole, and to the right stays.
				ConvexPolygon[] segs = ConvexPolygon.clip(copy, prev, curr);
				if (segs.length == 1) {
					// this case is uninteresting
				} else {
					copy = segs[0];
					diff.addPoly(segs[1]);
				}
			}
			return diff;
		} else {
			return new ConvexPolygon(P);
		}
	}

	/**
	 * Returns a set of polygons that, together, cover all and only the region
	 * covered by the specified polygons.
	 * 
	 * @param P
	 *            a convex polygon to union
	 * @param Q
	 *            a convex polygon to union
	 * @return a set of non-overlapping convex polygons that represents the
	 *         union of P and Q
	 */
	static public ConvexPolygon[] add(ConvexPolygon P, ConvexPolygon Q) {
		ConvexPolygon[] r = null;
		ConvexPolygon split = intersection(P, Q);
		if (split.area().equals(0)) {
			// Two distinct polygons
			r = new ConvexPolygon[] { P, Q };
		} else if (split.area() == P.area() || split.area() == Q.area()) {
			r = new ConvexPolygon[] {(P.area().lessThan(Q.area)) ? Q : P };
		} else {
			r = PolyList.union(P, Q).getConvexPolygonArray();
		}
		return r;
	}

	/**
	 * Tests to see if the specified polygon is completely contained within
	 * this polygon.
	 * 
	 * @param P
	 *            the polygon to check for
	 * @return <code>true</code> iff all of P is within this polygon
	 */
	public boolean isInside(ConvexPolygon P) {
		boolean hasPoints = false;
		for (Iterator iter = getVerteces(); iter.hasNext();) {
			hasPoints = true;
			Pnt curr = (Pnt) iter.next();
			if (!P.contains(curr)) {
				return false;
			}
		}
		return hasPoints;
	}

	/**
	 * This creates a new area that is the intersection of both.
	 * 
	 * @param P a polygon to intersect
	 * @param Q a polygon to intersect
	 * @return the polygon representing the region in both polygons
	 */
	static public ConvexPolygon intersection(
		ConvexPolygon P,
		ConvexPolygon Q) {
		if (!P.getBoundingBox().intersects(Q.getBoundingBox())) {
			return new ConvexPolygon();
		}
		ConvexPolygon solutionPoly = new ConvexPolygon();
		int n = P.getNumberOfVerteces();
		int m = Q.getNumberOfVerteces();
		int a = 0;
		int b = 0;
		int a1, b1;
		Component A, B;
		Rational crossProduct;
		Rational bHa, aHb;
		Pnt Origin = new Pnt(0, 0);
		Pnt p = new Pnt();
		Pnt q = new Pnt();
		int inflag = UNKNOWN; // -1 = outside, 0 = UNKNOWN, +1 - inside
		int aa = 0;
		int ba = 0;
		boolean firstPoint = true;
		char code;

		// Advance around the edge of both polygons, so that the
		// they chase each other. Save the relevant verteces to the edge
		// list.
		do {
			/* Calculate key variables */
			a1 = a - 1;
			b1 = b - 1;
			A = P.getVertex(a1).minus(P.getVertex(a));
			B = Q.getVertex(b1).minus(Q.getVertex(b));
			crossProduct = Util.areaSign(Origin, A, B);
			aHb =
				Util.areaSign(Q.getVertex(b1), Q.getVertex(b), P.getVertex(a));
			bHa =
				Util.areaSign(P.getVertex(a1), P.getVertex(a), Q.getVertex(b));

			/* If A & B intersect, update inflag. */
			code =
				Util.lineIntersection(
					P.getVertex(a1),
					P.getVertex(a),
					Q.getVertex(b1),
					Q.getVertex(b),
					p);

			if (code == '1' || code == 'v') {
				if (inflag == UNKNOWN && firstPoint) {
					aa = ba = 0;
					firstPoint = false;
				}
				try {
					solutionPoly.addVertex(p);
				} catch (BadDataException bdx) {
					StringBuffer errMsg = new StringBuffer();
					errMsg
						.append("While intersecting ")
						.append(P.toString())
						.append(" with ")
						.append(Q.toString())
						.append(" adding ")
						.append(P.toString());
					errMsg.append("\n\t").append(bdx.getMessage());
					errMsg.append("\n\t").append(" to get " + solutionPoly);
					throw new ArithmeticException(errMsg.toString());
				}
				inflag = inOut(inflag, aHb, bHa);
			}

			/* Advance! */
			/* A & B colinear in opposite directions */
			if ((code == 'e') && (Component.dot(A, B).lessThan(0))) {
				try {
					solutionPoly.clearPolygon();
					solutionPoly.addVertex(p);
					solutionPoly.addVertex(q);
				} catch (BadDataException bdx) {
					String s =
						"\nWhile intersecting "
							+ P
							+ " with "
							+ Q
							+ " to get "
							+ solutionPoly;
					throw new ArithmeticException(bdx.getMessage() + s);
				}
				solutionPoly.initBbox();
				return solutionPoly;
			}

			/* Special case: A & B parallel and separated. */
			else if (
				crossProduct.equals(0) && aHb.lessThan(0) && bHa.lessThan(0)) {
				return solutionPoly;
			}

			/* Special case: A & B collinear. */
			else if (
				crossProduct.equals(0) && aHb.equals(0) && bHa.equals(0)) {
				/* Advance but do not output point. */
				if (inflag == P_IN) {
					ba++;
					b++;
				} else {
					aa++;
					a++;
				}
			}

			/* Generic cases. */
			else if (
				(!crossProduct.lessThan(0) && bHa.greaterThan(0))
					|| (crossProduct.lessThan(0) && !aHb.greaterThan(0))) {
				if (inflag == P_IN) {
					try {
						solutionPoly.addVertex(P.getVertex(a));
					} catch (BadDataException bdx) {
						String s =
							"\nWhile intersecting "
								+ P
								+ " with "
								+ Q
								+ " to get "
								+ solutionPoly;
						throw new ArithmeticException(bdx.getMessage() + s);
					}
				}
				aa++;
				a++;
			} else if (
				(!crossProduct.lessThan(0) && !bHa.greaterThan(0))
					|| (crossProduct.lessThan(0) && aHb.greaterThan(0))) {
				if (inflag == Q_IN) {
					try {
						solutionPoly.addVertex(Q.getVertex(b));
					} catch (BadDataException bdx) {
						String s =
							"\nWhile intersecting "
								+ P
								+ " with "
								+ Q
								+ " to get "
								+ solutionPoly;
						throw new ArithmeticException(bdx.getMessage() + s);
					}
				}
				ba++;
				b++;
			}

			/*
			 * Quit when both adv. indices have cycled, or one has cycled
			 * twice.
			 */
		} while (((aa < n) || (ba < m)) && (aa < 2 * n) && (ba < 2 * m));

		// If the boundaries don't cross, see if one is inside the other
		if (inflag == 0) {
			if (P.contains(Q.getVertex(0))) {
				return new ConvexPolygon(Q);
			} else if (Q.contains(P.getVertex(0))) {
				return new ConvexPolygon(P);
			} else { // Boundaries don't cross, one is not inside the other
				return new ConvexPolygon();
			}
		}
		solutionPoly.initBbox();
		return solutionPoly;
	}

	/**
	 * Adds a single point to the polygon. Note that this
	 * must maintain the convexity property.
	 * 
	 * Before calling this, allocate edge. After calling this, call initBbox.
	 * 
	 * @param point the point to add to the polygon
	 * @return true if added, false if collinear
	 * @throws BadDataException
	 *             if attempting to add reflex vertex
	 */
	public boolean addVertex(Pnt point) throws BadDataException {
		if (point == null) {
			throw new NullPointerException("Trying to add a null point");
		}
		if (edgeList == null) {
			edgeList = new LinkedList();
		} else {
			int length = edgeList.size();
			if (length > 2) {
				Pnt a = getVertex(getNumberOfVerteces() - 1);
				Pnt b = getVertex(getNumberOfVerteces() - 2);
				Rational isOnEdge = Util.areaSign(point, getVertex(0), a);
				if (isOnEdge.equals(0)) {
					// three cases - either it is between the points and
					// redundant,
					// or it is to one side or the other and bad.
					if (Util
						.areaSign(point, getVertex(0), getVertex(1))
						.lessThan(0)
						|| Util.areaSign(point, b, a).lessThan(0)) {
						throw new BadDataException(
							"Trying to add a non-convex point "
								+ point
								+ " to a convex polygon "
								+ toStringListOfPoints()
								+ "\n isOnEdge = "
								+ isOnEdge);
					} else {
						return false;
					}
				} else if (isOnEdge.lessThan(0)) {
					throw new BadDataException(
						"Trying to add a non-convex point "
							+ point
							+ " to a convex polygon "
							+ toStringListOfPoints()
							+ "\n isOnEdge = "
							+ isOnEdge);
				}
				isOnEdge = Util.areaSign(point, b, a);
				if (isOnEdge.lessThan(0)) {
					throw new BadDataException(
						"Trying to add a reflex point "
							+ point
							+ " to a convex polygon "
							+ toStringListOfPoints()
							+ "\n isOnEdge = "
							+ isOnEdge);
				} else if (isOnEdge.equals(0)) {
					// This means that the most recent segment is being
					// extended
					edgeList.remove(length - 1);
				} // else it is in the right region and can simply be added
			} else if ((length == 1) && (point.equals(getVertex(0)))) {
				return false;
			} else if (length == 2) {
				Rational isOnEdge =
					Util.areaSign(point, getVertex(0), getVertex(1));
				if (!isOnEdge.greaterThan(0)) {
					if (isOnEdge.equals(0)) {
						return false;
					} else {
						throw new BadDataException(
							"Trying to add a non-convex point "
								+ point
								+ " to a convex polygon "
								+ toStringListOfPoints()
								+ "\n isOnEdge = "
								+ isOnEdge);
					}
				}
			}
		}
		edgeList.add(new Pnt(point));
		bbox = null;
		return true;
	}

	private Pnt getVertex(int index) {
		int length = getNumberOfVerteces();
		if (length == 0) {
			return null;
		}
		if (index < 0) {
			index += length;
		}
		return new Pnt((Pnt) edgeList.get(index % length));
	}

	int getNumberOfVerteces() {
		return (edgeList == null) ? 0 : edgeList.size();
	}

	public Iterator getVerteces() {
		if (edgeList == null) {
			return new Iterator() {
				public boolean hasNext() {
					return false;
				}
				public Object next() {
					throw new NoSuchElementException();
				}
				public void remove() {
					throw new UnsupportedOperationException();
				}
			};
		} else {
			return new Iterator() {
				private Iterator iter = edgeList.iterator();
				public boolean hasNext() {
					return iter.hasNext();
				}
				public Object next() {
					return new Pnt((Pnt) iter.next());
				}
				public void remove() {
					throw new UnsupportedOperationException();
				}
			};
		}
	}

	protected void clearPolygon() {
		edgeList = null;
	}
 
	/**
	 * Toggles in/out flag. See Computational Geometry in C
	 * 
	 * @param inflag The previous value for the inflag.
	 * @param aHb 
	 * @param bHa 
	 * @return The new value for the inflag.
	 */
	static private int inOut(int inflag, Rational aHb, Rational bHa) {
		/* Update inflag. */
		if (aHb.greaterThan(0)) {
			return P_IN;
		} else if (bHa.greaterThan(0)) {
			return Q_IN;
		} else /* Keep status quo. */
			return inflag;
	}

	/**
	 * Gets a String representation of the polygon as a
	 * list of points, where each point is given as (x,y).
	 * @return the points around the edge of the polygon
	 */
	public final String toStringListOfPoints() {
		if (composed) {
			return super.toStringListOfPoints();
		}
		StringBuffer buf = new StringBuffer();
		buf.append('(');
		Iterator pointIterator = getVerteces();
		while (pointIterator.hasNext()) {
			buf.append(pointIterator.next().toString());
		}
		buf.append(')');
		return buf.toString();
	}

	/**
	 * Gets a string representation of the polygon.
	 * If it is composed, uses the PolyList version.
	 * @return a String representation of the object
	 */
	public String toString() {
		if (composed) {
			return super.toString();
		}
		return toStringListOfPoints();
	}

	/**
	 * Finds the area inside the polygon.
	 * @return an approximation of the polygon's size
	 */
	public Rational area() {
		if (composed) {
			return super.area();
		}
		if (getNumberOfVerteces() < 3) {
			return new Rational(0);
		}

		// This works by finding the area of the trapezoid between
		// the x axis and each line segment (not parallel to y).
		// If the line points from left to right, it must be on
		// the bottom of the trapezoid.
		Rational total = new Rational(0);
		Iterator pointIterator = getVerteces();
		Pnt currPoint = (Pnt) pointIterator.next();
		Rational nextArea = new Rational(0);
		Rational width = new Rational(0);
		Rational height = new Rational(0);
		Pnt nextPoint;
		while (pointIterator.hasNext()) {
			nextPoint = (Pnt) pointIterator.next();
			// If it is a bottom edge, subtract the area beneath it.
			// Thankfully, if it is a bottom edge, curr.x - next.x
			// returns the negation of the length in the x direction!
			Rational.minus(currPoint.x, nextPoint.x, width);
			Rational.plus(nextPoint.y, currPoint.y, height);
			Rational.multiply(width, height, nextArea);
			Rational.plus(total, nextArea, total);
			currPoint = nextPoint;
		}
		nextPoint = getVertex(0);
		Rational.minus(currPoint.x, nextPoint.x, width);
		Rational.plus(nextPoint.y, currPoint.y, height);
		Rational.multiply(width, height, nextArea);
		Rational.plus(total, nextArea, total);

		nextArea.setTo(1, 2);
		Rational.multiply(total, nextArea, total);
		return total;
	}

	/**
	 * Tests to see if the point is within this region.
	 * @param X the x-coordinate of the point to check
	 * @param Y the y-coordinate of the point to check
	 * @return <code>true</code> iff the point is within the polygon
	 */
	public boolean contains(int X, int Y) {
		if (bbox.contains(X, Y)) {
			return contains(new Pnt(X, Y));
		} else {
			return false;
		}
	}

	/**
	 * Tests to see if the point is within this region.
	 * @param point the point to look for within the polygon
	 * @return <code>true</code> iff the point is within the polygon
	 */
	public boolean contains(Pnt point) {
		int length = getNumberOfVerteces();
		if (length == 1) {
			return getVertex(0).equals(point);
		}
		for (int i = 0; i < length; i++) {
			Pnt a = getVertex(i - 1);
			Pnt b = getVertex(i);
			if (Util.areaSign(a, b, point).lessThan(0))
				return false;
		}
		return true;
	}

	/**
	 * Tests to see if this region is the same as 
	 * that covered by the specified shape.
	 * @param o the shape to check against. Works for most children of PolyList
	 * @return <code>false</code> if the two regions are unequal
	 */
	public boolean equals(Object o) {
		if (this == o) {
			return true;
		} else if (o instanceof ConvexPolygon) {
			if (composed || ((ConvexPolygon) o).composed) {
				return super.equals(o);
			}

			ConvexPolygon other = (ConvexPolygon) o;
			if (!this.getBoundingBox().equals(other.getBoundingBox())) {
				return false; // The bounding boxes are of different size
			}

			int length = getNumberOfVerteces();
			if (length != other.getNumberOfVerteces())
				return false; // They have a different number of edges

			int offset = 0;
			Iterator iterThis = this.getVerteces();
			Iterator iterOther = other.getVerteces();

			// Find a common point, and compare. All the points should
			// be the same.
			Pnt curr = (Pnt) iterThis.next();
			Pnt opposite = (Pnt) iterOther.next();
			while (iterThis.hasNext() && !curr.equals(opposite)) {
				curr = (Pnt) iterThis.next();
				offset++;
			}
			if (offset == length)
				return false; // There is no common point

			offset = 1;
			while (offset++ < length) {
				if (!iterThis.hasNext())
					iterThis = this.getVerteces();
				curr = (Pnt) iterThis.next();

				opposite = (Pnt) iterOther.next();
				if (!opposite.equals(curr))
					return false; //These points should line up
			}
			return true; // All of the points line up
		} else {
			return false;
		}
	}
	
	public int hashCode() {
		return new HashCodeBuilder().append(area()).append(getBoundingBox()).toHashCode();
	}

	/**
	 * Gets all the component polygons of this set.
	 * 
	 * @return an iterator that just returns one element, <code>this</code>
	 */
	public Iterator getPolys() {
		ArrayList temp = new ArrayList(1);
		temp.add(this);
		return temp.iterator();
	}
}
