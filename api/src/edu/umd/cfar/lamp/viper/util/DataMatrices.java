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
 * A variety of static methods for manipulating 
 * data matrices, including a transpose function
 * and a function for computing the optimal assignment
 * via the Hungarian algorithm.
 * 
 * The implementation of the Hungarian algorithm is 
 * as Knuth presented in his <a 
 * href="http://www-cs-faculty.stanford.edu/~knuth/sgb.html" 
 * title="Knuth: The Stanford GraphBase">book of graph algorithms</a> 
 * in <acronym title="Literate Programming in C">CWEB</acronym>.
 * 
 * @author  davidm
 */
public class DataMatrices {
    private static final boolean verbose = false;

    /**
     * Returns a version of the matrix, flipped.
     * @param mtx the matrix to transpose
     * @return a copy of the matrix, with 
     *   <code>m.get(x,y) ==  transpose(m).get(y,x)</code>
     */
    public static DataMatrix2d transpose (DataMatrix2d mtx) {
        DataMatrix2d trans;
        if (mtx instanceof PackedMatrix) {
            trans = new PackedMatrix (mtx.sizeHigh(), mtx.sizeWide());
        } else {
            trans = new SparseMatrix (mtx.sizeHigh(), mtx.sizeWide());
        }
        MatrixIterator iter = mtx.getMatrixIterator();
        while (iter.hasNextColumn()) {
            int currColumn = iter.nextColumn();
            while (iter.hasNextInColumn()) {
                Object c = iter.nextInColumn();
                trans.set (iter.currRow(), currColumn, c);
            }
        }
        return trans;
    }

    /**
     * Returns the data elements in the data matrix that minimize
     * the total cost of the bipartite graph whose edges are the
     * weighted using the objects in the matrix <code>mtx</code> using
     * the cost function <code>c</code>.
     * @param mtx  the matrix to be used as the edge list of the 
     *               bigraph
     * @param c    the cost function, called on each element of mtx
     * @return a list of nodes from mtx, at most one from each column
     *   or row, that minimizes the total cost
     */
    public static List assign(DataMatrix2d mtx, DataMatrices.GetCost c) {
        if (mtx.sizeHigh() > mtx.sizeWide()) {
            if (verbose) System.err.println ("Transposing matrix");
            mtx = transpose (mtx);
        }

        int[] colMate = new int[mtx.sizeHigh()]; /// The column matching a given row
        int[] rowMate = new int[mtx.sizeWide()]; /// The row matching a given column
        int[] parentRow = new int[mtx.sizeWide()]; /// The ancestor to a given column
        int[] unchosenRow = new int[mtx.sizeHigh()]; /// Node in the forest
        int t = 0; /// total number of nodes in the forest
        int q; /// explored nodes in the forest
        long[] rowDec = new long[mtx.sizeHigh()]; /// amount subtracted from each row
        long[] colInc = new long[mtx.sizeWide()]; /// amount added to each column
        long[] slack = new long[mtx.sizeWide()]; /// minimum uncovered entry seen in a given column
        int[] slackRow = new int[mtx.sizeWide()]; /// where the slack in a given column can be found
        int unmatched; /// this many rows have yet to be matched

        final long INF = 0x7fffffffL;
        Arrays.fill (rowMate, -1);
        Arrays.fill (parentRow, -1);
        Arrays.fill (colMate, 0);
        Arrays.fill (slack, INF);
        rows: for (MatrixIterator iter = mtx.getMatrixIterator(); iter.hasNextRow(); ) {
            int currRow = iter.nextRow();
            long minForRow = c.cost (iter.nextInRow());
            int minCol = iter.currColumn();
            while (iter.hasNextInRow()) {
                long currCost = c.cost (iter.nextInRow());
                if (currCost < minForRow) {
                    minForRow = currCost;
                    minCol = iter.currColumn();
                } else if (currCost == minForRow && rowMate[minCol] >= 0) {
                    minCol = iter.currColumn();
                }
            }
            rowDec[currRow] = minForRow;
            if (rowMate[minCol] < 0) {
                colMate[currRow] = minCol;
                rowMate[minCol] = currRow;
                if (verbose) System.err.println ("Matching col " + minCol + " to row " + currRow);
            } else {
                colMate[currRow] = -1;
                unchosenRow[t++] = currRow;
                if (verbose) System.err.println ("Unmatched row " + currRow);
            }
        } // rows

        //
        if (t == 0) {
            if (verbose) System.err.println ("Simple solution");
        } else { // no simple solution
            unmatched = t;
            out: while (true) {
                if (verbose) System.err.println ("I've matched " + (mtx.sizeHigh() - t) + "  rows");
                int row = -1;
                int col = -1;
                thru: while (true) {
                    deforrest: for (q = 0; q < t; q++) {
                        // explore qth node of forest; if matching can be 
                        // increased, breakthrough
                        row = unchosenRow[q];
                        long s = rowDec[row];
                        for (col = 0; col < rowMate.length; col++) {
                            long delta = c.cost(mtx.get (col, row)) - s + colInc[col];
                            if (delta < slack[col]) {
                                if (delta == 0) { // found a new zero
                                    if (rowMate[col] < 0) {
                                        break thru;
                                    }
                                    slack[col] = 0;
                                    parentRow[col] = row;
                                    if (verbose) System.err.println ("node: " + t + ": row " + rowMate[col] + "==col " + col + "--row " + row);
                                    unchosenRow[t++] = rowMate[col];
                                } else {
                                    slack[col] = delta;
                                    slackRow[col] = row;
                                }
                            }
                        } // for each column
                    } // deforrest (for each unchosen row)


                    // Introduce a new zero into the matrix; if matching
                    // can be increased, breakthrough
                    long minSlack = INF; // minimum slack of an unchosen row
                    for (col = 0; col < rowMate.length; col++) {
                        if (slack[col] != 0 && slack[col] < minSlack) {
                            minSlack = slack[col];
                        }
                    }
                    for (q = 0; q < t; q++) {
                        rowDec[unchosenRow[q]] += minSlack;
                    }
                    for (col = 0; col < rowMate.length; col++) {
                        if (slack[col] != 0) {
                            slack[col] -= minSlack;
                            if (slack[col] == 0) {
                                // Look at a new zero. Break through with colInc up to date
                                // if possible
                                row = slackRow[col];
                                if (verbose) System.err.println (" Decreasing uncovered elements by " + minSlack + " produces zero at [" + row + "," + col + "]");
                                if (rowMate[col] < 0) {
                                    for (int j = col+1; j < rowMate.length; j++) {
                                        if (slack[j] == 0) {
                                            colInc[j] += minSlack;
                                        }
                                    }
                                    break thru;
                                } else { // not a breakthrough, but forest continues to grow
                                    parentRow[col] = row;
                                    if (verbose) System.err.println ("  node " + t + ": row " + rowMate[col] + "==col " + col + "--row " + row);
                                    unchosenRow[t++] = rowMate[col];
                                }
                            }
                        } else {
                            colInc[col] += minSlack;
                        }
                    }


                } // thru

                /// Update the matching by pairing row k with col l
                if (verbose) System.err.println ("Breakthrough at node " + q + " of " + t);
                while (true) {
                    int j = colMate[row];
                    colMate[row] = col;
                    rowMate[col] = row;
                    if (verbose) System.err.println (" rematching col " + col + "==row " + row);
                    if (j < 0) break;
                    row = parentRow[j]; // follow the chain
                    col = j;            // up we go...
                }

                if (--unmatched == 0) {
                    break out;
                }

                /// reset loop parameters
                t = 0;
                Arrays.fill (parentRow, -1);
                Arrays.fill (slack, INF);
                for (int k = 0; k < mtx.sizeHigh(); k++) {
                    if (colMate[k] < 0) {
                        if (verbose) System.err.println ("node " + t + ": unmatched row " + k);
                        unchosenRow[t++] = k;
                    }
                }
            } // out
        } // if not easy

        /// Check for mistakes
        for (int col = 0; col < rowMate.length; col++) {
            for (int row = 0; row < colMate.length; row++) {
                if (c.cost(mtx.get (col, row)) < rowDec[row] - colInc[col]) {
                    throw new ArithmeticException ("Error in computation: negative corrected value");
                }
            }
        }
        for (int row = 0; row < colMate.length; row++) {
            int col = colMate[row];
            if ((col < 0) || (c.cost (mtx.get (col, row)) != rowDec[row] - colInc[col])) {
                throw new ArithmeticException ("Error in computation: Missingg mate, or mate is non-zero");
            }
        }
        q = 0;
        for (int col = 0; col < rowMate.length; col++) {
            if (colInc[col] != 0) {
                q++;
            }
        }
        if (q > colMate.length) {
            throw new ArithmeticException("Error in computation: too many column adjustments");
        }

        /// Build list
        LinkedList matches = new LinkedList();
        for (int row = 0; row  < colMate.length; row++) {
            Object curr = mtx.get (colMate[row], row);
            if (verbose) System.err.println ("Found [" + colMate[row] + ", " + row + "] == " + curr);
            if (curr != null) {
                matches.add (curr);
            }
        }
        return matches;
    }

    /**
     * This interface is for functor objects that uses the nodes of
     * a {@link DataMatrix2d} as weighted edges of a
     * bipartite graph.
     */
    public static interface GetCost {
        /**
        * Converts a node on a bipartite graph to a weight.
        * @param obj a node in the matrix / edge object in the graph
        * @return <code>long</code> representing the weight/cost of 
        *   the given edge
        */
        public long cost (Object obj);
    }

    /**
     * Assumes Object is an instance of Number, and calls its 
     * getLong() method to determine the node cost.
     */
    public static class PassThrough implements DataMatrices.GetCost {
        /**
         * Gets the longValue of the object.
         * @param obj the object to determine the cost of
         * @return <code>((Number) obj).longValue()</code>
         * @throws ClassCastException if <code>obj</code> doesn't 
         *         implement Number
         */
        public long cost (Object obj) {
            return ((Number) obj).longValue();
        }
    }
}
