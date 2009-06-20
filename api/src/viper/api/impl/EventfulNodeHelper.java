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
import java.util.logging.*;

import javax.swing.event.*;

import viper.api.*;
import viper.api.extensions.*;
/**
 * An abstract viper node class.
 * @author davidm
 */
public abstract class EventfulNodeHelper implements TransactionalNode {
	protected String childNodeType;
	private long lastModified;
	private EventListenerList listeners = new EventListenerList();
	private HiddenEventTransaction trans = null;
	private static final Logger logger = Logger.getLogger("viper.api.impl");
	/**
	 * Tests to see if any listeners are attached to the current node.
	 * @return <code>false</code> when no listeners are attached
	 */
	public boolean hasListeners() {
		return listeners.getListenerCount() > 0;
	}
	/**
	 * @see viper.api.extensions.EventfulNode#addNodeListener(viper.api.extensions.NodeListener)
	 */
	public void addNodeListener(NodeListener nl) {
		listeners.add(NodeListener.class, nl);
	}
	/**
	 * @see viper.api.extensions.EventfulNode#removeNodeListener(viper.api.extensions.NodeListener)
	 */
	public void removeNodeListener(NodeListener nl) {
		listeners.remove(NodeListener.class, nl);
	}
	/**
	 * @see viper.api.Node#getRoot()
	 */
	public ViperData getRoot() {
		if (this instanceof ViperData) {
			return (ViperData) this;
		} else if (this.getParent() == null) {
			return null;
		} else {
			return getParent().getRoot();
		}
	}
	protected abstract Logger getLogger();
	private boolean checkEvent(ViperChangeEvent vce) {
		if (trans != null) {
			trans.addEvent(vce);
			return true;
		}
		return false;
	}
	protected boolean notifyingListeners = false;
	/**
	 * @see viper.api.extensions.EventfulNode#fireNodeChanged(viper.api.extensions.NodeChangeEvent)
	 */
	public void fireNodeChanged(NodeChangeEvent nce) {
		if (isWriteLocked()) {
			throw new IllegalStateException("Cannot fire new node change while still processing old one");
		}
		notifyingListeners = true;
		try {
			setLastModifiedTime(System.currentTimeMillis());
			if (!checkEvent(nce)) {
				Object[] listeners = this.listeners.getListenerList();
				for (int i = listeners.length - 2; i >= 0; i -= 2) {
					if (listeners[i] == NodeListener.class) {
						((NodeListener) listeners[i + 1]).nodeChanged(nce);
					}
				}
				if (getParent() instanceof EventfulNode) {
					((EventfulNode) getParent()).fireNodeChanged(nce);
				}
			}
		} finally {
			notifyingListeners = false;
		}
	}
	
	/**
	 * Tests to see if set will work.
	 * @return
	 */
	public boolean isWriteLocked() {
		if (notifyingListeners)
			return true;
		Node parent = getParent();
		if (parent instanceof EventfulNodeHelper) {
			return ((EventfulNodeHelper) parent).isWriteLocked();
		}
		return false;
	}
	/**
	 * @see viper.api.extensions.EventfulNode#fireMinorNodeChanged(viper.api.extensions.MinorNodeChangeEvent)
	 */
	public void fireMinorNodeChanged(MinorNodeChangeEvent mnce) {
		if (notifyingListeners) {
			throw new IllegalStateException("Cannot fire new node change while still processing old one");
		}
		notifyingListeners = true;
		try {
			setLastModifiedTime(System.currentTimeMillis());
			if (!checkEvent(mnce)) {
				Object[] listeners = this.listeners.getListenerList();
				for (int i = listeners.length - 2; i >= 0; i -= 2) {
					if (listeners[i] == NodeListener.class) {
						((NodeListener) listeners[i + 1])
								.minorNodeChanged(mnce);
					}
				}
				if (getParent() instanceof EventfulNode) {
					((EventfulNode) getParent()).fireMinorNodeChanged(mnce);
				}
			}
		} finally {
			notifyingListeners = false;
		}
	}
	/**
	 * @see viper.api.extensions.EventfulNode#fireMajorNodeChanged(viper.api.extensions.MajorNodeChangeEvent)
	 */
	public void fireMajorNodeChanged(MajorNodeChangeEvent mnce) {
		if (isWriteLocked()) {
			throw new IllegalStateException("Cannot fire new node change while still processing old one");
		}
		notifyingListeners = true;
		try {
			setLastModifiedTime(System.currentTimeMillis());
			if (!checkEvent(mnce)) {
				Object[] listeners = this.listeners.getListenerList();
				for (int i = listeners.length - 2; i >= 0; i -= 2) {
					if (listeners[i] == NodeListener.class) {
						((NodeListener) listeners[i + 1])
								.majorNodeChanged(mnce);
					}
				}
				if (getParent() instanceof EventfulNode) {
					((EventfulNode) getParent()).fireMajorNodeChanged(mnce);
				}
			}
		} finally {
			notifyingListeners = false;
		}
	}
	/**
	 * Replace the child at index i with the given node. If insert is set, makes
	 * the child at index i the given node and shifts i to i+1, i+1 to i+2, and
	 * so on. This should not generate an event.
	 * 
	 * @param i
	 *            the index to insert/set
	 * @param n
	 *            the node to insert
	 * @param t
	 *            the transaction associated with the event, if it exists.
	 *            Otherwise, it is <code>null</code>.
	 * @param insert
	 *            <code>true</code> if the node should be inserted,
	 *            <code>false</code> if the current node i should be replaced
	 */
	protected abstract void helpSetChild(int i, Node n, TransactionalNode.Transaction t,
			boolean insert);
	/**
	 * Invoked after the event has been generated for the corresponding
	 * helpSetChild call. This is useful if you need to order your events. This
	 * should not generate an event.
	 * 
	 * @param i
	 *            the index to insert/set
	 * @param n
	 *            the node to insert
	 * @param t
	 *            the transaction associated with the event, if it exists.
	 *            Otherwise, it is <code>null</code>.
	 * @param insert
	 *            <code>true</code> if the node should be inserted,
	 *            <code>false</code> if the current node i should be replaced
	 */
	protected abstract void postHelpSetChild(int i, Node n, TransactionalNode.Transaction t,
			boolean insert);
	/**
	 * @see viper.api.Node#addChild(viper.api.Node)
	 */
	public void addChild(Node n) {
		assert !isWriteLocked();
		addChild(n, true);
	}
	/**
	 * @see viper.api.Node#setChild(int, viper.api.Node)
	 */
	public void setChild(int i, Node n) {
		assert !isWriteLocked();
		setChild(i, n, true);
	}
	/**
	 * @see viper.api.Node#removeChild(int)
	 */
	public void removeChild(int i) {
		assert !isWriteLocked();
		removeChild(i, true);
	}
	/**
	 * @see viper.api.Node#removeChild(viper.api.Node)
	 */
	public void removeChild(Node n) {
		assert !isWriteLocked();
		removeChild(n, true);
	}
	/**
	 * Adds a child node, but returns an undoable or non-undoable
	 * event according to the parameter.
	 * @param n the node to add
	 * @param undoable whether to allow undo
	 * @see viper.api.Node#addChild(viper.api.Node)
	 */
	public void addChild(Node n, boolean undoable) {
		int idx = getNumberOfChildren();
		addChild(idx, n, undoable);
	}
	void addChild(int i, Node n, boolean undoable) {
		String lname = "Add" + childNodeType;
		String fname = ViperParser.IMPL + lname;
		TransactionalNode.Transaction t = null;
		if (undoable && getRoot() instanceof TransactionalNode) {
			t = ((TransactionalNode) getRoot()).begin(fname);
		}
		try {
			helpSetChild(i, n, t, true);
			NodeChangeEvent nce;
			if (undoable) {
				nce = new UndoableNodeChangeEventImpl(this, null, n, -1, i,
						lname);
			} else {
				nce = new InternalNodeChangeEvent(this, null, n, i, lname);
			}
			fireNodeChanged(nce);
			postHelpSetChild(i, n, t, true);
			if (t != null) {
				t.commit();
			}
		} catch (UnsupportedOperationException uox) {
			uox.fillInStackTrace();
			throw uox;
		} finally {
			if (t != null && t.isAlive()) {
				t.rollback();
			}
		}
	}
	void setChild(int i, Node n, boolean undoable) {
		String lname = "Set" + childNodeType;
		String fname = ViperParser.IMPL + lname;
		TransactionalNode.Transaction t = null;
		if (undoable && getRoot() instanceof TransactionalNode) {
			t = ((TransactionalNode) getRoot()).begin(fname);
		}
		try {
			Node o = null;
			if (i >= 0 && i < getNumberOfChildren()) {
				o = getChild(i);
			}
			helpSetChild(i, n, t, false);
			NodeChangeEvent nce;
			if (undoable) {
				nce = new UndoableNodeChangeEventImpl(this, o, n, i, i, lname);
			} else {
				nce = new InternalNodeChangeEvent(this, o, n, i, lname);
			}
			fireNodeChanged(nce);
			postHelpSetChild(i, n, t, false);
			if (t != null) {
				t.commit();
			}
		} catch (UnsupportedOperationException uox) {
			uox.fillInStackTrace();
			throw uox;
		} finally {
			if (t != null && t.isAlive()) {
				t.rollback();
			}
		}
	}
	/**
	 * Removes the specified child, emitting an undoable or
	 * non-undoable version of the event as requested.
	 * @param i the index of the child to remove
	 * @param undoable whether to allow undoing the event
	 */
	public void removeChild(int i, boolean undoable) {
		String lname = "Remove" + childNodeType;
		String fname = ViperParser.IMPL + lname;
		TransactionalNode.Transaction t = null;
		if (undoable && getRoot() instanceof TransactionalNode) {
			t = ((TransactionalNode) getRoot()).begin(fname);
		}
		try {
			Node o = getChild(i);
			helpSetChild(i, null, t, false);
			NodeChangeEvent nce;
			if (undoable) {
				nce = new UndoableNodeChangeEventImpl(this, o, null, i, -1,
						lname);
			} else {
				nce = new InternalNodeChangeEvent(this, o, null, i, lname);
			}
			fireNodeChanged(nce);
			postHelpSetChild(i, null, t, false);
			if (t != null) {
				t.commit();
			}
		} catch (UnsupportedOperationException uox) {
			uox.fillInStackTrace();
			throw uox;
		} catch (RuntimeException rx) {
			logger.log(Level.SEVERE, "Exception while removing child", rx);
			throw rx;
		} catch (AssertionError ae) {
			logger.log(Level.SEVERE, "Error while removing child", ae);
			throw ae;
		} finally {
			if (t != null && t.isAlive()) {
				t.rollback();
			}
		}
	}
	void removeChild(Node n, boolean undoable) {
		int i = indexOf(n);
		if (i < 0) {
			throw new IllegalArgumentException(n.toString());
		}
		removeChild(i, undoable);
	}
	/**
	 * @see viper.api.extensions.TransactionalNode#begin(java.lang.String)
	 */
	public TransactionalNode.Transaction begin(String uri) {
		trans = new EventTransaction(uri, (EventTransaction) trans);
		return trans;
	}
	public class HiddenEventTransaction implements TransactionalNode.Transaction {
		protected CombinableHiddenEvent me;
		protected boolean rollingBack = false;
		protected HiddenEventTransaction parent;
		protected HiddenEventTransaction child;
		HiddenEventTransaction(HiddenEventTransaction parent) {
			this(ViperParser.IMPL + "HiddenTransaction", parent);
		}
		HiddenEventTransaction(String uri, HiddenEventTransaction parent) {
			this.child = null;
			this.me = new CombinableHiddenEvent(uri);
			this.parent = parent;
		}
		void addChild(EventTransaction c) {
			if (child != null && child.isAlive()) {
				throw new IllegalStateException(
						"Cannot add new child transaction while previous is still alive");
			}
			this.child = c;
		}
		/**
		 * @see viper.api.extensions.TransactionalNode.Transaction#putProperty(java.lang.String, java.lang.Object)
		 */
		public void putProperty(String property, Object value) {
			me.addProperty(property, value);
		}
		/**
		 * @see viper.api.extensions.TransactionalNode.Transaction#commit()
		 */
		public void commit() {
			if (child != null && child.isAlive()) {
				child.commit();
			}
			trans = trans.parent;
			if (!me.events.isEmpty()) {
				if (parent == null) {
					fireMajorNodeChanged(me);
				} else {
					parent.addEvent(me);
				}
			}
			this.me = null;
			this.child = null;
			this.parent = null;
		}
		/**
		 * @see viper.api.extensions.TransactionalNode.Transaction#rollback()
		 */
		public void rollback() {
			rollingBack = true;
			if (child != null && child.isAlive()) {
				child.rollback();
			}
			getLogger().warning("Cannot rollback hidden transaction");
			this.me = null;
			this.child = null;
			this.parent = null;
			rollingBack = false;
		}
		protected void addEvent(ViperChangeEvent vce) {
			if (!isAlive()) {
				throw new IllegalStateException();
			} else {
				me.addEvent(vce);
			}
		}
		/**
		 * @see viper.api.extensions.TransactionalNode.Transaction#soFar()
		 */
		public ViperChangeEvent[] soFar() {
			List events = me.events;
			ViperChangeEvent[] soFar = new ViperChangeEvent[events.size()];
			return (ViperChangeEvent[]) events.toArray(soFar);
		}
		/**
		 * @see viper.api.extensions.TransactionalNode.Transaction#isAlive()
		 */
		public boolean isAlive() {
			return me != null;
		}
	}
	public class EventTransaction extends HiddenEventTransaction {
		EventTransaction(String uri, EventTransaction parent) {
			super(uri, parent);
			this.rollingBack = parent != null && parent.rollingBack;
			this.me = new CombinableUndoEvent(uri);
		}
		/**
		 * @see viper.api.impl.EventfulNodeHelper.HiddenEventTransaction#rollback()
		 */
		public void rollback() {
			rollingBack = true;
			if (child != null && child.isAlive()) {
				child.rollback();
			}
			((CombinableUndoEvent) this.me).undo();
			this.me = null;
			this.child = null;
			this.parent = null;
			rollingBack = false;
		}
		protected void addEvent(ViperChangeEvent vce) {
			if (rollingBack) {
				getLogger().finer(
						"Event captured during rollback: " + me.getUri());
			} else if (!isAlive()) {
				throw new IllegalStateException();
			} else if (vce instanceof ViperUndoableEvent) {
				me.addEvent(vce);
			} else {
				getLogger().severe(
						"ERROR: Non-undoable event occured during transaction "
								+ me.getUri() + ": " + vce);
			}
		}
	}
	public class CombinableHiddenEvent extends AbstractMajorNodeChangeEvent {
		CombinableHiddenEvent(String uri) {
			super(uri);
		}
		/**
		 * @see viper.api.extensions.ViperChangeEvent#getParent()
		 */
		public Node getParent() {
			return EventfulNodeHelper.this.getParent();
		}
		/**
		 * @see viper.api.extensions.ViperChangeEvent#getSource()
		 */
		public Object getSource() {
			return EventfulNodeHelper.this;
		}
		/**
		 * @see viper.api.extensions.ViperChangeEvent#getIndexes()
		 */
		public int[] getIndexes() {
			int i = EventfulNodeHelper.this.getParent().indexOf(
					EventfulNodeHelper.this);
			return new int[]{i};
		}
	}
	public class CombinableUndoEvent extends CombinableHiddenEvent
			implements
				ViperUndoableEvent,
				ViperUndoableEvent.Undoable {
		CombinableUndoEvent(String uri) {
			super(uri);
		}
		/**
		 * @see viper.api.extensions.ViperUndoableEvent#getUndoable()
		 */
		public ViperUndoableEvent.Undoable getUndoable() {
			return this;
		}
		/**
		 * @see viper.api.extensions.ViperUndoableEvent.Undoable#undo()
		 */
		public void undo() {
			boolean rb = trans != null && trans.rollingBack;
			trans = new HiddenEventTransaction(trans);
			trans.rollingBack = rb;
			ListIterator iter = events.listIterator(events.size());
			while (iter.hasPrevious()) {
				ViperUndoableEvent curr = (ViperUndoableEvent) iter.previous();
				curr.getUndoable().undo();
			}
			trans.commit();
		}
		/**
		 * @see viper.api.extensions.ViperUndoableEvent.Undoable#redo()
		 */
		public void redo() {
			trans = new HiddenEventTransaction(trans);
			Iterator iter = events.iterator();
			while (iter.hasNext()) {
				ViperUndoableEvent curr = (ViperUndoableEvent) iter.next();
				curr.getUndoable().redo();
			}
			trans.commit();
		}
		/**
		 * @see viper.api.extensions.ViperUndoableEvent.Undoable#canUndo()
		 */
		public boolean canUndo() {
			return events != null;
		}
		/**
		 * @see viper.api.extensions.ViperUndoableEvent.Undoable#canRedo()
		 */
		public boolean canRedo() {
			return events != null;
		}
	}
	/** @inheritDoc */
	public boolean isNotifyingListeners() {
		return notifyingListeners || ((getParent() instanceof EventfulNode) && ((EventfulNode) getParent()).isNotifyingListeners());
	}
	
	protected void setLastModifiedTime(long lastModified) {
		if (lastModified < this.lastModified) {
			throw new IllegalStateException("Cannot reverse time on lastModified timestamp");
		}
		if (lastModified == this.lastModified) {
			return;
		}
		this.lastModified = lastModified;
		EventfulNodeHelper parent = (EventfulNodeHelper) this.getParent();
		if (parent != null && parent.getLastModifiedTime() < lastModified) {
			parent.setLastModifiedTime(lastModified);
		}
	}
	
	public long getLastModifiedTime() {
		return lastModified;
	}
}