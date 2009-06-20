package edu.umd.cfar.lamp.chronicle.extras.emblems;

import java.awt.*;
import java.awt.geom.*;

import javax.swing.*;

import edu.umd.cfar.lamp.chronicle.*;
import edu.umd.cs.piccolo.*;
import edu.umd.cs.piccolo.event.*;
import edu.umd.cs.piccolo.nodes.*;
import edu.umd.cs.piccolo.util.*;


/**
 * The label that is displayed in front of each timeline.
 * This includes the name and any associated emblems.
 */
public class EmblemedTimeLineLabel extends PNode {
	private PNode label;
	private TimeLine tqe;
	private PCamera c;
	private EmblemModel em;
	private int inset = 2;
	private int emblemSize = 12;
	private int orientation = SwingConstants.HORIZONTAL;

	public EmblemedTimeLineLabel(TimeLineRenderer f, TimeLine tqe, EmblemModel m, ChronicleViewer cv, boolean isSelected, boolean hasFocus, double infoLength, int orientation) {
		this.tqe = tqe;
		this.em = m;
		this.orientation = orientation;
		if (tqe != null) {
			label = f.generateLabel(cv, tqe, isSelected, hasFocus, infoLength, orientation);
		} else {
			label = new PText("");
		}
		
		PLayer l = new PLayer();
		l.addChild(this.label);

		this.c = new PCamera();
		this.c.setBounds(this.label.getBounds());
		c.addLayer(l);
		//c.setViewConstraint(PCamera.VIEW_CONSTRAINT_CENTER);
		this.addChild(c);
		resetEmblems();
	}
	
	/**
	 * Refresh the emblems.
	 */
	public void resetEmblems() {
		while (this.getChildrenCount() > 1) {
			this.removeChild(1); // XXX do I have to do any cleanup?
		}
		PBounds bounds = super.getBoundsReference();
		int x = (int) bounds.getX()+inset;
		int y = (int) bounds.getY()+inset;
		int w = (int) bounds.getWidth()-2*inset;
		int h = (int) bounds.getHeight()-2*inset;
		if (orientation == SwingConstants.HORIZONTAL) {
			x = x+w - emblemSize*em.getMaxEmblemCount();
			y = y + (h-emblemSize)/2;
		} else {
			x = x + (w-emblemSize)/2;
			y = y + h - emblemSize*em.getMaxEmblemCount();
		}
		for (int i = 0; i < em.getMaxEmblemCount(); i++) {
			Image im = em.getEmblemFor(tqe, i);
			PNode c;
			if (im != null) {
				c = new PImage(im);
				c.addInputEventListener(new ClickListener(i));
			} else {
				c = new PNode();
			}
			c.setBounds(x, y, emblemSize, emblemSize);
			c.addClientProperty(PToolTip.TOOLTIP_PROPERTY,em.getTextEmblemFor(tqe, i));
			this.addChild(c);
			if (orientation == SwingConstants.HORIZONTAL) {
				x += emblemSize;
			} else {
				y += emblemSize;
			}
		}
	}
	private class ClickListener extends PBasicInputEventHandler {
		private int emblemIndex;
		
		ClickListener(int emblemIndex) {
			super();
			this.emblemIndex = emblemIndex;
		}

		/**
		 * Invokes the appropriate emblem's click event.
		 * @param event the event to check to see if is 
		 * over an emblem
		 */
		public void mouseClicked(PInputEvent event) {
			super.mouseClicked(event);
			em.click(tqe, emblemIndex);
		}
	}
	
	/**
	 * {@inheritDoc}
	 */
	public boolean intersects(Rectangle2D localBounds) {
		return super.intersects(localBounds);
	}
	
	/**
	 * {@inheritDoc}
	 */
	protected void paint(PPaintContext paintContext) {
		super.paint(paintContext);
		Graphics2D g2 = paintContext.getGraphics();
		Paint p = getPaint();
		g2.setPaint(p);
		PBounds bounds = super.getBoundsReference();
		int x = (int) bounds.getX();
		int y = (int) bounds.getY();
		int w = (int) bounds.getWidth()-1;
		int h = (int) bounds.getHeight()-1;
		g2.setPaint(Color.lightGray);
		g2.fillRect(x, y, w, h);
		g2.setPaint(Color.black);
		g2.drawRect(x, y, w, h);
		g2.setPaint(p);
	}

	/**
	 * {@inheritDoc}
	 */
	public boolean setBounds(double x, double y, double width, double height) {
		boolean changed = super.setBounds(x, y, width, height);
		if (changed) {
			// update kids. Right now, this is only the label, 
			// but it could include others later.
			
			x = Math.max(Math.min(x+inset, x+width-inset), 0);
			y =  Math.max(Math.min(y+inset, y+height-inset), 0);
			width = Math.max(width - 2*inset - em.getMaxEmblemCount()*this.emblemSize, 0);
			height = Math.max(height - 2*inset, 0);
			label.setBounds(x, y, width, height);
			c.setBounds(x, y, width, height);
			resetEmblems();
		}
		return changed;
	}
	
	private Dimension prefSize = null;
	
	/**
	 * Gets the preferred size of the label, which
	 * should at least be the smallest size to include 
	 * the text label and the emblems.
	 * @return the preferred size
	 */
	public Dimension getPreferredSize() {
		if (prefSize == null) {
			int w = inset * 2 + em.getMaxEmblemCount()*this.emblemSize + (int) Math.ceil(label.getBoundsReference().width);
			int h = inset * 2 + Math.max((int) Math.ceil(label.getBoundsReference().getHeight()), this.emblemSize);
			prefSize = new Dimension(w, h);
		}
		return prefSize;
	}
	public int getEmblemSize() {
		return emblemSize;
	}
	public void setEmblemSize(int emblemSize) {
		if (this.emblemSize != emblemSize) {
			this.emblemSize = emblemSize;
			prefSize = null;
			invalidatePaint();
		}
	}
	public int getInset() {
		return inset;
	}
	public void setInset(int inset) {
		if (this.inset != inset) {
			this.inset = inset;
			prefSize = null;
			invalidatePaint();
		}
	}
}
