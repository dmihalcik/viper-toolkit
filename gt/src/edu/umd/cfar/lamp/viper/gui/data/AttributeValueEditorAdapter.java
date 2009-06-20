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

package edu.umd.cfar.lamp.viper.gui.data;

import javax.swing.*;

import viper.api.*;
import viper.api.time.*;
import edu.umd.cfar.lamp.viper.gui.core.*;


/**
 * @author davidm@cfar.umd.edu
 */
public abstract class AttributeValueEditorAdapter extends AbstractCellEditor {
	protected Node n;
	private ViperViewMediator mediator;
	
	public ViperViewMediator getMediator() {
		return mediator;
	}
	public boolean isAttribute() {
		return n instanceof Attribute;
	}
	public boolean isAttrConfig() {
		return n instanceof AttrConfig;
	}
	public AttrConfig getAttrConfig() {
		if (isAttribute()) {
			return ((Attribute) n).getAttrConfig();
		} else {
			return (AttrConfig) n;
		}
	}
	public Attribute getAttribute() {
		if (isAttrConfig()) {
			throw new IllegalArgumentException("This represents an Attribute config, not an attribute instance");
		} else {
			return (Attribute) n;
		}
	}
	public void setAttributeValue(Object val) {
		if (n instanceof AttrConfig) {
			((AttrConfig) n).getEditor().setDefaultVal(val);
		} else if (n instanceof Attribute) {
			Attribute a = (Attribute) n;
			getMediator().setAttributeValueAtCurrentFrame(val, a);
		}
	}
	public Object getAttributeValue() {
		if (isAttrConfig()) {
			return getAttrConfig().getDefaultVal();
		} else if (isAttribute()) {
			Attribute a = getAttribute();
			if (null != mediator) {
				Sourcefile sf = (Sourcefile) a.getParent().getParent();
				String focusFile = mediator.getFocalFile();
				AttrConfig ac = a.getAttrConfig();
				if (sf.getReferenceMedia().getSourcefileName().equals(focusFile) && ac.isDynamic()) {
					Instant moment = mediator.getMajorMoment();
					if (moment != null) {
						return a.getAttrValueAtInstant(moment);
					}
				}
			}
			return a.getAttrValue();
		} else {
			return null;
		}
	}
	
	/**
	 * @return
	 */
	public Node getNode() {
		return n;
	}

	/**
	 * @param mediator
	 */
	public void setMediator(ViperViewMediator mediator) {
		this.mediator = mediator;
	}

	/**
	 * @param node
	 */
	public void setNode(Node node) {
		n = node;
	}
}
