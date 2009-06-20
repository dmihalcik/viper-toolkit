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

package edu.umd.cfar.lamp.apploader.prefs;

import com.hp.hpl.jena.rdf.model.*;

/**
 * A <code>TriggerHandler</code> is created and invoked when
 * a preference is set (a preference trigger or flag) and an
 * instance of the property is found on the command line or in
 * the java properties set on start-up of the application.
 * 
 * An example of a trigger is a command line key/value pair.
 * Upon finding a trigger, the application loader uses the 
 * preferences to find an implementation of TriggerHandler to
 * invoke. 
 * 
 * A flag is like a trigger, but it does not take a value.
 * They share an interface (this one, <code>TriggerHandler</code>,
 * for convenience.
 * 
 * The initial idea was just to convert the values into
 * RDF triples, but you are free to do whatever you wish upon 
 * handling a trigger. For example, a '--help' flag may either
 * add a javabeans property set method in the preference model
 * that turns on 'help' mode, or it may invoke the 
 * {@link PrintUsage} handler, which prints usage to the command
 * line and exits the program.
 * 
 * @author davidm
 */
public interface TriggerHandler {
	/**
	 * Invokes the given trigger using the given command
	 * line value, if it exists.
	 * 
	 * @param prefs The preferences to modify.
	 * @param def The RDF resource that defined the handler
	 * @param value The value passed from the command line or java property.
	 */
	public void invoke(PrefsManager prefs, Resource def, String value);
}
