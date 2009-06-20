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

package edu.umd.cfar.lamp.viper.examples.persontracking;

import java.awt.*;
import java.awt.Component;
import java.awt.event.*;
import java.awt.image.*;
import java.util.*;
import java.util.logging.*;

import javax.swing.*;
import javax.swing.border.*;

import org.apache.commons.collections.*;
import org.apache.commons.collections.iterators.*;

import viper.api.time.*;
import viper.api.time.Frame;
import edu.umd.cfar.lamp.viper.geometry.*;

/**
 * Details for a single instance of a transit of a person across a single feed.
 * 
 * @author davidm
 */
public class PersonIntervalListView extends JPanel implements Scrollable {
	private static final Logger log = Logger
			.getLogger("edu.umd.cfar.lamp.viper.examples.persontracking");

	private JPopupMenu popup;
	
	private PersonGallery galleryView = null;
	private PersonGalleryModel gallery = null;

	private GalleryEntity entity = null;

	private ActionListener pressButton = new ActionListener() {
		public void actionPerformed(ActionEvent e) {
			FrameButton fb = (FrameButton) e.getSource();
			int newFrame = gallery.getSelectedFrame();
			if (fb.getFrameNumber() == newFrame) {
				return;
			}
			Frame newFrameF = new Frame(newFrame);

			InstantRange ir = (InstantRange) entity.getRange().clone();
			if (fb.frameNumber < 0) {
				// the interval was previously empty
				ir.add(newFrameF, newFrameF.next());
			} else if (fb.isStartFrame()) {
				if (fb.getFrameNumber() < newFrame) {
					// moved to future. Remove from oldStart to newFrame - 1,
					// and add newFrame to newFrame + 1
					ir.remove(new Frame(fb.getFrameNumber()), newFrameF
							.previous());
					ir.add(newFrameF, newFrameF.next());
				} else {
					// Add from newFrameF to fb.currentFrame
					ir.add(newFrameF, new Frame(fb.getFrameNumber()));
				}
			} else { // moving the end of the interval
				if (fb.getFrameNumber() < newFrame) {
					// moved to future
					ir.add(new Frame(fb.getFrameNumber()), newFrameF.next());
				} else {
					// moved the endpoint backwards
					ir.remove(newFrameF.next(), new Frame(
							fb.getFrameNumber() + 1));
					ir.add(newFrameF, newFrameF.next());
				}
			}
			entity = entity.setValidRange(ir);
		}
	};

	private class FrameButton extends JButton {
		private int frameNumber = -1;

		private ImageSlice slice;

		private boolean startFrame;

		ImageObserver myObserver = new ImageObserver() {
			public boolean imageUpdate(Image img, int infoflags, int x, int y,
					int width, int height) {
				if (0 != (infoflags & ImageObserver.ALLBITS)) {
					FrameButton.this.repaint();
					return true;
				}
				return false;
			}
		};

		public int getFrameNumber() {
			return frameNumber;
		}

		public void setFrameNumber(int frameNumber) {
			this.frameNumber = frameNumber;
			if (this.frameNumber < 0) {
				return;
			}
			try {
				// XXX replace with image archive code to speed up response time on interval changes
				// Currently, everytime the user clicks on the frame buttons, we get a bunch of calls
				// to getImage in the event thread, which is a BAD THING. 
				int w = gallery.getMediator().getDataPlayer().getImage(new Frame(frameNumber))
						.getWidth(null);
				int h = gallery.getMediator().getDataPlayer().getImage(new Frame(frameNumber))
						.getHeight(null);
				BoundingBox bbox = new BoundingBox(0, 0, w, h);
				slice = ImageSlice.createImageSlice(gallery.getMediator()
						.getCurrFile().getReferenceMedia()
						.getSourcefileIdentifier(), this.frameNumber, bbox);

				// calculate the size to make the photo tile for the image
			//	Dimension prefSize = SmartImageUtilities.smartResize(bbox
			//			.getWidth(), bbox.getHeight(), galleryView.getTileSize());

				Dimension prefSize=new Dimension(320,240);
				this.setIcon(new ImageIcon(gallery.getSubImage(slice)
						.getScaledInstance(prefSize.width, prefSize.height,
								Image.SCALE_DEFAULT)));
			} catch (RuntimeException rx) {
				log.log(Level.SEVERE, "runtime exception while loading frame "
						+ frameNumber, rx);
			}
		}

		public boolean isStartFrame() {
			return startFrame;
		}

		public void setStartFrame(boolean startFrame) {
			this.startFrame = startFrame;
		}
	}

	private class SingleSpanDetail extends JPanel {
		private FrameButton startTimeButton;

		private FrameButton endTimeButton;

		public SingleSpanDetail() {
			super();
			startTimeButton = new FrameButton();
			startTimeButton.addMouseListener(popupListener);
			startTimeButton.setStartFrame(true);
			// Dimension preferredTileSize = new
			// Dimension(gallery.getTileSize(), gallery.getTileSize());
			endTimeButton = new FrameButton();
			endTimeButton.addMouseListener(popupListener);
			endTimeButton.setStartFrame(false);

			startTimeButton.addActionListener(pressButton);
			endTimeButton.addActionListener(pressButton);

			resetBorder();

			super.setLayout(new BoxLayout(this, BoxLayout.LINE_AXIS));
			super.add(startTimeButton);
			super.add(new JLabel(" to "));
			super.add(endTimeButton);
		}

		private void resetBorder() {
			TitledBorder title;
			title = BorderFactory.createTitledBorder(startTimeButton
					.getFrameNumber()
					+ ":" + endTimeButton.getFrameNumber());
			this.setBorder(title);
		}

		public void setStartFrame(int startFrame) {
			startTimeButton.setFrameNumber(startFrame);
			resetBorder();
		}

		public void setEndFrame(int endFrame) {
			endTimeButton.setFrameNumber(endFrame);
			resetBorder();
		}
	}

	private MouseListener popupListener = new MouseListener() {
		public void mouseExited(MouseEvent e) {
		}
	
		public void mouseEntered(MouseEvent e) {
		}
	
		public void mouseReleased(MouseEvent e) {
			if (maybeShowPopup(e)) {
				e.consume();
			}
		}
	
		public void mousePressed(MouseEvent e) {
			if (maybeShowPopup(e)) {
				e.consume();
			}
		}
	
		public void mouseClicked(MouseEvent e) {
			if (maybeShowPopup(e)) {
				e.consume();
			}
		}

	
	};
	
	public PersonIntervalListView() {
		super();
		super.setLayout(new BoxLayout(this, BoxLayout.PAGE_AXIS));
		super.addMouseListener(popupListener);
	}

	public void setGallery(PersonGallery galleryView) {
		this.galleryView = galleryView;
		this.gallery = galleryView.getModel();
	}

	public void setEntity(GalleryEntity entity) {
		this.entity = entity;
		assert entity != null;
	}

	public void resetPanels() {
		removeAll();
		if (entity == null) {
			return;
		}
		Iterator iter = entity.getRange().iterator();
		while (iter.hasNext()) {
			InstantInterval i = (InstantInterval) iter.next();
			SingleSpanDetail ssd = new SingleSpanDetail();
			ssd.setStartFrame(i.getStartInstant().intValue());
			ssd.setEndFrame(i.getEndInstant().intValue() - 1);
			add(ssd);
		}
		revalidate();
	}
	
	private Iterator getEvidenceInClickspan() {
		Iterator inSpan = new FilterIterator(entity.getEvidence(), new Predicate(){
			public boolean evaluate(Object o) {
				GalleryEvidence e = (GalleryEvidence) o;
				int f = e.getFrame();
				return clickStart <= f && f <= clickEnd;
			}
		});
		return inSpan;
	}
	
	private Action selectIntervalAction = new AbstractAction("Select") {
		{
			this.putValue(Action.SHORT_DESCRIPTION, "Display and Highlight the Highest Ranking Blob in this Interval");
		}
		public void actionPerformed(ActionEvent e) {
			Iterator inSpan = getEvidenceInClickspan();
			GalleryEvidence best = null;
			while (inSpan.hasNext()) {
				GalleryEvidence curr = (GalleryEvidence) inSpan.next();
				if (best == null || curr.getPriority() < best.getPriority() || (curr.getPriority() == best.getPriority() && curr.getSimilarity() > best.getSimilarity()) ) {
					best = curr;
				}
			}
			if (best != null) {
				gallery.setSelectedEvidence(best);
			}
		}
	};
	private Action splitIntervalHereAction = new AbstractAction("Split Interval at Current Frame") {
		public void actionPerformed(ActionEvent e) {
			if (!splitIntervalHereIsValid()) {
				return;
			}
			int here = gallery.getSelectedFrame();
			InstantRange ir = (InstantRange) entity.getRange().clone();
			ir.remove(new Frame(here), new Frame(here+1));
			entity.setValidRange(ir);
		}
	};
	private boolean splitIntervalInHalfIsValid() {
		return clickStart + 1 < clickEnd;
	}
	private boolean splitIntervalHereIsValid() {
		int here = gallery.getSelectedFrame();
		return clickStart < here && here < clickEnd;
	}
	private Action splitIntervalInHalfAction = new AbstractAction("Split Interval in Half") {
		public void actionPerformed(ActionEvent e) {
			if (!splitIntervalInHalfIsValid()) {
				return;
			}
			InstantRange ir = (InstantRange) entity.getRange().clone();
			int middle = clickStart + clickEnd / 2;
			ir.remove(new Frame(middle), new Frame(middle+1));
			/// XXX what about splits that end up leaving no evidence on one side?
			entity.setValidRange(ir);
		}
	};
	private Action unlabelIntervalAction = new AbstractAction("Mislabelled Interval") {
		{
			this.putValue(Action.SHORT_DESCRIPTION, "This Interval is for a Different Person");
		}
		public void actionPerformed(ActionEvent event) {
			ArrayList evidenceForNewPerson = new ArrayList();
			/// XXX unlabel might not preserve evidence priority order
			Iterator inSpan = getEvidenceInClickspan();
			while (inSpan.hasNext()) {
				evidenceForNewPerson.add(((GalleryEvidence) inSpan.next()).getSlice());
			}
			
			InstantRange ir = (InstantRange) entity.getRange().clone();
			Frame s = new Frame(clickStart);
			Frame e = new Frame(clickEnd+1);
			ir.remove(s, e);
			entity.setValidRange(ir);
			
			InstantRange nir = new InstantRange();
			nir.add(s, e);
			GalleryEntity newEntity = gallery.addEntity(null, nir);
			for (int i = 0; i < evidenceForNewPerson.size(); i++) {
				ImageSlice curr = (ImageSlice) evidenceForNewPerson.get(i);
				gallery.addEvidence(newEntity, curr.getFrame(), curr.getBox());
			}
		}
	};
	private Action deleteIntervalAction = new AbstractAction("Delete Interval") {
		{
			this.putValue(Action.SHORT_DESCRIPTION, "This is a Spurious Interval");
		}
		public void actionPerformed(ActionEvent e) {
			InstantRange ir = (InstantRange) entity.getRange().clone();
			ir.remove(new Frame(clickStart), new Frame(clickEnd+1));
			entity.setValidRange(ir);
		}
	};
	private Action goToFrameAction = new AbstractAction("Go to Frame") {
		public void actionPerformed(ActionEvent e) {
			this.putValue(Action.SHORT_DESCRIPTION, "Go to Frame " + clickFrame);
			gallery.setSelectedFrame(clickFrame);
		}
	};
	
	
	private int clickFrame = -1;
	private int clickStart = -1;
	private int clickEnd = -1;
	
	/**
	 * @param e
	 */
	private boolean maybeShowPopup(MouseEvent e) {
		if(maybeShowPopup(e, e.getComponent())) {
			return true;
		}
		return maybeShowPopup(e, PersonIntervalListView.this.getComponentAt(e.getPoint()));
	}

	/**
	 * @param e
	 * @param comp
	 */
	private boolean maybeShowPopup(MouseEvent e, Component comp) {
		FrameButton f = null;
		while (comp != null) {
			if (comp instanceof FrameButton) {
				f = (FrameButton) comp;
			} else if (comp instanceof SingleSpanDetail) {
				return PersonIntervalListView.this.maybeShowPopup(e, (SingleSpanDetail) comp, f);
			} else if (comp instanceof PersonIntervalListView) {
				return false;
			}
			comp = comp.getParent();
		}
		return false;
	}
	
	public boolean maybeShowPopup(MouseEvent event, SingleSpanDetail spanView, FrameButton button) {
		if (!event.isPopupTrigger()) {
			return false;
		}
		if (null == popup) {
			popup = new JPopupMenu("Edit Selected Interval");
			popup.add(new JMenuItem(selectIntervalAction));
			popup.add(new JSeparator());
			popup.add(new JMenuItem(splitIntervalHereAction));
			popup.add(new JMenuItem(splitIntervalInHalfAction));
			popup.add(new JMenuItem(unlabelIntervalAction));
			popup.add(new JMenuItem(deleteIntervalAction));
			popup.add(new JSeparator());
			popup.add(new JMenuItem(goToFrameAction));
		}
		if (null != button) {
			clickFrame = button.slice.getFrame();
			goToFrameAction.setEnabled(true);
		} else {
			clickFrame = -1;
			goToFrameAction.setEnabled(false);
		}
		splitIntervalHereAction.setEnabled(splitIntervalHereIsValid());
		splitIntervalInHalfAction.setEnabled(splitIntervalInHalfIsValid());
		
		clickStart = spanView.startTimeButton.slice.getFrame();
		clickEnd = spanView.endTimeButton.slice.getFrame();
		
		final Component whereComp = (Component) event.getComponent();
		final int whereX = event.getX();
		final int whereY = event.getY();
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				popup.show(whereComp, whereX, whereY);
			}
		
		});
		return true;
	}

	public Dimension getPreferredScrollableViewportSize() {
//		Dimension d = new Dimension(Short.MAX_VALUE, getPreferredSize().height);
		return getPreferredSize();//new Dimension(600, 352);
	}

	public int getScrollableUnitIncrement(Rectangle visibleRect, int orientation, int direction) {
		int wh = 0;
		if (direction < 0 && visibleRect.y != 0) {
			// scroll up one unit
			// makes the first one off the top the new top
			SingleSpanDetail op = getFirstAboveView(visibleRect);
			wh = visibleRect.y - op.getY();
		} else if (direction > 0) { 
			// scroll down one unit
			// makes the first one off the bottom at the bottom
			SingleSpanDetail op = getFirstBelowView(visibleRect);
			int opBottom = op.getHeight() + op.getY();
			int visBottom = visibleRect.height + visibleRect.y;
			wh = opBottom - visBottom;
		}
		// don't ever scroll more than the visible area
		return Math.min(visibleRect.height, wh);
	}
	private SingleSpanDetail getFirstBelowView(Rectangle visibleRect) {
		int y_end = visibleRect.y + visibleRect.height; // -1?
		SingleSpanDetail op = (SingleSpanDetail) getComponentAt(visibleRect.x, y_end);
		if (op.getY() + op.getHeight() == y_end) {
			op = (SingleSpanDetail) getComponentAt(visibleRect.x, y_end + 1);
		}
		return op;
	}

	private SingleSpanDetail getFirstAboveView(Rectangle visibleRect) {
		SingleSpanDetail op = (SingleSpanDetail) getComponentAt(visibleRect.x, visibleRect.y);
		if (op.getY() == visibleRect.y) {
			op = (SingleSpanDetail) getComponentAt(visibleRect.x, visibleRect.y - 1);
		}
		return op;
	}

	public int getScrollableBlockIncrement(Rectangle visibleRect, int orientation, int direction) {
		int wh = 0;
		if (direction < 0 && visibleRect.y != 0) {
			// scroll up one block
			// makes the first one off the top the new bottom
			SingleSpanDetail op = getFirstAboveView(visibleRect);
			int opBottom = op.getHeight() + op.getY();
			int visBottom = visibleRect.height + visibleRect.y;
			wh = visBottom - opBottom;
		} else if (direction > 0) { 
			// scroll down one block
			// makes the first one off the bottom at the top
			SingleSpanDetail op = getFirstBelowView(visibleRect);
			wh = op.getY() - visibleRect.y;
		}
		// don't ever scroll more than the visible area
		return Math.min(visibleRect.height, wh);
	}

	public boolean getScrollableTracksViewportWidth() {
		return false;
	}
	public boolean getScrollableTracksViewportHeight() {
		return false;
	}
}
