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

import java.util.*;

import edu.umd.cs.piccolo.*;

class QuickLayoutImageTilesNode extends PNode {
	/**
	 * 
	 */
	private PCanvas canvas;
	private double imageWidth = 128;
	private double imageHeight = 128;
	private double border = PersonGallery.QLAYOUT_BORDER;
	private double margin = PersonGallery.QLAYOUT_MARGIN;
	private double labelHeight = PersonGallery.QLAYOUT_LABEL_HEIGHT;

	QuickLayoutImageTilesNode(double tileSize, PCanvas canvas) {
		this(tileSize, tileSize, canvas);
	}
	QuickLayoutImageTilesNode(double imageWidth, double imageHeight, PCanvas canvas) {
		super();
		this.imageWidth = imageWidth;
		this.imageHeight = imageHeight;
		this.canvas = canvas;
	}

	protected void layoutChildren() {
		final double singleColumnWidth = margin * 2 + border * 2 + imageWidth;
		final double width = Math.max(canvas.getWidth(), singleColumnWidth);

		final double tileWidth = (imageWidth + 2 * border);
		final double tileSpaceX = tileWidth + margin;
		final double tileHeight = imageHeight + 2 * border + 2 * labelHeight;
		final double tileSpaceY = tileHeight + margin;

		final int tilesAcross = Math.max(1, (int) (width / tileSpaceX));
		final double realMargin = (width - (tilesAcross * tileWidth))
				/ tilesAcross;

		double yOffset = margin;
		double xOffset = realMargin / 2;
		int colIndex = 0;

		Iterator i = getChildrenIterator();
		while (i.hasNext()) {
			PNode each = (PNode) i.next();
			each.setOffset(xOffset, yOffset);

			// advance to where to put the next tile
			xOffset += tileWidth + realMargin;
			colIndex++;
			if (colIndex >= tilesAcross) {
				colIndex = 0;
				yOffset += tileSpaceY;
				xOffset = realMargin / 2;
			}
		}
	}
	public double getBorder() {
		return border;
	}
	public void setBorder(double border) {
		this.border = border;
		invalidateFullBounds();
	}
	public double getImageHeight() {
		return imageHeight;
	}
	public void setImageHeight(double imageHeight) {
		this.imageHeight = imageHeight;
		invalidateFullBounds();
	}
	public double getImageWidth() {
		return imageWidth;
	}
	public void setImageWidth(double imageWidth) {
		this.imageWidth = imageWidth;
		invalidateFullBounds();
	}
	public double getLabelHeight() {
		return labelHeight;
	}
	public void setLabelHeight(double labelHeight) {
		this.labelHeight = labelHeight;
		invalidateFullBounds();
	}
	public double getMargin() {
		return margin;
	}
	public void setMargin(double margin) {
		this.margin = margin;
		invalidateFullBounds();
	}
}