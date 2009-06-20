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

import java.awt.*;
import java.awt.geom.*;
import java.util.*;

import viper.api.time.*;
import edu.umd.cfar.lamp.viper.gui.chronology.VDescriptorTimeLineRenderer;
import edu.umd.cfar.lamp.viper.gui.chronology.VDescriptorTimeLine;
import edu.umd.cfar.lamp.viper.util.*;
import edu.umd.cs.piccolo.*;
import edu.umd.cs.piccolo.util.*;



/**
 * Represents a single line in a timeline as a set of segments. 
 */
public abstract class SegmentedTimeLineView extends PNode {
	private ShapeDisplayProperties backgroundStyle = null;
	
	/// The data model for this pnode
	private TimeLine c;

	/// The place where the segments end up.
	private ChronicleViewer viewer;

	/// The epoch in which the timeline is drawn
	private InstantInterval era;

	/// Which direction to draw the line
	// wouldn't it be cool to let this be a path? Or maybe something cooler...
	private int orientation;
	
	SegmentedTimeLineView(TimeLine c, InstantInterval era, ChronicleViewer f) {
		super();
		this.viewer = f;
		setData(c, era);
	}
	

	TimeLine getTimeLine() {
		return c;
	}

	void setTimeLine(TimeLine t) {
		c = t;
		resetRectangles();
		invalidatePaint();
	}

	public InstantInterval getEra() {
		return era;
	}
	public void setEra(InstantInterval era) {
		this.era = era;
		resetRectangles();
		invalidatePaint();
	}

	/**
	 * Gets the current orientation of the timeline. For details on
	 * orientations, see {@link ChronicleViewer}.
	 * @return int
	 */
	int getOrientation() {
		return orientation;
	}

	/**
	 * Sets the orientation.
	 * @param orientation The orientation to set
	 */
	void setOrientation(int orientation) {
		if (this.orientation != orientation) {
			this.orientation = orientation;
			invalidatePaint();
		}
	}
	
	/**
	 * Set both the timeline and the era at the same time. This is 
	 * more efficient, since calling either will cause a rerender of 
	 * the whole timeline, and it would be strange to reset the timeline 
	 * without altering the era as well.
	 * 
	 * @param c the timeline to be altered
	 * @param era the range over which it is/may be valid
	 */
	void setData(TimeLine c, InstantInterval era) {
		this.c = c;
		this.era = era;
		resetRectangles();
	}

	/**
	 * {@inheritDoc}
	 */
	public boolean setBounds(double x, double y, double width, double height) {
		boolean success = super.setBounds(x, y, width, height);
		if (success) {
			this.setData(this.c, this.era);
		}
		//System.out.println ("Changed bounds to: " + this.getBoundsReference() + ", which gives full bounds " + this.getFullBounds());
		return success;
	}


	/**
	 * resets the segments of the timeline.
	 *
	 */
	private void resetRectangles() {
		this.removeAllChildren();
		if (c == null || getBoundsReference().isEmpty()) {
			return;
		}

		PBounds bref = getBoundsReference();
		double height = bref.getHeight();
		double y = getBoundsReference().getY();
		TemporalRange r = c.getMyRange();
		assert r != null;
		assert era != null;
		double end;
		double start;
		if (r.isFrameBased() == era.isFrameBased()) {
			start = era.getStartInstant().doubleValue();
			end = era.getEndInstant().doubleValue();
		} else {
			FrameRate rate = viewer.getModel().getFrameRate();
			InstantInterval eraC = r.isFrameBased() ? rate.asFrame(era) : rate.asTime(era);
			start = eraC.getStartInstant().doubleValue();
			end = eraC.getEndInstant().doubleValue();
		}
		double allWidth = end - start;
		double alpha = bref.getWidth() / allWidth;
		for (Iterator iter = r.iterator(); iter.hasNext();) {
			InstantInterval currSpan = (InstantInterval) iter.next();
			double currStart = currSpan.getStartInstant().doubleValue();
			double currEnd = currSpan.getEndInstant().doubleValue();
			double currWidth = currEnd - currStart;
			// currWidth : allWidth :: currRect.width : outside.width,  so:
			// currWidth * (outside width / all width) == width for this rectangle
			double currRectWidth = currWidth * alpha;

			// similar with x offset. 
			double normalizedCurrStart = currStart - start;
			double currRectX =
				normalizedCurrStart * alpha + bref.getX();

			PNode currRect = null;
			if(c instanceof VDescriptorTimeLine){
				currRect = makeSegment(currSpan, currRectWidth, height, ((VDescriptorTimeLine)c).getDescriptor().getInterpolatedOverRange());
			}else{
				currRect = makeSegment(currSpan, currRectWidth, height);
			}
			currRect.setBounds(currRectX, y, currRectWidth, height);
			this.addChild(currRect);
		}
	}

	/**
	 * This is the method called to make a segment for the given timeline.
	 * Note that this will be called for every subinterval of the timeline's 
	 * range, for every time the shape is changed.
	 * @param i the interval to describe
	 * @param width the width of the area the segment will be given in piccolo units 
	 * @param height the width of the area the segment will be given in piccolo units
	 * @return a new segment
	 */
	protected PNode makeSegment(Interval i, double width, double height) {
		return new PRoundedTimeSegment();
	}
	
	//Very much a hack to get an interpolated range in
	protected PNode makeSegment(Interval i, double width, double height, InstantRange range) {
		return new PRoundedTimeSegment();
	}

	/**
	 * {@inheritDoc}
	 */
	public boolean intersects(Rectangle2D aBounds) {
		return this.getBoundsReference().intersects(aBounds);
	}


	/* (non-Javadoc)
	 * @see edu.umd.cfar.lamp.chronicle.TimeLineRenderer#generateLabel(edu.umd.cfar.lamp.chronicle.ChronicleViewer, edu.umd.cfar.lamp.chronicle.TimeLine, boolean, boolean, int, int)
	 */
	public PNode generateLabel(ChronicleViewer v, TimeLine tqe, boolean isSelected, boolean hasFocus, int infoLength, int orientation) {
		// TODO Auto-generated method stub
		return null;
	}


	/* (non-Javadoc)
	 * @see edu.umd.cfar.lamp.chronicle.TimeLineRenderer#getTimeLineRendererNode(edu.umd.cfar.lamp.chronicle.ChronicleViewer, edu.umd.cfar.lamp.chronicle.TimeLine, boolean, boolean, int, int, int)
	 */
	public PNode getTimeLineRendererNode(ChronicleViewer chronicle, TimeLine t, boolean isSelected, boolean hasFocus, int timeLength, int infoLength, int orientation) {
		// TODO Auto-generated method stub
		return null;
	}


	/* (non-Javadoc)
	 * @see edu.umd.cfar.lamp.chronicle.TimeLineRenderer#getPreferedTimeLineInfoLength(edu.umd.cfar.lamp.chronicle.ChronicleViewer, edu.umd.cfar.lamp.chronicle.TimeLine, boolean, boolean, int, int)
	 */
	public int getPreferedTimeLineInfoLength(ChronicleViewer chronicle, TimeLine t, boolean isSelected, boolean hasFocus, int timeLength, int orientation) {
		// TODO Auto-generated method stub
		return 0;
	}
	
	public ShapeDisplayProperties getBackgroundStyle() {
		return backgroundStyle;
	}
	public void setBackgroundStyle(ShapeDisplayProperties backgroundStyle) {
		if (this.backgroundStyle != backgroundStyle) {
			this.backgroundStyle = backgroundStyle;
			invalidatePaint();
		}
	}
	
	protected void paint(PPaintContext context) {
		if (backgroundStyle != null) {
			Graphics2D g2 = context.getGraphics();
			Paint oldPaint = g2.getPaint();
			Stroke oldStroke = g2.getStroke();
			try {
				g2.setPaint(backgroundStyle.getPaint());
				g2.fill(super.getBoundsReference());
				g2.setPaint(backgroundStyle.getStrokePaint());
				g2.setStroke(backgroundStyle.getStroke());
				g2.draw(super.getBoundsReference());
			} finally {
				g2.setPaint(oldPaint);
				g2.setStroke(oldStroke);
			}
		}
	}
}
