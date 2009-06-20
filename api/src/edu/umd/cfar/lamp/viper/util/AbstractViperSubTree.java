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

import javax.swing.event.*;

import viper.api.*;

/**
 * Implements some of the more annoyingly repetative aspects of
 * the ViperSubTree interface.
 */
public abstract class AbstractViperSubTree extends AbstractViperSelection implements ViperSubTree {
	/**
	 * This should be used instead of fireChangeEvent.
	 * @param e
	 */
	public void fireSelectionChanged(ViperSubTreeChangedEvent e) {
		super.fireChangeEvent(e);
	}

	
	
	/**
	 * @see edu.umd.cfar.lamp.viper.util.ViperSubTree#isFilteredBy(java.lang.Class)
	 */
	public boolean isFilteredBy(Class type) {
		if (Node.class.isAssignableFrom(type)) {
			return true;
		}
		throw new IllegalArgumentException("Not an attribute type: " );
	}

	/**
	 * @see edu.umd.cfar.lamp.viper.util.AbstractViperSelection#fireChangeEvent(javax.swing.event.ChangeEvent)
	 */
	public void fireChangeEvent(ChangeEvent e) {
		assert e instanceof ViperSubTreeChangedEvent;
		super.fireChangeEvent(e);
	}

	/**
	 * Prints out the subtree as a chain.
	 * @return subtree in the form of <code>[ {nodetype: nodes}* ]</code>
	 */
	public String toString() {
		StringBuffer sb = new StringBuffer().append("[");
		final Class[] classes = new Class[] {Sourcefile.class, Config.class, AttrConfig.class, Descriptor.class, Attribute.class};
		for (int i = 0; i < classes.length; i++) {
			if (isFilteredBy(classes[i])) {
				Iterator iter = getSelectedBy(classes[i]);
				sb.append(" {").append(classes[i].getName()).append(":");
				while(iter.hasNext()) {
					sb.append(" ").append(iter.next());
				}
				sb.append("}");
			}
		}
		sb.append(" ]");
		return sb.toString();
	}
	
	/** @inheritDoc */
	public Iterator getSelectedBy(Class c) {
		if (Attribute.class.equals(c)) {
			return getAttributes();
		}
		if (AttrConfig.class.equals(c)) {
			return getAttrConfigs();
		}
		if (Descriptor.class.equals(c)) {
			return getDescriptors();
		}
		if (Config.class.equals(c)) {
			return getConfigs();
		}
		if (Sourcefile.class.equals(c)) {
			return getSourcefiles();
		}
		throw new IllegalArgumentException("Not a filterable node type: " + c);
	}

	/** @inheritDoc */
	public boolean isEmpty() {
		return getFirstSourcefile() == null;
	}
}
