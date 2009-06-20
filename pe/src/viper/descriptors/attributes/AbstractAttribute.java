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

import org.w3c.dom.*;

import edu.umd.cfar.lamp.viper.util.*;

/**
 * This class exists to give a shared base class to Attribute and
 * AttributePrototype.
 */
public abstract class AbstractAttribute implements Cloneable {
	/** A String representing the Attribute name. */
	private String name = "";

	/**
	 * Returns the Attribute name. eg position, size, readability, etc.
	 * 
	 * @return A String representing the Attribute name.
	 */
	public final String getName() {
		return name;
	}
	/**
	 * Sets the Attribute name.
	 * 
	 * @param name
	 *            The new name.
	 */
	protected void setName(String name) {
		this.name = name;
	}

	/**
	 * Returns true if the Attribute is named S. It is case sensitive.
	 * 
	 * @param S
	 *            The name to test against.
	 * @return <code>true</code> if the Attribute is named S.
	 */
	public final boolean named(String S) {
		return S.equals(name);
	}

	/**
	 * Returns true if the Attribute is named S or mapped to S under map. It is
	 * case sensitive.
	 * 
	 * @param S
	 *            The name to test against.
	 * @param map
	 *            The list of equivalcencies.
	 * @return <code>true</code> if the Attribute is named S or in the same
	 *         equivalency set.
	 */
	public final boolean named(String S, Equivalencies map) {
		return S.equals(name) || map.eq(S, name) || map.eq(name, S);
	}

	/**
	 * Returns the Attribute type, eg <code>lvalue</code> or <code>bbox</code>.
	 * 
	 * @return The type.
	 */
	public abstract String getType();

	private AttributeValue defaultValue;

	/**
	 * Some attributes may have a default value. If the type is set, this will
	 * check the string using {@link Attributes#isGoodValue}.
	 * 
	 * @param defaultValue
	 *            What to set as the default.
	 * @throws BadDataException
	 *             if Attributes.isGoodValue returns <code>false</code>.
	 * @throws MethodNotSupportedException
	 */
	protected abstract void setDefaultValue(String defaultValue)
			throws BadDataException, MethodNotSupportedException;
	protected abstract void setDefaultValue(Element defaultValue)
			throws BadDataException, MethodNotSupportedException;

	/**
	 * Modifies the attribute's default value.
	 * @param o the new default value
	 */
	public final void resetDefault(AttributeValue o) {
		defaultValue = o;
	}

	/**
	 * A default value is what an attribute is set to when created.
	 * 
	 * @return A String representing the default.
	 */
	public final AttributeValue getDefault() {
		return defaultValue;
	}

	private boolean dynamic;
	/**
	 * A dynamic attribute can change from frame to frame.
	 * 
	 * @return if this can vary from frame to frame. It is meaningless for
	 *         <code>content</code> or <code>file</code> Descriptors.
	 */
	public final boolean isDynamic() {
		return dynamic;
	}
	/**
	 * Set this to allow the attribute to vary from frame to frame.
	 * 
	 * @param dynamic
	 *            The value.
	 */
	protected void setDynamic(boolean dynamic) {
		this.dynamic = dynamic;
	}

	/**
	 * Returns an XML formatted version of this data.
	 * 
	 * @param root
	 *            where to print the data.
	 * @return an xml element representing the attribute or attribute schema
	 */
	abstract public Element getXMLFormat(Document root);

	/**
	 * ExtraConfig information is information concerning the data that is not
	 * expressed in the type itself. For example, an
	 * {@link viper.descriptors.attributes.Attribute_lvalue lvalue}type has a
	 * list of possible values in its extra config info. It is maintained as a
	 * Map. It is up to the implementation to decide what to do with the Map; it
	 * is mostly used as a map for legacy reasons, and I will probably change it
	 * to an Object soon enough.
	 * 
	 * @param archetype
	 *            an example of the data type
	 */
	public void setArchetype(AttributeValue archetype) {
		this.archetype = archetype;
	}
	
	/**
	 * Gets the config value.
	 * @return the value archetype - the schema value
	 */
	public AttributeValue getArchetype() {
		return archetype;
	}
	
	/**
	 * Sets the archetype associated with this descriptor.
	 * @param E the description
	 * @throws BadDataException
	 */
	public void setArchetype(Element E) throws BadDataException {
		throw new BadDataException("The data type " + this.getType()
				+ " does not take extended config information");
	}
	
	/**
	 * Sets the archetype associated with this descriptor.
	 * @param S the description
	 * @throws BadDataException
	 */
	public void setArchetype(String S) throws BadDataException {
		throw new BadDataException("The data type " + this.getType()
				+ " does not take extended config information");
	}

	private AttributeValue archetype = null;

	/**
	 * Determines how to print it out to the old gtf format
	 * 
	 * @return the extra config string, to go in brackets in the GTF CONFIG
	 *         section
	 */
	public final String getExtraConfigString() {
		if (archetype != null && archetype instanceof ExtendedAttributeValue) {
			return ((ExtendedAttributeValue) archetype).getExtraConfigString();
		} else {
			return null;
		}
	}

	/**
	 * Gets the attribute-type-specific extended information,
	 * if it exists. It returns <code>null</code>, otherwise.
	 * @param root the DOM root
	 * @return the extended element, or <code>null</code>
	 */
	public final Element getExtraConfigInXML(Document root) {
		if (archetype != null && archetype instanceof ExtendedAttributeValue) {
			return ((ExtendedAttributeValue) archetype).getExtraConfig(root);
		} else {
			return null;
		}
	}

	/**
	 * Copies the attribute.
	 * @return {@inheritDoc}
	 */
	abstract public Object clone();

	/**
	 * Gets the int value of an XML attribute of the given XML element.
	 * 
	 * @param domEl
	 *            the element to parse the attribute of
	 * @param name
	 *            the name of the attribute
	 * @return the value of the attribute
	 * @throws BadDataException
	 */
	protected static int parseAnInt(Element domEl, String name)
			throws BadDataException {
		try {
			return Integer.parseInt(domEl.getAttribute(name));
		} catch (NullPointerException npx) {
			throw new BadDataException("Data value not complete, missing: "
					+ name);
		} catch (NumberFormatException nfx) {
			throw new BadDataException("Not a valid number: "
					+ domEl.getAttribute(name));
		}
	}

	/**
	 * Makes sure the java class for an attribute is loaded. Various static
	 * initializers must run for an attribute in order for them to be usable.
	 * 
	 * @param type
	 *            the type to load
	 * @return an instance of the type
	 * @throws ClassCastException
	 *             if the type is not found
	 */
	public static Measurable loadAttributeType(String type)
			throws ClassCastException {
		Class c = Attributes.getClassForAttribute(type);
		try {
			Object obj = c.newInstance();
			if (obj instanceof Measurable) {
				return (Measurable) obj;
			} else if (obj instanceof AbstractAttribute) {
				return ((AbstractAttribute) obj).getArchetype();
			} else {
				throw new ClassCastException(
						"Type does not implement Measurable interface: " + type);
			}
		} catch (InstantiationException ix) {
			throw new ClassCastException(ix.getMessage()
					+ "\n\tAttribute type " + type
					+ " has an invalid constructor");
		} catch (IllegalAccessException iax) {
			throw new ClassCastException(iax.getMessage()
					+ "\n\tAttribute type " + type + " has an invalid access");
		}
	}
}

