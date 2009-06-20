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

package edu.umd.cfar.lamp.apploader.propertysheets;

import java.util.*;

import com.hp.hpl.jena.rdf.model.*;

import edu.umd.cfar.lamp.apploader.*;

/**
 * A property list that is defined for all instances of a given class. It may
 * have extended properties that are specific to individual instances.
 */
public class ForClassPropertyList extends DescriberBasedProperties {
	/**
	 * Get all the describer resources for this bean type.
	 * 
	 * @return all describers that <code>prop:describes</code> this bean class
	 *         or any of its ancestors.
	 */
	public Collection getAllDescribers() {
		if (getObject() == null) {
			return Collections.EMPTY_LIST;
		}
		return getAllDescribersFor(getObject().getClass());
	}

	/**
	 * Gets all RDF resources that are declared as property describers for the
	 * given class or any of its superclasses/superinterfaces.
	 * 
	 * @param c
	 *            The class to search for describers of
	 * @return All describers found in the application prefs for the class or
	 *         its ancestors.
	 */
	private Set getAllDescribersFor(Class c) {
		if (null == c || null == getPrefs()) {
			return new HashSet();
		}

		getPrefs().model.enterCriticalSection(ModelLock.READ);
		try {
			// Get describers for superclass
			Set l = getAllDescribersFor(c.getSuperclass());

			// Get describers for implemented interfaces
			Class[] interfaces = c.getInterfaces();
			for (int i = 0; i < interfaces.length; i++) {
				l.addAll(getAllDescribersFor(interfaces[i]));
			}

			ResIterator iter = getPrefs().model.listSubjectsWithProperty(
					PROPS.describes, c.getName());
			while (iter.hasNext()) {
				l.add(iter.next());
			}
			return l;
		} finally {
			getPrefs().model.leaveCriticalSection();
		}
	}
}