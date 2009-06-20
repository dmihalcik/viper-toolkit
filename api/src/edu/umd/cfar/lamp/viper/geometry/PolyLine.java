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
 * This class represents a series of line segments.
 */
public class PolyLine extends PolyList implements Cloneable {
	/** A BoundingBox that surrounds the set of lines. */
	protected BoundingBox bbox;

	private List lines;

	/**
	 * Creates an empty polyline
	 */
	public PolyLine() {
		lines = new LinkedList();
	}

	/**
	 * Constructs a PolyLine copy.
	 * @param old The PolyLine to copy.
	 */
	public PolyLine(PolyLine old) {
		lines = new LinkedList();
		for (Iterator iter = old.lines.iterator(); iter.hasNext();) {
			lines.add(((Squiggle) iter.next()).clone());
		}
		initBbox();
	}

	/**
	 * Constructs the polyline from the string list
	 * of points.
	 * @param S the list of points
	 * @throws BadDataException if the string is malformed
	 */
	public PolyLine(String S) throws BadDataException {
		lines = new LinkedList();
		StringTokenizer input = new StringTokenizer(S, "[]");
		while (input.hasMoreTokens()) {
			lines.add(new Squiggle(input.nextToken()));
		}
		initBbox();
	}

	/**
	 * Constructs a copy of this object.
	 * @return A new Object that represents the same space.
	 */
	public Object clone() {
		return new PolyLine(this);
	}

	/**
	 * Gets a list of the points as a String, usually for debugging.
	 * @return A bracketed String, with a list of parenthesized lists
	 *   of points for each convex PolyLine.
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

	protected void initBbox() {
		if (bbox == null) {
			Iterator iter = lines.iterator();
			if ((iter != null) && iter.hasNext()) {
				bbox = ((Squiggle) iter.next()).getBoundingBox().copy();
				while (iter.hasNext()) {
					bbox.extendToContain(((Squiggle) iter.next()).getBoundingBox());
				}
			}
		}
	}

	/**
	 * Gets the nearest bounding box that contains the line
	 * @return the nearest bounding box that contains the line
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
	 * Gets an iterator through all the points on the
	 * edge of the polyline.
	 * XXX Needs to handle composed case, no points case.
	 * @return an Iterator of Pnt objects
	 */
	public Iterator getPoints() {
		if (lines != null && lines.size() > 0) {
			return ((Squiggle) lines.get(0)).getPoints();
		} else {
			return null;
		}
	}
	
	/**
	 * Tests to see if the two objects refer to
	 * the same set of line segments.
	 * @param o the set of line segments to compare with
	 * @return true iff they are the same set of line segments 
	 */
	public boolean equals(Object o) {
		if (o == this) {
			return true;
		} else if (o instanceof PolyLine) {
			PolyLine that = (PolyLine) o;
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
	 * Adds the given vertex to the end of the polyline.
	 * @param v the new point
	 * @return true if the point is added
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
}
