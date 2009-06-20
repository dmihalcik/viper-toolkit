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

import org.apache.commons.collections.*;
import org.w3c.dom.*;

import viper.api.*;
import viper.api.Node;
import viper.api.extensions.*;
import viper.api.time.*;
import edu.umd.cfar.lamp.viper.util.*;

/**
 * An implemenation of the 'Attribute' viper api node.
 */
public class AttributeImpl
	extends EventfulNodeHelper
	implements viper.api.Attribute, XmlVisibleNode, EventfulNode {
	private AttrConfig attribConf;
	Object attrValue;
	private DescriptorImpl parent;

	protected Logger getLogger() {
		return ((EventfulNodeHelper) parent).getLogger();
	}
	/**
	 * @see viper.api.Node#getParent()
	 */
	public Node getParent() {
		return parent;
	}
	/**
	 * @see viper.api.Node#getChildren()
	 */
	public Iterator getChildren() {
		return Collections.EMPTY_LIST.iterator();
	}
	/**
	 * @see viper.api.Node#getChild(int)
	 */
	public Node getChild(int i) {
		throw new IndexOutOfBoundsException("Attributes have no children");
	}

	/**
	 * Tests to see that the given object 
	 * represents the same value and schema as this.
	 * @param o the object to compare with
	 * @return <code>true</code> when the referenced object describes the same
	 * value at the same frames with an equivalent schema node
	 */
	public boolean equals(Object o) {
		if (this == o) {
			return true;
		} else if (o instanceof Attribute) {
			Attribute that = (Attribute) o;
			if (!that.getAttrConfig().equals(this.getAttrConfig())) {
				return false;
			} else if (
				getAttrConfig().isDynamic()
					&& that.getAttrConfig().isDynamic()) {
				Iterator thisIter = this.iterator();
				Iterator thatIter = that.iterator();
				if (!thisIter.hasNext() && !thatIter.hasNext()) {
					return true;
				} else {
					boolean good = true;
					boolean more = thisIter.hasNext() && thatIter.hasNext();
					while (good && more) {
						DynamicAttributeValue thisVal =
							(DynamicAttributeValue) thisIter.next();
						DynamicAttributeValue thatVal =
							(DynamicAttributeValue) thatIter.next();
						good = thisVal.equals(thatVal);
						more = thisIter.hasNext() && thatIter.hasNext();
					}
					return good
						&& !more
						&& !thisIter.hasNext()
						&& !thatIter.hasNext();
				}
			} else if (
				!getAttrConfig().isDynamic()
					&& !that.getAttrConfig().isDynamic()) {
				if (getAttrValue() == null) {
					return that.getAttrValue() == null;
				} else {
					return getAttrValue().equals(that.getAttrValue());
				}
			} else {
				return false;
			}
		} else {
			return false;
		}
	}

	/**
	 * Gets a hash integer for this attribute value.
	 * @return <code>AttrConfig.hashCode() ^ getAttrValue().hashCode()</code>
	 */
	public int hashCode() {
		if (parent != null) {
			return attribConf.hashCode() ^ parent.hashCode();
		} else {
			return attribConf.hashCode();
		}
	}

	/**
	 * Constructs a new attribute node, with the given descriptor as parent
	 * and attribute config as schema specifier. The value may be necessary for non-nillable nodes.
	 * @param parent the parent descriptor
	 * @param attrCfg the referring schema node
	 * @param value the value of the attribute. A dynamic attribute may take
	 * a lengthwise encoded value list directly.
	 */
	public AttributeImpl(
		DescriptorImpl parent,
		AttrConfig attrCfg,
		Object value) {
		this.parent = parent;
		this.attribConf = attrCfg;
		if (!this.attribConf.isDynamic()) {
			this.attrValue = attrCfg.getParams().setAttributeValue(value, this);
		} else if (parent != null) {
			Iterator spans = parent.getRange().iterator();
			while (spans.hasNext()) {
				helpSetAttrValueAtSpan(value, (InstantInterval) spans.next());
			}
		}
	}

	/**
	 * @see viper.api.Attribute#getAttrValue()
	 */
	public Object getAttrValue() {
		return attribConf.getParams().getObjectValue(getInternalAttrValue(), this, null);
	}
	
	/**
	 * Gets the internal value of the attribute.
	 * @return the internal value
	 * @throws NotStaticException when calling on a dynamic attribute
	 */
	Object getInternalAttrValue() {
		if (attribConf.isDynamic()) {
			throw new NotStaticException();
		} else {
			return attrValue;
		}
	}

	/**
	 * @see viper.api.Attribute#getAttrName()
	 */
	public String getAttrName() {
		return (attribConf.getAttrName());
	}

	/**
	 * Get the value of the attribute at the specified frame or time. Note that
	 * this returns <code>null</code> if frame is out of bounds; it does not
	 * throw an <code>IndexOutOfBoundsException</code>. Also, note further
	 * that it relies on the Sourcefile knowing the FrameRate to convert
	 * between nanoseconds and frames.
	 * @param i the instant to get the value
	 * @return the value at the given instant
	 */
	public Object getAttrValueAtInstant(Instant i) {
		if (attrValue == null) {
			return null;
		} else if (attribConf.isDynamic()) {
			assert attrValue instanceof TimeEncodedList;
			TimeEncodedList L = (TimeEncodedList) attrValue;
			MediaElement rm = ((Sourcefile) parent.getParent()).getReferenceMedia();
			FrameRate rate = rm.getFrameRate();
			if (null == L) {
				return null;
			} else if (L.isFrameBased()) {
				i = rate.asFrame(i);
			} else if (L.isTimeBased()) {
				i = rate.asTime(i);
			} else {
				return null;
			}
			return attribConf.getParams().getObjectValue(L.get(i), this, i);
		} else {
			return attribConf.getParams().getObjectValue(attrValue, this, i);
		}
	}

	/**
	 * @see viper.api.Attribute#getAttrValuesOverSpan(viper.api.time.InstantInterval)
	 */
	public Iterator getAttrValuesOverSpan(InstantInterval s) {
		return new MappingIterator(DECODE, getInternalAttrValuesOverSpan(s));
	}
	Iterator getInternalAttrValuesOverSpan(InstantInterval s) {
		if (attrValue == null) {
			return Collections.EMPTY_LIST.iterator();
		} else if (getAttrConfig().isDynamic()) {
			assert attrValue instanceof TimeEncodedList;
			TimeEncodedList v = (TimeEncodedList) attrValue;
			return v.iterator(s);
		} else {
			// is static
			InstantRange v = parent.getValidRange();
			IntervalIndexList L = v.subList(s.getStart(), s.getEnd());
			return new StaticIterator(L.iterator());
		}
	}
	
	/**
	 * @see viper.api.Attribute#getAttrValuesOverWholeRange()
	 */
	public Iterator getAttrValuesOverWholeRange() {
		return new MappingIterator(DECODE, getInternalAttrValuesOverWholeRange());
	}
	Iterator getInternalAttrValuesOverWholeRange() {
		if (attrValue == null) {
			return Collections.EMPTY_LIST.iterator();
		} else if (getAttrConfig().isDynamic()) {
			assert attrValue instanceof TimeEncodedList;
			// FIXME: Only iterate over valid parts of dynamic attribute values
			return ((TimeEncodedList) attrValue).iterator();
		} else {
			// is static
			return new StaticIterator(parent.getValidRange().iterator());
		}
	}

	/**
	 * @see viper.api.Attribute#iterator()
	 */
	public Iterator iterator() {
		return getAttrValuesOverWholeRange();
	}
	private MappingIterator.MappingFunctor DECODE = new MappingIterator.MappingFunctor() {
		public Object map(Object o) {
			DynamicAttributeValue v = (DynamicAttributeValue) o;
			Object encoded = v.getValue();
			Object decoded = attribConf.getParams().getObjectValue(encoded, AttributeImpl.this, null);
			if (decoded != encoded) {
				v = new TemporalObject(v.getStartInstant(), v.getEndInstant(), decoded);
			}
			return v;
		}
	};
	
	private class ConvertParams implements Transformer {
		private AttrValueWrapper oldParams;
		private AttrValueWrapper newParams;
		ConvertParams(AttrValueWrapper oldParams, AttrValueWrapper newParams) {
			this.oldParams = oldParams;
			this.newParams = newParams;
		}

		/**
		 * Tries to convert from one attribute data type to another.
		 * @see edu.umd.cfar.lamp.viper.util.IntervalIndexList.Change#change(java.lang.Object)
		 */
		public Object transform(Object value) {
			return AttrConfigImpl.tryToConvertAttrValue(value, oldParams, newParams, AttributeImpl.this);
		}
		
	}
	
	/**
	 * Converts the param object, which controls the attribute value.
	 * @param oldParams the old parameter object
	 * @param newParams the new parameter object
	 */
	public void changeParams (AttrValueWrapper oldParams, AttrValueWrapper newParams) {
		Object newVal = null;
		if (attrValue != null) {
			if (getAttrConfig().isDynamic()) {
				TimeEncodedList tel = (TimeEncodedList) attrValue;
				tel = (TimeEncodedList) tel.clone();
				tel.map(new ConvertParams(oldParams, newParams));
				newVal = tel;
			} else {
				newVal = AttrConfigImpl.tryToConvertAttrValue(attrValue, oldParams, newParams, AttributeImpl.this);
			}
			helpSetAttrValue(newVal, true);
		}
	}
	
	/**
	 * Change the dynamic value of the attribute. This should only be called by the
	 * attribute config changing.
	 * @param newState the new state
	 */
	public void changeDynamic (boolean newState) {
		Object newVal = null;
		if (attrValue != null) {
			if (newState) {
				TimeEncodedList tel = new TimeEncodedList();
				Iterator iter = getDescriptor().getValidRange().iterator();
				while (iter.hasNext()) {
					Interval ii = (Interval) iter.next();
					tel.set(ii, attrValue);
				}
				newVal = tel;
			} else {
				TimeEncodedList tel = (TimeEncodedList) attrValue;
				newVal = tel.get(tel.getExtrema().getStart());
			}
			helpSetAttrValue(newVal, true);
		}
	}

	/**
	 * @see viper.api.Attribute#getDescriptor()
	 */
	public Descriptor getDescriptor() {
		return parent;
	}

	/**
	 * @see viper.api.Attribute#getAttrConfig()
	 */
	public AttrConfig getAttrConfig() {
		return attribConf;
	}

	/**
	 * reparents the node
	 * @param d the new parent
	 */
	public void setParent(DescriptorImpl d) {
		parent = d;
	}

	/**
	 * Finishes the change
	 * @param v The encoded form
	 * @param undoable
	 */
	void helpSetAttrValue(Object v, boolean undoable) {
		Object oldValue = attrValue;
		if (attribConf.isDynamic() && !(v instanceof TimeEncodedList)) {
//			throw new NotStaticException(
//				"Attribute is not static: " + attribConf);
		}
		boolean unchanged = true;
		
		if (v == null) {
			unchanged = (attrValue == null);
		} else {
			unchanged = v.equals(attrValue);
		}
		if (unchanged) {
			return;
		}
		boolean bubble = parent instanceof EventfulNode;
		boolean report = hasListeners();
		this.attrValue = v;
		if (report || bubble) {
			MinorNodeChangeEvent mnce;
			if (undoable) {
				mnce = new UndoableStaticAttributeChangedEvent(v, oldValue);
			} else {
				mnce = new StaticAttributeChangedEvent(v, oldValue);
			}
			this.fireMinorNodeChanged(mnce);
		}
	}

	void setAttrValueDontUndo(Object v) {
		helpSetAttrValue(getAttrConfig().getParams().setAttributeValue(v, this), false);
	}

	/**
	 * @see viper.api.Attribute#setAttrValue(java.lang.Object)
	 */
	public void setAttrValue(Object v) {
		setInternalAttrValue(getAttrConfig().getParams().setAttributeValue(v, this));
	}
	void setInternalAttrValue(Object v) {
		if (isWriteLocked()) {
			throw new IllegalStateException("Cannot set attribute value while attribute is write locked");
		}
		helpSetAttrValue(v, true);
	}

	/**
	 * nb: this currently does not look at the containing descriptor's 'range'
	 * at all. So adding values outside of the range will work, but it won't
	 * change the descriptor's 'framespan' attribute. In similar vein, deleting
	 * all dynamic attributes at a given span will not alter the valid range of
	 * the descriptor as a whole.
	 * @see Attribute#setAttrValueAtSpan(Object, InstantInterval)
	 * @param v the value to set
	 * @param span the interval over which to set the value
	 * @throws UnknownFrameRateException if the interval isn't specified in the 
	 * same form (either Time or Frame) as the attribute and the frame rate 
	 * is unknown
	 * @throws BadAttributeDataException if the value wrapper
	 * indicates this (e.g. not the correct data type; not a valid lvalue)
	 */
	public void setAttrValueAtSpan(Object v, InstantInterval span)
		throws UnknownFrameRateException, BadAttributeDataException {
		// First, check to see if value is different
		if (isWriteLocked()) {
			throw new IllegalStateException("Cannot set attribute value " + v + "@" + span + " while attribute is write locked");
		}
		boolean changed = false;
		Iterator oldIter = getAttrValuesOverSpan(span);
		if (v != null) {
			changed = true;
			if (oldIter.hasNext()) {
				DynamicAttributeValue oldV = (DynamicAttributeValue) oldIter.next();
				if (!oldIter.hasNext() && oldV.contains(span) && oldV.getValue().equals(v)) {
					changed = false;
				}
			}
		} else { // v == null
			changed = oldIter.hasNext();
		}
		if (changed) {
			helpSetAttrValueAtSpan(true, v, span);
			parent.notifyChangeOverRange(span);
		}
	}

	private void helpSetAttrValueAtSpan(Object v, InstantInterval span) {
		helpSetAttrValueAtSpan(false, v, span);
	}
	private void helpSetAttrValueAtSpan(
		boolean undoable,
		Object v,
		InstantInterval span) {
		TimeEncodedList old = null;
		if (attrValue == null && v != null) {
			attrValue = new TimeEncodedList();
		} else if (attrValue == null && v == null) {
			return;
		}

		old = (TimeEncodedList) ((TimeEncodedList) attrValue).clone();
		DynamicAttributeChangedEvent e;
		if (undoable) {
			e = new UndoableDynamicAttributeChangedEvent(span, v, old);
		} else {
			e = new DynamicAttributeChangedEvent(span, v, old);
		}
		if(v instanceof TimeEncodedList)
			quietSetAttrValueAtSpan(v, span);
		else
			quietSetAttrValueAtSpan(getAttrConfig().getParams().setAttributeValue(v, this), span);
		fireMinorNodeChanged(e);
	}
	private void quietSetAttrValueAtSpan(Object internalVal, InstantInterval span) {
		TimeEncodedList tel = (TimeEncodedList) attrValue;
		MediaElement rm = ((Sourcefile) parent.getParent()).getReferenceMedia();
		if (span.isTimeBased() && tel.isFrameBased()) {
			FrameRate fr = rm.getFrameRate();
			if (fr == null) {
				throw new UnknownFrameRateException();
			}
			span = fr.asFrame(span);
		} else if (span.isFrameBased() && tel.isTimeBased()) {
			FrameRate fr = rm.getFrameRate();
			if (fr == null) {
				throw new UnknownFrameRateException();
			}
			span = fr.asTime(span);
		}
		if(internalVal instanceof TimeEncodedList){
			TimeEncodedList vals = (TimeEncodedList)internalVal;
			assert vals.getExtrema().equals(span);
			tel.remove(span.getStart(), span.getEnd());
			tel.addAll(vals);			
		}else{
			tel.set(span, internalVal);
		}
	}

	/**
	 * @see viper.api.extensions.XmlVisibleNode#getXMLFormat(org.w3c.dom.Document)
	 */
	public Element getXMLFormat(Document root) {
		Element el = root.createElement("attribute");
		el.setAttribute("name", getAttrConfig().getAttrName());
		if (attrValue == null) {
			String qualifier = "data";
			String uri = qualifier;
			// FIXME figure out how to do this right
			Element valEl = root.createElementNS(uri, qualifier + ":null");
			el.appendChild(valEl);
		} else if (attribConf.getParams() instanceof AttrValueParser) {
			el.appendChild(
				((AttrValueParser) attribConf.getParams()).getXMLFormat(
					root,
					attrValue, null));
		}
		return el;
	}

	public class StaticAttributeChangedEvent extends AbstractMinorChange {
		private Object newVal;
		private Object oldVal;
		StaticAttributeChangedEvent(
			Object newVal,
			Object oldVal) {
			this.newVal = newVal;
			this.oldVal = oldVal;
			super.localName = "AttrValueChange";
			super.source = AttributeImpl.this;
			super.index =
				AttributeImpl.this.getParent().indexOf(AttributeImpl.this);
		}
		/**
		 * The value of the attribute after the change.
		 * @return the new value
		 */
		public Object getNewVal() {
			return newVal;
		}
		/**
		 * The value of the attribute before the change.
		 * @return the old value
		 */
		public Object getOldVal() {
			return oldVal;
		}
	}
	public class UndoableStaticAttributeChangedEvent extends StaticAttributeChangedEvent
	implements ViperUndoableEvent {
		private ViperUndoableEvent.Undoable u;
		UndoableStaticAttributeChangedEvent(Object newVal, Object oldVal) {
			super(newVal, oldVal);
			u = new StaticValueUpdateUndoable(this);
		}
		/**
		 * @see viper.api.extensions.ViperUndoableEvent#getUndoable()
		 */
		public ViperUndoableEvent.Undoable getUndoable() {
			return u;
		}
	}

	public class DynamicAttributeChangedEvent extends AbstractMinorChange {
		private InstantInterval changed;
		private Object newVal;
		private TimeEncodedList oldVal;
		DynamicAttributeChangedEvent(
			InstantInterval changed,
			Object newVal,
			TimeEncodedList oldVal) {
			this.changed = changed;
			this.newVal = newVal;
			this.oldVal = oldVal;
			super.localName = "AttrValueChange";
			super.source = AttributeImpl.this;
			super.index =
				AttributeImpl.this.getParent().indexOf(AttributeImpl.this);
		}
		/**
		 * Gets the interval over which the change occurred.
		 * @return the changed interval
		 */
		public InstantInterval getChangedSpan() {
			return changed;
		}
		/**
		 * The value of the attribute after the change.
		 * @return the new value
		 */
		public Object getNewVal() {
			return newVal;
		}

		/**
		 * The value of the attribute before the change.
		 * @return the old value
		 */
		public TimeEncodedList getOldVal() {
			return oldVal;
		}

	}

	public static class DynamicValueUpdateUndoable
		implements ViperUndoableEvent.Undoable {
		private DynamicAttributeChangedEvent e;
		DynamicValueUpdateUndoable(DynamicAttributeChangedEvent e) {
			this.e = e;
		}
		/**
		 * @see viper.api.extensions.ViperUndoableEvent.Undoable#die()
		 */
		public void die() {
			e = null;
		}

		/**
		 * @see viper.api.extensions.ViperUndoableEvent.Undoable#undo()
		 */
		public void undo() {
			AttributeImpl attr = (AttributeImpl) e.getSource();
			TimeEncodedList tel = e.getOldVal();
			attr.attrValue = tel.clone();
			attr.fireMinorNodeChanged(e);
		}

		/**
		 * @see viper.api.extensions.ViperUndoableEvent.Undoable#redo()
		 */
		public void redo() {
			Object v = e.getNewVal();
			InstantInterval span = e.getChangedSpan();
			AttributeImpl attr = (AttributeImpl) e.getSource();
			attr.helpSetAttrValueAtSpan(v, span);
		}

		/**
		 * @see viper.api.extensions.ViperUndoableEvent.Undoable#canUndo()
		 */
		public boolean canUndo() {
			return e != null;
		}

		/**
		 * @see viper.api.extensions.ViperUndoableEvent.Undoable#canRedo()
		 */
		public boolean canRedo() {
			return e != null;
		}
		public InstantInterval getChangedSpan() {
			return e.getChangedSpan();
		}
		public Object getNewVal() {
			return e.getNewVal();
		}
		public TimeEncodedList getOldVal() {
			return e.getOldVal();
		}
		public Object getProperty(String prop) {
			return e.getProperty(prop);
		}
	}
	public static class StaticValueUpdateUndoable
		implements ViperUndoableEvent.Undoable {
		private StaticAttributeChangedEvent e;
		StaticValueUpdateUndoable(StaticAttributeChangedEvent e) {
			this.e = e;
		}
		/**
		 * @see viper.api.extensions.ViperUndoableEvent.Undoable#die()
		 */
		public void die() {
			e = null;
		}

		/**
		 * @see viper.api.extensions.ViperUndoableEvent.Undoable#undo()
		 */
		public void undo() {
			Object v = e.getOldVal();
			AttributeImpl attr = (AttributeImpl) e.getSource();
			attr.helpSetAttrValue(v, false);
		}

		/**
		 * @see viper.api.extensions.ViperUndoableEvent.Undoable#redo()
		 */
		public void redo() {
			Object v = e.getNewVal();
			AttributeImpl attr = (AttributeImpl) e.getSource();
			attr.helpSetAttrValue(v, false);
		}

		/**
		 * @see viper.api.extensions.ViperUndoableEvent.Undoable#canUndo()
		 */
		public boolean canUndo() {
			return e != null;
		}

		/**
		 * @see viper.api.extensions.ViperUndoableEvent.Undoable#canRedo()
		 */
		public boolean canRedo() {
			return e != null;
		}
		public Object getNewVal() {
			return e.getNewVal();
		}
		public Object getOldVal() {
			return e.getOldVal();
		}
		public Object getProperty(String prop) {
			return e.getProperty(prop);
		}
	}

	public class UndoableDynamicAttributeChangedEvent
		extends DynamicAttributeChangedEvent
		implements ViperUndoableEvent {
		private ViperUndoableEvent.Undoable u = null;

		/**
		 * @see viper.api.extensions.ViperUndoableEvent#getUndoable()
		 */
		public ViperUndoableEvent.Undoable getUndoable() {
			if (u == null) {
				u = new DynamicValueUpdateUndoable(new DynamicAttributeChangedEvent(getChangedSpan(), getNewVal(), getOldVal()));
			}
			return u;
		}

		/**
		 * @param changed
		 * @param newVal
		 * @param oldVal
		 */
		public UndoableDynamicAttributeChangedEvent(
			InstantInterval changed,
			Object newVal,
			TimeEncodedList oldVal) {
			super(changed, newVal, oldVal);
		}
	}

	private class StaticIterator implements Iterator {
		private Iterator wrapped;
		StaticIterator(Iterator spans) {
			wrapped = spans;
		}
		/**
		 * @see java.util.Iterator#hasNext()
		 */
		public boolean hasNext() {
			return wrapped.hasNext();
		}
		/**
		 * @see java.util.Iterator#next()
		 */
		public Object next() {
			return new TimeEncodedList.DynamicAttributeValueImpl(
				(Span) wrapped.next(),
				attrValue);
		}
		/**
		 * @see java.util.Iterator#remove()
		 */
		public void remove() {
			throw new UnsupportedOperationException();
		}

	}

	/**
	 * @see viper.api.TemporalNode#getRange()
	 */
	public TemporalRange getRange() {
		if (this.getAttrConfig().isDynamic()) {
			return (TimeEncodedList) attrValue;
		} else {
			return null;
		}
	}
	protected void helpSetChild(int i, Node n, TransactionalNode.Transaction t, boolean insert) {
		throw new UnsupportedOperationException();
	}
	protected void postHelpSetChild(int i, Node n, TransactionalNode.Transaction t, boolean insert) {
		throw new UnsupportedOperationException();
	}

	/**
	 * @see viper.api.TemporalNode#setRange(viper.api.time.TemporalRange)
	 */
	public void setRange(TemporalRange r) {
		if (attribConf.isDynamic() && attrValue instanceof TemporalRange) {
			TemporalRange ar = (TemporalRange) attrValue;
			Iterator toRemove = Intervals.complement(r);
			while (toRemove.hasNext()) {
				Interval ii = (Interval) toRemove.next();
				ar.remove(ii.getStart(), ii.getEnd());
			}
		}
	}

	/**
	 * @see viper.api.Node#getNumberOfChildren()
	 */
	public int getNumberOfChildren() {
		return 0;
	}
	
	/**
	 * @see viper.api.Node#hasChild(viper.api.Node)
	 */
	public boolean hasChild(Node n) {
		return false;
	}
	
	/**
	 * @see viper.api.Node#indexOf(viper.api.Node)
	 */
	public int indexOf(Node n) {
		return -1;
	}
	
	/**
	 * Gets the attribute in the old gtf format.
	 * @return attrName : attrValue
	 */
	public String toString() {
		String valStr;
		if (attrValue == null) {
			valStr = "NULL";
		} else if (attribConf.isDynamic()) {
			Iterator iter = getAttrValuesOverWholeRange();
			valStr = "";
			while (iter.hasNext()) {
				DynamicAttributeValue curr = (DynamicAttributeValue) iter.next();
				valStr += curr.toString();
				if (iter.hasNext()) {
					valStr += ", ";
				}
			}
		} else {
			valStr = getAttrValue().toString();
			valStr = '"' + StringHelp.backslashify(valStr) + '"';
		}
		return attribConf.getAttrName() + " : " + valStr;
	}

	private boolean aggregating = false;
	private TimeEncodedList aggregateOldValue;
	private InstantInterval aggregateInterval;
	
	/* (non-Javadoc)
	 * @see viper.api.Attribute#startAggregating()
	 */
	public void startAggregating(){
		if(aggregating)
			throw new IllegalStateException();
		aggregating = true;
		aggregateOldValue = (TimeEncodedList) ((TimeEncodedList) attrValue).clone();
		aggregateInterval = Span.EMPTY_FRAME_SPAN;
	}
	
	/* (non-Javadoc)
	 * @see viper.api.Attribute#aggregateSetAttrValueAtSpan(java.lang.Object, viper.api.time.InstantInterval)
	 */
	public void aggregateSetAttrValueAtSpan(Object v, InstantInterval span)
	throws UnknownFrameRateException, BadAttributeDataException {
		if(!aggregating)
			throw new IllegalStateException();
		
		// First, check to see if value is different
		if (isWriteLocked()) {
			throw new IllegalStateException("Cannot set attribute value " + v + "@" + span + " while attribute is write locked");
		}
		boolean changed = false;
		Iterator oldIter = getAttrValuesOverSpan(span);
		if (v != null) {
			changed = true;
			if (oldIter.hasNext()) {
				DynamicAttributeValue oldV = (DynamicAttributeValue) oldIter.next();
				if (!oldIter.hasNext() && oldV.contains(span) && oldV.getValue().equals(v)) {
					changed = false;
				}
			}
		} else { // v == null
			changed = oldIter.hasNext();
		}
		if (changed) {
			aggregateHelpSetAttrValueAtSpan(v, span);
		}
	}
	
	private void aggregateHelpSetAttrValueAtSpan(Object v, InstantInterval span) {
		if (attrValue == null && v != null) {
			attrValue = new TimeEncodedList();
		} else if (attrValue == null && v == null) {
			return;
		}
		
		quietSetAttrValueAtSpan(getAttrConfig().getParams().setAttributeValue(v, this), span);

		//Adjust the interval changed over
		if(aggregateInterval == Span.EMPTY_FRAME_SPAN)
			aggregateInterval = span;
		else{
			Instant start1, start2, end1, end2, start, end;
			start1 = aggregateInterval.getStartInstant();
			start2 = span.getStartInstant();
			end1 = aggregateInterval.getEndInstant();
			end2 = aggregateInterval.getEndInstant();
			if(start1.compareTo(start2) < 0)
				start = start1;
			else
				start = start2;
			
			if(end1.compareTo(end2) < 0)
				end = end2;
			else
				end = end1;
			
			aggregateInterval = new Span(start, end);
		}
	}
	
	/* (non-Javadoc)
	 * @see viper.api.Attribute#finishAggregating(boolean)
	 */
	public void finishAggregating(boolean undoable){
		if(!aggregating)
			throw new IllegalStateException();

		if(undoable){
			DynamicAttributeChangedEvent e;
			e = new UndoableDynamicAttributeChangedEvent(aggregateInterval, attrValue, aggregateOldValue);
			fireMinorNodeChanged(e);
		}
		
		aggregating = false;
	}
}
