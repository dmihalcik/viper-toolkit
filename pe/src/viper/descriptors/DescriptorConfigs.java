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
public class DescriptorConfigs extends DescVector  { //implements viper.api.Config
	/**
	 * Constructs an empty set of DescriptorConfigs.
	 * @param parent the parent data
	 */
	public DescriptorConfigs(DescriptorData parent) {
		super(parent);
	}

	/**
	 * Reads in a set of Desxcriptor configuration information to this
	 * DescVector.
	 * @param allFiles a Vector of Strings containing the names
	 *            of the files to search for config information
	 */
	public void parseConfig(Vector allFiles) {
		VReader reader = new VReader(allFiles, "CONFIG");
		addDesConfig(reader);
		try {
			reader.close();
		} catch (IOException iox) {
			reader.printWarning("Error in closing files");
		}
	}

	/**
	 * Reads in a set of Desxcriptor configuration information to this
	 * DescVector.
	 * @param allFiles a Vector of Strings containing the names
	 *            of the files to search for config information
	 * @param all print out all lines
	 * @param err print out error messages
	 * @param warn print out warning messages
	 * @param bad print out general errors
	 * @param totals print out error totals
	 */
	public void parseConfig(
		Vector allFiles,
		boolean all,
		boolean err,
		boolean warn,
		boolean bad,
		boolean totals) {
		VReader reader = new VReader(allFiles, "CONFIG");
		reader.changeSwitches(all, err, warn, bad, totals);
		addDesConfig(reader);
		try {
			reader.close();
		} catch (IOException iox) {
			reader.printWarning("Error in closing files");
		}
	}

	/**
	 * Reads in Descriptor configuration information from the specified VReader.
	 * @param reader - the VReader containing the config info
	 * @return true if some info was read in
	 */
	public boolean addDesConfig(VReader reader) {
		boolean status = reader.advanceToBeginDirective("CONFIG");

		if (!status) {
			reader.printGeneralError("Unable to find BEGIN_CONFIG directive");
			return (false);
		}
		try {
			reader.gotoNextRealLine();
		} catch (IOException iox) {
			reader.printGeneralError(
				"Configuration Parser: Unknown Java IO Exception");
			return (false);
		}

		Descriptor nEl = Descriptor.parseDescriptorConfig(reader);
		while (nEl != null) {
			addElement(nEl);
			nEl = Descriptor.parseDescriptorConfig(reader);
		}
		return (true);
	}

	/**
	 * Adds the descriptor prototype described by
	 * the given XML DOM node.
	 * @param configEl the dom node (a config node)
	 * @return the node was successfully parsed
	 */
	public boolean addDesConfig(Element configEl) {
		NodeList configs =
			configEl.getElementsByTagNameNS(
				DescriptorData.NAMESPACE_URI,
				"descriptor");
		boolean some = false;
		for (int i = 0; i < configs.getLength(); i++) {
			try {
				addElement(
					Descriptor.parseDescriptorConfig(
						(Element) configs.item(i)));
				some = true;
			} catch (BadDataException bdx) {
				System.err.println(bdx.getMessage());
			}
		}
		return some;
	}

	/**
	 * Adds the descriptor schema from the given xml input source.
	 * @param input the input source
	 * @return if any descriptor definitions were found
	 * @throws BadDataException
	 * @throws IOException
	 */
	public boolean addDesConfig(InputSource input)
		throws BadDataException, IOException {
		DOMParser parser = new DOMParser();
		try {
			parser.setFeature(
				"http://apache.org/xml/features/validation/schema",
				false);
		} catch (SAXException snrx) {
			// parser does not support schemata
		}
		try {
			parser.parse(input);
		} catch (SAXException saxx) {
			String fname = input.getPublicId();
			if (fname == null) {
				throw new BadDataException(
					"Problem in parsing XML: " + saxx.getMessage());
			} else {
				throw new BadDataException(
					"Problem in parsing " + fname + ": " + saxx.getMessage());
			}
		}
		Document document = parser.getDocument();
		NodeList nodes =
			document.getElementsByTagNameNS(
				DescriptorData.NAMESPACE_URI,
				"config");
		if (nodes.getLength() < 1) {
			throw new BadDataException("config element missing.");
		} else if (nodes.getLength() > 1) {
			throw new BadDataException("Too many config elements.");
		}

		return addDesConfig((Element) nodes.item(0));
	}

	/**
	 * Reads in the next Descriptor from a VReader. Looks 
	 *   through and finds an appropriate Descriptor and 
	 *   returns a new Descriptor with the specified information.
	 * @param reader   where to get the Descriptor from
	 * @param relativeVector constructs them using this
	 * @return a new Descriptor filled with data from the file
	 * @throws BadDataException   if the data is in an incorrect format.
	 * @throws EndOfBlockException   if there is a data block overrun in the VReader
	 * @throws ImproperDescriptorException   if there is no such data found in the DescriptorConfigs
	 */
	public Descriptor addFromGtf(VReader reader, DescVector relativeVector)
		throws BadDataException, EndOfBlockException, ImproperDescriptorException {
		if (reader.currentLineIsEndDirective()) {
			throw (new EndOfBlockException());
		}

		int i = 0;
		try {
			while (i < size()) {
				try {
					return ((DescPrototype) get(i)).parseDescriptorData(
						reader,
						relativeVector);
				} catch (ImproperDescriptorException idx) {
					i++;
				}
			}
		} catch (CloneNotSupportedException cnsx) {
			cnsx.printStackTrace();
		}
		throw (
			new ImproperDescriptorException("Descriptor information for line not found in config file"));
	}

	/**
	 * Parses the given descriptor instance element.
	 * @param curr the descriptor instance
	 * @param relativeVector the parent list
	 * @return the parsed descriptor
	 * @throws BadDataException
	 * @throws ImproperDescriptorException
	 */
	public Descriptor addFromGtf(Element curr, DescVector relativeVector)
		throws BadDataException, ImproperDescriptorException {
		String name = curr.getAttribute("name");
		String type = curr.getTagName().toUpperCase();
		if (name == null) {
			throw new BadDataException("No name for descriptor");
		}
		if (!Descriptor.isCategory(type)) {
			throw new BadDataException(
				"Not an acceptable category for a descriptor: " + type);
		}

		for (Iterator iter = iterator(); iter.hasNext();) {
			DescPrototype protos = (DescPrototype) iter.next();
			if ((protos.named(name))
				&& (protos.getType().equalsIgnoreCase(type))) {
				return protos.parseDescriptorData(curr, relativeVector);
			}
		}
		throw new ImproperDescriptorException(
			"Descriptor \"" + type + " " + name + "\" not in config.");
	}

	/**
	 * Prints out all Descriptor objects in the list.
	 * @param output   where to print the information
	 */
	public void printConfig(PrintWriter output) {
		System.out.println(size());
		for (int i = 0; i < size(); i++)
			//      if((( Descriptor )elementAt( i )).inScope())
			output.println(elementAt(i));
	}
	/**
	 * Prints the data out to a file in the specified format.
	 * @param output - the PrintWriter to hand the data
	 */
	public void printOut( PrintWriter output )
	{
	  output.println( "#BEGIN_CONFIG" );
	  for( int t=0; t<size(); t++ )
		output.println( elementAt( t ) + "\n" );
	  output.println( "#END_CONFIG" );
	}

	/**
	 * Prints the descriptors in .gtf style.
	 * @return the .gtf data. Contains newlines.
	 */
	public String toString() {
		StringBuffer temp = new StringBuffer();
		temp.append("#BEGIN_CONFIG\n");
		for (int i = 0; i < size(); i++) {
			temp.append(get(i)).append("\n");
		}
		temp.append("#END_CONFIG\n");
		return temp.toString();
	}

	/**
	 * Returns a config tag with lots of children. 
	 * @param root The DOM root
	 * @return &lt;config&gt; element, with descriptor config children
	 */
	public Element getXMLFormat(Document root) {
		Element el = root.createElement("config");
		for (Iterator descs = iterator(); descs.hasNext();) {
			el.appendChild(((Descriptor) descs.next()).getXMLFormat(root));
		}
		return el;
	}
}
