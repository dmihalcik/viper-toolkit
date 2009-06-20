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

import javax.swing.*;

import edu.umd.cs.piccolo.*;
import edu.umd.cs.piccolox.swing.*;

public class ScrollableImageTable extends PScrollPane {
	private QuickLayoutImageTilesNode layoutManager;
	private double tileSize = 64;
	
	
	public ScrollableImageTable() {
		super(new PCanvas(),
					ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS,
					ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);
		PCanvas c = (PCanvas) getViewport().getView();
		layoutManager = new QuickLayoutImageTilesNode(tileSize, c);
		PLayer l = c.getLayer();
		l.addChild(layoutManager);
	}
	
	public void clearTable() {
		PCanvas c = (PCanvas) getViewport().getView();
		c.setZoomEventHandler(null);
		c.setPanEventHandler(null);
		layoutManager.removeAllChildren();
	}

	public void addImage(PVideoAnnotationItem item) {
		layoutManager.addChild(item);
	}

	public QuickLayoutImageTilesNode getLayer() {
		return layoutManager;
	}

	public PCamera getCamera() {
		PCanvas c = (PCanvas) getViewport().getView();
		return c.getCamera();
	}

	public double getTileSize() {
		return tileSize;
	}

	public void setTileSize(double tileSize) {
		this.tileSize = tileSize;
		layoutManager.setImageHeight(tileSize);
		layoutManager.setImageWidth(tileSize);
	}

	
	public Dimension getMinimumSize() {
		if (super.isMinimumSizeSet()) {
			return super.getMinimumSize();
		}
		return tiles2dimension(2, 2);
	}

	public Dimension getPreferredSize() {
		if (super.isPreferredSizeSet()) {
			return super.getPreferredSize();
		}
		int preferredTilesAcross = 3;
		int preferredTilesHigh = 3;
		return tiles2dimension(preferredTilesAcross, preferredTilesHigh);
	}

	/**
	 * Gets a dimension (in pixels) that accomidates an image grid of the given
	 * size (in tiles).
	 * @param preferredTilesAcross
	 * @param preferredTilesHigh
	 * @return
	 */
	private Dimension tiles2dimension(int preferredTilesAcross, int preferredTilesHigh) {
		int w = (int) ((layoutManager.getMargin() + layoutManager.getBorder() + layoutManager.getImageWidth()) *preferredTilesAcross); 
		int h = (int) ((layoutManager.getMargin() + layoutManager.getBorder() + layoutManager.getImageHeight() + layoutManager.getLabelHeight()) *preferredTilesHigh); 
		return new Dimension(w + super.getVerticalScrollBar().getWidth(),h + super.getHorizontalScrollBar().getHeight());
	}
	
	
}
