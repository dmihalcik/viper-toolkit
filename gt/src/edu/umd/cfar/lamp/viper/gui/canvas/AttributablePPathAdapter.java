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


package edu.umd.cfar.lamp.viper.gui.canvas;

import viper.api.*;
import viper.api.time.*;
import edu.umd.cfar.lamp.viper.gui.canvas.datatypes.*;
import edu.umd.cfar.lamp.viper.gui.core.*;
import edu.umd.cfar.lamp.viper.util.*;
import edu.umd.cs.piccolo.nodes.*;

/**
 * Adds some style management to the PPath node.
 * @author davidm
 */
public abstract class AttributablePPathAdapter extends PPath implements Attributable {
	private ShapeDisplayProperties displayProperties = HighlightSingleton.STYLE_UNSELECTED;
	private ShapeDisplayProperties highlightDisplayProperties = HighlightSingleton.STYLE_HOVER;
	private ShapeDisplayProperties handleDisplayProperties = HighlightSingleton.STYLE_HANDLE;
	
	protected AttributablePPathAdapter (ViperViewMediator mediator) {
		this.mediator = mediator;
	}
	
	public ShapeDisplayProperties getHandleDisplayProperties() {
		return handleDisplayProperties;
	}
	public ShapeDisplayProperties getDisplayProperties() {
		return displayProperties;
	}
	public ShapeDisplayProperties getHighlightDisplayProperties() {
		return highlightDisplayProperties;
	}
	/** @inheritDoc */
	public void setHandleDisplayProperties(
			ShapeDisplayProperties handleDisplayProperties) {
		this.handleDisplayProperties = handleDisplayProperties;
		resetStyle();
	}
	/** @inheritDoc */
	public void setDisplayProperties(ShapeDisplayProperties properties) {
		this.displayProperties = properties;
		resetStyle();
	}
	/** @inheritDoc */
	public void setHighlightDisplayProperties(
			ShapeDisplayProperties properties) {
		this.highlightDisplayProperties = properties;
		resetStyle();
	}
	
	/**
	 * Called by the setDisplayProperties methods, and whenever the 
	 * style needs changing.
	 */
	protected abstract void resetStyle();

	public Attribute getAttribute() {
		return attr;
	}
	
	/**
	 * @return Returns the mediator.
	 */
	public ViperViewMediator getMediator() {
		return mediator;
	}

	/**
	 * The mediator is required for 'setAttribute' method, and for making sure
	 * that the rotation is only changed when there is a large enough change.
	 * 
	 * @param mediator
	 *            The mediator to set.
	 */
	public void setMediator(ViperViewMediator mediator) {
		this.mediator = mediator;
	}


	// api access
	protected Attribute attr;
	
	/**
	 * If this is non-null, then this is used as the current instant.
	 * Otherwise, the mediator's current instant (major moment)
	 * is used. If both are null, null is returned.
	 */
	protected Instant currentInstant;

	protected ViperViewMediator mediator;

	/**
	 * If this is non-null, then this is used as the current instant.
	 * Otherwise, the mediator's current instant (major moment)
	 * is used. If both are null, null is returned.
	 */
	public Instant getInstant() {
		if (currentInstant != null) {
			return currentInstant;
		}
		if (mediator != null) {
			return mediator.getMajorMoment();
		}
		return null;
	}

	/**
	 * To use the mediator's major moment, set to null.
	 */
	public void setInstant(Instant i) {
		this.currentInstant = i;
	}

}
