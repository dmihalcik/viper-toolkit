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

package edu.umd.cfar.lamp.viper.gui.data.svalue;

import java.util.*;

import javax.swing.*;
import javax.swing.event.*;

import viper.api.*;
import edu.umd.cfar.lamp.viper.gui.core.*;
import edu.umd.cfar.lamp.viper.gui.data.*;
import edu.umd.cfar.lamp.viper.gui.table.*;

/**
 * @author davidm@cfar.umd.edu
 * @since Jul 7, 2003
 */
public class SvalueEditor extends JTextField implements CellEditor, DataEditor {
	private AttributeValueEditorAdapter delegate = new AttributeValueEditorAdapter() {
		public Object getCellEditorValue() {
			String s = getText();
			if (s.length() == 0) {
				s = null;
			}
			return s;
		}
	};
	
	/**
	 * @param l
	 */
	public void addCellEditorListener(CellEditorListener l) {
		delegate.addCellEditorListener(l);
	}

	/**
	 * 
	 */
	public void cancelCellEditing() {
		delegate.cancelCellEditing();
	}

	/**
	 * @return
	 */
	public CellEditorListener[] getCellEditorListeners() {
		return delegate.getCellEditorListeners();
	}

	/**
	 * @return
	 */
	public Object getCellEditorValue() {
		return delegate.getCellEditorValue();
	}

	/**
	 * @return
	 */
	public ViperViewMediator getMediator() {
		return delegate.getMediator();
	}

	/**
	 * @return
	 */
	public Node getNode() {
		return delegate.getNode();
	}

	/**
	 * Gets the hashcode of the delegate cell editor.
	 * @return the hash code of the delegate editor
	 */
	public int hashCode() {
		return delegate.hashCode();
	}

	/**
	 * @param anEvent
	 * @return
	 */
	public boolean isCellEditable(EventObject anEvent) {
		return delegate.isCellEditable(anEvent);
	}

	/**
	 * @param l
	 */
	public void removeCellEditorListener(CellEditorListener l) {
		delegate.removeCellEditorListener(l);
	}

	/**
	 * @param mediator
	 */
	public void setMediator(ViperViewMediator mediator) {
		delegate.setMediator(mediator);
	}

	/**
	 * @param node
	 */
	public void setNode(Node node) {
		delegate.setNode(node);
		Object val = delegate.getAttributeValue();
		setText(null==val?"":val.toString());
	}

	/**
	 * @param anEvent
	 * @return
	 */
	public boolean shouldSelectCell(EventObject anEvent) {
		return delegate.shouldSelectCell(anEvent);
	}

	/**
	 * @return
	 */
	public boolean stopCellEditing() {
		return delegate.stopCellEditing();
	}

}
