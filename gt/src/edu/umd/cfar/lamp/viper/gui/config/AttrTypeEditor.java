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
import viper.api.extensions.*;

/**
 * Cell editor for the 'AttrType' property of attribute configuration objects.
 */
public class AttrTypeEditor extends JComboBox implements CellEditor {
	private CellEditor proxy = new AbstractCellEditor() {
		public Object getCellEditorValue() {
			SetAttrType.AttrTypePair v = new SetAttrType.AttrTypePair();
			v.type = (String) getSelectedItem();
			if (v.type.indexOf(":") == -1) {
				v.type = ViperData.ViPER_DATA_URI + v.type;
			}
			v.param = fact.getAttribute(v.type);
			return v;
		}
	};

	private Map typeMap = new HashMap();
	private ViperDataFactory fact;
	private static String[] shorten(String uri) {
		String key = uri;
		if (uri.startsWith(ViperData.ViPER_DATA_URI)) {
			key = uri.substring(ViperData.ViPER_DATA_URI.length());
		}
		return new String[] { key, uri };
	}
	public void setDataFactory(ViperDataFactory f) {
		DefaultComboBoxModel m = (DefaultComboBoxModel) getModel();
		fact = f;
		m.removeAllElements();
		TreeSet s = new TreeSet();
		for (Iterator iter = f.getTypes(); iter.hasNext(); ) {
			String uri = (String) iter.next();
			String[] entry = shorten(uri);
			typeMap.put (entry[0], entry[1]);
			s.add(entry[0]);
		}
		for (Iterator iter = s.iterator(); iter.hasNext(); ) {
			m.addElement(iter.next());
		}
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

	public void setAttrType(String typename) {
		String selected = shorten(typename)[0];
		setSelectedItem(selected);
	}
	public void setAttrType(AttrConfig cfg) {
		setAttrType(cfg.getAttrType());
	}
}
