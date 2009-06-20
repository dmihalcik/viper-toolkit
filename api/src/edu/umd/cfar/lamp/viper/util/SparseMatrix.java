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
 * This class implements a simple 2-dimensional sparse matrix.
 *
 * @author <a href="mailto:davidm@cfar.umd.edu">David Mihalcik</a>
 * @see MatrixIndexOutOfBoundsException
 */
public class SparseMatrix implements DataMatrix2d
{
  /**
   * A class representing a single cell.
   * Be careful, as the SparseMatrix class is responsible
   * for maintaining it. It just has some a convenience constructor
   * and a toString method.
   * Also, note that the cardinal directions are used synonymously
   * with coordinates, where North = +Y, South = -Y; East = +X, West = -X.
   */
  private class SparseNode
  {
    /** The data associated with this node. */
    public Object data = null;
    /** This node's x-location. */
    public int col;
    /** This node's y-location. */
    public int row;

    // North = +Y, South = -Y; East = +X, West = -X
    /** The node to the North, or a dummy if this is the northernmost. */
    public SparseNode north = null;
    /** The node to the East, or a dummy if this is the easternmost. */
    public SparseNode east = null;
    /** The node to the South, or a dummy if this is the Southernmost. */
    public SparseNode south = null;
    /** The node to the west, or a dummy if this is the westernmost. */
    public SparseNode west = null;

    /**
     * Creates a new SparseNode pointing to the specified data node.
     * @param data The data to use.
     */
    SparseNode (Object data) {
      this.data = data;
    }

    /**
     * Print out the node (usually for debugging) with its associated
     * location.
     * @return <code>[(<i>col</i>, <i>row</i>), <i>data</i>]</code>
     */
    public String toString ()
    {
      return "[(" + col + ", " + row + "), " + data + "]";
    }
  }

  /** The master node points to the dummies.
   * It is located at (DUMMY, DUMMY). */
  private SparseNode master;
  /** Convenience pointer. */
  private SparseNode curr;
  /** Points to a dummy node along the dummy row. */
  private SparseNode currentCol;
  /** Points to a dummy node along the dummy column.*/
  private SparseNode currentRow;

  /** A SparseMatrix can't have nodes stored to the east of this. */
  private int maxWidth = Integer.MAX_VALUE;
  /** A SparseMatrix can't have nodes stored to the North of this. */
  private int maxHeight = Integer.MAX_VALUE;

  /** The offset of the dummy rows / columns (-1). */
  private static final int DUMMY = -1;

  /** The number of data nodes currently stored. (Dummies don't count.) */
  private int size = 0;

  /**
   * Creates an empty matrix.
   * Maximum size defaults to <code>Integer.MAX_VALUE</code>.
   */
  public SparseMatrix ()
  {
    master = new SparseNode ("master");
    master.row = master.col = DUMMY;
    master.north = master.south = master;
    master.west = master.east = master;

    curr = currentCol = currentRow = master;
  }

  /**
   * Creates a new <code>SparseMatrix</code> that cannot 
   * have data beyond the specified dimensions.
   *
   * @param maxWidth The greatest column offset.
   * @param maxHeight The greatest row offset.
   * @see #SparseMatrix(int maxWidth, int maxHeight)
   */
  private void starter (int maxWidth, int maxHeight)
  {
    master = new SparseNode ("master");
    master.row = master.col = DUMMY;
    master.north = master.south = master;
    master.west = master.east = master;

    curr = currentCol = currentRow = master;

    this.maxWidth = maxWidth;
    this.maxHeight = maxHeight;
  }

  /**
   * Creates a new <code>SparseMatrix</code> that cannot 
   * have data beyond the specified dimensions.
   *
   * @param maxWidth The greatest column offset.
   * @param maxHeight The greatest row offset.
   */
  public SparseMatrix (int maxWidth, int maxHeight)
  {
    starter (maxWidth, maxHeight);
  }

  /**
   * Creates a new <code>SparseMatrix</code>, carving out from 
   * an old one. Logically, it creates a matrix of the count
   * of all set bits in <code>cols</code> wide and the count
   * of all set bits in <code>rows</code> high, and fills it
   * with the data from those rows and columns in the old matrix.
   *
   * @param old The matrix to canabalize.
   * @param cols The columns to copy.
   * @param rows The rows to copy.
   */
  public SparseMatrix (DataMatrix2d old, BitSet cols, BitSet rows)
  {
    LinkedList rowNumMap = new LinkedList();
    LinkedList colNumMap = new LinkedList();

    for (int i=0; i<cols.length(); i++)
      if (cols.get (i))
	colNumMap.add (new Integer (i));

    for (int i=0; i<rows.length(); i++)
      if (rows.get(i))
	rowNumMap.add (new Integer (i));

    starter (colNumMap.size(), rowNumMap.size());

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

  /**
   * Determines if this is the same as another {@link DataMatrix2d}.
   * Requires that all of the data implement equals() as well.
   * @param other Another {@link DataMatrix2d}.
   * @return <code>true</code> if the other has all the same data as this.
   */
  public boolean equals (Object other)
  {
    if (other == this)
      return true;
    else if (other instanceof DataMatrix2d) {
      MatrixIterator miOther = ((DataMatrix2d) other).getMatrixIterator();
      MatrixIterator miThis = getMatrixIterator();
      while (miThis.hasNextRow() || miOther.hasNextRow()) {
	if (!miThis.hasNextRow() || !miOther.hasNextRow())
	  return false;
	if (miThis.nextRow() != miOther.nextRow())
	  return false;

	while (miThis.hasNextInRow() || miOther.hasNextInRow()) {
	  if (!miThis.hasNextInRow() || !miOther.hasNextInRow())
	    return false;
	  if (miThis.currColumn() != miOther.currColumn())
	    return false;
	  if (! miThis.nextInRow().equals (miOther.nextInRow()))
	    return false;
	}
      }
      return true;
    }
    else
      return false;
  }

  /**
   * Returns the current real height, that is, the index of the rightmost
   * column that contains data +1.
   *
   * @return An integer set to the index eastmost column containing data +1.
   */
  public int width()
  {
    return master.west.col+1;
  }

  /**
   * Returns the current real height, that is, the largest row 
   * number (+1) that contains data.
   *
   * @return An integer set to the index northmost column containing data +1.
   */
  public int height()
  {
    return master.south.row+1;
  }

  /**
   * Get the logical width of the matrix. This is equivalent
   * to the number of possible columns, x-length, or .length.
   *
   * @return The maximum possible column index + 1.
   */
  public int sizeWide()
  {
    return maxWidth;
  }

  /**
   * Get the logical height of the matrix. This is equivalent 
   * to the number of rows, y-length, or [0].length.
   *
   * @return The maximum possible row index + 1.
   */
  public int sizeHigh()
  {
    return maxHeight;
  }

  /**
   * Returns the node (or the node to the south of)
   * the node in this column with the specified index.
   * 
   * @param row The y index of the node to look for.
   * @param col A node in the column. 
   * @return The node, if it exists, or the closest to the south.
   */
  private SparseNode findInColumn (SparseNode col, int row)
  {
    if ((row < 0) || (row >= maxHeight))
      throw new MatrixIndexOutOfBoundsException (": row " + row);

    if (col.row == DUMMY)
      col = col.north;

    if (col.row >= row) {
      while (col.row > row)
	col = col.south;
    } else {
      while ((col.row < row) && (col.row != DUMMY))
	col = col.north;
      if (col.row != row)
	col = col.south;
    }
    return col;
  }

  /**
   * Returns the node (or the node to the west of)
   * the node with the specified index.
   *
   * @param row A node in the row.
   * @param column The x index of the node to look for.
   * @return The node, if it exists, or the closest to the east.
   */
  private SparseNode findInRow (SparseNode row, int column)
  {
    if ((column < 0) || (column >= maxWidth))
      throw new MatrixIndexOutOfBoundsException (": col " + column);

    if (row.col == DUMMY)
      row = row.east;

    if (row.col >= column) {
      while (row.col > column)
	row = row.west;
    } else {
      while ((row.col < column) && (row.col != DUMMY))
	row = row.east;
      if (row.col != column)
	row = row.west;
    }
    return row;
  }

  /**
   * Add a dummy header for the specified row, if none exists,
   * and set currentCol to it.
   *
   * @param row The row to add.
   * @return The row dummy node.
   */
  private SparseNode addRow (int row)
  {
    if ((row < 0) || (row >= maxHeight))
      throw new MatrixIndexOutOfBoundsException (": row " + row);

    currentRow = findInColumn (master, row);
    if (currentRow.row == row)
      return currentRow;

    SparseNode temp = new SparseNode ("Row Dummy");
    temp.col = DUMMY;
    temp.row = row;
    temp.west = temp.east = temp;

    temp.south = currentRow;
    temp.north = currentRow.north;

    temp.south.north = temp;
    temp.north.south = temp;

    return currentRow = temp;
  }

  /**
   * Add a dummy header for the specified column, if none exists,
   * and set currentCol to it.
   *
   * @param column The column to add.
   * @return The column dummy node.
   */
  private SparseNode addColumn (int column)
  {
    if ((column < 0) || (column >= maxWidth))
      throw new MatrixIndexOutOfBoundsException (": column " + column);

    currentCol = findInRow (master, column);
    if (currentCol.col == column)
      return currentCol;

    SparseNode temp = new SparseNode ("Column Dummy");

    temp.col = column;
    temp.row = DUMMY;
    temp.north = temp.south = temp;

    temp.west = currentCol;
    temp.east = currentCol.east;

    temp.west.east = temp;
    temp.east.west = temp;

    return currentCol = temp;
  }

  /**
   * Sets the data at the specific location to the given value.
   *
   * @param column The column (x position) of the cell to set.
   * @param row The row (y position) of the cell to set.
   * @param data The object to put into the specified cell.
   */
  public void set (int column, int row, Object data)
  {
    if (data == null) {
      remove (column, row);
      return;
    }

    addRow (row);
    addColumn (column);

    curr = findInColumn (currentCol, row);

    // Check to see if the node at the location is in the matrix.
    // If so, change its data to point at the new data.
    if (curr.row == row) {
      curr.data = data;
    }
    // If the node isn't in the matrix, add it.
    else if (data != null) {
      size++;

      SparseNode temp = new SparseNode (data);
      temp.col = column;
      temp.row = row;

      temp.south = curr;
      temp.north = curr.north;
      temp.south.north = temp;
      temp.north.south = temp;

      curr = findInRow (currentRow, column);
      temp.west = curr;
      temp.east = curr.east;
      temp.west.east = temp;
      temp.east.west = temp;
    }
  }

  /**
   * Returns the object at the given location.
   * Take O(column+row).
   *
   * @param column The column (x position) of the cell to get.
   * @param row The row (y position) of the cell to get.
   * @return A reference to the object in the specified cell,
   *             or null if none is found.
   */
  public Object get (int column, int row)
  {
    if ((column < 0) || (column >= maxWidth)
	|| (row < 0) || (row >= maxHeight))
      throw new MatrixIndexOutOfBoundsException (column, row);
    curr = master;
    curr = findInRow (curr, column);
    if (curr.col == column) {
      curr = findInColumn (curr, row);
      if (curr.row == row)
	return curr.data;
    }
    return null;
  }


  /**
   * Sets the object at the specified location to null. It will no
   * longer be found by a MatrixIterator or an unordered Iterator.
   * Take O(column+row).
   * 
   * @param column The column (x position) of the cell to remove.
   * @param row The row (y position) of the cell to remove.
   * @return A reference to the object that was in the specified cell,
   *             or null if none is found.
   */
  public Object remove (int column, int row)
  {
    if ((column < 0) || (column >= maxWidth)
	|| (row < 0) || (row >= maxHeight))
      throw new MatrixIndexOutOfBoundsException (column, row);

    curr = master;
    curr = findInRow (curr, column);
    if (curr.col != column) {
      return null;
    }

    curr = findInColumn (curr, row);
    if (curr.row != row) {
      return null;
    }

    size--;

    // Remove it from the matrix.
    curr.north.south = curr.south;
    curr.south.north = curr.north;
    curr.east.west = curr.west;
    curr.west.east = curr.east;
    
    // If the column is now empty, remove its dummy
    if (curr.north == curr.south) {
      curr.north = curr.north.west;
      curr.north.east = curr.north.east.east;
      curr.north.east.west = curr.north;
    }

    // If the row is now empty, remove its dummy
    if (curr.east == curr.west) {
      curr.east = curr.east.south;
      curr.east.north = curr.east.north.north;
      curr.east.north.south = curr.east;
    }

    Object temp = curr.data;
    curr = master;
    return temp;
  }

  /**
   * Gets the current count of occupied cells.
   *
   * @return Number of occupied cells.
   */
  public int getUsedNodeCount()
  {
    return size;
  }

  /**
   * Implements MatrixIterator for Sparse Matrices.
   */
  class SparseMatrixIterator implements MatrixIterator
  {
    /** The node that a call to next most recently returned. */
    private SparseNode curr = master;
    /** A dummy node that points at the current Column (if going columnwise). */
    private SparseNode currentCol = master;
    /** A dummy node that points at the current Row (if going rowwise). */
    private SparseNode currentRow = master;
    /** Save time on nsexes, by creating one to keep. */
    private NoSuchElementException nsex = new NoSuchElementException();

    /**
     * Advances to the next column and resets the pointer to
     * the beginning of that column.
     *
     * @return The column number.
     * @throws NoSuchElementException if there is no next column
     */
    public int nextColumn () throws NoSuchElementException
    {
      if (currentCol.east.col == DUMMY) {
	nsex.fillInStackTrace();
	throw nsex;
      }
      curr = currentCol = currentCol.east;
      return currentCol.col;
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
      if (currentRow.north.row == DUMMY) {
	nsex.fillInStackTrace();
	throw nsex;
      }
      curr = currentRow = currentRow.north;
      return currentRow.row;
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
      if (curr.east.col == DUMMY || curr.row == DUMMY) {
	nsex.fillInStackTrace();
	throw nsex;
      }
      curr = curr.east;
      return curr.data;
    }

    /**
     * Returns the next non-empty cell in the matrix in the 
     * positive Y direction.
     *
     * @return The data stored next along the current column.
     * @throws NoSuchElementException if there is no more data in the column, or 
     *      not in a column.
     */
    public Object nextInColumn () throws NoSuchElementException
    {
      if (curr.north.row == DUMMY || curr.col == DUMMY) {
	nsex.fillInStackTrace();
	throw nsex;
      }
      curr = curr.north;
      return curr.data;
    }

    /**
     * Checks to see if there is a column to the east.
     *
     * @return <code>true</code> if iterator has another column.
     */
    public boolean hasNextColumn ()
    {
      return (currentCol.east.col != DUMMY);
    }

    /**
     * Is there another item in this column?
     *
     * @return <code>true</code> if there is another piece of data along
     *      the current column.
     */
    public boolean hasNextInColumn ()
    {
      return (curr.north.row != DUMMY);
    }

    /**
     * Checks to see if there is a row to the north.
     *
     * @return <code>true</code> if iterator has another row.
     */
    public boolean hasNextRow ()
    {
      return (currentRow.north.row != DUMMY);
    }

    /**
     * Returns the next non-empty cell in the matrix
     * in the positive x direction.
     *
     * @return The data stored next along the current row.
     * @throws NoSuchElementException if there is no more data in the row.
     */
    public boolean hasNextInRow ()
    {
      return (curr.east.col != DUMMY);
    }

    /**
     * Get the current row offset (y position).
     *
     * @return The row index.
     */
    public int currRow ()
    {
      return curr.row;
    }
    
    /**
     * Get the current column offset (x position).
     *
     * @return The column index.
     */
    public int currColumn ()
    {
      return curr.col;
    }

    /**
     * For debugging: Print this node and those nearby.
     * @return A multi-line String listing this 
     *         and the nodes in each direction.
     */
    String printNeighborhood ()
    {
      return "This = " + curr
	+ "\n  north = " + curr.north
	+ "\n  south = " + curr.south
	+ "\n  east = " + curr.east
	+ "\n  west = " + curr.west;
    }
  }

  /**
   * Returns a MatrixIterator for this matrix. 
   * <em>Use this whenever possible, as random access is slow.</em>
   *
   * @return A MatrixIterator that can find all set cells.
   */
  public MatrixIterator getMatrixIterator ()
  {
    return new SparseMatrixIterator();
  }
}
