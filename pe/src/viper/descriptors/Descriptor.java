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
 * This class represents a Descriptor of a given visual element in a 
 * video file. There are three types: FILE, CONTENT, and OBJECT.
 */
public abstract class Descriptor implements Cloneable {
	private String category;
	
	/**
	 * Sets the descriptor type, e.g. "CONTENT" or "FILE".
	 * @param s the new category
	 * @throws IllegalArgumentException if the category isn't valid
	 */
	public void setCategory(String s) throws IllegalArgumentException {
		if (!isCategory(s)) {
			throw new IllegalArgumentException(
					"Not a valid Descriptor category (eg OBJECT): " + s);
		}
		category = s;
	}

	private String name;

	/**
	 * The attributes. Remember -- for all attributes of the same type, they
	 * must remain sorted in the same order as the original Config.
	 */
	protected Attribute[] attributes = null;

	protected String croppingType = null;

	protected FrameSpan span;

	protected boolean composable = true;

	/***************************************************************************
	 * ------------------------------------------------------------------------
	 * ------------------------------------------------------------------------
	 */

	/**
	 * This constructor takes in the type of Descriptor to create.
	 * 
	 * @param designation
	 *            the type of Descriptor. Either FILE, CONTENT, or OBJECT
	 * @exception BadDataException -
	 *                if the specified category is not one of the three
	 *                allowable
	 */
	public Descriptor(String designation) throws BadDataException {
		span = null;
		if (!Descriptor.isCategory(designation)) {
			category = "OBJECT";
			throw (new BadDataException("Bad descriptor category -- "
					+ designation));
		}
		category = designation;
	}

	/**
	 * Constructs a new, empty descriptor.
	 */
	protected Descriptor() {
		category = "OBJECT";
		span = null;
	}

	/**
	 * Function resturns true if one of the attributes was ROC.
	 * 
	 * @return if one of the attributes was ROC
	 */
	public boolean isRoc() {
		if (attributes != null) {
			int i = 0;
			while (i < attributes.length) {
				i++;
			}
		}
		return false;
	}

	/**
	 * Function returns the name of the roc attribute or "" if not found
	 * 
	 * @return the name of the roc attribute or "" if not found
	 */
	public String rocAttributeName() {
		if (attributes != null) {
			int i = 0;
			while (i < attributes.length) {
				i++;
			}
		}
		return "";
	}

	/**
	 * Function returns the index of the named attribute or -1 if not found.
	 * 
	 * @param name
	 *            the name to look for
	 * @return the index of the attribute
	 */
	public int getAttributeIndex(String name) {
		if (attributes != null)
			for (int i = 0; i < attributes.length; i++)
				if (attributes[i].named(name))
					return i;
		return -1;
	}

	/**
	 * Gets the index of the attribute with the given name, 
	 * under the given ontology mapping.
	 * @param name the attribute to look for
	 * @param map the mapping to use. It will also check the original
	 * name.
	 * @return the index, or -1 if not found
	 */
	public int getAttributeIndex(String name, Equivalencies map) {
		if (attributes != null)
			for (int i = 0; i < attributes.length; i++)
				if (attributes[i].named(name, map))
					return i;
		return -1;
	}


	/**
	 * Tests to see if the String is a Descriptor category.
	 * 
	 * @param test -
	 *            the string to be tested
	 * @return true iff the string is either FILE, CONTENT, or OBJECT
	 */
	public static boolean isCategory(String test) {
		return ((test.equals("OBJECT")) || (test.equals("CONTENT")) || (test
				.equals("FILE")));
	}

	/***************************************************************************
	 * ------------------------------------------------------------------------
	 * ------------------------------------------------------------------------
	 */
	/**
	 * Tests to see if a String starts with a proper Descriptor category
	 * 
	 * @param test
	 *            String to be tested; usually a line of text
	 * @return true iff the string starts with "FILE ", "CONTENT ", or "OBJECT "
	 */
	public static boolean startsWithCategory(String test) {
		return ((test.startsWith("OBJECT ")) || (test.startsWith("CONTENT ")) || (test
				.startsWith("FILE ")));
	}

	/***************************************************************************
	 * ------------------------------------------------------------------------
	 * ------------------------------------------------------------------------
	 */
	/**
	 * Generates a new Descriptor Object sharing none of the references of the
	 * original but containing identical data.
	 * 
	 * @return A new Descriptor initialized with this Descriptor's data
	 */
	public abstract Object clone();

	/**
	 * This compares the Category and Name fields. Useful for sorting.
	 * 
	 * @param o the Descriptor to compare this with
	 * @return 0 if the names are equal, and less than or greater
	 *         lexicographically
	 * @throws ClassCastException
	 */
	public int compareTo(Object o) {
		if (!o.getClass().getName().equals(this.getClass().getName()))
			throw (new ClassCastException());
		return ((category + name).compareTo(((Descriptor) o).category
				+ ((Descriptor) o).name));
	}

	/**
	 * Tests to see if the descriptors refer to the same type
	 * under the given ontology mapping.
	 * @param other the descriptor to compare with
	 * @param map the type map
	 * @return if the two are the same type
	 */
	public boolean sameCategoryAs(Descriptor other, Equivalencies map) {
		// XXX what is going on here?
		name.toString();
		other.name.toString();
		map.toString();
		return category.equals(other.category)
				&& (name.equals(other.name) || map.eq(name, other.name) || map
						.eq(other.name, name));
	}

	/**
	 * Checks to see if this Descriptor has an attribute with the specifified
	 * name.
	 * 
	 * @param attribName
	 *            the attribute to check for
	 * @return true iff the attribute is found.
	 */
	public boolean hasAttrib(String attribName) {
		for (int i = 0; i < attributes.length; i++)
			if (attributes[i].named(attribName))
				return (true);
		return (false);
	}

	/**
	 * Returns the type of the Attribute named. If not found, null is returned.
	 * 
	 * @param attribName
	 *            the attribute to check for
	 * @return the type of the Attribute named; null if not found
	 */
	String getSelfType(String attribName) {
		for (int i = 0; i < attributes.length; i++)
			if (attributes[i].named(attribName))
				return (attributes[i].getType());
		return (null);
	}

	/**
	 * @return the ID number(s) of the descriptor
	 */
	public abstract Object getID();

	abstract void setID(int id);

	/**
	 * Returns the number of Identification numbers in this descriptor.
	 * Prototypes or empty descriptors have none, while aggregate
	 * descriptors have as many as there are descriptors inside.
	 * @return the number of Identification numbers in this descriptor
	 */
	public abstract int numIDs();

	/**
	 * Gets the frames where the descriptor exists.
	 * @return the valid frames of the descriptor
	 */
	public abstract FrameSpan getFrameSpan();

	/**
	 * Shifts the descriptor by the given offset.
	 * @param offset the number of frames to shift 
	 * the descriptor.
	 */
	public abstract void moveFrame(int offset);

	/**
	 * Replaces the frame span. Probably doesn't 
	 * modify any of the attributes.
	 * @param span the new span
	 */
	public abstract void setFrameSpan(FrameSpan span);

	/**
	 * Like clone, but only clones a subset of this descriptor.
	 * 
	 * @param span
	 *            the new span
	 * @return the cropped descriptor
	 */
	public abstract Descriptor crop(FrameSpan span);

	/**
	 * Returns the specified Attribute.
	 * 
	 * @param attribName -
	 *            the name of the Attribute to get
	 * @return the Attribute; null if not found
	 */
	public Attribute getAttribute(String attribName) {
		for (int i = 0; i < attributes.length; i++)
			if (attributes[i].named(attribName))
				return (attributes[i]);
		return (null);
	}

	/**
	 * Gets the attribute with the given name.
	 * @param attribName the attribute name
	 * @param map the ontology map
	 * @return the attribute, or <code>null</code> if not found
	 */
	public Attribute getAttribute(String attribName, Equivalencies map) {
		for (int i = 0; i < attributes.length; i++)
			if (attributes[i].named(attribName, map))
				return attributes[i];
		return null;
	}

	/**
	 * Returns the specified Attribute.
	 * 
	 * @param type
	 *            the type of Attribute to get
	 * @param getDerived
	 *            true indicates all types derived from the base type
	 * @return the Attribute; null if not found
	 * @throws ClassNotFoundException
	 *             if type is invalid
	 */
	public Attribute[] getAttributesOfType(String type, boolean getDerived)
			throws ClassNotFoundException {
		LinkedList l = new LinkedList();
		Class attrClass = getDerived
				? Attributes.getClassForAttribute(type)
				: null;
		for (int i = 0; i < attributes.length; i++) {
			if (attributes[i].getType().equals(type)) {
				l.add(attributes[i]);
			} else if (getDerived
					&& attrClass.isAssignableFrom(attributes[i].getClass())) {
				l.add(attributes[i]);
			}
		}
		return (Attribute[]) l.toArray(new Attribute[l.size()]);
	}

	/**
	 * Returns the specified Attribute or FrameSpan. Framespan is specified by
	 * the String " framespan".
	 * 
	 * @param attribName -
	 *            the name of the Attribute to get
	 * @return the Attribute; null if not found
	 */
	public Filterable getFilterable(String attribName) {
		if (attribName.equals(" framespan"))
			return span;
		for (int i = 0; i < attributes.length; i++)
			if (attributes[i].named(attribName))
				return (attributes[i]);
		return null;
	}

	/***************************************************************************
	 * ------------------------------------------------------------------------
	 * ------------------------------------------------------------------------
	 */
	/**
	 * Checks to see if a String can be assigned to the specified Attribute
	 * 
	 * @param attribName
	 *            the name of the Attribute to check
	 * @param value
	 *            the String to verify
	 * @return true if possible; false if not or if the specified Attribute is
	 *         not found
	 */
	boolean isPossible(String attribName, String value) {
		for (int i = 0; i < attributes.length; i++)
			if (attributes[i].named(attribName))
				return (attributes[i].possibleValueOf(value));
		return (false);
	}

	/***************************************************************************
	 * ------------------------------------------------------------------------
	 * ------------------------------------------------------------------------
	 */
	/**
	 * Returns a String representation of the Descriptor. Contains EOL
	 * characters.
	 * 
	 * @return a String containing the category, name, ID number, span, and all
	 *         scoped Attributes
	 */
	public String toString() {
		StringBuffer sb = new StringBuffer().append(getFullName());
		Iterator spans = null;
		FrameSpan curr = null;
		if (!getType().equals("FILE")) {
			spans = getFrameSpan().split().iterator();
			curr = (FrameSpan) spans.next();
			sb.append(' ').append(getID()).append(' ').append(curr);
		}
		sb.append('\n');
		if (spans != null) {
			while (curr != null) {
				for (int i = 0; i < attributes.length; i++) {
					sb.append("   ")
							.append(
									attributes[i].crop(curr, getFrameSpan())
											.toString());
				}
				if (spans.hasNext()) {
					curr = (FrameSpan) spans.next();
					sb.append(getFullName()).append(' ').append(getID())
							.append(' ').append(curr).append('\n');
				} else {
					curr = null;
				}
			}
		} else {
			for (int i = 0; i < attributes.length; i++)
				sb.append("   ").append(attributes[i].toString());
		}
		return sb.toString();
	}

	/**
	 * Gets the full name of the descriptor, including category,
	 * e.g. <b>OBJECT Person</b>.
	 * @return the full, .gtf style name
	 */
	public String getFullName() {
		return getType() + " " + getName();
	}

	/**
	 * parses the given descriptor config
	 * 
	 * @param desc
	 *            the config element to parse
	 * @return the parsed value
	 * @throws BadDataException
	 *             if there is an error in the xml
	 */
	public static DescPrototype parseDescriptorConfig(Element desc)
			throws BadDataException {
		DescPrototype newNode;
		try {
			String type = desc.getAttribute("type");
			String name = desc.getAttribute("name");
			newNode = new DescPrototype(type, name);
		} catch (NullPointerException npx) {
			throw new BadDataException("Missing required XML attribute.");
		}

		boolean dynamic = newNode.getCategory().equalsIgnoreCase("OBJECT");
		NodeList attribs = desc.getElementsByTagNameNS(
				DescriptorData.NAMESPACE_URI, "attribute");
		for (int i = 0; i < attribs.getLength(); i++) {
			AttributePrototype A = AttributePrototype.parseAttributeConfig(
					(Element) attribs.item(i), dynamic);
			if (A != null) {
				// if the node type exists in the old format.
				newNode.composable &= Attributes.isComposable(A.getType());
				newNode.addAttribute(A);
			}
		}
		return newNode;
	}

	/**
	 * Reads in a piece of Descriptor configuration data from a VReader.
	 * 
	 * @param reader -
	 *            the reader to get information from
	 * @return the Descriptor config found next in the reader
	 */
	public static DescPrototype parseDescriptorConfig(VReader reader) {
		DescPrototype newNode;
		StringTokenizer st = new StringTokenizer(reader.getCurrentLine());
		if (reader.currentLineIsEndDirective())
			return null;
		try {
			String type = st.nextToken();
			String name = st.nextToken();
			newNode = new DescPrototype(type, name);
		} catch (BadDataException bdx) {
			reader.printError(bdx.getMessage());
			return (null);
		} catch (NoSuchElementException nsex) {
			reader.printError("Cannot find Attribute name in line.");
			return null;
		}

		if (st.hasMoreTokens()) {
			reader.printWarning("Extra information in config ignored: "
					+ st.nextToken());
		}

		try {
			reader.gotoNextRealLine();
		} catch (IOException iox) {
			reader.printWarning("No attributes found for descriptor");
			return newNode;
		}

		boolean dynamic = newNode.getCategory().equalsIgnoreCase("OBJECT");
		while ((!reader.currentLineIsEndDirective())
				&& (!Descriptor.startsWithCategory(reader.getCurrentLine()))) {
			try {
				AttributePrototype A = AttributePrototype.parseAttributeConfig(
						reader, dynamic);
				newNode.composable &= Attributes.isComposable(A.getType());
				newNode.addAttribute(A);
			} catch (BadDataException bdx) {
				reader.printError(bdx.getMessage());
			}
			try {
				reader.gotoNextRealLine();
			} catch (IOException iox) {
				reader.printWarning("End directive for Config not found.");
				return (newNode);
			}
		}
		return newNode;
	}

	/**
	 * 
	 * Sets the scope variable for this Descriptor. This does not affect the
	 * scope of the individual Attributes, which may remain invisible, or the
	 * ability to make Descriptors with certain data invisible.
	 * 
	 * @param complete -
	 *            true makes the Descriptor visible
	 */

	/**
	 * Sets the Descriptor's name.
	 * 
	 * @param n -
	 *            the new name of this Descriptor
	 */
	public void setName(String n) {
		name = n;
	}

	/**
	 * Tests to see if the Descriptor is named S
	 * 
	 * @param S -
	 *            the name to test
	 * @return true if S equals the name
	 */
	public boolean named(String S) {
		return (S.equals(name));
	}

	/**
	 * Gets the name of the Descriptor.
	 * 
	 * @return the name
	 */
	public String getName() {
		return (name);
	}

	/**
	 * Gets the type of the descriptor, ie OBJECT or CONTENT.
	 * 
	 * @return the String that represents the type.
	 */
	public String getType() {
		return category;
	}

	/**
	 * Returns the category of this Descriptor.
	 * @return either "FILE", "CONTENT", or "OBJECT"
	 */
	public String getCategory() {
		return (category);
	}

	/**
	 * Prints this descriptor in .gtf format to the given
	 * output writer.
	 * @param output the stream to use
	 * @param padding the amount of padding to put to the left. 
	 * Useful for indenting descriptors.
	 */
	public void printSelf(PrintWriter output, String padding) {
		output.println(padding + category + " " + name + " " + getID() + " "
				+ getFrameSpan());
		for (int i = 0; i < attributes.length; i++)
			output.print(padding + "   " + attributes[i]);
	}

	/**
	 * Prints this descriptor in .gtf format to the given
	 * output writer.
	 * @param output the stream to use
	 */
	public void printSelf(PrintWriter output) {
		output.println(category + " " + getID() + " " + name + " "
				+ getFrameSpan());
		for (int i = 0; i < attributes.length; i++)
			output.print("   " + attributes[i]);
	}

	/**
	 * Returns the number of the highest frame among this Descriptor
	 * 
	 * @return the highest frame among this Descriptor and all of its partial
	 *         and correct matches
	 */
	public int getHighestFrame() {
		return getFrameSpan().ending();
	}

	/***************************************************************************
	 * ------------------------------------------------------------------------
	 * ------------------------------------------------------------------------
	 */

	/**
	 * Checks to see if this String could be a value for any of the Attributes.
	 * 
	 * @param S 
	 *            the String to check
	 * @return true if some Attribute will accept it
	 */
	public boolean hasAttribute(String S) {
		for (int i = 0; i < attributes.length; i++)
			if (attributes[i].possibleValueOf(S))
				return (true);
		return (false);
	}

	/**
	 * Gets the first frame of the frame span
	 * @return the first valid frame
	 */
	public int startFrame() {
		return getFrameSpan().beginning();
	}

	/**
	 * Gets the last valid frame.
	 * @return the last valid frame
	 */
	public int endFrame() {
		return getFrameSpan().ending();
	}

	/**
	 * Function returns the raw string representation of this object.
	 * @return name and ID
	 */
	public String rawFormat() {
		return name + " " + getID();
	}

	/**
	 * Turns a list of Descriptors into a single Descriptor in order.
	 * 
	 * @param L
	 *            the descriptors to compose
	 * @param scope
	 *            the scoping rules to use
	 * @return the aggregate of the descriptors in L
	 * @throws BadDataException
	 * @throws UncomposableException
	 */
	static public Descriptor composeThese(Iterator L,
			EvaluationParameters.ScopeRules scope) throws BadDataException,
			UncomposableException {
		if (!L.hasNext())
			return null;

		Descriptor ag = (Descriptor) L.next();
		while (L.hasNext()) {
			Descriptor curr = (Descriptor) L.next();
			ag = ag.compose(curr, scope);
		}
		return ag;
	}

	/**
	 * Tests to see if it is possible to aggregate this descriptor
	 * with others of the same type.
	 * @return <code>true</code> if the descriptor
	 * is amenable to aggregation
	 * @see #compose(Descriptor, EvaluationParameters.ScopeRules)
	 * @see DescAggregate
	 */
	public boolean isComposable() {
		return composable;
	}

	/**
	 * Composes this descriptor with the given descriptor
	 * under the given set of scope and composition rules.
	 * It returns a new copy of this descriptor, leaving it
	 * unmodified.
	 * @param D the descriptor to compose with
	 * @param scope rules for the composition
	 * @return a new descriptor representing both this descriptor 
	 * and the other one
	 * @throws UnsupportedOperationException 
	 * @throws BadDataException  
	 * @throws UncomposableException
	 * @see DescAggregate
	 */
	public abstract Descriptor compose(Descriptor D,
			EvaluationParameters.ScopeRules scope)
			throws UnsupportedOperationException, BadDataException,
			UncomposableException;

	/**
	 * Get all attribute names.
	 * @return all attribute names
	 */
	public String[] getAttributeNames() {
		if (attributes == null) {
			return new String[0];
		} else {
			String[] names = new String[attributes.length];
			for (int i = 0; i < attributes.length; i++) {
				names[i] = attributes[i].getName();
			}
			return names;
		}
	}

	/**
	 * Gets an XML representation of the descriptor.
	 * @param root
	 *            The DOM Document to attach the element to.
	 * @return A DOM Element node representing the descriptor.
	 */
	abstract public Element getXMLFormat(Document root);

	/**
	 * @inheritDoc
	 */
	public boolean equals(Object o) {
		if (o instanceof Descriptor) {
			Descriptor other = (Descriptor) o;
			return this.getID().equals(other.getID())
					&& this.getName().equals(other.getName())
					&& this.getCategory().equals(other.getCategory());
		} else {
			return false;
		}
	}


	/**
	 * @inheritDoc
	 */
	public int hashCode() {
		return this.getID().hashCode() ^ this.getName().hashCode()
				^ this.getCategory().hashCode();
	}
}

