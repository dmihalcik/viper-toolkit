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
 * Combines several actions upon a node (& its kids) into a single event.
 * 
 * Acts as a barrier to events. This node and its parents won't get events
 * until 'commit' is called. For the current implementation, rollback is
 * equivalent to calling the undo method on the undoable event that is 
 * announced when commit is called. This is not really much of a transaction,
 * as events aren't serializable so this is only useful for coagulating 
 * events, and not any sort of ACID stuff.
 */
public interface TransactionalNode extends EventfulNode {
	
	/**
	 * An object that keeps state information about a transaction
	 * in progress.
	 */
	public static interface Transaction {
		/**
		 * Adds the given property to the event that will be thrown.
		 * 
		 * @param property the name of the property to change
		 * @param value the value to give the property
		 */
		public void putProperty(String property, Object value);

		/**
		 * Commits the current changes.
		 */
		public void commit();

		/**
		 * Rolls back the transaction.
		 */
		public void rollback();
		
		/**
		 * Gets a list of events so far.
		 * @return
		 */
		public ViperChangeEvent[] soFar();
		
		/**
		 * Tests to see if the transaction is still active.
		 * @return <code>true</code> if the transaction hasn't
		 * been rolled back or committed
		 */
		public boolean isAlive();
	}
	
	/**
	 * Starts a transaction on this node.
	 * @param uri an identifier to associate with the 
	 * transaction; useful for logging
	 * @return the Transaction object associated with the
	 * new transaction.
	 */
	public Transaction begin(String uri);
	
	public boolean isWriteLocked();
}
