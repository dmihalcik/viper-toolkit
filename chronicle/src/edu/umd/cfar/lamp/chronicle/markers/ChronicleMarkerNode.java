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
import java.awt.event.*;
import java.awt.geom.*;
import java.util.*;

import viper.api.time.*;
import edu.umd.cfar.lamp.chronicle.*;
import edu.umd.cs.piccolo.*;
import edu.umd.cs.piccolo.event.*;
import edu.umd.cs.piccolo.util.*;

/**
 * The ChronicleMarkerNode is made up of three subnodes, the header, the footer and 
 * the line. The line color may be specified, and the header and footer may
 * be set directly. The way that I use this is by using the StyleMap on the 
 * AllChronicleMarkersNode.
 * @author davidm
 */
public class ChronicleMarkerNode extends PNode {
	private ChronicleMarker model;
	private ChronicleViewer viewer;
	private PNode header;
	private PNode footer;
	private Line2D line;
	private Paint lineStyle;
	private Stroke lineStroke = new BasicStroke(1);
	private Paint fillStyle;

	/**
	 * Constructs a new chronicle marker in the default  style.
	 */
	public ChronicleMarkerNode() {
		super();
		addInputEventListener(new ChronicleMarkerDragHandler());
		lineStyle = Color.red;
		fillStyle = Color.white;
	}
	
	/**
	 * Gets the model for the marker - basically, it is just 
	 * an Instant and a String (when and label).
	 * @return ChronicleMarker
	 */
	public ChronicleMarker getModel() {
		return model;
	}

	/**
	 * Sets the model of the marker.
	 * @param model the new marker model
	 */
	public void setModel(ChronicleMarker model) {
		this.model = model;
	}
	private boolean realtimeDrag = true;

	class ChronicleMarkerDragHandler extends PDragSequenceEventHandler {
		/// Cursor handling, stolen from PBoundsHandler
		private Set cursorsPushed = new HashSet();

		/**
		 * Creates a new zoom handler.
		 */
		public ChronicleMarkerDragHandler() {
			super();
			setEventFilter(new PInputEventFilter(InputEvent.BUTTON1_MASK));
		}

		protected void dragActivityFirstStep(PInputEvent aEvent) {
			super.dragActivityFirstStep(aEvent);
		}
		
		protected void dragActivityStep(PInputEvent aEvent) {
			super.dragActivityStep(aEvent);
			if (realtimeDrag) {
				Instant newWhen = viewer.getInstantFor(aEvent);
				model.setWhen(newWhen);
			}
		}

		protected void dragActivityFinalStep(PInputEvent aEvent) {
			super.dragActivityFinalStep(aEvent);
			if (!realtimeDrag) {
				Instant newWhen = viewer.getInstantFor(aEvent);
				model.setWhen(newWhen);
			}
		}
		/**
		 * Indicates that the mouse entered the marker.
		 * Adds a cursor, if necessary.
		 * @param e {@inheritDoc}
		 */
		public void mouseEntered(PInputEvent e) {
			super.mouseEntered(e);
			PComponent component = e.getTopCamera().getComponent();
			if (!cursorsPushed.contains(component)) {
				component.pushCursor(new Cursor(Cursor.W_RESIZE_CURSOR));
				cursorsPushed.add(component);
			}
		}

		/**
		 * Indicates that the mouse exited the marker.
		 * Pops a cursor, if necessary.
		 * @param e {@inheritDoc}
		 */
		public void mouseExited(PInputEvent e) {
			super.mouseExited(e);
			PComponent component = e.getTopCamera().getComponent();
			if (cursorsPushed.contains(component)) {
				component.popCursor();
				cursorsPushed.remove(component);
			}
		}
	}

	/**
	 * Gets the header node for the marker. This
	 * is usually something colorful or informative that 
	 * the user can grab onto.
	 * @return the header node
	 */
	public PNode getHeader() {
		return header;
	}

	/**
	 * Gets the color of the line.
	 * @return the line style
	 */
	public Paint getLineStyle() {
		return lineStyle;
	}

	/**
	 * Sets the header node for the marker.
	 * @param header new marker header node
	 */
	public void setHeader(PNode header) {
		if (this.header != null) {
			this.removeChild(this.header);
		}
		this.header = header;
		if (this.header != null) {
			Instant when = getModel().getWhen();
			if (when != null) {
				this.header.addClientProperty(PToolTip.TOOLTIP_PROPERTY, when.toString());
			}
			this.addChild(this.header);
		}
	}

	/**
	 * Sets the line style
	 * @param lineStyle how to draw the marker's line
	 * across the timelines
	 */
	public void setLineStyle(Paint lineStyle) {
		this.lineStyle = lineStyle;
		this.invalidatePaint();
	}
	
	protected void paint(PPaintContext paintContext) {
		if (getModel()!= null && getModel().getWhen() != null) {
			Graphics2D g2 = paintContext.getGraphics();
			Paint old = g2.getPaint();
			Stroke oldS = g2.getStroke();
			g2.setPaint(lineStyle);
			g2.setStroke(lineStroke);
			g2.draw(line);
			g2.setPaint(old);
			g2.setStroke(oldS);
		}
	}
	
	protected void layoutChildren() {
		if (getModel()!= null && getModel().getWhen() != null) {
			super.layoutChildren();
		}
	}

	/**
	 * Gets the footer node for the marker.
	 * @return the marker footer node. This may 
	 * be simply a mirror of the header node, something
	 * simple that acts as a foot, or used instead of a 
	 * header node. 
	 */
	public PNode getFooter() {
		return footer;
	}

	/**
	 * Sets the footer node.
	 * @param node the new node to place a the bottom 
	 * (or to the right, for vertical timelines)
	 * of the marker
	 */
	public void setFooter(PNode node) {
		footer = node;
	}

	/**
	 * Gets the holder that contains this marker node.
	 * @return the parent node
	 */
	public AllChronicleMarkersNode getMarkerHolder() {
		assert this.getParent() != null;
		return (AllChronicleMarkersNode) this.getParent();
	}


	/**
	 * @inheritDoc
	 */
	public boolean setBounds(double x, double y, double width, double height) {
		boolean changed = super.setBounds(x, y, width, height);
		if (changed) {
			Instant when = getModel().getWhen();
			String ttip = when.toString();
			if (getHeader() != null) {
				double h = getMarkerHolder().getHeaderHeight();
				getHeader().setBounds(x, y-h, width, h);
				if (when != null) {
					this.header.addClientProperty(PToolTip.TOOLTIP_PROPERTY, ttip);
				}
			}
			if (getFooter() != null) {
				double h = getMarkerHolder().getFooterHeight();
				double ly = y+height;
				getFooter().setBounds(x, ly, width, h);
				if (when != null) {
					this.footer.addClientProperty(PToolTip.TOOLTIP_PROPERTY, ttip);
				}
			}
			if (when != null) {
				this.addClientProperty(PToolTip.TOOLTIP_PROPERTY, ttip);
			}
			double x1 = getBoundsReference().getMinX();
			double y1 = getBoundsReference().getMinY();
			double x2 = getBoundsReference().getMaxX();
			double y2 = getBoundsReference().getMaxY();
			if (true) { // ORIENTATION == TOP-DOWN
				x2 = (x2 + x1) / 2;
				x1 = x2;
			}
			line = new Line2D.Double(x1, y1, x2, y2);
		}
		return changed;
	}
	
	/**
	 * Gets the fill style for the node.
	 * @return the fill style
	 */
	public Paint getFillStyle() {
		return fillStyle;
	}

	/**
	 * Sets the fill style for the node.
	 * This may be used by the header or footer,
	 * or it may be used by the line if I ever decide
	 * to make them fat.
	 * @param paint the (suggested) fill style
	 */
	public void setFillStyle(Paint paint) {
		fillStyle = paint;
		this.invalidatePaint();
	}

	/**
	 * {@inheritDoc}
	 */
	public boolean intersects(Rectangle2D localBounds) {
		return line.intersects(localBounds);
	}
	
	/**
	 * Gets a chronicle viewer of this marker node.
	 * @return the primary associated chronicle viewer
	 */
	public ChronicleViewer getViewer() {
		return viewer;
	}

	/**
	 * Sets the primary chronicle viewer for this node.
	 * @param viewer a chronicle viewer
	 */
	public void setViewer(ChronicleViewer viewer) {
		this.viewer = viewer;
	}
}
