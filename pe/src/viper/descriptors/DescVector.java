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

package viper.descriptors;

import java.io.*;
import java.util.*;

import org.w3c.dom.*;

import viper.descriptors.attributes.*;
import viper.filters.*;
import edu.umd.cfar.lamp.viper.util.*;
import edu.umd.cfar.lamp.viper.util.reader.*;

/**
 * A collection of Descriptor Objects that supports various operations on them,
 * such as comparison between to DescVector, collection of statistics, reading
 * and printing from files, and various others.
 * 
 * It extends Vector now, but could be made to extend some sorted list object
 * without too much hassle.
 */
public class DescVector extends Vector
		implements
			viper.descriptors.DescriptorList {
	private int highestId = 0;
	private int highestIdSet = 0;
	private String filename = null;
	private CanonicalFileDescriptor cfd = null;
	private DescriptorData holder;

	/**
	 * Gets the name of the file this metadata describes.
	 * 
	 * @return the source file name
	 */
	public String getSourcefileName() {
		return filename;
	}

	/**
	 * Sets the source file name.
	 * 
	 * @param S
	 *            the name of the file this set of metadata describes
	 */
	public void setSourcefileName(String S) {
		filename = S;
	}

	/**
	 * @inheritDoc
	 */
	public Collection getAllDescriptors() {
		return this;
	}

	/**
	 * Gets the map of the parent DescriptorData that holds the information for
	 * this sourcefile.
	 * 
	 * @see DescriptorList#getMap()
	 */
	public Equivalencies getMap() {
		return holder.getMap();
	}

	/**
	 * Sets the map of the parent DescriptorData that holds the information for
	 * this sourcefile.
	 * 
	 * @see DescriptorList#setMap(Equivalencies)
	 */
	public void setMap(Equivalencies map) {
		holder.setMap(map);
	}

	/**
	 * Constructs an empty list of descriptors with the 
	 * given parent data holder.
	 * @param parent the data holder that will contain this.
	 * You will have to add this to the holder manually.
	 */
	public DescVector(DescriptorData parent) {
		super();
		holder = parent;
	}

	/**
	 * Constructs an empty list of descriptors with the 
	 * given parent data holder.
	 * @param parent the data holder that will contain this.
	 * You will have to add this to the holder manually.
	 * @param initialCapacity the initial vector capacity
	 */
	public DescVector(int initialCapacity, DescriptorData parent) {
		super(initialCapacity);
		holder = parent;
	}

	/**
	 * Reads in Descriptor data from the specified list of files with the
	 * specified config info.
	 * 
	 * @param allFiles
	 *            a Vector of Strings containing the names of the files to
	 *            search for config information
	 * @param limits
	 *            a set of rules that are checked. Descriptors that don't pass
	 *            the limits are not parsed.
	 */
	public void parseData(List allFiles, RuleHolder limits) {
		parseData(new VReader(allFiles, "DATA"), limits);
	}

	/**
	 * Reads in Descriptor data from the specified list of files with the
	 * specified config info.
	 * 
	 * @param allFiles
	 *            a Vector of Strings containing the names of the files to
	 *            search for config information
	 * @param limits
	 *            a set of rules that are checked. Descriptors that don't pass
	 *            the limits are not parsed.
	 * @param all
	 *            print out all data-containing lines of the parsed file
	 * @param err
	 *            print out all error messages
	 * @param warn
	 *            print out all warning messages
	 * @param bad
	 *            print out the bad line in addition to the message and line
	 *            number
	 * @param totals
	 *            print out total number of errors/warnings/etc.
	 */
	public void parseData(List allFiles, RuleHolder limits, boolean all,
			boolean err, boolean warn, boolean bad, boolean totals) {
		VReader reader = new VReader(allFiles, "DATA");
		reader.changeSwitches(all, err, warn, bad, totals);
		parseData(reader, limits);
	}

	/**
	 * @param reader
	 *            uses the VReader at its current place. Note that this method
	 *            closes the reader at the end.
	 * @param limits
	 *            only parse in descriptors that meet these rules. If
	 *            <code>null</code>, then no rules are applied.
	 */
	protected void parseData(VReader reader, RuleHolder limits) {
		try {
			reader.advanceToBeginDirective("DATA");
			try {
				reader.gotoNextRealLine();
			} catch (IOException iox) {
				reader.printGeneralError("IO Exception while reading data; no DATA section read");
				return;
			}

			try {
				while (true) {
					try {
						assert holder != null;
						assert holder.getDescriptorConfigs() != null;
						DescSingle to_be_added = (DescSingle) holder
								.getDescriptorConfigs()
								.addFromGtf(reader, this);
						if (limits == null || limits.meetsCriteria(to_be_added)) {
							DescSingle old = (DescSingle) getNodeByID(
									to_be_added.getType(), to_be_added
											.getName(), ((Number) to_be_added
											.getID()).intValue());
							if (old != null) {
								old.combineWith(to_be_added);
							} else {
								addElement(to_be_added);
							}
						}
					} catch (BadDataException bdx) {
						if (bdx.isChar()) {
							reader.printError(bdx.getMessage(), bdx.getStart(),
									bdx.getEnd());
						} else {
							reader.printError(bdx.getMessage());
						}
						reader.gotoNextRealLine();
					}
				}
			} catch (EndOfBlockException eobx) {
				if (size() < 1)
					reader.printWarning("Data file contained no data");
			} catch (IOException iox) {
				reader.printGeneralError(iox.getMessage());
			}

		} finally {
			try {
				reader.close();
			} catch (IOException iox) {
				reader.printWarning("Error in closing files");
			}
		}
	}

	private void resetCanonicalFileDescriptor() {
		DescriptorConfigs descCfgInfo = holder.getDescriptorConfigs();
		Iterator fdIter = descCfgInfo.getNodesByType("FILE", "Information");
		if (!fdIter.hasNext()) {
			cfd = new CanonicalFileDescriptor();
		} else {
			cfd = new CanonicalFileDescriptor((DescPrototype) fdIter.next());
			descCfgInfo.removeNodesWith("FILE", "Information");
			fdIter = getNodesByType("FILE", "Information");
			if (fdIter.hasNext()) {
				String errs = cfd.set((Descriptor) fdIter.next());
				if (errs != null) {
					System.err.println(errs);
				}
			}
		}
		descCfgInfo.add(cfd.getThisPrototype());
		String sourceFileNames = cfd.getSourceFiles();
		int maxFrame = cfd.getNumFrames();
		if (maxFrame <= 0) {
			maxFrame = getHighestFrame();
			if (sourceFileNames != null) {
				try {
					int lastSpace = sourceFileNames.lastIndexOf(" ");
					while (sourceFileNames.charAt(lastSpace - 1) == ' ') {
						lastSpace--;
					}
					if (lastSpace > 0) {
						int penultimate = sourceFileNames.lastIndexOf(" ",
								lastSpace - 1);
						if (penultimate > 0) {
							try {
								String lastFrame = sourceFileNames.substring(
										penultimate + 1, lastSpace);
								maxFrame = Math.max(maxFrame, Integer
										.parseInt(lastFrame));
							} catch (NumberFormatException nfx) {
								System.err
										.println("Error in SOURCEFILES, this should be a number: '"
												+ sourceFileNames.substring(
														penultimate, lastSpace)
												+ "'");
							}
						}
					}
				} catch (StringIndexOutOfBoundsException sioobx) {
					System.err.println("Error in SOURCEFILES format: "
							+ sourceFileNames);
					sourceFileNames = null;
					cfd.setSourceFiles(null);
				}
			} // if has sourceFileNames
			cfd.setNumFrames(maxFrame);
		}
	}

	/**
	 * Reads in the given data in xml format.
	 * @param source the source DOM node
	 * @param limits information about what not to read
	 */
	public void parseData(Element source, RuleHolder limits) {
		String[] tags = new String[]{"file", "content", "object"};
		for (int i = 0; i < tags.length; i++) {
			NodeList nodes = source.getElementsByTagName(tags[i]);
			for (int j = 0; j < nodes.getLength(); j++) {
				Element aDesc = (Element) nodes.item(j);
				try {
					Descriptor to_be_added = holder.getDescriptorConfigs()
							.addFromGtf(aDesc, this);
					if (limits == null || limits.meetsCriteria(to_be_added)) {
						DescSingle old = (DescSingle) getNodeByID(
								to_be_added.getType(), to_be_added
										.getName(), ((Number) to_be_added
										.getID()).intValue());
						if (old != null) {
							old.combineWith(to_be_added);
						} else {
							addElement(to_be_added);
						}
					}
				} catch (BadDataException bdx) {
					System.err.println(bdx.getMessage());
				}
			}
		}
	}

	/**
	 * Checks to see if a Descriptor with this category and title is in the
	 * list.
	 * 
	 * @param type
	 *            the category of the Descriptor
	 * @param name
	 *            the name of the Descriptor
	 * @return true if one or more are found
	 */
	public boolean hasDescriptor(String type, String name) {
		for (int i = 0; i < size(); i++)
			if ((((Descriptor) elementAt(i)).named(name))
					&& (((Descriptor) elementAt(i)).getCategory().equals(type)))
				return true;
		return false;
	}

	/**
	 * Returns the first Descriptor in the list with the specified category and
	 * title. Returns null if none is found.
	 * 
	 * @see DescriptorList#getNodesByType(String, String)
	 */
	public Iterator getNodesByType(String type, String name) {
		return new ExceptIterator(new OnlyNamedThis(type, name, null), this
				.iterator());
	}
	/**
	 * Returns the first Descriptor in the list with the specified category and
	 * title. Returns null if none is found.
	 * 
	 * @param type
	 *            the category of the Descriptor
	 * @param name
	 *            the name of the Descriptor
	 * @param map
	 *            Equivalencies to use while searching
	 * @return a Descriptor with the specified type and title
	 */
	public Iterator getNodesByType(String type, String name, Equivalencies map) {
		return new ExceptIterator(new OnlyNamedThis(type, name, map), this
				.iterator());
	}
	private static final class OnlyNamedThis
			implements
				ExceptIterator.ExceptFunctor {
		private String type;
		private String name;
		private Equivalencies map;
		OnlyNamedThis(String type, String name, Equivalencies map) {
			this.type = type;
			this.name = name;
			this.map = map;
		}
		
		/**
		 * Checks to see if the descriptor is of the 
		 * requested type.
		 * @return {@inheritDoc}
		 */
		public boolean check(Object o) {
			Descriptor curr = (Descriptor) o;
			return curr.getCategory().equals(type)
					&& (curr.named(name) || (map != null && (map.eq(name, curr
							.getName()) || map.eq(curr.getName(), name))));
		}
	}

	/**
	 * @inheritDoc
	 */
	public Descriptor getNodeByID(String type, String name, int idNumber) {
		Integer id = new Integer(idNumber);
		for (int i = 0; i < size(); i++) {
			Descriptor curr = (Descriptor) get(i);
			if (curr.getID().equals(id) && curr.getType().equals(type)
					&& curr.getName().equals(name)) {
				return curr;
			}
		}
		return null;
	}

	/**
	 * Gets a flattened list of all ids. Flattened because DescAggregates have
	 * lists of IDs. May contain duplicates if this contains prototypes (all ids
	 * are 0) or if it was put together improperly.
	 * 
	 * @return a flattened list of all ids.
	 */
	public List getIds() {
		LinkedList idList = new LinkedList();
		for (Iterator iter = this.iterator(); iter.hasNext();) {
			Object id = ((Descriptor) iter.next()).getID();
			if (id instanceof Collection) {
				idList.addAll((Collection) id);
			} else {
				idList.add(id);
			}
		}
		return idList;
	}

	private class FrameSpecifiedIterator implements Iterator {
		private int i = -1;
		private FrameSpan frames;
		private boolean lookahead = true;
		private Descriptor current = null;

		FrameSpecifiedIterator(int frameNumber) {
			frames = new FrameSpan(frameNumber, frameNumber);
		}
		FrameSpecifiedIterator(FrameSpan span) {
			frames = (FrameSpan) span.clone();
		}

		/** @inheritDoc */
		public boolean hasNext() {
			if (!lookahead)
				return true;

			while (++i < size()) {
				current = (Descriptor) elementAt(i);
				if (current.getFrameSpan().intersects(frames)) {
					lookahead = false;
					current = current.crop(frames);
					return true;
				}
			}
			return false;
		}

		/** @inheritDoc */
		public Object next() {
			if (!lookahead) {
				lookahead = true;
				return current;
			} else {
				while (++i < size()) {
					current = (Descriptor) elementAt(i);
					if (current.getFrameSpan().intersects(frames)) {
						current = current.crop(frames);
						return current;
					}
				}
				return null;
			}
		}

		/** @inheritDoc */
		public void remove() {
			throw (new UnsupportedOperationException());
		}
	}

	/**
	 * @inheritDoc
	 */
	public Iterator cropNodesToSpan(FrameSpan span) {
		return new FrameSpecifiedIterator(span);
	}
	
	/**
	 * @inheritDoc
	 */
	public Iterator getNodesByFrame(FrameSpan span) {
		return new ExceptIterator(new OnlyWithThisFrame(span), this.iterator());
	}
	
	private static final class OnlyWithThisFrame
			implements
				ExceptIterator.ExceptFunctor {
		private FrameSpan span;
		OnlyWithThisFrame(FrameSpan span) {
			this.span = span;
		}
		/**
		 * Checks to see if the descriptor
		 * is valid on the given span.
		 * @return {@inheritDoc}
		 */
		public boolean check(Object o) {
			Descriptor curr = (Descriptor) o;
			return curr.getFrameSpan().intersects(span);
		}
	}
	
	/**
	 * @inheritDoc
	 */
	public Iterator getNodesByID(int idNumber) {
		return new ExceptIterator(new DescVector.OnlyWithThisID(new Integer(
				idNumber)), this.iterator());
	}
	
	private static final class OnlyWithThisID
			implements
				ExceptIterator.ExceptFunctor {
		private Integer id;
		OnlyWithThisID(Integer id) {
			this.id = id;
		}
		/**
		 * Checks to see that the descriptor matches the 
		 * requested id number.
		 * @return {@inheritDoc}
		 */
		public boolean check(Object o) {
			Descriptor curr = (Descriptor) o;
			return curr.getID().equals(id);
		}
	}

	/**
	 * Returns an array containing all Descriptor objects in the list with the
	 * specified category and title. Returns null if none is found.
	 * 
	 * @param type
	 *            the category of the Descriptor
	 * @param name
	 *            the name of the Descriptor
	 * @return a Descriptor[] with the specified type and title Descriptor
	 *         objects
	 */
	Descriptor[] getNodesWith(String type, String name) {
		Vector els = new Vector();
		for (int i = 0; i < size(); i++)
			if ((((Descriptor) elementAt(i)).getCategory().equals(type))
					&& ((((Descriptor) elementAt(i)).named(name))
							|| (getMap().eq(name, ((Descriptor) elementAt(i))
									.getName())) || (getMap().eq(
							((Descriptor) elementAt(i)).getName(), name))))
				els.addElement(elementAt(i));
		Descriptor[] tempEls = new Descriptor[els.size()];
		for (int i = 0; i < els.size(); i++)
			tempEls[i] = (Descriptor) els.elementAt(i);
		return (tempEls);
	}

	/**
	 * Returns a how many of the Descriptor objects in the list have the
	 * specified category and title.
	 * 
	 * @param type
	 *            the category of the Descriptor
	 * @param name
	 *            the name of the Descriptor
	 * @return how many occurances in the DescVector object
	 */
	int numNodesWith(String type, String name) {
		int total = 0;
		for (int i = 0; i < size(); i++)
			if ((((Descriptor) elementAt(i)).getCategory().equals(type))
					&& ((((Descriptor) elementAt(i)).named(name))
							|| (getMap().eq(name, ((Descriptor) elementAt(i))
									.getName())) || (getMap().eq(
							((Descriptor) elementAt(i)).getName(), name))))
				total++;
		return (total);
	}

	/**
	 * Removes all specified Descriptor objects and returns them in an array.
	 * Returns null if none is found.
	 * 
	 * @param type
	 *            the category of the Descriptor
	 * @param name
	 *            the name of the Descriptor
	 * @return a Descriptor[] with the specified type and title Descriptor
	 *         objects
	 */
	Descriptor[] removeNodesWith(String type, String name) {
		Vector els = new Vector();
		for (int i = size() - 1; i >= 0; i--)
			if ((((Descriptor) elementAt(i)).getCategory().equals(type))
					&& ((((Descriptor) elementAt(i)).named(name))
							|| (getMap().eq(((Descriptor) elementAt(i))
									.getName(), name)) || (getMap().eq(name,
							((Descriptor) elementAt(i)).getName())))) {
				els.addElement(elementAt(i));
				removeElementAt(i);
			}
		Descriptor[] tempEls = new Descriptor[els.size()];
		for (int i = 0; i < els.size(); i++)
			tempEls[i] = (Descriptor) els.elementAt(i);
		return (tempEls);
	}

	/**
	 * Adds a Descriptor to this list.
	 * 
	 * @param newNode
	 *            the Descriptor to add.
	 */
	void addNode(Descriptor newNode) {
		addElement(newNode);
	}

	/**
	 * Unions two DescVector objects.
	 * 
	 * @param A
	 *            the vector to add to this
	 */
	void mergeWith(DescVector A) {
		for (int i = 0; i < A.size(); i++)
			if (!contains(A.elementAt(i)))
				addElement(A.elementAt(i));
	}

	/**
	 * Returns the greatest frame index of all Descriptor objects.
	 * 
	 * @return the last frame index of all Descriptor objects. Will return -1 if
	 *         there are no frames.
	 */
	public int getHighestFrame() {
		if (size() == 0)
			return -1;
		int highestFrame = ((Descriptor) elementAt(0)).getHighestFrame();
		for (int i = 1; i < size(); i++) {
			int test = ((Descriptor) elementAt(i)).getHighestFrame();
			if (test > highestFrame)
				highestFrame = test;
		}
		return (highestFrame);
	}
	/**
	 * Returns the least frame index of all Descriptor objects.
	 * 
	 * @return the first frame index of all Descriptor objects Will return -1 if
	 *         there are no frames.
	 */
	public int getLowestFrame() {
		if (size() == 0)
			return -1;
		int lowestFrame = Integer.MAX_VALUE;
		for (int i = 0; i < size(); i++) {
			Descriptor currentDesc = (Descriptor) elementAt(i);
			FrameSpan currentSpan = currentDesc.getFrameSpan();
			int test = currentSpan.beginning();
			if ((currentSpan.size() > 0)
					&& (test < lowestFrame)
					&& !((test == 0) && (currentDesc.getCategory()
							.equals("FILE"))))
				lowestFrame = test;
		}
		return (lowestFrame);
	}

	/**
	 * Prints the data out to a file in the specified format.
	 * 
	 * @param output -
	 *            the PrintWriter to hand the data
	 */
	public void printOut(PrintWriter output) {
		output.println("#BEGIN_DATA");
		for (int t = 0; t < size(); t++)
			output.println(elementAt(t) + "\n");
		output.println("#END_DATA");
	}

	/**
	 * Prints the data in .gtf format, header and footer
	 * included.
	 * @return {@inheritDoc}
	 */
	public String toString() {
		StringBuffer temp = new StringBuffer();
		temp.append("#BEGIN_DATA\n");
		for (int i = 0; i < size(); i++) {
			temp.append(get(i)).append("\n");
		}
		temp.append("#END_DATA\n");
		return temp.toString();
	}

	/**
	 * Gets the source media file information.
	 * @return the source media file information
	 */
	public String getInformation() {
		Iterator fnodes = getNodesByType("FILE", "Information");
		if (!fnodes.hasNext())
			return "# No \"FILE Information\" Descriptor found.\n";

		Descriptor fnode = (Descriptor) fnodes.next();

		Attribute temp = fnode.getAttribute("SOURCEDIR");
		String S = (temp == null) ? "" : "SOURCEDIR = "
				+ temp.getValueToString() + '\n';

		temp = fnode.getAttribute("SOURCEFILES");
		S += (temp == null) ? "" : "SOURCEFILES = " + temp.getValueToString()
				+ "\n";

		temp = fnode.getAttribute("SOURCETYPE");
		S += (temp == null) ? "" : "SOURCETYPE = " + temp.getValueToString()
				+ "\n";

		temp = fnode.getAttribute("NUMFRAMES");
		S += (temp == null) ? "" : "NUMFRAMES = " + temp.getValueToString()
				+ "\n";
		return S;
	}

	/**
	 * Returns a sourcefile tag with lots of children. Note that there is no
	 * filename in the sourcefile tag, as the vector doesn't know about it.
	 * FIXME: Perhaps it should...
	 * 
	 * @param root
	 *            The DOM root
	 * @return &lt;sourcefile&gt; element, with descriptor children
	 */
	public Element getXMLFormat(Document root) {
		Element el = root.createElement("sourcefile");
		for (Iterator descs = iterator(); descs.hasNext();) {
			el.appendChild(((Descriptor) descs.next()).getXMLFormat(root));
		}
		return el;
	}

	private int getHighestId() {
		if (highestIdSet != hashCode()) {
			highestId = 0;
			for (Iterator ids = getIds().iterator(); ids.hasNext();) {
				highestId = Math.max(highestId, ((Integer) ids.next())
						.intValue());
			}
			highestIdSet = hashCode();
		}
		return highestId;
	}

	/**
	 * @inheritDoc
	 */
	public void addDescriptor(Descriptor desc)
			throws UnsupportedOperationException {
		desc.setID(getHighestId() + 1);
		add(desc);
	}

	/**
	 * Gets file information. See all of the caveats therein -- it may be null,
	 * or it may not have any useful values.
	 * 
	 * @see DescriptorList#getFileInformation()
	 */
	public CanonicalFileDescriptor getFileInformation() {
		resetCanonicalFileDescriptor();
		return cfd;
	}

	/**
	 * Will try to set the canonical file descriptor. Note that any attributes
	 * that are not in the prototype that was parsed from file (or added later)
	 * should not be set. The policy on how to treat the SOURCEFILES attribute
	 * may be implementation dependent (use it for ordering, although it may
	 * also be used to remove files if you wish).
	 * 
	 * @see DescriptorList#setFileInformation(CanonicalFileDescriptor)
	 */
	public void setFileInformation(CanonicalFileDescriptor cfd) {
		this.cfd = cfd;
	}

	/**
	 * @inheritDoc
	 */
	public DescriptorData getParent() {
		return holder;
	}
}