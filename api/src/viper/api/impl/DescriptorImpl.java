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

import java.io.*;
import java.util.*;
import java.util.logging.*;

import org.apache.commons.lang.*;
import org.w3c.dom.*;

import viper.api.*;
import viper.api.Node;
import viper.api.extensions.*;
import viper.api.time.*;

/**
 * An implemenation of the viper api's descriptor node interface.
 */
public class DescriptorImpl
	extends EventfulNodeHelper
	implements Descriptor, XmlVisibleNode, EventfulNode {
	private ConfigImpl cfg;
	private int descId = -1;
	private InstantRange validRange;
	private List attributes;

	private InstantRange interpOverRange;
	
	// Note that parents and listeners are always explicitly avoided
	// when checking for equality.
	private Sourcefile parent;

	/**
	 * @see viper.api.Descriptor#getSourcefile()
	 */
	public Sourcefile getSourcefile() {
		return parent;
	}
	/**
	 * @see viper.api.Node#getParent()
	 */
	public Node getParent() {
		return parent;
	}
	void setParent(Sourcefile p) {
		parent = p;
	}
	/**
	 * @see viper.api.Node#getChildren()
	 */
	public Iterator getChildren() {
		return attributes.iterator();
	}

	/**
	 * Tests to see if this descriptor is the same as the given object, except for 
	 * their parents.
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	public boolean equals(Object o) {
		if (this == o) {
			return true;
		} else if (o instanceof Descriptor) {
			Descriptor that = (Descriptor) o;
			if (that.getConfig().equals(cfg) && (that.getDescId() == descId)) {
				if (cfg.getDescType() != Config.FILE) {
					if (validRange == null
						? that.getValidRange() != null
						: !validRange.equals(that.getValidRange())) {
						return false;
					}
				}
				Iterator a = this.getAttributes();
				Iterator b = that.getAttributes();
				while (a.hasNext() && b.hasNext()) {
					if (!a.next().equals(b.next())) {
						return false;
					}
				}
				return a.hasNext() == b.hasNext();
			} else {
				return false;
			}
		} else {
			return false;
		}
	}
	/**
	 * Since the config and id together act as a primary key,
	 * this just returns the xor of the descId with the 
	 * Config's hash code.
	 * @return <code>cfg.hashCode() ^ descId</code>
	 */
	public int hashCode() {
		return cfg.hashCode() ^ descId;
	}

	/**
	 * Checks to see if the preferred instant type is {@link Frame},
	 * instead of {@link Time}.
	 * @return <code>true</code> if the validity range is in frames
	 */
	public boolean isFrameBased() {
		return validRange.isFrameBased();
	}

	private DescriptorImpl() {
		super.childNodeType = "Attribute";
		attributes = new LinkedList();
		validRange = new InstantRange();
		interpOverRange = new InstantRange();
	}

	/**
	 * @see java.lang.Object#toString()
	 */
	public String toString() {
		return cfg.toString() + " " + getDescId();
	}

	/**
	 * Constructs a new instance of the given descriptor class attached to the 
	 * given sourcefile.
	 * @param parent the sourcefile that the descriptor describes
	 * @param descType the class of descriptor
	 * @param id a numeric id
	 */
	public DescriptorImpl(Sourcefile parent, ConfigImpl descType, int id) {
		super.childNodeType = "Attribute";
		this.parent = parent;
		cfg = descType;
		descId = id;
		attributes = new LinkedList();
		validRange = new InstantRange();
		interpOverRange = new InstantRange();
		reset();
	}

	private void reset() {
		attributes.clear();
		validRange.clear();
		interpOverRange.clear();
		for (Iterator iter = cfg.getAttributeConfigs();
			iter.hasNext();
			) {
			AttrConfig curr = (AttrConfig) iter.next();
			attributes.add(new AttributeImpl(this, curr, curr.getDefaultVal()));
		}
	}

	/**
	 * Changes the descriptor class.
	 * @param descType the new class
	 * @throws IllegalArgumentException if this descriptor isn't a valid instance of the
	 * new schema
	 */
	public void setDescType(ConfigImpl descType)
		throws IllegalArgumentException {
		ConfigImpl oldCfg = cfg;
		cfg = descType;
		if (!Util.validInstance(this)) {
			cfg = oldCfg;
			throw new IllegalArgumentException("Cannot convert this instance");
		}
	}

	/**
	 * @param bwt
	 */
	public void printDescType(BufferedWriter bwt) {

		try {
			if (cfg.getDescType() == Config.FILE)
				bwt.write("FILE");
			else if (cfg.getDescType() == Config.OBJECT)
				bwt.write("OBJECT");
			else if (cfg.getDescType() == Config.CONTENT)
				bwt.write("CONTENT");

			bwt.newLine();
		} catch (IOException et) {
			System.err.println("IOException: " + et.getMessage());
			et.printStackTrace();
			System.exit(-1);
		}
	}

	/**
	 * Sets the descriptor id without generating an event.
	 * @param new_id the new id
	 */
	public void setDescId(int new_id) {
		descId = new_id;
	}

	/**
	 * @see viper.api.Descriptor#setValidRange(viper.api.time.InstantRange)
	 */
	public void setValidRange(InstantRange fs) {
		setValidRange((TemporalRange) fs);
	}
	private void helpSetValidRange(TemporalRange fs) {
		if (fs.isFrameBased() == !validRange.isTimeBased()) {
			validRange.clear();
			validRange.addAll(fs);
		} else if (fs.isTimeBased() == !validRange.isFrameBased()) {
			validRange.clear();
			validRange.addAll(fs);
		} else {
			validRange.clear();
			MediaElement rm =
				((Sourcefile) parent.getParent()).getReferenceMedia();
			FrameRate fr = rm.getFrameRate();
			if (fs.isFrameBased()) {
				for (Iterator iter = fs.iterator(); iter.hasNext();) {
					validRange.add(fr.asTime((Span) iter.next()));
				}
			} else {
				for (Iterator iter = fs.iterator(); iter.hasNext();) {
					validRange.add(fr.asFrame((Span) iter.next()));
				}
			}
		}
	}
	private void quietSetValidRange(TemporalRange fs) {
		TemporalRange oldRange = (TemporalRange) validRange.clone();
		int index = this.parent.indexOf(this);
		helpSetValidRange(fs);
		MinorNodeChangeEvent e =
			new ValidRangeChangeEvent(
				this,
				oldRange,
				(TemporalRange) validRange.clone(),
				index);
		this.fireMinorNodeChanged(e);
	}

	/**
	 * Sets the frames over which this is valid.
	 * @param fs the valid frames
	 */
	public void setValidRange(TemporalRange fs) {
		if (fs == null) {
			fs = new InstantRange();
		}
		if (!ObjectUtils.equals(validRange, fs)) {
			TemporalRange oldRange = null;
			if(!isAggregating){
				oldRange = (TemporalRange) validRange.clone();
			}
			helpSetValidRange(fs);
			if (this.parent != null && !isAggregating) {
				int index = this.parent.indexOf(this);
				MinorNodeChangeEvent e =
					new UndoableValidRangeChangeEvent(
						this,
						oldRange,
						(TemporalRange) validRange.clone(),
						index);
				this.fireMinorNodeChanged(e);
			}
		}
	}
	public static class ValidRangeChangeEvent extends AbstractMinorChange {
		private TemporalRange oldRange;
		private TemporalRange newRange;

		ValidRangeChangeEvent(
			DescriptorImpl src,
			TemporalRange oldRange,
			TemporalRange newRange,
			int index) {
			super.index = index;
			super.localName = "DescriptorValidRangeChange";
			super.source = src;

			this.oldRange = oldRange;
			this.newRange = newRange;
		}

		/**
		 * Gets the range over which the descriptor is valid after the change.
		 * @return the new validity range
		 */
		public TemporalRange getNewRange() {
			return newRange;
		}

		/**
		 * Gets the range over which the descriptor was valid before the change.
		 * @return the old validity range
		 */
		public TemporalRange getOldRange() {
			return oldRange;
		}
	}
	public class UndoableValidRangeChangeEvent
		extends ValidRangeChangeEvent
		implements ViperUndoableEvent {

		/**
		 * Creates a new event describing a change of the 
		 * validity region
		 * @param src the source descriptor
		 * @param oldRange the old range
		 * @param newRange the new range
		 * @param index the index of the descriptor
		 */
		public UndoableValidRangeChangeEvent(
			DescriptorImpl src,
			TemporalRange oldRange,
			TemporalRange newRange,
			int index) {
			super(src, oldRange, newRange, index);
		}

		/**
		 * @see viper.api.extensions.ViperUndoableEvent#getUndoable()
		 */
		public ViperUndoableEvent.Undoable getUndoable() {
			return new ValidRangeChangeUndoable(this);
		}
	}
	public class ValidRangeChangeUndoable
		implements ViperUndoableEvent.Undoable {
		UndoableValidRangeChangeEvent event;

		/**
		 * Constructs a new undoable edit for the given
		 * validity range change event.
		 * @param event the corresponding event
		 */
		public ValidRangeChangeUndoable(UndoableValidRangeChangeEvent event) {
			this.event = event;
		}

		/**
		 * @see viper.api.extensions.ViperUndoableEvent.Undoable#die()
		 */
		public void die() {
			event = null;
		}

		/**
		 * @see viper.api.extensions.ViperUndoableEvent.Undoable#undo()
		 */
		public void undo() {
			quietSetValidRange(event.getOldRange());
		}

		/**
		 * @see viper.api.extensions.ViperUndoableEvent.Undoable#redo()
		 */
		public void redo() {
			quietSetValidRange(event.getNewRange());
		}

		/**
		 * @see viper.api.extensions.ViperUndoableEvent.Undoable#canUndo()
		 */
		public boolean canUndo() {
			return event != null;
		}

		/**
		 * @see viper.api.extensions.ViperUndoableEvent.Undoable#canRedo()
		 */
		public boolean canRedo() {
			return event != null;
		}
	}

	protected void helpSetChild(int i, Node n, TransactionalNode.Transaction t, boolean insert) {
		AttributeImpl a = (AttributeImpl) n;
		if (n == null) {
			a = (AttributeImpl) attributes.remove(i);
			a.setParent(null);
		} else {
			if (!cfg.getChild(i).equals(a.getAttrConfig())){
				throw new IllegalArgumentException("Not a valid attribute");
			}
			a.setParent(this);
			if (!insert) {
				if (i < attributes.size()) {
					AttributeImpl ai = (AttributeImpl) attributes.get(i);
					if (ai != null) {
						ai.setParent(null);
					}
				}
				attributes.set(i, a);
			} else {
				attributes.add(i, n);
			}
		}
	}
	protected void postHelpSetChild(int i, Node n, TransactionalNode.Transaction t, boolean insert) {
	}

	/**
	 * @see viper.api.Descriptor#getDescName()
	 */
	public String getDescName() {
		return cfg.getDescName();
	}

	/**
	 * @see viper.api.Descriptor#getDescType()
	 */
	public int getDescType() {
		return cfg.getDescType();
	}

	/**
	 * @see viper.api.Descriptor#getDescId()
	 */
	public int getDescId() {
		return descId;
	}

	/**
	 * @see viper.api.Descriptor#getValidRange()
	 */
	public InstantRange getValidRange() {
		return (InstantRange) validRange.clone();
	}

	/**
	 * @see Descriptor#getAttrList()
	 * @deprecated
	 */
	public Collection getAttrList() {
		return attributes;
	}

	/**
	 * Gets the index of the given child attribute.
	 * @param a the attribute to index
	 * @return the index of the attribute
	 */
	public int getAttributeIndex(Attribute a) {
		return cfg.getAttrConfigIndex(a.getAttrConfig());
	}

	/**
	 * @see viper.api.Descriptor#getAttribute(viper.api.AttrConfig)
	 */
	public Attribute getAttribute(AttrConfig cfg) {
		for (Iterator iter = attributes.iterator(); iter.hasNext();) {
			Attribute curr = (Attribute) iter.next();
			if (curr.getAttrConfig().equals(cfg)) {
				return curr;
			}
		}
		return null; // FIXME should through no such element exception?
	}

	/**
	 * @see viper.api.Descriptor#getAttribute(java.lang.String)
	 */
	public Attribute getAttribute(String attribName) {
		String testName = attribName;

		Iterator iter = attributes.iterator();
		while (iter.hasNext()) {
			Attribute temp = (Attribute) iter.next();
			if (temp.getAttrName().equals(testName))
				return temp;
		}
		return null;
	}

	/**
	 * @see viper.api.Descriptor#getConfig()
	 */
	public Config getConfig() {
		return cfg;
	}

	/**
	 * Returns a copy of this node and the nodes beneath it, with no
	 * parent node.
	 * @see viper.api.Descriptor#clone()
	 */
	public Object clone() {

		DescriptorImpl temp = new DescriptorImpl();

		temp.cfg = (ConfigImpl) cfg.clone();

		temp.setDescId(getDescId());
		temp.attributes.addAll(getAttrList());

		temp.setValidRange(getValidRange());

		return temp;

	}


	void printDescriptor(BufferedWriter out) {

		try {
			out.write("Descriptor Name : " + cfg.getDescName());
			out.newLine();

			out.write("Descriptor Type : ");
			//+ descType );
			printDescType(out);
			out.newLine();

			out.write("Descriptor Id : " + descId);
			out.newLine();

			out.write("Timespan/Framespan info");
			out.newLine();
			Iterator iter = attributes.iterator();

			while (iter.hasNext()) {
				AttributeImpl attr = (AttributeImpl) iter.next();
				out.write(" Attribute: " + attr.toString());
				out.newLine();
			}

		} catch (IOException et) {
			System.err.println("IOException : " + et.getMessage());
			et.printStackTrace();
			System.exit(-1);
		}
	}

	/**
	 * @see viper.api.extensions.XmlVisibleNode#getXMLFormat(org.w3c.dom.Document)
	 */
	public Element getXMLFormat(Document root) {
		Element dEl =
			root.createElement(
				Util.getDescType(cfg.getDescType()).toLowerCase());
		dEl.setAttribute("name", cfg.getDescName());
		dEl.setAttribute("id", String.valueOf(this.getDescId()));
		if (this.getValidRange() != null) {
			if (this.getValidRange().isFrameBased()) {
				dEl.setAttribute("framespan", getValidRange().toString());
			} else if (this.getValidRange().isTimeBased()) {
				dEl.setAttribute("timespan", getValidRange().toString());
			}
		}

		for (Iterator iter = attributes.iterator(); iter.hasNext();) {
			Node curr = (Node) iter.next();
			if (curr instanceof XmlVisibleNode) {
				dEl.appendChild(((XmlVisibleNode) curr).getXMLFormat(root));
			}
		}
		return dEl;
	}

	/**
	 * Same as {@link #getValidRange}.
	 * @see viper.api.TemporalNode#getRange()
	 */
	public TemporalRange getRange() {
		return getValidRange();
	}
	/**
	 * Same as {@link #setValidRange(TemporalRange)}.
	 * @see viper.api.TemporalNode#setRange(TemporalRange)
	 */
	public void setRange(TemporalRange r) {
		this.setValidRange(r);
	}

	/**
	 * @see viper.api.Descriptor#getAttributes()
	 */
	public Iterator getAttributes() {
		return this.attributes.iterator();
	}

	/**
	 * @see viper.api.Node#getNumberOfChildren()
	 */
	public int getNumberOfChildren() {
		return this.attributes.size();
	}
	/**
	 * @see viper.api.Node#hasChild(viper.api.Node)
	 */
	public boolean hasChild(Node n) {
		return this.attributes.contains(n);
	}
	/**
	 * @see viper.api.Node#getChild(int)
	 */
	public Node getChild(int i) {
		return (Node) attributes.get(i);
	}
	/**
	 * @see viper.api.Node#indexOf(viper.api.Node)
	 */
	public int indexOf(Node n) {
		return this.attributes.indexOf(n);
	}
	protected Logger getLogger() {
		return ((EventfulNodeHelper) parent).getLogger();
	}
	
	/**
	 * Fixes the interpOverRange to ensure that it is a subset of the valid range
	 */
//	 private void adjustInterpRange(){
//		if(!validRange.containsAll(interpOverRange)){
//			interpOverRange = InstantRange.parseFrameRange(validRange.intersect(interpOverRange).toString());
//		}
//	}
	
	private boolean freezingInterp = false;
	
	/* (non-Javadoc)
	 * @see viper.api.Descriptor#setFreezingInterp(boolean)
	 */
	public void setFreezingInterp(boolean b){
		freezingInterp = b;
	}
	

	/* (non-Javadoc)
	 * @see viper.api.Descriptor#notifyChangeOverRange(viper.api.time.InstantRange)
	 */
	public void notifyChangeOverRange(InstantInterval range){
		if(!freezingInterp){
			//InstantRange changed = InstantRange.parseFrameRange(interpOverRange.intersect(range).toString());
			interpOverRange.remove(range);
			//adjustInterpRange();  //Can't decide if allowing the interp range to be a non-subset of valid makes sense or not
		}
	}
	
	/* (non-Javadoc)
	 * @see viper.api.Descriptor#getInterpolatedOverRange()
	 */
	public InstantRange getInterpolatedOverRange(){
		return (InstantRange)interpOverRange.clone();
	}
	
	/* (non-Javadoc)
	 * @see viper.api.Descriptor#setInterpolatedOverRange(viper.api.time.InstantRange)
	 */
	public void setInterpolatedOverRange(InstantRange range){
		interpOverRange = range;
	}

	private boolean isAggregating = false;
	private TemporalRange savedValidRange;
	private InstantRange savedInterpOverRange;
	
	public void startAggregating(){
		savedValidRange = (TemporalRange)validRange.clone();
		savedInterpOverRange = (InstantRange)interpOverRange.clone();
		isAggregating = true;
	}
	
	public void finishAggregating(boolean undoable){
		if (undoable && this.parent != null) {
			int index = this.parent.indexOf(this);
			MinorNodeChangeEvent e =
				new UndoableValidRangeAndInterpRangeChangeEvent(
					this,
					savedValidRange,
					(TemporalRange) validRange.clone(),
					index,
					savedInterpOverRange,
					(InstantRange)interpOverRange.clone());
			this.fireMinorNodeChanged(e);
		}
		isAggregating = false;
	}

	private class UndoableValidRangeAndInterpRangeChangeEvent extends UndoableValidRangeChangeEvent{
		InstantRange oldInterpRange;
		InstantRange newInterpRange;
		
		UndoableValidRangeAndInterpRangeChangeEvent(DescriptorImpl src,
		TemporalRange oldRange,
		TemporalRange newRange,
		int index,
		InstantRange oldInterpRange,
		InstantRange newInterpRange) {
			super(src, oldRange, newRange, index);
			this.oldInterpRange = oldInterpRange;
			this.newInterpRange = newInterpRange;
		}
		
		/**
		 * @see viper.api.extensions.ViperUndoableEvent#getUndoable()
		 */
		public ViperUndoableEvent.Undoable getUndoable() {
			return new ValidRangeAndInterpRangeChangeUndoable(this);
		}
	}
	
	public class ValidRangeAndInterpRangeChangeUndoable extends ValidRangeChangeUndoable{
		/**
		 * Constructs a new undoable edit for the given
		 * validity range change event.
		 * @param event the corresponding event
		 */
		public ValidRangeAndInterpRangeChangeUndoable(UndoableValidRangeAndInterpRangeChangeEvent event) {
			super(event);
		}
		
		/**
		 * @see viper.api.extensions.ViperUndoableEvent.Undoable#undo()
		 */
		public void undo() {
			interpOverRange.clear();
			interpOverRange.addAll((Collection)((UndoableValidRangeAndInterpRangeChangeEvent)event).oldInterpRange);
			super.undo();
			
		}

		/**
		 * @see viper.api.extensions.ViperUndoableEvent.Undoable#redo()
		 */
		public void redo() {
			interpOverRange.clear();
			interpOverRange.addAll((Collection)((UndoableValidRangeAndInterpRangeChangeEvent)event).newInterpRange);
			super.redo();
		}

	}
}
