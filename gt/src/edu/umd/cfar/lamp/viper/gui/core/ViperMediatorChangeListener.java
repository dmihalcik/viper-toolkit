package edu.umd.cfar.lamp.viper.gui.core;

import java.util.*;

/**
 */
public interface ViperMediatorChangeListener extends EventListener {
	/**
	 * The schema has changed, which can cause the data to change.
	 * The sourcefiles haven't changed, though. Wait, if that's true, what
	 * event occurs when the user loads a new viper.xml file? In that case,
	 * you will get a schemaChanged followed by a currFileChanged.
	 * @param e the change event; will have an associated ViperChangeEvent
	 */
	public void schemaChanged(ViperMediatorChangeEvent e);

	/**
	 * The chosen file has changed. What files exist
	 * changing will invoke a data change event. This event
	 * will be notified after the selection listeners are
	 * notified that the user's selection has changed.
	 * @param e the change event
	 */
	public void currFileChanged(ViperMediatorChangeEvent e);
	
	/**
	 * The display of the media (ie the DataPlayer) has changed
	 * @param e the change event
	 */
	public void mediaChanged(ViperMediatorChangeEvent e);

	/**
	 * The majorMoment - the frame the user wants to be focussed on - 
	 * has changed.
	 * @param e
	 */
	public void frameChanged(ViperMediatorChangeEvent e);
	
	/**
	 * Wraps up any ViperChangeEvent that occur on the ViperData
	 * node beneath the sourcefiles level.
	 * @param e the change event; will have an associated ViperChangeEvent
	 */
	public void dataChanged(ViperMediatorChangeEvent e);
}
