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
import java.beans.*;
import java.util.*;

import javax.swing.*;
import javax.swing.event.*;

import viper.api.time.*;
import edu.umd.cfar.lamp.chronicle.markers.*;
import edu.umd.cs.piccolo.*;
import edu.umd.cs.piccolo.event.*;
import edu.umd.cs.piccolo.util.*;

/**
 * A basic timeline control. Presents three segments: the row headers, the
 * ruler, and the lines themselves. If you wish to put this in a scroll pane,
 * set labelsOutside to 'true' and use the get_Header methods to set up the
 * scroll pane.
 * 
 * @author davidm
 */
public class ChronicleViewer extends PCanvas {
	
	
	/// Generates the piccolo views of the timeline nodes
	private RendererCatalogue factory;

	/// Maintains the list of timeline input event handlers
	private EventListenerList listenerList = new EventListenerList();

	private double footerHeight = 0;

	/// the y-zoom
	private double lineZoom = 1.0;

	/// The total height of the lines
	private double linesHeight = 0;

	/// The number of units of length to devote to the chronicle's lines
	private double viewLength = 600;

	/// The length of the headers before each line (where the label is)
	private double labelLength = 128;

	/// The data model used.
	protected ChronicleViewModel model;

	/// The selection model; useful for selecting lines and ranges of time
	protected ChronicleSelectionModel selectionModel;

	/// Listens for changes in the data and redraws the display as appropriate
	private ChronicleListener cl = new ChronicleListener();

	/// This holds all of the user specfied markers and the current frame arrow
	private AllChronicleMarkersNode markersNode;

	/// This color is the background for the headers
	private Paint headColor = Color.lightGray;

	private ScrollViews scrollViews = null;

	/**
	 * The scroll views are windows for the corresponding segements of a java
	 * swing scroll pane.
	 */
	public class ScrollViews {
		/**
		 * The west widget scrolls vertically. This includes timeline names and
		 * emblems.
		 */
		public PCanvas rowHeader = null;

		/**
		 * The northwest widget doesn't scroll at all.
		 */
		public PCanvas cornerHeader = null;

		/**
		 * The north scroll view, this scrolls only horizontally. This includes
		 * the ruler, frame numbers, and any marker headers.
		 */
		public PCanvas columnHeader = null;

		/**
		 * The content widget scrolls both vertically and horizontally.
		 */
		public PCanvas content = null;

		private PCamera cornerCam;

		private PCamera labelCam;

		private PCamera rulerCam;

		private PCamera contentCam;

		ScrollViews() {
			labelCam = new PCamera();
			labelCam.addLayer(getLayer());
			this.rowHeader = new SimpleCanvas(labelCam);
			this.rowHeader
					.addInputEventListener(new ChroniclePanEventHandler());
			this.rowHeader.addInputEventListener(new TimeLineInputProxy());

			cornerCam = new PCamera();
			cornerCam.addLayer(getLayer());
			this.cornerHeader = new SimpleCanvas(cornerCam);
			this.cornerHeader
					.addInputEventListener(new ChroniclePanEventHandler());

			rulerCam = new PCamera();
			rulerCam.addLayer(getLayer());
			this.columnHeader = new SimpleCanvas(rulerCam);
			columnHeader.addInputEventListener(new ChronicleWheelZoomHandler(
					columnHeader));
			columnHeader.addInputEventListener(new ChroniclePanEventHandler());
			columnHeader.addInputEventListener(new TimeLineInputProxy());

			contentCam = new PCamera();
			contentCam.addLayer(getLayer());
			content = new SimpleCanvas(contentCam);
			//			content.getLayer().addChild(contentCam);

			resetCameras();
			content.getCamera().addPropertyChangeListener(
					PCamera.PROPERTY_VIEW_TRANSFORM, recenterOtherCameras);
			content.addComponentListener(resizeEventListener);
			content
					.addInputEventListener(new ChronicleWheelZoomHandler(
							content));
			content.addInputEventListener(new ChroniclePanEventHandler());
			content.addInputEventListener(new TimeLineInputProxy());
		}

		/**
		 * Sets the pan handler on all four views.
		 * 
		 * @param handler
		 *            the new handler
		 */
		public void setPanEventHandler(PPanEventHandler handler) {
			this.rowHeader.setPanEventHandler(handler);
			this.cornerHeader.setPanEventHandler(handler);
			this.columnHeader.setPanEventHandler(handler);
			this.content.setPanEventHandler(handler);
		}

		/**
		 * Sets the zoom handler on all four views.
		 * 
		 * @param handler
		 *            the new handler
		 */
		public void setZoomEventHandler(PZoomEventHandler handler) {
			this.rowHeader.setZoomEventHandler(handler);
			this.cornerHeader.setZoomEventHandler(handler);
			this.columnHeader.setZoomEventHandler(handler);
			this.content.setZoomEventHandler(handler);
		}

		void resetCameras() {
			double vl = getViewLength();
			double vh = linesHeight + footerHeight;

			cornerCam.setViewOffset(0, 0);
			cornerCam.setBounds(0, 0, labelLength, getRulerHeight());
			cornerHeader.setPreferredSize(new Dimension((int) getLabelLength(),
					getRulerHeight()));

			rulerCam.setViewOffset(-labelLength, 0);
			rulerCam.setBounds(0, 0, vl, getRulerHeight());
			columnHeader.setPreferredSize(new Dimension((int) vl,
					getRulerHeight()));

			labelCam.setViewOffset(0, -getRulerHeight());
			labelCam.setBounds(0, 0, labelLength, vh);
			rowHeader.setPreferredSize(new Dimension((int) labelLength,
					(int) vh));

			contentCam.setViewOffset(-labelLength, -getRulerHeight());
			contentCam.setBounds(0, 0, vl, vh);
			content.setPreferredSize(new Dimension((int) vl, (int) vh));
		}

		/// This listens for resize events and appropriately changes the
		/// viewLength, making sure that it is at least as large as the current
		// view
		private ComponentListener resizeEventListener = new ComponentAdapter() {
			public void componentResized(ComponentEvent e) {
				if (getViewLength() < getMinimumViewLength()) {
					setViewLength(getMinimumViewLength());
				} else {
					resetCameras();
				}
			}
		};

		private PropertyChangeListener recenterOtherCameras = new PropertyChangeListener() {
			public void propertyChange(PropertyChangeEvent evt) {
				if (evt.getPropertyName().equals(
						PCamera.PROPERTY_VIEW_TRANSFORM)) {
					//					resetCameras();
				}
			}
		};
	}

	private ChronicleMarkerListener cml = new ChronicleMarkerListener() {
		public void markersChanged(ChronicleMarkerEvent e) {
			if (e.getType() == ChronicleMarkerEvent.MOVED) {
				if (e.getChangedMarker().equals(getMajorMomentMarker())) {
					getModel().setMajorMoment(e.getChangedMarker().getWhen());
				}
			}
		}
	};

	/// The popup that appears when a use context-clicks on a marker.
	private ChronicleMarkerPopUp markerPop;

	private ChronicleMarker getMajorMomentMarker() {
		return markersNode.getModel().getMarker(0);
	}

	/**
	 * Gets the marker model associated with this viewer.
	 * 
	 * @return the marker model
	 */
	public ChronicleMarkerModel getMarkerModel() {
		return markersNode.getModel();
	}

	private class ChronicleListener implements ChronicleViewListener {
		/**
		 * Calls resetView {@inheritDoc}
		 */
		public void focusChanged(EventObject e) {
			// XXX Should only redo what's changed, instead of everything
			// focus change indicates need to redo markers, but only whole
			// view if focusInterval changed.
			resetView();
			overlay.redoLength();
			overlay.redoHeight();
		}

		/**
		 * Calls resetView {@inheritDoc}
		 */
		public void dataChanged(EventObject e) {
			resetView();
			overlay.redoLength();
			overlay.redoHeight();
		}
	};

	private class OverlayLayoutManager extends PLayer {
		private boolean dirtyLength = false;
		private boolean dirtyHeight = false;
		public void redoLength() {
			dirtyLength = true;
			invalidateLayout();
		}
		public void redoHeight() {
			dirtyHeight = true;
			invalidateLayout();
		}
		
		public void layoutChildren() {
			if (dirtyLength || dirtyHeight) {
				Iterator i = getChildrenIterator();
				while (i.hasNext()) {
					PNode each = (PNode) i.next();
					if (each instanceof NodeWithViewLength) {
						NodeWithViewLength n = (NodeWithViewLength) each;
						if (dirtyHeight && dirtyLength) {
							n.viewAndDataChanged();
						} else if (dirtyHeight) {
							n.dataHeightChanged();
						} else {
							n.viewLengthChanged();
						}
					}
				}
			}
			dirtyLength = false;
			dirtyHeight = false;
		}
	}
	
	private class LineLayoutManager extends PNode {
		double[] lineOffsets = new double[0];

		double instant2local(Instant i) {
			double offset = i.doubleValue();
			double focalWidth = model.getFocus().width();
			offset = offset * getViewLength() / focalWidth;
			return offset;
		}

		Instant local2instant(double d) {
			long focalWidth = model.getFocus().width();
			long offset = (long) (d * focalWidth / getViewLength());
			return model.getFocus().getStartInstant().go(offset);
		}

		double[] timeline2local(TimeLine tqe) {
			int i = model.indexOf(tqe);
			if (i < 0) {
				return null;
			}
			return new double[] { lineOffsets[i+1], lineOffsets[i + 2] };
		}

		TimeLine local2timeline(double d) {
			int i = Arrays.binarySearch(lineOffsets, d);
			if (i < 0) {
				// i = -insertionPoint - 1 -> insertionPoint = -i - 1 -> line =
				// -i - 2
				i = -i - 2;
			} else {
				i--;
			}
			if (i < 1 || i >= lineOffsets.length - 1) {
				return null;
			}
			return model.getElementAt(i-1); // take into account ruler
		}

		void layoutHorizontalChildren() {
			double xOffset = this.getX();
			double yOffset = this.getY();

			Iterator i = getChildrenIterator();
			int offsetIndex = 0;
			while (i.hasNext()) {
				PNode each = (PNode) i.next();
				each.setOffset(xOffset, yOffset - each.getY());
				lineOffsets[offsetIndex++] = yOffset;
				yOffset += each.getHeight();
			}
			lineOffsets[offsetIndex] = yOffset;
		}

		void layoutVerticalChildren() {
			double xOffset = this.getWidth();
			double yOffset = labelLength;

			Iterator i = getChildrenIterator();
			int offsetIndex = 0;
			while (i.hasNext()) {
				PNode each = (PNode) i.next();
				each.setOffset(xOffset - each.getX(), yOffset);
				lineOffsets[offsetIndex++] = xOffset;
				xOffset += each.getWidth();
			}
			lineOffsets[offsetIndex] = xOffset;
		}

		/**
		 * {@inheritDoc}
		 */
		public void layoutChildren() {
			if (lineOffsets.length != getChildrenCount()+1) {
				lineOffsets = new double[getChildrenCount() + 1];
			}
			if (getOrientation() == Adjustable.VERTICAL) {
				layoutVerticalChildren();
			} else {
				layoutHorizontalChildren();
			}
		}
	}

	private LineLayoutManager lineLayer;

	private LineLayoutManager labelLayer;
	
	private OverlayLayoutManager overlay;

	private ChronicleRuler underlay;
	
	private PNode headerSlug;

	private PNode cornerNode;

	/**
	 * String indicating
	 */
	public static final String CURR_FRAME_LABEL = "Current Frame";

	private class TimeLineInputProxy implements PInputEventListener {
		public void processEvent(PInputEvent e, int type) {
			fireTimeLineInputEvent(e, type);
		}
	}
	
	/**
	 * Refreshes the chronicle display.
	 */
	public void resetView() {
		recenter();
		resetLineLayer();
		resetMarkers();
		if (scrollViews != null) {
			scrollViews.resetCameras();
		}
	}

	/**
	 * Makes sure the current view contains the extents of the focus.
	 */
	private void recenter() {
		PCamera c = getCamera();
		// Find what the first and last pixel should be
		// Ignore this if focus is null or empty
		if (model == null || model.getFocus() == null
				|| model.getFocus().isEmpty()) {
			return;
		}
		PAffineTransform camTransform = c.getViewTransformReference();
		double currXOffset = -camTransform.getTranslateX();
		double totalXWidth = getViewLength() + getViewOffsetX();
		double maxXOffset = totalXWidth - c.getBoundsReference().getWidth();
		if (maxXOffset < currXOffset) {
			c.setViewOffset(-maxXOffset, camTransform.getTranslateY());
		}
	}

	/**
	 * Changes the focus to the current focus of the mfl, and changes the cursor
	 * to lay over the current major moment.
	 */
	public void resetMarkers() {
		this.markersNode
				.setBounds(getViewOffsetX(), 0, getViewLength(),
						getPreferredViewHeight() + getViewOffsetY()
								+ getFooterHeight());
		if (model != null) {
			InstantInterval all = model.getFocus();
			if (all == null) {
				return;
			}
			markersNode.setHeaderHeight(getRulerHeight());
			markersNode.setFooterHeight(footerHeight);
			markersNode.getModel().setInterval(all);
			ChronicleMarker nowMarker = getMajorMomentMarker();
			nowMarker.setWhen(model.getMajorMoment());
		}
		this.getLayer().addChild(this.markersNode);
	}

	/**
	 * Refreshes the timelines.
	 */
	public void resetLineLayer() {
		// clean line layer
		lineLayer.removeAllChildren();
		headerSlug.setBounds(0, 0, getViewLength(), getRulerHeight());
		lineLayer.addChild(headerSlug);
		linesHeight = 0;

		// clean label layer
		labelLayer.removeAllChildren();
		boolean horizontal = this.getOrientation() == SwingConstants.HORIZONTAL;
		cornerNode.setBounds(0, 0, horizontal ? labelLength : getRulerHeight(),
				horizontal ? getRulerHeight() : labelLength);
		labelLayer.addChild(cornerNode);

		if (model != null) {
			InstantInterval focus = model.getFocus();
			for (int i = 0; i < model.getSize(); i++) {
				TimeLine tqe = model.getElementAt(i);
				TimeLineRenderer renderer = factory.getTimeLineRenderer(tqe);
				boolean currSelected = getSelectionModel().isSelected(tqe);
				boolean currFocus = false; // XXX implement focus
				double neoSize = renderer.getPreferedTimeLineInfoLength(this,
						tqe, currSelected, currFocus, (int) viewLength,
						getOrientation());
				PNode view = renderer.getTimeLineRendererNode(this, tqe,
						currSelected, currFocus, (int) viewLength,
						(int) neoSize, getOrientation());
				lineLayer.addChild(view);

				PNode newLabel = renderer.generateLabel(this, tqe, currSelected, currFocus, (int) neoSize,
						getOrientation());
				PBounds viewBounds = view.getBoundsReference();
				double prefHeight = newLabel.getFullBounds().getHeight();
				if (prefHeight > viewBounds.getHeight()) {
					view.setBounds(viewBounds.x, viewBounds.y,
							viewBounds.width, prefHeight);
					newLabel.setBounds(0, 0, getViewOffsetX(), prefHeight);
				} else {
					newLabel.setBounds(0, 0, getViewOffsetX(), viewBounds
							.getHeight());
				}
				labelLayer.addChild(newLabel);
				if (this.getOrientation() == Adjustable.HORIZONTAL) {
					linesHeight += view.getBoundsReference().height;
				} else {
					linesHeight += view.getBoundsReference().width;
				}
			}
		}
		underlay.setBounds(0, 0, viewLength, getPreferredViewHeight() + getViewOffsetY()
				+ getFooterHeight());
		underlay.setOffset(getViewOffsetX(), 0);
	}

	/**
	 * Zoom out to fit the timelines in the window.
	 */
	public void fitInWindow() {
		Instant i = model.getMajorMoment();
		if (i != null) {
			this.setZoom(1, model.getMajorMoment());
		}
	}

	/**
	 * {@inheritDoc}
	 */
	public Dimension getPreferredSize() {
		double height = getRulerHeight() + linesHeight;
		double width = labelLength + getViewLength();
		return new Dimension((int) (width + .9), (int) (height + .9));
	}

	/**
	 * {@inheritDoc}
	 */
	public Dimension getMinimumSize() {
		int height = getRulerHeight();
		int width = (int) Math.ceil(getLabelLength());
		return new Dimension(1 + width, 1 + height);
	}

	/**
	 * {@inheritDoc}
	 */
	public Dimension getMaximumSize() {
		return new Dimension(Integer.MAX_VALUE, Integer.MAX_VALUE);
	}

	private int orientation = Adjustable.HORIZONTAL;

	/**
	 * Gets the orientation of the lines, either horizontal or vertical.
	 * 
	 * @return
	 */
	public int getOrientation() {
		return orientation;
	}

	/**
	 * Sets the orientation of the widget.
	 * 
	 * @param newOrientation
	 *            {@link Adjustable#HORIZONTAL}or {@link Adjustable#VERTICAL}
	 */
	public void setOrientation(int newOrientation) {
		if (newOrientation != this.orientation) {
			switch (newOrientation) {
			case Adjustable.HORIZONTAL:
			case Adjustable.VERTICAL:
				invalidate();
				break;
			default:
				String err = "Not a valid orientation";
				throw new IllegalArgumentException(err);
			}
			int oldOrientation = this.orientation;
			this.orientation = newOrientation;
			firePropertyChange("orientation", oldOrientation, newOrientation);
		}
	}

	/**
	 * Gets the y-direction zoom ratio.
	 * 
	 * @return the y zoom
	 */
	public double getYZoom() {
		return lineZoom;
	}

	/**
	 * Sets the y-direction zoom ratio.
	 * 
	 * @param yZoom
	 *            the y zoom
	 */
	public void setYZoom(double yZoom) {
		if (this.lineZoom != yZoom) {
			double oldZoom = this.lineZoom;
			this.lineZoom = yZoom;
			resetLineLayer();
			overlay.redoHeight();
			firePropertyChange("yZoom", oldZoom, yZoom);
		}
	}

	/**
	 * Gets where the timelines actually start in the view.
	 * 
	 * @return the label length
	 */
	public double getViewOffsetX() {
		return labelLength;
	}

	/**
	 * Gets where the timelines actually start in the view.
	 * 
	 * @return the ruler height
	 */
	public double getViewOffsetY() {
		return getRulerHeight();
	}

	/**
	 * Gets the actual length of the view, in pixels.
	 * 
	 * @return the length of the view
	 */
	public double getViewLength() {
		return viewLength;
	}

	/**
	 * Gets the preferred height of the canvas - this is the sum of the timeline
	 * heights.
	 * 
	 * @return
	 */
	public double getPreferredViewHeight() {
		return linesHeight;
	}

	/**
	 * Gets the height of the scroll window aperture.
	 * 
	 * @return the height of the scroll window
	 */
	public double getVisibleViewHeight() {
		return getBounds().getHeight() - getViewOffsetY();
	}

	/**
	 * Gets the overall view height. This is necessary, as the actual height can
	 * be greater than the preferred height, and some things need to be
	 * bottom-aligned.
	 * 
	 * @return the maxium of the preferred and visible heights.
	 */
	public double getViewHeight() {
		return Math.max(getPreferredViewHeight(), getVisibleViewHeight());
	}

	/**
	 * Sets the actual view length. I've been unable to determine this
	 * programatically, at least reliably, so it is best to just keep this up to
	 * date. You shouldn't have to worry about it, unless you put the canvas in
	 * some strange, non-swing holder.
	 * 
	 * @param viewWidth
	 *            the new length
	 */
	public void setViewLength(double viewWidth) {
		if (this.viewLength != viewWidth) {
			double oldViewWidth = this.viewLength;
			this.viewLength = viewWidth;
			resetView();
			overlay.redoLength();
			firePropertyChange("viewLength", oldViewWidth, viewWidth);
		}
	}

	/**
	 * Small enough to fit all the lines into the current view, but still take
	 * up available space.
	 * 
	 * @return
	 */
	private double getMinimumViewLength() {
		if (scrollViews == null) {
			return Math.max(0, super.getBounds().getWidth() - labelLength);
		} else {
			return Math.max(0, scrollViews.content.getWidth());
		}
	}

	/**
	 * Gets a view length that fits one instant in one width of the view.
	 * 
	 * @return
	 */
	private double getMaximumViewLength() {
		if (this.model == null || this.model.getFocus() == null) {
			return getMinimumViewLength();
		}
		double minLengh = getMinimumViewLength();
		long focalWidth = model.getFocus().width();
		double maxLength = focalWidth * labelLength;
		assert focalWidth >= 0;
		maxLength += labelLength;
		return Math.max(minLengh, maxLength);
	}

	/**
	 * Gets the data model behind this chronicle view.
	 * 
	 * @return ChronicleModel the supporting model
	 */
	public ChronicleViewModel getModel() {
		return model;
	}

	/**
	 * Sets the model to display.
	 * 
	 * @param cm
	 *            the new model
	 */
	public void setModel(ChronicleViewModel cm) {
		if (this.model != cm) {
			ChronicleViewModel oldModel = this.model;
			if (this.model != null) {
				this.model.removeChronicleViewListener(cl);
			}
			this.model = cm;
			if (cm != null) {
				this.model.addChronicleViewListener(cl);
			}
			resetView();
			overlay.redoLength();
			overlay.redoHeight();
			firePropertyChange("model", oldModel, cm);
		}
	}

	/**
	 * Sets the factory used to generate timeline factories.
	 * 
	 * @param metafact
	 *            the factory object that will generate templates for each
	 *            timeline
	 */
	public void setRendererCatalogue(RendererCatalogue cat) {
		if (this.factory != cat) {
			RendererCatalogue old = this.factory;
			this.factory = cat;
			firePropertyChange("rendererCatalogue", old, cat);
		}
	}

	/**
	 * Gets the timeline view metafactory.
	 * 
	 * @return the factory object that is used to generate templates for each
	 *         timeline
	 */
	public RendererCatalogue getRendererCatalogue() {
		return this.factory;
	}

	class PopupActionListener extends MouseAdapter {
		private PCanvas canvas;

		PopupActionListener(PCanvas c) {
			this.canvas = c;
		}

		/**
		 * Shows popup if a popup exists for the location and the mouse event
		 * indicates a popup request. {@inheritDoc}
		 */
		public void mousePressed(MouseEvent e) {
			maybeShowPopup(e);
		}

		/**
		 * Shows popup if a popup exists for the location and the mouse event
		 * indicates a popup request. {@inheritDoc}
		 */
		public void mouseReleased(MouseEvent e) {
			maybeShowPopup(e);
		}

		private void maybeShowPopup(MouseEvent e) {
			if (e.isPopupTrigger()) {
				PCamera pc = canvas.getCamera();
				PPickPath ppp = pc.pick(e.getX(), e.getY(), 4);
				PNode bottom = ppp.getPickedNode();
				while (bottom != pc && bottom != null) {
					if (bottom instanceof ChronicleMarkerNode) {
						markerPop.setCanvas(canvas);
						ChronicleMarkerNode cmn = (ChronicleMarkerNode) bottom;
						markerPop.show(e.getX(), e.getY(), e.getComponent(),
								cmn);
						return;
					}
					bottom = bottom.getParent();
				}
			}
		}
	}

	/**
	 * Modified version of Piccolo's PPanEventHandler to support middle-mouse
	 * button panning on the chronicle.
	 */
	public class ChroniclePanEventHandler extends PDragSequenceEventHandler {

		private boolean autopan;

		private double minAutopanSpeed = 5;

		private double maxAutopanSpeed = 15;

		/**
		 * Creates a new pan handler for the outer class.
		 */
		public ChroniclePanEventHandler() {
			super();
			PInputEventFilter filter = new PInputEventFilter(
					InputEvent.BUTTON2_MASK, InputEvent.ALT_DOWN_MASK
							| InputEvent.CTRL_DOWN_MASK);
			setEventFilter(filter);
			setAutopan(true);
		}

		protected void drag(PInputEvent e) {
			super.drag(e);
			pan(e);
		}

		private double squelchPanSpeed(double speed) {
			if (Math.abs(speed) < minAutopanSpeed) {
				speed = 0;
			} else if (speed < -minAutopanSpeed) {
				speed += minAutopanSpeed;
				speed = Math.max(speed, -maxAutopanSpeed);
			} else if (speed > minAutopanSpeed) {
				speed -= minAutopanSpeed;
				speed = Math.min(speed, maxAutopanSpeed);
			}
			return speed;
		}

		private void autoPan(PInputEvent e) {
			// Calculate amount to pan
			Point2D last = super.getMousePressedCanvasPoint();
			Point2D current = e.getCanvasPosition();
			PDimension r = new PDimension(current.getX() - last.getX(), current
					.getY()
					- last.getY());
			e.getPath().canvasToLocal(r, e.getCamera());
			PDimension d = (PDimension) e.getCamera().localToView(r);
			d.height = -squelchPanSpeed(d.height);
			d.width = -squelchPanSpeed(d.width);

			// Make sure panning doesn't go out of range
			PCamera c;
			if (scrollViews == null) {
				c = ChronicleViewer.this.getCamera();
			} else {
				c = scrollViews.content.getCamera();
			}
			PAffineTransform T = c.getViewTransform();
			double ty = T.getTranslateY() + d.height;
			double viewHeight = getViewHeight() + getFooterHeight();
			double cameraHeight = c.getHeight();
			ty = Math.max(ty, cameraHeight - viewHeight); // make sure it
														  // doesn't go past the
														  // bottom
			ty = Math.min(0, ty); // make sure it doesn't pan up

			double tx = T.getTranslateX() + d.width;
			tx = Math.min(0, tx); // make sure it doesn't pan to the left of the
								  // start
			tx = Math.max(tx, c.getWidth() - getViewLength()); // or right of
															   // end

			T.setOffset(tx, ty);
			c.setViewTransform(T);
		}

		protected void pan(PInputEvent e) {
			PCamera c = e.getCamera();
			Point2D l = e.getPosition();

			if (c.getViewBounds().contains(l)) {
				if (autopan) {
					autoPan(e);
				} else {
					PDimension d = e.getDelta();
					c.translateView(d.getWidth(), d.getHeight());
				}
			}
		}

		//****************************************************************
		// Auto Pan
		//****************************************************************

		/**
		 * Turns on or off autopan.
		 * 
		 * @param autopan
		 *            whether to use panning like in internet explorer, where
		 *            dragging the mouse is like a joystick
		 */
		public void setAutopan(boolean autopan) {
			this.autopan = autopan;
		}

		/**
		 * Gets whether autopan is enabled.
		 * 
		 * @return if panning is like in internet explorer, where dragging the
		 *         mouse is like a joystick
		 */
		public boolean getAutopan() {
			return autopan;
		}

		/**
		 * Set the minimum speed for autopan (outside of the dead region near
		 * where the mouse was clicked)
		 * 
		 * @param minAutopanSpeed
		 *            the minimum speed
		 */
		public void setMinAutopanSpeed(double minAutopanSpeed) {
			this.minAutopanSpeed = minAutopanSpeed;
		}

		/**
		 * This is the maximum autopan speed, which happens when the mouse is
		 * dragged outside the region of accelleration.
		 * 
		 * @param maxAutopanSpeed
		 *            the maximum speed
		 */
		public void setMaxAutopanSpeed(double maxAutopanSpeed) {
			this.maxAutopanSpeed = maxAutopanSpeed;
		}

		/**
		 * Do auto panning even when the mouse is not moving. {@inheritDoc}
		 */
		protected void dragActivityStep(PInputEvent aEvent) {
			if (!autopan)
				return;
			autoPan(aEvent);
		}
	}

	/**
	 * The right-click zoom handler for the chronicle has been deprecated in
	 * favor of the scroll wheel zoom handler. I'm leaving it in the code in
	 * case someone insists on using it.
	 */
	public class ChronicleRightClickZoomHandler extends
			PDragSequenceEventHandler {
		// Zoom in, while keeping 'zoomIntoInstant' at point 'zoomPoint'
		// the instant i is found at point zoomPoint in global coords
		private double zoomPoint;

		private Instant zoomIntoInstant;

		private PCanvas canvas;

		/**
		 * Creates a new zoom handler.
		 * 
		 * @param canvas
		 *            the canvas to zoom into when drag events occur
		 */
		public ChronicleRightClickZoomHandler(PCanvas canvas) {
			super();
			this.canvas = canvas;
			setEventFilter(new PInputEventFilter(InputEvent.BUTTON3_MASK));
		}

		protected void dragActivityFirstStep(PInputEvent aEvent) {
			Point2D p = aEvent.getPosition();
			zoomPoint = p.getX()
					+ canvas.getCamera().getViewTransform().getTranslateX();
			if (zoomPoint > getViewOffsetX() && model != null
					&& model.getFocus() != null) {
				// If on the chronicle and not on the row header area
				double localX = underlay.globalToLocal(p).getX();
				zoomIntoInstant = lineLayer.local2instant(localX);
				super.dragActivityFirstStep(aEvent);
			} // else on the row header. don't do zooming there (yet?)
		}

		protected void dragActivityStep(PInputEvent aEvent) {
			if (zoomIntoInstant == null) {
				return;
			}
			double dx = aEvent.getCanvasPosition().getX()
					- getMousePressedCanvasPoint().getX();
			double scaleDelta = (1.0 + (0.001 * dx));
			setZoom(getZoomAmount() * scaleDelta, zoomIntoInstant, zoomPoint);
		}
	}

	/**
	 * The wheel zoom handler zooms in when the user scrolls up, and out when
	 * the user scrolls down.
	 */
	public class ChronicleWheelZoomHandler extends PBasicInputEventHandler {
		private PCanvas canvas;

		/**
		 * Creates a new zoom handler.
		 * 
		 * @param canvas
		 *            the canvas to zoom in when scroll wheel events occur
		 */
		public ChronicleWheelZoomHandler(PCanvas canvas) {
			super();
			this.canvas = canvas;
			PInputEventFilter scrollFilter = new PInputEventFilter();
			scrollFilter.rejectAllEventTypes();
			scrollFilter.setAcceptsMouseWheelRotated(true);
			setEventFilter(scrollFilter);
		}

		/**
		 * {@inheritDoc}
		 */
		public void mouseWheelRotated(PInputEvent event) {
			if (model == null || model.getFocus() == null) {
				resetView();
			} else {
				Point2D p = event.getPosition();
				double clickPoint = p.getX();
				double offset;
				if (scrollViews == null) {
					offset = canvas.getCamera().getViewTransform()
							.getTranslateX();
				} else {
					offset = -labelLength;
				}
				double zoomPoint = clickPoint + offset;

				// If on the chronicle and not on the row header area
				double localX = underlay.globalToLocal(p).getX();
				Instant zoomIntoInstant = lineLayer.local2instant(localX);

				// Zoom
				int r = event.getWheelRotation();
				double scaleDelta = (1.0 - (0.125 * r));

				setZoom(getZoomAmount() * scaleDelta, zoomIntoInstant,
						zoomPoint);
			}
		}

		/**
		 * {@inheritDoc}
		 */
		public void mouseWheelRotatedByBlock(PInputEvent event) {
			super.mouseWheelRotatedByBlock(event);
		}
	}

	/**
	 * Resets the zoom of the chronicle, keeping the given instant at the same
	 * position (if possible).
	 * 
	 * @param zoomAmount
	 * @param where
	 */
	public void setZoom(double zoomAmount, Instant where) {
		setZoom(zoomAmount, where, instant2pixelDistance(where));
	}

	/**
	 * Zooms in on the specified moment in time. Keeps the instant
	 * <code>where</code> located at the point <code>zoomPoint</code>.
	 * 
	 * @param zoomAmount
	 * @param where
	 *            the instant the user zooms in toward
	 * @param zoomPoint
	 *            the place the user put her mouse
	 */
	private void setZoom(double zoomAmount, Instant where, double zoomPoint) {
		// Zoom
		if (zoomAmount < 1) {
			zoomAmount = 1;
		}
		double newViewLength = getMinimumViewLength() * zoomAmount;
		newViewLength = Math.min(getMaximumViewLength(), newViewLength);
		newViewLength = Math.max(getMinimumViewLength(), newViewLength);
		if (getViewLength() == newViewLength) {
			return;
		}
		zoomAmount = newViewLength / getViewLength();

		// Recenter
		PCamera c;
		if (scrollViews == null) {
			c = ChronicleViewer.this.getCamera();
		} else {
			c = scrollViews.content.getCamera();
		}
		PAffineTransform T = c.getViewTransform();
		double newZoomPoint = zoomPoint * zoomAmount;
		double tx = zoomPoint - newZoomPoint + T.getTranslateX();
		double ty = T.getTranslateY();
		tx = Math.min(0, tx); // make sure it doesn't zoom to the left of the
							  // start
		tx = Math.max(tx, c.getWidth() - newViewLength); // or right of end

		T.setOffset(tx, ty);
		c.setViewTransform(T);
		setViewLength(newViewLength);
	}

	/**
	 * Gets the current zoom ratio.
	 * 
	 * @return the current zoom ratio
	 */
	public double getZoomAmount() {
		if (getViewLength() == 0 || getMinimumViewLength() == 0) {
			return 0;
		}
		return getViewLength() / getMinimumViewLength();
	}

	/**
	 * Gets the hight of the ruler, in pixels.
	 * 
	 * @return the ruler height
	 */
	public int getRulerHeight() {
		return underlay.getRulerHeight();
	}

	public ChronicleRuler getRuler() {
		return underlay;
	}
	
	/**
	 * Gets the height of the footer line.
	 * 
	 * @return the height of the footer
	 */
	public double getFooterHeight() {
		return footerHeight;
	}

	/**
	 * Gets the color to be used in the header.
	 * 
	 * @return the head color
	 */
	public Paint getHeadColor() {
		return headColor;
	}

	/**
	 * Gets the length of the timeline labels.
	 * 
	 * @return the number of pixels to reserve for the timeline names and
	 *         emblems
	 */
	public double getLabelLength() {
		return labelLength;
	}

	/**
	 * Sets the height to reserve for the footer.
	 * 
	 * @param newFooterHeight
	 *            the height for the footer row
	 */
	public void setFooterHeight(double newFooterHeight) {
		if (footerHeight != newFooterHeight) {
			double oldFooterHeight = footerHeight;
			footerHeight = newFooterHeight;
			firePropertyChange("footerHeight", oldFooterHeight, newFooterHeight);
			invalidate();
		}
	}

	/**
	 * Sets the color for the header.
	 * 
	 * @param paint
	 *            the head color
	 */
	public void setHeadColor(Paint paint) {
		if (headColor != paint) {
			Paint oldPaint = headColor;
			headColor = paint;
			invalidate();
			firePropertyChange("headColor", oldPaint, paint);
		}
	}

	/**
	 * Sets the length to reserve for line labels.
	 * 
	 * @param d
	 *            the size of the space to use for line names and emblems
	 */
	public void setLabelLength(double d) {
		if (labelLength != d) {
			double oldLabelLength = this.labelLength;
			this.labelLength = d;
			invalidate();
			firePropertyChange("labelLength", oldLabelLength, d);
		}
	}

	/**
	 * Sets the height to allocate for the header, including the ruler and
	 * marker labels.
	 * 
	 * @param d
	 *            the ruler height
	 */
	public void setRulerHeight(int d) {
		if (this.getRulerHeight() != d) {
			int oldRulerHeight = getRulerHeight();
			underlay.setRulerHeight(d);
			invalidate();
			firePropertyChange("rulerHeight", oldRulerHeight, d);
		}
	}

	/**
	 * Gets the markers node.
	 * 
	 * @return the marker node
	 */
	public AllChronicleMarkersNode getMarkersNode() {
		return markersNode;
	}

	/**
	 * Gets the instant beneath the point.
	 * 
	 * @param point2D
	 *            the point
	 * @return the instant beneath the point
	 */
	public Instant getInstantFor(Point2D localPoint) {
		return lineLayer
				.local2instant(getOrientation() == SwingConstants.HORIZONTAL ? localPoint
						.getX()
						: localPoint.getY());
	}

	/**
	 * Gets the instant beneath the event's point.
	 * 
	 * @param aEvent
	 *            the event
	 * @return the instant beneath the event
	 */
	public Instant getInstantFor(PInputEvent aEvent) {
		return getInstantFor(getLocalPosition(aEvent));
	}

	/**
	 * Gets the timeline beneath the point.
	 * 
	 * @param point2D
	 *            the point, relative to the top of the timeline
	 * @return the timeline beneath the point
	 */
	public TimeLine getTimeLineFor(Point2D global) {
		double offset = getViewOffsetY();
		if (getOrientation() == SwingConstants.HORIZONTAL) {
			offset += global.getY();
		} else {
			offset += global.getX();
		}
		return lineLayer.local2timeline(offset);
	}

	
	/**
	 * Gets the line beneath the event's point.
	 * 
	 * @param aEvent
	 *            the event
	 * @return the line beneath the event
	 */
	public TimeLine getTimeLineFor(PInputEvent aEvent) {
		Point2D localPosition = getLocalPosition(aEvent);
		return getTimeLineFor(localPosition);
	}

	/**
	 * @param aEvent
	 * @return
	 */
	public Point2D getLocalPosition(PInputEvent aEvent) {
		// first check the event's
		PPickPath path = aEvent.getPath();
		PCamera top = path.getTopCamera();
		PStack pathStack = path.getNodeStackReference();
		if (scrollViews != null && pathStack.size() > 2 && (pathStack.get(2) instanceof PCamera)) {
			top = (PCamera) pathStack.get(2);
		}
		Point2D canvasPosition = aEvent.getCanvasPosition();
		Point2D localPosition = path.canvasToLocal(canvasPosition, top);
		if (scrollViews != null) {
			if (pathStack.contains(scrollViews.contentCam)) {
			} else if (pathStack.contains(scrollViews.cornerCam)) {
				localPosition.setLocation(localPosition.getX() - top.getWidth(), localPosition.getY() - top.getHeight());
			} else if (pathStack.contains(scrollViews.rulerCam)) {
				localPosition.setLocation(localPosition.getX(), localPosition.getY() - top.getHeight());
			} else if (pathStack.contains(scrollViews.labelCam)) {
				localPosition.setLocation(localPosition.getX() - top.getWidth(), localPosition.getY());
			}
		}
		return localPosition;
	}

	/**
	 * Gets the number of instants that the given number of pixels is equivalent
	 * to.
	 * 
	 * @param pixels
	 *            the number of pixels
	 * @return the number of instants the number of pixels represents, rounded
	 *         down
	 */
	public long pixel2instantDistance(double pixels) {
		return lineLayer.local2instant(pixels).longValue();
	}

	/**
	 * Gets the number of instants that the given number of pixels is equivalent
	 * to.
	 * 
	 * @param i
	 *            the count of instants
	 * @return the number of pixels the number of instants represents
	 */
	public double instant2pixelDistance(Instant i) {
		return lineLayer.instant2local(i);
	}

	/**
	 * Gets the number of instants that the given number of pixels is equivalent
	 * to.
	 * 
	 * @param i
	 *            the count of instants
	 * @return the number of pixels the number of instants represents
	 */
	public double instant2pixelLocation(Instant i) {
		return lineLayer.instant2local(i.go(-model.getFocus().getStartInstant().longValue()));
	}

	/**
	 * Gets the number of instants that the given number of pixels is equivalent
	 * to.
	 * 
	 * @param pixels
	 *            the number of pixels
	 * @return the number of instants the number of pixels represents, rounded
	 *         down
	 */
	public TimeLine pixel2timeLine(double pixel) {
		return lineLayer.local2timeline(pixel);
	}

	/**
	 * Gets the number of instants that the given number of pixels is equivalent
	 * to.
	 * 
	 * @param i
	 *            the count of instants
	 * @return the number of pixels the number of instants represents
	 */
	public double[] timeLine2pixels(TimeLine tqe) {
		return lineLayer.timeline2local(tqe);
	}

	private class SimpleCanvas extends PCanvas {
		SimpleCanvas(PCamera cam) {
			super();
			super.setPanEventHandler(null);
			super.setZoomEventHandler(null);
			super.getLayer().addChild(cam);
			super.setToolTipText("text");
			this.addMouseListener(new PopupActionListener(this));
		}

		/**
		 * {@inheritDoc}
		 */
		public Point getToolTipLocation(MouseEvent event) {
			//			getToolTipText(event);
			return super.getToolTipLocation(event);
		}

		/**
		 * {@inheritDoc}
		 */
		public String getToolTipText(MouseEvent event) {
			PCamera cam = this.getCamera();
			Point2D point = cam.globalToLocal(event.getPoint());
			PPickPath path = cam.pick(point.getX(), point.getY(), 3);
			PNode n = path.getPickedNode();
			return (String) n.getClientProperty(PToolTip.TOOLTIP_PROPERTY);
		}
	}

	/**
	 * Gets the canvases associated with this canvas.
	 * 
	 * @return a set of four views for use in a scroll pane
	 */
	public ScrollViews getScrollViews() {
		if (scrollViews == null) {
			scrollViews = new ScrollViews();
		}
		return scrollViews;
	}

	/**
	 * {@inheritDoc}
	 */
	public void setPanEventHandler(PPanEventHandler handler) {
		if (this.getPanEventHandler() != handler) {
			PPanEventHandler oldHandler = this.getPanEventHandler();
			super.setPanEventHandler(handler);
			if (scrollViews != null) {
				scrollViews.setPanEventHandler(handler);
			}
			firePropertyChange("panEventHandler", oldHandler, handler);
		}
	}

	/**
	 * {@inheritDoc}
	 */
	public void setZoomEventHandler(PZoomEventHandler handler) {
		if (handler != getZoomEventHandler()) {
			PZoomEventHandler oldHandler = getZoomEventHandler();
			super.setZoomEventHandler(handler);
			if (scrollViews != null) {
				scrollViews.setZoomEventHandler(handler);
			}
			firePropertyChange("zoomEventHandler", oldHandler, handler);
		}
	}

	/**
	 * Gets the popup menu for the markers. This is necessary if you, instead of
	 * using the canvas, insert the view into your own scroll pane, or similar
	 * container widget. In this case, make sure to set the pop-up's viewer
	 * appropriately.
	 * 
	 * @return Returns the markerPop.
	 */
	public ChronicleMarkerPopUp getMarkerPop() {
		return markerPop;
	}

	/**
	 * Gets the model to be used for selection and viewing.
	 * 
	 * @return Returns the selectionModel.
	 */
	public ChronicleSelectionModel getSelectionModel() {
		return selectionModel;
	}

	/**
	 * Changes the chronicle selection model
	 * 
	 * @param selectionModel
	 *            The selectionModel to set.
	 */
	public void setSelectionModel(ChronicleSelectionModel selectionModel) {
		if (this.selectionModel != selectionModel) {
			if (this.selectionModel != null) {
				this.selectionModel
						.removeChangeListener(timeSelectionChangeListener);
			}
			ChronicleSelectionModel oldSelectionModel = this.selectionModel;
			this.selectionModel = selectionModel;
			if (this.selectionModel != null) {
				this.selectionModel
						.addChangeListener(timeSelectionChangeListener);
			}
			firePropertyChange("selectionModel", oldSelectionModel,
					selectionModel);
		}
	}

	private TimeSelectionOverlay timeSelectionOverlay;

	/**
	 * Creates a new ChronicleViewer, using the mediator as the data model.
	 */
	public ChronicleViewer() {
		super();
	
		this.setSelectionModel(new DefaultChronicleSelectionModel());
	
		this.factory = new DefaultRendererCatalogue();
	
		this.markersNode = new AllChronicleMarkersNode();
		this.markersNode.setViewer(this);
		this.lineLayer = new LineLayoutManager();
		this.lineLayer.setOffset(getViewOffsetX(), 0);
		this.overlay = new OverlayLayoutManager();
		this.labelLayer = new LineLayoutManager();
		this.markerPop = new ChronicleMarkerPopUp();
		this.markerPop.setViewer(this);
		this.markerPop.setCanvas(this);
		this.underlay = new ChronicleRuler(this);
		this.headerSlug = new PNode();
	
		this.timeSelectionOverlay = new TimeSelectionOverlay();
	
		boolean horizontal = this.getOrientation() == SwingConstants.HORIZONTAL;
		cornerNode = new PNode();
		cornerNode.setBounds(0, 0, horizontal ? labelLength : getRulerHeight(),
				horizontal ? getRulerHeight() : labelLength);
	
		this.removeInputEventListener(this.getPanEventHandler());
		this.removeInputEventListener(this.getZoomEventHandler());
		//		this.setDefaultRenderQuality(PPaintContext.LOW_QUALITY_RENDERING);
	
		this.addInputEventListener(new ChronicleRightClickZoomHandler(this));
		this.addInputEventListener(new TimeLineInputProxy());
		this.addMouseListener(new PopupActionListener(this));
		this.setViewLength(getMinimumViewLength());
	
		ChronicleMarkerModel markerModel = new DefaultChronicleMarkerModel();
		ChronicleMarker majorMomentMarker = markerModel.createMarker();
		markerModel.addChronicleMarkerListener(cml);
		majorMomentMarker.setLabel(CURR_FRAME_LABEL);
		this.markersNode.setModel(markerModel);
	
		this.getLayer().addChild(underlay);
		this.getLayer().addChild(lineLayer);
		this.getLayer().addChild(labelLayer);
		this.getLayer().addChild(timeSelectionOverlay);
		this.getLayer().addChild(overlay);
		resetView();
	}

	private ChangeListener timeSelectionChangeListener = new ChangeListener() {
		public void stateChanged(ChangeEvent e) {
			timeSelectionOverlay.resetRanges();
			resetView();
		}
	};

	private class TimeSelectionOverlay extends PNode {
		double[] ranges;

		/**
		 * {@inheritDoc}
		 */
		public boolean intersects(Rectangle2D localBounds) {
			double x1 = localBounds.getMinX();
			double x2 = localBounds.getMaxX();
			int start = Arrays.binarySearch(ranges, x1);
			if (start < 0) {
				start = -(start + 1);
				if ((start & 1) == 1) {
					// inserts within a selected span
					return true;
				}
			} else {
				return true;
			}
			int end = Arrays.binarySearch(ranges, x2);
			if (end < 0) {
				end = -(end + 1);
				if ((end & 1) == 1) {
					// inserts within a selected span
					return true;
				}
			}
			return end != start + 1;
		}

		/**
		 * {@inheritDoc}
		 */
		protected void paint(PPaintContext paintContext) {
			Graphics2D g2 = paintContext.getGraphics();
			g2.setPaint(getPaint());
			Rectangle rect = g2.getClipBounds();
			double x1 = 0;
			double x2 = Double.MAX_VALUE;
			double y1 = 0;
			double y2 = Double.MAX_VALUE;
			int startIndex = 0;
			int afterLastIndex = ranges.length;
			if (rect != null) {
				x1 = rect.getMinX();
				x2 = rect.getMaxX();
				y1 = rect.getMinY();
				y2 = rect.getMaxY();
				startIndex = Arrays.binarySearch(ranges, x1);
				if (startIndex < 0) {
					startIndex = -(startIndex + 1);
				}
				startIndex = startIndex & 0xFFFFFFFE;
				afterLastIndex = Arrays.binarySearch(ranges, x2);
				if (afterLastIndex < 0) {
					afterLastIndex = -afterLastIndex;
				}
				afterLastIndex = afterLastIndex | 1;
			}
			Rectangle2D.Double r = new Rectangle2D.Double();
			r.y = y1;
			r.height = y2 - y1;
			for (int i = startIndex; i < afterLastIndex; i += 2) {
				r.x = ranges[i];
				r.width = ranges[i + 1] - r.x;
				g2.fill(r);
			}
		}

		private void resetRanges() {
			PBounds b = super.getBoundsReference();
			TemporalRange time = selectionModel.getSelectedTime();
			if (time == null || time.isEmpty()) {
				ranges = new double[0];
				return;
			}
			if (ranges.length != time.getContiguousIntervalCount() * 2) {
				ranges = new double[time.getContiguousIntervalCount() * 2];
			}
			Iterator iter = time.iterator();
			InstantInterval i = model.getFocus();
			long s = i.getStartInstant().longValue();
			long e = i.getEndInstant().longValue();
			long width = e - s;
			double pixelsPerInstant = b.getWidth() / width;
			int counter = 0;
			while (iter.hasNext()) {
				InstantInterval ii = (InstantInterval) iter.next();
				long i1 = ii.getStartInstant().longValue();
				long i2 = ii.getEndInstant().longValue();
				ranges[counter++] = (i1 - s) * pixelsPerInstant;
				ranges[counter++] = (i2 - s) * pixelsPerInstant;
			}
		}

		/**
		 * {@inheritDoc}
		 */
		public boolean setBounds(double x, double y, double width, double height) {
			if (super.setBounds(x, y, width, height)) {
				resetRanges();
				return true;
			}
			return false;
		}
	}

	public void addTimeLineInputEventListener(TimeLineInputEventListener l) {
		listenerList.add(TimeLineInputEventListener.class, l);
	}

	public void removeTimeLineInputEventListener(TimeLineInputEventListener l) {
		listenerList.remove(TimeLineInputEventListener.class, l);
	}

	public TimeLineInputEventListener[] getTimeLineInputEventListeners() {
		return (TimeLineInputEventListener[]) listenerList
				.getListeners(TimeLineInputEventListener.class);
	}

	protected void fireTimeLineInputEvent(PInputEvent pe, int ptype) {
		Object[] listeners = listenerList.getListenerList();
		TimeLineInputEvent e = null;
		for (int i = listeners.length - 2; i >= 0; i -= 2) {
			if (listeners[i] == TimeLineInputEventListener.class) {
				// Lazily create the event:
				if (e == null)
					e = new TimeLineInputEvent(this, pe);
				((TimeLineInputEventListener) listeners[i + 1]).processEvent(e,
						ptype);
			}
		}
	}

	/**
	 * @return
	 */
	public PLayer getOverlay() {
		return this.overlay;
	}

	public void setSize(Dimension arg0) {
		super.setSize(arg0);
		setViewLength(Math.max(getViewLength(), getMinimumViewLength()));
	}

	public void setSize(int arg0, int arg1) {
		super.setSize(arg0, arg1);
		setViewLength(Math.max(getViewLength(), getMinimumViewLength()));
	}
}