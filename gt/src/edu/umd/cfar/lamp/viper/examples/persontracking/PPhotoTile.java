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
import java.awt.image.*;

import edu.umd.cfar.lamp.viper.examples.persontracking.images.*;
import edu.umd.cs.piccolo.*;
import edu.umd.cs.piccolo.nodes.*;
import edu.umd.cs.piccolo.util.*;

/**
 * An image tile for use in a photo browser
 * or the like. In order to manage memory usage, it
 * decodes the image in the background as required, using
 * a worker thread. It may keep a small version of the image around,
 * though. 
 * @author davidm
 */
public class PPhotoTile extends PWorkerNode {
	private static final int THUMB_SIZE = 16;
	private PImage smallVersion;
	
	private Paint backgroundPaint;
	private int borderWidth;

	protected void paint(PPaintContext paintContext) {
		Graphics2D g2 = paintContext.getGraphics();
		
		if (backgroundPaint != null) {
			g2.setPaint(backgroundPaint);
			PBounds b = getBoundsReference();
			int x = (int) (b.x - borderWidth);
			int y = (int) (b.y-borderWidth);
			int w = (int) (b.width + 2*borderWidth);
			int h = (int) (b.height + 2*borderWidth);
			g2.fillRect(x, y, w, h);
		}
		PNode ln = getLastNode();
		if (ln == null && smallVersion != null) {
			smallVersion.setBounds(getBoundsReference());
			smallVersion.fullPaint(paintContext);
			rehireWorkerIfNecessary();
		} else if (ln != null && smallVersion == null && (ln instanceof PImage)) {
			// FIXME modify to happen in the background thread
			BufferedImage original = PImage.toBufferedImage(((PImage) ln).getImage(), false);
			setSmallVersion(original);
			super.paint(paintContext);
		} else {
			super.paint(paintContext);
		}
	}
	
	/**
	 * @param original
	 */
	public void setSmallVersion(BufferedImage original) {
		Dimension t = SmartImageUtilities.smartResize(original.getWidth(), original.getHeight(), THUMB_SIZE);
		smallVersion = new PImage(original.getScaledInstance(t.width, t.height, Image.SCALE_DEFAULT));
	}

	public Paint getBackgroundPaint() {
		return backgroundPaint;
	}

	public void setBackgroundPaint(Paint backgroundPaint) {
		this.backgroundPaint = backgroundPaint;
	}

	public int getBorderWidth() {
		return borderWidth;
	}

	public void setBorderWidth(int borderWidth) {
		this.borderWidth = borderWidth;
	}

}
