package edu.umd.cfar.lamp.chronicle;

import javax.swing.*;

import viper.api.time.InstantRange;

import edu.umd.cfar.lamp.viper.util.*;
import edu.umd.cs.piccolo.*;

/**
 * A simple renderer that adds line renderers as requested.
 * The render node is a  uses this class's makeInterval() method
 */
public class BasicTimeLineRenderer extends AdapterForTimeLineRenderer {
	private class DelegatingSegmentedView extends SegmentedTimeLineView {
		/**
		 * {@inheritDoc}
		 */
		public DelegatingSegmentedView(TimeLine c, ChronicleViewer f) {
			super(c, f.getModel().getFocus(), f);
		}
		
		/**
		 * {@inheritDoc}
		 */
		protected PNode makeSegment(Interval i, double width, double height) {
			return BasicTimeLineRenderer.this.makeSegment(i, width, height);
		}
		
		//Hack necessary to get an interpolated range in
		protected PNode makeSegment(Interval i, double width, double height, InstantRange range) {
			return BasicTimeLineRenderer.this.makeSegment(i, width, height, range);
		}
	}
	
	/**
	 * 
	 */
	public BasicTimeLineRenderer() {
		super();
		this.setPreferredTimeLineInfoLength(8);
	}

	public PNode makeSegment(Interval i, double width, double height) {
		return new PRoundedTimeSegment();
	}
	
	public PNode makeSegment(Interval i, double width, double height, InstantRange range) {
		return new PRoundedTimeSegment();
	}

	public PNode getTimeLineRendererNode(ChronicleViewer chronicle, TimeLine t,
			boolean isSelected, boolean hasFocus, double timeLength,
			double infoLength, int orientation) {
		DelegatingSegmentedView v = new DelegatingSegmentedView(t, chronicle);
		v.setOrientation(orientation);
		boolean vertical = orientation == SwingConstants.VERTICAL;
		v.setBounds(0,0,vertical ? infoLength : timeLength, vertical ? timeLength : infoLength);
		return v;
	}
}
