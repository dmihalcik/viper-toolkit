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

package edu.umd.cfar.lamp.apploader.undo;

import java.awt.*;
import java.util.*;
import java.util.List;

import javax.swing.*;
import javax.swing.event.*;
import javax.swing.table.*;
import javax.swing.undo.*;

import edu.umd.cfar.lamp.apploader.*;
import edu.umd.cfar.lamp.apploader.misc.*;

/**
 * A javabean/Swing widget that provides a view into the user's undo history. It
 * is designed to work with the AppLoader framework.
 */
public class ActionHistoryPane extends JScrollPane {
	private List history;
	private int cursor;
	private AppLoader core;
	private int lastSavePoint;
	/// number of items to save before the last save point
	private int bufferLength = 99;

	/**
	 * Draws the cells in the undo list.
	 */
	private class UndoCellRenderer extends DefaultTableCellRenderer {
		/**
		 * {@inheritDoc}
		 */
		public Component getTableCellRendererComponent(JTable table,
				Object value, boolean isSelected, boolean hasFocus, int row,
				int column) {
			Component c = super.getTableCellRendererComponent(table,
					((UndoableEdit) value).getPresentationName(), isSelected,
					hasFocus, row, column);
			if (row < cursor) {
				c.setBackground(Color.white);
				c.setForeground(Color.darkGray);
			} else {
				c.setBackground(Color.lightGray);
				c.setForeground(Color.black);
			}
			return c;
		}
	}

	/**
	 * The table data model for the undo table.
	 */
	private class UndoTableModel extends AbstractTableModel {
		/**
		 * {@inheritDoc}
		 * 
		 * @return 1
		 */
		public int getColumnCount() {
			return 1;
		}
		/**
		 * {@inheritDoc}
		 * 
		 * @return number of events in the history
		 */
		public int getRowCount() {
			return history.size();
		}
		/**
		 * {@inheritDoc}
		 * 
		 * @return <code>false</code>
		 */
		public boolean isCellEditable(int rowIndex, int columnIndex) {
			return false;
		}
		/**
		 * {@inheritDoc}
		 */
		public Object getValueAt(int rowIndex, int columnIndex) {
			return history.get(rowIndex);
		}
		/**
		 * {@inheritDoc}
		 */
		public String getColumnName(int columnIndex) {
			return "Action";
		}
		/**
		 * {@inheritDoc}
		 */
		public Class getColumnClass(int columnIndex) {
			return UndoableEdit.class;
		}
	}

	/**
	 * Get the table that is currently displayed.
	 * 
	 * @return The table widget.
	 */
	private EnhancedTable getTable() {
		return (EnhancedTable) getViewport().getView();
	}
	/**
	 * Get the table model that backs the action history pane.
	 * 
	 * @return The data model.
	 */
	private UndoTableModel getModel() {
		return (UndoTableModel) getTable().getModel();
	}

	/**
	 * Add a new action after the cursor. Currently, this deletes all existing
	 * events after the cursor.
	 * 
	 * @param e
	 *            The new edit.
	 */
	public void addAction(UndoableEdit e) {
		if (cursor < lastSavePoint) {
			lastSavePoint = -1;
		}
		if (history.size() != cursor) {
			int oldSize = history.size();
			history.subList(cursor, history.size()).clear();
			getModel().fireTableRowsDeleted(cursor, oldSize - 1);
		}
		history.add(e);
		cursor = history.size();
		getModel().fireTableRowsInserted(cursor - 1, cursor - 1);
		clearToBuffer();
	}

	/**
	 * Creates a new history pane without any undo objects.
	 */
	public ActionHistoryPane() {
		super(new EnhancedTable());
		history = new ArrayList();
		lastSavePoint = 0;

		// Add table
		EnhancedTable table = getTable();
		UndoTableModel tableModel = new UndoTableModel();
		table.setModel(tableModel);
		table.setDefaultRenderer(UndoableEdit.class, new UndoCellRenderer());
		table.addTableListener(new MyTableListener());
	}

	/**
	 * A table listener that allows the user to navigate through the history
	 * directly.
	 */
	private class MyTableListener implements TableListener {
		/**
		 * Does nothing.
		 * 
		 * @param e
		 *            {@inheritDoc}
		 */
		public void contextClick(TableEvent e) {
		}
		/**
		 * Goes to the appropriate point in the action history.
		 * 
		 * @param e
		 *            {@inheritDoc}
		 */
		public void actionClick(TableEvent e) {
			int goTo = e.getRow();
			goTo = goTo < 0 ? 0 : history.size() < goTo
					? history.size() - 1
					: goTo;
			if (cursor <= goTo) {
				// XXX
				// If the user clicks a gray space, make it white
				// Otherwise, make the gray space white. This
				// makes the double-click a toggle,
				// and avoids having to put a blank box at the end
				// or a 'load file' or 'save file' box at the top
				// This may prove too confusing.
				goTo += 1;
			}
			moveCursorTo(goTo);
		}
		/**
		 * Does nothing.
		 * 
		 * @param e
		 *            {@inheritDoc}
		 */
		public void click(TableEvent e) {
		}
		/**
		 * Does nothing.
		 * 
		 * @param e
		 *            {@inheritDoc}
		 */
		public void altClick(TableEvent e) {
		}

	}

	/**
	 * Get the associated application launcher.
	 * 
	 * @return the AppLoader
	 */
	public AppLoader getCore() {
		return core;
	}

	/**
	 * Sets the associated application loader. This is needed for user
	 * preferences, such as the undo limit and key controls.
	 * 
	 * @param loader
	 *            The application loader for the app that holds this widget.
	 */
	public void setCore(AppLoader loader) {
		core = loader;
	}

	/**
	 * Undo/redo until the cursor is as close to the given index as possible.
	 * 
	 * @param i
	 *            Where to move the cursor
	 */
	public void moveCursorTo(int i) {
		if (i < 0) {
			i = 0;
		} else if (i > history.size()) {
			i = history.size();
		}
		if (i != cursor) {
			if (cursor < i) {
				while (cursor < i) {
					UndoableEdit curr = (UndoableEdit) history.get(cursor++);
					curr.redo();
				}
			} else {
				while (cursor > i) {
					UndoableEdit curr = (UndoableEdit) history.get(--cursor);
					curr.undo();
				}
			}
			getModel().fireTableDataChanged(); // change the display
		}
		clearToBuffer();
	}

	/**
	 * Undo one action. Moves the cursor back one.
	 */
	public void undo() {
		if (canUndo()) {
			moveCursorTo(cursor - 1);
		}
	}
	/**
	 * Tests to see if any undo events are available on the stack.
	 * @return if undo will do anything
	 */
	public boolean canUndo() {
		return cursor > 0;
	}
	/**
	 * Redo one action. Moves the cursor ahead one.
	 */
	public void redo() {
		if (canRedo()) {
			moveCursorTo(cursor + 1);
		}
	}

	/**
	 * Tests to see if any redo events are available above the stack.
	 * @return if redo will do anything
	 */
	public boolean canRedo() {
		return cursor < history.size();
	}
	/**
	 * Get the location of the history cursor. It is equal to the number of
	 * actions in the history that have been applied.
	 * 
	 * @return The action cursor location.
	 */
	public int getHistoryCursor() {
		return cursor;
	}

	/**
	 * Get the number of events currently stored in the history archive.
	 * 
	 * @return The available number of events.
	 */
	public int getHistorySize() {
		return history.size();
	}

	/**
	 * Reduce the number of events currently stored in the history archive.
	 * 
	 * @param size
	 *            the maximum number of recent events to keep around. It keeps a
	 *            window of min(size, this.size()), which goes between 0 and
	 *            size in either direction, with the preference of being size/2
	 *            in either direction. This assures that multiple calls to
	 *            setHistorySize(C) for some constant C don't reduce the size of
	 *            the stack when mixed with undos and redos.
	 */
	public void setHistorySize(int size) {
		if (history.size() > size) {
			int oldSize = history.size();
			int removedCount = oldSize - size;

			if (size == 0) {
				lastSavePoint = 0;
			} else if (lastSavePoint < size) {
				lastSavePoint = -1;
			}
			int above = oldSize - cursor;
			// above: number of events in the future.
			int below = oldSize - above;
			// below: events in the past (beneath the cursor)

			if (above < size / 2) {
				// not enough elements in the future, so keep buffer to the past
				// If all elements are removed, this is the path taken
				history.subList(0, removedCount).clear();
				getModel().fireTableRowsDeleted(0, removedCount);
				cursor -= removedCount;
				lastSavePoint = Math.max(-1, lastSavePoint - removedCount);
			} else if (below < size / 2) {
				// Don't erase any past elements
				history.subList(size, oldSize).clear();
				getModel().fireTableRowsDeleted(size, oldSize);
			} else {
				// remove all but size/2 in each direction.
				int removeBelow = cursor - size / 2;
				history.subList(0, removeBelow).clear();
				history.subList(size, history.size()).clear();
				getModel().fireTableStructureChanged();

				cursor -= removeBelow;
				lastSavePoint = Math.max(-1, lastSavePoint - removeBelow);
			}
		}
	}

	private void clearToBuffer() {
		if (lastSavePoint > bufferLength && cursor > bufferLength) {
			int removedCount = Math.min(lastSavePoint, cursor) - bufferLength;
			history.subList(0, removedCount).clear();
			getModel().fireTableRowsDeleted(0, removedCount);
			cursor -= removedCount;
			lastSavePoint = Math.max(-1, lastSavePoint - removedCount);
		}
	}

	/**
	 * Tests to see if the cursor is not at the last save point. This allows the
	 * user to undo so that the 'file changed' status can be updated properly.
	 * 
	 * @return <code>true</code> when the cursor is not at the last save point
	 */
	public boolean hasChanged() {
		return getHistoryCursor() != lastSavePoint;
	}

	/**
	 * Mark the cursor location as the last saved point and clear the undo
	 * buffer.
	 */
	public void markSavedNow() {
		lastSavePoint = getHistoryCursor();
		clearToBuffer();
	}

	private EventListenerList historyListeners = new EventListenerList();

	/**
	 * Add a listener for history change events. These events occur when an
	 * event is added to the history list.
	 * 
	 * @param cl
	 *            The listener to add
	 */
	public void addHistoryChangeListener(ChangeListener cl) {
		historyListeners.add(ChangeListener.class, cl);
	}
	/**
	 * Gets a list of all current listeners.
	 * 
	 * @return All listeners attached for history change events.
	 */
	public ChangeListener[] getHistoryChangeListeners() {
		return (ChangeListener[]) historyListeners
				.getListeners(ChangeListener.class);
	}
	/**
	 * Remove the given change listener, if it is currently attached.
	 * 
	 * @param cl
	 *            the listener to remove
	 */
	public void removeHistoryChangeListener(ChangeListener cl) {
		historyListeners.remove(ChangeListener.class, cl);
	}

	/**
	 * Fire a history change event to all attached listeners.
	 */
	public void fireHistoryChangeEvent() {
		Object[] L = historyListeners.getListenerList();
		ChangeEvent e = null;
		for (int i = L.length - 2; i >= 0; i -= 2) {
			if (L[i] == ChangeListener.class) {
				if (e == null) {
					e = new ChangeEvent(this);
				}
				((ChangeListener) L[i + 1]).stateChanged(e);
			}
		}
	}
	/**
	 * Gets how many items will be kept before the last save point.
	 * 
	 * @return
	 */
	public int getBufferLength() {
		return bufferLength;
	}

	/**
	 * Sets the number of items that should be saved before the last save point.
	 * 
	 * @param i
	 */
	public void setBufferLength(int i) {
		bufferLength = i;
		clearToBuffer();
	}
}