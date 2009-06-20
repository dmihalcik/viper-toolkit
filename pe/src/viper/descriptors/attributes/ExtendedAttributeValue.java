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
 * ExtendedAttributeValue.java
 *
 * Created on June 28, 2002, 3:44 PM
 */

package viper.descriptors.attributes;


import org.w3c.dom.*;

/**
 * Some attributes have extra config information. Their must implement
 * this interface. When outputing the attribute config section, this
 * method is called on each attribute's archetype.
 * @author  davidm
 */
public interface ExtendedAttributeValue extends AttributeValue {
	
	/**
	 * Gets the extra config information for the attribute.
	 * @return the extra config string
	 */
    public String getExtraConfigString();
    
    /**
     * Gets the extra config information as an XML 
     * DOM node.
     * @param root the document root; necessary for
     * creation of a DOM node
     * @return the element
     */
    public Element getExtraConfig(Document root);
}
