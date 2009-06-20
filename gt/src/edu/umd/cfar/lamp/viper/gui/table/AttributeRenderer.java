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
import java.util.*;

import javax.swing.*;
import javax.swing.table.*;

import viper.api.*;
import edu.umd.cfar.lamp.viper.gui.core.*;

/**
 * @author davidm@cfar.umd.edu
 * @since Jun 4, 2003
 */
public class AttributeRenderer extends JLabel implements TableCellRenderer {
	private HasMediator model;

	public AttributeRenderer(HasMediator model) {
		super();
		this.model = model;
		setOpaque(true);
	}
	public ViperViewMediator getMediator() {
		return model.getMediator();
	}
	private static String NULL_STR = "NULL";

	public void validate() {
	}
	public void revalidate() {
	}
	public void repaint(long tm, int x, int y, int w, int h) {
	}
	public void repaint(Rectangle r) {
	}
	public void firePropertyChange(String p, Object o, Object n) {
	}
	public void firePropertyChange(String p, boolean o, boolean n) {
	}
	
	// How to display the values in a particular cell...
	// if it is Rectangle, Point, or Oval
	public void setValue(Object value) {
		if (value instanceof Attribute) {
			Attribute attr = (Attribute) value;
			value = attr.getAttrValueAtInstant(getMediator().getMajorMoment());
		}
		if (value == null) {
			setText(NULL_STR);
		} else {
			setText(value.toString());
		}
	}
	

	public Component getTableCellRendererComponent(
		JTable table,
		Object value,
		boolean isSelected,
		boolean hasFocus,
		int row,
		int col) {
		Font f = table.getFont();
		int color = 0;
		Descriptor desc = null;
		int[] selCols = table.getSelectedColumns();
		col = table.convertColumnIndexToModel(col);
		for (int index = 0; index < selCols.length; index++) {
			selCols[index] = table.convertColumnIndexToModel(selCols[index]);
		}
		Arrays.sort(selCols);
		boolean cellSelected = false;
		if (value instanceof Attribute) {
			Attribute attr = (Attribute) value;
			desc = attr.getDescriptor();
			cellSelected = model.getMediator().getSelection().isSelected(attr);
		} else if (value instanceof Descriptor) {
			desc = (Descriptor) value;
			value = new Integer(desc.getDescId());
			cellSelected = model.getMediator().getSelection().isSelected(desc);
		}
		
		if (desc != null) {
			int style = 0;
			boolean valid = getMediator().isThisValidNow(desc);
			PropagateInterpolateModule p = getMediator().getPropagator();
			boolean propagate = p.getPropagatingDescriptors().contains(desc);
			if (!valid && propagate) {
				style = Font.ITALIC | Font.BOLD;
			} else if (!valid) {
				style = Font.ITALIC;
			} else if (propagate) {
				style = Font.BOLD;
			} else {
				style = Font.PLAIN;
			}
			f = f.deriveFont(style);
			if (cellSelected) {
				color = 2;
			} else if (table.isRowSelected(row)) {
				color = 1;
			}
		}
		EnhancedCellUtilities.setAppropriateColor(this, color, table);
		EnhancedCellUtilities.setAppropriateBorder(this, hasFocus, table);
		setFont(f);
		

		setValue(value);

		return this;
	}
}