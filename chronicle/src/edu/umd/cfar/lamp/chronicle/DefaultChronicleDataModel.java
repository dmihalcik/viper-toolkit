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


package edu.umd.cfar.lamp.chronicle;

import java.util.*;

import org.apache.commons.lang.*;

public class DefaultChronicleDataModel extends AbstractChronicleDataModel {
	protected List timeLines = new ArrayList();
	
	public Collection getTimeLines() {
		return Collections.unmodifiableList(timeLines);
	}

	public boolean add(TimeLine tqe) {
		if (!timeLines.add(tqe)) {
			return false;
		}
		fireTimeLinesAdded();
		return true;
	}

	public void add(int i, TimeLine tqe) {
		timeLines.add(i, tqe);
		fireTimeLinesAdded();
	}

	public boolean addAll(Collection tqes) {
		if (!timeLines.addAll(tqes)) {
			return false;
		}
		fireTimeLinesAdded();
		return true;
	}

	public boolean addAll(int i, Collection tqes) {
		if (!timeLines.addAll(i, tqes)) {
			return false;
		}
		fireTimeLinesAdded();
		return true;
	}

	public void clear() {
		if (timeLines.isEmpty()) {
			return;
		}
		timeLines.clear();
		fireTimeLinesRemoved();
	}

	public boolean remove(TimeLine tqe) {
		if (!timeLines.remove(tqe)) {
			return false;
		}
		fireTimeLinesRemoved();
		return true;
	}

	public Object remove(int index) {
		Object old = timeLines.remove(index);
		fireTimeLinesRemoved();
		return old;
	}

	public boolean removeAll(Collection tqes) {
		if (!timeLines.removeAll(tqes)) {
			return false;
		}
		fireTimeLinesRemoved();
		return true;
	}

	public boolean retainAll(Collection tqes) {
		if (!timeLines.retainAll(tqes)) {
			return false;
		}
		fireTimeLinesRemoved();
		return true;
	}

	public Object set(int i, TimeLine tqe) {
		Object old = timeLines.set(i, tqe);
		if (ObjectUtils.equals(old, tqe)) {
			return old;
		}
		fireTimeLinesChanged();
		return old;
	}
}
