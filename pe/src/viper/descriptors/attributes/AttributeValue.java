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

/*
 * AttributeValue.java
 *
 * Created on June 25, 2002, 8:49 PM
 */

package viper.descriptors.attributes;

import org.w3c.dom.*;

/**
 * All Attributes operate on Attribute values. Some may also implement 
 * Composable. These objects should implement clone, hashCode, toString and
 * equals in addition to the methods proscribed by Measurable.
 * Note that they should be immutable. This will make everything
 * quicker, as there is far more cloning going on than I would like.
 * @author  davidm
 */
public interface AttributeValue extends Measurable {
    /**
     * Returns an xml element for this object. It should be in the namespace
     * Attribute.DEFAULT_NAMESPACE_QUALIFIER with a qualified name in the form 
     * <i><code>Attribute.DEFAULT_NAMESPACE_QUALIFIER</code>:type</i>.
     * @param doc The root for the element.
     * @return New DOM element for this data.
     */
    public Element toXML (Document doc);
    
    /**
     * Returns a new copy of the object with the data the String
     * represents. Useful for old GTF format.
     * The following should be true: 
     * <code>a.equals (a.setValue (a.toString()))</code>
     * Should try to use XML format whenever possible.
     * 
     * @param S String representation of this type of value.
     * @return the parsed value
     * @throws IllegalArgumentException If the data is ill-formed
     */
    public AttributeValue setValue (String S) throws IllegalArgumentException;

    /**
     * Returns a new copy of the object set to the data the xml-dom
     * element represents.
     * @param el DOM Node to parse
     * @return the parsed value
     * @throws IllegalArgumentException If the data is ill-formed
     */
    public AttributeValue setValue (Element el) throws IllegalArgumentException;
    
    /**
     * Checks to make sure that the value can be set.
     * @param v the value to check against this archetype
     * @return true iff the prototype can take this value.
     */
    public boolean validate (AttributeValue v);
}
