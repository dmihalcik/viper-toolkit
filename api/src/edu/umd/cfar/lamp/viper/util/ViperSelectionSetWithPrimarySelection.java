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

/**
 * An extension to the 'set' type of selection to include information
 * about which node in the viper tree was the last
 * one to be added to the set.
 */
public class ViperSelectionSetWithPrimarySelection extends ViperSelectionSet {
	private DefaultSingleViperSubTree primary;
	
	/** @inheritDoc */
	public void setRoot(ViperData root) {
		primary.setRoot(root);
		super.setRoot(root);
	}
	
	/**
	 * Creates an empty selection set with the primary selection
	 * empty, as well.
	 */
	public ViperSelectionSetWithPrimarySelection() {
		primary = new DefaultSingleViperSubTree();
	}
	
	/** @inheritDoc */
	public void setTo(Node node) {
		if (node == null) {
			clear();
			return;
		}
		primary.setConfig(null);
		primary.selectPathTo(node);
		super.setTo(node);
	}
	
	/**
	 * Gets the primary selection.
	 * @return
	 */
	public DefaultSingleViperSubTree getPrimary() {
		return primary;
	}
	
	
	
	/** @inheritDoc */
	public void setTo(Set set) {
		Iterator iter = set.iterator();
		if (iter.hasNext()) {
			primary.selectPathTo((Node) iter.next());
		}
		super.setTo(set);
	}
	
	/** @inheritDoc */
	public void setTo(ViperSelectionSet set) {
		if (set instanceof ViperSelectionSetWithPrimarySelection) {
			ViperSelectionSetWithPrimarySelection other = (ViperSelectionSetWithPrimarySelection) set;
			primary.selectPathTo(other.getPrimary().getLastSelectedNode());
		} else {
			// XXX the selection's primary selection should always point to something reasonable
			primary.selectPathTo(null);
		}
		super.setTo(set);
	}

	/** @inheritDoc */
	public void addNode(Node n) {
		primary.selectPathTo(n);
		super.addNode(n);
	}

	/** @inheritDoc */
	public void clear() {
		primary.setConfig(null);
		super.clear();
	}
	
	/** @inheritDoc */
	public void removeNode(Node n) {
		primary.selectPathTo(n.getParent());
		super.removeNode(n);
	}

	public boolean equals(Object obj) {
		if (!super.equals(obj)) {
			return false;
		}
		if (!(obj instanceof ViperSelectionSetWithPrimarySelection)) {
			return false;
		}
		ViperSelectionSetWithPrimarySelection o = (ViperSelectionSetWithPrimarySelection) obj;
		return o.getPrimary().equals(primary);
	}

	public int hashCode() {
		return super.hashCode() ^ getPrimary().hashCode();
	}

	public Object clone() {
		ViperSelectionSetWithPrimarySelection o = (ViperSelectionSetWithPrimarySelection) super.clone();
		o.primary = (DefaultSingleViperSubTree) o.primary.clone();
		return o;
	}
	
}
