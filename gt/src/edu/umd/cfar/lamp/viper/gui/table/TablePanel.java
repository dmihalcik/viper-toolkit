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
import javax.swing.event.*;

import viper.api.*;
import viper.api.extensions.*;
import viper.api.time.*;

import com.hp.hpl.jena.rdf.model.*;

import edu.umd.cfar.lamp.apploader.prefs.*;
import edu.umd.cfar.lamp.viper.gui.core.*;
import edu.umd.cfar.lamp.viper.util.*;

/**
 * @author smoore
 * @author davidm
 */
public class TablePanel extends JPanel {
	// holds the icons for visualize table display status
	ImageIcon[] visibilityIcons = new ImageIcon[NodeVisibilityManager.VISIBLE + 1];
	private int lastIndex = -1;
	private Config[] objectsAtTabs;
	private Map objectTabIndexes;
	private JTabbedPane tabs = null;
	private JButton createButton;
	private JButton deleteButton;
	private JButton duplicateButton;
	private String createText = "Create";
	private String deleteText = "Delete";
	private String duplicateText = "Duplicate";

	private SpringLayout layout;
	private SpringLayout.Constraints pCons;

	private ViperViewMediator mediator;
	private PrefsManager prefs;
	private TabChangeListener tabChangeListener;
	/// map from configs to their table parameters
	private WeakHashMap paramsForEachDesc = new WeakHashMap();

	private String noMediatorMessage =
		"No descriptors are currently available. Open a file.";
	private String noConfigsMessage =
		"Your data file has no configs. Use the schema editor to create a file with defined descriptor types.";
	private JTextArea unloadedMessage;

	public boolean isAllowingVisibilityChanges() {
		return visibilityIcons[0] != null;
	}

	public TablePanel() {
		this.tabChangeListener = new TabChangeListener();
		this.tabs =
			new JTabbedPane(SwingConstants.TOP, JTabbedPane.WRAP_TAB_LAYOUT);
		this.tabs.getModel().addChangeListener(tabChangeListener);
		this.tabs.addMouseListener(new MouseAdapter() {
			public void mouseClicked(MouseEvent e) {
				if (isAllowingVisibilityChanges()) {
					int index =
						tabs.getUI().tabForCoordinate(tabs, e.getX(), e.getY());
					if (index > -1) {
						if (lastIndex == index) {
							toggleVisibility(index);
						}
						lastIndex = index;
					}
				}
			}
		});
		this.objectTabIndexes = new HashMap();
		this.objectsAtTabs = new Config[0];

		this.unloadedMessage = new JTextArea(noMediatorMessage);
		this.unloadedMessage.setBackground(this.getBackground());
		this.unloadedMessage.setWrapStyleWord(true);
		this.unloadedMessage.setLineWrap(true);
		this.unloadedMessage.setEditable(false);

		this.createButton = new JButton(createText);
		this.deleteButton = new JButton(deleteText);
		this.duplicateButton = new JButton(duplicateText);

		this.setMinimumSize(new Dimension(0, 0));
		this.setPreferredSize(new Dimension(480, 480));
		this.setMaximumSize(new Dimension(Short.MAX_VALUE, Short.MAX_VALUE));

		this.layout = new SpringLayout();
		super.setLayout(layout);
		super.add(unloadedMessage);
		super.add(tabs);
		unloadedMessage.setVisible(true);
		tabs.setVisible(false);
		super.add(createButton);
		super.add(deleteButton);
		super.add(duplicateButton);
		applyLayoutConstraints(5);
	}

	private void applyLayoutConstraints(int lead) {
		pCons = layout.getConstraints(this);
		SpringLayout.Constraints mainCons = layout.getConstraints(tabs);
		SpringLayout.Constraints mainCopy =
			layout.getConstraints(unloadedMessage);

		Spring leading = Spring.constant(lead);
		Spring x_end = leading;
		Spring x = leading;
		Spring y = leading;

		mainCons.setX(x);
		mainCons.setY(y);
		y = mainCons.getConstraint(SpringLayout.SOUTH);
		x_end = Spring.sum(leading, mainCons.getConstraint(SpringLayout.EAST));

		mainCopy.setX(mainCons.getX());
		mainCopy.setY(mainCons.getY());
		mainCopy.setHeight(mainCons.getHeight());
		mainCopy.setWidth(mainCons.getWidth());

		SpringLayout.Constraints createCons =
			layout.getConstraints(createButton);
		createCons.setX(x);
		createCons.setY(Spring.sum(leading, y));

		SpringLayout.Constraints deleteCons =
			layout.getConstraints(deleteButton);
		x =
			Spring.sum(
				createCons.getConstraint(SpringLayout.EAST),
				Spring.constant(lead));
		deleteCons.setX(x);
		deleteCons.setY(createCons.getConstraint(SpringLayout.NORTH));
		y = createCons.getConstraint(SpringLayout.SOUTH);

		SpringLayout.Constraints duplicateCons =
			layout.getConstraints(duplicateButton);
		x =
			Spring.sum(
				deleteCons.getConstraint(SpringLayout.EAST),
				Spring.constant(lead));
		duplicateCons.setX(x);
		duplicateCons.setY(createCons.getConstraint(SpringLayout.NORTH));

		pCons.setConstraint(SpringLayout.SOUTH, Spring.sum(leading, y));
		pCons.setConstraint(SpringLayout.EAST, x_end);
	}

	private void buttonConfigure() {
		ViperSubTree selection = null;
		Config selCfg = null;
		if (mediator != null) {
			selection = mediator.getPrimarySelection();
			selCfg = selection.getFirstConfig();
		}

		if (selCfg == null
			|| selCfg.getDescType() == Config.FILE
			|| mediator.getCurrFile() == null) {
			createButton.setEnabled(false);
			deleteButton.setEnabled(false);
			duplicateButton.setEnabled(false);
		} else if (selCfg.getDescType() == Config.CONTENT) {
			Instant now = mediator.getMajorMoment();
			Iterator i = mediator.getCurrFile().getDescriptorsBy(selCfg, now);
			boolean valid = i.hasNext();
			createButton.setEnabled(!valid);
			deleteButton.setEnabled(valid);
			duplicateButton.setEnabled(false);
		} else {
			boolean selDesc = selection.getFirstDescriptor() != null;
			createButton.setEnabled(true);
			deleteButton.setEnabled(selDesc);
			duplicateButton.setEnabled(selDesc);
		}
	}

	private void setVisibilityBallForTab(int tab, int visible) {
		if (tab < 0 || tab >= this.tabs.getTabCount()) {
			throw new IllegalArgumentException("Not a valid tab: " + tab);
		}
		tabs.setIconAt(tab, visibilityIcons[visible]);
	}

	private int getTabForConfig(Config cfg) {
		if (cfg != null && cfg.getDescType() == Config.CONTENT) {
			cfg = null;
		}
		return ((Integer) objectTabIndexes.get(cfg)).intValue();
	}

	public void handleDataChange(ViperChangeEvent nce) {
		Config c = null;
		Descriptor d = null;
		if (nce.getParent() instanceof Descriptor) {
			d = (Descriptor) nce.getParent();
			c = d.getConfig();
		} else if (nce.getParent() instanceof Attribute) {
			d = (Descriptor) nce.getParent().getParent();
			c = d.getConfig();
		} else {
			// Don't know what to do - either reload tabs
			// or 
		}

		if (c != null) {

		}
	}

	private void resetVisibilityBalls() {
		for (int i = 0; i < tabs.getTabCount(); i++) {
			setVisibilityBallForTab(i, NodeVisibilityManager.VISIBLE);
		}

		if (mediator == null) {
			return;
		}

		NodeVisibilityManager H = mediator.getHiders();
		int[] hiddenTypes = H.getHidingTypes();
		Config[] hiddenConfigs = H.getHidingConfigs();
		for (int i = 0; i < hiddenTypes.length; i++) {
			if (hiddenTypes[i] == Config.FILE) {
				Config f =
					(Config) mediator
						.getViperData()
						.getConfigsOfType(Config.FILE)
						.next();
				int tnum = getTabForConfig(f);
				if (tnum > 0) {
					setVisibilityBallForTab(tnum, H.getTypeVisibility(Config.FILE));
				}
			} else if (hiddenTypes[i] == Config.CONTENT) {
				int tnum = getTabForConfig(null);
				if (tnum > 0) {
					setVisibilityBallForTab(tnum, H.getTypeVisibility(Config.CONTENT));
				}
			} else {
				// XXX: what to do when OBJECT descriptors are disabled?
			}
		}

		for (int i = 0; i < hiddenConfigs.length; i++) {
			Config cfg = hiddenConfigs[i];
			int tnum = getTabForConfig(cfg);
			if (tnum > -1) {
				setVisibilityBallForTab(tnum, H.getConfigVisibility(cfg));
			}
		}
	}

	private void resetSelectionToCurrent() {
		ViperSubTree sel = mediator.getPrimarySelection();
		Config curr = sel.getFirstConfig();
		if (curr != null) {
			int oldIndex = this.tabs.getSelectedIndex();
			int newIndex = oldIndex;
			if (curr.getDescType() == Config.CONTENT) {
				newIndex = 0;
				contentTab.redoSelectionModel();
			} else {
				Integer i = (Integer) objectTabIndexes.get(curr);
				if (i != null) {
					newIndex = i.intValue();
				} else {
					newIndex = oldIndex;
				}
			}
			if (oldIndex != newIndex) {
				this.tabs.setSelectedIndex(newIndex);
			}
			getViperTableTab(newIndex).redoSelectionModel();
		}
		buttonConfigure();
	}

	private void resetVisibilityBallIcons() {
		String dir;
		Resource self = prefs.getCore().getResourceForBean(this);

		if (self != null) {
			Statement s = self.getProperty(TABLE.enableTableIcon);
			if (s != null) {
				dir = s.getString();
				visibilityIcons[NodeVisibilityManager.VISIBLE] = new ImageIcon(dir);
				visibilityIcons[NodeVisibilityManager.RANGE_LOCKED] = new ImageIcon(dir);
			}
			s = self.getProperty(TABLE.lockedTableIcon);
			if (s != null) {
				dir = s.getString();
				visibilityIcons[NodeVisibilityManager.LOCKED] = new ImageIcon(dir);
			}
			s = self.getProperty(TABLE.disableTableIcon);
			if (s != null) {
				dir = s.getString();
				visibilityIcons[NodeVisibilityManager.HIDDEN] = new ImageIcon(dir);
			}
		}
		resetVisibilityBalls();
	}

	public PrefsManager getPrefs() {
		return prefs;
	}
	public void setPrefs(PrefsManager p) {
		this.prefs = p;
		resetVisibilityBallIcons();
	}

	private boolean hasContentPane = false;
	private boolean hasFilePane = false;

	private ViperTableTabComponent getViperTableTab(int i) {
		Component c = tabs.getComponentAt(i);
		if (c instanceof JScrollPane) {
			JScrollPane sp = (JScrollPane) c;
			return (ViperTableTabComponent) sp.getViewport().getView();
		} else {
			return (ViperTableTabComponent) c;
		}
	}

	private FocusListener mfl = new FocusListener();
	private ChangeListener selectionListener = new ChangeListener() {
		public void stateChanged(ChangeEvent e) {
			resetSelectionToCurrent();
		}
	};
	private ChangeListener visibilityListener = new ChangeListener() {
		public void stateChanged(ChangeEvent e) {
			resetVisibilityBalls();
		}
	};
	private ContentTable contentTab;
	private class FocusListener
		implements
			ViperMediatorChangeListener,
			PropagateInterpolateModule.PropagateListener {
		public void frameChanged(ViperMediatorChangeEvent e) {
			for (int i = 0; i < tabs.getTabCount(); i++) {
				ViperTableTabComponent c = getViperTableTab(i);
				if (!(c instanceof FileTable)) {
					c.redoDataModel();
				}
			}
			invalidate();
		}

		public void listChanged() {
			// propagate changed
			for (int i = 0; i < tabs.getTabCount(); i++) {
				getViperTableTab(i).redoPropagateModel();
				getViperTableTab(i).redoSelectionModel();
			}
		}

		public void dataChanged(ViperMediatorChangeEvent e) {
			for (int i = 0; i < tabs.getTabCount(); i++) {
				getViperTableTab(i).redoDataModel();
			}
		}

		public void currFileChanged(ViperMediatorChangeEvent o) {
			// TODO: store and reload tab positions (hash on media file & metadata file)
			for (int i = 0; i < tabs.getTabCount(); i++) {
				getViperTableTab(i).redoDataModel();
			}
			buttonConfigure();
		}
		public void schemaChanged(ViperMediatorChangeEvent e) {
			reloadTabs();
			tabs.setVisible(true);
			tabs.invalidate();
		}
		public void mediaChanged(ViperMediatorChangeEvent e) {
		}
	}

	private class TabChangeListener implements ChangeListener {
		public void stateChanged(ChangeEvent e) {
			if (mediator != null) {
				ViperSubTree selection = mediator.getPrimarySelection();
				Config oldCfg = selection.getFirstConfig();
				int newIndex = tabs.getSelectedIndex();
				if (newIndex == -1) {
					if (oldCfg != null) {
						mediator.getSelection().setTo(mediator.getCurrFile());
					}
				} else {
					Config newCfg = objectsAtTabs[newIndex];
					if (null == newCfg) {
						// the CONTENT tab
						// XXX should select the first config / or the last one selected
						mediator.getSelection().setTo(mediator.getCurrFile());
					} else if (null == oldCfg || !oldCfg.equals(newCfg)) {
						// an OBJECT or FILE tab
						mediator.getSelection().setTo(newCfg);
					}
				}
			}
		}
	}

	/**
	 * This function is responsible for taking the mediator
	 * and making visible table components out of it.
	 * @param mediator the viper view mediator associated with the panel
	 */
	public void setMediator(ViperViewMediator mediator) {
		if (this.mediator != null) {
			this.mediator.getPropagator().removePropagateListener(mfl);
			this.mediator.removeViperMediatorChangeListener(mfl);
			this.mediator.getSelection().removeChangeListener(
				selectionListener);
			this.mediator.getHiders().removeChangeListener(visibilityListener);
			this.createButton.removeActionListener(
				this.mediator.getCreateInstanceActionListener());
			this.deleteButton.removeActionListener(
				this.mediator.getDeleteInstanceActionListener());
			this.duplicateButton.removeActionListener(
				this.mediator.getDuplicateInstanceActionListener());
		}
		this.mediator = mediator;
		if (this.mediator != null) {
			this.mediator.addViperMediatorChangeListener(mfl);
			this.mediator.getPropagator().addPropagateListener(mfl);
			this.mediator.getSelection().addChangeListener(selectionListener);
			this.mediator.getHiders().addChangeListener(visibilityListener);
			this.createButton.addActionListener(
				this.mediator.getCreateInstanceActionListener());
			this.deleteButton.addActionListener(
				this.mediator.getDeleteInstanceActionListener());
			this.duplicateButton.addActionListener(
				this.mediator.getDuplicateInstanceActionListener());
		}
		// XXX:4.5 add listener on ConfigsNode so we get changes to the configs
		reloadTabs();
	}
	private void storeTableWidths(AbstractViperTable t) {
		if (t != null) {
			paramsForEachDesc.put(t.getConfig(), t.getTable().getColumnWidthDescription());
		}
	}
	private void restoreTableWidths(AbstractViperTable t) {
		if (t != null) {
			String s = (String) paramsForEachDesc.get(t.getConfig());
			if (s != null) {
				t.getTable().setColumnWidthsFromDescription(s);
			}
		}
	}
	
	private void clean() {
		this.tabs.getModel().removeChangeListener(tabChangeListener);
		int idx = 0;
		if (hasContentPane) {
			JScrollPane sp = (JScrollPane) this.tabs.getComponentAt(idx);
			ContentTable ct = (ContentTable) sp.getViewport().getView();
			for (Iterator iter = ct.getConfigs(); iter.hasNext(); ) {
				Config c = (Config) iter.next();
				storeTableWidths(ct.getTableFor(c));
			}
			ct.setMediator(null);
			idx++;
		}
		int objCount = this.tabs.getTabCount();
		if (hasFilePane) {
			objCount--;
		}
		while (idx < objCount) {
			ObjectTable ot = (ObjectTable) this.tabs.getComponentAt(idx);
			storeTableWidths(ot);
			ot.setMediator(null);
			idx++;
		}
		if (hasFilePane) {
			FileTable ft = (FileTable) this.tabs.getComponentAt(idx);
			storeTableWidths(ft);
			ft.setMediator(null);
		}
		hasContentPane = false;
		hasFilePane = false;
		this.tabs.removeAll();
	}
	/**
	 * Redo all the tabs in the table pane.
	 * First, stores the current state of the table columns, 
	 * then destroys the tables, then constructs the tables, 
	 * using any known state information about them. 
	 */
	private void reloadTabs() {
		// Remove tabs that no longer exist
		// Returns a list of tabs that were kept
		// adds new tabs to the right?
		// What I really should do is serialize the tab information, then
		// unserialize it as possible. that would be easier to program.
		clean();

		if (null == mediator) {
			unloadedMessage.setText(noMediatorMessage);
			tabs.setVisible(false);
			unloadedMessage.setVisible(true);
		} else {
			ViperData v = mediator.getViperData();
			if (null == v) {
				unloadedMessage.setText(noMediatorMessage);
				tabs.setVisible(false);
				unloadedMessage.setVisible(true);
			} else if (!v.getConfigs().hasNext()) {
				unloadedMessage.setText(noConfigsMessage);
				tabs.setVisible(false);
				unloadedMessage.setVisible(true);
			} else {
				LinkedList perTab = new LinkedList();

				// Create CONTENT descriptor tab
				Iterator cfgs = v.getConfigsOfType(Config.CONTENT);
				hasContentPane = cfgs.hasNext();
				if (hasContentPane) {
					contentTab = new ContentTable(this);
					contentTab.setMediator(mediator);
					for (Iterator iter = contentTab.getConfigs(); iter.hasNext(); ) {
						Config c = (Config) iter.next();
						restoreTableWidths(contentTab.getTableFor(c));
					}
					JScrollPane ctabSp = new JScrollPane(contentTab);
					//					ctabSp.set
					this.tabs.addTab("Content", visibilityIcons[NodeVisibilityManager.VISIBLE], ctabSp);
					perTab.add(null);
				}

				// Creating the OBJECT Views
				cfgs = v.getConfigsOfType(Config.OBJECT);
				while (cfgs.hasNext()) {
					Config ccfg = (Config) cfgs.next();
					ObjectTable otab = new ObjectTable(this);
					otab.setMediator(mediator);
					otab.setConfig(ccfg);
					restoreTableWidths(otab);

					perTab.add(ccfg);
					objectTabIndexes.put(
						ccfg,
						new Integer(this.tabs.getTabCount()));
					objectsAtTabs =
						(Config[]) perTab.toArray(new Config[perTab.size()]);
					this.tabs.addTab(ccfg.getDescName(), visibilityIcons[NodeVisibilityManager.VISIBLE], otab);
				}

				// Create FILE descriptor tab
				cfgs = v.getConfigsOfType(Config.FILE);
				hasFilePane = cfgs.hasNext();
				if (hasFilePane) {
					FileTable ftab = new FileTable(this);
					ftab.setMediator(mediator);
					restoreTableWidths(ftab);
					Config fcfg = (Config) cfgs.next();
					perTab.add(fcfg);
					objectTabIndexes.put(
						fcfg,
						new Integer(this.tabs.getTabCount()));

					this.tabs.addTab("File", visibilityIcons[NodeVisibilityManager.VISIBLE], ftab);
				}

				// Set up perTab list, which maps tabs to desc configs
				objectsAtTabs =
					(Config[]) perTab.toArray(new Config[perTab.size()]);
				this.tabs.getModel().addChangeListener(tabChangeListener);
				unloadedMessage.setVisible(false);
				tabs.setVisible(true);
				if (perTab.size() > 0 && perTab.get(0) != null) {
					this.mediator.getSelection().setTo(
						(Config) perTab.get(0));
				}
			}
		}
		applyLayoutConstraints(5);
		resetVisibilityBalls();
		buttonConfigure();
	}

	/**
	 * Toggle visibility on the specified tab. 
	 * @param tab the tab whose visibility to toggle
	 * @throws IllegalArgumentException if tab is non-existant, or not an object tab.
	 */
	public void toggleVisibility(int tab) {
		if (tab < 0 || tab >= this.tabs.getTabCount()) {
			throw new IllegalArgumentException("Not a valid tab: " + tab);
		}
		if (mediator == null) {
			return;
		}
		NodeVisibilityManager H = mediator.getHiders();
		if (objectsAtTabs[tab] == null) {
			int visibility = H.getTypeVisibility(Config.CONTENT);
			H.setVisibilityByType(Config.CONTENT, (visibility + 2) % 3);
		} else {
			int oldVisibility = H.getConfigVisibility(objectsAtTabs[tab]);
			int newVisibility = NodeVisibilityManager.ROTATE_VISIBILITY[oldVisibility];
			H.setVisibilityByConfig(objectsAtTabs[tab], newVisibility);
		}
	}

	public ViperTableEditor getCurrentlyVisibleEditor() {
		return (ViperTableEditor) tabs.getComponentAt(lastIndex);
	}

	public AbstractViperTable getCurrentlyVisibleTable() {
		return getCurrentlyVisibleEditor().getTable();
	}
}
