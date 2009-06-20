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

import java.awt.*;
import java.awt.geom.*;
import java.util.*;

import viper.api.time.*;
import edu.umd.cfar.lamp.chronicle.*;
import edu.umd.cs.piccolo.*;

/**
 * Draws all the nodes described in the marker model using the 
 * renderering styles indicated. It handles the layout of the markers.
 */
public class AllChronicleMarkersNode extends PNode {
	private ChronicleMarkerModel model;
	private ChronicleMarkerListener cml;
	private ChronicleViewer viewer;
	private Map model2node;
	private double headerHeight;
	private double footerHeight;
	private double width = 16;
	private MarkerStyles styles;

	/**
	 * Constructs a new, empty marker holder node.
	 */
	public AllChronicleMarkersNode() {
		model2node = new HashMap();
		cml = new ChronicleMarkerListener() {
			public void markersChanged(ChronicleMarkerEvent e) {
				switch(e.getType()) {
					case ChronicleMarkerEvent.MOVED:
						resetBoundsFor(e.getChangedMarker());
						break;
					case ChronicleMarkerEvent.DELETED:
						removeMarkerRepresentation(e.getChangedMarker());
						break;
					case ChronicleMarkerEvent.ADDED:
						addMarkerRepresentation(e.getChangedMarker());
						break;
					case ChronicleMarkerEvent.CHANGED:
						removeMarkerRepresentation(e.getChangedMarker());
						addMarkerRepresentation(e.getChangedMarker());
						break;
					default:// MULTIPLE
						refreshFromModel();
				};
			}};
		MarkerStyleMap styles = new MarkerStyleMap();
		styles.setDefaultStyle(new DefaultStyleForLabel(Color.red, Color.white));
		this.styles = styles;
	}
	
	private void refreshFromModel() {
		this.removeAllChildren();
		if (this.model != null) {
			for (int i = 0; i < this.getModel().getSize(); i++) {
				ChronicleMarker m = this.getModel().getMarker(i);
				addMarkerRepresentation(m);
			}
		}
		this.invalidateFullBounds();
	}
	private void addMarkerRepresentation(ChronicleMarker m) {
		MarkerStyles.StyleForLabel s = styles.getStyleForLabel(m.getLabel());
		ChronicleMarkerNode n = new ChronicleMarkerNode();
		model2node.put(m, n);
		n.setViewer(viewer);
		n.setModel(m);
		n.setLineStyle(s.getLineStyle());
		n.setFillStyle(s.getFillStyle());
		n.setHeader(s.createHeaderNode());
		this.addChild(n);
		resetBoundsFor(n);
	}
	private void resetBoundsFor(ChronicleMarker m) {
		resetBoundsFor((ChronicleMarkerNode) model2node.get(m));
	}
	private void resetBoundsFor(ChronicleMarkerNode n) {
		ChronicleMarker m = n.getModel();
		if (m == null || m.getWhen() == null) {
			return;
		}
		InstantInterval ii = model.getInterval();
		long start = ii.getStartInstant().longValue();
		long end = ii.getEndInstant().longValue();
		long frameCount = end - start;
		long mFrame = m.getWhen().longValue() - start;

		double viewLength = this.getBoundsReference().getWidth();
		double x = mFrame;
		if (viewLength > 0) {
			x = (mFrame * viewLength) / frameCount;
		}
		x += this.getBoundsReference().getX();

		double viewHeight = this.getBoundsReference().getHeight() - headerHeight - footerHeight;
		// now, instead of header, extends view height
		// viewHeight -= getHeaderHeight();
		
		n.setBounds(x-(width/2), headerHeight, width, viewHeight);
		/// XXX fails when time is mixed with frames
		/// XXX doesn't work for vertical orientation
	}

	private void removeMarkerRepresentation(ChronicleMarker m) {
		PNode n = (PNode) model2node.get(m);
		this.removeChild(n);
	}

	/**
	 * @return ChronicleMarkerModel
	 */
	public ChronicleMarkerModel getModel() {
		return model;
	}

	/**
	 * Sets the marker data model.
	 * @param model the new marker model
	 */
	public void setModel(ChronicleMarkerModel model) {
		if (this.model != null) {
			this.model.removeChronicleMarkerListener(cml);
		}
		this.model = model;
		if (this.model != null) {
			this.model.addChronicleMarkerListener(cml);
		}
		refreshFromModel();
	}

	/**
	 * Clears the node.
	 */
	public void removeAllChildren() {
		super.removeAllChildren();
		model2node.clear();
	}

	/**
	 * Removes the marker from the model2node map if it
	 * is in there.
	 * @param pnode
	 */
	private void removeMapIfAppropriate(PNode pnode) {
		if (pnode instanceof ChronicleMarkerNode) {
			ChronicleMarkerNode n = (ChronicleMarkerNode) pnode;
			model2node.remove(n.getModel());
		}
	}
	
	/**
	 * @inheritDoc
	 */
	public PNode removeChild(int index) {
		removeMapIfAppropriate(this.getChild(index));
		return super.removeChild(index);
	}
	
	/**
	 * @inheritDoc
	 */
	public PNode removeChild(PNode child) {
		removeMapIfAppropriate(child);
		return super.removeChild(child);
	}
	
	/**
	 * @inheritDoc
	 */
	public void removeChildren(Collection childrenNodes) {
		for (Iterator iter = childrenNodes.iterator(); iter.hasNext(); ) {
			removeMapIfAppropriate((PNode) iter.next());
		}
		super.removeChildren(childrenNodes);
	}
	
	/**
	 * @inheritDoc
	 */
	public boolean setBounds(double x, double y, double width, double height) {
		boolean r = super.setBounds(x, y, width, height);
		if (r) {
			for (Iterator iter = getChildrenIterator(); iter.hasNext(); ) {
				Object o = iter.next();
				if (o instanceof ChronicleMarkerNode) {
					resetBoundsFor((ChronicleMarkerNode) o);
				}
			}
		}
		return r;
	}

	/**
	 * Gets the height of marker footers.
	 * @return the footer height, in pixels
	 */
	public double getFooterHeight() {
		return footerHeight;
	}

	/**
	 * Gets the height of marker headers
	 * @return the header height, in pixels
	 */
	public double getHeaderHeight() {
		return headerHeight;
	}

	/**
	 * Sets the height to use for footers.
	 * @param d the new footer height
	 */
	public void setFooterHeight(double d) {
		footerHeight = d;
	}

	/**
	 * Sets the height of marker headers.
	 * @param headerHeight the new header height
	 */
	public void setHeaderHeight(double headerHeight) {
		this.headerHeight = headerHeight;
	}


	/**
	 * Gets the styles to use for different node types.
	 * @return the style mapping
	 */
	public MarkerStyles getStyles() {
		return styles;
	}

	/**
	 * Sets the styles to use for different node types.
	 * @param styles the style mapping
	 */
	public void setStyles(MarkerStyles styles) {
		this.styles = styles;
	}

	/**
	 * Gets the associated viewer.
	 * @return a viewer associated with this marker holder
	 */
	public ChronicleViewer getViewer() {
		return viewer;
	}

	/**
	 * Sets the associated viewer.
	 * @param viewer a viewer associated with this marker holder
	 */
	public void setViewer(ChronicleViewer viewer) {
		this.viewer = viewer;
	}

	/**
	 * @inheritDoc
	 */
	public boolean intersects(Rectangle2D localBounds) {
		return false;
	}
}
