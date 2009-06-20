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

import edu.umd.cfar.lamp.chronicle.*;
import edu.umd.cfar.lamp.viper.examples.persontracking.images.*;
import edu.umd.cs.piccolo.*;
import edu.umd.cs.piccolo.nodes.*;
import edu.umd.cs.piccolo.util.*;

/**
 * The item annotation includes space for things like title, keywords,
 * a background color, possibly drop shadows, and a good place to put 
 * clickable icons and so forth. It holds on to an image node of some kind, 
 * as well.
 * <p />
 * <pre>
 * +---------------------+
 * | margin space        |
 * | +-----------------+ |
 * | |                 | |
 * (image title here)  | |
 * | |                 | |
 * | |                 | |
 * | |-sparkline--|-^v-| |
 * | +-----------------+ |
 * | (keyword keyword >) |
 * |                MORE |
 * +---------------------+
 * </pre>
 * @author davidm
 */
public class PVideoAnnotationItem extends PNode {
	private PImageArchiveNode image;
	private PTextLabel name;
	private PTextLabel keywords;
	private PImage deleteIcon;
	private PNode sparkline;
	
	private Paint backgroundPaint;
	private Paint boundaryPaint;
	private Stroke boundaryStroke = new BasicStroke(1);
	private double margin;
	private double labelSize;
	private double imageHeight;
	private double imageWidth;
	
	protected void paint(PPaintContext paintContext) {
		Graphics2D g2 = paintContext.getGraphics();

		if (backgroundPaint != null || boundaryPaint != null) {
			PBounds b = getBoundsReference();
			int x = (int) (b.x);
			int y = (int) (b.y);
			int w = (int) (b.width);
			int h = (int) (b.height);
			if (backgroundPaint != null) {
				g2.setPaint(backgroundPaint);
				g2.fillRect(x, y, w, h);
			}
			if (boundaryPaint != null) {
				g2.setPaint(boundaryPaint);
				g2.setStroke(boundaryStroke);
				g2.drawRect(x, y, w, h);
			}
		}
	}

	public boolean setBounds(double x, double y, double width, double height) {
		if (!super.setBounds(x, y, width, height)) {
			return false;
		}
		assert height >= (margin + imageHeight + margin);
		
		reboundAll();
		return true;
	}
	
	private void redoChildOrder() {
		removeAllChildren();
		if (image != null) {
			addChild(image);
		}
		if (keywords != null) {
			addChild(keywords);
		}
		if (name != null) {
			addChild(name);
		}
		if (deleteIcon != null) {
			addChild(deleteIcon);
		}
	}

	
	private void reboundAll() {
		reboundImage();
		reboundName();
		reboundKeywords();
		reboundDeleteIcon();
		setBounds(getX(), getY(), margin + imageWidth + margin, margin + imageHeight + labelSize + labelSize + margin);
	}

	/**
	 * Check and redo the bounds of the node for the thumbnail itself.
	 */
	private void reboundImage() {
		if (null == image) {
			return;
		}
		double dX = margin + (imageWidth - image.getWidth())/2.0;
		double dY = margin + (imageHeight - image.getHeight())/2.0;
		
		assert image.getWidth() <= imageWidth;
		assert image.getHeight() <= imageHeight;
		if (dX != 0 || dY != 0) {
			image.setOffset(dX, dY);
		}
	}
	
	/**
	 * Check and redo the bounds of the Name object.
	 */
	private void reboundName() {
		if (null == name) {
			return;
		}
		//assert name.getWidth() <= this.getBoundsReference().width;
		name.setOffset(0, margin + labelSize/2);
	}

	/**
	 * Check and redo the bounds of the delete icon.
	 */
	private void reboundDeleteIcon() {
		if (null == deleteIcon) {
			return;
		}
		final PBounds diBoundsRef = deleteIcon.getBoundsReference();
		final double cX = getBoundsReference().getMaxX() - margin - diBoundsRef.width;
		final double cY = getBoundsReference().getMaxY() - margin - diBoundsRef.height;
		final double dX = cX - diBoundsRef.x;
		final double dY = cY - diBoundsRef.y;
		
		assert diBoundsRef.height <= labelSize;
		assert diBoundsRef.width <= this.getBoundsReference().width - 2*margin;
		assert diBoundsRef.width <= 4*labelSize;
		if (dX != 0 || dY != 0) {
			deleteIcon.setBounds(diBoundsRef.x + dX, diBoundsRef.y + dY, diBoundsRef.width, diBoundsRef.height);
		}
	}

	/**
	 * Check and redo the bounds of the keywords node.
	 */
	private void reboundKeywords() {
		if (null == keywords) {
			return;
		}
		final PBounds kwBoundsRef = keywords.getBoundsReference();
		final double cX = getBoundsReference().getMaxX() - margin - labelSize;
		final double cY = getBoundsReference().getMinY() + margin;
		final double dX = cX - kwBoundsRef.x;
		final double dY = cY - kwBoundsRef.y;
		
		assert kwBoundsRef.height <= labelSize;
		assert kwBoundsRef.width <= this.getBoundsReference().width - 2*margin;
		if (dX != 0 || dY != 0) {
			keywords.setBounds(kwBoundsRef.x + dX, kwBoundsRef.y + dY, kwBoundsRef.width, kwBoundsRef.height);
		}
	}

	/**
	 * Check and redo the bounds of the sparkline node.
	 */
	private void reboundSparkline() {
		if (null == sparkline) {
			return;
		}
		double dX = margin;
		double dY = margin + imageHeight - sparkline.getHeight();
		
		if (dX != 0 || dY != 0) {
			sparkline.setOffset(dX, dY);
		}
	}

	public Paint getBackgroundPaint() {
		return backgroundPaint;
	}

	public void setBackgroundPaint(Paint backgroundPaint) {
		this.backgroundPaint = backgroundPaint;
		invalidatePaint();
	}

	public Paint getBoundaryPaint() {
		return boundaryPaint;
	}

	public void setBoundaryPaint(Paint boundaryStroke) {
		this.boundaryPaint = boundaryStroke;
		invalidatePaint();
	}

	public PImage getDeleteIcon() {
		return deleteIcon;
	}

	public void setDeleteIcon(PImage deleteIcon) {
		this.deleteIcon = deleteIcon;
		redoChildOrder();
		reboundDeleteIcon();
	}

	public PImageArchiveNode getImage() {
		return image;
	}

	public void setImage(PImageArchiveNode image) {
		this.image = image;
		redoChildOrder();
		reboundImage();
	}

	public double getImageHeight() {
		return imageHeight;
	}

	public void setImageHeight(double imageHeight) {
		this.imageHeight = imageHeight;
		reboundAll();
	}

	public PTextLabel getKeywords() {
		return keywords;
	}

	public void setKeywords(PTextLabel keywords) {
		this.keywords = keywords;
		redoChildOrder();
		reboundKeywords();
	}

	public double getLabelSize() {
		return labelSize;
	}

	public void setLabelSize(double d) {
		this.labelSize = labelSize;
		reboundAll();
	}

	public double getMargin() {
		return margin;
	}

	public void setMargin(double margin) {
		this.margin = margin;
		reboundAll();
	}

	public PTextLabel getName() {
		return name;
	}

	public void setName(PTextLabel name) {
		this.name = name;
		redoChildOrder();
		reboundName();
	}

	public void setBoundsFromXY(double x, double y) {
		setBounds(x, y, margin + imageWidth + margin, margin + imageHeight + labelSize + labelSize + margin);
	}

	public double getImageWidth() {
		return imageWidth;
	}

	public void setImageWidth(double imageWidth) {
		this.imageWidth = imageWidth;
		reboundAll();
	}

	public PNode getSparkline() {
		return sparkline;
	}

	public void setSparkline(PNode sparkline) {
		this.sparkline = sparkline;
	}

	public Stroke getBoundaryStroke() {
		return boundaryStroke;
	}

	public void setBoundaryStroke(Stroke boundaryStroke) {
		this.boundaryStroke = boundaryStroke;
		invalidatePaint();
	}
}
