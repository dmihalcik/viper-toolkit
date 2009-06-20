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


package edu.umd.cfar.lamp.viper.examples.persontracking.images;

import java.awt.*;
import java.lang.reflect.*;

import javax.swing.*;

import org.apache.commons.collections.Predicate;


import edu.oswego.cs.dl.util.concurrent.*;
import edu.umd.cfar.lamp.viper.examples.persontracking.images.ImageArchive.*;
import edu.umd.cs.piccolo.*;
import edu.umd.cs.piccolo.util.*;

/**
 * A piccolo visual node for displaying an image
 * that is backed by the image archive. (Unlike the PImage
 * class, which keeps a hard link to the image itself.)
 * @author davidm
 */
public class PImageArchiveNode extends PNode {
	/**
	 * 
	 */
	private final ImageArchive archive;

	private Callable imageFunction;
	
	private Object key;

	private int borderSize = 1;

	private Paint backgroundColor;

	/**
	 * @param gallery
	 */
	public PImageArchiveNode(ImageArchive archive) {
		this.archive = archive;
	}

	protected void paint(PPaintContext paintContext) {
		Graphics2D g2 = paintContext.getGraphics();

		if (backgroundColor != null) {
			g2.setPaint(backgroundColor);
			PBounds b = getBoundsReference();
			int x = (int) (b.x - borderSize);
			int y = (int) (b.y - borderSize);
			int w = (int) (b.width + 2 * borderSize);
			int h = (int) (b.height + 2 * borderSize);
			g2.fillRect(x, y, w, h);
		}
		if (!archive.paint(paintContext, this.getBoundsReference(), key, imageFunction, false)) {
			Dimension prefDim = new Dimension((int) getBoundsReference()
					.getWidth(), (int) getBoundsReference().getHeight());
			Predicate holdAndPaint = new Predicate() {
				public boolean evaluate(Object imageState) {
					final ImageArchive.ImageState is = (ImageState) imageState;
					is.holdOn();
					try {
						SwingUtilities.invokeAndWait(new Runnable() {
							public void run() {
								PImageArchiveNode.this.repaint();
							}
						});
						return true;
					} catch (InterruptedException e) {
						e.printStackTrace();
					} catch (InvocationTargetException e) {
						e.printStackTrace();
					}
					is.holdOff();
					return false;
				}
			};
			archive.put(key, imageFunction, prefDim, null);
		}
	}

	public Paint getBackgroundColor() {
		return backgroundColor;
	}

	public void setBackgroundColor(Paint backgroundColor) {
		this.backgroundColor = backgroundColor;
	}

	public int getBorderSize() {
		return borderSize;
	}

	public void setBorderSize(int borderSize) {
		this.borderSize = borderSize;
	}

	public Callable getImageFunction() {
		return imageFunction;
	}

	public void setImageFunction(Callable imageFunction) {
		this.imageFunction = imageFunction;
	}

	public Object getKey() {
		return key;
	}

	public void setKey(Object key) {
		this.key = key;
	}
}