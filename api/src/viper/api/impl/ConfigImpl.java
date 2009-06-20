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

import org.w3c.dom.*;

import viper.api.*;
import viper.api.Node;
import viper.api.extensions.*;

/**
 * Implements the Config interface of the viper.api,
 * including all standard extensions.
 * This means that it is editable, serializable to xml, 
 * and can take event listeners.
 */
public class ConfigImpl
	extends EventfulNodeHelper
	implements viper.api.Config, Cloneable, XmlVisibleNode, EventfulNode, Config.Edit {
	private String descName;
	private int descType;
	private List attrNodes;
	private Configs parent;
	void setParent(Configs parent) {
		this.parent = parent;
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
		return attrNodes.iterator();
	}
	/**
	 * @see viper.api.Node#getChild(int)
	 */
	public Node getChild(int i) {
		return (Node) attrNodes.get(i);
	}

	/**
	 * Constructs a new descriptor class definition, with the given 
	 * class name and type.
	 * @param name the name of the class
	 * @param type the descriptor type
	 */
	public ConfigImpl(String name, int type) {
		descType = type;
		descName = name;
		attrNodes = new LinkedList();
		childNodeType = "AttrConfig";
	}

	/**
	 * Returns a copy of this node, unconnected to the parent, but with
	 * copies of the attribute config nodes.
	 * @see java.lang.Object#clone()
	 */
	public Object clone() {
		ConfigImpl i = new ConfigImpl(descName, descType);
		i.attrNodes = (List) ((LinkedList) attrNodes).clone();
		Iterator iter = i.attrNodes.iterator();
		while (iter.hasNext()) {
			AttrConfigImpl ai = (AttrConfigImpl) iter.next();
			ai.parent = i;
		}
		return i;
	}

	/**
	 * @see viper.api.Config#getDescName()
	 */
	public String getDescName() {
		return descName;
	}

	/**
	 * @see viper.api.Config#getDescType()
	 */
	public int getDescType() {
		return descType;
	}

	/**
	 * @see Config#getAttrConfigs()
	 * @deprecated
	 */
	public Collection getAttrConfigs() {
		return attrNodes;
	}

	/**
	 * Checks to see that this descriptor class definition
	 * describes the same type of descriptor objects as the
	 * referenced object.
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	public boolean equals(Object o) {
		if (this == o) {
			return true;
		} else if (o instanceof Config) {
			Config c = (Config) o;
			if ((c.getDescType() == getDescType())
				&& c.getDescName().equals(getDescName())) {
				if (c.getNumberOfChildren() != this.getNumberOfChildren()) {
					return false;
				} else {
					for (Iterator i = this.getAttributeConfigs();
						i.hasNext();
						) {
						if (!c.hasChild((AttrConfig) i.next())) {
							return false;
						}
					}
					return true;
				}
			} else {
				return false;
			}
		} else {
			return false;
		}
	}
	/**
	 * Since there can only be one of a given
	 * type/name pair, this only returns the hash of 
	 * the name and type. So if you have a hash table
	 * that uses multiple Config objects with the
	 * same name and type, you might want to subclass
	 * this to xor the result with
	 * <code>getAttrConfigs().hashCode()</code>.
	 * @return <code>getDescType() ^ getDescName().hashCode()</code>
	 */
	public int hashCode() {
		return getDescType() ^ getDescName().hashCode();
	}
	/**
	 * Returns a old school gtf representation of the descriptor config.
	 * @see java.lang.Object#toString()
	 */
	public String toString() {
		return Util.getDescType(getDescType()) + " " + getDescName();
	}

	/**
	 * @see viper.api.Config#hasAttrConfig(java.lang.String)
	 */
	public boolean hasAttrConfig(String name) {
		for (Iterator iter = attrNodes.iterator(); iter.hasNext();) {
			AttrConfig currNode = (AttrConfig) iter.next();
			if (currNode.getAttrName().equals(name)) {
				return true;
			}
		}
		return false;
	}

	/**
	 * Gets the index of the given config.
	 * @param ac the config for check for
	 * @return the index
	 * @throws UnknownAttributeTypeException if the config isn't found
	 */
	public int getAttrConfigIndex(AttrConfig ac) {
		Iterator iter = attrNodes.iterator();
		int i = 0;
		while (iter.hasNext()) {
			if (iter.next().equals(ac)) {
				return i++;
			}
		}
		throw new UnknownAttributeTypeException(
			"Unknown attribute for " + this +": " + ac);
	}

	/**
	 * @see viper.api.Config#getAttrConfig(java.lang.String)
	 */
	public AttrConfig getAttrConfig(String name) {
		Iterator iter = attrNodes.iterator();

		while (iter.hasNext()) {
			AttrConfig currNode = (AttrConfig) iter.next();
			if (currNode.getAttrName().equals(name))
				return currNode;
		}

		return null;
	}

	/**
	 * Note that this will not check the case of the type or
	 * the name, or check for spaces.
	 * @see Config#createAttrConfig(String, String, boolean, Object, AttrValueWrapper)
	 */
	public AttrConfig createAttrConfig(
		String name,
		String type,
		boolean dynamic,
		Object def,
		AttrValueWrapper params)
		throws IllegalArgumentException {
		AttrConfig temp =
			new AttrConfigImpl(this, name, type, dynamic, def, params);
		addChild(temp);
		return temp;
	}

	/**
	 * @see viper.api.Config.Edit#setDescName(java.lang.String)
	 */
	public void setDescName(String str) {
		int i = getParent().indexOf(this);
		UndoableDescNameChangeEvent e =
			new UndoableDescNameChangeEvent(this, descName, str, i);
		descName = str;
		fireMinorNodeChanged(e);
	}
	public static class DescNameChangeEvent extends AbstractMinorChange {
		private String oldName;
		private String newName;

		DescNameChangeEvent(
			ConfigImpl src,
			String oldName,
			String newName,
			int index) {
			super.index = index;
			super.localName = "DescNameChange";
			super.source = src;

			this.oldName = oldName;
			this.newName = newName;
		}
		DescNameChangeEvent(DescNameChangeEvent old) {
			this((ConfigImpl) old.source, old.oldName, old.newName, old.index);
		}

		/**
		 * Gets the new name for the class of descriptors.
		 * @return the new name
		 */
		public String getNewName() {
			return newName;
		}

		/**
		 * Gets the old name for the class of descriptors.
		 * @return the old name
		 */
		public String getOldName() {
			return oldName;
		}
	}

	public static class DescNameChangeUndoable
		implements ViperUndoableEvent.Undoable {
		private DescNameChangeEvent e;
		DescNameChangeUndoable(DescNameChangeEvent e) {
			this.e = e;
		}
		/**
		 * Gets the old name for the class of descriptors.
		 * @return the old name
		 */
		public String getOldName() {
			return e.getOldName();
		}
		/**
		 * Gets the new name for the class of descriptors.
		 * @return the new name
		 */
		public String getNewName() {
			return e.getNewName();
		}
		/**
		 * @see viper.api.extensions.ViperUndoableEvent.Undoable#die()
		 */
		public void die() {
			e.source = null;
			e = null;
		}
		/**
		 * @see viper.api.extensions.ViperUndoableEvent.Undoable#undo()
		 */
		public void undo() {
			if (e != null) {
				ConfigImpl ci = (ConfigImpl) e.getSource();
				ci.descName = getOldName();
				DescNameChangeEvent ue =
					new DescNameChangeEvent(
						ci,
						getNewName(),
						getOldName(),
						e.getIndexes()[0]);
				ci.fireMinorNodeChanged(ue);
			}
		}
		/**
		 * @see viper.api.extensions.ViperUndoableEvent.Undoable#redo()
		 */
		public void redo() {
			if (e != null) {
				ConfigImpl ci = (ConfigImpl) e.getSource();
				ci.descName = getNewName();
				ci.fireMinorNodeChanged(new DescNameChangeEvent(e));
			}
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
	}

	public static class UndoableDescNameChangeEvent
		extends DescNameChangeEvent
		implements ViperUndoableEvent {
		UndoableDescNameChangeEvent(
			ConfigImpl src,
			String oldName,
			String newName,
			int index) {
			super(src, oldName, newName, index);
		}

		/**
		 * @see viper.api.extensions.ViperUndoableEvent#getUndoable()
		 */
		public ViperUndoableEvent.Undoable getUndoable() {
			return new DescNameChangeUndoable(this);
		}
	}

	/**
	 * @see viper.api.Config.Edit#setDescType(int)
	 */
	public void setDescType(int i) throws IllegalArgumentException {
		Util.getDescType(i); // throws an exception if not valid.
		int oldType = descType;
		descType = i;
		if (!Util.validConfig(this)) {
			descType = oldType;
			throw new IllegalArgumentException("Cannot convert this instance");
		}
		int j = getParent().indexOf(this);
		fireMinorNodeChanged(
			new UndoableDescTypeChangeEvent(this, oldType, i, j));
	}
	public static class DescTypeChangeEvent extends AbstractMinorChange {
		private int oldType;
		private int newType;
		DescTypeChangeEvent(
			ConfigImpl src,
			int oldType,
			int newType,
			int index) {
			super.index = index;
			super.localName = "DescTypeChange";
			super.source = src;

			this.oldType = oldType;
			this.newType = newType;
		}
		/**
		 * Gets the new descriptor type.
		 * @return the new descriptor type, e.g. {@link Config#CONTENT}
		 */
		public int getNewType() {
			return newType;
		}

		/**
		 * Gets the old descriptor type.
		 * @return the old descriptor type, e.g. {@link Config#CONTENT}
		 */
		public int getOldType() {
			return oldType;
		}
	}
	public static class DescTypeChangeUndoable
		implements ViperUndoableEvent.Undoable {
		private UndoableDescTypeChangeEvent e;
		DescTypeChangeUndoable(UndoableDescTypeChangeEvent e) {
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
			if (e != null) {
				ConfigImpl ci = (ConfigImpl) e.getSource();
				ci.descType = e.getOldType();
				DescTypeChangeEvent ue =
					new DescTypeChangeEvent(
						(ConfigImpl) e.getSource(),
						e.getNewType(),
						e.getOldType(),
						e.getIndexes()[0]);
				ci.fireMinorNodeChanged(ue);
			}
		}
		/**
		 * @see viper.api.extensions.ViperUndoableEvent.Undoable#redo()
		 */
		public void redo() {
			if (e != null) {
				ConfigImpl ci = (ConfigImpl) e.getSource();
				ci.descType = e.getNewType();
				DescTypeChangeEvent ue =
					new DescTypeChangeEvent(
						(ConfigImpl) e.getSource(),
						e.getOldType(),
						e.getNewType(),
						e.getIndexes()[0]);
				ci.fireMinorNodeChanged(ue);
			}
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
		/**
		 * Gets the old descriptor type.
		 * @return the old descriptor type, e.g. {@link Config#CONTENT}
		 */
		public String getOldType() {
			return Util.getDescType(e.getOldType());
		}
		/**
		 * Gets the new descriptor type.
		 * @return the new descriptor type, e.g. {@link Config#CONTENT}
		 */
		public String getNewType() {
			return Util.getDescType(e.getNewType());
		}
	}
	public static class UndoableDescTypeChangeEvent
		extends DescTypeChangeEvent
		implements ViperUndoableEvent {
		UndoableDescTypeChangeEvent(
			ConfigImpl src,
			int oldType,
			int newType,
			int index) {
			super(src, oldType, newType, index);
		}

		/**
		 * @see viper.api.extensions.ViperUndoableEvent#getUndoable()
		 */
		public ViperUndoableEvent.Undoable getUndoable() {
			return new DescTypeChangeUndoable(this);
		}
	}

	/**
	 * Old skool print config; exports the data in the old gtf format.
	 * @param pw where to put the data
	 * @throws java.io.IOException if there is a problem while writing to the 
	 * printwriter
	 */
	public void printConfig(PrintWriter pw) throws java.io.IOException {
		pw.println(Util.getDescType(descType) + " " + descName);
		Iterator i = attrNodes.iterator();
		while (i.hasNext()) {
			AttrConfig ac = (AttrConfig) i.next();
			pw.println("\t" + ac.getAttrName() + ": " + ac.getAttrType());
		}
	}

	/**
	 * @see viper.api.extensions.XmlVisibleNode#getXMLFormat(org.w3c.dom.Document)
	 */
	public Element getXMLFormat(Document root) {
		Element el = root.createElement("descriptor");
		el.setAttribute("name", getDescName());
		el.setAttribute("type", Util.getDescType(this.descType));
		for (Iterator iter = this.getAttrConfigs().iterator();
			iter.hasNext();
			) {
			Node curr = (Node) iter.next();
			if (curr instanceof XmlVisibleNode) {
				el.appendChild(((XmlVisibleNode) curr).getXMLFormat(root));
			}
		}
		return el;
	}

	

	protected void helpSetChild(int i, Node n, TransactionalNode.Transaction t, boolean insert) {
		boolean undoable = t != null;
		AttrConfigImpl ac = (AttrConfigImpl) n;
		if (n == null) {
			helpRemoveAttrConfig(i, undoable);
		} else if (i <= this.attrNodes.size()) {
			if (!insert) {
				helpRemoveAttrConfig(i, undoable);
			}
			ac.parent = this;
			attrNodes.add(i, ac);
			// modifying descriptors is handled in the postHelpSet step
		} else {
			throw new IndexOutOfBoundsException("Node index not found: " + i);
		}
	}
	protected void postHelpSetChild(int i, Node n, TransactionalNode.Transaction t, boolean insert) {
		boolean undoable = t != null;
		AttrConfigImpl ac = (AttrConfigImpl) n;
		if (n == null) {
		} else if (i == this.attrNodes.size()-1) {
			if (undoable) {
				Iterator iter = Util.getAllInstancesOf(this);
				while(iter.hasNext()) {
					DescriptorImpl d = (DescriptorImpl) iter.next();
					Attribute a = new AttributeImpl(d, ac, ac.getDefaultVal());
					d.addChild(i, a, undoable);
				}
			}
		}
	}
	private void helpRemoveAttrConfig(int i, boolean undoable) {
		if (undoable) {
			Iterator iter = Util.getAllInstancesOf(this);
			while(iter.hasNext()) {
				DescriptorImpl d = (DescriptorImpl) iter.next();
				d.removeChild(i, undoable);
			}
		}
		AttrConfigImpl aci = (AttrConfigImpl) attrNodes.remove(i);
		aci.parent = null;
	}

	/**
	 * @see viper.api.Config#getAttributeConfigs()
	 */
	public Iterator getAttributeConfigs() {
		return this.attrNodes.iterator();
	}
	/**
	 * @see viper.api.Node#getNumberOfChildren()
	 */
	public int getNumberOfChildren() {
		return this.attrNodes.size();
	}
	/**
	 * @see viper.api.Node#hasChild(viper.api.Node)
	 */
	public boolean hasChild(Node n) {
		return this.attrNodes.contains(n);
	}

	/**
	 * @see viper.api.Node#indexOf(viper.api.Node)
	 */
	public int indexOf(Node n) {
		return attrNodes.indexOf(n);
	}

	/**
	 * @see viper.api.Config#getEditor()
	 */
	public Config.Edit getEditor() {
		return this;
	}
	protected Logger getLogger() {
		return ((EventfulNodeHelper) parent).getLogger();
	}
}
