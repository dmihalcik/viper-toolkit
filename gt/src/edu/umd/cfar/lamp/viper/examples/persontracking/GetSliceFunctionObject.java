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

import org.apache.commons.lang.builder.*;

import edu.oswego.cs.dl.util.concurrent.*;
import edu.umd.cs.piccolo.*;
import edu.umd.cs.piccolo.nodes.*;
import edu.umd.cs.piccolo.util.*;

/**
 * Given the specified 
 * @author davidm
 */
class GetSliceFunctionObject implements Callable {
	/**
	 * 
	 */
	private final PersonGalleryModel gallery;

	final private ImageSlice id;

	private PNode parent;

	private PCamera cam;

	GetSliceFunctionObject(PersonGalleryModel gallery, ImageSlice id,
			PNode parent, PCamera cam) {
		assert gallery != null;
		assert id != null;
		this.gallery = gallery;
		this.id = id;
		this.parent = parent;
		this.cam = cam;
	}

	public Object call() throws Exception {
		boolean needsCorrelogram = gallery.getCorrelogramForSlice(id) == null;
		boolean visible = false;
		Rectangle2D myBounds = null;
		if (parent != null) {
			myBounds = parent.getBoundsReference();
			visible = cam.intersects(cam.viewToLocal((PBounds) myBounds.clone()));
		}
		if (!needsCorrelogram && !visible) {
			return null;
		}
		final Image subImage = this.gallery.getSubImage(id);
		if (needsCorrelogram) {
			this.gallery.checkCorrelogram(null, id, subImage);
		}
		if (null == subImage) {
			return null;
		}
		if (!visible) {
			return subImage;
		}
		final Image scaledSubImage = subImage.getScaledInstance((int) myBounds.getWidth(),
				(int) myBounds.getHeight(), Image.SCALE_DEFAULT);
		return PImage.toBufferedImage(scaledSubImage, true);
	}

	public String toString() {
		return new ToStringBuilder(this).append("Slice", id).toString();
	}

	public ImageSlice getSliceId() {
		return id;
	}
}