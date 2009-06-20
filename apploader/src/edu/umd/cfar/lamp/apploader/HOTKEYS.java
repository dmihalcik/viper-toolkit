/***************************************
 *:// L A M P . cfar . umd . edu       *
 *      AppLoader                      *
 *                                     *
 *      A tool for loading java apps   *
 *             from RDF descriptions.  *
 *                                     *
 * Distributed under the GPL license   *
 *        Terms available at gnu.org.  *
 *                                     *
 *  Copyright University of Maryland,  *
 *                      College Park.  *
 ***************************************/

package edu.umd.cfar.lamp.apploader;

import com.hp.hpl.jena.rdf.model.*;

/**
 * Adds hotkeys to components in an apploader interface.
 * 
 * For information, see the
 * <a href="http://viper-toolkit.sourceforge.net/owl/apploader/hotkey#">namespace
 * document</a>.
 * 
 * @author davidm
 */
public class HOTKEYS {
	protected static final String uri =
		"http://viper-toolkit.sourceforge.net/owl/apploader/hotkeys#";

	/** 
	 * Returns the URI for this schema,
	 * <code>http://viper-toolkit.sourceforge.net/owl/apploader/hotkeys#</code>.
	 * @return the URI for this schema
	 */
	public static String getURI() {
		return uri;
	}

	/**
	 * A class of OWL objects that describe a binding from
	 * a keypress or stroke to some kind of action.
	 */
	public static final Resource HotkeyBinding =
		ResourceFactory.createResource(uri + "HotkeyBinding");

	/**
	 * Has an input action. This property isn't used directly; instead,
	 * its children are used. The key binding architecture mirror's java's;
	 * for more detail, see <cite><a href="http://java.sun.com/docs/books/tutorial/uiswing/misc/keybinding.html">How to Use Java's Key Bindings</a></cite>.
	 */
	public static final Property inputAction =
		ResourceFactory.createProperty(uri + "inputAction");

	/**
	 * Has a local input action. This is analogous to 
	 * <a href="http://java.sun.com/j2se/1.4.2/docs/api/javax/swing/JComponent.html#WHEN_FOCUSED">JComponent.WHEN_FOCUSED</a>.
	 * This means that the hot key will only be used when the bean 
	 * has focus, and the event hasn't been swallowed by another binding.
	 */
	public static final Property localInputAction =
		ResourceFactory.createProperty(uri + "localInputAction");

	/**
	 * The action occurs when any of the swing ancestors of
	 * bean this is bound to see the hotkey stroke.
	 */
	public static final Property ancestorInputAction =
		ResourceFactory.createProperty(uri + "ancestorInputAction");

	/**
	 * The action will happen when the window containing the bound
	 * bean has seen the stroke.
	 */
	public static final Property windowInputAction =
		ResourceFactory.createProperty(uri + "windowInputAction");

	/**
	 * An action that will be invoked when the binding takes effect.
	 */
	public static final Property hasAction =
		ResourceFactory.createProperty(uri + "hasAction");

	/**
	 * A hot key for the action.
	 */
	public static final Property hotkey =
		ResourceFactory.createProperty(uri + "hotkey");

	/**
	 * When, either before or after dispatch, the binding
	 * should be tried. This is very important on things like tables
	 * or text input beans, which usually suck up all key events.
	 */
	public static final Property when =
		ResourceFactory.createProperty(uri + "when");

	/**
	 * A marker for the {@link #when} property, indicating that
	 * the event should be processed before the focused bean has its
	 * go.
	 */
	public static final Resource DuringDispatch =
		ResourceFactory.createResource(uri + "DuringDispatch");

	/**
	 * A marker for the {@link #when} property, indicating that
	 * the event should be processed <em>after</em> the focused bean has its
	 * go.
	 */
	public static final Resource DuringPost =
		ResourceFactory.createResource(uri + "DuringPost");


	/**
	 * The three input types; this list corresponds to the three 
	 * input map conditions for a JComponent.
	 */
	public static final Property[] INPUT_TYPES = new Property[] {
		localInputAction, ancestorInputAction, windowInputAction
	};
	
	/**
	 * Gets the int version of the type id, e.g. {@link #localInputAction}
	 * of {@link #windowInputAction}.
	 * @param p the action to check
	 * @return the index of the type into the INPUT_TYPES array. 
	 * -1 iff not a known type of input action binding.
	 */
	public static int getTypeIDForInputActionPredicate(Property p) {
		if (p == null) {
			return -1;
		}
		String uriToCheck = p.getURI();
		for (int i = 0; i < INPUT_TYPES.length; i++) {
			Property checkAgainst = INPUT_TYPES[i];
			/// FIXME using uri to avoid npx in jena
			if (checkAgainst.getURI().equals(uriToCheck)) {
				return i;
			}
		}
		return -1;
	}
}
