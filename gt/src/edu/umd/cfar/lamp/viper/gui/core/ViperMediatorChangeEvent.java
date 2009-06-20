package edu.umd.cfar.lamp.viper.gui.core;

import java.util.*;

import viper.api.extensions.*;

/**
 * Indicates a change that UI elements might
 * want to respond to.
 */
public class ViperMediatorChangeEvent extends EventObject {
	private ViperChangeEvent viperEvent;

	/**
	 * Constructs a new event object
	 * with the given source.
	 * @param source where the event occurred
	 */
	public ViperMediatorChangeEvent(ViperViewMediator source) {
		super(source);
	}

	/**
	 * Constructs a new change event.
	 * @param source the source of the change. Probably should be the mediator.
	 * @param viperEvent the associated viper change event, if it exists
	 */
	public ViperMediatorChangeEvent(ViperViewMediator source, ViperChangeEvent viperEvent) {
		super(source);
		this.viperEvent = viperEvent;
	}

	/**
	 * Gets the viper event that caused this change.
	 * @return the change event
	 */
	public ViperChangeEvent getViperEvent() {
		return viperEvent;
	}
}
