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

import java.awt.event.*;
import java.util.*;

import viper.api.time.*;
import edu.umd.cfar.lamp.chronicle.*;
import edu.umd.cfar.lamp.chronicle.markers.*;
import edu.umd.cfar.lamp.viper.util.*;

/**
 */
public class DescriptorTimeDragHandler extends BasicTimeLineInputEventAdapter {
	private static final double WELL_DISTANCE = 6;
		
	private ViperChronicleView viewer;
	
	/// the line to modify
	private VDescriptorTimeLine line = null;
	
	/// the instant the user clicked on
	private Instant startInstant;
	
	/// the instant the last drag event was over
	private Instant lastInstant;

	private int dragType;
	
	protected static interface DRAG {
		public static final int LEFT_END = 0;
		public static final int RIGHT_END = 1;
		public static final int INSIDE = 2;
		public static final int OUTSIDE = 3;
	}
	
	/// the contiguous interval the user clicked on
	private InstantInterval startSpan;
	
	/// the range of the descriptor; what the user will alter
	private TemporalRange startRange;

	private void cancel() {
		if (line != null) {
			line.setUserRange(null);
		}
		line = null;
		startInstant = null;
		startSpan = null;
		lastInstant = null;
		startRange = null;
	}
	
	public void mouseDragged(TimeLineInputEvent event) {
		if (line != null) {
			alterRangeFor(event);
		}
	}
	public void mouseMoved(TimeLineInputEvent event) {
	}
	public void mouseClicked(TimeLineInputEvent event) {
		if (line == null && event.getTimeLine() != null && (event.getInstant() == null || !viewer.getModel().getFocus().contains(event.getInstant()))) {
			viewer.getMediator().getSelection().setTo(((ViperNodeTimeLine) event.getTimeLine()).getNode());
		}
	}
	public void mousePressed(TimeLineInputEvent event) {
		cancel();
		if (!(event.getTimeLine() instanceof VDescriptorTimeLine)) {
			return;
		}
		if (event.getInstant() == null || !viewer.getModel().getFocus().contains(event.getInstant())) {
			return;
		}
		line = (VDescriptorTimeLine) event.getTimeLine();
		TemporalRange r = line.getMyRange();
		startRange = (TemporalRange) r.clone();
		startInstant = getInstantToSnapTo(event);
		if (!r.contains(startInstant)) {
			dragType = DRAG.OUTSIDE;
		} else {
			Instant s = startInstant;
			if (!r.contains(startInstant.previous())) {
				dragType = DRAG.LEFT_END;
			} else if (!r.contains(startInstant.next())) {
				dragType = DRAG.RIGHT_END;
				s = (Instant) r.firstBefore(s);
			} else {
				dragType = DRAG.INSIDE;
				s = (Instant) r.firstBefore(s);
			}
			startSpan = new Span(s, (Instant) r.endOf(s));
		}
	}
	
	public void mouseReleased(TimeLineInputEvent event) {
		if (line != null) {
			alterRangeFor(event);
			line.getDescriptor().setValidRange(line.getUserRange());
			cancel();
		}
	}

	private Instant getInstantToSnapTo(TimeLineInputEvent event) {
		Instant i = event.getInstant();
		if (i == null) {
			return null;
		}
		if ((event.getModifiersEx() & InputEvent.SHIFT_DOWN_MASK) == 0) {
			// if shift isn't held down, snap to nearest marker when within the
			// marker's field of gravity
			ChronicleMarker cm =
				viewer.getMarkerModel().getMarkerClosestTo(i);
			List toSnapTo = new ArrayList(3);
			if (cm != null) {
				toSnapTo.add(cm.getWhen());
			}
			boolean inside = startRange.contains(i);
			Comparable c = startRange.firstBefore(i);
			if (c != null) {
				if (inside) {
					toSnapTo.add(c);
				}
				toSnapTo.add(((Incrementable)startRange.endOf(c)).previous());
			}
			c = startRange.firstAfterOrAt(i);
			if (c != null) {
				toSnapTo.add(c);
				if (inside) {
					toSnapTo.add(((Incrementable)startRange.endOf(c)).previous());
				}
			}
			double d = Double.POSITIVE_INFINITY;
			double clickPoint = event.getPosition().getX() - viewer.getLabelLength();
			Instant toSnap = null;
			for (int count = 0; count < toSnapTo.size(); count++) {
				Instant snapInstant = (Instant) toSnapTo.get(count);
				double snapPoint = viewer.instant2pixelLocation(snapInstant);
				double pix = Math.abs(snapPoint - clickPoint);
				if (pix < d) {
					d = pix;
					toSnap = snapInstant;
				}
			}
			if (d < WELL_DISTANCE) {
				i = toSnap;
			}
		}
		InstantInterval ii = viewer.getModel().getFocus();
		if (i.compareTo (ii.getStartInstant()) < 0) {
			i = ii.getStartInstant();
		} else if (i.compareTo (ii.getEndInstant()) >= 0) {
			i = (Instant) ii.getEndInstant().previous();
		}
		return i;
	}
	
	/**
	 * Gets the new interval corresponding to the drag event so far.
	 * @return the new interval
	 */
	private InstantInterval getIntervalFor(Instant i) {
		Instant s;
		Instant e;
		if (dragType == DRAG.LEFT_END) {
			s = i;
			e = (Instant) startSpan.getEndInstant().previous();
		} else {
			s = startSpan.getStartInstant();
			e = i;
		}
		if (s.compareTo(e) > 0) {
			i = s;
			s = e;
			e = i;
		}
		return (InstantInterval) startSpan.change(s, e.next());
	}

	private void alterRangeFor(TimeLineInputEvent event) {
		Instant dragInstant = getInstantToSnapTo(event);
		if ((dragInstant == null) || dragInstant.equals(lastInstant)) {
			return;
		}
		Span toInclude = new Span(dragInstant, (Instant) dragInstant.next());
		InstantRange r = (InstantRange) startRange.clone();
		if (dragType == DRAG.RIGHT_END || dragType == DRAG.LEFT_END) {
			// Dragging the end of the line is simpler
			// Remove the old segment, then add the start to the end
			InstantInterval ni = getIntervalFor(dragInstant);
			r.remove(startSpan);
			r.add(ni);
		} else if (dragType == DRAG.OUTSIDE) {
			int startMinusDragInstant = startInstant.compareTo(dragInstant);
			if (startMinusDragInstant < 0) { // dragging right
				r.add(startInstant, dragInstant);
			} else if (startMinusDragInstant > 0) {// dragging left
				r.add(dragInstant, startInstant);
			}
		} else { // drag == inside
			int startMinusNow = startInstant.compareTo(dragInstant);
			Span toAdd = null;
			Span toRemove = null;
			if (startMinusNow < 0) {
				Instant siNext = (Instant) startInstant.next();
				if (siNext.compareTo(dragInstant) < 0) {
					toRemove = (Span) toInclude.change(siNext, dragInstant);
				}
				if (dragInstant.compareTo(startSpan.getEnd()) > 0) {
					toAdd = new Span(startSpan.getEndInstant(), dragInstant);
				}
			} else if (startMinusNow > 0) {
				Instant dtNext = (Instant) dragInstant.next();
					toRemove = (Span) toInclude.change(dtNext, startInstant);
				if (dragInstant.compareTo(startSpan.getStart()) < 0) {
					toAdd = new Span(dragInstant, startSpan.getStartInstant());
				}
			}
			if (toRemove != null) {
				r.remove(toRemove.getStart(), toRemove.getEnd());
			}
			if (toAdd != null) {
				r.add(toAdd.getStart(), toAdd.getEnd());
			}
		}
		line.setUserRange(r);
		lastInstant = dragInstant;
	}

	public ViperChronicleView getViewer() {
		return viewer;
	}
	public void setViewer(ViperChronicleView viewer) {
		this.viewer = viewer;
	}
}
