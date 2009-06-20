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


/**
 * A generalized interface for a 2 dimensional matrix of Objects.
 * 
 * @author <a href="mailto:davidm@cfar.umd.edu">David Mihalcik</a>
 * @see edu.umd.cfar.lamp.viper.util.MatrixIndexOutOfBoundsException
 */
public interface DataMatrix2d
{
  /**
   * Returns the current real height, that is, the index of the rightmost
   * column that contains data +1.
   *
   * @return An integer set to the index eastmost column containing data +1.
   */
  public int width();

  /**
   * Returns the current real height, that is, the largest row 
   * number (+1) that contains data.
   *
   * @return An integer set to the index northmost column containing data +1.
   */
  public int height();

  /**
   * Get the logical width of the matrix. This is equivalent
   * to the number of possible columns, x-length, or .length.
   *
   * @return The maximum possible column index + 1.
   */
  public int sizeWide();

  /**
   * Get the logical height of the matrix. This is equivalent 
   * to the number of rows, y-length, or [0].length.
   *
   * @return The maximum possible row index + 1.
   */
  public int sizeHigh();

  /**
   * Sets the data at the specific location to the given value.
   *
   * @param column The column (x position) of the cell to set.
   * @param row The row (y position) of the cell to set.
   * @param data The object to put into the specified cell.
   * @throws MatrixIndexOutOfBoundsException if the index is 
   *             not within the boundaries of this matrix.
   */
  public void set (int column, int row, Object data) throws MatrixIndexOutOfBoundsException;

  /**
   * Returns the object at the given location.
   *
   * @param column The column (x position) of the cell to get.
   * @param row The row (y position) of the cell to get.
   * @return A reference to the object in the specified cell,
   *           or null if none is found.
   * @throws MatrixIndexOutOfBoundsException if the index is 
   *             not within the boundaries of this matrix.
   */
  public Object get (int column, int row) throws MatrixIndexOutOfBoundsException;

  /**
   * Sets the object at the specified location to null. It will no
   * longer be found by a MatrixIterator or an unordered Iterator.
   *
   * @param column The column (x position) of the cell to remove.
   * @param row The row (y position) of the cell to remove.
   * @return A reference to the object that was in the specified cell,
   *             or null if none is found.
   * @throws MatrixIndexOutOfBoundsException if the index is 
   *             not within the boundaries of this matrix.
   */
  public Object remove (int column, int row) throws MatrixIndexOutOfBoundsException;

  /**
   * Returns a MatrixIterator for this matrix.
   *
   * @return A MatrixIterator that can find all set cells.
   */
  public MatrixIterator getMatrixIterator ();
}
