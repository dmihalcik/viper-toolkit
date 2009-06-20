package edu.umd.cfar.lamp.viper.geometry;

import java.util.*;

import edu.umd.cfar.lamp.viper.util.*;

/**
 * Represents a squiggly line as a list of connected line segments.
 */
public class Squiggle implements Cloneable, Moveable {
	private final class SquiggleMover extends AbstractMoveable {
		public Moveable shift(int x, int y) {
			Squiggle s = new Squiggle();
			Iterator iter = segments.iterator();
			while (iter.hasNext()) {
				Pnt p = (Pnt) iter.next();
				s.addPoint((Pnt) p.shift(x, y));
			}
			return s;
		}
	}

	private List segments = new LinkedList();

	private BoundingBox bbox = null;

	private AbstractMoveable mover = new SquiggleMover();

	/**
	 * Constructs the empty squiggle.
	 */
	public Squiggle() {
		super();
	}

	/**
	 * Constructs a new squiggle from the given list of parenthesized points.
	 * 
	 * @param S
	 *            a string in the form
	 *            <q>(x y) ...</q>
	 * @throws BadDataException
	 */
	public Squiggle(String S) throws BadDataException {
		StringTokenizer st = new StringTokenizer(S, "() ", true);
		int lx = 0;
		int ly = 0;
		int gx = 0;
		int gy = 0;
		boolean first = true;
		while (st.hasMoreTokens()) {
			try {
				if (!st.nextToken().equals("(")) {
					throw new BadDataException("Expected '('.");
				}
				int x = Integer.parseInt(st.nextToken());
				if (!st.nextToken().equals(" ")) {
					throw new BadDataException("Expected ' '.");
				}
				int y = Integer.parseInt(st.nextToken());
				if (!st.nextToken().equals(")")) {
					throw new BadDataException("Expected ' '.");
				}
				if (first) {
					first = false;
					lx = gx = x;
					ly = gy = y;
				} else {
					lx = Math.min(lx, x);
					gx = Math.max(gx, x);
					ly = Math.min(ly, y);
					gy = Math.max(gy, y);
				}
				segments.add(new Pnt(x, y));
			} catch (NoSuchElementException nsex) {
				throw new BadDataException("Malformed line segment");
			}
		}
		this.bbox = new BoundingBox(lx, ly, gx - lx, gy - ly);
	}

	/**
	 * Copies the squiggle.
	 * 
	 * @return a new copy of the squiggle.
	 */
	public Object clone() {
		Squiggle nl = new Squiggle();
		for (Iterator iter = segments.iterator(); iter.hasNext();) {
			nl.segments.add(new Pnt((Pnt) iter.next()));
		}
		nl.bbox = (BoundingBox) this.bbox.clone();
		return nl;
	}

	/**
	 * Gets a string representation.
	 * 
	 * @return a string representation as a list of vertices
	 */
	public String toString() {
		StringBuffer buf = new StringBuffer();
		for (Iterator iter = segments.iterator(); iter.hasNext();) {
			buf.append(iter.next().toString());
		}
		return buf.toString();
	}

	/**
	 * Gets the closest bounding box around the squiggle.
	 * 
	 * @return the closest bounding box around the squiggle
	 */
	public BoundingBox getBoundingBox() {
		if (bbox == null) {
			int lx = 0;
			int ly = 0;
			int gx = 0;
			int gy = 0;
			boolean first = true;
			Iterator iter = segments.iterator();
			while (iter.hasNext()) {
				Pnt p = (Pnt) iter.next();
				if (first) {
					first = false;
					lx = p.getX().floor().intValue();
					gx = p.getX().ceiling().intValue();
					ly = p.getY().floor().intValue();
					gy = p.getY().ceiling().intValue();
				} else {
					lx = Math.min(lx, p.getX().floor().intValue());
					gx = Math.max(gx, p.getX().ceiling().intValue());
					ly = Math.min(ly, p.getY().floor().intValue());
					gy = Math.max(gy, p.getY().ceiling().intValue());
				}
			}
			this.bbox = new BoundingBox(lx, ly, gx - lx, gy - ly);
		}
		return bbox;
	}

	/**
	 * Gets an iterator through all the (@link Pnt} objects making up the
	 * squiggle.
	 * 
	 * @return an Iterator of Pnts.
	 */
	public Iterator getPoints() {
		return segments.iterator();
	}

	/**
	 * Adds a new point at the end of the squiggle.
	 * 
	 * @param p
	 *            the point to add.
	 */
	public void addPoint(Pnt p) {
		segments.add(p);
		this.bbox = null;
	}

	/** @inheritDoc */
	public Moveable move(int direction, int distance) {
		return mover.move(direction, distance);
	}

	/** @inheritDoc */
	public Moveable shift(int x, int y) {
		return mover.shift(x, y);
	}

	/**
	 * Returns this squiggle, with the points in the opposite order.
	 * 
	 * @return the reversed-point-list squiggle
	 */
	public Squiggle reverse() {
		Squiggle s2 = new Squiggle();
		ListIterator li = segments.listIterator(segments.size());
		while (li.hasPrevious()) {
			s2.addPoint((Pnt) li.previous());
		}
		return s2;
	}

	Rational signedArea() {
		Rational total = new Rational(0);
		if (segments.size() < 3) {
			return total;
		}
		Iterator iter = segments.iterator();
		Pnt alpha = (Pnt) iter.next();
		Pnt beta = (Pnt) iter.next();

		while (iter.hasNext()) {
			Pnt gamma = (Pnt) iter.next();
			Rational next = Util.areaSign(alpha, beta, gamma);
			Rational.plus(total, next, total);
			beta = gamma;
		}
		return total;
	}

	/**
	 * Determine the direction of the points in the squiggle, assuming a closed
	 * squiggle.
	 * 
	 * @return if the polygon's points are listed in a clockwise direction
	 */
	public boolean isClockwise() {
		if (segments.size() < 3) {
			return true;
		}
		return signedArea().lessThan(0);
	}

	/**
	 * Returns a list of {@link ConvexPolygon triangle polygons}that, together,
	 * tile the interior of this squiggle.
	 * 
	 * @return
	 */
	public List triangulate() {
		List all = new LinkedList();
		assert !isClockwise();
		if (segments.size() == 3) {
			all.add(new ConvexPolygon(segments));
		} else if (segments.size() > 3) {
			int i = segments.size()-2;
			int j = i+1;
			int k = 0;
			while (k < segments.size()) {
				if (diagonal(i, k)) {
					Squiggle s = new Squiggle();
					s.segments.addAll (this.segments);
					Pnt[] l = new Pnt[3];
					l[0] = (Pnt) s.segments.get(i);
					l[1] = (Pnt) s.segments.get(j);
					l[2] = (Pnt) s.segments.get(k);
					if (Util.areaSign(l[0], l[1], l[2]).lessThan(0)) {
						Pnt t = l[0];
						l[0] = l[2];
						l[2] = t;
					}
					ConvexPolygon cp = new ConvexPolygon(Arrays.asList(l));
					s.segments.remove(j);
					all.add(cp);
					if (s.segments.size() > 2) {
						all.addAll(s.triangulate());
					}
					break;
				}
				i = j;
				j = k;
				k++;
			}
		}
		return all;
	}

	/**
	 * Determines if the segment ab is a diagonal that doesn't intersect the
	 * edge. It may be internal or external
	 * 
	 * @param a
	 * @param b
	 * @return
	 */
	public boolean diagonalie(Pnt a, Pnt b) {
		if (segments.size() < 2) {
			return true;
		}
		Iterator iter = getPoints();
		Pnt c = (Pnt) iter.next();
		while (iter.hasNext()) {
			Pnt d = (Pnt) iter.next();
			// skip vertex points
			if (c.equals(a) || d.equals(a) || c.equals(b) || d.equals(b)) {
				;
			} else if (Util.intersects(a, b, c, d)) {
				return false;
			}
			c = d;
		}
		return true;
	}
	
	boolean diagonal(int i, int j) {
		return isInCone(i, j) && diagonalie((Pnt) segments.get(i), (Pnt) segments.get(j));
	}

	/**
	 * Determines if the line from vertex i to vertex j goes to the left of its
	 * local corner.
	 * 
	 * @param i
	 *            the start of the 'diagonal'
	 * @param j
	 *            the end
	 * @return if ij goes into the squiggle
	 */
	boolean isInCone(int i, int j) {
		int last = segments.size() - 1;
		Pnt aPrev = (Pnt) (i == 0 ? segments.get(last) : segments.get(i - 1));
		Pnt a = (Pnt) segments.get(i);
		Pnt aNext = (Pnt) (i == last ? segments.get(0) : segments.get(i + 1));
		Pnt b = (Pnt) segments.get(j);
		// if a1 is a convex vertex
		if (aPrev.isLeftOfOrOn(a, aNext)) {
			return aPrev.isLeftOf(a, b) && aNext.isLeftOf(b, a);
		} else {
			return !(aNext.isLeftOfOrOn(a, b) && aPrev.isLeftOfOrOn(b, a));
		}
	}
}