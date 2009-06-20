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

package edu.umd.cfar.lamp.viper.gui.table;

import java.awt.*;
import java.awt.event.*;
import java.util.*;

import javax.swing.*;
import javax.swing.table.*;

import viper.api.*;
import edu.umd.cfar.lamp.apploader.prefs.*;
import edu.umd.cfar.lamp.viper.gui.core.*;
import edu.umd.cfar.lamp.viper.gui.data.*;

/**
 * @author davidm@cfar.umd.edu
 * @since Jun 5, 2003
 */
public class AttributeEditor extends AbstractCellEditor implements TableCellEditor  {
	private HasMediator parentTable = null;
	private Map cachedFactories = new HashMap();
	private DataEditor currentEditor = null;
	private int editClickCount = 1;

	boolean drawUpdate = false;
	public AttributeEditor(HasMediator table) {
		parentTable = table;
	}

	public Component getTableCellEditorComponent(
		final JTable table,
		Object value,
		boolean isSelected,
		int row,
		int column) {
		Attribute currentAttribute = (Attribute) value;
		AttrConfig acfg = currentAttribute.getAttrConfig();
		PrefsManager prefs = parentTable.getMediator().getPrefs();
		AttrValueEditorFactory avef;
		if (cachedFactories.containsKey(acfg)) {
			avef = (AttrValueEditorFactory) cachedFactories.get(acfg);
		} else {
			avef = new AttrValueEditorFactory();
			avef.setPrefs(prefs);
			avef.setAttributeConfig(acfg);
			cachedFactories.put(acfg, avef);
		}

		currentEditor = (DataEditor) avef.getAttrValueEditor();
		currentEditor.setMediator(parentTable.getMediator());
		currentEditor.setNode(currentAttribute);
		
		// FIXME: Possible memory leak?
		// I'm pretty sure this loop below fixes it, but it seems like a hack.
		// however, developing with the awt, lots of things I consider hacks are actually
		// 'patterns'
		FocusListener[] fls = ((Component) currentEditor).getFocusListeners();
		for (int i = 0; i < fls.length; i++) {
			if (fls[i] instanceof LostFocusListener) {
				((Component) currentEditor).removeFocusListener(fls[i]);
			}
		}
		((Component) currentEditor).addFocusListener(new LostFocusListener(table));
		return (Component) currentEditor;
	}
	
	private static class LostFocusListener extends FocusAdapter {
		private JTable table;
		public LostFocusListener(JTable t) {
			table = t;
		}
		public void focusLost(FocusEvent e) {
			table.removeEditor();
		}
	}

	public Object getCellEditorValue() {
		if (currentEditor != null) {
			return currentEditor.getCellEditorValue();
		}
		return null;
	}

	public boolean isCellEditable(EventObject e) {
		boolean ed = super.isCellEditable(e);
		if (ed) {
			if (e instanceof MouseEvent) {
				return ((MouseEvent) e).getClickCount() >= editClickCount;
			} else {
				return true;
			}
		} else {
			return false;
		}
	}

	/**
	 * @return int
	 */
	public int getEditClickCount() {
		return editClickCount;
	}

	/**
	 * Sets the editClickCount.
	 * @param editClickCount The editClickCount to set
	 */
	public void setEditClickCount(int editClickCount) {
		this.editClickCount = editClickCount;
	}

}