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

import java.util.*;

import javax.swing.*;
import javax.swing.event.*;
import javax.swing.tree.*;

import viper.api.*;
import viper.api.extensions.*;
import viper.api.impl.*;
import edu.umd.cfar.lamp.viper.gui.core.*;

/**
 * @author davidm@cfar.umd.edu
 * @since Jun 9, 2003
 */
public class ViperTreeModel implements TreeModel {
	private Configs root;

	private ViperViewMediator mediator;

	private MediatorListener listener;

	private ConfigEditor ed;

	private Set treesToSelect;

	private Node[] nodesChangedBy(ViperChangeEvent e) {
		if (e instanceof MajorNodeChangeEvent) {
			Iterator iter = ((MajorNodeChangeEvent) e).getSubEvents();
			Set changedNodes = new HashSet();
			while (iter.hasNext()) {
				changedNodes
						.addAll(Arrays
								.asList(nodesChangedBy((ViperChangeEvent) iter
										.next())));
			}
			changedNodes.remove(root);
			if (!changedNodes.isEmpty()) {
				return (Node[]) changedNodes.toArray(new Node[changedNodes
						.size()]);
			}
		} else if (e != null) {
			Node n = e.getParent();
			if (Util.isChildOf(root, n)) {
				int[] idxs = e.getIndexes();
				if (idxs.length == 1 && n.getNumberOfChildren() > idxs[0]
						&& 0 <= idxs[0]) {
					return new Node[] { n.getChild(idxs[0]) };
				}
				return new Node[] { n };
			}
		}
		return new Node[] { root };
	}

	private Node toSelect = null;

	private Runnable updateSelection = new Runnable() {
		public void run() {
			if (toSelect == null || toSelect.getRoot() == null) {
				return;
			}
			TreePath pathToChange = new TreePath(
					getViperPathTo(toSelect));
			for (Iterator iter = treesToSelect.iterator(); iter.hasNext();) {
				JTree tree = (JTree) iter.next();
				if (true || tree.getSelectionCount() == 0) {
					int row = tree.getRowForPath(pathToChange);
					if (row < 0) {
						tree.expandPath(pathToChange.getParentPath());
						row = tree.getRowForPath(pathToChange);
					}
					tree.setSelectionRow(row);
				}
			}
//			toSelect = null;
		}
	};

	private Node[] getViperPathTo(Node n) {
		if (n instanceof Configs) {
			return new Node[] { n };
		} else if (n instanceof Config) {
			return new Node[] { n.getParent(), n };
		} else if (n instanceof AttrConfig) {
			return new Node[] { n.getParent().getParent(), n.getParent(), n };
		} else {
			throw new IllegalArgumentException("Not a valid schema tree node: "
					+ n);
		}
	}

	public void enqueueNodeSelection(Node toSelect) {
		if (toSelect != null && toSelect != root) {
			this.toSelect = toSelect;
			SwingUtilities.invokeLater(updateSelection);
		}
	}

	private class MediatorListener implements ViperMediatorChangeListener {
		public void schemaChanged(ViperMediatorChangeEvent e) {
			Configs cs = mediator.getViperData().getConfigsNode();
			if (cs != root) {
				root = cs;
			}
			Node n = toSelect == null ? (Node) ed.getSheet().getObject() : toSelect;
			ed.resetListOfExpandedNodes();
			fireStructureChangeEventFromNode(root);
			reExpandExpandedNodes();
			enqueueNodeSelection(n);
		}

		public void currFileChanged(ViperMediatorChangeEvent e) {
		}

		public void mediaChanged(ViperMediatorChangeEvent e) {
		}

		public void frameChanged(ViperMediatorChangeEvent e) {
		}

		public void dataChanged(ViperMediatorChangeEvent e) {
		}
	}

	public ViperTreeModel(ConfigEditor ed) {
		listener = new MediatorListener();
		this.ed = ed;
	}

	public Object getRoot() {
		return root;
	}

	public int getChildCount(Object n) {
		return ((Node) n).getNumberOfChildren();
	}

	public boolean isLeaf(Object n) {
		return (n instanceof Attribute) || (n instanceof AttrConfig);
	}

	private Set listeners = new HashSet();

	public void addTreeModelListener(TreeModelListener tml) {
		listeners.add(tml);
	}

	public void removeTreeModelListener(TreeModelListener tml) {
		listeners.remove(tml);
	}

	public Object getChild(Object parent, int index) {
		return ((Node) parent).getChild(index);
	}

	public int getIndexOfChild(Object parent, Object child) {
		return ((Node) parent).indexOf((Node) child);
	}

	private static interface FireTreeEvent {
		public void fire(TreeModelListener l, TreeModelEvent e);
	}

	private void fireEvents(FireTreeEvent type, TreeModelEvent event) {
		for (Iterator iter = listeners.iterator(); iter.hasNext();) {
			type.fire((TreeModelListener) iter.next(), event);
		}
	}

	private static class FireNodeChanged implements FireTreeEvent {
		public void fire(TreeModelListener l, TreeModelEvent e) {
			l.treeNodesChanged(e);
		}
	}

	private static class FireNodeInserted implements FireTreeEvent {
		public void fire(TreeModelListener l, TreeModelEvent e) {
			l.treeNodesInserted(e);
		}
	}

	private static class FireNodeRemoved implements FireTreeEvent {
		public void fire(TreeModelListener l, TreeModelEvent e) {
			l.treeNodesRemoved(e);
		}
	}

	private static class FireStructureChanged implements FireTreeEvent {
		public void fire(TreeModelListener l, TreeModelEvent e) {
			l.treeStructureChanged(e);
		}
	}

	private static final FireTreeEvent FIRE_STRUCT_CHANGED = new FireStructureChanged();

	private static final FireTreeEvent FIRE_NODE_CHANGED = new FireNodeChanged();

	private static final FireTreeEvent FIRE_NODE_INSERT = new FireNodeInserted();

	private static final FireTreeEvent FIRE_NODE_REMOVE = new FireNodeRemoved();

	public void valueForPathChanged(TreePath path, Object newValue) {
		Object[] p = path.getPath();
		if (p.length == 1) {
			if (!newValue.equals(root)) {
				root = (Configs) newValue;
				fireEvents(FIRE_NODE_CHANGED, new TreeModelEvent(newValue, p));
			}
		} else if (p.length == 2) {
			Config nv = (Config) newValue;
			if (!root.hasChild(nv)) {
				fireEvents(FIRE_NODE_CHANGED, new TreeModelEvent(newValue, p));
			}
		} else if (p.length == 3) {
			// (AttrConfig) newValue has changed
		}
	}

	public ViperViewMediator getMediator() {
		return mediator;
	}

	/**
	 * @param mediator
	 */
	public void setMediator(ViperViewMediator mediator) {
		if (this.mediator != null) {
			this.mediator.removeViperMediatorChangeListener(listener);
		}
		this.mediator = mediator;
		resetRoot();
		if (this.mediator != null) {
			this.mediator.addViperMediatorChangeListener(listener);
		}
	}

	private void resetRoot() {
		Object r = root;
		r = root;
		if (mediator != null) {
			root = mediator.getViperData().getConfigsNode();
			r = root;
		}
		TreeModelEvent tme = new TreeModelEvent(ViperTreeModel.this,
				new TreePath(r));
		fireEvents(FIRE_STRUCT_CHANGED, tme);
	}

	public void addTreeToSelect(JTree tree) {
		if (this.treesToSelect == null)
			this.treesToSelect = new HashSet();
		this.treesToSelect.add(tree);
	}

	public void removeTreeToSelect(JTree tree) {
		if (this.treesToSelect != null) {
			this.treesToSelect.remove(tree);
			if (this.treesToSelect.isEmpty()) {
				this.treesToSelect = null;
			}
		}
	}

	/**
	 * @param curr
	 */
	private void fireStructureChangeEventFromNode(Node curr) {
		Node[] path = getViperPathTo(curr);
		TreeModelEvent tme = new TreeModelEvent(
				ViperTreeModel.this, path); // XXX = this isn't
											// right
		fireEvents(FIRE_STRUCT_CHANGED, tme);
		if (curr instanceof Config) {
			ed.getTree().expandPath(new TreePath(path));
		}
	}

	/**
	 * 
	 */
	private void reExpandExpandedNodes() {
		Config[] expandeds = ed.getCopyOfExpandedNodes();
		for (int i = 0; i < expandeds.length; i++) {
			Config c = expandeds[i];
			if (!root.hasChild(c)) {
				ed.nodeRemoved(c);
			} else {
				TreePath tp = new TreePath(new Object[] { root, c });
				ed.getTree().expandPath(tp);
			}
		}
	}

}