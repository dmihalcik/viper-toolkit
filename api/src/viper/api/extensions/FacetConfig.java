package viper.api.extensions;

import viper.api.*;

/**
 * A facet is an attribute that is linked to another attribute, 
 * usually representing some subset of that attribute's data.
 */
public interface FacetConfig extends AttrConfig {

	/**
	 * Gets the name of the attribute to which this facet refers
	 * @return
	 */
	public String getReferenceConfigName();
	
	/**
	 * Gets the attribute to which this facet refers
	 * @return
	 */
	public AttrConfig getReferenceConfig();

	/**
	 * Gets the utility for actually modifying the facet.
	 * @return the facet wrapper, a function object for modifying the
	 * facet
	 */
	public FacetValueWrapper getFacetWrapper();
}
