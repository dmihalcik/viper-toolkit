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

package viper.descriptors.attributes;

import java.lang.reflect.*;

import org.w3c.dom.*;

import viper.descriptors.*;
import edu.umd.cfar.lamp.viper.util.*;
import edu.umd.cfar.lamp.viper.util.reader.*;

/**
 * This class is used as a factory for {@link Attribute} data.
 */
public class AttributePrototype extends AbstractAttribute {
	private String type = null;

	/**
	 * Returns the Attribute type, eg <code>lvalue</code> or 
	 * <code>bbox</code>.
	 * @return The type.
	 */
	public String getType() {
		return type;
	}
	
	/**
	 * Gets the local part of the type name.
	 * @return the local part of the type name. Might
	 * not be unique to a type, but it has been so far.
	 */
	public String getLocalType() {
		int l = type.lastIndexOf("#");
		return type.substring(l+1);
	}

	/**
	 * Sets the attribute type.
	 * @param type The new type.
	 * @throws  BadDataException if Attributes.isType returns
	 *                    <code>false</code>.
	 */
	public void setType(String type) throws BadDataException {
		if (Attributes.isType(type))
			this.type = type;
		else
			throw new BadDataException("Cannot find Attribute type: " + type);
	}

	/** @inheritDoc */
	public void setName(String name) {
		super.setName(name);
	}

	/** @inheritDoc */
	public void setDynamic(boolean dynamic) {
		super.setDynamic(dynamic);
	}

	/**
	 * Create an empty Attribute. Use the setter methods to 
	 * initialize it.
	 */
	private AttributePrototype() {
	}

	/**
	 * Attributes for OBJECT type should use this 
	 * constructor for their dynamic attributes.
	 * Other Descriptor types can use this with mobile set to false.
	 * @param mobile Descriptor that holds this Attribute is of type OBJECT
	 */
	private AttributePrototype(boolean mobile) {
		setDynamic(mobile);
	}

	/**
	 * Default copy constructor.
	 * @param old the value to copy
	 */
	public AttributePrototype(AbstractAttribute old) {
		setName(old.getName());
		setDynamic(old.isDynamic());
		setArchetype(old.getArchetype());
		resetDefault(old.getDefault());
		try {
			setType(old.getType());
		} catch (BadDataException bdx) {
			throw new IllegalStateException(
				"Unexpected BadDataException: " + bdx.getMessage());
		}
	}

	/**
	 * Returns a clone of this Attribute.
	 * @return a reference to a new Attribute with all the values 
	 *     of the original
	 */
	public Object clone() {
		return new AttributePrototype(this);
	}

	/**
	 * Returns the format properly for config information.
	 * @param root the document to use when creating the element
	 * @return a dom element describing this attribute configuration
	 */
	public Element getXMLFormat(Document root) {
		Element attribute = root.createElement("attribute");
		attribute.setAttribute("name", getName());
		attribute.setAttribute("dynamic", String.valueOf(isDynamic()));
		if (!"none".equals(getType())) {
			attribute.setAttribute("type", getType());
		}
		if (getArchetype() != null
			&& getArchetype() instanceof ExtendedAttributeValue) {
			ExtendedAttributeValue eav =
				(ExtendedAttributeValue) getArchetype();
			Element child = eav.getExtraConfig(root);
			if (child != null) {
				attribute.appendChild(child);
			}
		}
		if (null != getDefault()) {
			Attribute instance = create();
			Element defEl = root.createElement("default");
			defEl.appendChild(instance.getDefault().toXML(root));
			attribute.appendChild(defEl);
		}
		return attribute;
	}

	/**
	 * Gets the string representation of static Attribute
	 *     data or an individual frame of static data. Use this 
	 *     for the classic GtfConfig format.
	 * Equivalent to <code>toString( true, true )</code>.
	 * @return <code>String</code> in gtf format for parsing
	 */
	public String toString() {
		return toString(true, true);
	}

	/**
	 * Gets the string representation of static Attribute
	 *     data or an individual frame of static data. Use this 
	 *     for the classic GtfConfig format.
	 *     It isn't as useful as the newer format, although it
	 *     is far more human-readable.
	 * @param verbose set to include ending <em>[static]</em> for static
	 *                attributes
	 * @param endln   set to include an endln
	 * @return a string in proper format of the data value
	 */
	public String toString(boolean verbose, boolean endln) {
		StringBuffer buf = new StringBuffer("   ");
		buf.append(getName()).append(" : ").append(getLocalType());
		create();
		if (getArchetype() != null
			&& getArchetype() instanceof ExtendedAttributeValue) {
			ExtendedAttributeValue eav =
				(ExtendedAttributeValue) getArchetype();
			String child = eav.getExtraConfigString();
			if (child != null) {
				buf.append(" [").append(child).append("]");
				;
			}
		}
		AttributeValue def = getDefault();
		if (null != def) {
			buf.append(
				" [default \""
					+ StringHelp.backslashify(def.toString())
					+ "\"]");
		}
		if (verbose && !isDynamic())
			buf.append(" [static]");
		if (endln)
			buf.append('\n');
		return buf.toString();
	}

	/**
	 * Creates a new attribute configuration.
	 * @param type the data type of the attribute
	 * @param name the name to give the attribute
	 * @param dynamic if the attribute may vary with the frame number
	 * @return the new attribute class definition
	 * @throws BadDataException
	 */
	public static AttributePrototype createAttributePrototype(
		String type,
		String name,
		boolean dynamic)
		throws BadDataException {
		AttributePrototype newAttr = new AttributePrototype(dynamic);
		newAttr.setName(name);
		newAttr.setType(type);
		return newAttr;
	}

	/**
	 * Reads in the attribute prototype from the XML DOM node.
	 * @param reader the element to read in
	 * @param dynamic if the attribute may be dynamic
	 * @return the new descrition
	 * @throws BadDataException
	 */
	public static AttributePrototype parseAttributeConfig(
		Element reader,
		boolean dynamic)
		throws BadDataException {
		try {
			AttributePrototype newAttr = new AttributePrototype(dynamic);
			Attr tempAttr;

			tempAttr = reader.getAttributeNode("name");
			if (null == tempAttr) {
				throw new BadDataException("Cannot find attribute name");
			} else {
				newAttr.setName(tempAttr.getValue());
			}

			tempAttr = reader.getAttributeNode("type");
			if (null == tempAttr) {
				throw new BadDataException("Cannot find attribute type");
			} else {
				newAttr.setType(tempAttr.getValue());
			}

			tempAttr = reader.getAttributeNode("dynamic");
			if (null != tempAttr) {
				String S = tempAttr.getValue();
				if (S.equalsIgnoreCase("false") || S.equals("0"))
					newAttr.setDynamic(false);
				else if (S.equalsIgnoreCase("true") || S.equals("1"))
					newAttr.setDynamic(true);
				else
					throw new BadDataException(
						"Not a value for dynamic attr: " + S);
			}

			Attribute toyInstance = newAttr.create();

			NodeList extras = reader.getChildNodes();
			Element defaultElement = null;
			boolean hasMore = false;
			for (int i = 0; i < extras.getLength(); i++) {
				Node curr = extras.item(i);
				if (curr.getNodeType() == Node.ELEMENT_NODE) {
					Element cel = (Element) curr;
					if (cel
						.getNamespaceURI()
						.equals(DescriptorData.NAMESPACE_URI)
						&& cel.getLocalName().equals("default")) {
						if (defaultElement != null) {
							throw new BadDataException(
								"There can only be one default value for each attribute;\n this "
									+ toyInstance.getType()
									+ " has multiple <default> elements");
						} else {
							defaultElement = cel;
						}
					} else {
						hasMore = true;
					}
				}
			}

			if (hasMore) {
				toyInstance.setArchetype(reader);
				newAttr.setArchetype(toyInstance.getArchetype());
			}
			if (null != defaultElement) {
				NodeList defaults = defaultElement.getElementsByTagName("*");
				if (defaults.getLength() != 1) {
					throw new BadDataException(
						"There can only be one default value for each attribute; this has "
							+ defaults.getLength()
							+ " listed.");
				}
				defaultElement = (Element) defaults.item(0);
				if (null != defaultElement) {
					newAttr.setDefaultValue(defaultElement);
				}
			}
			return newAttr;
		} catch (BadDataException bdx) {
			bdx.printStackTrace();
			return null;
		}
	}

	/**
	 * Parses an Attribute configuration line.
	 * @param reader   the VReader pointing at the line to be parsed
	 * @param mobile   set true if this is an OBJECT Descriptor
	 * @return an AttributePrototype representing the line in the gtfcfg
	 * @throws BadDataException
	 */
	public static AttributePrototype parseAttributeConfig(
		VReader reader,
		boolean mobile)
		throws BadDataException {
		String s = reader.getCurrentLine();
		AttributePrototype newAttr = new AttributePrototype(mobile);
		CountingStringTokenizer st = new CountingStringTokenizer(s);
		String temp = null;

		newAttr.setName(st.nextToken());

		if (newAttr.getName() == null) {
			reader.printError(
				"Attribute poorly named",
				st.getStart(),
				st.getEnd());
			throw new BadDataException(
				"Cannot find attribute name in line \"" + s + "\"",
				st.getStart(),
				st.getEnd());
		}

		if (st.hasMoreTokens() && st.nextToken().equals(":")) {
			while (st.hasMoreTokens()) {
				temp = st.nextToken();
				if (temp.equalsIgnoreCase("[static]")) {
					newAttr.setDynamic(false);
				} else if (temp.equalsIgnoreCase("[dynamic]")) {
					newAttr.setDynamic(true);
				} else if (temp.equalsIgnoreCase("[default")) {
					temp = st.nextToken();
					if (temp.equals("=")) {
						temp = st.nextToken();
					}
					if ((temp.charAt(0) == '"')
						&& (temp.charAt(temp.length() - 2) == '"')) {
						newAttr.setDefaultValue(
							StringHelp.debackslashify(
								temp.substring(1, temp.length() - 2)));
					} else {
						newAttr.setDefaultValue(
							temp.substring(0, temp.length() - 1));
					}
				} else if (temp.startsWith("[")) {
					if (!temp.endsWith("]")) {
						int start = st.getStart() + 1;
						while (st.hasMoreTokens()
							&& !st.nextToken().endsWith("]"));
						int end = st.getEnd() - 1;
						if (']' != s.charAt(end)) {
							reader.printWarning(
								"Extended attribute info missing ']' character");
						}
						temp = s.substring(start, end);
					} else {
						temp = temp.substring(1, temp.length() - 1);
					}
					try {
						Attribute toyInstance = newAttr.create();
						toyInstance.setArchetype(temp);
						newAttr.setArchetype(toyInstance.getArchetype());
					} catch (BadDataException bdx) {
						if (bdx.isChar()) {
							reader.printError(
								bdx.getMessage(),
								bdx.getStart(),
								bdx.getEnd());
						} else {
							reader.printError(bdx.getMessage());
						}
					}
				} else if (!Attribute.isType(temp)) {
					reader.printError(
						"Improper attribute type \"" + temp + "\"",
						st.getStart(),
						st.getEnd());
				} else { // This is the type information
					try {
						newAttr.setType(temp);
					} catch (BadDataException bdx) {
						if (bdx.isChar()) {
							reader.printError(
								bdx.getMessage(),
								bdx.getStart(),
								bdx.getEnd());
						} else {
							reader.printError(bdx.getMessage());
						}
					}
				}
			}
			if (newAttr.getType() == null) {
				reader.printError("Missing attribute type");
			}
		} else {
			reader.printError("Missing ':' in line");
		}
		if (newAttr.getType() == null) {
			throw new BadDataException("Cannot find type information for Attribute");
		}
		return newAttr;
	}

	/**
	 * Create a new attribute using this Prototype as a template.
	 * @return A new Attribute with the specwith no data.
	 * @throws IllegalArgumentException if the attribute is not valid
	 */
	public Attribute create() {
		Class[] constructorType = {
		};
		Object[] constructorArguments = {
		};
		Attribute newAttr;
		try {
			Class c = Attributes.getClassForAttribute(getType());
			newAttr =
				((Attribute) (c
					.getConstructor(constructorType)
					.newInstance(constructorArguments)));
		} catch (NoSuchMethodException nsmx) {
			throw new IllegalArgumentException(
				nsmx.getMessage()
					+ "\n\tAttribute type "
					+ getType()
					+ " is improperly defined (missing constructor)");
		} catch (InstantiationException ix) {
			throw new IllegalArgumentException(
				ix.getMessage()
					+ "\n\tAttribute type "
					+ getType()
					+ "Attribute is improperly defined (not a concrete class)");
		} catch (IllegalAccessException iax) {
			throw new IllegalArgumentException(
				iax.getMessage()
					+ "\n\tAttribute type "
					+ getType()
					+ " is missing or otherwise inaccessible");
		} catch (InvocationTargetException itx) {
			// This is an exception that wraps an exception thrown by something
			// invoked. In this case, this is any exceptions thrown by the constructor.
			itx.printStackTrace();
			throw new IllegalArgumentException(
				itx.getTargetException().getMessage());
		}
		newAttr.setName(getName());
		newAttr.setDynamic(isDynamic());

		if (getArchetype() != null) {
			newAttr.setArchetype(getArchetype());
		}
		newAttr.resetDefault(getDefault());

		return newAttr;
	}

	/**
	 * Create a new attribute of the specified type using its constructor
	 * that takes an Attribute value (copy-constructor?).
	 * @param old the attribute prototype or another copy of the 
	 * same attribute type
	 * @return a new attribute that conforms to the old attribute
	 * type
	 * @throws BadDataException
	 */
	public Attribute create(Attribute old) throws BadDataException {
		String attributeClassName =
			"viper.descriptors.attributes.Attribute_" + getType();
		Class[] constructorType = { Attribute.class };
		Object[] constructorArguments = { old };
		Attribute newAttr;
		try {
			newAttr =
				((Attribute) (Attribute
					.class
					.getClassLoader()
					.loadClass(attributeClassName)
					.getConstructor(constructorType)
					.newInstance(constructorArguments)));
		} catch (ClassNotFoundException cnfx) {
			throw (
				new BadDataException(
					cnfx.getMessage()
						+ "\n\tAttribute type "
						+ getType()
						+ " not found (checked for "
						+ attributeClassName
						+ " in "
						+ Attribute.class.getClassLoader()
						+ ")"));
		} catch (NoSuchMethodException nsmx) {
			throw (
				new BadDataException(
					nsmx.getMessage()
						+ "\n\tAttribute type "
						+ getType()
						+ " is improperly defined (missing constructor)"));
		} catch (InstantiationException ix) {
			throw (
				new BadDataException(
					ix.getMessage()
						+ "\n\tAttribute type "
						+ getType()
						+ "Attribute is improperly defined (not a concrete class)"));
		} catch (IllegalAccessException iax) {
			throw (
				new BadDataException(
					iax.getMessage()
						+ "\n\tAttribute type "
						+ getType()
						+ " is missing or otherwise inaccessible"));
		} catch (InvocationTargetException itx) {
			// This is an exception that wraps an exception thrown by something
			// invoked. In this case, this is any exceptions thrown by the
			//  constructor.
			throw new BadDataException(itx.getTargetException().getMessage());
		}

		newAttr.setName(getName());
		newAttr.setDynamic(isDynamic());

		if (getArchetype() != null) {
			newAttr.setArchetype(getArchetype());
		}
		newAttr.resetDefault(getDefault());
		return newAttr;
	}

	/**
	 * Some attributes may have a default value. If the type is set,
	 * this will check the string using
	 * {@link Attributes#isGoodValue(Attribute,String)}.
	 * @param defaultValue What to set as the default.
	 * @throws BadDataException if Attributes.isGoodValue returns
	 *                 <code>false</code>.
	 */
	protected void setDefaultValue(String defaultValue)
		throws BadDataException {
		try {
			Attribute temp = create();
			if (temp.possibleValueOf(defaultValue)) {
				AttributeValue av = temp.getArchetype().setValue(defaultValue);
				resetDefault(av);
			} else {
				throw new BadDataException(
					"Not a valid default value for "
						+ getType()
						+ ": "
						+ defaultValue);
			}
		} catch (IllegalArgumentException iax) {
			throw new BadDataException(iax.getMessage());
		}
	}

	protected void setDefaultValue(Element defaultValue)
		throws BadDataException {
		try {
			Attribute temp = create();
			AttributeValue av = temp.getArchetype().setValue(defaultValue);
			resetDefault(av);
		} catch (IllegalArgumentException iax) {
			throw new BadDataException(iax.getMessage());
		}
	}

	/** @inheritDoc */
	public void setArchetype(Element E) throws BadDataException {
		Attribute toyInstance = create();
		toyInstance.setArchetype(E);
		setArchetype(toyInstance.getArchetype());
	}
	
	/** @inheritDoc */
	public void setArchetype(String S) throws BadDataException {
		Attribute toyInstance = create();
		toyInstance.setArchetype(S);
		setArchetype(toyInstance.getArchetype());
	}
}
