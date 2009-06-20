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
 * Namespace schema for an application WIMP menuing system.
 * 
 * For information, see the
 * <a href="http://viper-toolkit.sourceforge.net/owl/apploader/menu">namespace
 * document</a>.
 * 
 * @author davidm
 */
public class MENU {
	protected static final String uri =
		"http://viper-toolkit.sourceforge.net/owl/apploader/menu#";

	/** 
	 * Returns the URI for this schema,
	 * <code>http://viper-toolkit.sourceforge.net/owl/apploader/menu#</code>.
	 * @return the URI for this schema
	 */
	public static String getURI() {
		return uri;
	}

	/**
	 * The class of internal menu nodes.
	 */
	public static final Resource Menu =
		ResourceFactory.createResource(uri + "Menu");
	
	/**
	 * The class of menu items: leaves on the menu tree.
	 */
	public static final Resource Item =
		ResourceFactory.createResource(uri + "Item");
	
	/**
	 * A Group is like a Menu, but is instead included in the parent
	 * menu, surrounded by lines, while a Menu is labeled and has a 
	 * pop-up submenu.
	 */
	public static final Resource Group =
		ResourceFactory.createResource(uri + "Group");

	/**
	 * Indicates where the node is to be attached, either to another
	 * menu or group, or to a bean for top-level menus.
	 */
	public static final Property attachment =
		ResourceFactory.createProperty(uri + "attachment");
	
	/**
	 * The mnemonic is the underlined text. This is localized.
	 */
	public static final Property mnemonic =
		ResourceFactory.createProperty(uri + "mnemonic");

	/**
	 * The priority is used while sorting the node in its 
	 * menu or group. The highest priorities (top of the menu)
	 * are 0, 1, 2... The lowest priorities (bottom to top) are
	 * -1, -2, ...
	 */
	public static final Property priority =
		ResourceFactory.createProperty(uri + "priority");
	
	/**
	 * The Actionator that is invoked when the menu item is 
	 * selected.
	 */
	public static final Property generates =
		ResourceFactory.createProperty(uri + "generates");
}
