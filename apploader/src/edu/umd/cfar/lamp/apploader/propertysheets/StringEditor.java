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

import java.util.*;

import javax.swing.*;
import javax.swing.event.*;
import javax.swing.text.*;

/**
 * A swing cell editor for Strings.
 */
public class StringEditor extends JTextField implements CellEditor {
	protected CellEditor proxy = new AbstractCellEditor() {
		public Object getCellEditorValue() {
			try {
				String value = getDocument().getText(0, getDocument().getLength());
				return value;
			} catch (BadLocationException blx) {
				throw new IllegalArgumentException();
			}
		}
	};
	
	/**
	 * {@inheritDoc}
	 */
	public void cancelCellEditing() {
		proxy.cancelCellEditing();
	}
	/**
	 * {@inheritDoc}
	 */
	public boolean stopCellEditing() {
		return proxy.stopCellEditing();
	}
	/**
	 * {@inheritDoc}
	 */
	public Object getCellEditorValue() {
		return proxy.getCellEditorValue();
	}
	/**
	 * {@inheritDoc}
	 */
	public boolean isCellEditable(EventObject e) {
		return proxy.isCellEditable(e);
	}
	/**
	 * {@inheritDoc}
	 */
	public boolean shouldSelectCell(EventObject e) {
		return proxy.shouldSelectCell(e);
	}
	/**
	 * {@inheritDoc}
	 */
	public void addCellEditorListener(CellEditorListener e) {
		proxy.addCellEditorListener(e);
	}
	/**
	 * {@inheritDoc}
	 */
	public void removeCellEditorListener(CellEditorListener e) {
		proxy.removeCellEditorListener(e);
	}
}
