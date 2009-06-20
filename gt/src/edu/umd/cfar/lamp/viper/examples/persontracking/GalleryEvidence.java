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

import edu.umd.cfar.lamp.viper.geometry.*;

public interface GalleryEvidence {
	public GalleryEntity getEntity();
	public BoundingBox getBox();
	public int[][][] getCorrelogramForEvidence();
	public int getFrame();
	public int getSimilarity();
	public boolean upToDate();
	public ImageSlice getSlice();
	public int getPriority();
}
