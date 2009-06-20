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

import java.lang.reflect.*;
import java.util.*;

import com.hp.hpl.jena.rdf.model.*;

import edu.umd.cfar.lamp.apploader.*;
import edu.umd.cfar.lamp.apploader.prefs.*;

/**
 * An object for manipulating a field on a property sheet. It includes
 * methods for setting and getting the values of the field, as well as 
 * displaying it and editing it. It uses the AppLoader preferences model to 
 * get information about how to edit the property.
 * </p>
 * <p>
 * For example, given the RDF description of a property below:
 * <pre>
 *	[	lal:propertyName "AttrName" ;
 *		props:interfacer :setAttrName ;
 *		props:renderer :attrConfigNameEditor ;
 *		props:editor :attrConfigNameEditor ;
 *		rdfs:label "Name"@en , "Nom"@fr ]
 * </pre>
 * </p>
 * <p>
 * Then there must exist a bean with a property given the 
 * text description "Name" (in english) that has the java name 
 * "AttrName". The preferences may also define an interfacer,
 * as a java bean (see the apploader information) with the uri :setAttrName
 * and the same bean for the renderer and editor with the uri 
 * :setConfigNameEditor. Without an interfacer, it uses the 
 * java name to find the javabeans methods 'getJname' and 
 * 'setJname'.
 * <pre>
 * :setAttrName
 * 		a lal:Bean ;
 *		rdfs:label "Function Object to Set an Attribute Name"@en ;
 *		lal:className "edu.umd.cfar.lamp.viper.gui.config.SetAttrName" .
 * :attrConfigNameEditor
 *		a props:Editor ;
 *		lal:className "edu.umd.cfar.lamp.viper.gui.config.AttrNameEditor" ;
 *		lal:setProperty [ 
 *			lal:propertyName "Text" ] .
 * </pre>
 * The setAttrName is a PropertyInterfacer, impelemnted by the class specified
 * by lal:className. The label isn't used currently.
 * </p>
 */
public class ForClassPropertyDescriptor extends ExplicitPropertyDescriptor {
	/**
	 * A simple property interfacer that uses reflection to get 
	 * the expected names for the set and get methods. Note that this
	 * will assume that the property is always applicable, so long
	 * as it has a getter or a setter for the given property name.
	 * @author davidm
	 */
	private static class BasicProperty extends PropertyAdapter {
		private Class propType;
		/**
		 * @see edu.umd.cfar.lamp.apploader.propertysheets.PropertyInterfacer#getPropertyClass()
		 */
		public Class getPropertyClass() {
			return propType;
		}
		/**
		 * Creates a new property interfacer using java bean naming patterns.
		 * @param javaName The name of the property
		 * @param beanType the class of the bean with the given property
		 */
		public BasicProperty(String javaName, Class beanType) {
			String setName = AppLoader.toBeanSet(javaName);
			String getName = AppLoader.toBeanGet(javaName);
			String isName = AppLoader.toBeanIs(javaName);
			
			Method[] M = beanType.getMethods();
			HashSet setOfSets = new HashSet();
			// getter is authorative source for property type
			// should be able to assign set value to get value
			// but the reverse may require a cast
			// First, get the getter value, then find the
			// most general setter that accepts the get value
			for (int i = 0; i < M.length; i++) {
				if (M[i].getParameterTypes().length == 0) {
					String currName = M[i].getName();
					if (currName.equals(getName) || currName.equals(isName)) {
						getter = M[i];
						propType = getter.getReturnType();
					}
				} else if (M[i].getParameterTypes().length == 1) {
					if (M[i].getName().equals(setName)) {
						setOfSets.add(M[i]);
					}
				}
			}
			for (Iterator iter = setOfSets.iterator(); iter.hasNext(); ) {
				Method possible = (Method) iter.next();
				Class nType = possible.getReturnType();
				if (setter == null) {
					if (nType.isAssignableFrom(propType) || propType.isAssignableFrom(nType)) {
						setter = possible;
					}
				} else if (nType.isAssignableFrom(setter.getReturnType())) {
					setter = possible;
				}
			}
		}
	}
	
	/**
	 * Creates a new descriptor for the property of the given instance 
	 * as described in the preferences by the resource <code>prop</code>
	 * @param prefs The apploader prefs that describe this resource.
	 * @param prop The RDF node that represents this kind of bean property
	 * @param bean The instance to describe
	 * @throws PreferenceException
	 */
	public ForClassPropertyDescriptor(PrefsManager prefs, Resource prop, Object bean) throws PreferenceException {
		super(prefs, prop, bean);
		if (!prop.hasProperty(PROPS.interfacer)) {
			String javaName = prop.getProperty(LAL.propertyName).getString();
			Class beanType = bean.getClass();
			super.setProxy (new BasicProperty(javaName, beanType));
		}
	}
}