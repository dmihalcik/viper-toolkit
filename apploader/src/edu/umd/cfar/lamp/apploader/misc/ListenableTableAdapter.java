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

import javax.swing.event.*;

/**
 * Implements the ListenableTable interface, but doesn't provide
 * a table.
 */
public class ListenableTableAdapter implements ListenableTable {
	protected EventListenerList tableListeners = new EventListenerList();

	/**
	 * @see edu.umd.cfar.lamp.apploader.misc.ListenableTable#getTableListeners()
	 */
	public TableListener[] getTableListeners() {
		return (TableListener[]) tableListeners.getListeners(TableListener.class);
	}

	/**
	 * @see edu.umd.cfar.lamp.apploader.misc.ListenableTable#removeTableListener(edu.umd.cfar.lamp.apploader.misc.TableListener)
	 */
	public void removeTableListener(TableListener tl) {
		tableListeners.remove(TableListener.class, tl);
	}

	/**
	 * @see edu.umd.cfar.lamp.apploader.misc.ListenableTable#addTableListener(edu.umd.cfar.lamp.apploader.misc.TableListener)
	 */
	public void addTableListener(TableListener tl) {
		tableListeners.add(TableListener.class, tl);
	}

	/**
	 * Fire the given event to all listeners.
	 * @param e The event to fire.
	 */
	public void fireEvent(TableEvent e) {
		Object[] L = tableListeners.getListenerList();
		for (int i = L.length - 2; i >= 0; i -= 2) {
			if (L[i] == TableListener.class) {
				TableListener curr = (TableListener) L[i+1];
				switch (e.getType()) {
					case TableEvent.CONTEXT_CLICK :
						curr.contextClick(e);
						break;
					case TableEvent.MAIN_CLICK :
						curr.click(e);
						break;
					case TableEvent.ALT_CLICK :
						curr.altClick(e);
						break;
					case TableEvent.ACTION_CLICK :
						curr.actionClick(e);
						break;
					default :
						throw new IllegalArgumentException(
							"Not a click type: " + e.getType());
				}
			}
		}
	}

	/**
	 * Fire a context click to the table.
	 * This is equivalent to a windows right-click.
	 * @param source The JTable that acts as the source.
	 * @param row The row containing the cell the user clicked.
	 * @param column The column containing the cell the user clicked.
	 * @param direction the section of the cell that is clicked
	 */
	public void fireContextClick(ListenableTable source, int row, int column, int direction) {
		if (tableListeners.getListenerCount() > 0) {
			fireEvent(
				new TableEvent(source, TableEvent.CONTEXT_CLICK, row, column, direction));
		}
	}

	/**
	 * Fire a context click to the table.
	 * This is equivalent to a windows left-click.
	 * @param source The JTable that acts as the source.
	 * @param row The row containing the cell the user clicked.
	 * @param column The column containing the cell the user clicked.
	 * @param direction the section of the cell that is clicked
	 */
	public void fireClick(ListenableTable source, int row, int column, int direction) {
		if (tableListeners.getListenerCount() > 0) {
			fireEvent(
				new TableEvent(source, TableEvent.MAIN_CLICK, row, column, direction));
		}
	}
	/**
	 * Fire an click to the table.
	 * This is equivalent to a windows center-click.
	 * @param source The JTable that acts as the source.
	 * @param row The row containing the cell the user clicked.
	 * @param column The column containing the cell the user clicked.
	 * @param direction the section of the cell that is clicked
	 */
	public void fireAltClick(ListenableTable source, int row, int column, int direction) {
		if (tableListeners.getListenerCount() > 0) {
			fireEvent(
				new TableEvent(source, TableEvent.ALT_CLICK, row, column, direction));
		}
	}
	/**
	 * Fire an action click to the table.
	 * This is equivalent to a windows double-left-click.
	 * @param source The JTable that acts as the source.
	 * @param row The row containing the cell the user clicked.
	 * @param column The column containing the cell the user clicked.
	 * @param direction the section of the cell that is clicked
	 */
	public void fireActionClick(ListenableTable source, int row, int column, int direction) {
		if (tableListeners.getListenerCount() > 0) {
			fireEvent(
				new TableEvent(source, TableEvent.ACTION_CLICK, row, column, direction));
		}
	}
}
