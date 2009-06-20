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

import java.awt.*;
import java.awt.geom.*;
import java.util.Iterator;
import java.util.Vector;

import viper.api.time.*;
import edu.umd.cfar.lamp.chronicle.*;
import edu.umd.cs.piccolo.*;
import edu.umd.cs.piccolo.nodes.*;
import edu.umd.cs.piccolo.util.*;

public class TQESegment extends PNode {
	private PPath startNode;
	private PPath endNode;
	private PNode[] interpNodes;
	private Line2D line;
	private InstantInterval interval;
	private InstantRange interpRange;
	
	private static final double INTERP_RECT_SIZE = 10;
	
	public TQESegment(
		double radius,
		Paint color,
		InstantInterval i) {
		startNode =
			new PPath(new Ellipse2D.Double(0, 0, radius * 2, radius * 2));
		startNode.addClientProperty(PToolTip.TOOLTIP_PROPERTY, i.getStart().toString());
		startNode.setPaint(color);
		this.addChild(startNode);
		endNode =
			new PPath(new Ellipse2D.Double(0, 0, radius * 2, radius * 2));
		endNode.addClientProperty(PToolTip.TOOLTIP_PROPERTY, i.getEndInstant().previous().toString());
		endNode.setPaint(color);
		this.addChild(endNode);
		this.setPaint(color);
		this.interval = i;
		interpNodes = null;		
	}

	public TQESegment(
			double radius,
			Paint color,
			InstantInterval i,
			InstantRange interpRange) {
		this(radius, color, i);
		if(interpRange != null && !interpRange.isEmpty()){
			Vector vector = new Vector();
			
			InstantRange temp = new InstantRange();
			temp.add(i.getStart(), i.getEnd());
			interpRange = InstantRange.parseFrameRange(interpRange.intersect(temp).toString());
			for(Iterator iter = interpRange.iterator(); iter.hasNext();){
				InstantInterval currInterval = (InstantInterval)iter.next();
//				Instant start = currInterval.getStartInstant();
//				Instant end = currInterval.getEndInstant();
//				PNode ppath = new PPath(new Rectangle2D.Double(0, 0, end.intValue() - start.intValue(), INTERP_RECT_SIZE)); 
				TQESegment segment = new TQESegment(radius/2, Color.blue, currInterval);
				this.addChild(segment);
				vector.add(segment);
			}
			Object[] arr = vector.toArray();
			interpNodes = new PNode[arr.length];
			for(int j = 0; j < arr.length; j++)
				interpNodes[j] = (PNode)arr[j];
			this.interpRange = interpRange;
		}
	}
	
	public boolean setBounds(
		double x,
		double y,
		double width,
		double height) {
		if (super.setBounds(x, y, width, height)) {
			double yC = super.getBoundsReference().getCenterY();
			double x1 = super.getBoundsReference().getMinX();
			double x2 = super.getBoundsReference().getMaxX();
			double delta = width / interval.width();
			x2 -= delta;
			x2 = Math.max(x1, x2);

			PBounds oldStartBounds = startNode.getBoundsReference();
			double w = oldStartBounds.getWidth();
			double h = oldStartBounds.getHeight();
			double yMin = y + (height / 2) - (h / 2);
			startNode.setBounds(x1 - (w / 2), yMin, w, h);
			endNode.setBounds(x2 - (w / 2), yMin, w, h);

			line = new Line2D.Double(x1, yC, x2, yC);
			
			if(interpNodes != null){
				Iterator iter = interpRange.iterator();
				for(int i = 0; i < interpNodes.length; i++){
					InstantInterval currInterval = (InstantInterval)iter.next();
					double startPos = x1 + delta*(currInterval.getStartInstant().intValue() - this.interval.getStartInstant().intValue());
					double endPos = x1 + delta*(currInterval.getEndInstant().intValue() - this.interval.getStartInstant().intValue());
					interpNodes[i].setBounds(startPos, yMin, endPos - startPos, h);
				}
			}
			return true;
		} else {
			return false;
		}
	}
	
	/**
	 * @return
	 */
	public PPath getEndNode() {
		return endNode;
	}

	/**
	 * @return
	 */
	public InstantInterval getI() {
		return interval;
	}

	/**
	 * @return
	 */
	public PPath getStartNode() {
		return startNode;
	}
	
	/**
	 * @see edu.umd.cs.piccolo.PNode#paint(edu.umd.cs.piccolo.util.PPaintContext)
	 */
	protected void paint(PPaintContext paintContext) {
		Graphics2D g2 = paintContext.getGraphics();
		Paint old = g2.getPaint();
		g2.setPaint(getPaint());
		g2.draw(line);
		g2.setPaint(old);
	}

}
