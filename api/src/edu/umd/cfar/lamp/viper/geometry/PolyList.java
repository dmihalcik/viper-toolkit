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

import edu.umd.cfar.lamp.viper.util.*;

/**
 * This class represents the intersection / union
 * of several Convex Polygons. ConvexPolygon extends this,
 * and if I ever have non-convex polygons, they should, too.
 */
public class PolyList implements Cloneable {
	/** If true, then it is composed, and need to watch for redundancies. */
	protected boolean composed = false;

	/** The list of convex polygons that this data structure describes.
	 * you know your object model is screwed when you have to use protected
	 * fields. */
	protected List polys = new LinkedList();

	/** While the polys list has no overlaps, the originals list does not.
	 * Instead, it offers quicker bounds checking, and fragmentation
	 * counts by keeping track of the original polygons that compose
	 * this. */
	protected List originals = new LinkedList();

	/** A BoundingBox that surrounds the PolyList. */
	protected BoundingBox bbox;

	/** The area. Negative if not set*/
	protected Rational area;

	/**
	 * This boolean should be set to 'false' to avoid carving polygons
	 * into simple polygons. This can cause performance delays, and, as
	 * of now, can cause crashes, so it is a good idea for at least viper-gt
	 * to set this to false. 
	 */
	public static boolean CARVE_POLYS = true;

	/**
	 * Constructs an empty PolyList.
	 */
	public PolyList() {
		this(null);
	}

	/**
	 * Constructs a PolyList copy of an old PolyList.
	 * @param old The PolyList to copy.
	 */
	public PolyList(PolyList old) {
		if (old == null) {
			composed = false;
		} else {
			Iterator iter = old.getPolys();
			while (iter.hasNext())
				polys.add(((PolyList) iter.next()).clone());
			if (old.originals.size() == 0) {
				originals.addAll(polys);
			}
			initBbox();
			area = old.area;
			composed = true;
		}
	}

	/**
	 * Constructs a copy of this object.
	 * @return A new Object that represents the same space.
	 */
	public Object clone() {
		return new PolyList(this);
	}

	/**
	 * Checks to see if this equals the other space.
	 * @param other The object to test with.
	 * @return <code>true</code> if they are equal.
	 */
	public boolean equals(Object other) {
		if (this == other) {
			return true;
		} else if (other instanceof PolyList) {
			return (((PolyList) other).area().equals(area()))
				&& (PolyList.intersection(this, (PolyList) other).area().equals(area()));
		} else {
			return false;
		}
	}

	/**
	 * Get a hashcode for this region. It is generated
	 * from the area.
	 * @return An integer that is unique to this region.
	 */
	public int hashCode() {
		return area().hashCode();
	}

	/**
	 * Gets a list of the points as a String, usually for debugging.
	 * @return A bracketed String, with a list of parenthesized lists
	 *   of points for each convex polygon.
	 */
	public String toStringListOfPoints() {
		Iterator iter = getPolys();
		StringBuffer buf = new StringBuffer("[");
		while (iter.hasNext()) {
			PolyList temp = (PolyList) iter.next();
			buf.append('(').append(
				((ConvexPolygon) temp).toStringListOfPoints()).append(
				')');
		}
		buf.append("]");
		return buf.toString();
	}

	/**
	 * Returns a string representation of this list of convex polygons.
	 * @return The same as {@link #toStringListOfPoints} right now.
	 */
	public String toString() {
		Iterator iter = getPolys();
		StringBuffer buf = new StringBuffer("[");
		while (iter.hasNext()) {
			PolyList temp = (PolyList) iter.next();
			buf.append('(').append(temp.toString()).append(')');
		}
		buf.append("]");
		return buf.toString();
	}

	/**
	 * Get the area of coverage by these polygons.
	 * @return The nearest double value to the area.
	 */
	public Rational area() {
		if (area != null)
			return area;

		Iterator iter = getPolys();
		area = new Rational(0);
		while (iter.hasNext()) {
			PolyList curr = (PolyList) iter.next();
			Rational.plus(area, curr.area(), area);
		}
		return area;
	}

	/**
	 * Generate the union of two PolyLists.
	 * @param alpha A region of 2-dimensional space to unify.
	 * @param zappa Another region of 2-dimensional space to unify.
	 * @return A new PolyList representing the the space covered by both regions.
	 */
	public static PolyList union(PolyList alpha, PolyList zappa) {
		PolyList result = new PolyList(alpha);
		Iterator iterCheck = result.getPolys();

		if (!iterCheck.hasNext()) {
			return new PolyList(zappa);
		}
		iterCheck = zappa.getPolys();
		if (!iterCheck.hasNext()) {
			return result;
		}

		if (alpha.originals.size() == 0) {
			result.originals.addAll(alpha.polys);
		} else {
			result.originals.addAll(alpha.originals);
		}

		while (iterCheck.hasNext()) {
			PolyList curr = (PolyList) iterCheck.next();
			result.addPoly((PolyList) (curr).clone());
			if (zappa.originals.size() == 0) {
				result.originals.add(curr);
			}
		}

		if (zappa.originals.size() > 0) {
			result.originals.addAll(zappa.originals);
		}
		result.bbox = null;
		result.area = null;
		return result;
	}

	/**
	 * Find the intersection of two PolyLists.
	 * @param alpha A region of 2-dimensional space.
	 * @param zappa Another region of 2-dimensional space to intersect with.
	 * @return A new PolyList representing the shared region.
	 */
	public static PolyList intersection(PolyList alpha, PolyList zappa) {
		// Union together all of the disparite parts
		Iterator iterAlpha = alpha.getPolys();
		PolyList result = new PolyList();

		while (iterAlpha.hasNext()) {
			PolyList tempAlpha = (PolyList) iterAlpha.next();
			Iterator iterZappa = zappa.getPolys();
			while (iterZappa.hasNext()) {
				PolyList tempZappa = (PolyList) iterZappa.next();
				if (tempAlpha
					.getBoundingBox()
					.intersects(tempZappa.getBoundingBox())) {
					PolyList newPoly = tempAlpha.getIntersection(tempZappa);
					if ((newPoly != null) && (newPoly.area().greaterThan(0))) {
						result.addPoly(newPoly);
						if (newPoly.originals.size() > 0) {
							result.originals.addAll(newPoly.originals);
						} else {
							result.originals.add(newPoly);
						}
						result.composed = true;
					}
				}
			}
		}
		result.initBbox();
		result.area = null;
		return result;
	}

	/**
	 * Get the intersection of this PolyList with another.
	 *
	 * @param other The region to intersect with.
	 * @return A new PolyList that represents the shared region.
	 */
	public PolyList getIntersection(PolyList other) {
		return PolyList.intersection(this, other);
	}

	/**
	 * Creates a BoundingBox around this, for use by getBoundingBox,
	 * etc. This is called whenever the bbox is requested and
	 * it is currently set to null; the bbox field therefore acts as a 
	 * dirty bit. 
	 */
	protected void initBbox() {
		if (bbox != null) {
			return;
		}
		Iterator iter;
		if (originals.size() > 0) {
			iter = originals.iterator();
		} else {
			iter = polys.iterator();
		}
		if (iter.hasNext()) {
			bbox = ((PolyList) iter.next()).getBoundingBox().copy();
			while (iter.hasNext())
				bbox.extendToContain(((PolyList) iter.next()).getBoundingBox());
		} else {
			bbox = new BoundingBox();
		}
	}

	/**
	 * Gets the closest box around the set of polygons.
	 * @return the closest box around the set of polygons
	 */
	public BoundingBox getBoundingBox() {
		initBbox();
		return bbox;
	}

	/**
	 * Gets an iterator that goes through a list of polygons
	 * that are contained within this region, such that the union
	 * of all of them is equal to this region.
	 * @return An Iterator that goes through the region.
	 */
	protected Iterator getPolys() {
		initBbox();
		return polys.iterator();
	}

	/**
	 * Gets the original list of polygons added to this
	 * list, instead of the list of non-overlapping ones.
	 * @return an iterator of PolyLists.
	 */
	public Iterator getOriginals() {
		if (originals.size() == 0) {
			return getPolys();
		} else {
			return originals.iterator();
		}
	}

	/**
	 * Tests to see if one of the subpolys contains the given point.
	 * @param point the point to test for
	 * @return true if the point is somoewhere within the polylist
	 */
	public boolean contains(Pnt point) {
		for (Iterator iter = this.getOriginals(); iter.hasNext();) {
			if (((ConvexPolygon) iter.next()).contains(point)) {
				return true;
			}
		}
		return false;
	}

	private static PolyList subtract(ConvexPolygon cp, PolyList clipper) {
		for (Iterator myPolys = clipper.getPolys(); myPolys.hasNext();) {
			ConvexPolygon curr = (ConvexPolygon) myPolys.next();
			Rational overlapArea = ConvexPolygon.intersection(curr, cp).area();
			if (overlapArea.greaterThan(0)) {
				return ConvexPolygon.subtract(cp, curr);
			}
		}
		return cp;
	}

	protected void clearPolyList() {
		polys.clear();
	}
	
	/**
	 * Sets this PolyList into this unioned with the given shape.
	 * @param shape the region to add to this.
	 */
	protected void addPoly(PolyList shape) {
		if (shape instanceof ConvexPolygon) {
			ConvexPolygon con = (ConvexPolygon) shape;
			if (polys.isEmpty()) {
				polys.add(con);
				area = null;
				bbox = null;
				return;
			}
			PolyList attempt = PolyList.subtract(con, this);
			if (attempt.area().lessThan(con.area())) {
				addPoly(attempt);
				bbox = null;
			} else if (attempt.area().equals(con.area())) {
				polys.add(con);
				composed = true;
				if (area != null) {
					Rational.plus(area, con.area(), area);
				}
				bbox = null;
			} else {
				throw new RuntimeException(
					"error while subtracting " + this +" from " + con);
			}
		} else {
			for (Iterator polys = shape.getPolys(); polys.hasNext();) {
				addPoly((PolyList) polys.next());
			}
		}
	}

	/**
	 * Gets an array of non-overlapping ConvexPolygons whose union
	 * completely tiles the region described by this PolyList. 
	 * @return a non-overlapping set of ConvexPolygons
	 */
	public ConvexPolygon[] getConvexPolygonArray() {
		return (ConvexPolygon[]) polys.toArray(new ConvexPolygon[polys.size()]);
	}

	/**
	 * Gets a count of how many original polygons
	 * the other hits.
	 * @param other the polygon to test against
	 * @return the number of original polygons within the other
	 * that this polylist intersects
	 */
	public int getFragmentationCount(PolyList other) {
		Iterator iter;
		if (composed) {
			if (originals.size() > 0) {
				iter = originals.iterator();
			} else {
				iter = getPolys();
			}
		} else {
			return intersects(other) ? 1 : 0;
		}
		int count = 0;
		while (iter.hasNext()) {
			PolyList curr = (PolyList) iter.next();
			if (curr.intersects(other)) {
				count++;
			}
		}
		return count;
	}
	
	/**
	 * Tests to see if this region intersects the 
	 * given region.
	 * @param other
	 * @return
	 */
	public boolean intersects(PolyList other) {
		if (getBoundingBox().intersects(other.getBoundingBox())) {
			PolyList newPoly = getIntersection(other);
			if (newPoly != null && newPoly.area().greaterThan(0)) {
				return true;
			}
		}
		return false;
	}

	/**
	 * Gets the points in the polygon; to be implemented
	 * by the concrete subclasses. 
	 * @return the empty iterator
	 */
	public Iterator getPoints() {
		return Collections.EMPTY_SET.iterator();
	}
	
	/**
	 * Adds the vertex to the polygon this represents; this 
	 * is unimplemented in this root class.
	 * @param p the point to add
	 * @return true if the addition of the point changes the shape
	 * @throws BadDataException if the point cannot be added
	 * @throws UnsupportedOperationException in this root class
	 */
	public boolean addVertex(Pnt p) throws BadDataException  {
		throw new UnsupportedOperationException();
	}
}
