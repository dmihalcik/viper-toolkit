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

package viper.api.extensions;

/**
 * Interface, extending a viper event to make it undoable.
 */
public interface ViperUndoableEvent extends ViperChangeEvent {
	/**
	 * Gets the undoable edit object associated with the event.
	 * @return the Undoable editor
	 */
	public ViperUndoableEvent.Undoable getUndoable();
	
	/**
	 * An undo object, that supports undoing and redoing
	 * a given event.
	 * TODO: add 'replaceEdit' method.
	 */
	public interface Undoable {
		/**
		 * Detach/destroy the edit object.
		 */
		public void die();
		/**
		 * Undo the event, if possible.
		 * @throws IllegalStateException if cannot undo
		 */
		public void undo();
		/**
		 * Redo the event, if possible.
		 * @throws IllegalStateException if cannot redo
		 */
		public void redo();
		
		/**
		 * Check to see if the event may be undone at the
		 * current time.
		 * @return <code>true</code> if calling undo will work
		 */
		public boolean canUndo();

		/**
		 * Check to see if the event may be redone at the
		 * current time.
		 * @return <code>true</code> if calling redo will work
		 */
		public boolean canRedo();
	}
}
