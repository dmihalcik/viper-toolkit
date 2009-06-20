package viper.api.extensions;

import java.util.*;

import viper.api.*;

/**
 * 
 */
public interface FacetedAttributeWrapper extends AttrValueWrapper {
	public List getFacetDefinitions(AttrConfig link);
	public FacetConfig getFacetByName(String name, AttrConfig link);
}
