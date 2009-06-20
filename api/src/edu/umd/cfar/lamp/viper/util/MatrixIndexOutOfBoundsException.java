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

package edu.umd.cfar.lamp.viper.util;

import java.awt.*;

/**
 * Thrown by {@link DataMatrix2d} objects when attempting to access data with a
 * negative index or above the specified boundaries.
 * 
 * @author <a href="mailto:davidm@cfar.umd.edu">David Mihalcik</a>
 * @see edu.umd.cfar.lamp.viper.util.DataMatrix2d
 */
public class MatrixIndexOutOfBoundsException extends IndexOutOfBoundsException {
	/**
	 * 1
	 */
	private static final long serialVersionUID = 1L;

	/**
	 * Constructs a new <code>MatrixIndexOutOfBoundsException</code> with no
	 * error message.
	 */
	public MatrixIndexOutOfBoundsException() {
	}

	/**
	 * Constructs a new <code>MatrixIndexOutOfBoundsException</code> with the
	 * specified message.
	 * 
	 * @param s
	 *            The detail message.
	 */
	public MatrixIndexOutOfBoundsException(String s) {
		super(s);
	}

	/**
	 * Constructs a new <code>MatrixIndexOutOfBoundsException</code> with an
	 * argument indicating the illegal index.
	 * 
	 * @param p
	 *            The x and y position of the error.
	 */
	public MatrixIndexOutOfBoundsException(Point p) {
		super(p.toString());
	}

	/**
	 * Constructs a new <code>MatrixIndexOutOfBoundsException</code> at the
	 * specified index.
	 * 
	 * @param column
	 *            The column that was attempted to be accessed.
	 * @param row
	 *            The row that was attempted to be accessed.
	 */
	public MatrixIndexOutOfBoundsException(int column, int row) {
		super("(" + column + ", " + row + ")");
	}
}
