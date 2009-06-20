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

import javax.swing.*;
import javax.swing.border.*;

import edu.umd.cfar.lamp.apploader.misc.*;

/**
 * @author davidm
 * 
 * TODO To change the template for this generated type comment go to Window -
 * Preferences - Java - Code Generation - Code and Comments
 */
public class EnhancedCellUtilities {

	public static final int UNSELECTED = 0;

	public static final int ROW_SELECTED = 1;

	public static final int CELL_SELECTED = 2;

	public static Color getCellSelectionForeground(JTable t) {
		if (t instanceof EnhancedTable) {
			return ((EnhancedTable) t).getCellSelectionForeground();
		} else {
			return t.getForeground();
		}
	}

	public static Color getCellSelectionBackground(JTable t) {
		if (t instanceof EnhancedTable) {
			return ((EnhancedTable) t).getCellSelectionBackground();
		} else {
			return t.getBackground();
		}
	}

	public static Color getSelectionForeground(JTable t) {
		return t.getSelectionForeground();
	}

	public static Color getSelectionBackground(JTable t) {
		return t.getSelectionBackground();
	}

	public static Color getForeground(JTable t) {
		return t.getForeground();
	}

	public static Color getBackground(JTable t) {
		return t.getBackground();
	}

	/**
	 * 
	 * @param c the component to color
	 * @param color the color, either UNSELECTED, ROW_SELECTED or CELL_SELECTED 
	 * @param table
	 */
	public static void setAppropriateColor(Component c, int color, JTable table) {
		switch (color) {
		case 1:
			c.setForeground(getSelectionForeground(table));
			c.setBackground(getSelectionBackground(table));
			break;

		case 2:
			c.setForeground(getCellSelectionForeground(table));
			c.setBackground(getCellSelectionBackground(table));
			break;

		case 0:
			c.setForeground(getForeground(table));
			c.setBackground(getBackground(table));
			break;

		default:
			throw new IllegalArgumentException("Not a valid cell color: " + color);
		}
	}
	
	public static void setAppropriateBorder(JComponent c, boolean hasFocus, JTable table) {
		if (hasFocus) {
			c.setBorder(UIManager.getBorder("Table.focusCellHighlightBorder"));
		} else {
			c.setBorder(getEmptyBorder());
		}
	}
	
	private static Border _emptyBorder;
	private static Border getEmptyBorder() {
		if (_emptyBorder == null) {
			_emptyBorder = new EmptyBorder(1, 1, 1, 1);
		}
		return _emptyBorder;
	}
}
