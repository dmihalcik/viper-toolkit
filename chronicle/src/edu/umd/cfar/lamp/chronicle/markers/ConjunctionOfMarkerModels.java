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
 * This combines multiple marker models into one marker model.
 * This lets you do things like have seperate models for
 * the current frame, user generated markers, markers that 
 * represent keyframes, etc.
 */
public class ConjunctionOfMarkerModels extends AbstractChronicleMarkerModel {
	private List models;
	
	/**
	 * Constructs a new conjunction interface.
	 */
	public ConjunctionOfMarkerModels() {
		models = new ArrayList();
	}
	
	/**
	 * Adds the given model to the list of combined models.
	 * @param m the new model
	 */
	public void addModel(ChronicleMarkerModel m) {
		models.add(m);
	}
	
	/**
	 * Removes the given model to the list of combined models.
	 * @param m the model to delete
	 */
	public void removeModel(ChronicleMarkerModel m) {
		models.remove(m);
	}

	/**
	 * @inheritDoc
	 */
	public ChronicleMarker getMarker(int i) {
		for (int k = 0; k < models.size(); k++) {
			ChronicleMarkerModel m = (ChronicleMarkerModel) models.get(k);
			if (i < m.getSize()) {
				return m.getMarker(i);
			} else {
				i -= m.getSize();
			}
		}
		throw new IndexOutOfBoundsException();
	}

	/**
	 * @inheritDoc
	 */
	public int getSize() {
		int count = 0;
		for (int k = 0; k < models.size(); k++) {
			ChronicleMarkerModel m = (ChronicleMarkerModel) models.get(k);
			count += m.getSize();
		}
		return count;
	}

	/**
	 * Not implemented.
	 * @inheritDoc
	 * @throws UnsupportedOperationException
	 */
	public ChronicleMarker createMarker() {
		throw new UnsupportedOperationException();
	}

	/**
	 * Not implemented.
	 * @inheritDoc
	 * @throws UnsupportedOperationException
	 */
	public void removeMarker(ChronicleMarker m) {
		throw new UnsupportedOperationException();
	}

	/**
	 * Not implemented.
	 * @inheritDoc
	 * @throws UnsupportedOperationException
	 */
	public void removeMarker(int i) {
		throw new UnsupportedOperationException();
	}

	/**
	 * @inheritDoc
	 */
	public InstantInterval getInterval() {
		ChronicleMarkerModel m;
		if (models.isEmpty()) {
			return null;
		} else if (models.size() == 1) {
			m = (ChronicleMarkerModel) models.get(0);
			return m.getInterval();
		} else {
			InstantInterval i = null;
			m = (ChronicleMarkerModel) models.get(0);;
			i = m.getInterval();
			for (int index = 1; index < models.size(); index++) {
				m = (ChronicleMarkerModel) models.get(index);
				InstantInterval i2 = m.getInterval();
				Comparable s1 = i.getStart();
				Comparable s2 = i2.getStart();
				Comparable e1 = i.getEnd();
				Comparable e2 = i2.getEnd();
				
				Comparable ns = s2.compareTo(s1) > 0 ? s1 : s2;
				Comparable ne = e2.compareTo(e1) < 0 ? e1 : e2;
				
				i = (InstantInterval) i.change(ns, ne);
			}
			return i;
		}
	}

	/**
	 * @inheritDoc
	 */
	public void setInterval(InstantInterval i) {
		throw new UnsupportedOperationException();
	}
	
	/**
	 * @inheritDoc
	 */
	public Set getLabels() {
		Set s = new HashSet();
		for (int index = 0; index < models.size(); index++) {
			ChronicleMarkerModel m = (ChronicleMarkerModel) models.get(index);
			s.addAll(m.getLabels());
		}
		return s;
	}

	/**
	 * @inheritDoc
	 */
	public void removeMarkersWithLabel(String l) {
		throw new UnsupportedOperationException();
	}

	/**
	 * @inheritDoc
	 */
	public Iterator getMarkersWithLabel(String l) {
		throw new UnsupportedOperationException();
	}
	
	/**
	 * @inheritDoc
	 */
	public Iterator markerIterator(){
		throw new UnsupportedOperationException();
	}

	/**
	 * @inheritDoc
	 */
	public ChronicleMarker getMarkerClosestTo(Instant i) {
		ChronicleMarker closest = null;
		long referenceInstant = i.longValue();
		long dist = Long.MAX_VALUE;
		Iterator iter = models.iterator();
		while (iter.hasNext()) {
			ChronicleMarker nextClosest = ((ChronicleMarkerModel) iter.next()).getMarkerClosestTo(i);
			if (nextClosest != null) {
				long nextDist = Math.abs(nextClosest.getWhen().longValue() - referenceInstant);
				if (nextDist == 0) {
					return nextClosest;
				} else if (nextDist < dist) {
					closest = nextClosest;
					dist = nextDist;
				}
			}
		}
		return closest;
	}
}
