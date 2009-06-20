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

import viper.api.*;
import viper.api.time.*;


class VAttributeTimeLine extends ViperNodeTimeLine  {
	private Attribute my;
	
	public Node getNode() {
		return my;
	}

	public VAttributeTimeLine(Attribute a) {
		this.my = a;
	}
	public TemporalRange getMyRange() {
		TemporalRange r = my.getRange();
		if (r == null) {
			r = new InstantRange();
		}
		return r;
	}
	public Iterator getChildren() {
		return Collections.EMPTY_LIST.iterator();
	}
	public int getNumberOfChildren() {
		return 0;
	}
	public String getTitle() {
		return my.getAttrName();
	}
	public String getSingularName() {
		return "attribute";
	}
	public String getPluralName() {
		return "attributes";
	}
	public Attribute getAttribute() {
		return my;
	}
}