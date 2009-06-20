/***************************************
 *:// L A M P . cfar . umd . edu       *
 *      AppLoader                      *
 *                                     *
 *      A tool for loading java apps   *
 *             from RDF descriptions.  *
 *                                     *
 * Distributed under the GPL license   *
 *        Terms available at gnu.org.  *
 *                                     *
 *  Copyright University of Maryland,  *
 *                      College Park.  *
 ***************************************/

package edu.umd.cfar.lamp.apploader.misc;

import java.awt.*;

/**
 * An event that indicates a user interaction on a
 * JTable. It is a little higher-level than a MouseEvent.
 * Note that the events are all called 'clicks', but there
 * is nothing preventing them from being generated in other
 * ways.
 */
public class TableEvent extends AWTEvent {
	/**
	 * Indicates a click asking for a context information.
	 * This is currently bound to 'BUTTON_2'.
	 */
	public static final int CONTEXT_CLICK = 0;

	/**
	 * Indicates is the standard click type.
	 * This is currently bound to 'BUTTON_1'.
	 */
	public static final int MAIN_CLICK    = 1;

	/**
	 * Indicates a click asking for an alternate click.
	 * This is currently bound to 'BUTTON_3'.
	 */
	public static final int ALT_CLICK     = 2;

	/**
	 * Indicates a click asking for an action to occur.
	 * This is currently bound to a double click of 'BUTTON_1'.
	 */
	public static final int ACTION_CLICK  = 3;

	/**
	 * Places the click in the center of the cell.
	 */
	public static final int CENTER = 0;
	/**
	 * Places the click in the east of the cell.
	 */
	public static final int EAST = 1;
	/**
	 * Places the click in the south of the cell.
	 */
	public static final int SOUTH = 2;
	/**
	 * Places the click in the southeast of the cell.
	 */
	public static final int SOUTHEAST = EAST | SOUTH;
	/**
	 * Places the click in the west of the cell.
	 */
	public static final int WEST = 4;
	/**
	 * Places the click in the southwest of the cell.
	 */
	public static final int SOUTHWEST = WEST | SOUTH;
	
	/**
	 * An arbitrary number selected to indicate the event type.
	 */
	public static final int TABLE_EVENT_ID = 1337;
	
	private int type;
	private int row, col;
	private int direction;
	
	/**
	 * Get the column of the cell-recipient of the event.
	 * @return The column that contained the click.
	 */
	public int getColumn() {
		return col;
	}
	/**
	 * Get the row of the cell-recipient of the event.
	 * -1 is the header.
	 * @return The row that contained the click.
	 */
	public int getRow() {
		return row;
	}
	/**
	 * Get the type of interaction event.
	 * @return The type of interaction event.
	 */
	public int getType() {
		return type;
	}
	
	/**
	 * Detects if the click was slightly to the
	 * east or south of the clicked cell. Useful for double-click
	 * on edge of a column.
	 * @return <code>true</code> iff the user clicked on the
	 * edge of the cell.
	 */
	public boolean onEdge() {
		return direction != CENTER;
	}
	
	/**
	 * Gets the direction in the cell where the event occurred,
	 * e.g. {@link #SOUTHWEST} of {@link #CENTER}.
	 * @return the direction
	 */
	public int getDirection() {
		return direction;
	}

	/**
	 * Creates a new event for the given source table
	 * of the given type at the specified cell
	 * @param source The table that contains the event
	 * @param type The type of event
	 * @param row of the cell with the event
	 * @param column column of the cell with the event
	 * @param direction 
	 */
	public TableEvent(Object source, int type, int row, int column, int direction) {
		super(source, TABLE_EVENT_ID);
		this.type = type;
		this.row = row;
		this.col = column;
		this.direction = direction;
	}
}
