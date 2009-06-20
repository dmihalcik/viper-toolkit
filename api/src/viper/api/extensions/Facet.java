package viper.api.extensions;

import viper.api.*;

/**
 * An attribute that is linked to another attribute, or
 * set of attributes. Changing its value modifies the linked
 * attribute in some way.
 */
public interface Facet extends Attribute {
	public Attribute getReferenceAttribute();
}
