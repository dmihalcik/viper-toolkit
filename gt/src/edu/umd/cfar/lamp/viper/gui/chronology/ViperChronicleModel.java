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
import edu.umd.cfar.lamp.chronicle.extras.*;
import edu.umd.cfar.lamp.viper.gui.core.*;

class ViperChronicleModel extends TreeChronicleViewModel {
	private ViperViewMediator mediator = null;
	
	private ViperChronicleSelectionModel selectionModel;

	private AbstractChronicleDataModel graph = new AbstractChronicleDataModel() {
		public Collection getTimeLines() {
			return roots;
		}
	};
	
	private ViperMediatorChangeListener cvl = new ViperMediatorChangeListener() {
		public void frameChanged(ViperMediatorChangeEvent e) {
			fireFocusChange(null);
		}

		public void dataChanged(ViperMediatorChangeEvent e) {
			massChange(e.getSource());
			fireDataChanged(null);
		}

		public void currFileChanged(ViperMediatorChangeEvent e) {
			this.dataChanged(e);
		}

		public void schemaChanged(ViperMediatorChangeEvent e) {
			this.dataChanged(e);
		}
		public void mediaChanged(ViperMediatorChangeEvent e) {
		}
	};
	private PropagateInterpolateModule.PropagateListener pil = new PropagateInterpolateModule.PropagateListener () {
		public void listChanged() {
			fireDataChanged(null);
		}
	};
	
	public ViperChronicleModel() {
		// no mediator!
		this.roots = new LinkedList();
	}
	public void setMediator(ViperViewMediator m) {
		if (mediator != null) {
			mediator.removeViperMediatorChangeListener(cvl);
			mediator.getPropagator().removePropagateListener(pil);
		}
		this.mediator = m;
		if (mediator != null) {
			mediator.addViperMediatorChangeListener(cvl);
			mediator.getPropagator().addPropagateListener(pil);
		}
		massChange(this);
		fireDataChanged(null);
	}
	public ViperViewMediator getMediator() {
		return this.mediator;
	}
	public Instant getMajorMoment() {
		if (mediator != null) {
			return mediator.getMajorMoment();
		} else {
			return null;
		}
	}
	public void setMajorMoment(Instant m) {
		if (mediator != null) {
			mediator.setMajorMoment(m);
		}
	}

	private void massChange(Object src) {
		Sourcefile sf = mediator.getCurrFile();
		this.roots.clear();
		if (sf != null) {
			Iterator cfgs = mediator.getViperData().getConfigs();
			while (cfgs.hasNext()) {
				Config c = (Config) cfgs.next();
				if (c.getDescType() != Config.FILE) {
					VConfigTimeLine nl = new VConfigTimeLine(sf, c);
					nl.setViewModel(this);
					nl.setSelectionModel(this.getSelectionModel());
					this.roots.add(nl);
				}
			}
			Set oldExpandeds = this.expandeds;
			this.expandeds = new HashSet();
			for (Iterator iter = roots.iterator(); iter.hasNext(); ) {
				helpExpandAll((TimeLine) iter.next());
			}
			oldExpandeds.retainAll(this.expandeds);
			this.expandeds = oldExpandeds;
		} else {
			this.expandeds.clear();
		}
		super.resetLines();
	}
	public InstantInterval getFocus() {
		if (mediator != null) {
			return mediator.getFocusInterval();
		} else {
			return null;
		}
	}
	public void setFocus(InstantInterval ii) {
		throw new UnsupportedOperationException();
	}

	public ChronicleDataModel getGraph() {
		return graph;
	}
	public FrameRate getFrameRate() {
		if (mediator != null && mediator.getCurrFile() != null && 
				mediator.getCurrFile().getReferenceMedia() != null) {
			return mediator.getCurrFile().getReferenceMedia().getFrameRate();
		}
		return null;
	}
	public ViperChronicleSelectionModel getSelectionModel() {
		return selectionModel;
	}
	public void setSelectionModel(ViperChronicleSelectionModel selectionModel) {
		this.selectionModel = selectionModel;
	}
}