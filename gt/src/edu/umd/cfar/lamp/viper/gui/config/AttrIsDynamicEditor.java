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

package edu.umd.cfar.lamp.viper.gui.config;

import java.util.*;

import javax.swing.*;
import javax.swing.event.*;

import viper.api.*;

/**
 * Cell editor for the 'isDynamic' property of attribute configuration objects.
 */
public class AttrIsDynamicEditor extends JComboBox implements CellEditor {
	private CellEditor proxy = new AbstractCellEditor() {
		public Object getCellEditorValue() {
			return Boolean.valueOf((String) getSelectedItem());
		}
	};
	
	public AttrIsDynamicEditor() {
		super();
		DefaultComboBoxModel m = (DefaultComboBoxModel) getModel();
		m.removeAllElements();
		m.addElement(Boolean.toString(true));
		m.addElement(Boolean.toString(false));
	}

	public void cancelCellEditing() {
		proxy.cancelCellEditing();
	}
	public boolean stopCellEditing() {
		return proxy.stopCellEditing();
	}
	public Object getCellEditorValue() {
		return proxy.getCellEditorValue();
	}
	public boolean isCellEditable(EventObject e) {
		return proxy.isCellEditable(e);
	}
	public boolean shouldSelectCell(EventObject e) {
		return proxy.shouldSelectCell(e);
	}
	public void addCellEditorListener(CellEditorListener e) {
		proxy.addCellEditorListener(e);
	}
	public void removeCellEditorListener(CellEditorListener e) {
		proxy.removeCellEditorListener(e);
	}

	public void setAttrConfig(AttrConfig cfg) {
		setSelectedItem(Boolean.toString(cfg.isDynamic()));
	}
}
