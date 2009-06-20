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

package edu.umd.cfar.lamp.viper.gui.data;

import java.util.*;

import javax.swing.*;
import javax.swing.event.*;

import viper.api.*;
import edu.umd.cfar.lamp.apploader.misc.*;
import edu.umd.cfar.lamp.viper.gui.core.*;
import edu.umd.cfar.lamp.viper.gui.table.*;

/**
 * @author davidm@cfar.umd.edu
 * @since Jul 8, 2003
 */
public abstract class ViperDataFsmTextEditor extends JTextField implements CellEditor, DataEditor {
	public abstract Object parse(String s);
	
	private AttributeValueEditorAdapter proxy =
		new AttributeValueEditorAdapter() {
		public Object getCellEditorValue() {
			return parse(getFSMDocument().getValidPart());
		}
	};

	public FsmDocument getFSMDocument() {
		return (FsmDocument) getDocument();
	}

	public ViperDataFsmTextEditor(StringParserFSM fsm) {
		super(new FsmDocument(), null, 0);
		getFSMDocument().setFsm(fsm);
	}

	public void setNode(Node n) {
		proxy.setNode(n);
		Object val = proxy.getAttributeValue();
		if (val != null) {
			this.setText(val.toString());
		} else {
			this.setText("");
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
