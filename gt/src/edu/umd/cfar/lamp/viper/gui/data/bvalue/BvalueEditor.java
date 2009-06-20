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

package edu.umd.cfar.lamp.viper.gui.data.bvalue;

import java.util.*;

import javax.swing.*;
import javax.swing.event.*;

import viper.api.*;
import edu.umd.cfar.lamp.viper.gui.core.*;
import edu.umd.cfar.lamp.viper.gui.data.*;
import edu.umd.cfar.lamp.viper.gui.table.*;

/**
 * @author davidm@cfar.umd.edu
 * @since Jul 8, 2003
 */
public class BvalueEditor extends JComboBox implements CellEditor, DataEditor{
	private AttributeValueEditorAdapter proxy = new AttributeValueEditorAdapter() {
		public Object getCellEditorValue() {
			return getSelectedItem();
		}
	};

	public BvalueEditor() {
		super();
		super.setModel(new BvalueSelectionModel());
	}
	
	private class BvalueSelectionModel extends AbstractListModel implements ComboBoxModel {
		public Object getSelectedItem() {
			return proxy.getAttributeValue();
		}

		public void setSelectedItem(Object anItem) {
			proxy.setAttributeValue(anItem);
		}

		public int getSize() {
			return 3;
		}
		
		public final Object[] vals = new Object[] { null, Boolean.TRUE, Boolean.FALSE };
		
		public Object getElementAt(int index) {
			return vals[index];
		}
	}

	public void setNode(Node n) {
		proxy.setNode(n);
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
		setSelectedItem(typename);
	}
	public void setAttrType(AttrConfig cfg) {
		setAttrType(cfg.getAttrType());
	}
	/**
	 * @return
	 */
	public ViperViewMediator getMediator() {
		return proxy.getMediator();
	}

	/**
	 * @return
	 */
	public Node getNode() {
		return proxy.getNode();
	}

	/**
	 * @param mediator
	 */
	public void setMediator(ViperViewMediator mediator) {
		proxy.setMediator(mediator);
	}
}
