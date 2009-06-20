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
import java.util.*;
import java.util.logging.*;

import javax.swing.*;
import javax.swing.event.*;
import javax.swing.tree.*;

import viper.api.*;
import edu.umd.cfar.lamp.apploader.propertysheets.*;
import edu.umd.cfar.lamp.viper.gui.core.*;

/**
 * The ConfigEditor presents a JTree view of a viperdata 
 * configuration, presented within a JScrollPane. The 
 * view is pretty straightforward, and can have a property 
 * sheet attached to it, which displays the value for the 
 * currently selected node.
 * 
 * @author David Mihalcik
 */
public class ConfigEditor extends JScrollPane {
	public static Logger logger = Logger.getLogger("edu.umd.cfar.lamp.viper.gui.config");
	private class ConfigEditPopup extends JPopupMenu {
		private JMenuItem newDescriptor;
		private JMenuItem newAttribute;
		private JMenuItem duplicate;
		private JMenuItem delete;

		private Node node;

		private class NewAttributeAction implements ActionListener {
			public void actionPerformed(ActionEvent e) {
				createAttrConfigForNode(node);
			}
		}

		private class DeleteAction implements ActionListener {
			public void actionPerformed(ActionEvent e) {
				node.getParent().removeChild(node);
			}
		}

		private class DuplicateAction implements ActionListener {
			public void actionPerformed(ActionEvent e) {
			}
		}

		public ConfigEditPopup() {
			super("Config Properties");

			newDescriptor = new JMenuItem("New Descriptor Type");
			newDescriptor.addActionListener(newConfigListener);

			newAttribute = new JMenuItem("New Attribute Type");
			newAttribute.addActionListener(new NewAttributeAction());

			delete = new JMenuItem("Delete");
			delete.addActionListener(new DeleteAction());

			duplicate = new JMenuItem("Duplicate");
			duplicate.addActionListener(new DuplicateAction());

			add(newDescriptor);
			add(newAttribute);
			//add(duplicate);
			add(new JSeparator());
			add(delete);
		}

		public void show(Component invoker, int x, int y) {
			Object o =
				getTree()
					.getClosestPathForLocation(x, y)
					.getLastPathComponent();
			if (null != o) {
				node = (Node) o;
				if ((node instanceof AttrConfig)
					|| (node instanceof Config)) {
					newAttribute.setEnabled(true);
					duplicate.setEnabled(true);
					delete.setEnabled(true);
				} else {
					newAttribute.setEnabled(false);
					duplicate.setEnabled(false);
					delete.setEnabled(false);
				}
				super.show(invoker, x, y);
			}
		}
	}

	private class MySelectionChangeListener implements TreeSelectionListener {
		public void valueChanged(TreeSelectionEvent e) {
			Node node =
				(Node) getTree().getLastSelectedPathComponent();
			if (getSheet() == null)
				return;
			if (node == null) {
				node = (Node) getTree().getPathForRow(0).getLastPathComponent();
			}
			nextToSetTo = node;
			if (getSheet().getObject() != nextToSetTo) {
				SwingUtilities.invokeLater(setSheetObject);
			}
		}
	}
	private Runnable setSheetObject = new Runnable() {
		public void run() {
			getSheet().setObject(nextToSetTo);
			nextToSetTo = null;
		}
	};
	private Object nextToSetTo = null;
	
	/**
	 * Checks to see if a path to a Config node
	 * is expanded. It adds/removes the node from
	 * the expandedNodes set as appropriate.
	 */
	private class CheckIfExpanded implements Runnable {
		private TreePath t;
		/**
		 * Creates a new check for the Config in the 
		 * path.
		 * @param t The path to the descriptor config to
		 * check.
		 */
		public CheckIfExpanded (TreePath t) {
			assert t.getLastPathComponent() instanceof Config;
			this.t = t;
		}
		public void run() {
			if (getTree().isExpanded(t)) {
				expandedNodes.add(t.getLastPathComponent());
			} else {
				expandedNodes.remove(t.getLastPathComponent());
			}
		}
	}

	private JPopupMenu popup;
	private ViperTreeModel model;
	private Set expandedNodes;

	/**
	 * Create a new config editor
	 * that isn't attached to any ViperData.
	 */
	public ConfigEditor() {
		super(new JTree());
		popup = new ConfigEditPopup();
		getTree().addMouseListener(new MouseAdapter() {
			public void mousePressed(MouseEvent e) {
				maybeShowPopup(e);
				TreePath p = getTree().getClosestPathForLocation(e.getX(), e.getY());
				Object end = p.getLastPathComponent();
				if (end instanceof Config) {
					EventQueue.invokeLater(new CheckIfExpanded(p));
				}
			}

			public void mouseReleased(MouseEvent e) {
				maybeShowPopup(e);
			}

			private void maybeShowPopup(MouseEvent e) {
				if (e.isPopupTrigger()) {
					popup.show(e.getComponent(), e.getX(), e.getY());
				}
			}
		});
		model = new ViperTreeModel(this);
		model.addTreeToSelect(getTree());
		//getTree().setRootVisible(false);
		expandedNodes = new HashSet();
		getTree().setModel(model);
		getTree().getSelectionModel().setSelectionMode(
			TreeSelectionModel.SINGLE_TREE_SELECTION);

		// Listen for when the selection changes.
		getTree().addTreeSelectionListener(new MySelectionChangeListener());
	}
	
	public void resetListOfExpandedNodes() {
		expandedNodes.clear();
		for (int i = 1; i < getTree().getRowCount(); i++) {
			if (getTree().isExpanded(i)) {
				Object o =getTree().getPathForRow(i).getLastPathComponent();
				if (o instanceof Config) {
					expandedNodes.add(o);
				}
			}
		}
	}

	Iterator getExpandedNodes() {
		return expandedNodes.iterator();
	}
	
	Config[] getCopyOfExpandedNodes() {
		return (Config[]) expandedNodes.toArray(new Config[0]);
	}

	/**
	 * Call when an expanded node has gone missing
	 * @param c The config that is gone
	 */
	void nodeRemoved(Config c) {
		expandedNodes.remove(c);
	}

	JTree getTree() {
		return (JTree) this.getViewport().getComponent(0);
	}

	/**
	 * Get the viper view mediator.
	 * @return The mediator that the view is listening to.
	 */
	public ViperViewMediator getMediator() {
		return model.getMediator();
	}

	/**
	 * Sets the view mediator.
	 * @param mediator The mediator
	 */
	public void setMediator(ViperViewMediator mediator) {
		model.setMediator(mediator);
	}

	private PropertySheet sheet;
	/**
	 * Gets the current property sheet that is used to
	 * display the details about the selected node. 
	 * @return a PropertySheet that displays (and allows the
	 * user to edit) the properties of the selected node
	 */
	public PropertySheet getSheet() {
		return sheet;
	}
	/**
	 * Sets the property sheet to keep updated.
	 * @param sheet A PropertySheet to reflect the value
	 * of the selected node.
	 */
	public void setSheet(PropertySheet sheet) {
		this.sheet = sheet;
	}

	/**
	 * An ActionListener for a 'Create New Descriptor Type' action.
	 * @return an ActionListener that creates a new descriptor.
	 */
	public ActionListener getNewConfigActionListener() {
		return newConfigListener;
	}
	private ActionListener newConfigListener = new NewConfigActionListener();
	private class NewConfigActionListener implements ActionListener {
		public void actionPerformed(ActionEvent event) {
			ViperData v = model.getMediator().getViperData();
			String newName = unusedName("Desc", new DescNameIter());
			Config c = v.createConfig(Config.OBJECT, newName);
			select(c);
		}
	}

	/**
	 * Iterates over the names of all the descriptors.
	 */
	private class DescNameIter implements Iterator {
		private Iterator descs;
		public DescNameIter() {
			descs = model.getMediator().getViperData().getConfigs();
		}
		public void remove() {
			throw new UnsupportedOperationException();
		}
		public boolean hasNext() {
			return descs.hasNext();
		}
		public Object next() {
			return ((Config) descs.next()).getDescName();
		}
	}
	/**
	 * Iterates over the names of all the attributes for the 
	 * given descriptor type.
	 */
	private class AttrNameIter implements Iterator {
		private Iterator attrs;
		public AttrNameIter(Config c) {
			attrs = c.getChildren();
		}
		public void remove() {
			throw new UnsupportedOperationException();
		}
		public boolean hasNext() {
			return attrs.hasNext();
		}
		public Object next() {
			return ((AttrConfig) attrs.next()).getAttrName();
		}
	}

	/**
	 * Returns an action listener that creates a 
	 * new attribute for the currently selected 
	 * descriptor type. It doesn't do anything
	 * if no descriptor type is selected.
	 * @return An ActionListener that creates a 
	 *  new attribute field on the current descriptor
	 *  type.
	 */
	public ActionListener getNewAttrConfigActionListener() {
		return newAttrConfigListener;
	}
	private ActionListener newAttrConfigListener =
		new NewAttrConfigActionListener();
	private class NewAttrConfigActionListener implements ActionListener {
		public void actionPerformed(ActionEvent event) {
			JTree tree = getTree();
			TreePath path = tree.getSelectionPath();
			if (path == null) {
				sheet.getLogger().warning(
					"Warning: User must select a node to attach a new config to");
				// XXX add a message telling users to select a node
			} else {
				createAttrConfigForNode((Node) path.getLastPathComponent());
			}
		}
	}
	private void duplicateNode(Node selected) {
		if (selected instanceof AttrConfig) {
			duplicateAttrConfig((AttrConfig) selected);
		} else if (selected instanceof Config) {
			duplicateConfig((Config) selected);
		}
	}
	private void duplicateAttrConfig(AttrConfig selected) {

	}
	private void duplicateConfig(Config selected) {

	}

	private void createAttrConfigForNode(Node selected) {
		Config parent = null;
		if (selected instanceof Config) {
			parent = (Config) selected;
		} else if (selected instanceof AttrConfig) {
			parent = (Config) ((AttrConfig) selected).getParent();
		}
		if (parent != null) {
			String newName = unusedName("Attr", new AttrNameIter(parent));
			String type = ViperData.ViPER_DATA_URI + "svalue";
			AttrValueWrapper params =
				model.getMediator().getDataFactory().getAttribute(type);
			Node n = parent.createAttrConfig(newName, type, false, null, params);
			select(n);
		}
	}

	/**
	 * Gets an action listener for events that delete
	 * the currently selected attriubute or descriptor config.
	 * @return An ActionListener that will delete the current selection
	 */
	public ActionListener getDeleteActionListener() {
		return deleteActionListener;
	}
	private void select(Node n) {
		model.enqueueNodeSelection(n);
	}
	
	private ActionListener deleteActionListener = new DeleteActionListener();
	private class DeleteActionListener implements ActionListener {
		public void actionPerformed(ActionEvent event) {
			JTree tree = getTree();
			TreePath path = tree.getSelectionPath();
			if (path == null) {
				sheet.getLogger().warning(
					"Warning: User must select a node to delete");
				// XXX add message telling users to select a node
			} else {
				Node selected = (Node) path.getLastPathComponent();
				try {
					Node parent = selected.getParent();
					if (parent != null) {
						parent.removeChild(selected);
						select(parent);
					}
				} catch (UnsupportedOperationException uox) {
					sheet.getLogger().warning(
						"Cannot delete node: " + selected);
				}
			}
		}
	}

	/**
	 * Find a new name that isn't among the used names.
	 * @param prefix
	 * @param usedNames
	 * @return
	 */
	public static String unusedName(String prefix, Iterator usedNames) {
		SortedSet u = new TreeSet();
		while (usedNames.hasNext()) {
			String curr = (String) usedNames.next();
			if (curr.startsWith(prefix)) {
				String postfix = curr.substring(prefix.length());
				u.add(postfix);
			}
		}
		int count = 0;
		while (u.contains(String.valueOf(count))) {
			count++;
		}
		return prefix + count;
	}

	/**
	 * @return
	 */
	public Node getSelectedNode() {
		int[] s = getTree().getSelectionRows();
		if (s == null || s.length == 0) {
			return null;
		} else {
			return (Node) getTree().getPathForRow(s[0]).getLastPathComponent();
		}
	}
}
