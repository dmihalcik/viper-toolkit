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

/************************************************************************** *
 * File:        ContentTable.java
 * Purpose:     The Content Table is the table that you see in the upper right
 *              right corner of the ViPER Application. It is bascially a spreadsheet
 *              like table (much like Microsoft Excel) and contains mostly the
 *              information about Content data types.
 * Written by:  Felix Sukhenko
 *              Charles Lin
 * Date:        November 1998
 * Notes:       Should be compiled one directory back (i.e. javac <classpath including
 *              swing classes> gui/ContentTable.java
 * ************************************************************************ *
 * Modification Log:
 *
 * DATE       WHO              MODIFICATION
 * 12/09/98   Felix Sukhenko   Changed the Constructors to accept a properties file
 *                             as a string parameter so that you could customize the
 *                             Hot Keys.
 *
 * ************************************************************************ *
 *      Copyright (C) 1997 by the University of Maryland, College Park
 *
 * 		Laboratory for Language and Media Processing
 *		   Institute for Advanced Computer Studies
 *	       University of Maryland, College Park, MD  20742
 *
 *  email: lamp@cfar.umd.edu               http: documents.cfar.umd.edu/LAMP
 * ************************************************************************ *
 * ************************************************************************ */
package edu.umd.cfar.lamp.viper.gui.table;

import java.awt.*;
import java.awt.event.*;
import java.util.*;

import javax.swing.*;

import viper.api.*;
import edu.umd.cfar.lamp.viper.gui.core.*;
import edu.umd.cfar.lamp.viper.util.*;

public class ContentTable extends JPanel implements Scrollable, ViperTableTabComponent {
	private static class Selection {
		public Config cfg;
		public Descriptor desc;
		public Attribute attr;
	}
	
	private TablePanel outerTablePanel;
	
	private Selection oldSelection = new Selection();
	
	private Map configs2panels;
	private ViperViewMediator mediator;
	
	public Iterator getConfigs() {
		return configs2panels.keySet().iterator();
	}
	public AbstractViperTable getTableFor(Config c) {
		OnePanel p = (OnePanel) configs2panels.get(c);
		if (p != null) {
			return p.tab;
		} else {
			return null;
		}
	}

	public class ContentDataModel extends ViperTableModel {
		public ContentDataModel(Config cfg) {
			super(cfg);
		}
		public ViperViewMediator getMediator() {
			return ContentTable.this.getMediator();
		}
	}
	public class SingleContentTable extends AbstractViperTable {
		public SingleContentTable (TablePanel tp, Config c) {
			super(tp);
			super.setCurrentModel(new ContentDataModel(c));
		}

		public Descriptor getSelectedRow() {
			return getCurrentModel().getDescriptorAtRow(
				getTable().getSelectedRow());
		}
		protected void maybeShowPopup(MouseEvent e) {
			if (e.isPopupTrigger()) {
				popup.show(e.getComponent(), e.getX(), e.getY());
			}
		}

		public void redoSelectionModel() {
			int[] oldRows = getTable().getSelectedRows();
			int[] oldCols = getTable().getSelectedColumns();
			if (oldRows.length > 0 || oldCols.length > 0) {
				getTable().clearSelection();
			}
			if (getMediator() == null) {
				return;
			}
			ViperSelectionSet selection = getMediator().getSelection();
			Config currCfg = getCurrentModel().getConfig();
			Iterator iter = selection.getSelectionBeneath(currCfg);
			while (iter.hasNext()) {
				Node n = (Node) iter.next();
				Descriptor d = null;
				Attribute a = null;
				if (n instanceof Descriptor) {
					d = (Descriptor) n;
				} else if (n instanceof Attribute) {
					a = (Attribute) n;
					d = a.getDescriptor();
				} else {
					continue;
				}
				int newRow = getCurrentModel().getRowForDescriptor(d);
				int newCol = (a != null) ? getCurrentModel().getColumnForAttribute(
						a) : 0;
				if (newRow > -1) {
					getTable().addRowSelectionInterval(newRow, newRow);
				}
				if (newCol > 0) {
					getTable().addColumnSelectionInterval(newCol, newCol);
					ContentTable.this.scrollToAttribute(a);
				}
			}
		}

		public void redoDataModel() {
			ContentDataModel m = (ContentDataModel) getCurrentModel();
			m.resetDescs();
			m.fireTableDataChanged();
		}

		public Config getConfig() {
			ContentDataModel m = (ContentDataModel) getCurrentModel();
			return m.getConfig();
		}
	}
	
	public static void printsizes(String name, Component c) {
		System.err.println("==== component " + name);
		System.err.println("min: " + c.getMinimumSize() + "; pref: " + c.getPreferredSize() + "; max: " + c.getMaximumSize());
		System.err.println("===============");
	}
 
	private class OnePanel extends JPanel {
		private NameLabel nl;
		private SingleContentTable tab;
		
		private class NameLabel extends JButton {
			public NameLabel() {
				super("Content");
				Config cfg = getConfig();
				super.setText(cfg == null ? " " : cfg.getDescName());
			}
			private Color defBkg = null;
			private Color hilite = Color.cyan;
			
			protected void paintComponent(Graphics g) {
				if (defBkg == null) {
					defBkg = super.getBackground();
				}
				if (getMediator().getSelection().isSelected(getConfig())) {
					super.setBackground(hilite);
				} else {
					super.setBackground(defBkg);
				}
				super.paintComponent(g);
			}
		}
		
		ActionListener selectCfg = new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				getMediator().getSelection().setTo(getConfig());
			}
		};
		
		public OnePanel(TablePanel tp, Config cfg) {
			tab = new SingleContentTable(tp, cfg);
			nl = new NameLabel();
			nl.addActionListener(selectCfg);

			boxLayout();
//			springLayout();

		}
		void redoSelection() {
			tab.redoSelectionModel();
			nl.invalidate();
			this.invalidate();
		}
		private void resetSizes(JComponent c) {
			c.setMinimumSize(null);
			c.setPreferredSize(null);
			c.setMaximumSize(null);
		}
		private void boxLayout() {
			super.removeAll();
			resetSizes(nl);
			resetSizes(tab);

			int height = (int) nl.getMaximumSize().getHeight();
//			int width = (int) Math.max(nl.getPreferredSize().getWidth(), tab.getPreferredSize().getWidth());
			nl.setMaximumSize(new Dimension(Short.MAX_VALUE, height));
			tab.setMaximumSize(new Dimension(Short.MAX_VALUE, 3*height));
			super.setMaximumSize(new Dimension(Short.MAX_VALUE, 4*height));


			// The tablePanel will consist of three parts.
			//   At the very top is the name of the content descriptor (e.g. Scene)
			//   In the center, there is the table that holds the information.
			//   At the botoom, there is some spacing added by a vertical strut.
			super.add(nl);
			super.add(tab);

			super.setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
		}

		private void springLayout() {
			super.removeAll();
			resetSizes(nl);
			resetSizes(tab);
			SpringLayout layout = new SpringLayout();
			super.setLayout(layout);

			int height = (int) nl.getMaximumSize().getHeight();
			int width = (int) Math.max(nl.getPreferredSize().getWidth(), tab.getPreferredSize().getWidth());

			super.add(nl);
			super.add(tab);

			nl.setMaximumSize(new Dimension(Short.MAX_VALUE, height));
			tab.setMaximumSize(new Dimension(Short.MAX_VALUE, 3*height));
//			super.setPreferredSize(new Dimension(width, 4*height));
			super.setMaximumSize(new Dimension(Short.MAX_VALUE, 4*height));

			SpringLayout.Constraints panCons = layout.getConstraints(this);
			SpringLayout.Constraints nlCons = layout.getConstraints(nl);
			SpringLayout.Constraints tabCons = layout.getConstraints(tab);

//			Spring x_end = panCons.getConstraint(SpringLayout.EAST);
			nlCons.setX(Spring.constant(0));
			nlCons.setY(Spring.constant(0));
			nlCons.setWidth(Spring.constant(0, width, Short.MAX_VALUE));
			nlCons.setHeight(Spring.constant(height, height, height));

			tabCons.setX(Spring.constant(0));
			tabCons.setY(nlCons.getConstraint(SpringLayout.SOUTH));
			tabCons.setWidth(Spring.constant(0, width, Short.MAX_VALUE));
			tabCons.setHeight(Spring.constant(height, 2*height, 3*height));

//			Spring leading = Spring.constant(5);
//			Spring y = Spring.sum(leading, tabCons.getConstraint(SpringLayout.SOUTH));
			panCons.setWidth(Spring.constant(0, Short.MAX_VALUE, Short.MAX_VALUE));
			panCons.setConstraint(SpringLayout.SOUTH, tabCons.getConstraint(SpringLayout.SOUTH));
		}
		public Config getConfig () {
			ViperTableModel mod = tab.getCurrentModel();
			return mod == null ? null : mod.getConfig();
		}
	}
	
	public ContentTable(TablePanel tp) {
		super();
		this.outerTablePanel = tp;
	}

	public void setMediator(ViperViewMediator mediator) {
		// this.getMediator(). remove listeners
		super.removeAll();
		this.mediator = mediator;
		if (mediator == null) {
			return;
		}
		Iterator cfgs =
			mediator.getViperData().getConfigsOfType(Config.CONTENT);
		this.setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
		count = 0;
		totalHeight = 0;
		int height = Short.MAX_VALUE;
		configs2panels = new HashMap();
		while (cfgs.hasNext()) {
			// Each table will be placed in its own tablePanel
			Config curr = (Config) cfgs.next();
			OnePanel op = new OnePanel(outerTablePanel, curr);
			configs2panels.put(curr, op);
			op.tab.setMediator(mediator);
			this.add(op);
			count++;
			int opHeight = op.getMaximumSize().height;
			totalHeight += opHeight;
			height = Math.min(height, opHeight);
		}
		this.setPreferredSize(new Dimension(this.getPreferredSize().width, totalHeight));
	}
	
	private int totalHeight;
	private int count = 0;
	
	public ViperViewMediator getMediator() {
		return this.mediator;
	}

	public Dimension getPreferredScrollableViewportSize() {
		Dimension d2 = new Dimension(Short.MAX_VALUE, totalHeight);
		return d2;
	}
	private OnePanel getFirstBelowView(Rectangle visibleRect) {
		int y_end = visibleRect.y + visibleRect.height; // -1?
		OnePanel op = (OnePanel) getComponentAt(visibleRect.x, y_end);
		if (op.getY() + op.getHeight() == y_end) {
			op = (OnePanel) getComponentAt(visibleRect.x, y_end + 1);
		}
		return op;
	}
	private OnePanel getFirstAboveView(Rectangle visibleRect) {
		OnePanel op = (OnePanel) getComponentAt(visibleRect.x, visibleRect.y);
		if (op.getY() == visibleRect.y) {
			op = (OnePanel) getComponentAt(visibleRect.x, visibleRect.y - 1);
		}
		return op;
	}
	public int getScrollableUnitIncrement(Rectangle visibleRect, int orientation, int direction) {
		int wh = 0;
		if (direction < 0 && visibleRect.y != 0) {
			// scroll up one unit
			// makes the first one off the top the new top
			OnePanel op = getFirstAboveView(visibleRect);
			wh = visibleRect.y - op.getY();
		} else if (direction > 0) { 
			// scroll down one unit
			// makes the first one off the bottom at the bottom
			OnePanel op = getFirstBelowView(visibleRect);
			int opBottom = op.getHeight() + op.getY();
			int visBottom = visibleRect.height + visibleRect.y;
			wh = opBottom - visBottom;
		}
		// don't ever scroll more than the visible area
		return Math.min(visibleRect.height, wh);
	}
	public int getScrollableBlockIncrement(Rectangle visibleRect, int orientation, int direction) {
		int wh = 0;
		if (direction < 0 && visibleRect.y != 0) {
			// scroll up one block
			// makes the first one off the top the new bottom
			OnePanel op = getFirstAboveView(visibleRect);
			int opBottom = op.getHeight() + op.getY();
			int visBottom = visibleRect.height + visibleRect.y;
			wh = visBottom - opBottom;
		} else if (direction > 0) { 
			// scroll down one block
			// makes the first one off the bottom at the top
			OnePanel op = getFirstBelowView(visibleRect);
			wh = op.getY() - visibleRect.y;
		}
		// don't ever scroll more than the visible area
		return Math.min(visibleRect.height, wh);
	}

	public boolean getScrollableTracksViewportWidth() {
		return true;
	}
	public boolean getScrollableTracksViewportHeight() {
		return false;
	}

	public void redoSelectionModel() {
		ViperSubTree selection = null;
		Config newCfg = null;
		boolean hadSelection = oldSelection.cfg != null;
		boolean hasSelection = false;
		boolean removeOld = hadSelection;
		boolean addNew = false;
		if (getMediator() != null) {
			selection = getMediator().getPrimarySelection();
			newCfg = selection.getFirstConfig();
			hadSelection = oldSelection.cfg != null;
			hasSelection = newCfg != null && newCfg.getDescType() == Config.CONTENT;
			removeOld = hadSelection && !oldSelection.cfg.equals(newCfg);
			addNew = hasSelection && !newCfg.equals(oldSelection);
		}
		if (removeOld) {
			OnePanel op = (OnePanel) configs2panels.get(oldSelection.cfg);
			op.redoSelection();
			oldSelection.cfg = null;
			oldSelection.desc = null;
			oldSelection.attr = null;
		}
		if (addNew) {
			oldSelection.cfg = selection.getFirstConfig();
			oldSelection.desc = selection.getFirstDescriptor();
			oldSelection.attr = selection.getFirstAttribute();
			OnePanel op = (OnePanel) configs2panels.get(oldSelection.cfg);
			if (op != null) {
				op.redoSelection();
			}
			if (oldSelection.attr != null) {
				scrollToAttribute(oldSelection.attr);
			} else if (oldSelection.cfg != null) {
				scrollToConfig(oldSelection.cfg);
			}
		}
	}

	public void redoDataModel() {
		for (int i = 0; i < count; i++) {
			OnePanel op = (OnePanel) ContentTable.this.getComponent(i);
			op.tab.redoDataModel();
		}
	}
	public void redoPropagateModel() {
		for (int i = 0; i < count; i++) {
			OnePanel op = (OnePanel) ContentTable.this.getComponent(i);
			op.tab.redoPropagateModel();
		}
	}
	
	public void scrollToAttribute(Attribute a) {
		OnePanel op = scrollToConfig(a.getDescriptor().getConfig());
		if (op != null) {
			op.tab.scrollToAttribute(a);
		}
	}
	
	public OnePanel scrollToConfig(Config c) {
		if (!(getParent() instanceof JViewport)) {
            return null;
        }
		JViewport viewport = (JViewport) getParent();
		for (int i = 0; i < count; i++) {
			OnePanel op = (OnePanel) ContentTable.this.getComponent(i);
			if (op.tab.getConfig().equals(c)) {
		        Rectangle rect = op.getBounds();
		    
		        // The location of the viewport relative to the table
		        Point pt = viewport.getViewPosition();
		    
		        // Translate the cell location so that it is relative
		        // to the view, assuming the northwest corner of the
		        // view is (0,0)
		        rect.setLocation(rect.x-pt.x, rect.y-pt.y);
		    
		        // Scroll the area into view
		        viewport.scrollRectToVisible(rect);
		        
		        return op;
			}
		}
		return null;
	}
}
