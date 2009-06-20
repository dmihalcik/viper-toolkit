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

package viper.api;

/**
 * Configuration information for an attribute attatched to a Config
 * object. An instance contains information describing an attribute
 * data type. Note that this class is immutable in its current form.
 */
public interface AttrConfig extends Node {
	/**
	 * Returns the name of the attribute. This is usually something
	 * short and descriptive, and cannot contain spaces. Examples
	 * include "FaceBox," "NUMFRAMES" and "Comment."
	 * A given attribute name is unique for its enclosing Descriptor.
	 * @return The attribute's name. 
	 */
	public String getAttrName();

	/**
	 * Gets the datatype name of the attribute, for example "lvalue,"
	 * "bbox" or "circle." Note that the actual data types are defined
	 * in viper.api.datatypes. Note this is just the local part of 
	 * the name, not the full name. Since we will be switching to the
	 * RDF standard way of doing things in the future, this may
	 * be deprecated in favor of getTypeURI or something similar.
	 * 
	 * @return the attribute type
	 */
	public String getAttrType();

	/**
	 * Determines if the attribute is dynamic. For attributes of FILE
	 * or CONTENT descriptors, this necisarily returns <code>false</code>.
	 * Only attributes of OBJECT-type descriptors may be dynamic.
	 * @return <code>true</code> iff the attribute may change over time 
	 *    for one instance of the descriptor.
	 */
	public boolean isDynamic();

	/**
	 * Gets the default value for this attribute. When new instances 
	 * are created using this attribute, they will first have this
	 * default value.
	 * @return the default value. It may be <code>null</code>.
	 */
	public Object getDefaultVal();

	/**
	 * Get the param object passed to the createAttrConfig method.
	 * @return the parameter(s) used to create the Attribute.
	 */
	public AttrValueWrapper getParams();

	/**
	 * Get the editor for the node, if it exists.
	 * @return the editor object; <code>null</code> if it doesn't exist.
	 */
	public AttrConfig.Edit getEditor();
	
	/**
	 * Tests to see if the attribute may take on a null value.
	 * If it is a dynamic attribute, then it will be null if and 
	 * only if the containing descriptor is marked invalid at the
	 * tested frame. 
	 * @return <code>true</code> if the attribute may take
	 * the value of <code>null</code>
	 */
	public boolean isNillable();
	
	/**
	 * Defines the methods for editing an existing attribute configuration.
	 */
	public interface Edit {
		/**
		 * Sets the name of the attribute, e.g. "Face" or "Location".
		 * @param name The name of the attribute
		 */
		public void setAttrName(String name);

		/**
		 * I'm not sure if this is correct or not. 
		 * The type is a type, and the params are information about
		 * the implementation of the type. As such, it would be 
		 * bad for them to get out of sync, so I'm only letting them
		 * be set at the same time. (For example, the type could 
		 * be <code>lvalue</code> and the params would be a list
		 * of possibilities.)
		 * @param uri 
		 * @param params
		 */
		public void setAttrType(String uri, AttrValueWrapper params);
		
		/**
		 * Sets the default value of the attribute. It should be coherent with
		 * the type.
		 * @param val the new default value (in external format)
		 */
		public void setDefaultVal(Object val);
		
		/**
		 * Sets the dynamisticity of the attribute. Can only be set to <code>true</code>
		 * for attributes of OBJECT descriptors.
		 * @param d <code>true</code> for dynamic attributes
		 */
		public void setDynamic(boolean d);
		
		/**
		 * Sets the ability of the attribute to take no value. This 
		 * defaults to <code>true</code>, and is set in the xml file
		 * by setting nillable to false on the 
		 * @param n <code>true</code> if the attribute should be allowed
		 * to be given an empty value
		 */
		public void setNillable(boolean n);
	}
}
