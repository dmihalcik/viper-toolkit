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

import javax.swing.event.*;

import org.apache.commons.lang.*;

import viper.api.*;
import viper.api.time.*;
import edu.umd.cfar.lamp.chronicle.*;
import edu.umd.cfar.lamp.viper.gui.core.*;

/**
 * Reflects the mediator's selection as a chronicle selection.
 */
public class ViperChronicleSelectionModel
		extends
			AbstractChronicleSelectionModel {
	private ViperViewMediator mediator;
	private Node nodeWhoseTimeToSelect = null;
	
	public boolean isSelected(TimeLine tl) {
		if (this.mediator != null) {
			if (tl instanceof ViperNodeTimeLine) {
				ViperNodeTimeLine d = (ViperNodeTimeLine) tl;
				return mediator.getSelection().isSelected(d.getNode());
			}
		}
		return false;
	}
	public Iterator getSelectedLines() {
		throw new NotImplementedException("I'm really lazy - davidm");
	}
	public TemporalRange getSelectedTime() {
		if (nodeWhoseTimeToSelect instanceof TemporalNode) {
			return ((TemporalNode) nodeWhoseTimeToSelect).getRange();
		} else if (nodeWhoseTimeToSelect instanceof Config) {
			Sourcefile sf = mediator.getCurrFile();
			if (sf != null) {
				Iterator iter = sf.getDescriptorsBy((Config) nodeWhoseTimeToSelect);
				if (iter.hasNext()) {
					LinkedList ll = new LinkedList();
					while (iter.hasNext()) {
						ll.add(((Descriptor) iter.next()).getRange());
					}
					TemporalRange[] R = new TemporalRange[ll.size()];
					ll.toArray(R);
					return new MultipleRange(R);
				}
			}
		}
		return null;
	}
	
	/**
	 * @return Returns the mediator.
	 */
	public ViperViewMediator getMediator() {
		return mediator;
	}

	/**
	 * @param mediator The mediator to set.
	 */
	public void setMediator(ViperViewMediator mediator) {
		if (this.mediator != null) {
			this.mediator.getSelection().removeChangeListener(selectionChanged);
			this.mediator.setChronicleSelectionModel(null);
		}
		this.mediator = mediator;
		if (this.mediator != null) {
			this.mediator.getSelection().addChangeListener(selectionChanged);
			this.mediator.setChronicleSelectionModel(this);
		}
	}
	
	private ChangeListener selectionChanged = new ChangeListener() {
		public void stateChanged(ChangeEvent e) {
			fireChangeEvent(e);
		}};
	/**
	 * @return Returns the nodeWhoseTimeToSelect.
	 */
	public Node getNodeWhoseTimeToSelect() {
		return nodeWhoseTimeToSelect;
	}
	/**
	 * @param nodeWhoseTimeToSelect The nodeWhoseTimeToSelect to set.
	 */
	public void setNodeWhoseTimeToSelect(Node nodeWhoseTimeToSelect) {
		this.nodeWhoseTimeToSelect = nodeWhoseTimeToSelect;
		fireChangeEvent(null);
	}
}
