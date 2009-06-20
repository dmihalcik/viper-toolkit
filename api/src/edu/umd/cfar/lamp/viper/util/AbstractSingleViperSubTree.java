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
 * A simple, chain-type subtree. It implements the iterator methods using the
 * getFirst methods.
 */
public abstract class AbstractSingleViperSubTree extends AbstractViperSubTree {
	/**
	 * {@inheritDoc}
	 * 
	 * @return <code>false</code>, as only one is ever allowed
	 */
	public boolean isMultipleSelectionAllowedOn(Class type) {
		return false;
	}
	private Iterator smartShortIterator(Object o) {
		if (o == null) {
			return Collections.EMPTY_SET.iterator();
		} else {
			return Collections.singleton(o).iterator();
		}
	}

	/**
	 * {@inheritDoc}
	 */
	public Iterator getSourcefiles() {
		return smartShortIterator(getFirstSourcefile());
	}

	/**
	 * {@inheritDoc}
	 */
	public Iterator getConfigs() {
		return smartShortIterator(getFirstConfig());
	}

	/**
	 * {@inheritDoc}
	 */
	public Iterator getAttrConfigs() {
		return smartShortIterator(getFirstAttrConfig());
	}
	/**
	 * {@inheritDoc}
	 */
	public Iterator getDescriptors() {
		return smartShortIterator(getFirstDescriptor());
	}
	/**
	 * {@inheritDoc}
	 */
	public Iterator getAttributes() {
		return smartShortIterator(getFirstAttribute());
	}

	/**
	 * {@inheritDoc}
	 */
	public boolean isSelected(Node n) {
		if (n == null) {
			return false;
		} else {
			boolean fBy = isFilteredBy(n.getClass());
			if (n instanceof Attribute) {
				Attribute a = (Attribute) n;
				if (getFirstAttribute() == null) {
					return !fBy && isSelected(a.getAttrConfig())
							&& isSelected(a.getDescriptor());
				} else {
					return getFirstAttribute().equals(a);
				}
			} else if (n instanceof AttrConfig) {
				AttrConfig ac = (AttrConfig) n;
				if (getFirstAttrConfig() == null) {
					return !fBy && isSelected(ac.getParent());
				} else {
					return getFirstAttrConfig().equals(ac);
				}
			} else if (n instanceof Config) {
				Config c = (Config) n;
				if (getFirstConfig() == null) {
					return !fBy;
				} else {
					return getFirstConfig().equals(c);
				}
			} else if (n instanceof Descriptor) {
				Descriptor d = (Descriptor) n;
				if (getFirstDescriptor() == null) {
					return !fBy && isSelected(d.getParent())
							&& isSelected(d.getConfig());
				} else {
					return getFirstDescriptor().equals(d);
				}
			} else if (n instanceof Sourcefile) {
				Sourcefile s = (Sourcefile) n;
				if (getFirstSourcefile() == null) {
					return !fBy;
				} else {
					return getFirstSourcefile().equals(s);
				}
			}
		}
		return true;
	}
}