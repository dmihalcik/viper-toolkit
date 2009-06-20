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

package edu.umd.cfar.lamp.viper.examples.textline;

import java.util.*;

import javax.swing.*;
import javax.swing.event.*;

import viper.api.*;
import viper.api.datatypes.*;

/**
 * Created on Apr 22, 2005
 * 
 * @author spikes51@umiacs.umd.edu
 */

// TODO: create the static svalue if a non-existing name is entered

public class TextLinkEditor extends JComboBox implements CellEditor {
	private AttrConfig lastAC;

	protected CellEditor proxy = new AbstractCellEditor() {
		public Object getCellEditorValue() {
			return getSelectedItem();
		}
	};

	public TextLinkEditor() {
		super();
		setEditable(true);
	}

	public void setNode(Node n) {
		if (n instanceof AttrConfig) {
			lastAC = (AttrConfig) n;
		} else {
			lastAC = ((Attribute) n).getAttrConfig();
		}

		// build the list of all svalues in our current descriptor
		Vector list = new Vector();
		Config conf = (Config) lastAC.getParent();
		for (int i = 0; i < conf.getNumberOfChildren(); i++) {
			AttrConfig curr = (AttrConfig) conf.getChild(i);
			if (curr.getAttrType().equals(ViperDataFactoryImpl.SVALUE)) {
				list.add(curr.getAttrName());
			}
		}
		// make the dropdown menu
		Object o = this.getSelectedItem();
		setModel(new DefaultComboBoxModel(list));
		if(o != null) setSelectedItem(o);
	}

	public Object getCellEditorValue() {
		Config conf = (Config) lastAC.getParent();
		return conf.getAttrConfig((String) proxy.getCellEditorValue());
	}

	/**
	 * @param l
	 */
	public void addCellEditorListener(CellEditorListener l) {
		proxy.addCellEditorListener(l);
	}

	/**
	 *  
	 */
	public void cancelCellEditing() {
		proxy.cancelCellEditing();
	}

	/**
	 * @param anEvent
	 * @return
	 */
	public boolean isCellEditable(EventObject anEvent) {
		return proxy.isCellEditable(anEvent);
	}

	/**
	 * @param l
	 */
	public void removeCellEditorListener(CellEditorListener l) {
		proxy.removeCellEditorListener(l);
	}

	/**
	 * @param anEvent
	 * @return
	 */
	public boolean shouldSelectCell(EventObject anEvent) {
		return proxy.shouldSelectCell(anEvent);
	}

	/**
	 * @return
	 */
	public boolean stopCellEditing() {
		return proxy.stopCellEditing();
	}
	/**
	 * @param comboBox
	 */
}