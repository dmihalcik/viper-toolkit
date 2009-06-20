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

import java.net.*;
import java.util.*;

import viper.api.*;
import viper.api.extensions.*;
import viper.api.time.*;
import edu.umd.cfar.lamp.viper.util.*;

/**
 * A class containing a variety of utility methods for dealing with
 * viper data.
 */
public class Util {
	private static class NodeChildIterator extends AbstractCircularIterator {
		private Node n;
		private int i;
		NodeChildIterator(Node n) {
			this.n = n;
			this.i = 0;
		}
		
		/**
		 * @see java.util.ListIterator#next()
		 */
		public Object next() {
			if (!hasNext()) {
				throw new NoSuchElementException();
			}
			i = this.nextIndex();
			return n.getChild(i);
		}
		/**
		 * @see java.util.ListIterator#previous()
		 */
		public Object previous() {
			if (!hasPrevious()) {
				throw new NoSuchElementException();
			}
			i = this.previousIndex();
			return n.getChild(i);
		}
		/**
		 * @see java.util.ListIterator#nextIndex()
		 */
		public int nextIndex() {
			int next = i-1;
			if (next >= n.getNumberOfChildren()) {
				next = 0;
			}
			return next;
		}
		/**
		 * @see java.util.ListIterator#previousIndex()
		 */
		public int previousIndex() {
			int prev = i-1;
			if (prev < 0) {
				prev = n.getNumberOfChildren()-1;
			}
			return prev;
		}
		/**
		 * @see edu.umd.cfar.lamp.viper.util.AbstractCircularIterator#remove()
		 */
		public void remove() {
			n.removeChild(i);
		}
		/**
		 * @see edu.umd.cfar.lamp.viper.util.AbstractCircularIterator#set(java.lang.Object)
		 */
		public void set(Object o) {
			n.setChild(i, (Node) o);
		}
		/**
		 * @see edu.umd.cfar.lamp.viper.util.AbstractCircularIterator#add(java.lang.Object)
		 */
		public void add(Object o) {
			n.addChild((Node) o);
		}
		/**
		 * @see edu.umd.cfar.lamp.viper.util.CircularIterator#current()
		 */
		public Object current() {
			if (!isEmpty()) {
				return n.getChild(i);
			}
			return null;
		}
		/**
		 * @see edu.umd.cfar.lamp.viper.util.CircularIterator#isEmpty()
		 */
		public boolean isEmpty() {
			return n.getNumberOfChildren() > 0;
		}
	}

	/**
	 * Get the local part of the uri, if possible. Otherwise,
	 * returns the whole uri.
	 * @param uri the uri to crop
	 * @return the local part of the uri
	 */
	public static String getLocalPart(String uri) {
		try {
			URI u = new URI(uri);
			String lname = u.getFragment();
			if (lname == null) {
				lname = u.getPath();
			}
			return lname;
		} catch (URISyntaxException e) {
			return uri;
		}
	}

	/**
	 * Checks to see that the given string is a descriptor type,
	 * e.g. FILE or OBJECT.
	 * @param s the string to check
	 * @return <code>true</code> if the string is a descriptor type
	 */
	public static boolean isDescType(String s) {
		return (s.length() == 4 && (s.equals("file") || s.equals("FILE")))
			|| (s.length() == 6 && (s.equals("object") || s.equals("OBJECT")))
			|| (s.length() == 7 && (s.equals("content") || s.equals("CONTENT")));
	}
	
	/**
	 * Gets the enumeration corresponding to the string name
	 * for a descriptor, e.g. CONTENT returns {@link Config#CONTENT}.
	 * @param s the string to check
	 * @return the corresponding constant
	 * @throws IllegalArgumentException if {@link #isDescType(String)} 
	 * returns <code>false</code> for the parameter
	 */
	public static int getDescType(String s) throws IllegalArgumentException {
		int ret = -1;
		if (s.length() == 4 && (s.equals("file") || s.equals("FILE"))) {
			ret = Config.FILE;
		} else if (
			s.length() == 6 && (s.equals("object") || s.equals("OBJECT"))) {
			ret = Config.OBJECT;
		} else if (
			s.length() == 7 && (s.equals("content") || s.equals("CONTENT"))) {
			ret = Config.CONTENT;
		} else {
			throw new IllegalArgumentException(
				"Not a valid Descriptor type: '" + s + "'");
		}
		return ret;
	}
	/**
	 * Gets the string name for the descriptor type corresponding to 
	 * the given constant.
	 * @param i the descriptor type, e.g. {@link Config#CONTENT}
	 * @return the descriptor type name, e.g. <q>CONTENT</q>
	 * @throws IllegalArgumentException if not a descriptor type
	 * constant
	 */
	public static String getDescType(int i) throws IllegalArgumentException {
		switch (i) {
			case Config.OBJECT :
				return "OBJECT";
			case Config.CONTENT :
				return "CONTENT";
			case Config.FILE :
				return "FILE";
			default :
				throw new IllegalArgumentException("Invalid Descriptor Type ID");
		}
	}

	/**
	 * Checks to see if all instances of the config satisfy its schema.
	 * 
	 * @param c The descriptor configuration of which to test the instances.
	 * @return <code>true</code> iff all instances of <code>c</code> are valid
	 */
	public static boolean validConfig(Config c) {
		// First, make sure that c is valid itself
		if (c.getDescType() != Config.OBJECT) {
			for (Iterator iter = c.getChildren(); iter.hasNext();) {
				AttrConfig curr = (AttrConfig) iter.next();
				if (curr.isDynamic()) {
					return false;
				}
			}
		}
		ViperData v = (ViperData) c.getParent().getParent();
		Sourcefiles s = v.getSourcefilesNode();
		for (Iterator files = s.getChildren(); files.hasNext();) {
			Sourcefile f = (Sourcefile) files.next();
			// constraints : there can only be one FILE descriptor,
			// and content descriptors cannot overlap in time
			Iterator ofType = f.getDescriptorsBy(c);
			switch (c.getDescType()) {
				case Config.FILE :
					if (ofType.hasNext()) {
						ofType.next();
						if (ofType.hasNext()) {
							return false;
						}
					}
					break;
				case Config.CONTENT :
					Range r = new Range();
					while (ofType.hasNext()) {
						Descriptor currDesc = (Descriptor) ofType.next();
						Range currRange = currDesc.getValidRange();
						if (r.intersects(currRange)) {
							return false;
						}
						r.add(currRange);
					}
					break;
			}

			for (Iterator descs = f.getDescriptorsBy(c); descs.hasNext();) {
				if (!validInstance((Descriptor) descs.next())) {
					return false;
				}
			}
		}
		return true;
	}

	/**
	 * Checks to see that the given instance of a descriptor
	 * properly satisfies its configuration.
	 * @param d the descriptor to validate
	 * @return <code>true</code> iff the descriptors attributes
	 *   match the attribute configs, and it doesn't have any odd attributes.
	 */
	public static boolean validInstance(Descriptor d) {
		Config cfg = d.getConfig();
		for (Iterator ats = d.getAttributes(); ats.hasNext();) {
			Attribute curr = (Attribute) ats.next();
			AttrConfig mcfg = curr.getAttrConfig();
			if (cfg.indexOf(mcfg) < 0) {
				return false; // unknown attribute
			}
			if (!validAttribute(curr)) {
				return false;
			}
		}
		return true;
	}

	/**
	 * Checks to see that the given instance of a descriptor's attribute field
	 * properly satisfies its configuration.
	 * @param a the attribute to validate
	 * @return <code>true</code> iff the attribute match its attribute config
	 */
	public static boolean validAttribute(Attribute a) {
		AttrConfig cfg = a.getAttrConfig();
		if (cfg.isDynamic()) {
			Iterator iter = a.iterator();
			while (iter.hasNext()) {
				try {
					iter.next();
				} catch (BadAttributeDataException badx) {
					return false; // whatever badx says
				}
			}
		} else {
			try {
				cfg.getParams().setAttributeValue(a.getAttrValue(), a);
			} catch (BadAttributeDataException badx) {
				return false; // whatever badx says
			}
		}
		return true;
	}
	
	private static class DescriptorIterator implements Iterator {
		private boolean dirtyCache;
		private Config c;
		private Iterator sources;
		private Iterator descs;
		private Descriptor next;
		
		DescriptorIterator (Config c) {
			this.c = c;
			ViperData v = this.c.getRoot();
			if (v != null) {
				Sourcefiles s = v.getSourcefilesNode();
				if (s != null) {
					sources = s.getChildren();
				}
			}
			if (sources == null) {
				sources = Collections.EMPTY_LIST.iterator();
			}
			cacheNext();
		}
		
		private void cacheNext() {
			dirtyCache = false;
			if (descs == null || !descs.hasNext()) {
				while (sources.hasNext()) {
					Sourcefile cs = (Sourcefile) sources.next();
					descs = cs.getDescriptorsBy(c);
					if (descs.hasNext()) {
						next = (Descriptor) descs.next();
						return;
					}
				}
			} else {
				next = (Descriptor) descs.next();
				return;
			}
			next = null;
		}

		/**
		 * @see java.util.Iterator#hasNext()
		 */
		public boolean hasNext() {
			if (dirtyCache) {
				cacheNext();
			}
			return next != null;
		}

		/**
		 * @see java.util.Iterator#next()
		 */
		public Object next() {
			dirtyCache = true;
			return next;
		}

		/**
		 * @see java.util.Iterator#remove()
		 */
		public void remove() {
			if (!dirtyCache) {
				throw new IllegalStateException();
			} else {
				descs.remove();
			}
		}
	}
	private static class AttributeIterator implements Iterator {
		private AttrConfig c;
		private Iterator descs;
		
		AttributeIterator(AttrConfig c) {
			this.c = c;
			descs = Util.getAllInstancesOf ((Config) c.getParent());
		}
		/**
		 * @see java.util.Iterator#hasNext()
		 */
		public boolean hasNext() {
			return descs.hasNext();
		}
		/**
		 * @see java.util.Iterator#next()
		 */
		public Object next() {
			Descriptor d = (Descriptor) descs.next();
			return d.getAttribute(c);
		}
		/**
		 * @see java.util.Iterator#remove()
		 */
		public void remove() {
			throw new UnsupportedOperationException();
		}

	}
	
	/**
	 * Gets an iterator that returns all instances of the attribute 
	 * in the corresponding viper data node, accross all 
	 * sourcefiles.
	 * @param c the config to search for
	 * @return an iterator of Attribute nodes
	 */
	public static Iterator getAllInstancesOf(AttrConfig c) {
		return new AttributeIterator(c);
	}

	/**
	 * Gets an iterator that returns all instances of the descriptor 
	 * in the corresponding viper data node, accross all 
	 * sourcefiles.
	 * @param c the config to search for
	 * @return an iterator of Descriptor nodes
	 */
	public static Iterator getAllInstancesOf(Config c) {
		return new DescriptorIterator(c);
	}

	/**
	 * Checks to see if a node contains another as a parent or grandparent. 
	 * @param ancestor
	 * @param descendant
	 * @return
	 */
	public static boolean isChildOf(Node ancestor, Node descendant) {
		if (descendant == null) {
			return false;
		} if (ancestor.equals(descendant)) {
			return true;
		} else {
			return isChildOf(ancestor, descendant.getParent());
		}
	}

	/**
	 * Gets the string representation of the given interval.
	 * This is the old skool, inclusive format. 
	 * @param i the interval
	 * @return string in the form <code>start:end</code>, inclusive
	 */
	public static String valueOf(Interval i) {
		return i.getStart() + ":" + ((Instant) i.getEnd()).previous();
	}
	
	/**
	 * Returns a circular iterator (never ends, unless there
	 * are no elements) through all children of the node.
	 * @param n
	 * @return
	 */
	public static CircularIterator childIterator(Node n) {
		return new NodeChildIterator(n);
	}
	
	/**
	 * Tries to run the runnable while wrapped in a transaction
	 * on the given node, if it is a Transactionable node.
	 * If it succeeds, returns true and commits the transaction.
	 * If it fails, it rolls back the transaction, if the node is
	 * transactionable. 
	 * @param r the commands to run
	 * @param n the node beneath which the actions occur
	 * @param uri the name for the transaction
	 * @param properties properties to attach to the transaction, if
	 * it is constructed.
	 */
	public static void tryTransaction(Runnable r, Node n, String uri, Object[] properties) {
		TransactionalNode.Transaction t = null;
		try {
			if (n instanceof TransactionalNode) {
				t = ((TransactionalNode) n).begin(uri);
			}
			r.run();
			if (t != null) {
				for (int i = 0; i < properties.length; i+=2) {
					t.putProperty((String) properties[i], properties[i+1]);
				}
				t.commit();
			}
		} finally {
			if (t != null && t.isAlive()) {
				t.rollback();
			}
		}
	}
	
	
	/**
	 * Shifts the given descriptors by the value new-old.
	 * @param d the descriptors to shift
	 * @param oldMark the original offset
	 * @param newMark the new offset
	 */
	public static void shiftDescriptors(final Descriptor[] d, final Instant oldMark, final Instant newMark) {
		if (d == null || d.length <= 0) {
			return;
		}
		Sourcefiles sf = (Sourcefiles) d[0].getParent().getParent();
		Runnable r = new Runnable() {
			public void run() {
				for (int i = 0; i < d.length; i++) {
					Instant shift;
					Sourcefile sf = d[i].getSourcefile();
					InstantInterval ii = null;
					if (sf instanceof CanonicalSourcefile) {
						CanonicalSourcefile csf = (CanonicalSourcefile) sf;
						CanonicalFileDescriptor info = csf.getCanonicalFileDescriptor();
						ii = new Span(new Frame(1), new Frame(info.getNumFrames()));
						FrameRate fr = info.getFrameRate();
						if (d[i].getValidRange().isFrameBased()) {
							shift = fr.asFrame(newMark).go(-fr.asFrame(oldMark).longValue());
						} else {
							shift = fr.asTime(newMark).go(-fr.asTime(oldMark).longValue());
							ii = fr.asFrame(ii);
						}
					} else {
						shift = newMark.go(-oldMark.longValue());
					}
					helpShiftADescriptor(d[i], shift, ii);
				}
			}
		};
		Object[] props = new Object[] {"start", oldMark, "end", newMark};
		tryTransaction(r, sf, ViperParser.IMPL + "Shift", props);
	}
	
	/**
	 * Replaces the existing descriptor with another that has the
	 * same data in a different location and the same id.
	 * @param d
	 * @param amount
	 * @param validSpan the span to crop to (to avoid descriptors outside the video)
	 */
	private static void helpShiftADescriptor(Descriptor d, Instant amount, InstantInterval validSpan) {
		Sourcefile sf = d.getSourcefile();
		Config cfg = d.getConfig();
		sf.removeChild(d);
		// events from the descriptor will now go nowhere, so we can
		// change it with impunity
		InstantRange ir = d.getValidRange();
		ir.shift(amount);
		if (validSpan != null) {
			ir.crop(validSpan);
		}
		Descriptor newd = sf.createDescriptor(cfg, d.getDescId());
		newd.setValidRange(ir);
		Iterator attrs = cfg.getAttributeConfigs();
		while (attrs.hasNext()) {
			AttrConfig ac = (AttrConfig) attrs.next();
			if (ac.isDynamic()) {
				AttributeImpl a = (AttributeImpl) d.getAttribute(ac);
				TimeEncodedList av = (TimeEncodedList) a.attrValue;
				if (av != null) {
					av = (TimeEncodedList) av.clone();
					av.shift(amount);
					if (validSpan != null) {
						av.crop(validSpan);
					}
					Iterator iter = av.iterator();
					Attribute newa = newd.getAttribute(ac);
					while(iter.hasNext()) {
						DynamicAttributeValue curr = (DynamicAttributeValue) iter.next();
						newa.setAttrValueAtSpan(curr.getValue(), curr);
					}
				}
			} else {
				newd.getAttribute(ac).setAttrValue(d.getAttribute(ac).getAttrValue());
			}
		}
//		// now add it back. This will be the second and last event fired in this method.
//		sf.addChild(newd);
	}
	
	/**
	 * Tests to see if the node is below the node labeled
	 * above, or is the node labeled above, in the tree (e.g.
	 * above is below, or below's ancestor).
	 * @param above the possible ancestor
	 * @param below the possible descendant
	 * @return if the two nodes are the same or 
	 * if below is the descendant of above
	 */
	public static boolean beneath(Node above, Node below) {
		if ((above instanceof AttrConfig) && (below instanceof Attribute)) {
			below = ((Attribute) below).getAttrConfig();
		} else if (above instanceof Config) {
			if (below instanceof Attribute) {
				below = ((Attribute) below).getAttrConfig();
			} else if (below instanceof Descriptor) {
				below = ((Descriptor) below).getConfig();
			}
		}
		while (below != null) {
			if (below.equals(above)) {
				return true;
			}
			below = below.getParent();
		}
		return false;
	}
	
	public static InstantInterval guessMediaInterval(Sourcefile sf) {
		Instant start = null;
		Instant end = null;
		Iterator descIter = sf.getChildren();
		while (descIter.hasNext()) {
			Descriptor curr = (Descriptor) descIter.next();
			InstantRange r = curr.getValidRange();
			if (!r.isEmpty()) {
				InstantInterval ii = (InstantInterval) r.getExtrema();
				start = min(ii.getStartInstant(), start);
				end = max(ii.getEndInstant(), end);
			}
		}
		if (start == null) {
			return new Span(new Frame(1), new Frame(2));
		}
		return new Span(start, end);
	}
	private static Instant min(Instant a, Instant b) {
		return a == null ? b : b == null ? a : a.compareTo(b) < 0 ? a : b;
	}
	private static Instant max(Instant a, Instant b) {
		return a == null ? b : b == null ? a : a.compareTo(b) > 0 ? a : b;
	}
}
