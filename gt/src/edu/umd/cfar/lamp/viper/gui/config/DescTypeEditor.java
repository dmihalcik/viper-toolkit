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
import viper.api.impl.*;

/**
 * Cell editor for the 'Type' property of descriptor configuration objects.
 */
public class DescTypeEditor extends JComboBox implements CellEditor {
	private CellEditor proxy = new AbstractCellEditor() {
		public Object getCellEditorValue() {
			String val = (String) getSelectedItem();
			return codes[Util.getDescType(val)];
		}
	};
	private static Integer[] codes;
	private static String[] types;
	private static int max(int[] ints) {
		int m = ints[0];
		for(int i = 1; i < ints.length; i++) {
			m = (m < ints[i]) ? ints[i] : m;
		}
		return m;
	}
	static {
		int[] intCodes = new int[]{Config.FILE, Config.CONTENT, Config.OBJECT};
		int max = max(intCodes);
		codes = new Integer[max+1];
		types = new String[intCodes.length];
		for (int i = 0; i < intCodes.length; i++) {
			codes[intCodes[i]] = new Integer(intCodes[i]);
			types[i] = Util.getDescType(intCodes[i]);
		}
	}
	public DescTypeEditor() {
		super(DescTypeEditor.types);
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
	
	public void setDescType(int x) {
		setSelectedItem(Util.getDescType(x));
	}
	public void setDescType(Config cfg) {
		setDescType(cfg.getDescType());
	}
}
