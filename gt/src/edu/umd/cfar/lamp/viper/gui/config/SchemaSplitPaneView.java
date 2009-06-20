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

import java.awt.*;
import java.awt.event.*;

import javax.swing.*;
import javax.swing.event.*;

import viper.api.*;
import edu.umd.cfar.lamp.apploader.misc.*;
import edu.umd.cfar.lamp.apploader.propertysheets.*;
import edu.umd.cfar.lamp.viper.gui.core.*;

/**
 * 
 */
public class SchemaSplitPaneView extends JPanel {
	private JSplitPane splitPane;
	private ConfigEditor tree;
	private JButton newCfgButton;
	private JButton newAttrButton;
	private JButton delButton;
	private JButton importButton;
	private JButton doneButton;
	
	private void resetEnabledButtons() {
		Object o = tree.getTree().getLastSelectedPathComponent();
		boolean enableAttrs = (o instanceof Config) || (o instanceof AttrConfig);
		newAttrButton.setEnabled(enableAttrs);
		delButton.setEnabled(enableAttrs);
	}
	
	/**
	 * Called by containers to tell them that they have 
	 * a new parent. This exists to add window listener that moves 
	 * @see java.awt.Component#addNotify()
	 */
	public void addNotify() {
		super.addNotify();
		if (lastWindow != null) {
			lastWindow.removeWindowFocusListener(stopEditingOnFocusLost);
		} else {
			Component l = this;
			lastWindow = null;
			do {
				l = l.getParent();
			} while (l != null && !(l instanceof Window));
			if (l instanceof Window) {
				lastWindow = (Window) l;
				lastWindow.addWindowFocusListener(stopEditingOnFocusLost);
			}
		}
	}
	private WindowFocusListener stopEditingOnFocusLost = new WindowFocusListener() {
		public void windowGainedFocus(WindowEvent e) {
		}
		public void windowLostFocus(WindowEvent e) {
			EnhancedTable t = tree.getSheet().getTable();
			t.editingStopped(new ChangeEvent(t));
		}
	};
	private Window lastWindow = null;
	
	/**
	 * 
	 */
	public SchemaSplitPaneView() {
		super();
		tree = new ConfigEditor();
		tree.setSheet(new PropertySheet());
		tree.getTree().getSelectionModel().addTreeSelectionListener(new TreeSelectionListener() {
			public void valueChanged(TreeSelectionEvent e) {
				resetEnabledButtons();
			}
		});
		splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, tree, tree.getSheet());
		newCfgButton = new JButton("Add Descriptor");
		newCfgButton.addActionListener(getNewConfigActionListener());
		newAttrButton = new JButton("Add Attribute");
		newAttrButton.addActionListener(getNewAttrConfigActionListener());
		delButton = new JButton("Delete");
		delButton.addActionListener(getDeleteActionListener());
		importButton = new JButton("Import...");
		importButton.setEnabled(false);
		doneButton = new JButton("Finished");
		doneButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				// Make sure the table is finished editing and hide the window
				JRootPane root = SchemaSplitPaneView.this.getRootPane();
				root.getParent().setVisible(false);
				getSheet().getTable().editCellAt(-1,-1);			
			}
		});
		arrangeAllThings(5);
		resetEnabledButtons();
	}
	
	private void arrangeAllThings(int lead) {
		SpringLayout layout = new SpringLayout();
		setLayout(layout);
		SpringLayout.Constraints pCons = layout.getConstraints(this);

		SpringLayout.Constraints spCons = layout.getConstraints(splitPane);

		super.add(splitPane);
		super.add(newCfgButton);
		super.add(newAttrButton);
		super.add(delButton);
		super.add(importButton);
		super.add(doneButton);
		
		Spring leading = Spring.constant(lead);
		Spring x_end = leading;

		Spring x = leading;
		Spring y = leading;

		spCons.setX(x);
		spCons.setY(y);
		y = spCons.getConstraint(SpringLayout.SOUTH);
		x_end = Spring.max(x_end, spCons.getConstraint(SpringLayout.EAST));

		SpringLayout.Constraints ndCons = layout.getConstraints(newCfgButton);
		ndCons.setX(x);
		ndCons.setY(Spring.sum(leading, y));

		SpringLayout.Constraints naCons = layout.getConstraints(newAttrButton);
		x = Spring.sum(ndCons.getConstraint(SpringLayout.EAST), Spring.constant(lead));
		naCons.setX(x);
		naCons.setY(ndCons.getConstraint(SpringLayout.NORTH));

		SpringLayout.Constraints dCons = layout.getConstraints(delButton);
		x = Spring.sum(naCons.getConstraint(SpringLayout.EAST), Spring.constant(lead));
		dCons.setX(x);
		dCons.setY(ndCons.getConstraint(SpringLayout.NORTH));

		SpringLayout.Constraints iCons = layout.getConstraints(importButton);
		x = Spring.sum(dCons.getConstraint(SpringLayout.EAST), Spring.constant(lead));
		iCons.setX(x);
		iCons.setY(ndCons.getConstraint(SpringLayout.NORTH));

		SpringLayout.Constraints aCons = layout.getConstraints(doneButton);
		x = Spring.sum(iCons.getConstraint(SpringLayout.EAST), Spring.constant(lead));
		aCons.setX(x);
		aCons.setY(ndCons.getConstraint(SpringLayout.NORTH));

		y = naCons.getConstraint(SpringLayout.SOUTH);
		x_end = Spring.max(x_end, iCons.getConstraint(SpringLayout.EAST));

		pCons.setConstraint(SpringLayout.SOUTH, Spring.sum(leading, y));
		pCons.setConstraint(
			SpringLayout.EAST,
			Spring.sum(x_end, Spring.constant(lead)));
	}

	public ActionListener getNewConfigActionListener() {
		return tree.getNewConfigActionListener();
	}
	public ActionListener getNewAttrConfigActionListener() {
		return tree.getNewAttrConfigActionListener();
	}
	public ActionListener getDeleteActionListener() {
		return tree.getDeleteActionListener();
	}

	public ViperViewMediator getMediator() {
		return tree.getMediator();
	}
	public PropertySheet getSheet() {
		return tree.getSheet();
	}
	public ConfigEditor getTree () {
		return tree;
	}

	public void setMediator(ViperViewMediator m) {
		tree.setMediator(m);
		tree.getSheet().setPrefs(m.getPrefs());
		if (tree.getMediator() == null) {
			importButton.setEnabled(false);
			ActionListener[] als = importButton.getActionListeners();
			for (int i = 0; i < als.length; i++) {
				importButton.removeActionListener(als[i]);
			}
		} else {
			importButton.addActionListener(tree.getMediator().getImportConfigActionListener());
			importButton.setEnabled(true);
		}
	}
	public void setSheet(PropertySheet sheet) {
		this.tree.setSheet(sheet);
	}
}
