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

package edu.umd.cfar.lamp.chronicle.markers;

import java.util.*;

import viper.api.time.*;

/**
 * Maintains the markers as a String (the label) indexed set. 
 */
public class DefaultChronicleMarkerModel extends AbstractChronicleMarkerModel {
	private List markers;
	private Map labels2markers;
	private InstantInterval interval;
	
	/**
	 * Constructs a new default marker model.
	 */
	public DefaultChronicleMarkerModel () {
		super();
		labels2markers = new HashMap();
		markers = new Vector();
	}
	
	private class Marker implements ChronicleMarker {
		private String label;
		private Instant when;
		
		Marker() {
			Set labelSet = (Set) labels2markers.get(null);
			if (labelSet == null) {
				labelSet = new HashSet();
				labels2markers.put(this.label, labelSet);
			}
			labelSet.add(Marker.this);
		}

		boolean moveIntoInterval () {
			if (this.when == null) {
				return false;
			}
			if (interval.isFrameBased() != when instanceof Frame) {
				if (interval.isEmpty()) {
					this.when = null;
				} else {
					this.when = interval.getStartInstant();
				}
			} else if (interval.contains(this.when)) {
				return false;
			} else if (interval.isEmpty()) {
				this.when = null;
			} else if (this.when.compareTo(interval.getStart()) < 0) {
				this.when = (Instant) interval.getStart();
			} else {
				this.when = (Instant) interval.getEndInstant().previous();
			}
			return true;
		}
		
		/**
		 * @inheritDoc
		 */
		public Instant getWhen() {
			if (moveIntoInterval()) {
				fireMarkerMoved(this);
			}
			return when;
		}

		/**
		 * @inheritDoc
		 */
		public ChronicleMarkerModel getParentModel() {
			return DefaultChronicleMarkerModel.this;
		}
		
		/**
		 * @inheritDoc
		 */
		public void setWhen(Instant i) {
			Instant old = this.when;
			this.when = i;
			moveIntoInterval();
			if (this.when == null ? (this.when != old) : (!this.when.equals(old))) {
				fireMarkerMoved(this);
			}
		}
		
		/**
		 * @inheritDoc
		 */
		public boolean isEditable() {
			return true;
		}
		
		/**
		 * @inheritDoc
		 */
		public String getLabel() {
			return label;
		}
		
		/**
		 * @inheritDoc
		 */
		public void setLabel(String label) {
			if (this.label == null && label == null) {
				return;
			} else if (this.label != null && this.label.equals(label)) {
				return;
			}
			Set labelSet = (Set) labels2markers.get(this.label);
			if (labelSet != null) {
				labelSet.remove(Marker.this);
				if (labelSet.isEmpty()) {
					labels2markers.remove(this.label);
				}
			}
			this.label = label;
			labelSet = (Set) labels2markers.get(this.label);
			if (labelSet == null) {
				labelSet = new HashSet();
				labels2markers.put(this.label, labelSet);
			}
			labelSet.add(Marker.this);
			fireMarkerChanged(this);
		}
	}
	

	/**
	 * @inheritDoc
	 */
	public ChronicleMarker getMarker(int i) {
		return (ChronicleMarker) markers.get(i);
	}
	
	/**
	 * @inheritDoc
	 */
	public void removeMarker(ChronicleMarker m) {
		if (markers.remove(m)) {
			labels2markers.remove(m.getLabel());
			fireMarkerRemoved(m);
		} 
	}

	/**
	 * @inheritDoc
	 */
	public void removeMarker(int i) {
		ChronicleMarker m = (ChronicleMarker) markers.remove(i);
		if (m != null) {
			fireMarkerRemoved(m);
		}
	}
	
	/**
	 * @inheritDoc
	 */
	public int getSize() {
		return markers.size();
	}

	/**
	 * @inheritDoc
	 */
	public ChronicleMarker createMarker() {
		Marker m = new Marker();
		this.markers.add(m);
		fireMarkerAdded(m);
		return m;
	}

	/**
	 * @inheritDoc
	 */
	public Set getLabels() {
		return labels2markers.keySet();
	}

	/**
	 * @inheritDoc
	 */
	public void removeMarkersWithLabel(String l) {
		for (Iterator iter = getMarkersWithLabel(l); iter.hasNext(); ) {
			ChronicleMarker m = (ChronicleMarker) iter.next();
			removeMarker(m);
		}
		labels2markers.remove(l);
	}

	/**
	 * @inheritDoc
	 */
	public Iterator getMarkersWithLabel(String l) {
		Set s = (Set) labels2markers.get(l);
		if (s != null) {
			return s.iterator();
		} else {
			return Collections.EMPTY_SET.iterator();
		}
	}
	
	/**
	 * @inheritDoc
	 */
	public Iterator markerIterator(){
		return markers.iterator();
	}
	
	/**
	 * @inheritDoc
	 */
	public InstantInterval getInterval() {
		return interval;
	}

	/**
	 * Sets the interval.
	 * @param interval The interval to set
	 */
	public void setInterval(InstantInterval interval) {
		if (this.interval == null && interval == null) {
			return;
		} else if (this.interval == null || !this.interval.equals(interval)) {
			this.interval = interval;
			Iterator markers = this.markers.iterator();
			while (markers.hasNext()) {
				Marker m = (Marker) markers.next();
				m.moveIntoInterval();
			}
			fireMultipleChangesEvent();
		}
	}

	/**
	 * @inheritDoc
	 */
	public ChronicleMarker getMarkerClosestTo(Instant i) {
		if (i == null) {
			return null;
		}
		ChronicleMarker closest = null;
		long referenceInstant = i.longValue();
		long dist = Long.MAX_VALUE;
		Iterator iter = markers.iterator();
		while (iter.hasNext()) {
			ChronicleMarker nextClosest = (ChronicleMarker) iter.next();
			long nextDist = Math.abs(nextClosest.getWhen().longValue() - referenceInstant);
			if (nextDist == 0) {
				return nextClosest;
			} else if (nextDist < dist) {
				closest = nextClosest;
				dist = nextDist;
			}
		}
		return closest;
	}

}
