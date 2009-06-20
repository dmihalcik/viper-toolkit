/*
 * Created on Jan 13, 2004
 *
 * To change the template for this generated file go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
package viper.api.impl;

import java.util.*;

import viper.api.extensions.*;

/**
 * Implements some of the common elements of a viper change event.
 */
public abstract class AbstractViperChangeEvent implements ViperChangeEvent {
	private Map properties;
	
	/**
	 * Creates a new empty vipre change event.
	 */
	public AbstractViperChangeEvent() {
		properties = new HashMap();
	}

	/**
	 * {@inheritDoc}
	 */
	public Object getProperty(String prop) {
		return properties.get(prop);
	}
	
	/**
	 * {@inheritDoc}
	 */
	public Iterator listProperties() {
		return properties.keySet().iterator();
	}

	protected void addProperty(String name, Object value) {
		properties.put(name, value);
	}
}
