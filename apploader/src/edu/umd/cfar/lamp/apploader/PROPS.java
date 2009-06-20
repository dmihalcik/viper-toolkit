/***************************************
 *:// L A M P . cfar . umd . edu       *
 *      AppLoader                      *
 *                                     *
 *      A tool for loading java apps   *
 *             from RDF descriptions.  *
 *                                     *
 * Distributed under the GPL license   *
 *        Terms available at gnu.org.  *
 *                                     *
 *  Copyright University of Maryland,  *
 *                      College Park.  *
 ***************************************/

package edu.umd.cfar.lamp.apploader;

import com.hp.hpl.jena.rdf.model.*;

/**
 * Property sheet namespace convenience class, as described in 
 * the <a href="http://viper-toolkit.sourceforge.net/owl/apploader/props">props
 * schema</a>.
 */
public class PROPS {
	protected static final String uri =
		"http://viper-toolkit.sourceforge.net/owl/apploader/props#";

	/** 
	 * Returns the URI for this schema,
	 * <code>http://viper-toolkit.sourceforge.net/owl/apploader/props#</code>.
	 * @return the URI for this schema
	 */
	public static String getURI() {
		return uri;
	}

	/**
	 * Links to the bean that this PropertySet describes.
	 */
	public static final Property describes =
		ResourceFactory.createProperty(uri + "describes");

	/**
	 * Links to one of the PropertyDescriptors for the 
	 * given bean.
	 */
	public static final Property hasProperty =
		ResourceFactory.createProperty(uri + "hasProperty");

	/**
	 * Links to a List of PropertyDescriptor nodes for this
	 * set. This implies that the Set hasProperty each
	 * element in the list.
	 */
	public static final Property propertyOrder =
		ResourceFactory.createProperty(uri + "propertyOrder");

	/**
	 * Gets an interface bean, an object that supports get and set methods.
	 * This is useful when you are trying to get
	 * properties that don't have the normal get/set methods, or may
	 * have arbitrary names.
	 */
	public static final Property interfacer =
		ResourceFactory.createProperty(uri + "interfacer");

	/**
	 * Gets a java object that lists more properties than 
	 * exist in the file, and may come and go over time.
	 */
	public static final Property extendedProperties =
		ResourceFactory.createProperty(uri + "extendedProperties");

	/**
	 * A table cell renderer for the property.
	 */
	public static final Property renderer =
		ResourceFactory.createProperty(uri + "renderer");

	/**
	 * A table cell editor for the property.
	 */
	public static final Property editor =
		ResourceFactory.createProperty(uri + "editor");

	// XXX Maybe I should add grouping ontology for properties?
	// should probably add grouping to menus first

	/**
	 * Indicates that this listener will be notified when
	 * properties change on the associated bean.
	 */
	public static final Property addListener =
		ResourceFactory.createProperty(uri + "addListener");

	/**
	 * The java name of the listener interface. The AppLoader
	 * will construct a dynamic proxy that will respond to events
	 * sent to this listener's methods.
	 */
	public static final Property listenerType =
		ResourceFactory.createProperty(uri + "listenerType");
	
	/**
	 * The name of the listener, e.g. the part after add in
	 * <code>addListenerName</code> on the bean.
	 */
	public static final Property listenerName =
		ResourceFactory.createProperty(uri + "listenerName");
}
