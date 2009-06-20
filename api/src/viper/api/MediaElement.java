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

package viper.api;

import java.net.*;

import viper.api.time.*;

/**
 * Represents a media file. Used in MediaSequences and to describe Sourcefiles.
 */
public interface MediaElement {
	/**
	 * Gets the name of the media file this object describes.
	 * @return the file name
	 */
	public String getSourcefileName();
	
	
	/**
	 * Gets the sourcefile name as a URI.
	 * @return the file name
	 */
	public URI getSourcefileIdentifier();

	/**
	 * @param rate
	 */
	public void setFrameRate(FrameRate rate);

	/**
	 * Gets the frame rate of the video. Probably will be moved to a 
	 * seperate 'file information' interface to avoid bloat.
	 * @return the rate at which frames should appear to display the file at real time
	 */
	public FrameRate getFrameRate();

	/**
	 * Returns the Instant <code>i</code> in the the Instant format
	 * preferred by this Sourcefile (either Time or Frame). 
	 * 
	 * @param i
	 * @return Instant
	 */
	public Instant normalize(Instant i);

	/**
	 * Returns the Span <code>s</code> in the the Instant format
	 * preferred by this Sourcefile (either Time or Frame). 
	 * 
	 * @param s converts the time/frame specified interval into the preferred
	 * format of the element, either time or frame.
	 * @return the type of interval that can play nicely with getSpan,
	 * using the current frame rate information
	 */
	public InstantInterval normalize(InstantInterval s);
	
	/**
	 * Sets the span of the media element.
	 * @param r The new span.
	 */
	public void setSpan(InstantInterval r);
	
	/**
	 * Gets the time or frame span of the media element.
	 * @return The current span of the element.
	 */
	public InstantInterval getSpan();

}
