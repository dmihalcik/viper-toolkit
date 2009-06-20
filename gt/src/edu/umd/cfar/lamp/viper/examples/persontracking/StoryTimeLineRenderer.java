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
import java.awt.geom.*;

import edu.umd.cfar.lamp.chronicle.*;
import edu.umd.cfar.lamp.viper.examples.persontracking.PersonGallery.*;
import edu.umd.cs.piccolo.*;
import edu.umd.cs.piccolo.nodes.*;

/**
 * Renders the timeline for person tracks with notes for where evidence 
 * is noted.
 * @author davidm
 */
final class StoryTimeLineRenderer extends AdapterForTimeLineRenderer {
	/**
	 * 
	 */
	private final PersonGallery gallery;

	/**
	 * @param gallery
	 */
	StoryTimeLineRenderer(PersonGallery gallery) {
		this.gallery = gallery;
	}

	private BasicTimeLineRenderer btr = new BasicTimeLineRenderer();

	public PNode getTimeLineRendererNode(ChronicleViewer chronicle,
			TimeLine t, boolean isSelected, boolean hasFocus,
			double timeLength, double infoLength, int orientation) {
		long w = chronicle.getModel().getFocus().width();
		PCamera refCam = chronicle.getScrollViews().content.getCamera();
		if (t instanceof SinglePersonTimeLine) {
			SinglePersonTimeLine spt = (SinglePersonTimeLine) t;
			GalleryEntity selectedEntity = gallery.getModel().getSelectedEntity();
			boolean selected = selectedEntity != null && spt.entity.equals(selectedEntity);
			PNode parent = new PNode();
			
			PPath background = new PPath();
			background.setPaint(selected ? Color.darkGray : Color.black);
			background.setStrokePaint(selected ? Color.red : Color.gray);
			parent.addChild(background);
			
			PNode other = btr.getTimeLineRendererNode(chronicle, t, isSelected, hasFocus, timeLength, 16, orientation);
			parent.addChild(other);
			
			if (selected && gallery.isDisplayThumbsInTimeline()) {
				PNode splayed = this.gallery.splayout(timeLength, w, refCam, spt.entity);
				splayed.setOffset(0, other.getHeight());
				parent.addChild(splayed);
			}
			
			parent.setBounds(parent.getFullBounds());
			background.setPathTo(new Rectangle2D.Double(parent.getX(), parent.getY(), parent.getWidth()-1, parent.getHeight()-1));
			return parent;
		}
		return this.gallery.splayout(timeLength, w, refCam, null);
	}
	
	public PNode generateLabel(final ChronicleViewer v, final TimeLine tqe,
			boolean isSelected, boolean hasFocus, double infoLength,
			int orientation) {
		PTextLabel label = new PTextLabel();
		label.setHOffset(-5);
		label.setWOffset(-2);
		label.setText(tqe.getTitle());
		return label;
	}
}