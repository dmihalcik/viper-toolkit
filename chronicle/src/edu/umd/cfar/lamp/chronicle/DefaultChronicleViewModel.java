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

import viper.api.time.*;

public class DefaultChronicleViewModel extends AbstractChronicleViewModel {
	protected ChronicleDataModel graph;
	protected Instant majorMoment;
	protected InstantInterval focus;
	protected FrameRate frameRate;
	
	

	/**
	 * 
	 */
	public DefaultChronicleViewModel() {
		this(new DefaultChronicleDataModel(), new Frame(1), new Span(new Frame(1), new Frame(2)), new RationalFrameRate(1));
	}

	/**
	 * @param graph
	 * @param moment
	 * @param focus
	 * @param rate
	 */
	public DefaultChronicleViewModel(ChronicleDataModel graph, Instant moment, InstantInterval focus, FrameRate rate) {
		super();
		this.graph = graph;
		majorMoment = moment;
		this.focus = focus;
		frameRate = rate;
	}

	public ChronicleDataModel getGraph() {
		return graph;
	}

	public int getSize() {
		return graph.getTimeLines().size();
	}

	public TimeLine getElementAt(int i) {
		Collection s = graph.getTimeLines();
		if (s instanceof List) {
			return (TimeLine) ((List) s).get(i);
		}
		Iterator iter = s.iterator();
		while (0 < i) {
			iter.next();
		}
		return (TimeLine) iter.next();
	}

	public Instant getMajorMoment() {
		return majorMoment;
	}

	public void setMajorMoment(Instant m) {
		this.majorMoment = m;
	}

	public InstantInterval getFocus() {
		return focus;
	}

	public void setFocus(InstantInterval ii) {
		this.focus = ii;
	}

	public FrameRate getFrameRate() {
		return this.frameRate;
	}
	
	public void setFrameRate(FrameRate frameRate) {
		this.frameRate = frameRate;
	}

	public int indexOf(TimeLine tqe) {
		Collection s = graph.getTimeLines();
		if (s instanceof List) {
			return ((List) s).indexOf(tqe);
		}
		Iterator iter = s.iterator();
		int idx = 0;
		while (iter.hasNext()) {
			if (tqe.equals(iter.next())) {
				return idx;
			}
			idx++;
		}
		return -1;
	}

}
