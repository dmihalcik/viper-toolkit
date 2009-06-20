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
import edu.umd.cfar.lamp.chronicle.*;


class VConfigTimeLine extends ViperNodeTimeLine {
	private Config type;
	private Sourcefile s;
	private List children;
	private MultipleRange r;

	public void setViewModel(ViperChronicleModel viewModel) {
		super.setViewModel(viewModel);
		children = null;
	}
	public Node getNode() {
		return type;
	}

	private void resetKids() {
		this.children.clear();
		for (Iterator iter = s.getDescriptorsBy(type);
			iter.hasNext();
			) {
			VDescriptorTimeLine n =
				new VDescriptorTimeLine((Descriptor) iter.next());
			n.setViewModel(this.getViewModel());
			n.setSelectionModel(this.getSelectionModel());
			this.children.add(n);
		}
		TemporalRange[] kidRanges = new TemporalRange[this.children.size()];
		int count = 0;
		for(Iterator iter = this.children.iterator(); iter.hasNext(); ) {
			TimeLine curr = (TimeLine) iter.next();
			kidRanges[count++] = curr.getMyRange();
		}
		this.r = new MultipleRange(kidRanges);
	}

	public VConfigTimeLine(Sourcefile s, Config type) {
		this.type = type;
		this.s = s;
	}
	public TemporalRange getMyRange() {
		return this.r;
	}
	public Iterator getChildren() {
		if (this.children == null) {
			this.children = new LinkedList();
			resetKids();
		}
		return this.children.iterator();
	}
	public int getNumberOfChildren() {
		getChildren();
		return children.size();
	}
	public String getTitle() {
		return this.type.getDescName();
	}
	public String getSingularName() {
		return "descriptor type";
	}
	public String getPluralName() {
		return "descriptor types";
	}
	/**
	 * @return
	 */
	public Config getConfig() {
		return type;
	}

	public boolean equals(Object o) {
		if (this == o) {
			return true;
		} else if (o instanceof VConfigTimeLine) {
			VConfigTimeLine that = (VConfigTimeLine) o;
			return that.type.equals(this.type) && that.s.equals(this.s);
		} else {
			return false;
		}
	}

	public int hashCode() {
		return this.type.hashCode() ^ this.s.hashCode();
	}
}