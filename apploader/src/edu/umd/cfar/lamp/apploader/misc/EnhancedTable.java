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
import java.awt.event.*;
import java.util.*;

import javax.swing.*;
import javax.swing.event.*;
import javax.swing.table.*;

import edu.umd.cfar.lamp.viper.util.*;

/**
 * Provides a JTable with some higher-level functionality than the standard
 * Swing version. This includes listeners for each cell (instead of having to
 * add a mouse listener and then check what cell it is over).
 */
public class EnhancedTable extends JTable implements ListenableTable {
	private ListenableTableAdapter ltaDelegate;
	private int minimumNaturalColumnWidth = 12;
	private int minimumNaturalRowHeight = 16;
	private int columnMargin = 8;
	private Map lastUserSetWidthForColumn;
	private Color cellSelectionBackground;
	private Color cellSelectionForeground;

	/**
	 * Create a new, empty table.
	 */
	public EnhancedTable() {
		super();
		commonInit();
	}

	/**
	 * Create a new table with the given number of columns and rows.
	 * 
	 * @param numRows
	 *            The height of the table (in cells)
	 * @param numColumns
	 *            The width of the table (in cells)
	 */
	public EnhancedTable(int numRows, int numColumns) {
		super(numRows, numColumns);
		commonInit();
	}

	/**
	 * Creates a new table from the given table model.
	 * 
	 * @param dm
	 *            The data model
	 */
	public EnhancedTable(TableModel dm) {
		super(dm);
		commonInit();
	}

	/**
	 * Creates a new table using the default table model cell renderers, using
	 * the given array to populate the model.
	 * 
	 * @param rowData
	 *            The data items
	 * @param columnNames
	 *            The titles for the columns
	 */
	public EnhancedTable(Object[][] rowData, Object[] columnNames) {
		super(rowData, columnNames);
		commonInit();
	}

	/**
	 * Creates a new default table model for a new table using the given
	 * vector-of-vectors for the data and the vector-of-titles for the titles.
	 * 
	 * @param rowData
	 *            The data, rowwise.
	 * @param columnNames
	 *            The names for each column
	 */
	public EnhancedTable(Vector rowData, Vector columnNames) {
		super(rowData, columnNames);
		commonInit();
	}

	/**
	 * Create a new table with the given data model and title data model.
	 * 
	 * @param dm
	 *            The data model for the table.
	 * @param cm
	 *            The model for the columns
	 */
	public EnhancedTable(TableModel dm, TableColumnModel cm) {
		super(dm, cm);
		commonInit();
	}

	/**
	 * Create a new table with the given table data, column, and selection
	 * models.
	 * 
	 * @param dm
	 *            The data model
	 * @param cm
	 *            The column model
	 * @param sm
	 *            The selection model
	 */
	public EnhancedTable(
		TableModel dm,
		TableColumnModel cm,
		ListSelectionModel sm) {
		super(dm, cm, sm);
		commonInit();
	}

	private void commonInit() {
		ltaDelegate = new ListenableTableAdapter();
		super.getTableHeader().addMouseListener(new VHeaderMouseListener());
		super.addMouseListener(new VTableMouseListener());
		lastUserSetWidthForColumn = new WeakHashMap();
		resizeAllColumnsToNaturalWidth();
	}

	private class VHeaderMouseListener extends MouseAdapter {
		/**
		 * @see java.awt.event.MouseAdapter#mouseClicked(java.awt.event.MouseEvent)
		 */
		public void mouseClicked(MouseEvent e) {
			Point p1 = e.getPoint();
			int col = getTableHeader().columnAtPoint(p1);
			int dir = 0;

			Point p2 = new Point(p1.x + columnMargin, p1.y);
			boolean east = getTableHeader().columnAtPoint(p2) != col;
			dir = dir | (east ? TableEvent.EAST : TableEvent.CENTER);

			p2 = new Point(p1.x - columnMargin, p1.y);
			boolean west = getTableHeader().columnAtPoint(p2) != col;
			dir = dir | (west ? TableEvent.WEST : TableEvent.CENTER);

			p2 = new Point(p1.x, p1.y + columnMargin);
			boolean south = !getTableHeader().contains(p2);
			dir = dir | (south ? TableEvent.SOUTH : TableEvent.CENTER);

			int row = -1;
			if (e.getClickCount() == 1) {
				if (e.getButton() == MouseEvent.BUTTON1) {
					ltaDelegate.fireClick(EnhancedTable.this, row, col, dir);
				} else if (e.getButton() == MouseEvent.BUTTON2) {
					ltaDelegate.fireContextClick(
						EnhancedTable.this,
						row,
						col,
						dir);
				} else if (e.getButton() == MouseEvent.BUTTON3) {
					ltaDelegate.fireAltClick(EnhancedTable.this, row, col, dir);
				}
			} else if (e.getClickCount() == 2) {
				if (e.getButton() == MouseEvent.BUTTON1) {
					ltaDelegate.fireActionClick(
						EnhancedTable.this,
						row,
						col,
						dir);
					if (east) {
						resizeColumnToNaturalWidth(col);
					}
				}
			}
		}
	}

	/**
	 * Converts mouse click events into table events.
	 */
	private class VTableMouseListener extends MouseAdapter {
		/**
		 * @see java.awt.event.MouseAdapter#mouseClicked(java.awt.event.MouseEvent)
		 */
		public void mouseClicked(MouseEvent e) {
			int row = EnhancedTable.this.rowAtPoint(e.getPoint());
			int col = EnhancedTable.this.columnAtPoint(e.getPoint());
			if (e.getClickCount() == 1) {
				if (e.getButton() == MouseEvent.BUTTON1) {
					ltaDelegate.fireClick(
						EnhancedTable.this,
						row,
						col,
						TableEvent.CENTER);
				} else if (e.getButton() == MouseEvent.BUTTON2) {
					ltaDelegate.fireContextClick(
						EnhancedTable.this,
						row,
						col,
						TableEvent.CENTER);
				} else if (e.getButton() == MouseEvent.BUTTON3) {
					ltaDelegate.fireAltClick(
						EnhancedTable.this,
						row,
						col,
						TableEvent.CENTER);
				}
			} else if (e.getClickCount() == 2) {
				if (e.getButton() == MouseEvent.BUTTON1) {
					ltaDelegate.fireActionClick(
						EnhancedTable.this,
						row,
						col,
						TableEvent.CENTER);
				}
			}
		}
	}

	/**
	 * Add a TableListener
	 * 
	 * @param tl
	 *            the new listener to add.
	 */
	public void addTableListener(TableListener tl) {
		ltaDelegate.addTableListener(tl);
	}

	/**
	 * Gets all table listeners attached to the table.
	 * 
	 * @return Array of the listeners
	 */
	public TableListener[] getTableListeners() {
		return ltaDelegate.getTableListeners();
	}

	/**
	 * Removes the given table listener, if it is among the ones attached.
	 * 
	 * @param tl
	 *            The listener to remove.
	 */
	public void removeTableListener(TableListener tl) {
		ltaDelegate.removeTableListener(tl);
	}

	/**
	 * Resizes all columns to the preferred width of their contents.
	 * This involves enumerating all cells in the table and checking
	 * their preferred sizes.
	 */
	public void resizeAllColumnsToNaturalWidth() {
		for (int i = 0; i < this.getColumnCount(); i++) {
			resizeColumnToNaturalWidth(i);
		}
	}
	
	/**
	 * Sets the row heights to be the maximum preferred height
	 * of all cells.
	 * This involves enumerating all cells in the table and checking
	 * their preferred sizes.
	 */
	public void setRowHeightToMaximumPreferredHeight () {
		int prefHeight = super.getRowMargin() * 2;
		for (int i = 0; i < this.getRowCount(); i++) {
			prefHeight = Math.max(prefHeight, getNaturalHeightForRow(i));
		}
		this.setRowHeight(prefHeight);
	}

	/**
	 * Set the row heights to the maximum preferred height of each 
	 * row. This may result in heterogenous row sizes, which may seem odd.
	 * For homogenous row resizing, use 
	 * {@link #setRowHeightToMaximumPreferredHeight()}.
	 * This involves enumerating all cells in the table and checking
	 * their preferred sizes.
	 */
	public void resizeAllRowsToNaturalHeight() {
		for (int i = 0; i < this.getRowCount(); i++) {
			resizeRowToNaturalHeight(i);
		}
	}

	private int getNaturalHeightForRow(int row) {
		int prefHeight = minimumNaturalRowHeight;
		TableModel model = getModel();
		if (row == -1) {
			throw new UnsupportedOperationException("Not yet implemented for header");
		}
		for (int col = 0; col < model.getColumnCount(); col++) {
			TableCellRenderer renderer = getCellRenderer(row, col);
			int modelCol = convertColumnIndexToModel(col);
			Object value = model.getValueAt(row, modelCol);
			Component comp =
				renderer.getTableCellRendererComponent(
						this,
						value,
						false,
						false,
						row,
						col);
			int compHeight = comp.getPreferredSize().height;
			prefHeight = Math.max(prefHeight, compHeight);
			if (isCellEditable(row, col)) {
				TableCellEditor editor = getCellEditor(row, col);
				comp = editor.getTableCellEditorComponent(this, value, false, row, col);
				compHeight = comp.getPreferredSize().height;
				prefHeight = Math.max(prefHeight, compHeight);
			}
		}
		return prefHeight + rowMargin;
	}

	private int getNaturalWidthForColumn(TableColumn column) {
		int prefWidth = minimumNaturalColumnWidth;
		TableModel model = getModel();
		TableCellRenderer headerRenderer =
			getTableHeader().getDefaultRenderer();

		int modelCol = column.getModelIndex();

		Component comp =
			headerRenderer.getTableCellRendererComponent(
					null,
					column.getHeaderValue(),
					false,
					false,
					0,
					0);
		prefWidth = comp.getPreferredSize().width;

		for (int row = 0; row < model.getRowCount(); row++) {
			int viewCol = convertColumnIndexToView(modelCol);
			TableCellRenderer renderer = getCellRenderer(row, viewCol);
			Object value = model.getValueAt(row, modelCol);
			comp =
				renderer.getTableCellRendererComponent(
						this,
						value,
						false,
						false,
						row,
						viewCol);
			int compWidth = comp.getPreferredSize().width;
			prefWidth = Math.max(prefWidth, compWidth);
			// XXX Should also take into account editor size, but this is too slow, for some reason.
//			if (isCellEditable(row, viewCol)) {
//				TableCellEditor editor = getCellEditor(row, viewCol);
//				comp = editor.getTableCellEditorComponent(this, value, false, row, viewCol);
//				compWidth = comp.getPreferredSize().width;
//				prefWidth = Math.max(prefWidth, compWidth);
//			}
		}
		return prefWidth + columnMargin;
	}
	
	/**
	 * Resizes the given column to the maximum preferred
	 * width of its cells.
	 * @param col the column to resize
	 */
	public void resizeColumnToNaturalWidth(int col) {
		TableColumn column = getColumnModel().getColumn(col);
		int prefWidth = getNaturalWidthForColumn(column);
		column.setPreferredWidth(prefWidth);
		lastUserSetWidthForColumn.put(column, new Integer(prefWidth));
	}

	/**
	 * Resizes the given row to the maximum preferred height
	 * of its contents.
	 * @param row the row to resize
	 */
	public void resizeRowToNaturalHeight(int row) {
		int prefHeight = getNaturalHeightForRow(row);
		setRowHeight(row, prefHeight);
	}

	/**
	 * Gets the minimum width that each column will be 
	 * allowed to auto-resize to. 
	 * @return the minimum width auto-resize allows
	 */
	public int getMinimumNaturalColumnWidth() {
		return minimumNaturalColumnWidth;
	}

	/**
	 * Sets the minimum allowed width for auto-resize.
	 * Note that this only takes effect when 
	 * resizeColumnToNaturalWidth is invoked.
	 * @param i the new minimum allowed width
	 */
	public void setMinimumNaturalColumnWidth(int i) {
		minimumNaturalColumnWidth = i;
	}
	
	/**
	 * Gets the description of the current set of widths, and
	 * how they were set. This is useful for serializing 
	 * the current state.
	 * @return the widths of the columns
	 */
	public String getColumnWidthDescription() {
		Enumeration cols = getColumnModel().getColumns();
		StringBuffer output = new StringBuffer();
		while (cols.hasMoreElements()) {
			TableColumn tc = (TableColumn) cols.nextElement();
			ColumnDescription cdesc = new ColumnDescription(tc);
			cdesc.appendTo(output);
			output.append(" ");
		}
		return output.toString();
	}
	private class ColumnDescription {
		Class colClass;
		int modelIndex;
		int prefWidth;
		String name;
		ColumnDescription() {
		}
		ColumnDescription(TableColumn c) {
			this.modelIndex = c.getModelIndex();
			this.colClass = dataModel.getColumnClass(this.modelIndex);
			this.prefWidth = c.getPreferredWidth();
			if (lastUserSetWidthForColumn.containsKey(c)) {
				Integer userPrefWidth =
					(Integer) lastUserSetWidthForColumn.get(c);
				this.prefWidth = userPrefWidth.intValue();
			}
			this.name = (String) c.getHeaderValue();
		}
		
		/**
		 * Gets the string version of this description.
		 * @return a machine and human readable description
		 * of a table's column sizes 
		 */
		public String toString() {
			return appendTo(new StringBuffer()).toString();
		}
		StringBuffer appendTo(StringBuffer b) {
			String printClass = StringHelp.backslashify(colClass.getName());
			String printName = StringHelp.backslashify(name);

			b
				.append("col ")
				.append(modelIndex)
				.append(" \"")
				.append(printClass)
				.append("\" \"")
				.append(printName)
				.append("\" ")
				.append(prefWidth);
			return b;
		}
		/**
		 * Parses a table column width description.
		 * @param s the description to parse
		 */
		public void parseInFrom(String s) {
			parseInFrom(StringHelp.debackslashedTokenizer(s));
		}
		void parseInFrom(Iterator iter) {
			String colKeyword = (String) iter.next();
			String modelIndex = (String) iter.next();
			String className = (String) iter.next();
			String columnName = (String) iter.next();
			String columnWidth = (String) iter.next();

			assert "col".equals(colKeyword);
			try {
				this.colClass =
					ClassLoader.getSystemClassLoader().loadClass(className);
			} catch (ClassNotFoundException e) {
				throw new IllegalArgumentException(
					"Not a valid class for the column: " + className);
			}
			this.modelIndex = Integer.parseInt(modelIndex);
			this.prefWidth = Integer.parseInt(columnWidth);
			this.name = columnName;
		}
		/**
		 * Applies the description to the current table.
		 * @return
		 */
		public int applyThis() {
			int colIndex = columnModel.getColumnIndex(name);
			if (colIndex >= 0) {
				columnModel.getColumn(colIndex).setPreferredWidth(prefWidth);
			}
			return colIndex;
		}
	}
	
	/**
	 * Deserialize the column widths.
	 * @param description the widths
	 */
	public void setColumnWidthsFromDescription(String description) {
		Iterator st = StringHelp.debackslashedTokenizer(description);
		TableColumnModel tcm = getColumnModel();
		ColumnDescription cd = new ColumnDescription();
		int placedOrder = 0;
		while (st.hasNext()) {
			try {
				cd.parseInFrom(st);
				int colIndex = cd.applyThis();
				if (colIndex >= 0) {
					tcm.moveColumn(colIndex, placedOrder++);
				}
			} catch (IllegalArgumentException iax) {
				//
			}
		}
	}
	private JViewport viewport = null;
	private TableColumn getLastColumn() {
		return getColumnModel().getColumn(
				getColumnModel().getColumnCount() - 1);
		
	}
	private ChangeListener viewChangeListener = new ChangeListener() {
		public void stateChanged(ChangeEvent e) {
			TableColumn resizingColumn = getTableHeader().getResizingColumn();
			if (resizingColumn != null) {
//				TableColumn lastColumn = getLastColumn();
//				if (resizingColumn.equals(lastColumn)) {
//					// the user is resizing the last column
//					return; 
//				}
//				int prefWidthForCol = lastColumn.getPreferredWidth();
//				int storedPreferredWidth = 0;
//				if (lastUserSetWidthForColumn.containsKey(lastColumn)) {
//					Integer i = (Integer) lastUserSetWidthForColumn.get(lastColumn);
//					storedPreferredWidth = i.intValue();
//				} else {
//					storedPreferredWidth = getNaturalWidthForColumn(lastColumn);
//				}
//				int delta = viewport.getWidth() - getSize().width;
//				if (storedPreferredWidth < prefWidthForCol) {
//					// last column is bigger than it should be
//					lastColumn.setPreferredWidth(Math.min(storedPreferredWidth, prefWidthForCol+delta));
//				} else if (delta != 0) {
//					// last column is smaller than it should be 
//					lastColumn.setPreferredWidth(lastColumn.getPreferredWidth()+delta);
//				}
				return;
			}
			Enumeration cols = getColumnModel().getColumns();
			int remainingSize = viewport.getWidth();
			lastFullViewportSize = remainingSize;
			while (cols.hasMoreElements()) {
				TableColumn col = (TableColumn) cols.nextElement();
				int prefWidthForCol = col.getPreferredWidth();
				if (lastUserSetWidthForColumn.containsKey(col)) {
					Integer i = (Integer) lastUserSetWidthForColumn.get(col);
					prefWidthForCol = i.intValue();
				} else {
					prefWidthForCol = getNaturalWidthForColumn(col);
				}
				if (!cols.hasMoreElements()) {
					lastColumnWidth = prefWidthForCol;
					prefWidthForCol = Math.max(lastColumnWidth, remainingSize);
					col.setPreferredWidth(prefWidthForCol);
				}
				col.setPreferredWidth(prefWidthForCol);
				remainingSize -= prefWidthForCol;
			}
		}
	};
	/**
	 * This is a total hack to handle scroll panes.
	 * 
	 * @see java.awt.Component#addComponentListener(java.awt.event.ComponentListener)
	 */
	public synchronized void addComponentListener(ComponentListener l) {
		if (viewport != null) {
			viewport.removeChangeListener(viewChangeListener);
		}
		super.addComponentListener(l);
		if (this.getParent() instanceof JViewport) {
			viewport = (JViewport) this.getParent();
			viewport.addChangeListener(viewChangeListener);
		}
	}
	
	/**
	 * @see java.awt.Component#removeComponentListener(java.awt.event.ComponentListener)
	 */
	public synchronized void removeComponentListener(ComponentListener l) {
		super.removeComponentListener(l);
		if (viewport != null) {
			viewport.removeChangeListener(viewChangeListener);
		}
	}

	private int lastFullViewportSize;
	private int lastColumnWidth;
	
	/**
	 * @see javax.swing.JTable#doLayout()
	 */
	public void doLayout() {
		// this is called when the box size changes, but not
		// when the scrollpane size changes
		super.doLayout();
		TableColumn lastColumn = getLastColumn();
		TableColumn resizingColumn = getTableHeader().getResizingColumn();
		if (resizingColumn == null) { 
			// check to see if the last column needs to be shrunk
		} else {
			// save the resize size as the column preferred size
			Integer rWidth = new Integer(resizingColumn.getWidth());
			lastUserSetWidthForColumn.put(resizingColumn, rWidth);
			if (!resizingColumn.equals(lastColumn)) {
				int delta = lastFullViewportSize - viewport.getWidth();
				if (delta < 0) {
					// 
				}
			}
		}
	}
	/**
	 * @return Returns the cellSelectionBackground.
	 */
	public Color getCellSelectionBackground() {
		if (cellSelectionBackground == null) {
			return getBackground();
		} else {
			return cellSelectionBackground;
		}
	}
	/**
	 * @param cellSelectionBackground The cellSelectionBackground to set.
	 */
	public void setCellSelectionBackground(Color cellSelectionBackground) {
		this.cellSelectionBackground = cellSelectionBackground;
	}
	/**
	 * @return Returns the cellSelectionForeground.
	 */
	public Color getCellSelectionForeground() {
		if (cellSelectionForeground == null) {
			return getForeground();
		} else {
			return cellSelectionForeground;
		}
	}
	/**
	 * @param cellSelectionForeground The cellSelectionForeground to set.
	 */
	public void setCellSelectionForeground(Color cellSelectionForeground) {
		this.cellSelectionForeground = cellSelectionForeground;
	}
}
