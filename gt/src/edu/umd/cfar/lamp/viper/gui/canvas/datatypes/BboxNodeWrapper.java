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

package edu.umd.cfar.lamp.viper.gui.canvas.datatypes;

import java.awt.*;

import edu.umd.cfar.lamp.viper.geometry.*;
import edu.umd.cfar.lamp.viper.gui.canvas.*;
import edu.umd.cs.piccolo.*;
import edu.umd.cs.piccolo.nodes.*;


/**
 * Converts from a 'bbox' object to a PNode for display on 
 * the viper canvas.
 * 
 * @author davidm
 */
public class BboxNodeWrapper implements ViewableAttribute {
	public PNode getViewable(Object o) {
		if (o == null) {
			return null;
		} else {
			PPath mpath = new PPath();
			//mpath.setStroke(new BasicStroke((float)(1/ c.getCamera().getViewScale())));
			mpath.setStrokePaint(Color.GREEN);
			setPath(mpath, (BoundingBox) o);
			return mpath; 
		}
	}
	public PNode updateViewable(Object o, PNode old) {
		if ((o instanceof BoundingBox) && (old instanceof PPath)) {
			BoundingBox box = (BoundingBox) o;
			PPath mpath = (PPath) old;
			setPath(mpath, box);
			return mpath;
		} else {
			return getViewable(o);
		}
	}

	private void setPath(PPath p, BoundingBox box) {
		p.setPathToRectangle(box.getX(), box.getY(), box.getWidth(), box.getHeight());
	}
}
