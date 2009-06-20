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

package edu.umd.cfar.lamp.apploader.propertysheets;

import java.awt.*;
import java.util.*;
import java.util.logging.*;

import javax.swing.*;
import javax.swing.event.*;
import javax.swing.table.*;

import edu.umd.cfar.lamp.apploader.misc.*;
import edu.umd.cfar.lamp.apploader.prefs.*;

/**
 * A property sheet, allowing the user to edit different 
 * properties. The default implementation uses the javabean
 * standard naming conventions to extract property names and
 * types. The user/system can override this with the application
 * preferences.
 */
public class PropertySheet extends JScrollPane {
	private DescriberBasedProperties props;
	private Logger logger = Logger.getLogger("edu.umd.cfar.lamp.apploader.propertysheets");

	/**
	 * Gets the data model associated with the property sheet.
	 * @return the table model
	 */
	private MyTableModel getTableModel() {
		return (MyTableModel) getTable().getModel();
	}
	
	/**
	 * Gets the table contained within the property sheet.
	 * I'm not sure that this is the best way to present properties,
	 * and may stop using tables in the future.
	 * @return the table
	 */
	public EnhancedTable getTable() {
		return (EnhancedTable) getViewport().getView();

	}
	private class MyTableModel extends AbstractTableModel {
		/**
		 * @return 2
		 */
		public int getColumnCount() {
			return 2;
		}
		/**
		 * @return the number of properties
		 */
		public int getRowCount() {
			return props.size();
		}
		/**
		 * Gets the property descriptor for the row.
		 * @param r the row number (0-indexed)
		 * @return the corresponding property
		 */
		public InstancePropertyDescriptor getRow(int r) {
			return (InstancePropertyDescriptor) props.get(r);
		}
		
		/**
		 * Determines if the value at a cell is editable
		 * @param rowIndex the property
		 * @param columnIndex whether the property name (column zero)
		 * or property value (column one)
		 * @return true, if the corresponding property is settable
		 * and the column index is 1
		 */
		public boolean isCellEditable(int rowIndex, int columnIndex) {
			InstancePropertyDescriptor row = getRow(rowIndex);
			if (columnIndex == 0) {
				return false;
			} else if (columnIndex == 1) {
				return row.isSettable(props.getObject());
			} else {
				throw new IndexOutOfBoundsException("Not a valid cell: " + rowIndex + "x" + columnIndex);
			}
		}
		
		/**
		 * Gets the appropriate property name or property value.
		 * @param rowIndex the row corresponding to a property
		 * @param columnIndex either the name column (zero) or 
		 * value column (one)
		 * @return the name or value of the appropriate property
		 */
		public Object getValueAt(int rowIndex, int columnIndex) {
			InstancePropertyDescriptor row = getRow(rowIndex);
			if (columnIndex == 0) {
				return row.getName();
			} else if (columnIndex == 1) {
				return row;
			} else {
				throw new IndexOutOfBoundsException("Not a valid cell: " + rowIndex + "x" + columnIndex);
			}
		}

		/**
		 * Either "property" or "value". XXX: this should be localizable.
		 * @param columnIndex either the name column (zero) or 
		 * value column (one)
		 * @return "property" or "value"
		 */
		public String getColumnName(int columnIndex) {
			if (columnIndex == 0) {
				return "Property";
			} else if (columnIndex == 1) {
				return "Value";
			} else {
				throw new IndexOutOfBoundsException("Not a valid column: " + columnIndex);
			}
		}

		/**
		 * Class corresponding to the column
		 * @param columnIndex either the name column (zero) or 
		 * value column (one)
		 * @return <code>String.class</code> 
		 * or <code>{@link InstancePropertyDescriptor}.class</code>
		 */
		public Class getColumnClass(int columnIndex) {
			if (columnIndex == 0) {
				return String.class;
			} else if (columnIndex == 1) {
				return InstancePropertyDescriptor.class;
			} else {
				throw new IndexOutOfBoundsException("Not a valid column: " + columnIndex);
			}
		}

		/**
		 * Sets the appropriate property value, if possible. Otherwise,
		 * this silently fails.
		 * @param value the new value for the appropriate property
		 * @param rowIndex the row corresponding to a property
		 * @param columnIndex only the value column (column one) works
		 */
		public void setValueAt(Object value, int rowIndex, int columnIndex) {
			if (columnIndex == 1) {
				InstancePropertyDescriptor row = getRow(rowIndex);
				if (row.isSettable(props.getObject())) {
					row.applySetter(props.getObject(), value);
				}
			}
		}
		
		/**
		 * Gets the primary property table corresponding to this model.
		 * @return the table
		 */
		public EnhancedTable getTable() {
			return (EnhancedTable) getViewport().getView();
		}
	}

	/**
	 * Create a new, empty property sheet.
	 */
	public PropertySheet() {
		super(new EnhancedTable());
		this.getPreferredSize();
		props = new ForClassPropertyList();
		
		// Add bean change event listener
		props.addChangeListener(new ChangeListener(){
			public void stateChanged(ChangeEvent e) {
				// XXX Should I fire a 'stop editing' event here?
				refresh();
			}
		});

		// Add table
		JTable table = getTable();
		MyTableModel tableModel = new MyTableModel();
		table.setModel(tableModel);
		table.setDefaultRenderer(InstancePropertyDescriptor.class, new MyCellRenderer());
		table.setDefaultEditor(InstancePropertyDescriptor.class, new MyCellEditor());
	}

	private class MyCellRenderer implements TableCellRenderer {
		private JComponent lastEditor;
		/**
		 * @see javax.swing.table.TableCellRenderer#getTableCellRendererComponent(javax.swing.JTable, java.lang.Object, boolean, boolean, int, int)
		 */
		public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
			InstancePropertyDescriptor v = (InstancePropertyDescriptor) value;
			try {
				lastEditor = v.getRenderer(props.getObject(), props.getPrefs().getCore());
				return lastEditor;
			} catch (PreferenceException e) {
				e.printStackTrace();
				return null;
			}
		}
	}
	
	private class MyCellEditor extends AbstractCellEditor implements TableCellEditor {
		private CellEditor lastEditor;
		/**
		 * @see javax.swing.CellEditor#getCellEditorValue()
		 */
		public Object getCellEditorValue() {
			if (lastEditor != null) {
				return lastEditor.getCellEditorValue();
			}
			return null;
		}
		
		/**
		 * @see javax.swing.table.TableCellEditor#getTableCellEditorComponent(javax.swing.JTable, java.lang.Object, boolean, int, int)
		 */
		public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int column) {
			InstancePropertyDescriptor v = (InstancePropertyDescriptor) value;
			JComponent c;
			try {
				c = v.getEditor(props.getObject(), props.getPrefs().getCore());
				lastEditor = (CellEditor) c;
				return c;
			} catch (PreferenceException e) {
				e.printStackTrace();
				return null;
			}
		}
	}
	
	/**
	 * Gets the preference manager associated with this property 
	 * sheet.
	 * @return the preference manager
	 */
	public PrefsManager getPrefs() {
		return props.getPrefs();
	}
	
	/**
	 * Sets the preference manager associated with this property 
	 * sheet. This is necessary for localization and determining
	 * extended properties.
	 * @param manager the preference manager
	 */
	public void setPrefs(PrefsManager manager) {
		props.setPrefs(manager);
	}

	/**
	 * Sort the descriptors by their display name using the current 
	 * lexicographical ordering.
	 */
	private static class PropertySorter implements Comparator {
		/**
		 * Sort the descriptors by their display name using the current 
		 * lexicographical ordering
		 * @param a a property descriptor
		 * @param b the other ObjectPropertyDescriptor
		 * @return <code>getName().compareto(b.getName())</code>
		 */
		public int compare(Object a, Object b) {
			InstancePropertyDescriptor A = (InstancePropertyDescriptor) a;
			InstancePropertyDescriptor B = (InstancePropertyDescriptor) b;
			
			return A.getName().compareTo(B.getName());
		}
	}
	
	static final PropertySorter SORT_BY_PROPERTY_NAME = new PropertySorter();

	/**
	 * Set the subject bean to check for properties.
	 * @param o the subject bean
	 */
	public void setObject(Object o) {
		if (o != props.getObject()) {
			if (getTable().isEditing()) {
				getTable().editingStopped(new ChangeEvent(this));
			}
			getTable().editingCanceled(new ChangeEvent(this));
			props.setObject(o);
			getTableModel().fireTableStructureChanged();
			getTable().setRowHeightToMaximumPreferredHeight ();
		}
	}

	/**
	 * Gets the bound subject bean.
	 * @return the subject
	 */
	public Object getObject() {
		return props.getObject();
	}
	
	/**
	 * Refresh the properties from the bound object.
	 */
	public void refresh() {
		props.refresh();
		getTableModel().fireTableDataChanged();
	}

	/**
	 * @return
	 */
	public Logger getLogger() {
		return logger;
	}

	/**
	 * @param logger
	 */
	public void setLogger(Logger logger) {
		this.logger = logger;
	}

}
