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

import viper.api.*;
import viper.api.datatypes.*;
import viper.api.extensions.*;
import viper.api.time.*;
import edu.umd.cfar.lamp.viper.util.*;

/**
 * An implementation of the attribute schema node from the viper api.
 * This also implements all appropriate extension, for things
 * like eventfulness, editing, and serialization. This data type is pretty
 * much immutable, in that it should act as though it is immutable. In order
 * to do set type things, use the <code>cp</code> methods and change the 
 * parent.
 */
public class AttrConfigImpl extends EventfulNodeHelper implements
		viper.api.AttrConfig, EventfulNode, AttrConfig.Edit {
	private String attrName;

	private String attrType;

	private boolean isDynamic;

	private boolean nillable = true;

	private Object defaultVal;

	private AttrValueWrapper params;

	Config parent;

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
		throw new IndexOutOfBoundsException("AttrConfigs have no children");
	}

	/**
	 * Tests to see if this config is the same as the passed one.
	 * It doesn't check parent nodes, so this is useful when comparing accross
	 * trees or in the same tree.
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	public boolean equals(Object o) {
		if (this == o) {
			return true;
		} else if (o instanceof AttrConfig) {
			AttrConfig ac = (AttrConfig) o;
			return (ac.isDynamic() == isDynamic())
					&& ac.getAttrType().equals(getAttrType())
					&& ac.getAttrName().equals(getAttrName())
					&& ac.getParams().equals(getParams())
					&& (ac.getDefaultVal() == null ? (getDefaultVal() == null)
							: ac.getDefaultVal().equals(getDefaultVal()));
		} else {
			return false;
		}
	}

	/**
	 * Returns a hash based on the name, type,
	 * and extra information.
	 * @see java.lang.Object#hashCode()
	 */
	public int hashCode() {
		int h = attrName.hashCode() ^ attrType.hashCode();
		if (isDynamic()) {
			h = ~h;
		}
		if (params != null) {
			h ^= params.hashCode();
		}
		if (defaultVal != null) {
			h ^= defaultVal.hashCode();
		}
		return h;
	}

	/**
	 * Returns the old skool gtf format.
	 * @return <code><i>type</i>: <i>name</i> [static]?</code>
	 * @see java.lang.Object#toString()
	 */
	public String toString() {
		return XmlHelper.localName(attrType) + ": " + attrName
				+ (!isDynamic ? " [static]" : "");
	}

	/**
	 * Creates a new, simple, unnamed attribute declaration.
	 */
	public AttrConfigImpl() {
		isDynamic = false;
	}

	/**
	 * Constructs a new Attribute Configuration node.
	 * @param parent The parent Config(Impl) node
	 * @param name   The name of the attribute
	 * @param type   The attribute type, eg ViperData.ViPER_DATA_URI + "svalue" for strings.
	 * @param flag   Set to <code>true</code> to indicate a dynamic attribute
	 * @param def    The default value for the attribute
	 * @param params The prototype for the value, or additional parameters
	 */
	public AttrConfigImpl(Config parent, String name, String type,
			boolean flag, Object def, AttrValueWrapper params) {
		this.parent = parent;
		attrName = name;
		attrType = type;
		isDynamic = flag;
		if (params != null) {
			this.params = params;
			resetLinkedParent();
		} else {
			this.params = new InstanceOfConstraint();
		}
		defaultVal = this.params.setAttributeValue(def, this);
	}

	/**
	 * Updates the link to the current document root for relation-type attributes
	 */
	private void resetLinkedParent() {
		if (this.params instanceof LinkedAttrValueParser) {
			((LinkedAttrValueParser) this.params).setAttrConfig(this);
		}
	}

	/**
	 * @see viper.api.AttrConfig#getAttrName()
	 */
	public String getAttrName() {
		return attrName;
	}

	/**
	 * @see viper.api.AttrConfig#getAttrType()
	 */
	public String getAttrType() {
		return attrType;
	}

	/**
	 * @see viper.api.AttrConfig#isDynamic()
	 */
	public boolean isDynamic() {
		return isDynamic;
	}

	/**
	 * @see viper.api.AttrConfig#getDefaultVal()
	 */
	public Object getDefaultVal() {
		return defaultVal == null ? null : params.getObjectValue(defaultVal, this, null);
	}

	private AttrConfigImpl copy() {
		AttrConfigImpl data = new AttrConfigImpl();

		data.attrName = getAttrName();
		data.attrType = getAttrType();
		data.isDynamic = isDynamic();
		data.params = params;
		data.defaultVal = defaultVal;
		return data;
	}

	protected void helpSetChild(int i, Node n, TransactionalNode.Transaction t,
			boolean insert) {
		throw new UnsupportedOperationException();
	}

	protected void postHelpSetChild(int i, Node n,
			TransactionalNode.Transaction t, boolean insert) {
		throw new UnsupportedOperationException();
	}

	/**
	 * Change the parent of this, but, since it is immutable, returns the 
	 * new value.
	 * @param parent the new parent
	 * @return a copy of this, with the new parent
	 */
	public AttrConfig cpParent(Config parent) {
		if (parent == this.parent) {
			return this;
		} else {
			AttrConfigImpl cp = copy();
			cp.parent = parent;
			cp.resetLinkedParent();
			return cp;
		}
	}

	/**
	 * Returns a copy with the new attribute name.
	 * @param name The new attribute name.
	 * @return a copy, with the name changed
	 */
	public AttrConfig cpAttrName(String name) {
		if (name != this.attrName) {
			AttrConfigImpl cp = copy();
			cp.attrName = name;
			return cp;
		} else {
			return this;
		}
	}

	/**
	 * Changes the attribute type, by returning an altered copy.
	 * @param str the new type
	 * @return an altered copy of this attrconfig
	 */
	public AttrConfig cpAttrType(String str) {
		if (this.attrType != str) {
			AttrConfigImpl cp = copy();
			cp.attrType = str;
			return cp;
		} else {
			return this;
		}
	}

	/**
	 * Returns a new copy of the attribute config, with the change
	 * to the 'dynamic' property applied.
	 * @param val <code>true</code> indicates that instances may
	 * vary over time
	 * @return the new node
	 */
	public AttrConfig cpDynamic(boolean val) {
		if (this.isDynamic != val) {
			AttrConfigImpl cp = copy();
			cp.isDynamic = val;
			return cp;
		} else {
			return this;
		}
	}

	/**
	 * Returns a copy of this, with the default value changed.
	 * @param o the new default value
	 * @return a copy of <code>this</code>, with the new 
	 * default value
	 */
	public AttrConfig cpDefault(Object o) {
		if (this.defaultVal != o) {
			AttrConfigImpl cp = copy();
			this.defaultVal = this.params.setAttributeValue(o, cp);
			return cp;
		} else {
			return this;
		}
	}

	/**
	 * @see viper.api.AttrConfig#getParams()
	 */
	public AttrValueWrapper getParams() {
		return params;
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
	 * @see viper.api.AttrConfig#getEditor()
	 */
	public AttrConfig.Edit getEditor() {
		return this;
	}

	public static class AttrNameChangeEvent extends AbstractMinorChange {
		private String oldName;

		private String newName;

		AttrNameChangeEvent(AttrConfigImpl src, String oldName, String newName,
				int index) {
			super.index = index;
			super.localName = "AttrNameChange";
			super.source = src;

			this.oldName = oldName;
			this.newName = newName;
		}

		String getNewName() {
			return newName;
		}

		String getOldName() {
			return oldName;
		}
	}

	public static class AttrNameChangeUndoable implements ViperUndoableEvent.Undoable {
		private UndoableAttrNameChangeEvent e;

		AttrNameChangeUndoable(UndoableAttrNameChangeEvent e) {
			this.e = e;
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
			if (e != null && e.source != null) {
				AttrConfigImpl aci = (AttrConfigImpl) e.source;
				aci.attrName = getOldName();
				AttrNameChangeEvent ue = new AttrNameChangeEvent(
						(AttrConfigImpl) e.source, getNewName(), getOldName(),
						e.index);
				aci.fireMinorNodeChanged(ue);
			}
		}

		/**
		 * @see viper.api.extensions.ViperUndoableEvent.Undoable#redo()
		 */
		public void redo() {
			if (e != null && e.source != null) {
				AttrConfigImpl aci = (AttrConfigImpl) e.source;
				aci.attrName = getNewName();
				AttrNameChangeEvent ue = new AttrNameChangeEvent(
						(AttrConfigImpl) e.source, getOldName(), getNewName(),
						e.index);
				aci.fireMinorNodeChanged(ue);
			}
		}

		/**
		 * @see viper.api.extensions.ViperUndoableEvent.Undoable#canUndo()
		 */
		public boolean canUndo() {
			return e != null && e.source != null;
		}

		/**
		 * @see viper.api.extensions.ViperUndoableEvent.Undoable#canRedo()
		 */
		public boolean canRedo() {
			return e != null && e.source != null;
		}

		/**
		 * Gets the old attribute name.
		 * @return the old name
		 */
		public String getOldName() {
			return e.getOldName();
		}

		/**
		 * Gets the new attribute name.
		 * @return the new name
		 */
		public String getNewName() {
			return e.getNewName();
		}
	}

	public static class UndoableAttrNameChangeEvent extends AttrNameChangeEvent
			implements ViperUndoableEvent {

		UndoableAttrNameChangeEvent(AttrConfigImpl src, String oldName,
				String newName, int index) {
			super(src, oldName, newName, index);
		}

		/**
		 * @see viper.api.extensions.ViperUndoableEvent#getUndoable()
		 */
		public ViperUndoableEvent.Undoable getUndoable() {
			return new AttrNameChangeUndoable(this);
		}
	}

	public static class AttrTypeChangeEvent extends AbstractMinorChange {
		private String oldType;

		private AttrValueWrapper oldParams;

		private Object oldDefault;

		private String newType;

		private AttrValueWrapper newParams;

		private Object newDefault;

		AttrTypeChangeEvent(AttrConfigImpl src, String oldType,
				AttrValueWrapper oldParams, Object oldDefault, String newType,
				AttrValueWrapper newParams, Object newDefault, int index) {
			super.index = index;
			super.localName = "AttrTypeChange";
			super.source = src;

			this.oldType = oldType;
			this.oldParams = oldParams;
			this.oldDefault = oldDefault;
			this.newType = newType;
			this.newParams = newParams;
			this.newDefault = newDefault;

			assert verifyEncoded(oldParams, oldDefault, src);
			assert verifyEncoded(newParams, newDefault, src);
		}

		/**
		 * Gets the new attribute data type.
		 * @return the new data type
		 */
		public String getNewType() {
			return newType;
		}

		/**
		 * Gets the old attribute data type
		 * @return the old data type
		 */
		public String getOldType() {
			return oldType;
		}

		/**
		 * Gets the new parameter value wrapper
		 * @return the new value wrapper
		 */
		public AttrValueWrapper getNewParams() {
			return newParams;
		}

		/**
		 * Gets the old parameter value wrapper
		 * @return the old value wrapper
		 */
		public AttrValueWrapper getOldParams() {
			return oldParams;
		}

		/**
		 * Gets the new default value
		 * @return the new default value
		 */
		public Object getNewDefault() {
			return newDefault;
		}

		/**
		 * Gets the old default value
		 * @return the old default value
		 */
		public Object getOldDefault() {
			return oldDefault;
		}

	}

	public static class UndoableAttrTypeChangeEvent extends AttrTypeChangeEvent
			implements ViperUndoableEvent {
		UndoableAttrTypeChangeEvent(AttrConfigImpl src, String oldType,
				AttrValueWrapper oldParams, Object oldDefault, String newType,
				AttrValueWrapper newParams, Object newDefault, int index) {
			super(src, oldType, oldParams, oldDefault, newType, newParams,
					newDefault, index);
		}

		/**
		 * @see viper.api.extensions.ViperUndoableEvent#getUndoable()
		 */
		public ViperUndoableEvent.Undoable getUndoable() {
			return new AttrTypeChangeUndoable(this);
		}
	}

	public static class AttrTypeChangeUndoable implements ViperUndoableEvent.Undoable {
		private AttrTypeChangeEvent e;

		AttrTypeChangeUndoable(AttrTypeChangeEvent e) {
			this.e = e;
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
				AttrConfigImpl aci = (AttrConfigImpl) e.getSource();
				aci.attrType = e.getOldType();
				aci.params = e.getOldParams();
				aci.defaultVal = e.getOldDefault();
				AttrTypeChangeEvent ue = new AttrTypeChangeEvent(aci,
						getNewType(), getNewParams(), getNewDefault(),
						getOldType(), getOldParams(), getOldDefault(), e
								.getIndexes()[0]);
				aci.fireMinorNodeChanged(ue);
			}
		}

		/**
		 * @see viper.api.extensions.ViperUndoableEvent.Undoable#redo()
		 */
		public void redo() {
			if (e != null) {
				AttrConfigImpl aci = (AttrConfigImpl) e.getSource();
				aci.attrType = e.getNewType();
				aci.params = e.getNewParams();
				aci.defaultVal = e.getNewDefault();
				AttrTypeChangeEvent ue = new AttrTypeChangeEvent(aci,
						getOldType(), getOldParams(), getOldDefault(),
						getNewType(), getNewParams(), getNewDefault(), e
								.getIndexes()[0]);
				aci.fireMinorNodeChanged(ue);
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
		 * Gets the local part of the new type name's uri, or, rather, the part
		 * after the #, whatever that is called.
		 * @return the type name
		 */
		public String getNewTypeLocal() {
			return Util.getLocalPart(getNewType());
		}

		/**
		 * Gets the local part of the old type name's uri, or, rather, the part
		 * after the #, whatever that is called.
		 * @return the type name
		 */
		public String getOldTypeLocal() {
			return Util.getLocalPart(getOldType());
		}

		Object getNewDefault() {
			return e.getNewDefault();
		}

		AttrValueWrapper getNewParams() {
			return e.getNewParams();
		}

		String getNewType() {
			return e.getNewType();
		}

		Object getOldDefault() {
			return e.getOldDefault();
		}

		AttrValueWrapper getOldParams() {
			return e.getOldParams();
		}

		String getOldType() {
			return e.getOldType();
		}
	}

	/**
	 * @see viper.api.AttrConfig.Edit#setAttrName(java.lang.String)
	 */
	public void setAttrName(String name) {
		if (!getAttrName().equals(name)) {
			String oldName = this.attrName;
			int idx = getMyIndex();
			this.attrName = name;
			this.fireMinorNodeChanged(new UndoableAttrNameChangeEvent(this,
					oldName, name, idx));
		}
	}

	/**
	 * @return
	 */
	private int getMyIndex() {
		return (parent != null ) ? parent.indexOf(this) : -1;
	}

	static Object tryToConvertAttrValue(Object oldValue,
			AttrValueWrapper oldWrapper, AttrValueWrapper newWrapper, Node container) {
		try {
			// First, try setting the encoded version
			// this is good for lvalues, where 
			// you want to keep the value the
			// nth element in the list, not the string
			return newWrapper.setAttributeValue(newWrapper
					.getObjectValue(oldValue, container, null), container);
		} catch (BadAttributeDataException badx) {
			try {
				// Try setting to the decoded value.
				// This is good for converting to strings
				// or that sort of thing
				return newWrapper.setAttributeValue(oldWrapper
						.getObjectValue(oldValue, container, null), container);
			} catch (BadAttributeDataException badx2) {
				return null;
			}
		}
	}

	/**
	 * @see viper.api.AttrConfig.Edit#setAttrType(java.lang.String, viper.api.AttrValueWrapper)
	 */
	public void setAttrType(String uri, AttrValueWrapper params) {
		if (!this.attrType.equals(uri) || !this.params.equals(params)) {
			int idx = getMyIndex();
			Object oldDefault = defaultVal;
			AttrValueWrapper oldParams = this.params;
			Object newDefault = tryToConvertAttrValue(oldDefault, this.params,
					params, this);

			TransactionalNode v = (TransactionalNode) getRoot();
			TransactionalNode.Transaction t = v.begin(ViperParser.IMPL
					+ "AttrTypeChange");

			t.putProperty("name", this.attrName);
			t.putProperty("oldType", this.attrType);
			t.putProperty("oldTypeLocal", Util.getLocalPart(this.attrType));
			t.putProperty("newType", uri);
			t.putProperty("newTypeLocal", Util.getLocalPart(uri));

			MinorNodeChangeEvent e = new UndoableAttrTypeChangeEvent(this,
					this.attrType, this.params, this.defaultVal, uri, params,
					newDefault, idx);
			this.attrType = uri;
			this.params = params;
			this.defaultVal = newDefault;
			assert verifyEncoded(this.params, this.defaultVal, this);
			this.fireMinorNodeChanged(e);

			Iterator allInstances = Util.getAllInstancesOf(this);
			while (allInstances.hasNext()) {
				AttributeImpl a = (AttributeImpl) allInstances.next();
				a.changeParams(oldParams, this.params);
			}

			t.commit();
		}
	}

	/**
	 * @see viper.api.AttrConfig.Edit#setDefaultVal(java.lang.Object)
	 */
	public void setDefaultVal(Object val) {
		Object newDefault = val == null ? null : this.params
				.setAttributeValue(val, this);
		Object oldDefault = this.defaultVal;
		if (null == oldDefault ? null != newDefault : !oldDefault
				.equals(newDefault)) {
			MinorNodeChangeEvent e = new UndoableAttrDefaultChangeEvent(this,
					oldDefault, newDefault, getMyIndex());
			this.defaultVal = newDefault;
			fireMinorNodeChanged(e);
		}
	}

	/**
	 * @see viper.api.AttrConfig.Edit#setDynamic(boolean)
	 */
	public void setDynamic(boolean d) {
		if (isDynamic != d) {
			ViperData v = getRoot();
			TransactionalNode.Transaction t = ((TransactionalNode) v)
					.begin("AttrIsDynamicChange");

			t.putProperty("name", this.attrName);
			t.putProperty("static", d ? "dynamic" : "static");

			Iterator allInstances = Util.getAllInstancesOf(this);
			while (allInstances.hasNext()) {
				AttributeImpl a = (AttributeImpl) allInstances.next();
				a.changeDynamic(d);
			}

			isDynamic = d;
			fireMinorNodeChanged(new UndoableAttrIsDynamicChangeEvent(this, !d,
					getMyIndex()));

			t.commit();
		}
	}

	public static class AttrDefaultChangeEvent extends AbstractMinorChange {
		private Object oldDefault;

		private Object newDefault;

		AttrDefaultChangeEvent(AttrConfigImpl src, Object oldD, Object newD,
				int index) {
			super.index = index;
			super.localName = "AttrDefaultValueChange";
			super.source = src;

			this.oldDefault = oldD;
			this.newDefault = newD;
		}

		/**
		 * Gets the default value after the change.
		 * @return the new default value for the attribute
		 */
		public Object getNewDefault() {
			return newDefault;
		}

		/**
		 * Gets the default value from before thhe change.
		 * @return the previous default value
		 */
		public Object getOldDefault() {
			return oldDefault;
		}
	}

	private static boolean verifyEncoded(AttrValueWrapper p, Object o, Node container) {
		try {
			p.getObjectValue(o, null, null);
			return true;
		} catch (BadAttributeDataException badx) {
			return false;
		}
	}

	public static class AttrDefaultChangeUndoable implements
			ViperUndoableEvent.Undoable {
		private UndoableAttrDefaultChangeEvent e;

		AttrDefaultChangeUndoable(UndoableAttrDefaultChangeEvent e) {
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
				AttrConfigImpl aci = (AttrConfigImpl) e.getSource();
				aci.defaultVal = e.getOldDefault();
				assert verifyEncoded(aci.params, aci.defaultVal, aci);
				AttrDefaultChangeEvent ue = new AttrDefaultChangeEvent(
						(AttrConfigImpl) e.getSource(), e.getNewDefault(), e
								.getOldDefault(), e.getIndexes()[0]);
				aci.fireMinorNodeChanged(ue);
			}
		}

		/**
		 * @see viper.api.extensions.ViperUndoableEvent.Undoable#redo()
		 */
		public void redo() {
			if (e != null) {
				AttrConfigImpl aci = (AttrConfigImpl) e.getSource();
				aci.defaultVal = e.getNewDefault();
				assert verifyEncoded(aci.params, aci.defaultVal, aci);
				AttrDefaultChangeEvent ue = new AttrDefaultChangeEvent(
						(AttrConfigImpl) e.getSource(), e.getOldDefault(), e
								.getNewDefault(), e.getIndexes()[0]);
				aci.fireMinorNodeChanged(ue);
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
		 * Gets the default value before the change.
		 * @return the old default
		 */
		public Object getOldDefaultValue() {
			return e.getOldDefault();
		}

		/**
		 * Gets the default after the change.
		 * @return the new default
		 */
		public Object getNewDefaultValue() {
			return e.getNewDefault();
		}
	}

	public static class AttrIsDynamicChangeUndoable implements
			ViperUndoableEvent.Undoable {
		private UndoableAttrIsDynamicChangeEvent e;

		AttrIsDynamicChangeUndoable(UndoableAttrIsDynamicChangeEvent e) {
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
				AttrConfigImpl aci = (AttrConfigImpl) e.getSource();
				aci.isDynamic = e.getOldIsDynamic();
				AttrIsDynamicChangeEvent ue = new AttrIsDynamicChangeEvent(
						(AttrConfigImpl) e.getSource(), e.getNewIsDynamic(), e
								.getIndexes()[0]);
				aci.fireMinorNodeChanged(ue);
			}
		}

		/**
		 * @see viper.api.extensions.ViperUndoableEvent.Undoable#redo()
		 */
		public void redo() {
			if (e != null) {
				AttrConfigImpl aci = (AttrConfigImpl) e.getSource();
				aci.isDynamic = e.getNewIsDynamic();
				AttrIsDynamicChangeEvent ue = new AttrIsDynamicChangeEvent(
						(AttrConfigImpl) e.getSource(), e.getOldIsDynamic(), e
								.getIndexes()[0]);
				aci.fireMinorNodeChanged(ue);
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
		 * Gets the new value for the dynamic property.
		 * @return the new value of the dynamic property
		 */
		public boolean getNewIsDynamic() {
			return e.getNewIsDynamic();
		}

		/**
		 * Gets the old value of the dynamic property.
		 * @return the old value of dynamic
		 */
		public boolean getOldIsDynamic() {
			return e.getOldIsDynamic();
		}
	}

	public static class UndoableAttrDefaultChangeEvent extends
			AttrDefaultChangeEvent implements ViperUndoableEvent {
		/**
		 * Creates a new undoable change of the default value.
		 * @param src The attrconfig that generated the event
		 * @param oldD The encoded form of the old default value
		 * @param newD The encoded form of the new default value
		 * @param index The index of the source node from its parent
		 */
		public UndoableAttrDefaultChangeEvent(AttrConfigImpl src, Object oldD,
				Object newD, int index) {
			super(src, oldD, newD, index);
			assert verifyEncoded(src.getParams(), oldD, src);
			assert verifyEncoded(src.getParams(), newD, src);
		}

		/**
		 * @see viper.api.extensions.ViperUndoableEvent#getUndoable()
		 */
		public ViperUndoableEvent.Undoable getUndoable() {
			return new AttrDefaultChangeUndoable(this);
		}
	}

	public static class AttrIsDynamicChangeEvent extends AbstractMinorChange {
		private boolean oldIsDynamic;

		AttrIsDynamicChangeEvent(AttrConfigImpl src, boolean oldIsDynamic,
				int index) {
			super.index = index;
			super.localName = "AttrIsDynamicChange";
			super.source = src;

			this.oldIsDynamic = oldIsDynamic;
		}

		/**
		 * Gets the new value of the 'dynamic' property.
		 * @return the new value of 'dynamic
		 */
		public boolean getNewIsDynamic() {
			return !oldIsDynamic;
		}

		/**
		 * Gets the old value of the 'dynamic' property.
		 * @return the old value of 'dynamic
		 */
		public boolean getOldIsDynamic() {
			return oldIsDynamic;
		}
	}

	public static class UndoableAttrIsDynamicChangeEvent extends
			AttrIsDynamicChangeEvent implements ViperUndoableEvent {
		UndoableAttrIsDynamicChangeEvent(AttrConfigImpl src, boolean oldValue,
				int index) {
			super(src, oldValue, index);
		}

		/**
		 * @see viper.api.extensions.ViperUndoableEvent#getUndoable()
		 */
		public ViperUndoableEvent.Undoable getUndoable() {
			return new AttrIsDynamicChangeUndoable(this);
		}
	}

	protected Logger getLogger() {
		return ((EventfulNodeHelper) parent).getLogger();
	}

	/**
	 * @see viper.api.AttrConfig#isNillable()
	 */
	public boolean isNillable() {
		return nillable;
	}

	private void scrapeAttribute(AttributeImpl a) {
		Iterator times = a.getDescriptor().getValidRange().iterator();
		Descriptor d = a.getDescriptor();
		InstantRange r = (InstantRange) d.getValidRange().clone();
		boolean changed = false;
		while (times.hasNext()) {
			InstantInterval i = (InstantInterval) times.next();
			Iterator vals = a.getAttrValuesOverSpan(i);
			Instant from = i.getStartInstant();
			while (vals.hasNext()) {
				Interval ai = (Interval) vals.next();
				if (ai.getStart().compareTo(from) > 0) {
					changed = true;
					r.remove(from, ai.getStart());
				}
				from = (Instant) ai.getEnd();
			}
			if (i.getEnd().compareTo(from) > 0) {
				changed = true;
				r.remove(from, i.getEnd());
			}
		}
		if (changed) {
			d.setValidRange(r);
		}
	}

	/**
	 * Sets the attribute value to the given default whenever it is
	 * set to null and the enclosing descriptor is marked invalid.
	 * @param a the attribute to change
	 * @param def the value to give the attribute when it is currently 
	 * null but its parent descriptor is valid.
	 */
	public void redefaultAttribute(AttributeImpl a, Object def) {
		Iterator times = a.getDescriptor().getValidRange().iterator();
		List l = new ArrayList();
		while (times.hasNext()) {
			InstantInterval i = (InstantInterval) times.next();
			Iterator vals = a.getAttrValuesOverSpan(i);
			Instant from = i.getStartInstant();
			while (vals.hasNext()) {
				Interval ai = (Interval) vals.next();
				if (ai.getStart().compareTo(from) > 0) {
					l.add(new Span(from, (Instant) ai.getStart()));
				}
				from = (Instant) ai.getEnd();
			}
			if (i.getEnd().compareTo(from) > 0) {
				l.add(new Span(from, (Instant) i.getEnd()));
			}
		}
		for (int i = 0; i < l.size(); i++) {
			Span s = (Span) l.get(i);
			a.setAttrValueAtSpan(def, s);
		}
	}

	private void helpMakeNonNillable(AttributeImpl a) {
		Object d = getDefaultVal();
		boolean scrape = d != null;
		if (isDynamic) {
			if (scrape) {
				scrapeAttribute(a);
			} else {
				redefaultAttribute(a, d);
			}
		} else if (a.getAttrValue() != null) {
			if (scrape && (getParams() instanceof DefaultedAttrValueWrapper)) {
				DefaultedAttrValueWrapper dw = (DefaultedAttrValueWrapper) getParams();
				d = dw.getMetaDefault(a);
			}
			if (d == null) {
				a.getDescriptor().setValidRange(new InstantRange());
			} else {
				a.setAttrValue(d);
			}
		}
	}

	/**
	 * If setting nillable to false - need to remove all nill 
	 * attribute instances, somehow. There are two ways I can 
	 * think of - changing to the default value, or removing 
	 * the descriptor.
	 * So, what this does is set the attribute to the default, 
	 * if it exists. If no default exists, it sets the valid 
	 * bit to false. For static nulls, it sets the valid bit 
	 * to false for the whole descriptor, setting the attribute
	 * value to some meta-default (e.g. the empty 
	 * string or zero). This relies on the attribute param implementing
	 * the DefaultedAttrValueWrapper interface in the extensions package.
	 * @param n
	 */
	public void setNillable(boolean n) {
		if (this.nillable != n) {
			ViperData v = getRoot();
			TransactionalNode.Transaction t = ((TransactionalNode) v)
					.begin("AttrIsNillableChange");

			t.putProperty("name", this.attrName);
			t.putProperty("nillable", n ? "true" : "false");

			if (!n) {
				Iterator allInstances = Util.getAllInstancesOf(this);
				while (allInstances.hasNext()) {
					AttributeImpl a = (AttributeImpl) allInstances.next();
					helpMakeNonNillable(a);
				}
			}

			nillable = n;
			fireMinorNodeChanged(new UndoableAttrIsNillableChangeEvent(this,
					!n, getMyIndex()));

			t.commit();
		}
	}

	public static class AttrIsNillableChangeEvent extends AbstractMinorChange {
		private boolean oldIsNillable;

		AttrIsNillableChangeEvent(AttrConfigImpl src, boolean oldIsNillable,
				int index) {
			super.index = index;
			super.localName = "AttrIsNillableChange";
			super.source = src;

			this.oldIsNillable = oldIsNillable;
		}

		/**
		 * Gets the new value of the 'nillable' property.
		 * @return the new value of 'nillable'
		 */
		public boolean getNewIsNillable() {
			return !oldIsNillable;
		}

		/**
		 * Gets the old value of the 'nillable' property.
		 * @return the old value of 'nillable'
		 */
		public boolean getOldIsNillable() {
			return oldIsNillable;
		}
	}

	public static class UndoableAttrIsNillableChangeEvent extends
			AttrIsNillableChangeEvent implements ViperUndoableEvent {
		UndoableAttrIsNillableChangeEvent(AttrConfigImpl src, boolean oldValue,
				int index) {
			super(src, oldValue, index);
		}

		/**
		 * @see viper.api.extensions.ViperUndoableEvent#getUndoable()
		 */
		public ViperUndoableEvent.Undoable getUndoable() {
			return new AttrIsNillableChangeUndoable(this);
		}
	}

	public static class AttrIsNillableChangeUndoable implements
			ViperUndoableEvent.Undoable {
		private UndoableAttrIsNillableChangeEvent e;

		AttrIsNillableChangeUndoable(UndoableAttrIsNillableChangeEvent e) {
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
				AttrConfigImpl aci = (AttrConfigImpl) e.getSource();
				aci.isDynamic = e.getOldIsNillable();
				AttrIsNillableChangeEvent ue = new AttrIsNillableChangeEvent(
						(AttrConfigImpl) e.getSource(), e.getNewIsNillable(), e
								.getIndexes()[0]);
				aci.fireMinorNodeChanged(ue);
			}
		}

		/**
		 * @see viper.api.extensions.ViperUndoableEvent.Undoable#redo()
		 */
		public void redo() {
			if (e != null) {
				AttrConfigImpl aci = (AttrConfigImpl) e.getSource();
				aci.nillable = e.getNewIsNillable();
				AttrIsDynamicChangeEvent ue = new AttrIsDynamicChangeEvent(
						(AttrConfigImpl) e.getSource(), e.getOldIsNillable(), e
								.getIndexes()[0]);
				aci.fireMinorNodeChanged(ue);
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
		 * Gets the new value of the 'nillable' property.
		 * @return the new value of 'nillable'
		 */
		public boolean getNewIsNillable() {
			return e.getNewIsNillable();
		}

		/**
		 * Gets the old value of the 'nillable' property.
		 * @return the old value of 'nillable'
		 */
		public boolean getOldIsNillable() {
			return e.getOldIsNillable();
		}
	}
}

