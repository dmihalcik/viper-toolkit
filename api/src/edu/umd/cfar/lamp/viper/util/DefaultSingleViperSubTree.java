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

import java.lang.ref.*;

import viper.api.*;
import viper.api.extensions.*;

/**
 * The default single subtree is a simple chain selection, where
 * the nodes that are descendents of the chain are all
 * marked as selected. To test to see if an element is in the chain
 * or just a descendant of it, use the {@link #isFilteredBy(Class)}
 * method. 
 */
public class DefaultSingleViperSubTree extends AbstractSingleViperSubTree {
	private ViperData root;
	private Sourcefile sourcefile;
	private Config config;
	private AttrConfig attrConfig;
	private Descriptor descriptor;
	private Attribute attribute;
	private static class WatchForRemovals implements NodeListener {
		WeakReference outer;
		WatchForRemovals(DefaultSingleViperSubTree outer) {
			this.outer = new WeakReference(outer);
		}
		private void tryToRemoveDetachedNodes() {
			DefaultSingleViperSubTree outer = (DefaultSingleViperSubTree) this.outer.get();
			if (outer != null) {
				outer.removeDetachedNodes();
			}
		}
		/**
		 * @see viper.api.extensions.NodeListener#nodeChanged(viper.api.extensions.NodeChangeEvent)
		 */
		public void nodeChanged(NodeChangeEvent nce) {
			tryToRemoveDetachedNodes();
		}
		/**
		 * @see viper.api.extensions.NodeListener#minorNodeChanged(viper.api.extensions.MinorNodeChangeEvent)
		 */
		public void minorNodeChanged(MinorNodeChangeEvent mnce) {
			tryToRemoveDetachedNodes();
		}
		/**
		 * @see viper.api.extensions.NodeListener#majorNodeChanged(viper.api.extensions.MajorNodeChangeEvent)
		 */
		public void majorNodeChanged(MajorNodeChangeEvent mnce) {
			tryToRemoveDetachedNodes();
		}
	}
	private NodeListener vnl;
	private Node lastSelectedNode;
	
	/**
	 * Creates an empty subtree with no root, so nothing is selected.
	 */
	public DefaultSingleViperSubTree() {
		super();
	}
	/**
	 * Creates an empty subtree with the specified root, 
	 * so everything is selected and nothing is filtered by.
	 * @param root the root of the tree to select from
	 */
	public DefaultSingleViperSubTree(ViperData root) {
		super();
		this.setRoot(root);
	}
	
	
	/**
	 * Sets the root of the selection. Note: this does not fire
	 * a change event.
	 * @param root the root of the tree that this selects from
	 */
	public void setRoot(ViperData root) {
		if (this.root == root) {
			return;
		}
		if (this.root instanceof EventfulNode) {
			((EventfulNode) this.root).removeNodeListener(vnl);
		}
		this.root = root;
		lastSelectedNode = null;
		if (root instanceof EventfulNode) {
			if (null == vnl) {
				vnl = new WatchForRemovals(this);
			}
			((EventfulNode) this.root).addNodeListener(vnl);
		}
	}
	
	
	protected void finalize() throws Throwable {
		setRoot(null); // cleans up listener
		super.finalize();
	}
	
	/**
	 * Cleans out the selection tree, removing nodes that have been detached.
	 */
	private void removeDetachedNodes() {
		boolean changed = false;
		DefaultSingleViperSubTree removed = new DefaultSingleViperSubTree(null);
		if (sourcefile != null && sourcefile.getRoot() != root) {
			removed.sourcefile = sourcefile;
			changed = true;
		}
		if (descriptor != null && descriptor.getRoot() != root) {
			removed.descriptor = descriptor;
			changed = true;
		}
		if (attribute != null && attribute.getRoot() != root) {
			removed.attribute = attribute;
			changed = true;
		}
		if (config != null && config.getRoot() != root) {
			removed.config = config;
			changed = true;
		}
		if (attrConfig != null && attrConfig.getRoot() != root) {
			removed.attrConfig = attrConfig;
			changed = true;
		}
		if (changed) {
			DefaultSingleViperSubTree added = new DefaultSingleViperSubTree(null);
			applyChanges(removed, added);
			fireSelectionChanged(new ViperSubTreeChangedEvent(this, removed, added));
		}
	}
	
	/**
	 * @see edu.umd.cfar.lamp.viper.util.ViperSubTree#getRoot()
	 */
	public ViperData getRoot() {
		return root;
	}

	/**
	 * @see edu.umd.cfar.lamp.viper.util.AbstractViperSubTree#isFilteredBy(java.lang.Class)
	 */
	public boolean isFilteredBy(Class type) {
		if (super.isFilteredBy(type)) {
			if (Sourcefile.class.isAssignableFrom(type)) {
				return sourcefile != null;
			} else if (Config.class.isAssignableFrom(type)) {
				return config != null;
			} else if (AttrConfig.class.isAssignableFrom(type)) {
				return attrConfig != null;
			} else if (Descriptor.class.isAssignableFrom(type)) {
				return descriptor != null;
			} else if (Attribute.class.isAssignableFrom(type)) {
				return attribute != null;
			} else { // ViperData, Sourcefiles or Configs node
				return false;
			}
		}
		return false;
	}

	/**
	 * @see edu.umd.cfar.lamp.viper.util.ViperSubTree#getFirstSourcefile()
	 */
	public Sourcefile getFirstSourcefile() {
		return sourcefile;
	}
	/**
	 * @see edu.umd.cfar.lamp.viper.util.ViperSubTree#getFirstConfig()
	 */
	public Config getFirstConfig() {
		return config;
	}
	/**
	 * @see edu.umd.cfar.lamp.viper.util.ViperSubTree#getFirstAttrConfig()
	 */
	public AttrConfig getFirstAttrConfig() {
		return attrConfig;
	}
	/**
	 * @see edu.umd.cfar.lamp.viper.util.ViperSubTree#getFirstDescriptor()
	 */
	public Descriptor getFirstDescriptor() {
		return descriptor;
	}
	/**
	 * @see edu.umd.cfar.lamp.viper.util.ViperSubTree#getFirstAttribute()
	 */
	public Attribute getFirstAttribute() {
		return attribute;
	}
	
	private static boolean areDifferent (Node a, Node b) {
		if (a == b) {
			return false;
		} else if (a == null) {
			return true; // a != b, and a== null, therefore b must != null
		} else if (a.equals(b)) {
			return areDifferent(a.getParent(), b.getParent());
		}
		return true;
	}
	
	private void applyChanges(DefaultSingleViperSubTree removed, DefaultSingleViperSubTree added) {
		// I miss C++, where this could be an array or a struct
		writeLock();
		try {
			if (added.sourcefile != null) {
				sourcefile = added.sourcefile;
			} else if (removed.sourcefile != null) {
				sourcefile = null;
			}
			if (added.config != null) {
				config = added.config;
			} else if (removed.config != null) {
				config = null;
			}
			if (added.attrConfig != null) {
				attrConfig = added.attrConfig;
			} else if (removed.attrConfig != null) {
				attrConfig = null;
			}
			if (added.descriptor != null) {
				descriptor = added.descriptor;
			} else if (removed.descriptor != null) {
				descriptor = null;
			}
			if (added.attribute != null) {
				attribute = added.attribute;
			} else if (removed.attribute != null) {
				attribute = null;
			}
		} finally {
			writeUnlock();
		}
	}
	
	/**
	 * Sets the attribute config to select on. 
	 * Selecting <code>null</code> removes the current
	 * attribute schema node selection.
	 * @param config the attribute config to filter on,
	 *  or <code>null</code> if none is to be filtered on
	 */
	public void setAttrConfig(AttrConfig config) {
		if (areDifferent(config, this.attrConfig)) {
			lastSelectedNode = config;
			DefaultSingleViperSubTree removed = new DefaultSingleViperSubTree(null);
			DefaultSingleViperSubTree added = new DefaultSingleViperSubTree(null);
			added.attrConfig = config;
			added.config = (config == null) ? this.config : (Config) config.getParent();
			
			removed.attrConfig = this.attrConfig;
			removed.config = this.config;
			if (this.descriptor != null && !this.descriptor.getConfig().equals(added.config)) {
				removed.descriptor = this.descriptor;
			}
			removed.attribute = this.attribute;
			applyChanges(removed, added);
			fireSelectionChanged(new ViperSubTreeChangedEvent(this, removed, added));
		}
	}
	
	private void copyInto(DefaultSingleViperSubTree target) {
		target.writeLock();
		try {
			target.sourcefile = this.sourcefile;
			target.config = this.config;
			target.descriptor = this.descriptor;
			target.attrConfig = this.attrConfig;
			target.attribute = this.attribute;
		} finally {
			target.writeUnlock();
		}
	}

	/**
	 * Select the path to the given node. This works
	 * by calling the approprite <code>set<i>NodeType</i></code>
	 * method, so it won't remove part of the chain that
	 * is beneath it in the tree.
	 * @param n the node to select to
	 */
	public void selectPathTo(Node n) {
		if (n instanceof Attribute) {
			setAttribute((Attribute) n);
		} else if (n instanceof AttrConfig) {
			setAttrConfig((AttrConfig) n);
		} else if (n instanceof Descriptor) {
			setDescriptor((Descriptor) n);
		} else if (n instanceof Config) {
			setConfig((Config) n);
		} else if (n instanceof Sourcefile) {
			setSourcefile((Sourcefile) n);
		} else if (n instanceof ViperData) {
			setRoot((ViperData) n);
		} else {
			setSourcefile(null);
		}
	}
	
	/**
	 * Sets the attribute to select on. 
	 * Selecting <code>null</code> removes the current
	 * attribute selection.
	 * @param attribute the attribute to filter on,
	 *  or <code>null</code> if none is to be filtered on
	 */
	public void setAttribute(Attribute attribute) {
		if (areDifferent(attribute, this.attribute)) {
			lastSelectedNode = attribute;
			DefaultSingleViperSubTree removed = new DefaultSingleViperSubTree(null);
			DefaultSingleViperSubTree added = new DefaultSingleViperSubTree(null);
			copyInto(removed);
			if (attribute != null) {
				added.attribute = attribute;
				added.descriptor = (Descriptor) attribute.getParent();
				added.attrConfig = attribute.getAttrConfig();
				added.config = added.descriptor.getConfig();
				added.sourcefile = added.descriptor.getSourcefile();
			} else {
				copyInto(added);
				added.attribute = null;
			}
			applyChanges(removed, added);
			fireSelectionChanged(new ViperSubTreeChangedEvent(this, removed, added));
		}
	}

	/**
	 * Sets the descriptor config to select on. 
	 * Selecting <code>null</code> removes the current
	 * config selection.
	 * @param config the descriptor config to filter on,
	 *  or <code>null</code> if none is to be filtered on
	 */
	public void setConfig(Config config) {
		if (areDifferent(config, this.config)) {
			lastSelectedNode = config;
			DefaultSingleViperSubTree removed = new DefaultSingleViperSubTree(null);
			DefaultSingleViperSubTree added = new DefaultSingleViperSubTree(null);
			copyInto(removed);
			removed.sourcefile = null;
			added.config = config;
			applyChanges(removed, added);
			fireSelectionChanged(new ViperSubTreeChangedEvent(this, removed, added));
		}
	}

	/**
	 * Sets the descriptor to select on. 
	 * Selecting <code>null</code> removes the current
	 * descriptor selection.
	 * @param descriptor the descriptor to filter on,
	 *  or <code>null</code> if none is to be filtered on
	 */
	public void setDescriptor(Descriptor descriptor) {
		if (areDifferent(descriptor, this.descriptor)) {
			lastSelectedNode = descriptor;
			DefaultSingleViperSubTree removed = new DefaultSingleViperSubTree(null);
			DefaultSingleViperSubTree added = new DefaultSingleViperSubTree(null);
			removed.descriptor = this.descriptor;
			removed.attribute = this.attribute;
			added.descriptor = descriptor;
			if (descriptor != null) {
				if (!descriptor.getConfig().equals(this.config)) {
					removed.config = this.config;
					added.config = descriptor.getConfig();
					removed.attrConfig = this.attrConfig;
				} else if (this.attrConfig != null) {
					added.attribute = descriptor.getAttribute(this.attrConfig);
				}
				if (!descriptor.getSourcefile().equals(this.sourcefile)) {
					removed.sourcefile = this.sourcefile;
					added.sourcefile = descriptor.getSourcefile();
				}
			}
			applyChanges(removed, added);
			fireSelectionChanged(new ViperSubTreeChangedEvent(this, removed, added));
		}
	}

	/**
	 * Sets the sourcefile to select on. 
	 * Selecting <code>null</code> removes the current
	 * sourcefile selection.
	 * @param sourcefile the sourcefile to filter on,
	 *  or <code>null</code> if none is to be filtered on
	 */
	public void setSourcefile(Sourcefile sourcefile) {
		if (areDifferent(sourcefile, this.sourcefile)) {
			lastSelectedNode = sourcefile;
			DefaultSingleViperSubTree removed = new DefaultSingleViperSubTree(null);
			DefaultSingleViperSubTree added = new DefaultSingleViperSubTree(null);
			copyInto(removed);
			removed.attrConfig = null;
			removed.config = null;
			added.sourcefile = sourcefile;
			applyChanges(removed, added);
			fireSelectionChanged(new ViperSubTreeChangedEvent(this, removed, added));
		}
	}
	public Node getLastSelectedNode() {
		return lastSelectedNode;
	}
}
