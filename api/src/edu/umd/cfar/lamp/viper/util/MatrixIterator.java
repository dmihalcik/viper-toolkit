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

import java.util.*;

/**
 * An iterator for moving through data arranged in a 2-dimensional
 * matrix. To iterate column-by-column, use
 * <pre>
 * MatrixIterator mi = matrix.getMatrixIterator();
 * while (hasNextColumn()) {
 *     mi.nextColumn();
 *     while (hasNextInColumn())
 *         Object data = mi.nextInColumn();
 * }
 * 
 * </pre>
 * Alternatively, you can go through each row:
 * <pre>
 * MatrixIterator mi = matrix.getMatrixIterator();
 * while (hasNextRow()) {
 *     mi.nextRow();
 *     while (hasNextInRow())
 *         Object data = mi.nextInRow();
 * }
 * 
 * </pre>
 * 
 * @author <a href="mailto:davidm@cfar.umd.edu">David Mihalcik</a>
 * @see edu.umd.cfar.lamp.viper.util.DataMatrix2d
 */
public interface MatrixIterator
{
  /**
   * Advances to the next column and resets the pointer to
   * the beginning of that column.
   *
   * @return The column number.
   * @throws NoSuchElementException if there is no next column
   */
  public int nextColumn() throws NoSuchElementException;

  /**
   * Checks to see if there is a column to the east.
   *
   * @return <code>true</code> if iterator has another column.
   */
  public boolean hasNextColumn ();

  /**
   * Returns the next non-empty cell in the matrix
   * in the positive x direction.
   *
   * @return The data stored next along the current row.
   * @throws NoSuchElementException if there is no more data in the row.
   */
  public Object nextInRow () throws NoSuchElementException;

  /**
   * Is there another item in the current column?
   *
   * @return <code>true</code> if iterator has another item in this row.
   */
  public boolean hasNextInRow ();


  /**
   * Advances to the start of the next non-empty row and
   * returns its index.
   *
   * @return The row number.
   * @throws NoSuchElementException if there is no next row.
   */
  public int nextRow() throws NoSuchElementException;

  /**
   * Checks to see if there is a row to the north.
   *
   * @return <code>true</code> if iterator has another row.
   */
  public boolean hasNextRow ();

  /**
   * Returns the next non-empty cell in the matrix in the 
   * positive Y direction.
   *
   * @return The data stored next along the current column.
   * @throws NoSuchElementException if there is no more data in the column, or 
   *      not in a column.
   */
  public Object nextInColumn () throws NoSuchElementException;

  /**
   * Is there another item in this column?
   *
   * @return <code>true</code> if there is another piece of data along
   *      the current column.
   */
  public boolean hasNextInColumn ();

  /**
   * Get the current column offset (x position).
   *
   * @return The column index.
   */
  public int currColumn ();

  /**
   * Get the current row offset (y position).
   *
   * @return The row index.
   */
  public int currRow ();
}
