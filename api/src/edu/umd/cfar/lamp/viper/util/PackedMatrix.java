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

import java.lang.reflect.*;
import java.util.*;

/**
 * This class implements a simple 2-dimensional matrix as a
 * wrapper around a standard java 2-dimensional array.
 *
 * @author <a href="mailto:davidm@cfar.umd.edu">David Mihalcik</a>
 * @see MatrixIndexOutOfBoundsException
 */
public class PackedMatrix implements edu.umd.cfar.lamp.viper.util.DataMatrix2d
{
  /** The internal representation of the packed matrix. */
  private Object[][] data;
  /** The type of object. This might be necessary for casting. */
  private Class type = Object.class;

  /**
   * Constructs a new PackedMatrix as a wrapper around
   * the given 2-dimensional array.
   *
   * @param data The array to wrap.
   */
  public PackedMatrix (Object[][] data)
  {
    this.data = data;
    String arrayClassName = data.getClass().toString();
    String className = arrayClassName;
    int index = arrayClassName.indexOf("[L");
    if (index >= 0) { 
      className = arrayClassName.substring (index+2, arrayClassName.length()-1);
      try {
        this.type = Class.forName (className);
      } catch (ClassNotFoundException cnfx) {
        index = -1;
      }
    }
    if (index < 0) {
      this.type = Object.class;
    }
  }

  /**
   * This constructor is necessary if you want {@link #getDataModel()
   * getDataModel} to return
   * an array of a specific type instead of Object[][].
   *
   * @param maxWidth The number of columns to have in the matrix.
   * @param maxHeight The number of rows to have in the matrix.
   * @param type The class object specifying the type of object stored here.
   */
  public PackedMatrix (int maxWidth, int maxHeight, Class type)
  {
    int[] dims = new int[] {maxWidth, maxHeight};
    this.type = type;
    data = (Object[][]) Array.newInstance (type, dims);
  }

  /**
   * Constructs a new matrix with the given dimensions.
   *
   * @param maxWidth The number of columns to have in the matrix.
   * @param maxHeight The number of rows to have in the matrix.
   */
  public PackedMatrix (int maxWidth, int maxHeight)
  {
    this.data = new Object[maxWidth][maxHeight];
  }

  /**
   * Creates a new <code>PackedMatrix</code>, carving out from 
   * an old one. Logically, it creates a matrix of the count
   * of all set bits in <code>cols</code> wide and the count
   * of all set bits in <code>rows</code> high, and fills it
   * with the data from those rows and columns in the old matrix.
   *
   * @param old The matrix to canabalize.
   * @param cols The columns to copy.
   * @param rows The rows to copy.
   */
  public PackedMatrix (DataMatrix2d old, BitSet cols, BitSet rows)
  {
    if (old instanceof PackedMatrix) {
      PackedMatrix pmold = (PackedMatrix) old;
      int[] newRowNums = new int[pmold.height()];
      int[] newColNums = new int[pmold.width()];

      int newWidth = 0;
      for (int i=0; i<cols.length(); i++) {
	if (cols.get (i)) {
	  newColNums[i] = newWidth++;
	} else {
	  newColNums[i] = -1;
	}
      }

      int newHeight = 0;
      for (int i=0; i<rows.length(); i++){
	if (rows.get(i)) {
	  newRowNums[i] = newHeight++;
	} else {
	  newRowNums[i] = -1;
	}
      }

      data = (Object[][]) Array.newInstance (pmold.type, new int[] {newWidth, newHeight});
      MatrixIterator mi = pmold.getMatrixIterator();
      while (mi.hasNextColumn()) {
	int currCol = mi.nextColumn();
	if (cols.get (currCol)) {
	  while (mi.hasNextInColumn()) {
	    Object temp = mi.nextInColumn();
	    int currRow = mi.currRow();
	    if (rows.get (currRow))
	      data[newColNums[currCol]][newRowNums[currRow]] = temp;
	  }
	}
      }
    } else {
      LinkedList rowNumMap = new LinkedList();
      LinkedList colNumMap = new LinkedList();

      for (int i=0; i<cols.length(); i++)
	if (cols.get (i))
	  colNumMap.add (new Integer (i));

      for (int i=0; i<rows.length(); i++)
	if (rows.get(i))
	  rowNumMap.add (new Integer (i));

      data = (Object[][]) Array.newInstance (Object.class, 
					     new int[] {colNumMap.size(),
						        rowNumMap.size()});

      MatrixIterator mi = old.getMatrixIterator();
      while (mi.hasNextColumn()) {
	Integer currCol = new Integer (mi.nextColumn());
	int newColNum = colNumMap.indexOf (currCol);
	if (0 <= newColNum) {
	  while (mi.hasNextInColumn()) {
	    Object temp = mi.nextInColumn();
	    Integer currRow = new Integer (mi.currRow());
	    int newRowNum = rowNumMap.indexOf (currRow);
	    if (0 <= newRowNum)
	      set (newColNum, newRowNum, temp);
	  }
	}
      }
    }
  }


  /**
   * Get the java array used to internally represent this matrix.
   *
   * @return The array that this wraps.
   */
  public Object[][] getDataModel ()
  {
    return data;
  }

  /**
   * Returns the current real height, that is, the index of the rightmost
   * column that contains data +1. This takes a while.
   *
   * @return An integer set to the index eastmost column containing data +1.
   */
  public int width()
  {
    for (int i = data.length-1; i>=0; i--)
      for (int j = data[0].length-1; j>=0; j--)
	if (null != data[i][j])
	  return i+1;
    return 0;
  }

  /**
   * Returns the current real height, that is, the largest row 
   * number (+1) that contains data. This takes a while.
   *
   * @return An integer set to the index northmost column containing data +1.
   */
  public int height()
  {
    for (int j = data[0].length-1; j>=0; j--)
      for (int i = data.length-1; i>=0; i--)
	if (null != data[i][j])
	  return j+1;
    return 0;
  }

  /**
   * Get the logical width of the matrix. This is equivalent
   * to the number of possible columns, x-length, or .length.
   * It is equavalent to <code>getDataModel().length</code>.
   *
   * @return The maximum possible column index + 1.
   */
  public int sizeWide()
  {
    return data.length;
  }

  /**
   * Get the logical height of the matrix. This is equivalent 
   * to the number of rows, y-length, or [0].length.
   * It is equavalent to <code>getDataModel()[0].length</code>.
   *
   * @return The maximum possible row index + 1.
   */
  public int sizeHigh()
  {
    return data[0].length;
  }

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
  public Object get (int column, int row)
  {
    try {
      return data[column][row];
    } catch (ArrayIndexOutOfBoundsException aioobx) {
      throw new MatrixIndexOutOfBoundsException (column, row);
    }
  }

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
  public Object remove (int column, int row)
  {
    try {
      Object o = data[column][row];
      data[column][row] = null;
      return o;
    } catch (ArrayIndexOutOfBoundsException aioobx) {
      throw new MatrixIndexOutOfBoundsException (column, row);
    }
  }

  /**
   * Sets the data at the specific location to the given value.
   *
   * @param column The column (x position) of the cell to set.
   * @param row The row (y position) of the cell to set.
   * @param data The object to put into the specified cell.
   * @throws MatrixIndexOutOfBoundsException if the index is 
   *             not within the boundaries of this matrix.
   */
  public void set (int column, int row, Object data)
  {
    try {
      this.data[column][row] = data;
    } catch (ArrayIndexOutOfBoundsException aioobx) {
      throw new MatrixIndexOutOfBoundsException (column, row);
    }
  }
  
  /**
   * Returns a MatrixIterator for this matrix.
   *
   * @return A MatrixIterator that can find all set cells.
   */
  public MatrixIterator getMatrixIterator ()
  {
    return new PackedMatrixIterator (data);
  }
}

/**
 * Implements {@link MatrixIterator} for {@link PackedMatrix packed matrices}.
 */
class PackedMatrixIterator implements MatrixIterator
{
  /** The column that next() just went to. */
  private int currCol = -1;
  /** The row that next() just went to. */
  private int currRow = -1;
  /** The column that hasNext just looked at. */
  private int nextCol = -1;
  /** The row that hasNext just looked at. */
  private int nextRow = -1;
  /** Keep a nsex around, so you don't have to make a new one. */
  private NoSuchElementException nsex = new NoSuchElementException();
  /** A reference to the data. */
  private Object[][] data;

  /**
   * The constructor takes the array that this will use.
   *
   * @param matrix A reference to the data to iterate through.
   */
  public PackedMatrixIterator (Object[][] matrix)
  {
    data = matrix;
  }

  /**
   * Advances to the next column and resets the pointer to
   * the beginning of that column.
   *
   * @return The column number.
   * @throws NoSuchElementException if there is no next column
   */
  public int nextColumn () throws NoSuchElementException
  {
    if (hasNextColumn()) {
      currRow = -1;
      return currCol = nextCol;
    }
    throw nsex;
  }

  /**
   * Advances to the start of the next non-empty row and
   * returns its index.
   *
   * @return The row number.
   * @throws NoSuchElementException if there is no next row.
   */
  public int nextRow () throws NoSuchElementException
  {
    if (hasNextRow()) {
      currCol = -1;
      return currRow = nextRow;
    }
    throw nsex;
  }

  /**
   * Returns the next non-empty cell in the matrix
   * in the positive x direction.
   *
   * @return The data stored next along the current row.
   * @throws NoSuchElementException if there is no more data in the row.
   */
  public Object nextInRow () throws NoSuchElementException
  {
    if (hasNextInRow())
      return data[currCol = nextCol][currRow];
    throw nsex;
  }

  /**
   * Returns the next non-empty cell in the matrix in the 
   * positive Y direction.
   *
   * @return The data stored next along the current column.
   * @throws NoSuchElementException if there is no more data in the column, or 
   *      not in a column.
   */
  public Object nextInColumn () throws NoSuchElementException {
    if (hasNextInColumn())
      return data[currCol][currRow = nextRow];
    throw nsex;
  }

  /**
   * Checks to see if there is a column to the east.
   *
   * @return <code>true</code> if iterator has another column.
   */
  public boolean hasNextColumn ()
  {
    if (nextCol <= currCol) {
      nextCol = currCol;
      while (++nextCol < data.length) {
	for (int j=0; j<data[nextCol].length; j++) {
	  if (data[nextCol][j] != null) {
	    return true;
	  }
	}
      }
      return false;
    } else {
      return nextCol < data.length;
    }
  }

  /**
   * Is there another item in this column?
   *
   * @return <code>true</code> if there is another piece of data along
   *      the current column.
   */
  public boolean hasNextInColumn ()
  {
    nextRow = currRow+1;
    while (nextRow < data[currCol].length) {
      if (data[currCol][nextRow] != null) {
	return true;
      }
      nextRow++;
    }
    return false;
  }

  /**
   * Checks to see if there is a row to the north.
   *
   * @return <code>true</code> if iterator has another row.
   */
  public boolean hasNextRow ()
  {
    if (nextRow <= currRow) {
      nextRow = currRow;
      while (++nextRow < data[0].length) {
	for (int i=0; i<data.length; i++) {
	  if (data[i][nextRow] != null) {
	    return true;
	  }
	}
      }
      return false;
    } else {
      return nextRow < data[0].length;
    }
  }

  /**
   * Is there another item in the current column?
   *
   * @return <code>true</code> if iterator has another item in this row.
   */
  public boolean hasNextInRow ()
  {
    nextCol = currCol+1;
    while (nextCol < data.length) {
      if (data[nextCol][currRow] != null) {
	return true;
      }
      nextCol++;
    }
    return false;
  }

  /**
   * Get the current row offset (y position).
   *
   * @return The row index.
   */
  public int currRow ()
  {
    return currRow;
  }

  /**
   * Get the current column offset (x position).
   *
   * @return The column index.
   */
  public int currColumn ()
  {
    return currCol;
  }
}
