package viper.api.extensions;

import viper.api.*;

/**
 * 
 */
public interface FacetValueWrapper extends AttrValueWrapper {
	/**
	 * Gets the facet value given the attribute value.
	 * @param attrValue the attribute value, in encoded (internal) form
	 * @return gets the facet value
	 */
	public Object getFacetValue(Object attrValue);
	
	/**
	 * Modifies this facet of the value
	 * @param facetValue the new value of the attribute's facet
	 * @param attrValue the attribute to apply the new facet value to
	 * @return the new attribute value
	 */
	public Object setFacetValue(Object facetValue, Object attrValue);
}
