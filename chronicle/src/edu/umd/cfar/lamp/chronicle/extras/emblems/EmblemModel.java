package edu.umd.cfar.lamp.chronicle.extras.emblems;

import java.awt.*;

import javax.swing.event.*;

import edu.umd.cfar.lamp.chronicle.*;

/**
 * Model for the emblems that appear next to the labels for the timelines.
 */
public interface EmblemModel {
	/**
	 * Gets the maximum number of emblems any 
	 * timeline has. This is useful if you want
	 * to left-align your emblems or something 
	 * @return the maximum number of emblems
	 */
	public int getMaxEmblemCount();
	
	/**
	 * Gets the emblems for the given timeline.
	 * @param tqe the timeline to key on
	 * @param i the emblem to get
	 * @return an emblem, if the ith emblem exists
	 */
	public Image getEmblemFor(TimeLine tqe, int i);
	
	/**
	 * Gets the tooltip text for the emblem.
	 * @param tqe the timeline to key on
	 * @param i the emblem index for the given timeline
	 * @return the string, if the ith emblem exists
	 */
	public String getTextEmblemFor(TimeLine tqe, int i);
	
	/**
	 * Indicates a mouse-click on the specified emblem.
	 * @param tqe the timeline whose emblem was clicked
	 * @param i the offset of the emblem that was clicked
	 */
	public void click(TimeLine tqe, int i);
	
	/**
	 * Listen for changes to the emblem model!
	 * @param cl Using this listener!  
	 */
	public void addChangeListener(ChangeListener cl);
	
	/**
	 * Removes the listener from the emblem model's listener list.
	 * @param cl
	 */
	public void removeChangeListener(ChangeListener cl);
}
