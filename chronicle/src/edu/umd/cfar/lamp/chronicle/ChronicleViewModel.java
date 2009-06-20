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

package edu.umd.cfar.lamp.chronicle;

import viper.api.time.*;

/**
 * The view model is a necessary part of the 
 * chronicle's view, indicating which timelines to display,
 * and what interval to display over.
 */
public interface ChronicleViewModel {
	/**
	 * Gets the data model this view mediates.
	 * @return the data model
	 */
	public abstract ChronicleDataModel getGraph();

	/**
	 * Gets the number of lines currently in view.
	 * @return the number of lines to display
	 */
	public abstract int getSize();
	
	/**
	 * Gets the ith visible line.
	 * @param i the line to retrieve
	 * @return the line
	 * @throws IndexOutOfBoundsException when i is not within
	 * the [0,getSize) interval
	 */
	public abstract TimeLine getElementAt(int i);

	/**
	 * Gets the current moment the user is most focussed on.
	 * @return the major moment
	 */
	public abstract Instant getMajorMoment();
	
	/**
	 * Sets the focal instant.
	 * @param m the major moment
	 */
	public abstract void setMajorMoment(Instant m);

	/**
	 * Gets the instant interval to display.
	 * @return the frames to display in the chronicle
	 */
	public abstract InstantInterval getFocus();
	
	/**
	 * Sets the frames to display.
	 * @param ii the new span
	 */
	public abstract void setFocus (InstantInterval ii);
	
	/**
	 * Adds a new listener for changes to the view.
	 * @param cl a listener
	 */
	public abstract void addChronicleViewListener(ChronicleViewListener cl);
	
	/**
	 * Removes an existing listener for changes to the view.
	 * @param cl a listener
	 */
	public abstract void removeChronicleViewListener(ChronicleViewListener cl);

	/**
	 * Gets the frame rate for the timelines.
	 * This is useful when dealing with data that may be specified 
	 * in frames or in time. I suppose a frame really should be treated
	 * as an interval of time, but for our purposes a frame is the first 
	 * instant the frame appears.
	 * @return the frame rate for media the timeline describes
	 */
	public abstract FrameRate getFrameRate();
	
	public int indexOf(TimeLine tqe);
}
