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


import java.awt.event.*;
import java.awt.geom.*;

import viper.api.*;
import viper.api.extensions.*;
import viper.api.time.*;
import edu.umd.cfar.lamp.viper.gui.canvas.datatypes.*;
import edu.umd.cfar.lamp.viper.gui.core.*;
import edu.umd.cfar.lamp.viper.util.*;
import edu.umd.cs.piccolo.*;
import edu.umd.cs.piccolo.event.*;

/**
 * Base class for spatial data editor proxies.
 */
public abstract class CanvasEditor extends PBasicInputEventHandler implements
		Selectable {
	static Highlightable colorTable = HighlightSingleton.colorTable;

	private Attributable attr;

	private ViperViewMediator mediator;

	private boolean displayWRTmode = false;

	private boolean selected = false;

	public CanvasEditor(Attributable attrIn) {
		assert attrIn instanceof PNode;
		attr = attrIn;
		displayUnselected();
	}

	public abstract String getName();

	public PNode getShape() {
		return (PNode) attr;
	}

	public void updateAttributable(Instant lastFrame, Instant currentFrame,
			Instant nextFrame) {
		Object newVal = attr.getUpdatedAttribute();
		Attribute a = attr.getAttribute();
		if (((TransactionalNode) a).isWriteLocked()) {
			// Cannot update attributable that is locked. This probably means that 'propagate' is set.
			// I probably should lock the attribute that the user is dragging...
			return;
		}

		Span s;
		if (lastFrame.compareTo(currentFrame) <= 0) {
			s = new Span(lastFrame, nextFrame);
		} else {
			s = new Span(currentFrame, lastFrame);
		}
		// XXX: dragging while playing back should interpolate the values 
		// between lastFrame and currentFrame, then propagate from 
		// currentFrame to nextFrame. Unfortunately, the frames seem
		// to be a little out of whack for this, so instead it just 
		// propagates through the widest possible swath.
		mediator.setAttributeValueAtSpan(newVal, a, s);
		mediator.getSelection().setTo(a);
	}

	public Attributable getAttributable() {
		return attr;
	}

	public void setSelected(boolean isSelected) {
		selected = isSelected;
		refresh();
	}

	/**
	 * @return
	 */
	public boolean isDisplayWRTmode() {
		return displayWRTmode;
	}

	/**
	 * @param b
	 */
	public void setDisplayWRTmode(boolean turnOn) {
		displayWRTmode = turnOn;
		refresh();
	}

	protected void refresh() {
		if (selected)
			displaySelected();
		else
			displayUnselected();
	}

	protected boolean isLockedMode() {
		if (mediator != null) {
			// this is usually true, except during construction
			return NodeVisibilityManager.LOCKED == mediator.getHiders()
					.getAttributeVisibility(attr.getAttribute());
		}
		return false;
	}

	protected boolean isLocked(PInputEvent e) {
		return isDisplayWRTmode() || isLockedMode();
	}

	protected boolean isLeftClicked(PInputEvent e) {
		return (e.getModifiersEx() & InputEvent.BUTTON1_DOWN_MASK) != 0;
	}

	protected boolean doNotProcessEvent(PInputEvent e) {
		return !isLeftClicked(e) || isLocked(e);
	}

	private ShapeDisplayProperties styleSelected = HighlightSingleton.STYLE_SELECTED;

	private ShapeDisplayProperties styleLockedSelected = HighlightSingleton.STYLE_LOCKED_SELECTED;

	private ShapeDisplayProperties styleLocked = HighlightSingleton.STYLE_LOCKED_UNSELECTED;

	private ShapeDisplayProperties styleDefault = HighlightSingleton.STYLE_UNSELECTED;
	
	private ShapeDisplayProperties styleNone = HighlightSingleton.STYLE_HIDDEN;
	
	private ShapeDisplayProperties styleHandle = HighlightSingleton.STYLE_HANDLE;

	private void displaySelected() {
		doWhenSelected();
		boolean lock = isDisplayWRTmode() || isLockedMode();
		attr.setDisplayProperties(lock ? styleLockedSelected : styleSelected);
		attr.setHandleDisplayProperties(lock ? styleNone : styleHandle);
	}

	private void displayUnselected() {
		doWhenUnselected();
		boolean lock = isDisplayWRTmode() || isLockedMode();
		attr.setDisplayProperties(lock ? styleLocked : styleDefault);
		attr.setHandleDisplayProperties(styleNone);
	}

	// Override if you want it to do something interesting
	// such as highlighting other shapes that aren't part of shape
	public void doWhenSelected() {

	}

	public abstract void doWhenUnselected();

	/**
	 * @return
	 */
	public ViperViewMediator getMediator() {
		return mediator;
	}

	/**
	 * @param mediator
	 */
	public void setMediator(ViperViewMediator mediator) {
		this.mediator = mediator;
		refresh();
	}

	/**
	 * 
	 * @param point
	 * @return true if the CanvasEditor thinks this point is close enough to do
	 *         something with. It returns false if point is far away enough that
	 *         it's willing to let the EditorManager find a new shape to be
	 *         selected as editor.
	 */
	public abstract boolean inRangeOfInterest(Point2D point);

	// Returns true if this is the selected editor
	// Does so by querying the mediator
	public boolean isSelected() {
		if (mediator == null)
			return false;

		// Check to see if this is the selected attribute
		return mediator.getSelection().isSelected(attr.getAttribute());
	}

	/**
	 * Sets the attribute to null, so that it is no longer being drawn This
	 * method is called when the user removes an object by pressing the delete
	 * key. This also removes the attribute from selection.
	 */
	public void zap() {
		Attribute atToChange = getAttributable().getAttribute();
		mediator.setAttributeValueAtCurrentFrame(null, atToChange);
		if (mediator.getSelection().isSelected(atToChange)) {
			mediator.getSelection().setTo(atToChange.getParent());
		}
	}
}

