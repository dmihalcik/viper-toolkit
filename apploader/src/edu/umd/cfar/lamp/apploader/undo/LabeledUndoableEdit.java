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

package edu.umd.cfar.lamp.apploader.undo;

/**
 * Alternative to java's undoable edit, this edit interface 
 * works better with the LAL framework, offering a URI as 
 * GUID to allow external manipulation with the preferences.
 * 
 * It is a little simplified, for now. 
 * 
 * @author davidm@cfar.umd.edu
 */
public interface LabeledUndoableEdit {
	/**
	 * Kill the undo. Not sure why this is necessary; assume
	 * has something to do with java's lack of descructors,
	 * so can be null-op in most implementations, as it will
	 * only be called before all references are removed.
	 */
	public void die();
	
	/**
	 * Checks to see if the action may be applied
	 * in the given state of the system.
	 * @return <code>true</code> if the action may be redone
	 */
	public boolean canRedo();

	/**
	 * Checks to see if the action may be undone
	 * in the given state of the system.
	 * @return <code>true</code> if the action may be undone
	 */
	public boolean canUndo();

	/**
	 * Apply the action again.
	 * @throws IllegalStateException if the action cannot be redone
	 */
	public void redo();

	/**
	 * Undo the action.
	 * @throws IllegalStateException if the action cannot be undone
	 */
	public void undo();

	/**
	 * Get the URI that identifies the type of action this
	 * object represents.
	 * @return A valid URI that represents the type of action
	 */
	public String getUri();

	/**
	 * Gets the recipient of the action.
	 * @return The object that had the 
	 * action acted upon it.
	 */
	public Object getClient();

	/**
	 * Gets the source of the action.
	 * @return The object where the action
	 * originated
	 */
	public Object getSource();
}
