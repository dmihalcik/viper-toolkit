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
import java.awt.event.*;
import java.awt.geom.*;
import java.io.*;
import java.util.*;
import java.util.List;

import javax.swing.*;
import javax.swing.border.*;

import viper.api.time.*;
import viper.api.time.Frame;
import edu.umd.cfar.lamp.viper.util.*;
import edu.umd.cs.piccolo.*;
import edu.umd.cs.piccolo.event.*;
import edu.umd.cs.piccolo.nodes.*;
import edu.umd.cs.piccolox.swing.*;

/**
 * A simple 'draw a timeline' test application for the chronicle. It supports a
 * few basic modifications, and has open/save functionality.
 * 
 * @author davidm
 */
public class TestApplication {
	private static final Frame ONE = new Frame(1);

	private static final Frame FIFTY = new Frame(50);

	private static final String[] LINE_NAMES = new String[] { "Alpha" };

	private static final double RADIUS = 4;

	private static class ColoredLinkedLine implements TimeLine, Serializable {
		private InstantRange myRange;

		private String title;

		private Color color;

		private List children = new ArrayList();

		public ColoredLinkedLine() {
		}

		public ColoredLinkedLine(TemporalRange myRange, String title) {
			this(myRange, title, Color.black);
		}

		public ColoredLinkedLine(TemporalRange myRange, String title,
				Color color) {
			super();
			this.myRange = new InstantRange();
			this.myRange.addAll(myRange);
			this.title = title;
			this.color = color;
		}

		@Override
		public boolean hasInterpolatedInformation() {
			return false;
		}
		@Override
		public InstantRange getInterpolatedOverRange() {
			throw new UnsupportedOperationException();
		}
		
		public Iterator getChildren() {
			return children.iterator();
		}

		public int getNumberOfChildren() {
			return children.size();
		}

		public String getSingularName() {
			return getTitle();
		}

		public String getPluralName() {
			return getTitle() + "s";
		}

		/**
		 * @return Returns the myRange.
		 */
		public TemporalRange getMyRange() {
			return myRange;
		}

		/**
		 * @param myRange
		 *            The myRange to set.
		 */
		public void setMyRange(TemporalRange myRange) {
			if (!this.myRange.equals(myRange)) {
				this.myRange.clear();
				this.myRange.addAll(myRange);
			}
		}

		/**
		 * @return Returns the title.
		 */
		public String getTitle() {
			return title;
		}

		/**
		 * @param title
		 *            The title to set.
		 */
		public void setTitle(String title) {
			this.title = title;
		}

		public void addChild(int index, TimeLine tqe) {
			children.add(index, tqe);
		}

		public boolean addChild(TimeLine tqe) {
			return children.add(tqe);
		}

		public void clearChildren() {
			children.clear();
		}

		public boolean contains(TimeLine tqe) {
			return children.contains(tqe);
		}

		public TimeLine getChild(int i) {
			return (TimeLine) children.get(i);
		}

		public int indexOf(TimeLine tqe) {
			return children.indexOf(tqe);
		}

		public boolean hasChildren() {
			return !children.isEmpty();
		}

		public int lastIndexOf(TimeLine tqe) {
			return children.lastIndexOf(tqe);
		}

		public TimeLine removeChild(int i) {
			return (TimeLine) children.remove(i);
		}

		public boolean removeChild(TimeLine tqe) {
			return children.remove(tqe);
		}

		public TimeLine setChile(int i, TimeLine tqe) {
			return (TimeLine) children.set(i, tqe);
		}

		public Color getColor() {
			return color;
		}

		public void setColor(Color color) {
			this.color = color;
		}

		public void addSpan(InstantInterval ii) {
			this.myRange.add(ii);
		}

		public void removeSpan(InstantInterval ii) {
			this.myRange.remove(ii);
		}

		public void invertSpan(InstantInterval ii) {
			Iterator iter = this.myRange.iterator(ii);
			if (!iter.hasNext()) {
				addSpan(ii);
			} else {
				InstantInterval curr = (InstantInterval) iter.next();
				Instant s = ii.getStartInstant();
				InstantRange toRemove = new InstantRange();
				InstantRange toAdd = new InstantRange();
				if (curr.contains(s)) {
					if (curr.getEnd().compareTo(ii.getEnd()) > 0) {
						toRemove.add(s, ii.getEndInstant());
					} else {
						toRemove.add(s, curr.getEndInstant());
					}
					s = curr.getEndInstant();
				}
				while (iter.hasNext() && ii.contains(s)) {
					curr = (InstantInterval) iter.next();
					toAdd.add(s, curr.getStartInstant());
					if (curr.getEnd().compareTo(ii.getEnd()) > 0) {
						toRemove.add(s, ii.getEndInstant());
					} else {
						toRemove.add(s, curr.getEndInstant());
					}
					s = curr.getEndInstant();
				}
				if (ii.contains(s)) {
					toAdd.add(s, ii.getEndInstant());
				}

				this.myRange.removeAll(toRemove);
				this.myRange.addAll((IntervalIndexList) toAdd);
			}
		}
	}

	private static class SampleTimeLineData extends AbstractChronicleDataModel
			implements Serializable {
		private final List lines = new LinkedList();

		public Collection getTimeLines() {
			return lines;
		}
	}

	private static class SampleTimeLineView extends AbstractChronicleViewModel {
		private SampleTimeLineData graph = new SampleTimeLineData();

		private Instant majorMoment;

		private InstantInterval focus;

		private FrameRate rate = new RationalFrameRate(12, 1);

		public ChronicleDataModel getGraph() {
			return graph;
		}

		public int getSize() {
			return graph.lines.size();
		}

		public TimeLine getElementAt(int i) {
			return (TimeLine) graph.lines.get(i);
		}

		/**
		 * @return Returns the focus.
		 */
		public InstantInterval getFocus() {
			return focus;
		}

		/**
		 * @param focus
		 *            The focus to set.
		 */
		public void setFocus(InstantInterval focus) {
			this.focus = focus;
		}

		/**
		 * @return Returns the majorMoment.
		 */
		public Instant getMajorMoment() {
			return majorMoment;
		}

		/**
		 * @param majorMoment
		 *            The majorMoment to set.
		 */
		public void setMajorMoment(Instant majorMoment) {
			this.majorMoment = majorMoment;
		}

		public FrameRate getFrameRate() {
			return rate;
		}

		public int indexOf(TimeLine tqe) {
			return graph.lines.indexOf(tqe);
		}
	}

	private ChronicleViewer chronicle = null;

	private SampleTimeLineView view = null;

	private SampleTimeLineData data = null;

	private void configureSimpleChronicle() {
		chronicle = new ChronicleViewer();
		view = new SampleTimeLineView();
		data = view.graph;

		Span[] S = new Span[] { new Span(ONE, FIFTY) };

		for (int i = 0; i < S.length; i++) {
			data.lines.add(new ColoredLinkedLine(
					Intervals.singletonRange(S[i]), LINE_NAMES[i
							% LINE_NAMES.length]));
		}

		view.setFocus(new Span(ONE, FIFTY));
		chronicle.setModel(view);

		chronicle.addTimeLineInputEventListener(new PopupListener());
		chronicle.addTimeLineInputEventListener(new FisheyeListener());
		chronicle.addTimeLineInputEventListener(new ChangeTimeListener());
	}

	public static void main(String[] args) {
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				TestApplication a = new TestApplication();
				a.configureSimpleChronicle();

				a.displayApplication();
			}
		});
	}

	/**
	 *  
	 */
	private void displayApplication() {
		ChronicleViewer.ScrollViews sv = chronicle.getScrollViews();
		PScrollPane sp = new PScrollPane(sv.content);
		sp.setWheelScrollingEnabled(false); // scroll wheel zooms, not scrolls
		sp.setRowHeaderView(sv.rowHeader);
		sp.setColumnHeaderView(sv.columnHeader);
		sp.setCorner(ScrollPaneConstants.UPPER_LEFT_CORNER, sv.cornerHeader);
		sp.setBorder(new EmptyBorder(0, 0, 0, 0));

		JFrame f = new JFrame("Chronicle Test Application");
		f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		f.getContentPane().add(sp);
		f.pack();
		f.show();
	}

	private static Color LINECOLOR = Color.white;

	private static Color FILLCOLOR = Color.black;

	private static Stroke STROKE1 = new BasicStroke(1f, BasicStroke.CAP_BUTT,
			BasicStroke.JOIN_ROUND);

	private static Stroke STROKE2 = new BasicStroke((float) (RADIUS * 2 + 2),
			BasicStroke.CAP_BUTT, BasicStroke.JOIN_ROUND);

	private class TimeLineSelection extends PNode implements NodeWithViewLength {
		public TimeLineSelection(Instant startTime, Instant endTime,
				TimeLine line) {
			super();
			this.startTime = startTime;
			this.endTime = endTime;
			this.line = line;
			reset();
		}

		private void reset() {
			removeAllChildren();
			double xOffset = chronicle.getViewOffsetX();
			double yOffset = chronicle.getViewOffsetY();
			double instantWidth = chronicle.instant2pixelDistance(startTime.go(1 - startTime.longValue()));
			double x1 = xOffset + chronicle.instant2pixelLocation(startTime) + instantWidth/2;
			double x2 = xOffset + chronicle.instant2pixelLocation(endTime) + instantWidth/2;
			double[] Y = chronicle.timeLine2pixels(this.line);
			double y = (Y[0] + Y[1]) / 2;
			Ellipse2D e1 = new Ellipse2D.Double(x1 - RADIUS, y - RADIUS,
					RADIUS * 2, RADIUS * 2);
			Ellipse2D e1Outline = new Ellipse2D.Double(x1 - RADIUS - 1, y
					- RADIUS - 1, RADIUS * 2 + 2, RADIUS * 2 + 2);
			PPath c1 = new PPath(e1);
			c1.setPaint(FILLCOLOR);
			c1.setStroke(STROKE1);
			c1.setStrokePaint(LINECOLOR);
			PPath c1o = new PPath(e1Outline);
			c1o.setPaint(LINECOLOR);
			c1o.setStroke(STROKE1);
			c1o.setStrokePaint(LINECOLOR);
			addChild(c1o);
			PTextLabel lab1 = new PTextLabel(startTime.toString());
			lab1.centerBoundsOnPoint(x1, y);
			lab1.translate(0, RADIUS * 2 + lab1.getHeight() / 2);
			PPath line = null;
			if (x1 != x2) {
				Line2D l = new Line2D.Double(x1, y, x2, y);
				line = new PPath(l);
				line.setStrokePaint(FILLCOLOR);
				PPath lineOutline = new PPath(l);
				lineOutline.setStroke(STROKE2);
				lineOutline.setStrokePaint(LINECOLOR);
				addChild(lineOutline);

				Ellipse2D e2 = new Ellipse2D.Double(x2 - RADIUS, y - RADIUS,
						RADIUS * 2, RADIUS * 2);
				Ellipse2D e2Outline = new Ellipse2D.Double(x2 - RADIUS - 1, y
						- RADIUS - 1, RADIUS * 2 + 2, RADIUS * 2 + 2);
				PPath c2 = new PPath(e2);
				c2.setPaint(FILLCOLOR);
				c2.setStroke(STROKE1);
				c2.setStrokePaint(LINECOLOR);
				PPath c2o = new PPath(e2Outline);
				c2o.setPaint(LINECOLOR);
				c2o.setStroke(STROKE1);
				c2o.setStrokePaint(LINECOLOR);
				addChild(c2o);

				PTextLabel lab2 = new PTextLabel(endTime.toString());
				lab2.centerBoundsOnPoint(x2, y);
				lab2.translate(0, RADIUS * 2 + lab2.getHeight() / 2);

				addChild(c2);
				addChild(lab2);
			}
			addChild(lab1);
			addChild(c1);
			if (line != null) {
				addChild(line);
			}
		}

		private Instant startTime;

		private Instant endTime;

		private TimeLine line;

		public void viewLengthChanged() {
			reset();
		}

		public void dataHeightChanged() {
			reset();
		}

		public void viewAndDataChanged() {
			reset();
		}
	}

	private class ChangeTimeListener extends DragTimeLineListener {
		private TimeLineSelection node = null;

		private Instant lastValidEndInstant = null;

		protected boolean shouldStartDragInteraction(TimeLineInputEvent event) {
			return startInstant != null && startLine != null;
		}

		synchronized protected void startDrag(TimeLineInputEvent event) {
			cancelDrag();
			node = new TimeLineSelection(startInstant, currentInstant,
					startLine);
			chronicle.getOverlay().addChild(node);
			lastValidEndInstant = currentInstant;
		}

		synchronized protected void cancelDrag() {
			if (node != null) {
				assert chronicle.getOverlay().indexOfChild(node) >= 0;
				chronicle.getOverlay().removeChild(node);
				node = null;
			}
		}

		synchronized protected void drag(TimeLineInputEvent event) {
			if (lastValidEndInstant.equals(currentInstant)) {
				return;
			}
			if (currentInstant != null && node != null) {
				Instant e = currentInstant;
				InstantInterval focus = chronicle.getModel().getFocus();
				if (!focus.contains(e)) {
					if (e.compareTo(focus.getEnd()) >= 0) {
						e = (Instant) focus.getEndInstant().previous();
					} else if (e.compareTo(focus.getStart()) < 0) {
						e = focus.getStartInstant();
					}
				}
				if (e != null && !e.equals(lastValidEndInstant)) {
					chronicle.getOverlay().removeChild(node);
					node = new TimeLineSelection(startInstant, e, startLine);
					chronicle.getOverlay().addChild(node);
					lastValidEndInstant = e;
				}
			} else {
				cancelDrag();
			}
		}

		synchronized protected void endDrag(TimeLineInputEvent event) {
			InstantInterval toChange;
			if (startLine != null) {
				if (startInstant.compareTo(lastValidEndInstant) >= 0) {
					toChange = new Span(lastValidEndInstant,
							(Instant) startInstant.next());
				} else {
					toChange = new Span(startInstant,
							(Instant) lastValidEndInstant.next());
				}
				if (event.isPopupTrigger()) {

				} else {
					ColoredLinkedLine l = (ColoredLinkedLine) startLine;
					l.invertSpan(toChange);
					data.fireStructureChange();
					view.fireDataChanged(null);
				}
			}
			cancelDrag();
		}
	}

	private class FisheyeListener extends BasicTimeLineInputEventAdapter {
		private boolean isFisheyeing = true;

		public void mouseMoved(TimeLineInputEvent event) {
			if (isFisheyeing) {
				// TODO fisheye!
			}
		}
	}

	private class PopupListener extends BasicTimeLineInputEventAdapter {
		public void mouseClicked(TimeLineInputEvent event) {
			maybeShowPopup(event);
		}

		public void mouseReleased(TimeLineInputEvent event) {
			maybeShowPopup(event);
		}

		private void maybeShowPopup(TimeLineInputEvent event) {
			PInputEvent pe = event.getPiccoloEvent();
			TimeLine tl = event.getTimeLine();
			if (pe.isPopupTrigger() && tl != null) {
				System.out.println("Showpopup");
				Point2D where = pe.getCanvasPosition();
				configurePopupFor(tl);
				popup.show((Component) pe.getComponent(), (int) where.getX(),
						(int) where.getY());
			} else {
				System.out.println("don't Showpopup");
			}
		}
	}

	private JPopupMenu popup = new JPopupMenu("Edit Chronicle");

	private void configurePopupFor(TimeLine tqe) {
		popup.removeAll();
		popup.add(new JMenuItem(new RemoveTimelineAction(tqe)));
	}

	private class RemoveTimelineAction extends AbstractAction {
		private TimeLine tqe;

		public RemoveTimelineAction(TimeLine t) {
			super("Remove " + t.getTitle());
			tqe = t;
			super.putValue(Action.MNEMONIC_KEY, new Integer('r'));
		}

		public void actionPerformed(ActionEvent e) {
			data.lines.remove(tqe);
			data.fireStructureChange();
			view.fireDataChanged(null);
		}
	}

	private Action[] editActions = new Action[] { new AbstractAction(
			"Add New Line") {
		public void actionPerformed(ActionEvent e) {
			final int[] i = new int[] { 1 };
			TemporalRange r = Intervals.singletonRange(new Span(ONE, FIFTY));
			data.lines.add(new ColoredLinkedLine(r, LINE_NAMES[i[0]++
					% LINE_NAMES.length]));
			data.fireStructureChange();
			view.fireDataChanged(null);
		}
	} };
}