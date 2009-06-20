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
 * An Oriented Box is a rectangle that is rotated about its bottom right
 * corner.
 */
public class OrientedBox extends ConvexPolygon 
						 implements BoxInformation, HasCentroid, Moveable {
	private final class OboxMover extends AbstractMoveable {
		public Moveable shift(int x, int y) {
			return new OrientedBox(data[0] + x, data[1] + y, data[2], data[3], data[4]);
		}
	}
	static final int X_POS = 0;
	static final int Y_POS = 1;
	static final int WIDTH = 2;
	static final int HEIGHT = 3;
	static final int ROTATION = 4;

	int[] data;
	private Pnt centroid;

	private Moveable moveDelegate = new OboxMover();

	/**
	 * Constructs the empty oriented box
	 */
	public OrientedBox() {
		data = new int[5];
		initPoly();
	}

	/**
	 * Constructs a new oriented box from the given set of five
	 * parameters
	 * @param params an integer array containing x and y coordinates of the 
	 * box origin, width and height of the box, and angle of rotation in degrees,
	 * in that order.
	 */
	public OrientedBox(int[] params) {
		if (params.length < 5) {
			throw (
				new IllegalArgumentException(
					"Not enough numbers to form an obox" + " - " + params));
		}
		data = new int[5];
		System.arraycopy(params, 0, data, 0, 5);
		cleanParameters();
		initPoly();
	}

	/**
	 * Creates a new oriented box with the specified parameters
	 * @param x the x-coordinate of the origin point.
	 * @param y the y-coordinate of the origin point
	 * @param width the width of the box
	 * @param height the height of the box
	 * @param rotation the angle of rotation, in degrees.
	 */
	public OrientedBox(int x, int y, int width, int height, int rotation) {
		data = new int[] { x, y, width, height, rotation % 360 };
		cleanParameters();
		initPoly();
	}

	/**
	 * Creates a new Oriented Box from a string representation.
	 * @param S a string representation
	 * @return a new oriented box for the given string
	 * @throws BadAttributeDataException when the string isn't in the valid form
	 */
	public static OrientedBox valueOf(String S) {
		try {
			return new OrientedBox(S);
		} catch (BadDataException bdx) {
			throw new BadAttributeDataException(bdx.getMessage());
		}
	}

	/**
	 * Creates a new Oriented Box from a string representation.
	 * @param S a string representation
	 * @throws BadDataException when the string isn't in the valid form
	 */
	public OrientedBox(String S) throws BadDataException {
		data = new int[5];
		try {
			StringTokenizer st = new StringTokenizer(S);
			for (int i = 0; i < 5; i++)
				data[i] = Integer.parseInt(st.nextToken());
			cleanParameters();
		} catch (NumberFormatException nfx) {
			initPoly();
			throw (new BadDataException("That wasn't a number - " + S));
		} catch (NoSuchElementException nsex) {
			initPoly();
			throw (new BadDataException("Not enough numbers in obox - " + S));
		}
		initPoly();
	}

	/**
	 * Makes certain that width and height are non-negative, and  
	 * rotation is in the right interval. This should be called
	 * after any changes to those parameters, but before 'initPoly'. 
	 */
	private void cleanParameters() {
		if (data[HEIGHT] < 0 || data[WIDTH] < 0) {
			double[] t = new double[] {0,0};
			if (data[HEIGHT] < 0) {
				t[1]= data[HEIGHT];
				data[HEIGHT] *= -1;
			}
			if (data[WIDTH] < 0) {
				t[0]= data[WIDTH];
				data[WIDTH] *= -1;
			}
			AffineTransform.getRotateInstance(Math.toRadians(-data[ROTATION])).transform(t,0,t,0,1);
			data[X_POS] += (int) t[0];
			data[Y_POS] += (int) t[1];
		}
		data[ROTATION] = data[ROTATION] % 360;
		if (data[ROTATION] < 0) {
			data[ROTATION] += 360;
		}
	}
	
	/**
	 * Directly change the value of this oriented box.
	 * @param x the x-coordinate of the origin
	 * @param y the y-coordinate of the origin
	 * @param width the width of the box
	 * @param height the height of the box
	 * @param rotation the orientation of the box
	 */
	public void set(int x, int y, int width, int height, int rotation) {
		data = new int[] { x, y, width, height, rotation % 360 };
		cleanParameters();
		initPoly();
	}

	/**
	 * Gets a String representation of the oriented box
	 * @return a space delimited string of integers 
	 */
	public String toString() {
		return (
			data[0]
				+ " "
				+ data[1]
				+ " "
				+ data[2]
				+ " "
				+ data[3]
				+ " "
				+ data[4]);
	}

	/**
	 * Copies the box.
	 * @return a new copy of the box.
	 */
	public Object clone() {
		return new OrientedBox(data);
	}

	protected void initPoly() {
		clearPolygon();
		/** These points represent the edge of the polygon */
		double[] points = {
				0,           0,
				data[WIDTH], 0,
				data[WIDTH], data[HEIGHT],
				0,           data[HEIGHT]  };
		Rational xCenter = new Rational(data[X_POS]);
		Rational yCenter = new Rational(data[Y_POS]);
		/** This is the rotation transform.*/
		AffineTransform trans = 
			AffineTransform.getTranslateInstance(data[X_POS], data[Y_POS]);
		trans.concatenate(AffineTransform.getRotateInstance(Math.toRadians(data[ROTATION])));
		trans.transform(points, 0, points, 0, 4);
		for (int i = 0; i < 4; i++) {
			Pnt temp =
				new Pnt(
					(int) Math.round(points[i * 2]),
					(int) Math.round(points[i * 2 + 1]));
			if (i == 2) { // Point diagonal from origin
				Rational temp2 = new Rational(2);

				Rational diagX = new Rational();
				Rational.minus(temp.getX(), xCenter, diagX);
				Rational.divide(diagX, temp2, diagX);
				Rational.plus(xCenter, diagX, xCenter);

				Rational diagY = new Rational();
				Rational.minus(temp.getY(), yCenter, diagY);
				Rational.divide(diagY, temp2, diagY);
				Rational.plus(yCenter, diagY, yCenter);

				centroid = new Pnt(xCenter, yCenter);
			}
			try {
				addVertex(temp);
			} catch (BadDataException bdx) {
				throw new RuntimeException(
					"Reflex Oriented Box Error! " + bdx.getMessage());
			}
		}
		initBbox();
	}

	/**
	 * Gets the x-coordinate of the box origin.
	 * @return the x-coordinate of the box origin
	 */
	public int getX() {
		return data[X_POS];
	}

	/**
	 * Gets the y-coordinate of the box origin.
	 * @return the y-coordinate of the box origin
	 */
	public int getY() {
		return data[Y_POS];
	}

	/**
	 * Gets the width of the box.
	 * @return the width of the box
	 */
	public int getWidth() {
		return data[WIDTH];
	}

	/**
	 * Gets the height of the box.
	 * @return the height of the box
	 */
	public int getHeight() {
		return data[HEIGHT];
	}

	/**
	 * Gets the rotation around the origin.
	 * @return the rotation around the origin in degrees.
	 */
	public int getRotation() {
		return data[ROTATION];
	}

	/**
	 * Gets the approximate center of the box.
	 * @return a point pretty close to the center of the box
	 */
	public Pnt getCentroid() {
		return centroid;
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
