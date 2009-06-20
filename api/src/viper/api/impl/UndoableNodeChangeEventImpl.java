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

package viper.api.impl;

import java.util.*;

import javax.swing.event.*;
import javax.swing.undo.*;

import viper.api.*;
import viper.api.extensions.*;

/**
 * Implementation of the NodeChangeEvent as an UndoableEditEvent.
 */
class UndoableNodeChangeEventImpl
	extends UndoableEditEvent
	implements NodeChangeEvent, ViperUndoableEvent {
		private Map properties;
	

		/**
		 * @see viper.api.extensions.ViperChangeEvent#getProperty(java.lang.String)
		 */
		public Object getProperty(String prop) {
			return properties.get(prop);
		}

		/**
		 * @see viper.api.extensions.ViperChangeEvent#listProperties()
		 */
		public Iterator listProperties() {
			return properties.keySet().iterator();
		}

		protected void addProperty(String name, Object value) {
			properties.put(name, value);
		}

	/**
	 * 
	 */
	public static class UndoableEditor implements UndoableEdit, ViperUndoableEvent.Undoable {
		private EventfulNodeHelper parent;
		private Node oldValue, newValue;
		private int oldIndex;
		private int newIndex;
		private int state = 0;
		private static final int DONE = 0;
		private static final int UNDONE = 1;
		private static final int KILLED = -1;
		UndoableEditor(EventfulNodeHelper parent, Node oldValue, Node newValue, int oldIndex, int newIndex) {
			this.parent = parent;
			this.oldValue = oldValue;
			this.newValue = newValue;
			this.oldIndex = oldIndex;
			this.newIndex = newIndex;
		}
		/**
		 * Returns the newValue.
		 * @return Node
		 */
		public Node getNewValue() {
			return newValue;
		}

		/**
		 * Returns the oldValue.
		 * @return Node
		 */
		public Node getOldValue() {
			return oldValue;
		}

		/**
		 * Returns the parent.
		 * @return Node
		 */
		public Node getParent() {
			return parent;
		}

		/**
		 * @see javax.swing.undo.UndoableEdit#undo()
		 */
		public void undo() throws CannotUndoException {
			if (canUndo()) {
				if (oldValue == null) {
					parent.removeChild(newIndex, false);
				} else if (newValue == null) {
					parent.addChild(oldIndex, oldValue, false);
				} else {
					parent.setChild(oldIndex, oldValue, false);
				}
				state = UNDONE;
			} else {
				throw new CannotUndoException ();
			}
		}
		/**
		 * @see javax.swing.undo.UndoableEdit#canUndo()
		 */
		public boolean canUndo() {
			return state == DONE;
		}
		/**
		 * @see javax.swing.undo.UndoableEdit#redo()
		 */
		public void redo() throws CannotRedoException {
			if (canRedo()) {
				if (newValue == null) {
					parent.removeChild(oldIndex, false);
				} else if (oldValue == null) {
					parent.addChild(newIndex, newValue, false);
				} else {
					parent.setChild(newIndex, newValue, false);
				}
				state = DONE;
			} else {
				throw new CannotRedoException();
			}
		}
		/**
		 * @see javax.swing.undo.UndoableEdit#canRedo()
		 */
		public boolean canRedo() {
			return state == UNDONE;
		}
		/**
		 * @see javax.swing.undo.UndoableEdit#die()
		 */
		public void die() {
			parent = null;
			oldValue = newValue = null;
			state = KILLED;
		}
		/**
		 * @see javax.swing.undo.UndoableEdit#addEdit(javax.swing.undo.UndoableEdit)
		 */
		public boolean addEdit(UndoableEdit anEdit) {
			// XXX-> Maybe should congeal ones with same parent?
			return false;
		}
		/**
		 * @see javax.swing.undo.UndoableEdit#replaceEdit(javax.swing.undo.UndoableEdit)
		 */
		public boolean replaceEdit(UndoableEdit anEdit) {
			return false;
		}
		/**
		 * @see javax.swing.undo.UndoableEdit#isSignificant()
		 */
		public boolean isSignificant() {
			return oldValue != newValue;
		}
		/**
		 * @see javax.swing.undo.UndoableEdit#getPresentationName()
		 */
		public String getPresentationName() {
			if (oldValue == null) {
				return "Created " + newValue;
			} else if (newValue == null) {
				return "Removed " + oldValue;
			} else {
				return "Changed " + oldValue + " to " + newValue;
			}
		}
		/**
		 * @see javax.swing.undo.UndoableEdit#getUndoPresentationName()
		 */
		public String getUndoPresentationName() {
			return "Undo " + getPresentationName();
		}
		/**
		 * @see javax.swing.undo.UndoableEdit#getRedoPresentationName()
		 */
		public String getRedoPresentationName() {
			return "Redo " + getPresentationName();
		}
	}

	/**
	 * Creates a new NodeChangeEvent.
	 * @param parent   the node above the changed node
	 * @param oldValue the old value. <code>null</code> if the node is new.
	 * @param newValue the new value. <code>null</code> if the node is removec.
	 * @param oldIndex the offset into the children of the parent that this node is/was
	 * @param newIndex the index of the new value
	 * @param localName the local part of the event uri
	 */
	public UndoableNodeChangeEventImpl(EventfulNodeHelper parent, Node oldValue, Node newValue, int oldIndex, int newIndex, String localName) {
		super(parent, new UndoableEditor(parent, oldValue, newValue, oldIndex, newIndex));
		properties = new HashMap();
		lname = localName;
	}
	private String lname;
	
	/**
	 * @see viper.api.extensions.NodeChangeEvent#getIndex()
	 */
	public int getIndex() {
		return ((UndoableEditor) getEdit()).oldIndex;
	}

	/**
	 * @see ViperChangeEvent#getParent()
	 */
	public Node getParent() {
		return ((UndoableEditor) getEdit()).getParent();
	}

	/**
	 * @see NodeChangeEvent#getNewValue()
	 */
	public Node getNewValue() {
		return ((UndoableEditor) getEdit()).getNewValue();
	}

	/**
	 * @see NodeChangeEvent#getOldValue()
	 */
	public Node getOldValue() {
		return ((UndoableEditor) getEdit()).getOldValue();
	}

	/**
	 * @see viper.api.extensions.ViperChangeEvent#getUri()
	 */
	public String getUri() {
		return ViperParser.IMPL + lname;
	}

	/**
	 * @see viper.api.extensions.ViperChangeEvent#getIndexes()
	 */
	public int[] getIndexes() {
		return new int[]{getIndex()};
	}

	/**
	 * @see viper.api.extensions.ViperUndoableEvent#getUndoable()
	 */
	public ViperUndoableEvent.Undoable getUndoable() {
		return (ViperUndoableEvent.Undoable) getEdit();
	}
}
