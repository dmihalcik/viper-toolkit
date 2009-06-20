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

package edu.umd.cfar.lamp.viper.util;

import java.util.*;

import viper.api.*;
import viper.api.extensions.*;
import viper.api.impl.*;

/**
 * @author davidm
 * 
 */
public class ViperSelectionSet extends AbstractViperSelection {
	private Set selections;
	private ViperData root;

	private static class NodeBox {
		private Node n;
		NodeBox(Node n) {
			this.n = n;
		}
		public boolean equals(Object o) {
			if (this == o) {
				return true;
			} else if (o instanceof NodeBox) {
				return ((NodeBox) o).n == this.n;
			}
			return false;
		}
		public int hashCode() {
			return this.n.hashCode();
		}
	}

	/**
	 * Constructs an empty selection set.
	 */
	public ViperSelectionSet() {
		super();
		selections = new HashSet();
	}

	/** @inheritDoc */
	public boolean isSelected(Node n) {
		return selections.contains(new NodeBox(n));
	}

	/**
	 * Adds a new node to the set of selected nodes.
	 * 
	 * @param n
	 */
	public void addNode(Node n) {
		if (selections.add(new NodeBox(n))) {
			fireChangeEvent(null);
		}
	}

	/**
	 * Removes any selection.
	 */
	public void clear() {
		if (!selections.isEmpty()) {
			selections.clear();
			fireChangeEvent(null);
		}
	}

	/**
	 * Removes the given node from the set of selected nodes.
	 * 
	 * @param n
	 *            the node to remove
	 */
	public void removeNode(Node n) {
		if (selections.remove(new NodeBox(n))) {
			fireChangeEvent(null);
		}
	}

	/**
	 * Replaces the current set of selections with the given set.
	 * 
	 * @param set
	 */
	public void setTo(Set set) {
		boolean same = selections.size() == set.size();
		if (same) {
			Iterator iter = set.iterator();
			while (iter.hasNext()) {
				if (!isSelected((Node) iter.next())) {
					same = false;
					break;
				}
			}
		}
		if (!same) {
			selections.clear();
			Iterator iter = set.iterator();
			while (iter.hasNext()) {
				selections.add(new NodeBox((Node) iter.next()));
			}
			fireChangeEvent(null);
		}
	}
	
	public void setTo(ViperSelectionSet other) {
		if (this != other && !selections.equals(other.selections)) {
			root = other.root;
			selections.clear();
			selections.addAll(other.selections);
			fireChangeEvent(null);
		}
	}

	/**
	 * Sets to the single element selection 'node'
	 * 
	 * @param node
	 *            the node to select
	 */
	public void setTo(Node node) {
		if (node == null) {
			clear();
			return;
		}
		if (node.getRoot() != root) {
			throw new IllegalArgumentException("Not a node in the right tree");
		}
		if (selections.size() != 1 || !selections.contains(new NodeBox(node))) {
			selections.clear();
			selections.add(new NodeBox(node));
			fireChangeEvent(null);
		}
	}

	/** @inheritDoc */
	public boolean isEmpty() {
		return selections.isEmpty();
	}

	/**
	 * @return
	 */
	public ViperData getRoot() {
		return root;
	}

	/**
	 * @param root
	 */
	public void setRoot(ViperData root) {
		if (this.root != root) {
			if (this.root instanceof EventfulNode) {
				((EventfulNode) root).removeNodeListener(nl);
			}
			this.root = root;
			if (this.root instanceof EventfulNode) {
				((EventfulNode) root).addNodeListener(nl);
			}
			selections.clear();
			fireChangeEvent(null);
		}
	}

	void verifyList() {
		Set toRemove = null;
		Iterator iter = selections.iterator();
		while (iter.hasNext()) {
			NodeBox n = (NodeBox) iter.next();
			if (n.n.getRoot() != this.root) {
				if (toRemove == null) {
					toRemove = new HashSet();
				}
				toRemove.add(n);
			}
		}
		if (toRemove != null) {
			selections.removeAll(toRemove);
			fireChangeEvent(null);
		}
	}

	private NodeListener nl = new NodeListener() {
		public void nodeChanged(NodeChangeEvent nce) {
			verifyList();
		}
		public void minorNodeChanged(MinorNodeChangeEvent mnce) {
			verifyList();
		}
		public void majorNodeChanged(MajorNodeChangeEvent mnce) {
			verifyList();
		}
	};

	/**
	 * Returns the nodes in the selection set that are
	 * {@link Util#beneath(Node, Node) beneath}the given node in the tree
	 * 
	 * @param n
	 *            the common ancestor
	 * @return the selected nodes beneath the ancestor node
	 */
	public Iterator getSelectionBeneath(final Node n) {
		return new MappingIterator(new MappingIterator.MappingFunctor() {
			public Object map(Object o) {
				return ((NodeBox) o).n;
			}
		}, new ExceptIterator(new ExceptIterator.ExceptFunctor() {
			public boolean check(Object o) {
				return Util.beneath(n, ((NodeBox) o).n);
			}
		}, selections.iterator()));
	}

	public boolean equals(Object obj) {
		if (obj == this) {
			return true;
		} else if (!(obj instanceof ViperSelectionSet)) {
			return false;
		}
		ViperSelectionSet o = (ViperSelectionSet) obj;
		return o.root == this.root && o.selections.equals(o.selections);
	}

	public int hashCode() {
		return selections.hashCode() ^ root.hashCode();
	}

	public Object clone() {
		ViperSelectionSet copy = (ViperSelectionSet) super.clone();
		copy.selections = new HashSet();
		copy.selections.addAll(selections);
		return copy;
	}
	
	
}