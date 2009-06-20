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

import viper.comparison.*;
import viper.descriptors.attributes.*;
import viper.filters.*;
import edu.umd.cfar.lamp.viper.util.*;
import edu.umd.cfar.lamp.viper.util.reader.*;

/**
 * The DescPrototype is a descriptor object that does not contain data and
 * supports
 */
public class DescPrototype extends Descriptor {
	private Vector attrs = new Vector();
	private int count = 0;

	/**
	 * Gets the names of all attributes instances of this descriptor type may
	 * have.
	 * 
	 * @return the attribute names, as Strings.
	 */
	public Set getAttribNames() {
		HashSet names = new HashSet(attrs.size());
		for (int i = 0; i < attrs.size(); i++) {
			names.add(((AttributePrototype) attrs.get(i)).getName());
		}
		return names;
	}

	/**
	 * Gets that specification for the attribute with the given name.
	 * 
	 * @param name
	 *            the name of that attribute to look up
	 * @return type information of the named attributed
	 */
	public AttributePrototype getAttributePrototype(String name) {
		for (int i = 0; i < attrs.size(); i++) {
			if (((AttributePrototype) attrs.get(i)).getName().equals(name)) {
				return (AttributePrototype) attrs.get(i);
			}
		}
		throw new NoSuchElementException("Descriptor does not have attribute: "
				+ name);
	}

	/**
	 * Constructs a new descriptor definition.
	 * 
	 * @param category
	 *            the category, e.g. "CONTENT" or "OBJECT"
	 * @param name
	 *            the name of the descriptor, e.g. "Person"
	 * @throws BadDataException
	 *             if the content is unknown or if the name is bad
	 */
	public DescPrototype(String category, String name) throws BadDataException {
		super(category);
		span = null;
		setName(name);
	}

	/**
	 * @inheritDoc
	 */
	public boolean hasAttrib(String attribName) {
		for (int i = 0; i < attrs.size(); i++) {
			if (((AttributePrototype) attrs.get(i)).named(attribName)) {
				return true;
			}
		}
		return false;
	}

	/**
	 * Adds a new attribute with the given type information.
	 * 
	 * @param name
	 *            the name of the new attribute
	 * @param type
	 *            the data type of the new attribute, e.g. viper:bbox.
	 * @throws BadDataException
	 */
	public void addAttribute(String name, String type) throws BadDataException {
		AttributePrototype temp = AttributePrototype.createAttributePrototype(
				type, name, getType().equalsIgnoreCase("object"));
		attrs.add(temp);
	}

	/**
	 * @inheritDoc
	 */
	public Filterable getFilterable(String attribName) {
		for (Iterator iter = attrs.iterator(); iter.hasNext();) {
			AttributePrototype curr = (AttributePrototype) iter.next();
			if (curr.getName().equals(attribName)) {
				return curr.create();
			}
		}
		return null;
	}

	/**
	 * Adds the given attribute with the specified parameters.
	 * 
	 * @param name
	 *            the name of the new attribute
	 * @param type
	 *            the type information for the attribute
	 * @param extra
	 *            extra information, such as the elements of an lvalue list
	 * @throws BadDataException
	 *             if there is an error in the type or the extras config
	 *             information
	 */
	public void addAttribute(String name, String type, String extra)
			throws BadDataException {
		AttributePrototype temp = AttributePrototype.createAttributePrototype(
				type, name, getType().equalsIgnoreCase("object"));
		temp.setArchetype(extra);
		attrs.add(temp);
	}

	/**
	 * Adds the given attribute type.
	 * 
	 * @param ap
	 *            the attribute type and name information
	 */
	public void addAttribute(AttributePrototype ap) {
		attrs.add(ap);
	}

	/**
	 * Gives the prototype in gtf format.
	 * 
	 * @return {@inheritDoc}
	 */
	public String toString() {
		StringBuffer sb = new StringBuffer().append(getCategory());
		sb.append(' ').append(getName());
		for (Iterator iter = attrs.iterator(); iter.hasNext();) {
			AttributePrototype curr = (AttributePrototype) iter.next();
			sb.append("\n   ").append(
					curr.toString("OBJECT".equals(getCategory()), false));
		}
		return sb.append('\n').toString();
	}

	/**
	 * @inheritDoc
	 */
	public Element getXMLFormat(Document root) {
		Element el = root.createElement("descriptor");
		el.setAttribute("name", getName());
		el.setAttribute("type", getCategory());
		for (Iterator iter = attrs.iterator(); iter.hasNext();) {
			el.appendChild(((AttributePrototype) iter.next())
					.getXMLFormat(root));
		}
		return el;
	}

	/**
	 * Creates a new, empty descriptor from this type information.
	 * 
	 * @return a new instance of this type of descriptor
	 */
	public Descriptor create() {
		DescSingle temp;
		try {
			temp = new DescSingle(getCategory(), getName());
		} catch (BadDataException bdx) {
			throw new IllegalStateException(bdx.getMessage());
		}

		int i = 0;
		temp.attributes = new Attribute[attrs.size()];
		for (Iterator iter = attrs.iterator(); iter.hasNext();) {
			temp.attributes[i++] = ((AttributePrototype) iter.next()).create();
		}

		temp.id = count++;
		temp.span = (span == null) ? null : (FrameSpan) span.clone();
		return temp;
	}

	/**
	 * Returns the ID number of the descriptor.
	 * 
	 * @return the ID number of the descriptor
	 */
	public Object getID() {
		return new Integer(0);
	}

	/**
	 * @inheritDoc
	 */
	public void setID(int id) {
		count = id;
	}

	/**
	 * @inheritDoc
	 * @return zero
	 */
	public int numIDs() {
		return 0;
	}

	/**
	 * @inheritDoc
	 * @return <code>null</code>
	 */
	public FrameSpan getFrameSpan() {
		return null;
	}

	/**
	 * Prototypes are not instantiated, so do not have frame spans; this method
	 * throws an exception.
	 * 
	 * @param span
	 *            the new span
	 * @throws UnsupportedOperationException
	 *             This method isn't implemented, as it doesn't really apply to
	 *             prototypes.
	 */
	public void setFrameSpan(FrameSpan span) {
		throw new UnsupportedOperationException(
				"Cannot set FrameSpan of a Prototype.");
	}

	/**
	 * Cannot compose prototypes
	 * 
	 * @param D
	 *            the descriptor to compose with
	 * @param scope
	 *            the scoping rules
	 * @return nothing; is an error
	 * @throws UnsupportedOperationException
	 *             This method isn't implemented, as it doesn't really apply to
	 *             prototypes.
	 */
	public Descriptor compose(Descriptor D,
			EvaluationParameters.ScopeRules scope)
			throws UnsupportedOperationException {
		throw new UnsupportedOperationException("Cannot compose Prototypes.");
	}

	/**
	 * @inheritDoc
	 */
	public Object clone() {
		DescPrototype temp;
		try {
			temp = new DescPrototype(getCategory(), getName());
		} catch (BadDataException bdx) {
			throw new IllegalStateException(bdx.getMessage());
		}

		temp.attrs = new Vector(attrs.size());
		for (Iterator iter = attrs.iterator(); iter.hasNext();) {
			temp.attrs.add(((AttributePrototype) iter.next()).clone());
		}

		temp.count = count;
		temp.span = (span == null) ? null : (FrameSpan) span.clone();
		return temp;
	}

	/**
	 * Reads in Descriptor data filtered through this Descriptor's config
	 * information.
	 * 
	 * @param reader
	 *            the reader from which to retrieve the next Descriptor data
	 * @param relativeVector
	 *            the configuration/rules to use are here
	 * @return a new Descriptor with data from the VReader
	 * @throws CloneNotSupportedException
	 * @throws BadDataException
	 */
	public Descriptor parseDescriptorData(VReader reader,
			DescVector relativeVector) throws CloneNotSupportedException,
			BadDataException {
		//First, check out the description
		Descriptor newDes = null;
		String line = reader.getCurrentLine();
		if (line == null) {
			throw new BadDataException("No line found");
		}
		CountingStringTokenizer st = new CountingStringTokenizer(line);
		String S;
		try {
			S = st.nextToken();
		} catch (NoSuchElementException nsex) {
			throw (new BadDataException("Unable to find Descriptor category.",
					st.getStart(), st.getEnd()));
		}
		if (!this.getCategory().equals(S)) {
			if (Descriptor.isCategory(S))
				throw (new ImproperDescriptorException(S
						+ " is not the proper category"
						+ " for this descriptor string", st.getStart(), st
						.getEnd()));
			else
				throw (new BadDataException(
						S + " is not a Descriptor category", st.getStart(), st
								.getEnd()));
		}
		newDes = create();
		try {
			S = st.nextToken();
		} catch (NoSuchElementException nsex) {
			throw (new BadDataException("Unable to find Descriptor name.", st
					.getStart(), st.getEnd()));
		}
		if (!newDes.named(S))
			throw (new ImproperDescriptorException(S
					+ " is not the proper name for this" + " descriptor string"));
		if (!getType().equalsIgnoreCase("FILE")) {
			try {
				newDes.setID(Integer.parseInt(st.nextToken()));
			} catch (NoSuchElementException nsex) {
				throw (new BadDataException(
						"Missing Descriptor identification number.", st
								.getStart(), st.getEnd()));
			} catch (NumberFormatException nfx) {
				throw new BadDataException(
						"Not a Descriptor identification number.", st
								.getStart(), st.getEnd());
			}
			try {
				newDes.setFrameSpan(FrameSpan.parseFrameSpan(line.substring(st
						.getEnd())));
			} catch (NoSuchElementException nsex) {
				throw new BadDataException("Missing frame range.", st
						.getStart(), st.getEnd());
			}
		}
		try {
			reader.gotoNextRealLine();
		} catch (IOException iox) {
			reader.printError("Unexpected end of data");
		}

		//Read in Attribute data
		int i = 0;
		int count = 0;
		HashSet found = new HashSet();
		while ((!reader.currentLineIsEndDirective())
				&& (!reader.currentLineIsDescriptor())) {
			line = reader.getCurrentLine();
			String attribNameString;
			String attribValueString = "NULL";
			int colonOffset = line.indexOf(':');
			if (colonOffset < 0) {
				line = line.trim();
				colonOffset = line.indexOf(' ');
				if (colonOffset < 0) {
					throw (new BadDataException(
							"Unable to find useful data on line.", reader
									.getCurrentLine().indexOf(line), reader
									.getCurrentLine().length()));
				} else {
					reader.printWarning("No ':' in definition",
							colonOffset - 1, colonOffset + 1);
				}
			}
			attribNameString = line.substring(0, colonOffset).trim();
			attribValueString = line.substring(colonOffset + 1).trim();
			count++;
			found.add(attribNameString);
			if (!attribValueString.equalsIgnoreCase("NULL")) {
				int j = 0;
				while ((!newDes.attributes[i].getName()
						.equals(attribNameString))
						&& (j < newDes.attributes.length)) {
					j++;
					i++;
					i = (i < newDes.attributes.length) ? i : 0;
				}
				if (newDes.attributes[i].getName().equals(attribNameString)) {
					newDes.attributes[i].setValue(attribValueString, reader,
							newDes.getFrameSpan(), relativeVector);
				} else {
					reader.printError(attribNameString
							+ " not an attribute of " + newDes.getName());
				}
			}
			try {
				reader.gotoNextRealLine();
			} catch (IOException iox) {
				reader.printError("Unexpected end of data");
			}
		}
		if (count < attrs.size()) {
			String tempString = "";
			for (Iterator iter = attrs.iterator(); iter.hasNext();) {
				String curr = ((AttributePrototype) iter.next()).getName();
				if (!found.contains(curr)) {
					tempString += " " + curr;
				}
			}
			reader.printWarning("The following attributes not specified:"
					+ tempString);
		}
		return newDes;
	}

	/**
	 * Reads in the data in xml format.
	 * 
	 * @param myElement
	 *            the xml element to parse
	 * @param relativeVector
	 *            the parsed data, so far
	 * @return the new descriptor
	 * @throws BadDataException
	 *             if there is an error in the file
	 */
	public Descriptor parseDescriptorData(Element myElement,
			DescVector relativeVector) throws BadDataException {
		if (!this.getCategory().equalsIgnoreCase(myElement.getTagName())) {
			throw new ImproperDescriptorException(myElement.getTagName()
					+ " is not the proper category"
					+ " for this descriptor string");
		}

		//First, check out the description
		Descriptor newDes = null;

		String S = null;
		S = myElement.getAttribute("name");
		if (S == null) {
			throw new BadDataException("Unable to find Descriptor name.");
		}
		if (!S.equals(getName())) {
			throw new ImproperDescriptorException(S
					+ " is not the proper name for this descriptor string");
		}

		newDes = create();

		if (!getType().equalsIgnoreCase("FILE")) {
			//Descriptor is OBJECT or CONTENT type
			try {
				newDes.setID(Integer.parseInt(myElement.getAttribute("id")));
			} catch (NullPointerException npx) {
				throw new BadDataException(
						"Missing Descriptor identification number.");
			}
			try {
				newDes.setFrameSpan(FrameSpan.parseFrameSpan(myElement
						.getAttribute("framespan")));
			} catch (NullPointerException npx) {
				throw new BadDataException("Unable to find frame numbers");
			}
		}

		//Read in Attribute data
		NodeList someAttrs = myElement.getElementsByTagName("attribute");
		int offset = 0;
		for (int i = 0; i < someAttrs.getLength(); i++) {
			Element currAttr = (Element) someAttrs.item(i);

			String attribNameString = currAttr.getAttribute("name");

			count++;
			int j = 0;
			while ((!newDes.attributes[offset].getName().equals(
					attribNameString))
					&& (j < newDes.attributes.length)) {
				j++;
				offset++;
				offset = (offset < newDes.attributes.length) ? offset : 0;
			}
			if (newDes.attributes[offset].getName().equals(attribNameString)) {
				newDes.attributes[offset].setValue(currAttr, newDes
						.getFrameSpan(), relativeVector);
			} else {
				System.err.println("No attributes named " + attribNameString
						+ " in descriptor " + getName());
			}
		}
		return newDes;
	}

	/**
	 * Does nothing.
	 * @inheritDoc
	 */
	public Descriptor crop(FrameSpan span) {
		return (Descriptor) this.clone();
	}

	/**
	 * @inheritDoc
	 * @throws UnsupportedOperationException
	 */
	public void moveFrame(int offset) {
		throw new UnsupportedOperationException();
	}
}