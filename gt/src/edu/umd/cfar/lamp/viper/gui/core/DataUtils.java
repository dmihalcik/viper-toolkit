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

package edu.umd.cfar.lamp.viper.gui.core;

import java.util.*;

import viper.api.*;

/**
 * Utility methods for dealing with viper data and video files,
 * something the api doesn't present.
 */
public class DataUtils {
	/**
	 * Gets the width (in pixels) of the given sourcefile.
	 * This doesn't take into account aspect ratio.
	 * @param s The sourcefile to check
	 * @return
	 */
	public static int getSourcefileWidth(Sourcefile s) {
		Iterator descIter = s.getDescriptorsBy(Config.FILE);
		if (descIter.hasNext()) {
			Descriptor info = (Descriptor) descIter.next();
			if (info.getConfig().hasAttrConfig("H-FRAME-SIZE")) {
				return ((Integer) info.getAttribute("H-FRAME-SIZE").getAttrValue()).intValue();
			}
		} 

		return 480;
	}

	/**
	 * Get the height (in pixels) of the 
	 * @param s
	 * @return
	 */
	public static int getSourcefileHeight(Sourcefile s) {
		Iterator descIter = s.getDescriptorsBy(Config.FILE);
		if (descIter.hasNext()) {
			Descriptor info = (Descriptor) descIter.next();
			if (info.getConfig().hasAttrConfig("V-FRAME-SIZE")) {
				return ((Integer) info.getAttribute("V-FRAME-SIZE").getAttrValue()).intValue();
			}
		} 

		return 360;
	}
	/**
	 * Get the number of frames in a given video file.
	 * @param s
	 * @return
	 */
	public static int getFrameCount(Sourcefile s) {
		Iterator descIter = s.getDescriptorsBy(Config.FILE);
		if (descIter.hasNext()) {
			Descriptor info = (Descriptor) descIter.next();
			if (info.getConfig().hasAttrConfig("NUMFRAMES")) {
				Attribute numFramesAttr = info.getAttribute("NUMFRAMES");
				Integer numFrames = (Integer) numFramesAttr.getAttrValue();
				if (numFrames != null) {
					return numFrames.intValue();
				}
			}
		}

		return 1;
	}
}
