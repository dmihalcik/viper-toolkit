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

package edu.umd.cfar.lamp.viper.gui.chronology;

import java.util.*;

import org.apache.commons.lang.*;

import viper.api.*;
import viper.api.datatypes.*;
import viper.api.time.*;


public class VDescriptorTimeLine extends ViperNodeTimeLine  {
	private Descriptor my;
	private Attribute nameAttr;
	private List children;
	private InstantRange userRange;

	public Node getNode() {
		return my;
	}

	public VDescriptorTimeLine(Descriptor d) {
		this.my = d;
		for (Iterator iter = getDescriptor().getAttributes(); iter.hasNext();) {
			Attribute a = (Attribute) iter.next();
			AttrConfig ac = a.getAttrConfig();
			if (ac.isDynamic()) {
//				String attrType = ac.getAttrType();
//				if (attrType.equals(ViperDataFactoryImpl.LVALUE) || attrType.equals(ViperDataFactoryImpl.BVALUE)) {
//					VNominalAttribute kid = new VNominalAttribute(a);
//					kid.setViewModel(this.getViewModel());
//					kid.setSelectionModel(this.getSelectionModel());
//					children.add(kid);
//				}
			} else if (nameAttr == null && ac.getAttrType().equals(ViperDataFactoryImpl.SVALUE)) {
				nameAttr = a;
			}
		}
	}
	public TemporalRange getMyRange() {
		if (userRange != null) {
			return userRange;
		}
		TemporalRange r = my.getRange();
		if (r == null) {
			r = new InstantRange();
		}
		return r;
	}

	public void setViewModel(ViperChronicleModel viewModel) {
		super.setViewModel(viewModel);
		children = null;
	}
	public int getNumberOfChildren() {
		getChildren();
		return children.size();
	}
	public Iterator getChildren() {
		if (children == null) {
			children = new LinkedList();
//			for (Iterator iter = getDescriptor().getAttributes(); iter.hasNext();) {
//				Attribute a = (Attribute) iter.next();
//				AttrConfig ac = a.getAttrConfig();
//				if (ac.isDynamic()) {
//					String attrType = ac.getAttrType();
//					if (attrType.equals(ViperDataFactoryImpl.LVALUE) || attrType.equals(ViperDataFactoryImpl.BVALUE)) {
//						VNominalAttribute kid = new VNominalAttribute(a);
//						kid.setViewModel(this.getViewModel());
//						kid.setSelectionModel(this.getSelectionModel());
//						children.add(kid);
//					}
//				} else if (ac.getAttrType().equals(ViperDataFactoryImpl.SVALUE) && nameAttr == null) {
//					nameAttr = a;
//				}
//			}
		}
		return children.iterator();
	}

	public String getTitle() {
		if (nameAttr != null) {
			String s = (String) nameAttr.getAttrValue();
			if (s != null && s.length() > 0) {
				return String.valueOf(my.getDescId()) + " (" + nameAttr.getAttrValue() + ")";
			}
		}
		return String.valueOf(my.getDescId());
	}
	public String getSingularName() {
		return "descriptor";
	}
	public String getPluralName() {
		return "descriptors";
	}
	/**
	 * @return
	 */
	public Descriptor getDescriptor() {
		return my;
	}

	public boolean equals(Object o) {
		if (this == o) {
			return true;
		} else if (o instanceof VDescriptorTimeLine) {
			VDescriptorTimeLine that = (VDescriptorTimeLine) o;
			return that.my.equals(this.my);
		} else {
			return false;
		}
	}

	public int hashCode() {
		return my.hashCode();
	}

	public InstantRange getUserRange() {
		return userRange;
	}
	public void setUserRange(InstantRange userRange) {
		if (!ObjectUtils.equals(userRange, this.userRange)) {
			TemporalRange oldRange = getMyRange();
			this.userRange = userRange;
			ViperChronicleModel vm = getViewModel();
			if (vm != null && vm.indexOf(this) >= 0 && !ObjectUtils.equals(userRange, oldRange)) {
				vm.fireDataChanged(null);
			}
		}
	}
	public Attribute getNameAttr() {
		return nameAttr;
	}
	public void setNameAttr(Attribute nameAttr) {
		this.nameAttr = nameAttr;
	}
}