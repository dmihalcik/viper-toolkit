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
import viper.api.time.*;
import edu.umd.cfar.lamp.viper.geometry.*;
import edu.umd.cfar.lamp.viper.util.*;

/**
 * Implements the root viper data node.
 */
public class ViperDataImpl extends EventfulNodeHelper
implements ViperData, XmlVisibleNode, EventfulNode {
	private static Rational DEFAULT_FPS = new Rational(25000);
	
	private List configInfo;
	
	private Logger logger = Logger.getLogger("viper.api.impl");

	private SortedMap allFiles = new NoticeTreeMap();
	private class NoticeTreeMap extends TreeMap {
		/**
		 * @see java.util.TreeMap#clear()
		 */
		public void clear() {
			allSourcefilesList = null;
			super.clear();
		}
		/**
		 * @see java.util.TreeMap#put(java.lang.Object, java.lang.Object)
		 */
		public Object put(Object arg0, Object arg1) {
			allSourcefilesList = null;
			return super.put(arg0, arg1);
		}
		/**
		 * @see java.util.TreeMap#putAll(java.util.Map)
		 */
		public void putAll(Map arg0) {
			allSourcefilesList = null;
			super.putAll(arg0);
		}
		/**
		 * @see java.util.TreeMap#remove(java.lang.Object)
		 */
		public Object remove(Object arg0) {
			allSourcefilesList = null;
			return super.remove(arg0);
		}
	}

	private List children;
	private Configs configsNode;
	private Sourcefiles sourcesNode;

	/**
	 * Checks to make sure that the viper data contains the same configuration
	 * and the same source files with the same instance data.
	 * 
	 * @param o the viper data object to check against
	 * @return <code>true</code> if they refer to the same set of viper data
	 */
	public boolean equals(Object o) {
		if (o == this) {
			return true;
		} else if (o instanceof ViperData) {
			ViperData v = (ViperData) o;
			if (configInfo.equals(v.getAllConfigs())) {
				Sourcefiles mine = this.getSourcefilesNode();
				Sourcefiles yours = v.getSourcefilesNode();
				return mine.equals(yours);
			}
		}
		return false;
	}
	/**
	 * The xor of the hashes  of the child nodes.
	 * @see java.lang.Object#hashCode()
	 */
	public int hashCode() {
		return configInfo.hashCode() ^ allFiles.hashCode();
	}

	private class ConfigsNodeImpl extends EventfulNodeHelper implements Configs, EventfulNode {
		ConfigsNodeImpl() {
			childNodeType = "Config";
		}
		/**
		 * @see viper.api.Node#getParent()
		 */
		public Node getParent() {
			return ViperDataImpl.this;
		}
		/**
		 * @see viper.api.Node#getChildren()
		 */
		public Iterator getChildren() {
			return getConfigs();
		}
		/**
		 * String representation.
		 * @return <q>config</q>
		 * @see java.lang.Object#toString()
		 */
		public String toString() {
			return "config";
		}
		/**
		 * Tests to see that the two viper schemata are equivalent.
		 * @param o the configs node to check against
		 * @return true when they are equal
		 */
		public boolean equals(Object o) {
			if (this == o) {
				return true;
			} else if (o instanceof Configs) {
				Configs that = (Configs) o;
				if (this.getNumberOfChildren() == that.getNumberOfChildren()) {
					Iterator those = that.getChildren();
					while (those.hasNext()) {
						if (!configInfo.contains(those.next())) {
							return false;
						}
					}
					return true;
				} else {
					return false;
				}
			} else {
				return false;
			}
		}
		/**
		 * @see java.lang.Object#hashCode()
		 */
		public int hashCode() {
			return configInfo.hashCode();
		}

		protected void helpSetChild(int i, Node n, TransactionalNode.Transaction t, boolean insert) {
			if (n == null) {
				Node oldN = configsNode.getChild(i);
				Iterator iter = Util.getAllInstancesOf((Config) oldN);
				List descsToRemove = new ArrayList();
				while (iter.hasNext()) {
					descsToRemove.add(iter.next());
				}
				for (int j = 0; j < descsToRemove.size(); j++) {
					Descriptor d = (Descriptor) descsToRemove.get(j);
					((SourcefileImpl) d.getParent()).removeChild(d, t != null);
				}
				ConfigImpl ci = (ConfigImpl) configInfo.remove(i);
				ci.setParent(null);
			} else if (insert) {
				ConfigImpl c = (ConfigImpl) n;
				Config alt = getConfig(c.getDescType(), c.getDescName());
				if (alt == null) {
					c.setParent(this);
					ViperDataImpl.this.configInfo.add(i, c);
				} else if (!alt.equals(c)) {
					throw new IllegalArgumentException("Cannot add a Configuration that already exists.");
				}
			} else {
				ConfigImpl c;
				if (i < configInfo.size()) {
					c = (ConfigImpl) configInfo.get(i);
					c.setParent(null);
				}
				c = (ConfigImpl) n;
				c.setParent(this);
				ViperDataImpl.this.configInfo.set(i, c);
			}
		}
		protected void postHelpSetChild(int i, Node n, TransactionalNode.Transaction t, boolean insert) {
		}

		/**
		 * @see viper.api.Configs#getConfig(int, java.lang.String)
		 */
		public Config getConfig(int type, String name) {
			Iterator descIter = configInfo.iterator();
			while (descIter.hasNext()) {
				Config retObj = (Config) descIter.next();
				if (retObj.getDescName().equals(name)
					&& (retObj.getDescType() == type)) {
					return retObj;
				}
			}
			return null;
		}
		/**
		 * @see viper.api.Node#getNumberOfChildren()
		 */
		public int getNumberOfChildren() {
			return configInfo.size();
		}
		/**
		 * @see viper.api.Node#hasChild(viper.api.Node)
		 */
		public boolean hasChild(Node n) {
			return configInfo.contains(n);
		}
		/**
		 * @see viper.api.Node#getChild(int)
		 */
		public Node getChild(int i) {
			return (Node) configInfo.get(i);
		}
		/**
		 * @see viper.api.Node#indexOf(viper.api.Node)
		 */
		public int indexOf(Node n) {
			return configInfo.indexOf(n);
		}
		protected Logger getLogger() {
			return ViperDataImpl.this.getLogger();
		}
	}

	private class SourcefilesNodeImpl extends EventfulNodeHelper implements Sourcefiles, EventfulNode {
		SourcefilesNodeImpl() {
			childNodeType = "Sourcefile";
		}
		/**
		 * @see viper.api.Node#getParent()
		 */
		public Node getParent() {
			return ViperDataImpl.this;
		}
		/**
		 * @see viper.api.Node#getChildren()
		 */
		public Iterator getChildren() {
			return getSourcefiles();
		}
		/**
		 * @return <q>data</q>
		 * @see java.lang.Object#toString()
		 */
		public String toString() {
			return "data";
		}
		/**
		 * Tests to see if the viper data set describes the
		 * same files with the same content.
		 * @see java.lang.Object#equals(java.lang.Object)
		 */
		public boolean equals(Object o) {
			if (this == o) {
				return true;
			} else if (o instanceof Sourcefiles) {
				Sourcefiles that = (Sourcefiles) o;
				if (this.getNumberOfChildren()
					== that.getNumberOfChildren()) {
					for (Iterator iter = getSourcefiles();
						iter.hasNext();
						) {
						Sourcefile curr = (Sourcefile) iter.next();
						MediaElement rm = curr.getReferenceMedia();
						String sfName = rm.getSourcefileName();
						ViperData thatData = (ViperData) that.getParent();
						Sourcefile compareTo = thatData.getSourcefile(sfName);
						if (!curr.equals(compareTo)) {
							return false;
						}
					}
					return true;
				}

				return this.getChildren().equals(
					((Sourcefiles) o).getChildren());
			} else {
				return false;
			}
		}
		/**
		 * Hashed on the file names.
		 * @see java.lang.Object#hashCode()
		 */
		public int hashCode() {
			return allFiles.hashCode();
		}

		/**
		 * @see viper.api.Node#addChild(viper.api.Node)
		 */
		public void addChild(Node n) {
			// FIXME check to make sure that the node is valid
			SourcefileImpl s = (SourcefileImpl) n;
			MediaElement rm = s.getReferenceMedia();
			Sourcefile old = getSourcefile(rm.getSourcefileName());
			if (old != null && old != s) {
				throw new IllegalArgumentException("Cannot have two sourcefiles with the same name");
			} else {
				s.setParent(this);
				allFiles.put(rm.getSourcefileName(), s);
				fireNodeChanged(
					new UndoableNodeChangeEventImpl(
						this,
						null,
						s,
						-1,
						allFiles.size() - 1,
						"AddSourcefile"));
			}
		}

		protected void helpSetChild(int i, Node n, TransactionalNode.Transaction t, boolean insert) {
			if (n == null) {
				SourcefileImpl f = (SourcefileImpl) getChild(i);
				MediaElement rm = f.getReferenceMedia();
				allFiles.remove(rm.getSourcefileName());
				f.setParent(null);
			} else {
				// XXX doesn't look at index
				//if (insert) {
				SourcefileImpl s = (SourcefileImpl) n;
				s.setParent(this);
				MediaElement rm = s.getReferenceMedia();
				allFiles.put(rm.getSourcefileName(), s);
			}
		}
		protected void postHelpSetChild(int i, Node n, TransactionalNode.Transaction t, boolean insert) {
		}
		/**
		 * @see viper.api.Node#getChild(int)
		 */
		public Node getChild(int i) {
			int count = 0;
			for (Iterator iter = allFiles.values().iterator();
				iter.hasNext();
				) {
				Object c = iter.next();
				if (count == i) {
					return (Node) c;
				}
				count++;
			}
			throw new IndexOutOfBoundsException(
				"Looking for child "
					+ i
					+ ", but only have "
					+ this.getNumberOfChildren()
					+ " children.");
		}
		/**
		 * @see viper.api.Node#indexOf(viper.api.Node)
		 */
		public int indexOf(Node n) {
			int count = 0;
			for (Iterator iter = allFiles.values().iterator();
				iter.hasNext();
				) {
				if (iter.next().equals(n)) {
					return count;
				}
				count++;
			}
			return -1;
		}
		/**
		 * @see viper.api.Node#getNumberOfChildren()
		 */
		public int getNumberOfChildren() {
			return allFiles.size();
		}
		/**
		 * @see viper.api.Node#hasChild(viper.api.Node)
		 */
		public boolean hasChild(Node n) {
			Sourcefile f = (Sourcefile) n;
			MediaElement rm = f.getReferenceMedia();
			if (allFiles.containsKey(rm.getSourcefileName())) {
				return allFiles.containsValue(f);
			}
			return false;
		}
		protected Logger getLogger() {
			return ViperDataImpl.this.getLogger();
		}
	}

	private class ViperDataTop extends AbstractList {
		// FIXME: Make immutable
		private Node[] kids;
		ViperDataTop() {
			kids = new Node[] { getConfigsNode(), getSourcefilesNode()};
		}
		/**
		 * @see java.util.List#toArray()
		 */
		public Object[] toArray() {
			return kids;
		}
		/**
		 * @see java.util.List#toArray(java.lang.Object[])
		 */
		public Object[] toArray(Object[] A) {
			return kids;
		}
		/**
		 * @see java.util.List#isEmpty()
		 */
		public boolean isEmpty() {
			return false;
		}
		/**
		 * @see java.util.List#size()
		 */
		public int size() {
			return kids.length;
		}
		/**
		 * @see java.util.AbstractList#get(int)
		 */
		public Object get(int i) {
			return kids[i];
		}
		/**
		 * @see java.util.List#contains(java.lang.Object)
		 */
		public boolean contains(Object o) {
			return kids[0] == o || kids[1] == o;
		}
		/**
		 * @see java.util.AbstractList#equals(java.lang.Object)
		 */
		public boolean equals(Object o) {
			return o == this
				|| ((o instanceof ViperDataTop)
					&& ((ViperDataTop) o).kids[0].equals(kids[0])
					&& ((ViperDataTop) o).kids[1].equals(kids[1]));
		}
		/**
		 * @see java.util.AbstractList#hashCode()
		 */
		public int hashCode() {
			return kids[0].hashCode() ^ kids[1].hashCode();
		}
	}

	/**
	 * @see viper.api.Node#getChildren()
	 */
	public Iterator getChildren() {
		return children.iterator();
	}
	/**
	 * @see viper.api.Node#getParent()
	 */
	public Node getParent() {
		return null;
	}

	/**
	 * Create an empty instance of this ViperData 
	 * implementation class.
	 */
	public ViperDataImpl() {
		configInfo = new LinkedList();
		sourcesNode = new SourcefilesNodeImpl();
		configsNode = new ConfigsNodeImpl();
		children = new ViperDataTop();
	}

	/**
	 * @see viper.api.ViperData#getAllConfigs()
	 */
	public List getAllConfigs() {
		return configInfo;
	}

	private List allSourcefilesList = null;

	/**
	 * @see viper.api.ViperData#getAllSourcefiles()
	 */
	public List getAllSourcefiles() {
		if (allSourcefilesList == null) {
			allSourcefilesList = Arrays.asList(allFiles.values().toArray());
		}
		return allSourcefilesList;
	}

	/**
	 * @see viper.api.ViperData#getConfig(int, java.lang.String)
	 */
	public Config getConfig(int type, String name) {
		Iterator descIter = configInfo.iterator();
		while (descIter.hasNext()) {
			Config retObj = (Config) descIter.next();
			if (retObj.getDescName().equals(name)
				&& (retObj.getDescType() == type)) {
				return retObj;
			}
		}
		return null;
	}
	
	public Descriptor findDescriptor(String filename, int cfgType, String cfgName, int id) {
		return getSourcefile(filename).getDescriptor(cfgType, cfgName, id);
	}

	/**
	 * @see viper.api.ViperData#getSourcefile(java.lang.String)
	 */
	public Sourcefile getSourcefile(String fname) {
		return (Sourcefile) allFiles.get(fname);
	}

	/**
	 * @see viper.api.ViperData#createConfig(int, java.lang.String)
	 */
	public Config createConfig(int type, String name) {
		ConfigImpl cfg = new ConfigImpl(name, type);
		cfg.setParent(getConfigsNode());
		configsNode.addChild(cfg);
		return cfg;
	}

	/**
	 * @see viper.api.ViperData#createSourcefile(java.lang.String)
	 */
	public Sourcefile createSourcefile(String filename) {
		FrameRate fr = new RationalFrameRate(DEFAULT_FPS);
		Sourcefile sf = new SourcefileImpl(this, filename, null, fr);
		sourcesNode.addChild(sf);
		return sf;
	}

	/**
	 * @see viper.api.ViperData#removeSourcefile(java.lang.String)
	 */
	public void removeSourcefile(String filename) {
		sourcesNode.removeChild(getSourcefile(filename));
	}
	
	void printViperData(PrintWriter pw) {
		try {
			Iterator confIter = configInfo.iterator();
			pw.println("#BEGIN_CONFIG\n");
			while (confIter.hasNext()) {
				Config c = (Config) confIter.next();
				((ConfigImpl) c).printConfig(pw);
			}
			pw.println("#END_CONFIG\n");
			//Iterator fileIter = allFiles.iterator ();
		} catch (IOException ept) {
			getLogger().severe("IOException: " + ept.getMessage());
		}
	}

	/**
	 * Returns the configsNode.
	 * @return Configs
	 */
	public Configs getConfigsNode() {
		return configsNode;
	}

	/**
	 * Returns the sourcesNode.
	 * @return Sourcefiles
	 */
	public Sourcefiles getSourcefilesNode() {
		return sourcesNode;
	}

	/**
	 * @see viper.api.extensions.XmlVisibleNode#getXMLFormat(org.w3c.dom.Document)
	 */
	public Element getXMLFormat(Document root) {
		Element el = root.createElement("viper");
		el.setAttribute("xmlns", ViperData.ViPER_SCHEMA_URI);
		el.setAttribute("xmlns:data", ViperData.ViPER_DATA_URI);

		Element cfgEl = root.createElement("config");
		for (Iterator cfgIter = getAllConfigs().iterator();
			cfgIter.hasNext();
			) {
			Node curr = (Node) cfgIter.next();
			if (curr instanceof XmlVisibleNode) {
				cfgEl.appendChild(((XmlVisibleNode) curr).getXMLFormat(root));
			}
		}
		el.appendChild(cfgEl);

		Element dataEl = root.createElement("data");
		for (Iterator srcIter = getAllSourcefiles().iterator();
			srcIter.hasNext();
			) {
			Sourcefile curr = (Sourcefile) srcIter.next();
			if (curr instanceof XmlVisibleNode) {
				Element currTag = ((XmlVisibleNode) curr).getXMLFormat(root);
				MediaElement rm = curr.getReferenceMedia();
				if (rm.getSourcefileName().length() > 0) {
					currTag.setAttribute("filename", rm.getSourcefileName());
				}
				dataEl.appendChild(currTag);
			}
		}
		el.appendChild(dataEl);

		return el;
	}


	/**
	 * Returns <q>viper</q>.
	 * @see java.lang.Object#toString()
	 */
	public String toString() {
		return "viper";
	}


	/**
	 * @see viper.api.Node#getNumberOfChildren()
	 * @return 2
	 */
	public int getNumberOfChildren() {
		return 2;
	}

	/**
	 * @see viper.api.Node#hasChild(viper.api.Node)
	 */
	public boolean hasChild(Node n) {
		return n.equals(this.getConfigsNode())
			|| n.equals(this.getSourcefilesNode());
	}

	/**
	 * @see viper.api.ViperData#getConfigs()
	 */
	public Iterator getConfigs() {
		return this.configInfo.iterator();
	}

	/**
	 * @see viper.api.ViperData#getConfigsOfType(int)
	 */
	public Iterator getConfigsOfType(int type) {
		return new ExceptIterator(new OnlyType(type), getConfigs());
	}
	private static class OnlyType implements ExceptIterator.ExceptFunctor {
		private int t;
		OnlyType(int t) {
			this.t = t;
		}
		/**
		 * Checks to see that the descriptor config object has the specified type.
		 * @see edu.umd.cfar.lamp.viper.util.ExceptIterator.ExceptFunctor#check(java.lang.Object)
		 */
		public boolean check(Object o) {
			return ((Config) o).getDescType() == t;
		}
	}
	
	/**
	 * @see viper.api.ViperData#getSourcefiles()
	 */
	public Iterator getSourcefiles() {
		return allFiles.values().iterator();
	}

	/**
	 * @see viper.api.Node#getChild(int)
	 */
	public Node getChild(int i) {
		if (i == 0) {
			return getConfigsNode();
		} else if (i == 1) {
			return getSourcefilesNode();
		} else {
			throw new IndexOutOfBoundsException("Node index out of bounds: " + i);
		}
	}

	/**
	 * @see viper.api.Node#indexOf(viper.api.Node)
	 */
	public int indexOf(Node n) {
		if (n instanceof Configs) {
			return 0;
		} else if (n instanceof Sourcefiles) {
			return 1;
		} else {
			return -1;
		}
	}

	protected void helpSetChild(int i, Node n, TransactionalNode.Transaction t, boolean insert) {
		throw new UnsupportedOperationException();
	}
	protected void postHelpSetChild(int i, Node n, TransactionalNode.Transaction t, boolean insert) {
		throw new UnsupportedOperationException();
	}
	/**
	 * @see viper.api.impl.EventfulNodeHelper#getLogger()
	 */
	public Logger getLogger() {
		return logger;
	}
	/**
	 * Sets the java logger for errors and debug-helpful messages.
	 * @param logger the new logger
	 */
	public void setLogger(Logger logger) {
		this.logger = logger;
	}
}
