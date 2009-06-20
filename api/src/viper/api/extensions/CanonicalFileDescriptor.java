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

package viper.api.extensions;

import viper.api.*;
import viper.api.time.*;

/**
 * @author davidm
 */
public interface CanonicalFileDescriptor {
	/**
	 * Gets the FILE Config associated with the 
	 * canonical file descriptor.
	 * @return the FILE config
	 */
	public abstract Config getConfig();
	
	/**
	 * Gets the real descriptor backing this
	 * canonical file descriptor java object.
	 * @return the backing descriptor; any change to 
	 * <code>this</code> is reflected here
	 */
	public abstract Descriptor getDescriptor();

	/**
	 * Gets the width and height of the file.
	 * @return the width and height of the source media
	 * image
	 */
	public abstract int[] getDimensions();
	
	/**
	 * The number of frames in the source video.
	 * @return the number of frames in the video
	 */
	public abstract int getNumFrames();

	/**
	 * Gets the media type. Should be a mime/type, but
	 * there is no constraint on this, so I don't know 
	 * how useful it is.
	 * @return the media type, as listed
	 * in the descriptor
	 */
	public abstract String getMediaType();

	/**
	 * Gets the frame rate of the video.
	 * @return the frame rate specified in the metadata
	 */
	public abstract FrameRate getFrameRate();

	/**
	 * Sets the number of frames information in the metadata
	 * @param numberOfFramesInClip the number of frames in the source
	 * video
	 */
	public abstract void setNumFrames(int numberOfFramesInClip);
	
	/**
	 * Sets the media type string for this file.
	 * @param mediaType the media type, preferrably a mime type
	 */
	public abstract void setMediaType(String mediaType);
	
	/**
	 * Sets the dimensions of the video clip.
	 * @param width the width, preferrably in pixels
	 * @param height the height, preferrably in pixels
	 */
	public abstract void setDimensions(int width, int height);

	/**
	 * Sets the frame rate associated with the video clip.
	 * @param rate the frame rate of the source video
	 */
	public abstract void setFrameRate(FrameRate rate);
}