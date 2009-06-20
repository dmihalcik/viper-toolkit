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
import edu.umd.cfar.lamp.viper.util.*;

/**
 * An implementation of the viper api sourcefile node type,
 */
public class SourcefileImpl
	extends EventfulNodeHelper
	implements CanonicalSourcefile, XmlVisibleNode, EventfulNode {
	private List descObjs;
	private Sourcefiles parent;
	private MediaElement media;

	/**
	 * @see viper.api.Node#getChildren()
	 */
	public Iterator getChildren() {
		return descObjs.iterator();
	}
	/**
	 * @see viper.api.Node#getParent()
	 */
	public Node getParent() {
		return parent;
	}
	void setParent(Sourcefiles d) {
		parent = d;
	}

	/**
	 * Tests that this and the parameter have the same reference
	 * media and that their children are equal. Does not test parentage.
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	public boolean equals(Object o) {
		if (this == o) {
			return true;
		} else if (o instanceof Sourcefile) {
			Sourcefile that = (Sourcefile) o;
			if (that.getReferenceMedia().equals(this.getReferenceMedia())
				&& that.getNumberOfChildren() == this.getNumberOfChildren()) {
				for (Iterator iter = this.getDescriptors(); iter.hasNext();) {
					Descriptor myCurr = (Descriptor) iter.next();
					int myType = myCurr.getDescType();
					String myName = myCurr.getDescName();
					int myId = myCurr.getDescId();
					Descriptor yourCurr =
						that.getDescriptor(myType, myName, myId);
					if (!myCurr.equals(yourCurr)) {
						return false;
					}
				}
				return true;
			}
		}
		return false;
	}
	
	/**
	 * Since it is common for the filename to be unambiguous,
	 * this just returns the filename's hash code.
	 * @return <code>media.hashCode()</code>
	 */
	public int hashCode() {
		return media.hashCode();
	}

	/** @inheritDoc */
	public String toString() {
		return "SOURCE \"" + StringHelp.backslashify(getReferenceMedia().getSourcefileName()) + '"';
	}
	
	/**
	 * Creates a new Sourcefile object with the given parent ViperData.
	 * Actually, that is its grandparent. 
	 * @param parent the containing ViperData
	 * @param sfname the name of the media file this describes
	 * @param c a list of Descriptor instances to use
	 * @param rate the FrameRate of the media file
	 */
	public SourcefileImpl(
		ViperData parent,
		String sfname,
		Collection c,
		FrameRate rate) {
		this.parent = parent.getSourcefilesNode();
		fi = new FileInformation();
		super.childNodeType = "Descriptor";

		media = new MediaElementImpl(sfname, rate);

		descObjs = new LinkedList();

		if (c != null)
			descObjs.addAll(c);
	}

	/**
	 * @see viper.api.Sourcefile#getAllDescriptors()
	 */
	public Collection getAllDescriptors() {
		return descObjs;
	}

	/**
	 * @see viper.api.Sourcefile#getDescriptor(int, java.lang.String, int)
	 */
	public Descriptor getDescriptor(int type, String name, int id) {

		Iterator descIter = descObjs.iterator();

		while (descIter.hasNext()) {
			Descriptor desc = (Descriptor) descIter.next();

			if ((desc.getDescName().equals(name))
				&& (desc.getDescType() == type)
				&& (desc.getDescId() == id))
				return (desc);
		}

		return null;
	}
	
	
	public Descriptor getDescriptor(Config cfg, int id) {
		return getDescriptor(cfg.getDescType(), cfg.getDescName(), id);
	}
	
	/**
	 * @see viper.api.Sourcefile#getDescsByTime(viper.api.time.Instant)
	 */
	public Collection getDescsByTime(Instant i) {
		Collection c = new LinkedList();
		i = media.normalize(i);
		Iterator descIter = descObjs.iterator();
		while (descIter.hasNext()) {
			Descriptor desc = (Descriptor) descIter.next();
			if (desc.getValidRange().contains(i)) {
				c.add(desc);
			}
		}
		return c;
	}

	/**
	 * @see viper.api.Sourcefile#getDescsByTime(viper.api.time.InstantInterval)
	 */
	public Collection getDescsByTime(InstantInterval s) {
		Collection c = new LinkedList();
		s = media.normalize(s);
		Iterator descIter = descObjs.iterator();
		while (descIter.hasNext()) {
			Descriptor desc = (Descriptor) descIter.next();
			if (desc.getValidRange().intersects(s)) {
				c.add(desc);
			}
		}
		return c;
	}

	/**
	 * @see viper.api.Sourcefile#getDescByType(int)
	 * @deprecated
	 */
	public Collection getDescByType(int type) {

		Collection col = new LinkedList();
		Iterator tempIter = descObjs.iterator();

		while (tempIter.hasNext()) {
			Descriptor desc = (Descriptor) tempIter.next();

			if (desc.getDescType() == type)
				col.add(desc);

		}

		return col;
	}

	/**
	 * @see viper.api.Sourcefile#getDescByName(viper.api.Config)
	 * @deprecated
	 */
	public Collection getDescByName(Config c) {
		Collection byName = new LinkedList();
		for (Iterator iter = descObjs.iterator(); iter.hasNext();) {
			Descriptor curr = (Descriptor) iter.next();
			if (curr.getConfig().equals(c)) {
				byName.add(curr);
			}
		}
		return byName;
	}

	/**
	 * Looks for a free id in the given space.
	 * @param c the config space
	 * @return a free id
	 */
	public int findFreeIdFor(Config c) {
		// FIXME check for the id  
		Collection old = this.getDescByName(c);
		int i = 0;
		for (Iterator iter = old.iterator(); iter.hasNext();) {
			Descriptor curr = (Descriptor) iter.next();
			i = Math.max(i, curr.getDescId() + 1);
		}
		return i;
	}

	/**
	 * @see viper.api.Sourcefile#createDescriptor(viper.api.Config)
	 */
	public Descriptor createDescriptor(Config c) {
		return createDescriptor(c, findFreeIdFor(c));
	}

	/**
	 * @see viper.api.Sourcefile#createDescriptor(viper.api.Config, int)
	 */
	public Descriptor createDescriptor(Config c, int id) {
		// FIXME check id for use. maybe replace descriptor with
		// it if it exists, or throw exception?
		Descriptor newDesc = new DescriptorImpl(this, (ConfigImpl) c, id);
		descObjs.add(newDesc);
		NodeChangeEvent nce =
			new UndoableNodeChangeEventImpl(
				this,
				null,
				newDesc,
				-1,
				indexOf(newDesc),
				"AddDescriptor");
		fireNodeChanged(nce);
		return newDesc;
	}

	void printMediaFile(BufferedWriter bwt) {
		Iterator i = descObjs.iterator();

		while (i.hasNext()) {
			Descriptor d = (Descriptor) i.next();
			((DescriptorImpl) d).printDescriptor(bwt);
		}
	}

	/**
	 * @see viper.api.extensions.XmlVisibleNode#getXMLFormat(org.w3c.dom.Document)
	 */
	public Element getXMLFormat(Document root) {
		Element el = root.createElement("sourcefile");
		for (Iterator descs = getAllDescriptors().iterator();
			descs.hasNext();
			) {
			Node curr = (Node) descs.next();
			if (curr instanceof XmlVisibleNode) {
				el.appendChild(((XmlVisibleNode) curr).getXMLFormat(root));
			}
		}
		return el;
	}

	protected void helpSetChild(int i, Node n, TransactionalNode.Transaction t, boolean insert) {
		if (n == null) {
			DescriptorImpl d = (DescriptorImpl) descObjs.remove(i);
			d.setParent(null);
		} else if (!insert) {
			DescriptorImpl d;
			if (i < descObjs.size()) {
				d = (DescriptorImpl) descObjs.get(i);
				d.setParent(null);
			}
			d = (DescriptorImpl) n;
			d.setParent(this);
			descObjs.set(i, n);
		} else {
			DescriptorImpl d = (DescriptorImpl) n;
			d.setParent(this);
			descObjs.add(i, d);
		}
	}
	protected void postHelpSetChild(int i, Node n, TransactionalNode.Transaction t, boolean insert) {
	}

	/**
	 * @see viper.api.Sourcefile#getDescriptors()
	 */
	public Iterator getDescriptors() {
		return this.descObjs.iterator();
	}
	/**
	 * @see viper.api.Sourcefile#getDescriptorsBy(viper.api.time.InstantInterval)
	 */
	public Iterator getDescriptorsBy(InstantInterval i) {
		return new ExceptIterator(new CheckTimeFunctor(i), getDescriptors());
	}
	/**
	 * @see viper.api.Sourcefile#getDescriptorsBy(viper.api.time.Instant)
	 */
	public Iterator getDescriptorsBy(Instant i) {
		i = media.getFrameRate().asFrame(i);
		return new ExceptIterator(new CheckTimeFunctor(i), getDescriptors());
	}
	/**
	 * @see viper.api.Sourcefile#getDescriptorsBy(int)
	 */
	public Iterator getDescriptorsBy(int type) {
		return new ExceptIterator(
			new CheckDescTypeFunctor(type),
			getDescriptors());
	}
	/**
	 * @see viper.api.Sourcefile#getDescriptorsBy(viper.api.Config)
	 */
	public Iterator getDescriptorsBy(Config c) {
		return new ExceptIterator(
			new CheckDescClassFunctor(c),
			getDescriptors());
	}
	/**
	 * @see viper.api.Sourcefile#getDescriptorsBy(viper.api.Config, viper.api.time.InstantInterval)
	 */
	public Iterator getDescriptorsBy(Config c, InstantInterval ii) {
		return new ExceptIterator(
			new CheckDescClassFunctor(c),
			getDescriptorsBy(ii));
	}

	/**
	 * @see viper.api.Sourcefile#getDescriptorsBy(viper.api.Config, viper.api.time.Instant)
	 */
	public Iterator getDescriptorsBy(Config c, Instant i) {
		return new ExceptIterator(
			new CheckDescClassFunctor(c),
			getDescriptorsBy(i));
	}
	/**
	 * @see viper.api.Node#getNumberOfChildren()
	 */
	public int getNumberOfChildren() {
		return this.descObjs.size();
	}
	/**
	 * @see viper.api.Node#hasChild(viper.api.Node)
	 */
	public boolean hasChild(Node n) {
		return this.descObjs.contains(n);
	}
	/**
	 * @see viper.api.Node#getChild(int)
	 */
	public Node getChild(int i) {
		return (Node) descObjs.get(i);
	}
	/**
	 * @see viper.api.Node#indexOf(viper.api.Node)
	 */
	public int indexOf(Node n) {
		return descObjs.indexOf(n);
	}
	private FileInformation fi;
	/**
	 * @see viper.api.extensions.CanonicalSourcefile#getCanonicalFileDescriptor()
	 */
	public CanonicalFileDescriptor getCanonicalFileDescriptor() {
		if (fi.getDescriptor() == null) {
			Iterator iter = getDescriptorsBy(Config.FILE);
			if (iter.hasNext()) {
				fi.set((Descriptor) iter.next());
			} else {
				ViperData v = (ViperData) parent.getParent();
				iter = v.getConfigsOfType(Config.FILE);
				if (!iter.hasNext()) {
					return null;
				}
				final Config cfg = (Config) iter.next();
//				EventQueue.invokeLater(new Runnable() {
//					public void run() {
						fi.set(createDescriptor(cfg));
//					}});
			}
		}
		return fi;
	}

	/**
	 * @see viper.api.Sourcefile#getReferenceMedia()
	 */
	public MediaElement getReferenceMedia() {
		return media;
	}
	/**
	 * @see viper.api.TemporalNode#getRange()
	 */
	public TemporalRange getRange() {
		if (media == null || media.getSpan() == null) {
			return null;
		} else {
			return Intervals.singletonRange(media.getSpan());
		}
	}
	/**
	 * @see viper.api.TemporalNode#setRange(viper.api.time.TemporalRange)
	 */
	public void setRange(TemporalRange r) {
		media.setSpan((InstantInterval) r.getExtrema());
	}
	protected Logger getLogger() {
		return ((EventfulNodeHelper) parent).getLogger();
	}
}
/**
 * Looks for descriptors that overlap a given instant or interval.
 */
class CheckTimeFunctor implements ExceptIterator.ExceptFunctor {
	private final Object i;
	/**
	 * The interval to look for valid descriptors within.
	 * @param i the interval to check
	 */
	public CheckTimeFunctor(InstantInterval i) {
		this.i = Intervals.singletonRange(i);
	}
	/**
	 * Will check to see if the descriptor is valid at the 
	 * given instant.
	 * @param i the instant to look for
	 */
	public CheckTimeFunctor(Instant i) {
		this.i = i;
	}
	/**
	 * @see edu.umd.cfar.lamp.viper.util.ExceptIterator.ExceptFunctor#check(java.lang.Object)
	 */
	public boolean check(Object o) {
		if (o instanceof TemporalNode) {
			TemporalNode t = (TemporalNode) o;
			if (i instanceof Instant) {
				return t.getRange().contains(i);
			}
			if (i instanceof TemporalRange) {
				return t.getRange().intersects((TemporalRange) i);
			}
			assert false;
		}
		return false;
	}
}

/**
 * Checks for a certain type of descriptor, e.g. CONTENT or OBJECT.
 */
class CheckDescTypeFunctor implements ExceptIterator.ExceptFunctor {
	private final int type;
	/**
	 * Will look for descriptors of the given type.
	 * @param type the type, e.g. {@link Config#CONTENT}
	 */
	public CheckDescTypeFunctor(int type) {
		this.type = type;
	}
	/**
	 * @see edu.umd.cfar.lamp.viper.util.ExceptIterator.ExceptFunctor#check(java.lang.Object)
	 */
	public boolean check(Object o) {
		if (o instanceof Descriptor) {
			Descriptor d = (Descriptor) o;
			return d.getDescType() == this.type;
		}
		return false;
	}
}

/**
 * Checks to make sure that the descriptor is an instance of the 
 * given class of descriptors (config).
 */
class CheckDescClassFunctor implements ExceptIterator.ExceptFunctor {
	private final Config type;
	/**
	 * The config to search for.
	 * @param type the descriptor type to look for
	 */
	public CheckDescClassFunctor(Config type) {
		this.type = type;
	}
	/**
	 * Makes sure the object is a descriptor that is
	 * an instance of the config specified in the 
	 * constructor.
	 * @param o the descriptor to check
	 * @return <code>true</code> when the object is an
	 * instance of the descriptor config
	 */
	public boolean check(Object o) {
		if (o instanceof Descriptor) {
			Descriptor d = (Descriptor) o;
			return d.getConfig().equals(this.type);
		}
		return false;
	}
}
