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

package viper.api.impl;

import java.util.*;

import viper.api.*;
import viper.api.time.*;
import edu.umd.cfar.lamp.viper.util.*;

/**
 * Implements a description of a sequence of media elements to be 
 * treated as a single element.
 */
public class MediaSequenceImpl extends MediaElementImpl implements MediaSequence {
	private List els;

	private TimeEncodedIntegerVector time2el;
	private List starts;
	
	MediaSequenceImpl (String fname, FrameRate rate) {
		super(fname, rate);
		els = new Vector();
		time2el = new TimeEncodedIntegerVector();
		starts = new Vector();
	}

	/**
	 * @see viper.api.MediaSequence#getComponentsOfSequence()
	 */
	public Iterator getComponentsOfSequence() {
		return els.iterator();
	}

	private void recomputeTimeFor(int elementIndex, Instant start) {
		Sourcefile element = (Sourcefile) els.get(elementIndex);
		Interval i = element.getRange().getExtrema();
		Instant offset = ((Instant) i.getEnd()).go(- ((Instant) i.getStart()).longValue());
		Instant end = start.go(offset.longValue());
		time2el.set(start, end, els.size());
		starts.set(elementIndex, start);
	}
	
	private void resetAllIntervals() {
		time2el.clear();
		starts.clear();
		
		for (int i = 0; i < els.size(); i++) {
			recomputeTimeFor(i, (Instant) time2el.getExtrema().getEnd());
		}
	}

	/**
	 * @see viper.api.MediaSequence#addElementToSequence(viper.api.Sourcefile)
	 */
	public void addElementToSequence(Sourcefile element) {
		els.add(element);
		Instant start = (Instant) time2el.getExtrema().getEnd();
		recomputeTimeFor(els.size()-1, start);
	}

	/**
	 * @see viper.api.MediaSequence#addElementToSequence(int, viper.api.Sourcefile)
	 */
	public void addElementToSequence(int index, Sourcefile element) {
		if (index < els.size()) {
			els.add(index, element);
			resetAllIntervals();
		} else {
			addElementToSequence(element);
		}
	}

	/**
	 * @see viper.api.MediaSequence#removeElement(int)
	 */
	public void removeElement(int index) {
		els.remove(index);
		if (index < els.size()-1) {
			resetAllIntervals();
		} else {
			Instant s = (Instant) starts.remove(index);
			time2el.remove(s, time2el.getExtrema().getEnd());
		}
	}

	/**
	 * @see viper.api.MediaSequence#findElement(viper.api.Sourcefile)
	 */
	public int findElement(Sourcefile element) {
		return els.indexOf(element);
	}

	/**
	 * @see viper.api.MediaSequence#startOf(int)
	 */
	public Instant startOf(int index) {
		return (Instant) starts.get(index);
	}
}
