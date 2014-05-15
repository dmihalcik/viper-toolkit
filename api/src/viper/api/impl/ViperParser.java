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
import java.net.*;
import java.util.*;
import java.util.logging.*;

import javax.xml.parsers.*;

import org.w3c.dom.*;
import org.w3c.dom.Node;
import org.xml.sax.*;

import viper.api.*;
import viper.api.datatypes.*;
import viper.api.extensions.*;
import viper.api.time.*;
import edu.umd.cfar.lamp.viper.util.*;

/**
 * This class acts as a factory for ViperData classes, a parser, and 
 * a simple command line application to validate viper xml files.
 * 
 * If you want to add your own types, you can add them to the 
 * {@link #typeFactory} method or you can replace that 
 * with your own ViperDataFactory implementation.
 */
public class ViperParser {
	/**
	 * URI namespace for things related to this implementation.
	 */
	public static final String IMPL = "http://viper-toolkit.sourceforge.net/owl/api/impl#";
	
	private static Logger logger = Logger.getLogger("viper.api.impl");
	private ViperDataFactory typeFactory;
	/**
	 * Creates a new parser.
	 */
	public ViperParser() {
		typeFactory = new ViperDataFactoryImpl();
	}
	/**
	 * Gets the type name-to-valuewrapper object associated
	 * with the parser.
	 * @return the type factory
	 */
	public ViperDataFactory getTypeFactory () {
		return typeFactory;
	}
	
	/**
	 * Sets the attribute value wrapper generator used while parsing
	 * data.
	 * @param factory the factory, which converts from type names to
	 * wrapper objects
	 */
	public void setTypeFactory (ViperDataFactory factory) {
		this.typeFactory = factory;
	}
	
	/**
	 * Main method that parses in a file and spits it back out.
	 */
	public static class FileTranscriberTest {
		/**
		 * A simple tester for the parser. 
		 * @param args the command line arguments. The first is the file to
		 * parse, the optional second is the output file. If only one
		 * argument is passed, it prints to system.out.
		 */
		public static void main(String[] args) {
			String filename = null;
			String outputFile = null;
			BufferedWriter bw = null;

			if (args.length < 1) {
				System.err.println("Filename is missing.");
				System.exit(-1);
			} else if (args.length == 2) {
				outputFile = args[1];
			} else if (args.length > 2) {
				System.err.println("Too many input arguments");
				System.exit(-2);
			}

			filename = args[0];
			System.err.println("File is " + filename);
			FileInputStream inFile = null;

			try {
				inFile = new FileInputStream(filename);
			} catch (FileNotFoundException fe) {
				System.err.println("FileNotFoundException " + filename);
				return;
			}

			try {
				if (outputFile == null) {
					bw = new BufferedWriter(new OutputStreamWriter(System.err));
				} else {
					File outFile = new File(outputFile);
					if (outFile.canWrite()) {
						FileOutputStream outStream = new FileOutputStream(outFile);
						bw = new BufferedWriter(new OutputStreamWriter(outStream));
					} else {
						System.err.println("Cannot write to file: " + outFile);
						System.exit(-3);
					}
				}
			} catch (IOException et) {
				System.err.println("IOException: " + et.getMessage());
				et.printStackTrace();
				System.exit(-4);
			}

			try {

				DocumentBuilderFactory factory =
					DocumentBuilderFactory.newInstance();
				factory.setNamespaceAware(true);
				DocumentBuilder builder = factory.newDocumentBuilder();
				Document document = builder.parse(inFile);
				
				ViperParser vp = new ViperParser();

				ViperDataImpl newFile =
					(ViperDataImpl) vp.parseDoc(document.getDocumentElement());

				System.err.println();
				System.err.println("Starting test1...");
				XmlSerializer.toWriter(newFile, new PrintWriter(System.out));
			} catch (FactoryConfigurationError err) {
				System.err.println(
					"FactoryConfigurationError: " + err.getMessage());
			} catch (ParserConfigurationException pce) {
				System.err.println("ParserConfigurationException: ");
				pce.printStackTrace();
			} catch (SAXException se) {
				System.err.println("SAXException: " + se.getMessage());
			} catch (IOException ex) {
				System.err.println("IOException: " + ex.getMessage());
				ex.printStackTrace();
			}

			try {
				bw.close();
			} catch (IOException exc) {
				System.err.println("Error closing the output file");
			}

			System.err.print(StringHelp.banner("Finished", 60));

		}
	}
	
	/**
	 * Parses the viper xml file at the given location.
	 * @param fname the file to parse
	 * @return the data contained in the file
	 * @throws IOException if there is an error while opening the file
	 */
	public ViperData parseFromTextFile(URI fname)
		throws IOException {
		Element corrected = file2correctDOM(fname);
		return (ViperDataImpl) parseDoc(corrected);
	}
	/**
	 * Parses an XGTF file in to a DOM object, with corrected namespaces.
	 * @param fname the file to parse
	 * @return the DOM tree from the file, with old namespaces replaced with
	 * new namespaces
	 * @throws FileNotFoundException
	 * @throws IOException
	 */
	public static Element file2correctDOM(URI fname) throws FileNotFoundException, IOException {
		InputStream inFile = null;
		File f = new File(fname);
		Document document;
		try {
			if (f.exists()) {
				inFile = new FileInputStream(f);
			} else {
				try {
					inFile = fname.toURL().openStream();
				}  catch (MalformedURLException e) {
					throw new IllegalArgumentException(
						"File: " + fname + " not found");
				} catch (IOException e) {
					throw new IllegalArgumentException(
						"Error loading URL: " + fname 
						+ "\n\t" + e.getLocalizedMessage());
				}
			}
			DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
			factory.setNamespaceAware(true);
			DocumentBuilder builder;
			try {
				builder = factory.newDocumentBuilder();
			} catch (ParserConfigurationException e) {
				throw new IOException(e.getLocalizedMessage());
			}
			try {
				document = builder.parse(inFile);
			} catch (SAXException e) {
				throw new IOException(e.getLocalizedMessage());
			}
		} finally {
			if (inFile != null) {
				inFile.close();
			}
		}
		Element corrected = correctDocumentForOldNamespace(document);
		return corrected;
	}
	
	/**
	 * Convert from the old namespace to the new namespace (with a '#'
	 * at the end).
	 * @param root the document to convert
	 * @return the converted document
	 */
	public static Element correctDocumentForOldNamespace(Document root) {
		final String BAD_VIPER = ViperData.ViPER_SCHEMA_URI.substring(0, ViperData.ViPER_SCHEMA_URI.length()-1);
		Element rootEl = root.getDocumentElement();
		if (BAD_VIPER.equals(rootEl.getNamespaceURI())) {
			rootEl = correctElement(rootEl, root);
		}
		return rootEl;
	}
	
	private static Element correctElement(Element e, Document root) {
		final String BAD_VIPER = ViperData.ViPER_SCHEMA_URI.substring(0, ViperData.ViPER_SCHEMA_URI.length()-1);
		final String BAD_DATA = ViperData.ViPER_DATA_URI.substring(0, ViperData.ViPER_DATA_URI.length()-1);
		
		Element replace = null;
		if (BAD_DATA.equals(e.getNamespaceURI())) {
			replace = root.createElementNS(ViperData.ViPER_DATA_URI, e.getTagName());
		} else if (BAD_VIPER.equals(e.getNamespaceURI())) {
			replace = root.createElementNS(ViperData.ViPER_SCHEMA_URI, e.getTagName());
		} else {
			replace = root.createElementNS(e.getNamespaceURI(), e.getTagName());
		}
		NamedNodeMap atts = e.getAttributes();
		for (int i = 0; i < atts.getLength(); i++) {
			Attr a = (Attr) atts.item(i);
			Attr ra = null;
			if (a.getNodeName().equals("xmlns")) {
				
			} else {
				if (BAD_DATA.equals(a.getNamespaceURI())) {
					ra = root.createAttributeNS(ViperData.ViPER_DATA_URI, a.getName());
				} else if (BAD_VIPER.equals(a.getNamespaceURI())) {
					ra = root.createAttributeNS(ViperData.ViPER_SCHEMA_URI, a.getName());
				} else {
					ra = root.createAttributeNS(a.getNamespaceURI(), a.getName());
				}
				ra.setNodeValue(a.getNodeValue());
				replace.setAttributeNodeNS(ra);
			}
		}
		NodeList nl = e.getChildNodes();
		for (int i = 0; i < nl.getLength(); i++) {
			Node n = nl.item(i);
			if (n.getNodeType() == Node.ELEMENT_NODE) {
				Element re = correctElement((Element) n, root);
				replace.appendChild(re);
			}
		}
		return replace;
	}

	/**
	 * Parses viper data from the given node.
	 * @param e the viper node.
	 * @return the data contained in the element
	 */
	public ViperData parseDoc(Element e) {
		if (null == e.getNamespaceURI()) {
			logger.warning("No namespace on root element. Is your DOM parser namespace aware?");
		}
		NodeList children = e.getChildNodes();
		ViperDataImpl curr = new ViperDataImpl();

		for (int i = 0; i < children.getLength(); i++) {

			Node n = children.item(i);
			if (n.getNodeType() == Node.ELEMENT_NODE) {

				Element child = (Element) children.item(i);

				String tag = n.getNodeName().trim();

				if (tag.equals("config")) {
					parseConfig(child, curr);
				} else if (tag.equals("data")) {
					parseData(child, curr);
				}
			}
		}

		return (curr);
	}

	private void parseConfig(Element e, ViperData into) {
		NodeList allDesc = e.getChildNodes();
		boolean foundInformation = false;

		for (int k = 0; k < allDesc.getLength(); k++) {

			Node t = allDesc.item(k);

			if (t.getNodeType() == Node.ELEMENT_NODE) {
				Element desc = (Element) allDesc.item(k);
				try {

					String dtype = desc.getAttribute("type");
					String dname = desc.getAttribute("name");

					int type = Util.getDescType(dtype);

					Config cfg = into.createConfig(type, dname);
					parseAttrConfigs(desc, cfg);
					if (type == Config.FILE) {
						FileInformation.initConfig(cfg);
						foundInformation = true;
					}
				} catch (NullPointerException npx) {
					logger.severe(
						"NullPointerException: Missing required XML attribute.");
				}
			}
		}
		if (!foundInformation) {
			Config cfg = into.createConfig(Config.FILE, "Information");
			FileInformation.initConfig(cfg);
		}
	}

	private void parseAttrConfig(Element att, Config into) {
		try {
			String name = "";
			String type = "";
			String flag = "";
			boolean dynamicVal = false;

			if (att.hasAttribute("name"))
				name = att.getAttribute("name");

			if (att.hasAttribute("type"))
				type = att.getAttribute("type");

			if (att.hasAttribute("dynamic")) {
				flag = att.getAttribute("dynamic");
				if (!flag.trim().equals("false"))
					dynamicVal = true;
			}

			AttrValueWrapper attrv =
				typeFactory.getAttribute(type);
			if (attrv == null) {
				String newType = ViperData.ViPER_DATA_URI + type;
				attrv = typeFactory.getAttribute(newType);
				if (attrv != null) {
					type = newType;
				} else {
					System.err.println("Cannot find parser for type: " + type);
					System.err.println("  Tried looking for : " + newType);
				}
			}

			parseGenericAttrConfig(
				att,
				name,
				type,
				dynamicVal,
				into,
				(AttrValueParser) attrv);
		} catch (NullPointerException pe) {
			logger.severe(
				"NullPointerException: Missing required XML attribute");
		}
	}

	private void parseAttrConfigs(Element elt, Config into) {
		NodeList nl = elt.getChildNodes();

		for (int r = 0; r < nl.getLength(); r++) {
			Node n = nl.item(r);

			if (n.getNodeType() == Node.ELEMENT_NODE) {
				if (n.getNodeName().equals("attribute")) {
					Element att = (Element) nl.item(r);
					parseAttrConfig(att, into);
				}
			}
		}
	}

	private void parseGenericAttrConfig(
		Element elt,
		String name,
		String type,
		boolean dynamic,
		Config into,
		AttrValueParser archetype) {
		if (archetype instanceof ExtendedAttrValueParser) {
			// TODO should setConfig take a attrConfig?
			archetype =
				(AttrValueParser)
					((ExtendedAttrValueParser) archetype).setConfig(
					elt, null);
		}
		Object def = null;
		NodeList nlist =
			elt.getElementsByTagNameNS(ViperData.ViPER_SCHEMA_URI, "default");
		for (int m = 0; m < nlist.getLength(); m++) {
			Element defEl = (Element) nlist.item(m);
			NodeList defList = defEl.getChildNodes();

			for (int k = 0; k < defList.getLength(); k++) {
				Node temp = defList.item(k);

				if (temp.getNodeType() == Node.ELEMENT_NODE) {
					Element defObj = (Element) defList.item(k);
					// TODO should setValue take a attrConfig for defaults?
					def = archetype.getObjectValue(archetype.setValue(defObj, null), null, null);
				}
			}
		}
		into.createAttrConfig(
			name,
			type,
			dynamic,
			def,
			archetype);
	}

	private void parseData(Element e, ViperData c) {
		NodeList sourcefiles = e.getChildNodes();

		for (int j = 0; j < sourcefiles.getLength(); j++) {

			Node t = sourcefiles.item(j);

			if (t.getNodeType() == Node.ELEMENT_NODE) {
				Element sourcefile = (Element) sourcefiles.item(j);
				String nameOfFile = "";

				nameOfFile = sourcefile.getAttribute("filename");

				parseSourcefile(sourcefile, nameOfFile, c);
			}
		}
	}

	private void parseSourcefile(
		Element sf,
		String fileName,
		ViperData conf) {

		NodeList files = sf.getChildNodes();
		Sourcefile col = conf.createSourcefile(fileName);

		for (int k = 0; k < files.getLength(); k++) {

			Descriptor newDesc = null;
			Node n = files.item(k);
			if (n.getNodeType() == Node.ELEMENT_NODE) {

				Element file = (Element) files.item(k);
				String readinType = file.getTagName().trim();
				int dtype;
				try {
					dtype = Util.getDescType(readinType);
				} catch (IllegalArgumentException iax) {
					logger.severe(
						"Not a valid descriptor type: " + readinType);
					continue;
				}

				String readinName = "";
				if (file.hasAttribute("name")) {
					readinName = file.getAttribute("name");
				} else {
					logger.severe("Missing name attribute");
					continue;
				}

				int tempNum = 0;
				if (file.hasAttribute("id")) {
					String num = file.getAttribute("id");
					try {
						tempNum = Integer.parseInt(num.trim());
					} catch (NumberFormatException nfe) {
						logger.severe(
							"Error in Descriptor id, this should be a number, not "
								+ num
								+ ".");
						continue;
					}
				}

				Config cfg = conf.getConfig(dtype, readinName);
				if (cfg == null) {
					logger.severe(
						"No definition found for descriptor " + readinName);
					continue;
				}
				newDesc = col.getDescriptor(dtype, readinName, tempNum);
				if (newDesc == null) {
					newDesc = col.createDescriptor(cfg, tempNum);
				}
				if (dtype != Config.FILE) {
					parseSpan(newDesc, file);
				}
				parseAttrs(file, newDesc, false);
			}
		}
	}
	
	/**
	 * Parses the data at the given node into the specified sourcefile.
	 * Useful to merge two files.
	 * @param sf the sourcefile element
	 * @param col the target sourcefile
	 */
	public void parseIntoSourcefile (Element sf, SourcefileImpl col) {
		NodeList descNodes = sf.getChildNodes();
		for (int k = 0; k < descNodes.getLength(); k++) {
			Descriptor newDesc = null;
			Node n = descNodes.item(k);
			if (n.getNodeType() == Node.ELEMENT_NODE) {
				Element descN = (Element) descNodes.item(k);
				String readinType = descN.getTagName().trim();
				int dtype;
				try {
					dtype = Util.getDescType(readinType);
				} catch (IllegalArgumentException iax) {
					logger.severe(
						"Not a valid descriptor type: " + readinType);
					continue;
				}

				String readinName = "";
				if (descN.hasAttribute("name")) {
					readinName = descN.getAttribute("name");
				} else {
					logger.severe("Missing name attribute");
					continue;
				}

				int tempNum = 0;
				if (descN.hasAttribute("id")) {
					String num = descN.getAttribute("id");
					try {
						tempNum = Integer.parseInt(num.trim());
					} catch (NumberFormatException nfe) {
						logger.severe(
							"Error in Descriptor id, this should be a number, not "
								+ num
								+ ".");
						continue;
					}
				}

				Config cfg = null;
				for (Config curCfg : col.getRoot().getAllConfigs()) {
					if (curCfg.getDescName().equals(readinName)
						&& curCfg.getDescType() == dtype) {
						cfg = curCfg;
						break;
					}
				}
				if (cfg == null) {
					logger.severe(
						"No definition found for descriptor " + readinName);
					continue;
				}
				if (col.getDescriptor(cfg.getDescType(), cfg.getDescName(), tempNum) != null) {
					logger.warning("Duplicate id: " +tempNum);
					tempNum = col.findFreeIdFor(cfg);
				}
				newDesc = col.createDescriptor(cfg, tempNum);
				if (cfg.getDescType() != Config.FILE) {
					parseSpan(newDesc, descN);
				}

				parseAttrs(descN, newDesc, false);
			}
		}
	}

	private void parseSpan(Descriptor desc, Element elem) {
		try {
			InstantRange frameRange = null;
			InstantRange timeRange = null;
			InstantRange oldRange = desc.getValidRange();
			if (elem.hasAttribute("framespan") && elem.hasAttribute("timespan")) {
				String fRange = elem.getAttribute("framespan");
				frameRange = InstantRange.parseFrameRange(fRange);
				String tRange = elem.getAttribute("timespan");
				timeRange = InstantRange.parseTimeRange(tRange);
				MediaElement rm = desc.getSourcefile().getReferenceMedia();
				FrameRate oldRate = rm.getFrameRate();
				Interval frameSpan = frameRange.getExtrema();
				Interval timeSpan = timeRange.getExtrema();
				FrameRate startRate =
					getFrameRate(
						(Frame) frameSpan.getStart(),
						(Time) timeSpan.getStart());
				FrameRate endRate =
					getFrameRate(
						(Frame) frameSpan.getEnd(),
						(Time) timeSpan.getEnd());
				FrameRate rate = endRate;
				if (!close(startRate, endRate)) {
					logger.warning(
						"Warning: descriptor w/ timespan & framespan that don't align");
				} else if (oldRate == null) {
					rm.setFrameRate(rate);
				} else if (!close(rate, oldRate)) {
					rm.setFrameRate(rate);
					logger.warning(
						"Frame rate is off: old rate was "
							+ oldRate
							+ ", but computed rate is "
							+ rate);
				}
			} else if (elem.hasAttribute("framespan")) {
				String fRange = elem.getAttribute("framespan");
				frameRange = InstantRange.parseFrameRange(fRange);
			} else if (elem.hasAttribute("timespan")) {
				String tRange = elem.getAttribute("timespan");
				timeRange = InstantRange.parseTimeRange(tRange);
			} else {
				logger.severe ("could not find range for " + desc);
				return;
			}
			if (!oldRange.isEmpty()) {
				if (oldRange.isFrameBased()) {
					if (frameRange == null) {
						MediaElement rm = desc.getSourcefile().getReferenceMedia();
						FrameRate rate = rm.getFrameRate();
						frameRange = new InstantRange();
						for (Iterator iter = timeRange.iterator(); iter.hasNext(); ) {
							InstantInterval curr = (InstantInterval) iter.next();
							frameRange.add(rate.asFrame(curr));
						}
					}
					frameRange.addAll((IntervalIndexList) oldRange);
					desc.setValidRange(frameRange);
				} else {
					if (timeRange == null) {
						MediaElement rm = desc.getSourcefile().getReferenceMedia();
						FrameRate rate = rm.getFrameRate();
						timeRange = new InstantRange();
						for (Iterator iter = frameRange.iterator(); iter.hasNext(); ) {
							InstantInterval curr = (InstantInterval) iter.next();
							timeRange.add(rate.asTime(curr));
						}
					}
					timeRange.addAll((IntervalIndexList) oldRange);
					desc.setValidRange(timeRange);
				}
			} else {
				desc.setValidRange(frameRange != null ? frameRange : timeRange);
			}
		} catch (IllegalArgumentException iax) {
			logger.log(Level.SEVERE, "Format error in descriptor " + desc, iax);
		}
	}

	/**
	 * Returns a FrameRate object that represents f Frames for every t
	 * time units.
	 * @param f the number of frames per t time units
	 * @param t the number of time units
	 * @return FrameRate
	 */
	public RationalFrameRate getFrameRate(Frame f, Time t) {
		return new RationalFrameRate(((double) f.getFrame()) / t.getTime());
	}
	/**
	 * Determines if frame rates are 'close'. XXX ugly hack
	 * @param a
	 * @param b
	 * @return <code>true</code> iff the numbers are within a small 
	 *          number (currently 1E-5)
	 */
	private boolean close(FrameRate a, FrameRate b) {
		Frame f = new Frame(10000);
		if (!a.asTime(f).equals(b.asTime(f))) {
			return false;
		}
		Time t = new Time(10000);
		if (!a.asFrame(t).equals(b.asFrame(t))) {
			return false;
		}
		return true;
	}

	/**
	 * Takes the attribute defined in the XML DOM element elem that is 
	 * attached to Descriptor d with ViperData V, which may or may not
	 * be default and has the Span given in span.
	 * @param elem the xml element containing the attributes
	 * @param d the descriptor to attach the attributes to
	 * @param isDefault 
	 */
	private void parseAttrs(
		Element elem,
		Descriptor d,
		boolean isDefault) {

		NodeList children = elem.getChildNodes();

		for (int i = 0; i < children.getLength(); i++) {
			Node a = children.item(i);

			if (a.getNodeType() == Node.ELEMENT_NODE) {
				if (a.getNodeName().trim().equals("attribute")) {
					// For each attribute node
					parseAttr((Element) children.item(i), d, isDefault);
				}
			}
		}
	}

	private void parseAttr(Element el, Descriptor d, boolean isDef) {
		try {
			String name = el.getAttribute("name");
			Config cfg = d.getConfig();
			AttrConfig currAC = cfg.getAttrConfig(name);
			if (currAC == null) {
				logger.severe(
					"Cannot find attribute config of name: '" + name + "'");
				return;
			}

			NodeList dataChildren = el.getChildNodes();

			Object attrObj = null;
			Instant start;
			if (d.getDescType() == Config.FILE) {
				start = new Frame(0);
			} else if (d.getValidRange().isEmpty()){
				// no dynamic attributes may be parsed
				start = null;
			} else {
				start = (Instant) d.getValidRange().getExtrema().getStart();
			}
			for (int j = 0; j < dataChildren.getLength(); j++) {
				// for each data element
				attrObj = null;
				Node n = dataChildren.item(j);

				if (n.getNodeType() == Node.ELEMENT_NODE) {
					Element dataChild = (Element) n;

					String attType = dataChild.getLocalName();

					if (!attType.equals("null")) {
						if (currAC.getParams() instanceof AttrValueParser) {
							AttrValueParser pav =
								(AttrValueParser) currAC.getParams();
							try {
								attrObj = pav.setValue(dataChild, d.getAttribute(currAC));
							} catch (IllegalArgumentException badx) {
								logger.log(Level.SEVERE, "Cannot parse attribute value", badx);
							}
						} else {
							logger.severe(
								"Cannot parse attributes of type: "
									+ currAC.getAttrType());
						}
					}
					Span currSpan = null;
					try {
						if (dataChild.hasAttribute("framespan")) {
							currSpan =
								Span.parseFrameSpan(
									dataChild.getAttribute("framespan"));
						} else if (dataChild.hasAttribute("timespan")) {
							currSpan =
								Span.parseTimeSpan(
									dataChild.getAttribute("timespan"));
						} else if (dataChild.hasAttribute("span")) {
							String str = dataChild.getAttribute("span");
							long change = Long.parseLong(str);
							currSpan = new Span(start, start.go(change));
						} else if (start != null) {
							currSpan = new Span(start, (Instant) start.next());
						}
						if (currSpan != null) {
							start = (Instant) currSpan.getEnd();
						}
						if (!currAC.isDynamic()) {
							d.getAttribute(currAC).setAttrValue(attrObj);
						} else {
							d.getAttribute(currAC).setAttrValueAtSpan(
								attrObj,
								currSpan);
						}
					} catch (IllegalArgumentException iax) {
						logger.log(Level.SEVERE, "Format error while parsing attribute", iax);
					}
				}
			}
		} catch (NullPointerException npe) {
			logger.log(Level.SEVERE, "Missing important XML attribute.", npe);
		}
	}
}
