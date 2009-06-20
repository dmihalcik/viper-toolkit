package viper.api.impl;

import java.util.*;

import viper.api.extensions.*;

/**
 * An implementation of some of the more common forms of major node
 * change event methods.
 */
public abstract class AbstractMajorNodeChangeEvent extends AbstractViperChangeEvent implements MajorNodeChangeEvent {
	protected List events;
	private String uri;

	/**
	 * Creates a new major node change event with the 
	 * given class URI.
	 * @param uri the class of change event
	 */
	public AbstractMajorNodeChangeEvent(String uri) {
		this.uri = uri;
		this.events = new LinkedList();
	}
	
	/**
	 * Adds the new subevent to the list of events included
	 * in this major event.
	 * @param vce the event to add
	 */
	public void addEvent(ViperChangeEvent vce) {
		events.add(vce);
	}

	/**
	 * {@inheritDoc}
	 */
	public String getUri() {
		return uri;
	}

	/**
	 * {@inheritDoc}
	 */
	public Iterator getSubEvents() {
		return events.iterator();
	}

	/**
	 * Removes all children of the events, and performs
	 * any required cleanup.
	 */
	public void die() {
		events = null;
		uri = null;
	}
}
