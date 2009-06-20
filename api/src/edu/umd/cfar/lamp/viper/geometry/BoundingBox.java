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

import org.apache.commons.lang.builder.*;

import viper.api.*;
import edu.umd.cfar.lamp.viper.util.*;

/**
 * This class reprsents a rectangle aligned with the coordinate axes or a set
 * of such rectangles.
 */
public class BoundingBox
	extends ConvexPolygon
	implements BoxInformation, Moveable, HasCentroid {
	/**
	 * Instead of having a small inheritance tree, I am using one class that
	 * can be either one of two types, a box or list of boxes. If this is set
	 * to true, then it is a list of boxes. Otherwise, it is a box. I hope this
	 * is the right idea.
	 */
	Rectangle rect;
	LinkedList pieces;

	/**
	 * Construct the empty bounding box.
	 */
	public BoundingBox() {
		rect = new Rectangle();
	}

	/**
	 * Construct a bounding box with the given origin, width and height.
	 * 
	 * @param dimensions
	 *            int array in the form {x, y, width, height}
	 */
	public BoundingBox(int[] dimensions) {
		rect =
			new Rectangle(
				dimensions[0],
				dimensions[1],
				dimensions[2],
				dimensions[3]);
	}

	/**
	 * Construct a rectangle with the given parameters. Note that in screen
	 * coordinates, the origin is the top left edge.
	 * 
	 * @param leftEdge
	 *            the distance of the box from the y axis
	 * @param bottomEdge
	 *            the distance of the box from the x axis (in screen
	 *            coordinates, a more appropriate name would be <code>topEdge</code>
	 * @param width
	 *            the width of the box
	 * @param height
	 *            the height of the box
	 */
	public BoundingBox(int leftEdge, int bottomEdge, int width, int height) {
		rect = new Rectangle(leftEdge, bottomEdge, width, height);
	}

	/**
	 * Creates a new bounding box from the given <code>java.awt.Rectangle</code>.
	 * 
	 * @param dimensions
	 *            the box to use
	 */
	public BoundingBox(Rectangle dimensions) {
		rect = (Rectangle) dimensions.clone();
	}

	/**
	 * Creates a bounding box in double coordinates. Just casts the parameters
	 * to ints, for now.
	 * 
	 * @param leftEdge
	 *            the left edge
	 * @param bottomEdge
	 *            the bottom edge (top in screen coordinates)
	 * @param width
	 *            the width of the box
	 * @param height
	 *            the height of the box
	 */
	public BoundingBox(
		double leftEdge,
		double bottomEdge,
		double width,
		double height) {
		rect =
			new Rectangle(
				(int) leftEdge,
				(int) bottomEdge,
				(int) Math.ceil(width),
				(int) Math.ceil(height));
	}

	private void initPoly() {
		try {
			clearPolygon();

			Pnt temp = new Pnt(rect.x, rect.y);
			addVertex(temp);

			temp = new Pnt(rect.x + rect.width, rect.y);
			addVertex(temp);

			temp = new Pnt(rect.x + rect.width, rect.y + rect.height);
			addVertex(temp);

			temp = new Pnt(rect.x, rect.y + rect.height);
			addVertex(temp);

			bbox = this;
		} catch (BadDataException bdx) {
			throw new RuntimeException(
				"Reflex Rectangle Error! " + bdx.getMessage());
		}
	}

	/**
	 * This creates a new box that represents the area shared by two boxes.
	 * 
	 * @param A
	 *            a box to intersect
	 * @param B
	 *            a box to intersect
	 * @return a new bbox consisting solely of the shared region.
	 */
	public static BoundingBox intersection(BoundingBox A, BoundingBox B) {
		BoundingBox solution = new BoundingBox();
		//If we know this is going nowhere, bail
		if (!A.rect.intersects(B.rect))
			return solution;
		solution.rect = null;

		// The hard case -- two composed Bounding Boxes
		if (A.composed && B.composed) {
			solution.pieces = new LinkedList();
			solution.composed = true;

			Iterator iterA = A.pieces.iterator();
			while (iterA.hasNext()) {
				BoundingBox aBox = (BoundingBox) iterA.next();
				Iterator iterB = B.pieces.iterator();
				while (iterB.hasNext()) {
					BoundingBox bBox = (BoundingBox) iterB.next();
					assert !aBox.composed
						&& !bBox.composed : "BoundingBox wasn't flattened.";
					if (aBox.intersects(bBox)) {
						BoundingBox newBox =
							new BoundingBox(
								(Rectangle) aBox.rect.createIntersection(
									bBox.rect));

						if (solution.rect == null) {
							solution.rect = (Rectangle) newBox.rect.clone();
						} else {
							Rectangle2D.union(
								solution.rect,
								newBox.rect,
								solution.rect);
						}
						solution.pieces.add(newBox);
					}
				}
			}
			solution.simplify();

		} else if (A.composed || B.composed) {
			// Now handle the case where one is composed.
			BoundingBox uncomposedBox = (A.composed ? B : A);
			BoundingBox composedBox = (A.composed ? A : B);

			solution.pieces = new LinkedList();
			solution.composed = true;

			Iterator iter = composedBox.pieces.iterator();
			while (iter.hasNext()) {
				BoundingBox child = (BoundingBox) iter.next();
				assert !child.composed : "BoundingBox wasn't flattened.";
				if (child
					.rect
					.intersects(
						uncomposedBox.rect.x,
						uncomposedBox.rect.y,
						uncomposedBox.rect.width,
						uncomposedBox.rect.height)) {
					BoundingBox newBox =
						new BoundingBox(
							(Rectangle) child.rect.createIntersection(
								uncomposedBox.rect));
					if (solution.rect == null) {
						solution.rect = (Rectangle) newBox.rect.clone();
					} else {
						Rectangle2D.union(
							solution.rect,
							newBox.rect,
							solution.rect);
					}
					solution.pieces.add(newBox);
				}
			}
			solution.simplify();

		} else {
			// The easy case.
			if (A
				.rect
				.intersects(B.rect.x, B.rect.y, B.rect.width, B.rect.height)) {
				solution.rect = (Rectangle) A.rect.createIntersection(B.rect);
				solution.initPoly();
			} else {
				solution.rect = new Rectangle();
			}
		}
		return solution;
	}

	/**
	 * Tests whether <code>this</code> box intersects the specified box.
	 * 
	 * @param other
	 *            the other box to test against
	 * @return <code>true</code> iff a pixel is shared between the two
	 */
	public boolean intersects(BoundingBox other) {
		if (!this.composed && !other.composed) {
			return this.rect.intersects(other.rect);
		} else {
			// At least one is composed
			if (!this.getBoundingBox().intersects(other.getBoundingBox()))
				return false;

			if (this.composed && other.composed) {
				BoundingBox tempThis;
				Iterator iterThis = this.pieces.iterator();
				while (iterThis.hasNext()) {
					tempThis = (BoundingBox) iterThis.next();

					BoundingBox tempOther;
					Iterator iterOther = other.pieces.iterator();
					while (iterOther.hasNext()) {
						tempOther = (BoundingBox) iterOther.next();

						if (tempThis.rect.intersects(tempOther.rect))
							return true;
					}
				}
				return false;
			} else if (this.composed) {
				BoundingBox temp;
				Iterator iter = this.pieces.iterator();
				while (iter.hasNext()) {
					temp = (BoundingBox) iter.next();
					if (temp.rect.intersects(other.rect))
						return true;
				}
				return false;
			} else {
				return other.intersects(this);
			}
		}
	}

	/**
	 * Creates a new Box from a string representation.
	 * 
	 * @param S
	 *            a string representation, a series of 4 numbers representing
	 *            the bottom left corner, width, and height
	 * @return a <code>BoundingBox</code> represented by the string
	 * @throws BadAttributeDataException
	 *             if the String is malformed
	 */
	public static BoundingBox valueOf(String S) {
		try {
			return new BoundingBox(S);
		} catch (BadDataException e) {
			throw new BadAttributeDataException();
		}
	}

	/**
	 * Creates a new Box from a string representation.
	 * 
	 * @param S
	 *            a string representation, a series of 4 numbers representing
	 *            the bottom left corner, width, and height
	 * @throws BadDataException
	 *             if the String is malformed
	 */
	public BoundingBox(String S) throws BadDataException {
		try {
			StringTokenizer st = new StringTokenizer(S);
			rect =
				new Rectangle(
					Integer.parseInt(st.nextToken()),
					Integer.parseInt(st.nextToken()),
					Integer.parseInt(st.nextToken()),
					Integer.parseInt(st.nextToken()));
			initPoly();
		} catch (NumberFormatException nfx) {
			rect = new Rectangle();
			initPoly();
			throw (new BadDataException("Bad bbox - " + S));
		}
	}

	/**
	 * Call this after changing the rectangle in any way. It removes rectangles
	 * of size less than 1, and it also converts "composed" rectangles with
	 * only one rectangle to uncomposed; it also updates the polygon.
	 */
	private void simplify() {
		boolean startedComposed = composed;
		if (composed) {
			if (pieces.size() == 0) {
				pieces = null;
				composed = false;
				rect = new Rectangle();
			} else if (pieces.size() == 1) {
				BoundingBox child = (BoundingBox) pieces.getFirst();
				composed = false;
				if (child != null)
					setTo(child);
				else
					pieces.clear();
			} else if (pieces.size() > 1) {
				boolean changed = false;
				ListIterator iter = pieces.listIterator(0);
				while (iter.hasNext())
					if (!(((BoundingBox) iter.next()).area()).greaterThan(0)) {
						iter.remove();
						changed = true;
					}
				if (changed)
					simplify();
			}
		}
		if (startedComposed && !composed)
			initPoly();
	}

	/**
	 * Like an overloaded '='. This recycles as much of the data structure as
	 * possible.
	 * 
	 * @param other
	 *            the box(es) to union with this bbox set
	 * @return <code>this</code>
	 */
	public BoundingBox setTo(BoundingBox other) {
		if (other.composed) {
			composed = true;
			if (pieces == null)
				pieces = new LinkedList();
			else
				pieces.clear();
			Iterator iter = other.pieces.iterator();
			while (iter.hasNext())
				pieces.add(((BoundingBox) iter.next()).clone());
		} else {
			composed = false;
			if (pieces != null)
				pieces = null;
			if (other.rect == null) {
				rect.x = rect.y = rect.width = rect.height = 0;
			} else {
				if (rect != null) {
					rect.x = other.rect.x;
					rect.y = other.rect.y;
					rect.width = other.rect.width;
					rect.height = other.rect.height;
				} else {
					rect = (Rectangle) other.rect.clone();
				}
			}
		}
		bbox = null;
		return this;
	}

	/**
	 * Sets <code>this</code> to refer to only the given bounding box.
	 * 
	 * @param x
	 *            the x coordinate of the box origin point
	 * @param y
	 *            the y coordinate of the box origin point
	 * @param width
	 *            the width of the box
	 * @param height
	 *            the height of the box
	 */
	public void set(int x, int y, int width, int height) {
		rect = new Rectangle(x, y, width, height);
		initPoly();
	}

	/**
	 * Like clone, but returns the right type.
	 * 
	 * @return an exact copy of <code>this</code>
	 */
	public BoundingBox copy() {
		if (composed) {
			BoundingBox temp = new BoundingBox();
			temp.setTo(this);
			return temp;
		} else {
			return new BoundingBox(
				rect.x,
				rect.y,
				rect.width,
				rect.height);
		}
	}

	/**
	 * Copies the box(es).
	 * 
	 * @return a new copy of the data
	 */
	public Object clone() {
		return copy();
	}

	/**
	 * Tests to see if any of the boxes contains the specified point
	 * 
	 * @param point
	 *            the point to check for
	 * @return <code>true</code> iff at least one of the boxes in this set
	 *         contains point
	 */
	public boolean contains(Pnt point) {
		if (rect.contains(point.pointValue())) {
			if (composed) {
				Iterator iter = pieces.iterator();
				BoundingBox temp;
				while (iter.hasNext()) {
					temp = (BoundingBox) iter.next();
					if (temp.contains(point))
						return true;
				}
				return false;
			} else {
				return true;
			}
		} else {
			return false;
		}
	}

	protected void initBbox() {
		if (bbox != null) {
			return;
		}
		bbox = new BoundingBox(rect);
		initPoly();
	}
	
	/**
	 * Gets a parsable, human readable version of the box(es). If a single box,
	 * it returns a space delimited string containing four integers. If more,
	 * it returns a list of such four-lists, each surrounded in parentheses,
	 * with the whole list in brackets. Note that this list will be of
	 * non-overlapping boxes that, together, cover the same area as the set of
	 * boxes that this loosely represents.
	 * 
	 * @return a String version of the box(es)
	 */
	public String toString() {
		if (composed) {
			StringBuffer S = new StringBuffer().append("[");
			Iterator iter = pieces.iterator();
			while (iter.hasNext())
				S.append("(" + iter.next() + ")");
			S.append("]");
			return S.toString();
		} else {
			return rect.x
					+ " "
					+ rect.y
					+ " "
					+ rect.width
					+ " "
					+ rect.height;
		}
	}

	/**
	 * Calculates the area of the box(es)
	 * 
	 * @return the total number of pixels covered by the box(es)
	 */
	public Rational area() {
		if (composed) {
			Rational area = new Rational(0);
			Iterator iter = pieces.iterator();
			while (iter.hasNext())
				Rational.plus(area, ((BoundingBox) iter.next()).area(), area);
			return area;
		} else {
			return new Rational(rect.width * rect.height);
		}
	}

	/**
	 * Checks to see if the shapes are equal.
	 * 
	 * @param o
	 *            the shape to check. Works for most children of
	 *            {@link PolyList}.
	 * @return <code>true</code> if the given shape covers the same pixels as
	 *         this set of boxes
	 */
	public boolean equals(Object o) {
		if (o == this) {
			return true;
		} else if (o instanceof BoundingBox) {
			BoundingBox other = (BoundingBox) o;
			if (!composed && !other.composed) {
				return (other.rect.equals(rect));
			} else {
				return area() == BoundingBox.intersection(this, other).area();
			}
		} else {
			return false;
		}
	}
	
	public int hashCode() {
		return new HashCodeBuilder().append(composed).append(rect).toHashCode();
	}

	/**
	 * Subtracts the area of <code>this</this> from lowerBox
	 * and returns a linked list of BoundingBoxes such that 
	 * the area of all the boxes in the list + the area 
	 * of the lower box is equal to the area covered by this
	 * and the lower box, but with no overlap. 
	 * @param lowerBox the region to slice
	 * @return this, less the complement of <code>lowerBox</code>
	 */
	private LinkedList subtractFrom(BoundingBox lowerBox) {
		BoundingBox crossBox = BoundingBox.intersection(lowerBox, this);
		Rational crossArea = new Rational(crossBox.area());

		if (crossArea.greaterThan(0)) {
			LinkedList lit = new LinkedList();
			Rectangle newRect = new Rectangle();
			/* If the boxes overlap, then need to slice */

			if (lowerBox.rect.x < this.rect.x) {
				/* Draw first slice of box going from left to right */
				newRect.x = lowerBox.rect.x;
				newRect.y = lowerBox.rect.y;
				newRect.width = this.rect.x - newRect.x;
				newRect.height = lowerBox.rect.height;
				lit.add(new BoundingBox(newRect));
			}

			if ((lowerBox.rect.y + lowerBox.rect.height)
				> (this.rect.y + this.rect.height)) {
				/* Draw top center slice */
				newRect.x = Math.max(this.rect.x, lowerBox.rect.x);
				newRect.y = this.rect.y + this.rect.height;
				newRect.width =
					Math.min(
						lowerBox.rect.x + lowerBox.rect.width,
						this.rect.x + this.rect.width)
						- newRect.x;
				newRect.height =
					lowerBox.rect.y + lowerBox.rect.height - newRect.y;
				lit.add(new BoundingBox(newRect));
			}

			if (lowerBox.rect.y < this.rect.y) {
				/* Draw bottom center slice */
				newRect.x = Math.max(this.rect.x, lowerBox.rect.x);
				newRect.y = lowerBox.rect.y;
				newRect.width =
					Math.min(
						lowerBox.rect.x + lowerBox.rect.width,
						this.rect.x + this.rect.width)
						- newRect.x;
				newRect.height = this.rect.y - lowerBox.rect.y;
				lit.add(new BoundingBox(newRect));
			}

			if ((lowerBox.rect.x + lowerBox.rect.width)
				> (this.rect.x + this.rect.width)) {
				/* Draw rightmost slice of box */
				newRect.x = this.rect.x + this.rect.width;
				newRect.y = lowerBox.rect.y;
				newRect.width =
					(lowerBox.rect.x + lowerBox.rect.width) - newRect.x;
				newRect.height = lowerBox.rect.height;
				lit.add(new BoundingBox(newRect));
			}
			return lit;
		} else {
			return null;
		}
	}

	/**
	 * Gets a set of boxes which covers all and only the pixels covered by
	 * <code>A</code> and <code>B</code>.
	 * 
	 * @param A
	 *            a set of boxes to union with
	 * @param B
	 *            a set of boxes to union with
	 * @return a set of boxes corresponding to the region shared by A and B
	 */
	public static BoundingBox union(BoundingBox A, BoundingBox B) {
		BoundingBox temp = new BoundingBox();
		LinkedList aList;
		temp.composed = true;
		int x = Math.min(A.rect.x, B.rect.x);
		int y = Math.min(A.rect.y, B.rect.y);
		int x2 =  Math.max(
					A.rect.x + A.rect.width,
					B.rect.x + B.rect.width);
		int y2 = Math.max(
				A.rect.y + A.rect.height,
				B.rect.y + B.rect.height);

		temp.rect =
			new Rectangle(x, y, x2, y2);

		if (A.composed)
			aList = (LinkedList) A.pieces.clone();
		else {
			aList = new LinkedList();
			aList.add(A);
		}

		if (B.composed)
			temp = B.copy();
		else {
			temp.pieces = new LinkedList();
			temp.pieces.add(B.copy());
			temp.composed = true;
		}

		ListIterator iter = aList.listIterator(0);
		while (iter.hasNext()) {
			BoundingBox child = (BoundingBox) iter.next();
			Iterator ti = temp.pieces.iterator();
			LinkedList childRemains = null;

			/* remove an offending piece of the child */
			while (ti.hasNext()
				&& (null
					== (childRemains =
						((BoundingBox) ti.next()).subtractFrom(child))));

			/*
			 * Add the broken pieces into the list and break back to top loop
			 * remove the offending rectangle and replace it with its shards,
			 * then clean up those.
			 */
			if (childRemains != null) {
				ti = childRemains.iterator();
				iter.remove();
				while (ti.hasNext()) {
					iter.add(ti.next());
					iter.previous();
				}
			}
		}
		temp.pieces.addAll(aList);
		temp.simplify();
		return (temp);
	}

	/**
	 * Unions this with the specified box(es) and sets this to that union.
	 * 
	 * @param other
	 *            The box(es) to add to this set
	 */
	public void extendToContain(BoundingBox other) {
		if (composed || other.composed)
			throw new ArithmeticException("Cannot extend composed bboxes");

		rect.x = Math.min(rect.x, other.rect.x);
		rect.width =
			Math.max(rect.x + rect.width, other.rect.x + other.rect.width)
				- rect.x;

		rect.y = Math.min(rect.y, other.rect.y);
		rect.height =
			Math.max(rect.y + rect.height, other.rect.y + other.rect.height)
				- rect.y;
		bbox = null;
	}

	/**
	 * The getPolys iterator returns a set of non-overlapping polygons that,
	 * together, cover the same region as this set of boxes.
	 * 
	 * @return a set of convex polygons that tile this region
	 */
	public Iterator getPolys() {
		if (composed) {
			return pieces.iterator();
		} else {
			ArrayList temp = new ArrayList(1);
			temp.add(this);
			return temp.iterator();
		}
	}

	/**
	 * Gets the x-coordinate of the box's origin.
	 * 
	 * @return the x-coordinate of the box's origin.
	 * @throws ArithmeticException
	 *             if the set of boxes is not a singleton
	 */
	public int getX() {
		if (composed)
			throw new ArithmeticException("Cannot get got composed bboxes");
		return rect.x;
	}

	/**
	 * Gets the y-coordinate of the box's origin.
	 * 
	 * @return the y-coordinate of the box's origin.
	 * @throws ArithmeticException
	 *             if the set of boxes is not a singleton
	 */
	public int getY() {
		if (composed)
			throw new ArithmeticException("Cannot get got composed bboxes");
		return rect.y;
	}

	/**
	 * Gets the width of the box.
	 * 
	 * @return the width of the box.
	 * @throws ArithmeticException
	 *             if the set of boxes is not a singleton
	 */
	public int getWidth() {
		if (composed)
			throw new ArithmeticException("Cannot get got composed bboxes");
		return rect.width;
	}

	/**
	 * Gets the height of the box.
	 * 
	 * @return the height of the box.
	 * @throws ArithmeticException
	 *             if the set of boxes is not a singleton
	 */
	public int getHeight() {
		if (composed)
			throw new ArithmeticException("Cannot get got composed bboxes");
		return rect.height;
	}

	/**
	 * Gets the original rectangle
	 * 
	 * @return the box.
	 */
	public Rectangle getRectangle() {
		return rect;
	}

	/**
	 * Gets the rotation
	 * 
	 * @return the integer zero
	 */
	public int getRotation() {
		return 0;
	}

	/**
	 * Gets the center of the box.
	 * 
	 * @return the center point of the box as a pair of rational numbers
	 * @throws ArithmeticException
	 *             if the set of boxes is not a singleton
	 */
	public Pnt getCentroid() {
		Rational x = new Rational(getX());
		Rational.plus(x, new Rational(getWidth(), 2), x);

		Rational y = new Rational(getY());
		Rational.plus(y, new Rational(getHeight(), 2), y);

		return new Pnt(x, y);
	}

	/**
	 * Of the two points on the line between this box's centroid and q1 that
	 * instersect this box, this returns the one closer to q1.
	 * 
	 * @param q1
	 *            a point different from the centroid.
	 * @return point on the ray from centriod through q1 that is on the box
	 *         border
	 */
	public Pnt getNearIntersection(Pnt q1) {
		Pnt p1 = this.getCentroid();
		Pnt r1 = new Pnt();
		Rational rise = new Rational();
		Rational.minus(q1.getY(), p1.getY(), rise);
		Rational negRise = new Rational(rise).negate();
		Rational run = new Rational();
		Rational.minus(q1.getX(), p1.getX(), run);
		if (rise.greaterThan(0)
			&& run.lessThan(rise)
			&& run.greaterThan(negRise)) {
			// intersects top
			int y = getY() + getHeight();
			Util.lineIntersection(
				p1,
				q1,
				new Pnt(getX(), y),
				new Pnt(getX() + getWidth(), y),
				r1);
		} else if (
			rise.lessThan(0)
				&& run.lessThan(negRise)
				&& run.greaterThan(rise)) {
			// intersects bottom
			int y = getY();
			Util.lineIntersection(
				p1,
				q1,
				new Pnt(getX(), y),
				new Pnt(getX() + getWidth(), y),
				r1);
		} else if (run.lessThan(0)) { // goes left
			int x = getX();
			Util.lineIntersection(
				p1,
				q1,
				new Pnt(x, getY()),
				new Pnt(x, getY() + getHeight()),
				r1);
		} else { // must go right
			int x = getX() + getWidth();
			Util.lineIntersection(
				p1,
				q1,
				new Pnt(x, getY()),
				new Pnt(x, getY() + getHeight()),
				r1);
		}
		return r1;
	}

	/**
	 * Gets a copy of the box, shifted by the given amount in the specified
	 * direction.
	 * 
	 * @param direction
	 *            the direction to remove the box, e.g. {@link Moveable#NORTH}
	 * @param distance
	 *            the distance to move the box
	 * @return a new box, that is a copy of this box, shifted as specified
	 */
	public Moveable move(int direction, int distance) {
		switch (direction) {
			case Moveable.NORTH :
				return shift(0, distance);
			case Moveable.NORTHEAST :
				return shift(distance, distance);
			case Moveable.EAST :
				return shift(distance, 0);
			case Moveable.SOUTHEAST :
				return shift(distance, -distance);
			case Moveable.SOUTH :
				return shift(0, -distance);
			case Moveable.SOUTHWEST :
				return shift(-distance, -distance);
			case Moveable.WEST :
				return shift(-distance, 0);
			case Moveable.NORTHWEST :
				return shift(-distance, distance);
		}
		throw new IllegalArgumentException(
			"Not a cardinal direction: " + direction);
	}

	/**
	 * @see edu.umd.cfar.lamp.viper.geometry.Moveable#shift(int, int)
	 */
	public Moveable shift(int x, int y) {
		return new BoundingBox(
				getX() + x,
				getY() + y,
				getWidth(),
				getHeight());
	}
}
