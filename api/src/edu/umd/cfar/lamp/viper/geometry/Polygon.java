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

import java.awt.geom.*;
import java.util.*;

import viper.api.*;
import edu.umd.cfar.lamp.viper.util.*;

/**
 * This class represents a series of line segments.
 */
public class Polygon extends PolyList implements Cloneable, HasCentroid, Moveable {
	private final class PolygonMover extends AbstractMoveable {
		public Moveable shift(int x, int y) {
			Polygon P = new Polygon();
			for (Iterator iter = lines.iterator(); iter.hasNext();) {
				Squiggle s = (Squiggle) iter.next();
				P.lines.add(s.shift(x, y));
			}
			P.initBbox();
			return P;
		}
	}
	private AbstractMoveable moveProxy = new PolygonMover();
	
	private List lines;


	/**
	 * Constructs an empty, closed polygon.
	 */
	public Polygon() {
		lines = new LinkedList();
	}

	/**
	 * Constructs a Polygon copy.
	 * @param old The Polygon to copy.
	 */
	public Polygon(Polygon old) {
		lines = new LinkedList();
		for (Iterator iter = old.lines.iterator(); iter.hasNext();) {
			lines.add(((Squiggle) iter.next()).clone());
		}
	}

	/**
	 * Converts a list of points into a polygon. NB: This assumes 
	 * that the points are all integers. Also assumes list[ 0 ] 
	 * and list[ n - 1 ] are not the same point.
	 * @param list ordered set of points for the polygon
	 */
	public Polygon(Point2D[] list) {
		for (int i = 0; i < list.length; i++) {
			addVertex(new Pnt((int) list[i].getX(), (int) list[i].getY()));
		}
	}

	/**
	 * Constructs a polygon from a list of points. Can construct a
	 * set of polygons, where each individual polygon is in brackets
	 * @param S a polygon is a list of parenthestized int pairs, and 
	 * a set of polygons is delimited by brackets
	 * @return the described polygon or polylist
	 */
	public static Polygon valueOf(String S) {
		try {
			return new Polygon(S);
		} catch (BadDataException e) {
			throw new BadAttributeDataException(e.getLocalizedMessage());
		}
	}

	/**
	 * Constructs a polygon from a list of points. Can construct a
	 * set of polygons, where each individual polygon is in brackets
	 * @param S a polygon is a list of parenthestized int pairs, and 
	 * a set of polygons is delimited by brackets
	 * @throws BadDataException if the string isn't formatted properly
	 */
	public Polygon(String S) throws BadDataException {
		lines = new LinkedList();
		StringTokenizer input = new StringTokenizer(S, "[]");
		while (input.hasMoreTokens()) {
			lines.add(new Squiggle(input.nextToken()));
		}
		initBbox();
	}
	
	/**
	 * Tests to see if the other object contains the same 
	 * region as this polygon.
	 * @param o the PolyList or Polygon to compare with
	 * @return false if the two cover different regions
	 */
	public boolean equals(Object o) {
		if (o == this) {
			return true;
		} else if (o instanceof Polygon) {
			Polygon that = (Polygon) o;
			Iterator myPts = lines.iterator();
			Iterator yerPts = that.lines.iterator();
			boolean matching = true;
			boolean searching = true;
			while (matching && searching) {
				searching = myPts.hasNext();
				matching = searching == yerPts.hasNext();
				if (searching && matching) {
					matching = myPts.next().equals(yerPts.next());
				}
			}
			return matching && !searching;
		} else {
			return false;
		}
	}

	/**
	 * Constructs a copy of this object.
	 * @return A new Object that represents the same region.
	 */
	public Object clone() {
		return new Polygon(this);
	}

	/**
	 * Gets a list of the points as a String, usually for debugging.
	 * @return A bracketed String, with a list of parenthesized lists of points
	 *         for each convex polygon.
	 */
	public String toString() {
		if (lines.size() > 1) {
			StringBuffer buf = new StringBuffer();
			for (Iterator iter = lines.iterator(); iter.hasNext();) {
				buf.append("[").append(((Squiggle) iter.next()).toString()).append(
					"]");
			}
			return buf.toString();
		} else if (lines.size() == 1) {
			return ((Squiggle) lines.get(0)).toString();
		} else {
			return "";
		}
	}
	
	
	/**
	 * Creates a BoundingBox around this, for use by getBoundingBox, etc.
	 */
	protected void initBbox() {
		if (bbox != null) {
			return;
		}
		Iterator iter = lines.iterator();
		if ((iter != null) && iter.hasNext()) {
			bbox = ((Squiggle) iter.next()).getBoundingBox().copy();
			while (iter.hasNext()) {
				bbox.extendToContain(((Squiggle) iter.next()).getBoundingBox());
			}
		}
		if (PolyList.CARVE_POLYS) {
			initPoly();
		}
	}

	private void initPoly() {
		Iterator iter = lines.iterator();
		List simpleSquiggles = new LinkedList();
		while (iter.hasNext()) {
			Squiggle currPoly = (Squiggle) iter.next();
			// TODO: carve up the polygons into simple polys.
			// First, make sure we have simple polygons
			// This is accomplished by selecting the bottommost point
			// and sweeping around ccw, generating events and collecting interior shapes.

			Rational r = currPoly.signedArea();
			if (r.equals(0)) {
				continue;
			} else if (r.lessThan(0)) {
				currPoly = currPoly.reverse();
			}
			simpleSquiggles.add(currPoly);
		}
		iter = simpleSquiggles.iterator();
		while (iter.hasNext()) {
			Squiggle currPoly = (Squiggle) iter.next();

			// Then, Triangulate the polygon.
			// XXX: replace with faster triangulation code.
			List triangles = currPoly.triangulate();
			
			// Finally, remove the inessential lines.
			// TODO: implement inessential diagonal removal
			
			// Now add the polygons to this polylist
			polys.clear();
			polys.addAll(triangles);
		}
	}
	
	/**
	 * Gets the closest bounding box surrounding this polygon.
	 * @return the closest bounding box
	 */
	public BoundingBox getBoundingBox() {
		if (bbox == null) {
			initBbox();
			if (bbox == null) {
				bbox = new BoundingBox();
			}
		}
		return bbox;
	}

	/**
	 * Gets all the points around the outside of the
	 * polygon.
	 * XXX Does not handle composed case, no points case.
	 * @return an iterator through all the points on the polygon
	 */
	public Iterator getPoints() {
		if (lines != null && lines.size() > 0) {
			return ((Squiggle) lines.get(0)).getPoints();
		} else {
			return null;
		}
	}

	/**
	 * Adds the point to the end of the polygon as a point on its
	 * exterior.
	 * XXX Does not handle composed case, no points case.
	 * @param v the point to add to the edge list
	 * @return true, if the point was added
	 */
	public boolean addVertex(Pnt v) {
		if (lines != null && lines.size() > 0) {
			((Squiggle) lines.get(0)).addPoint(v);
		} else {
			Squiggle nl = new Squiggle();
			nl.addPoint(v);
			lines = new LinkedList();
			lines.add(nl);
		}
		
		return true;
	}

	/**
	 * @see edu.umd.cfar.lamp.viper.geometry.HasCentroid#getCentroid()
	 */
	public Pnt getCentroid() {
		BoundingBox bbox = getBoundingBox() ;
		return bbox.getCentroid() ;
	}
	/**
	 * {@inheritDoc}
	 * @param direction {@inheritDoc}
	 * @param distance {@inheritDoc}
	 * @return {@inheritDoc}
	 */
	public Moveable move(int direction, int distance) {
		return moveProxy.move(direction, distance);
	}
	/**
	 * {@inheritDoc}
	 * @param x {@inheritDoc}
	 * @param y {@inheritDoc}
	 * @return {@inheritDoc}
	 */
	public Moveable shift(int x, int y) {
		return moveProxy.shift(x, y);
	}
}
