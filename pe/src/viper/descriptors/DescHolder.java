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

import org.apache.xerces.parsers.*;
import org.w3c.dom.*;
import org.xml.sax.*;

import viper.api.impl.*;
import viper.descriptors.attributes.*;
import viper.filters.*;
import edu.umd.cfar.lamp.viper.util.*;

/**
 * Implements the DescriptorData interface for the old data file format, as well
 * as the XML format. It supports XML input and output, as well as the old
 * format. This makes it somewhat fragile... there are certain things that can
 * only be done in the XML format that must be avoided for now.
 *
 * Fixme - only supports either one sourcefile w/ no filename or all
 *  sourcefiles with a filename
 */
public class DescHolder implements DescriptorData {
	private Map myDescriptors;
	/** information about file for printing to raw */
	private String information;
	private List fileOrder = null;

	private DescriptorConfigs descCfgInfo;

	/**
	 * @inheritDoc
	 */
	public DescriptorConfigs getDescriptorConfigs() {
		return descCfgInfo;
	}

	/**
	 * Sets the descriptor schema associated with this holder.
	 * @param cfgs the new schema
	 */
	public void setDescriptorConfigs(DescriptorConfigs cfgs) {
		descCfgInfo = cfgs;
	}

	/**
	 * @inheritDoc
	 */
	public boolean isMultifile() {
		return (myDescriptors.size() > 1)
			|| (myDescriptors.size() == 1 && !myDescriptors.keySet().contains(""));
	}

	/**
	 * Constructs a new, empty descriptor holder.
	 */
	public DescHolder() {
		myDescriptors = new HashMap();
		information = "";
	}

	/**
	 * Parse the data.
	 * @param allFiles list of file names/paths (String objects)
	 * @param cfgs the configuration
	 * @param limits the rules to use while parsing
	 * @param map the equivalency list
	 */
	public void parseData(
		List allFiles,
		DescriptorConfigs cfgs,
		RuleHolder limits,
		Equivalencies map) {
		DescVector data = new DescVector(this);
		descCfgInfo = cfgs;
		this.map = map;
		data.parseData(allFiles, limits);
		parseData(data);
	}

	/**
	 * Parse the data.
	 * @param allFiles list of file names/paths (String objects)
	 * @param cfgs the configuration
	 * @param limits the rules to use while parsing
	 * @param map the equivalency list
	 * @param all display all lines 
	 * @param err display errors
	 * @param warn display warnings
	 * @param bad display general errors
	 * @param totals print error/warning totals at end
	 */
	public void parseData(
		List allFiles,
		DescriptorConfigs cfgs,
		RuleHolder limits,
		Equivalencies map,
		boolean all,
		boolean err,
		boolean warn,
		boolean bad,
		boolean totals) {
		descCfgInfo = cfgs;
		this.map = map;
		DescVector data = new DescVector(this);
		data.parseData(allFiles, limits, all, err, warn, bad, totals);
		parseData(data);
	}

	/**
	 * Parses a DescVector in flat format into a multifile format.
	 * This is not an issue with the xml data structure, but the 
	 * gtf structure treats multiple media files (for example, a list
	 * of jpegs) as a single file, mapped to a single 'framespace'
	 * using the FILE descriptor. A File descriptor containing the
	 * SOURCEFILES, SOURCEDIR, SOURCETYPE and NUMFRAMES attribute qualifies
	 * as in the canonical form.
	 * @param data A DescVector to convert to this DescHolder
	 */
	public void parseData(DescVector data) {
		CanonicalFileDescriptor cfd = data.getFileInformation();
		String sourceFileNames = cfd.getSourceFiles();

		if (null == sourceFileNames) {
			// FIXME Right now, there is no data that is properly namespaced, so assume none is.
			myDescriptors = new TreeMap();
			myDescriptors.put("", data);
		} else {
			myDescriptors = new HashMap();
			int maxFrame = cfd.getNumFrames();
			fileOrder = new LinkedList();
			StringTokenizer fileInfo = new StringTokenizer(sourceFileNames);
			int nextFrame = 0;
			try {
				nextFrame = Integer.parseInt(fileInfo.nextToken());
				while (nextFrame <= maxFrame) {
					String mediaFile = fileInfo.nextToken();
					fileOrder.add(mediaFile);
					int offset = nextFrame;
					if (fileInfo.hasMoreTokens()) {
						nextFrame = Integer.parseInt(fileInfo.nextToken());
					} else {
						nextFrame = maxFrame + 1;
					}
					Iterator iter =
						data.cropNodesToSpan(
							new FrameSpan(offset, nextFrame - 1));
					DescVector tempVector = new DescVector(this);
					while (iter.hasNext()) {
						Descriptor curr = (Descriptor) iter.next();
						if (!curr.getCategory().equals("FILE")) {
							curr.moveFrame(1 - offset);
							tempVector.add(curr);
						}
					}
					CanonicalFileDescriptor currFileCfd =
						(CanonicalFileDescriptor) cfd.clone();
					currFileCfd.setNumFrames(nextFrame - offset);
					currFileCfd.setSourceFiles("1 " + mediaFile);
					tempVector.add(0, currFileCfd.getDescriptor());
					myDescriptors.put(mediaFile, tempVector);
				}
			} catch (NoSuchElementException nsex) {
				System.err.println("ERROR: Malformed SOURCEFILES attribute.");
				System.err.println(sourceFileNames);
				myDescriptors = new TreeMap();
				myDescriptors.put("", data);
			}
		}
	}

	/**
	 * Gets the data in gtf format
	 * @return the data in gtf format. Includes (lots of) new lines.
	 */
	public String toString() {
		String temp = getFlattenedData().toString();
		return descCfgInfo + "\n\n" + temp;
	}

	/**
	 * @inheritDoc
	 */
	public DescriptorList getFlattenedData() {
		if (myDescriptors.get("") != null) {
			return (DescriptorList) myDescriptors.get("");
		} else if (myDescriptors.size() == 1) {
			return (DescriptorList) myDescriptors.values().iterator().next();
		} else if (myDescriptors.size() == 0) {
			return new DescVector(this);
		} else {
			CanonicalFileDescriptor cfd = null;
			for (Iterator iter = myDescriptors.entrySet().iterator();
				iter.hasNext();
				) {
				DescVector d =
					(DescVector) ((Map.Entry) iter.next()).getValue();
				if (cfd == null) {
					cfd = d.getFileInformation();
				} else {
					cfd.add(d.getFileInformation());
				}
			}

			StringBuffer buf = new StringBuffer();
			int i = 1;
			for (Iterator iter = fileOrder.iterator(); iter.hasNext();) {
				String currFname = (String) iter.next();
				buf.append(i).append(" ");
				DescriptorList currList =
					(DescriptorList) myDescriptors.get(currFname);
				i += currList.getFileInformation().getNumFrames();
				buf.append(currFname).append(" ");
			}
			cfd.setSourceFiles(buf.substring(0, buf.length() - 1));

			// Now we have a list of the sourcefiles and the new cfd
			// start constructing the new list
			DescVector flatland = new DescVector(this);
			flatland.add(cfd.getDescriptor());

			String sourceFiles = cfd.getSourceFiles();
			if (sourceFiles != null) {
				StringTokenizer st = new StringTokenizer(sourceFiles);
				int newid = 1;
				while (st.hasMoreTokens()) {
					// for each sourcefile, shift the descriptors, reset their
					// ids, and add them in
					int frameOffset = Integer.parseInt(st.nextToken());
					String fileName = st.nextToken();
					DescriptorList data =
						(DescriptorList) myDescriptors.get(fileName);
					if (data == null) {
					} else {
						for (Iterator currDescs = data.iterator();
							currDescs.hasNext();
							) {
							Descriptor desc = (Descriptor) currDescs.next();
							if (!desc.getType().equals("FILE")) {
								Descriptor copy = (Descriptor) desc.clone();
								copy.moveFrame(frameOffset - 1);
								copy.setID(newid++);
								flatland.add(copy);
							}
						}
					}
				} // end while has more sourcefiles
			}
			return flatland;
		} // end else has multiple sourcefiles
	}

	/**
	 * @inheritDoc
	 */
	public DescriptorList getForFile(String filename) {
		return (DescriptorList) myDescriptors.get(filename);
	}

	/**
	 * @inheritDoc
	 */
	public Iterator getFileNames() {
		if (fileOrder == null) {
			return myDescriptors.keySet().iterator();
		} else {
			return fileOrder.iterator();
		}
	}

	/**
	 * @inheritDoc
	 */
	public boolean addFileName(String S) {
		if (!myDescriptors.containsKey(S)) {
			myDescriptors.put(S, new DescVector(this));
			if (fileOrder == null) {
				fileOrder = new LinkedList();
			}
			fileOrder.add(S);
			return true;
		}
		return false;
	}

	/**
	 * @inheritDoc
	 */
	public DescriptorList setDataForFile(String S, DescriptorList l) {
		if (myDescriptors == null) {
			return null;
		} else {
			return (DescriptorList) myDescriptors.put(S, l);
		}
	}

	/**
	 * @inheritDoc
	 */
	public DescriptorList removeFile(String filename) {
		return (DescriptorList) myDescriptors.remove(filename);
	}

	/**
	 * @inheritDoc
	 */
	public String getInformation() {
		CanonicalFileDescriptor cfd = getFlattenedData().getFileInformation();

		StringBuffer sb = new StringBuffer();
		String temp = cfd.getSourceDirectory();
		if (null != temp) {
			sb.append("SOURCEDIR = ");
			sb.append(temp);
			sb.append("\n");
		}

		temp = cfd.getMediaType();
		if (null != temp) {
			sb.append("SOURCETYPE = ");
			sb.append(temp);
			sb.append("\n");
		}

		temp = cfd.getSourceFiles();
		if (null != temp) {
			sb.append("SOURCEFILES = ");
			sb.append(temp);
			sb.append("\n");
		}

		int numSourceFrames = cfd.getNumFrames();
		sb.append("NUMFRAMES = ").append(numSourceFrames).append("\n");
		sb.append("NUMFILES = ").append(myDescriptors.values().size()).append(
			"\n");

		int[] dims = cfd.getDimensions();
		if (dims[0] > 0) {
			sb.append("FRAME-SIZE: ").append(dims[0]).append(" x ").append(
				dims[1]).append(
				"\n");
		}
		information = sb.toString();
		if (0 == information.length())
			information = "// No \"FILE Information\" Descriptor found.\n";
		return information;
	}

	/**
	 * Parses data from an XML file.
	 * @param input the xml input source
	 * @param cfgs if this is <code>null</code>, the parser is looks 
	 *           for the &lt;config&gt; element and generates its own
	 *           config information. If you want to do things
	 *           like limitation parsing and evaluations, you will have 
	 *           to parse the config info first.
	 * @param limits the rules to use while parsing
	 * @param map the equivalency list
	 * @param logfile log for the errors/warnings
	 * @throws IOException
	 * @throws BadDataException
	 */
	public void parseData(
		InputSource input,
		Equivalencies map,
		RuleHolder limits,
		DescriptorConfigs cfgs,
		PrintWriter logfile)
		throws IOException, BadDataException {
		DOMParser parser = new DOMParser();
		try {
			parser.setFeature(
				"http://apache.org/xml/features/validation/schema",
				false);
		} catch (SAXException snrx) {
			if (logfile != null) {
				logfile.println(snrx.getMessage());
				// parser does not support schemata
			}
		}
		try {
			parser.parse(input);
		} catch (SAXException saxx) {
			String fname = input.getPublicId();
			if (fname == null) {
				throw new BadDataException(
					"Error while parsing XML: " + saxx.getMessage());
			} else {
				throw new BadDataException(
					"Error while parsing " + fname + ": " + saxx.getMessage());
			}
		}
		Document document = parser.getDocument();
		parseData(document, map, limits, cfgs, logfile, input.getSystemId());
	}

	/**
	 * Parses data from an XML DOM document root.
	 * @param document the xml document
	 * @param cfgs if this is <code></code>, the parser is looks 
	 *           for the &lt;config&gt; element and generates its own
	 *           config information. If you want to do things
	 *           like limitation parsing and evaluations, you will have 
	 *           to parse the config info first.
	 * @param limits the rules to use while parsing
	 * @param map the equivalency list
	 * @param logfile the error writer
	 * @param filename the name of the file; useful for error writing
	 * @throws IOException
	 * @throws BadDataException
	 */
	public void parseData(
		Document document,
		Equivalencies map,
		RuleHolder limits,
		DescriptorConfigs cfgs,
		PrintWriter logfile,
		String filename)
		throws IOException, BadDataException {
		this.map = map;
		NodeList nodes;
		Element root = ViperParser.correctDocumentForOldNamespace(document);
		if (cfgs == null) {
			nodes =
				root.getElementsByTagNameNS(
					DescriptorData.NAMESPACE_URI,
					"config");
			if (nodes.getLength() < 1) {
				throw new BadDataException("config element missing.");
			} else if (nodes.getLength() > 1) {
				throw new BadDataException("Too many config elements.");
			}

			descCfgInfo = new DescriptorConfigs(null);
			Element configEl = (Element) nodes.item(0);
			descCfgInfo.addDesConfig(configEl);
			configEl.getParentNode().removeChild(configEl);
			configEl = null;
		} else {
			descCfgInfo = (DescriptorConfigs) cfgs.clone();
		}

		nodes =
			root.getElementsByTagNameNS(DescriptorData.NAMESPACE_URI, "data");
		myDescriptors = new HashMap();
		if (nodes.getLength() < 1) {
			System.err.println("No data found in xml " + filename + ".");
		} else if (nodes.getLength() > 1) {
			System.err.println("Too many data elements.");
		} else {
			Element dataEl = (Element) nodes.item(0);
			int numFiles =
				dataEl
					.getElementsByTagNameNS(
						DescriptorData.NAMESPACE_URI,
						"sourcefile")
					.getLength();
			Revealer ticker = null;
			if (logfile != null) {
				ticker = new Revealer(numFiles, 40, ".", logfile);
			}

			fileOrder = new LinkedList();
			while (dataEl.hasChildNodes()) {
				Node currNode = dataEl.getFirstChild();
				if (currNode.getNodeType() == Node.ELEMENT_NODE) {
					Element sfEl = (Element) currNode;
					if (sfEl
						.getNamespaceURI()
						.equals(DescriptorData.NAMESPACE_URI)
						&& sfEl.getTagName().equals("sourcefile")) {
						DescVector forThisFile = new DescVector(this);
						String currSourceName = sfEl.getAttribute("filename");
						forThisFile.parseData(sfEl, limits);

						fileOrder.add(currSourceName);
						myDescriptors.put(currSourceName, forThisFile);
						if (ticker != null) {
							ticker.tick();
						}
					} else {
						System.err.println(
							"Error: misplaced \""
								+ sfEl.getTagName()
								+ "\" tag.");
					}
				}
				dataEl.removeChild(currNode);
			}
			if (logfile != null) {
				logfile.println();
			}
		}
	}

	/**
	 * @inheritDoc
	 */
	public Element getXMLFormat(Document root) {
		Element el = root.createElement("viper");
		el.setAttribute("xmlns", DescriptorData.NAMESPACE_URI);
		el.setAttribute(
			"xmlns:data",
			viper.descriptors.attributes.Attributes.DEFAULT_NAMESPACE_URI);
		el.appendChild(descCfgInfo.getXMLFormat(root));

		if (fileOrder == null) {
			return el;
		}

		Element dataEl = root.createElement("data");
		el.appendChild(dataEl);
		for (Iterator foIter = fileOrder.iterator(); foIter.hasNext();) {
			String fname = (String) foIter.next();
			DescriptorList dl = (DescriptorList) myDescriptors.get(fname);
			Element currTag = dl.getXMLFormat(root);
			if (fname.length() > 0) {
				currTag.setAttribute("filename", fname);
			}
			dataEl.appendChild(currTag);
		}
		return el;
	}

	/**
	 * @inheritDoc
	 */
	public void merge(DescriptorData other) throws IllegalArgumentException {
		for (Iterator iter = other.getFileNames(); iter.hasNext();) {
			String currFile = (String) iter.next();
			DescriptorList already = (DescVector) myDescriptors.get(currFile);
			if (currFile == null || currFile.equals("")) {
				// Unfortunately, since a lot of data is mistakenly missing 
				// a FILE Information SOURCEFILES line or xml data is missing
				// the sourcefile filename, we have to disable merging in the 
				// empty filename-space
				throw new IllegalArgumentException("The data is missing a SOURCEFILES attribute");
			} else if (already == null) {
				if (fileOrder != null) {
					fileOrder.add(currFile);
				}
				myDescriptors.put(currFile, other.getForFile(currFile));
			} else {
				System.err.println(
					"Warning: Merging the same file: " + currFile);
				List ids = already.getIds();
				int maxId = 0;
				for (Iterator idIter = ids.iterator(); idIter.hasNext();) {
					maxId =
						Math.max(maxId, ((Integer) idIter.next()).intValue());
				}
				maxId++;
				for (Iterator descIter = other.getForFile(currFile).iterator();
					descIter.hasNext();
					) {
					Descriptor currDesc = (Descriptor) descIter.next();
					// FIXME Fine for DescSingle, but what about DescAggregate and DescPrototype?
					currDesc.setID(
						((Integer) currDesc.getID()).intValue() + maxId);
					already.add(currDesc);
				}
			}
		}
		// FIXME handle diffrerent descconfigs DescriptorConfigs descCfgInfo = 
	}

	/**
	 * @inheritDoc
	 */
	public void resetIds() {
		int id = 1;
		for (Iterator iter = myDescriptors.entrySet().iterator();
			iter.hasNext();
			) {
			Map.Entry currEntry = (Map.Entry) iter.next();
			for (Iterator descIter = ((List) currEntry.getValue()).iterator();
				descIter.hasNext();
				) {
				Descriptor currDesc = (Descriptor) descIter.next();
				currDesc.setID(id++);
			}
		}
	}

	private Equivalencies map;
	/**
	 * Gets the Equivalency map for this list.
	 * @return the name equivalency list
	 */
	public Equivalencies getMap() {
		return map;
	}

	/**
	 * Sets the equivalency map of the list.
	 * @param map the name equivalencies
	 */
	public void setMap(Equivalencies map) {
		this.map = map;
	}

}
