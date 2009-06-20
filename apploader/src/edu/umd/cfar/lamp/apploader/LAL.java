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
import com.hp.hpl.jena.vocabulary.*;

/**
 * Namespace schema for LAMP AppLoader.
 * 
 * For information, see the
 * <a href="http://viper-toolkit.sourceforge.net/owl/apploader">namespace
 * document</a>.
 * 
 * @author davidm
 */
public class LAL {
	protected static final String uri =
		"http://viper-toolkit.sourceforge.net/owl/apploader#";

	/** 
	 * Returns the URI for this schema,
	 * <code>http://viper-toolkit.sourceforge.net/owl/apploader#</code>.
	 * @return the URI for this schema
	 */
	public static String getURI() {
		return uri;
	}


	/**
	 * The user directory, e.g. <code>{ [] lal:userDirectory "~/.fooApp" 
	 * . } </code> It should probably be computed
	 * on the fly using a trigger on the longName 'user.home'
	 * per the java standard.
	 */
	public static final Property userDirectory =
		ResourceFactory.createProperty(uri + "userDirectory");
	
	/**
	 * The install directory, where other useful files may be found.
	 * Doesn't have to be set if the directiry is where the system.n3 
	 * file is loaded from.
	 */
	public static final Property systemDirectory =
		ResourceFactory.createProperty(uri + "systemDirectory");

	/**
	 * An instance of a java object.
	 */
	public static final Resource Bean =
		ResourceFactory.createResource(uri + "Bean");

	/**
	 * A java object that is loaded when the application is loaded and 
	 * deleted after it ends.
	 */
	public static final Resource ApplicationBean =
		ResourceFactory.createResource(uri + "ApplicationBean");

	/**
	 * A bean that fits the specification for a bean that
	 * is relative to the current object, but doesn't get
	 * stored and may be reallocated as required. Multiple
	 * copies may be loaded at once, and there is no explicit
	 * way to globally refer to them (using the uri will just
	 * make more).
	 */
	public static final Resource TemporaryBean =
		ResourceFactory.createResource(uri + "TemporaryBean");

	/**
	 * URI that means 'the parent bean'.
	 */
	public static final Resource Parent =
		ResourceFactory.createResource(uri + "Parent");

	/**
	 * The name of the java class of which a bean is an instance.
	 */
	public static final Property className =
		ResourceFactory.createProperty(uri + "className");

	/**
	 * References a bean that must be loaded before this one, or that
	 * it uses in some way.
	 */
	public static final Property requires =
		ResourceFactory.createProperty(uri + "requires");

	/**
	 * The URI of the given resource. It might be a good idea to 
	 * replace this with rdf:subject, but that is pushing the use
	 * of the reification property.
	 */
	public static final Property addressOf =
		ResourceFactory.createProperty(uri + "addressOf");
	
	/**
	 * The bean to which to add this. Will call the 
	 * specified bean's 'add' method with the subject bean
	 * as the parameter. 
	 */
	public static final Property addTo =
		ResourceFactory.createProperty(uri + "addTo");

	/**
	 * The ApplicationBean that refers to the current
	 * instance of the AppLoader.
	 */
	public static final Resource Core =
		ResourceFactory.createResource(uri + "Core");

	/**
	 * The name of the shell command that invoked this instance of 
	 * the application. This is used for the '-h' command line
	 * output, or the application bar title, if there is no dc:title
	 * or rdf:label.
	 */
	public static final Property shellCommand =
		ResourceFactory.createProperty(uri + "shellCommand");

	/**
	 * An icon representing the bean. It will be used when 
	 * the bean is used in a context where it is required. 
	 * The target should be the uri of the bean, although it 
	 * can have an alternative location, using the '=' command, 
	 * and an lal:forLanguage.
	 */
	public static final Property icon =
		ResourceFactory.createProperty(uri + "icon");


	/**
	 * Description of a property on a bean.
	 */
	public static final Resource BeanProperty =
		ResourceFactory.createResource(uri + "BeanProperty");

	/**
	 * What bean the property is attached to.
	 */
	public static final Property propertyOf =
		ResourceFactory.createProperty(uri + "propertyOf");

	/**
	 * Set the given property to the described value.
	 */
	public static final Property setProperty =
		ResourceFactory.createProperty(uri + "setProperty");

	/**
	 * The name of the property.
	 */
	public static final Property propertyName =
		ResourceFactory.createProperty(uri + "propertyName");

	/**
	 * The value to which to set the property
	 */
	public static final Property propertyValue =
		ResourceFactory.createProperty(uri + "propertyValue");


	/**
	 * The class of OWL nodes that describe java action bindings.
	 */
	public static final Resource ActionListener =
		ResourceFactory.createResource(uri + "ActionListener");
	
	/**
	 * Binds an {@link #ActionListener} to a {@link #Bean}.
	 */
	public static final Property listenerBean =
		ResourceFactory.createProperty(uri + "listenerBean");
	
	/**
	 * The bean property name to check for on the listener bean
	 * to get the listener. Meaning, the bean has to have a
	 * get<i>ListenerType</i> method, where <i>ListenerType</i>
	 * is the value of this property.
	 */
	public static final Property listenerType =
		ResourceFactory.createProperty(uri + "listenerType");

	/**
	 * All instances of an action.
	 */
	public static final Resource Action =
		ResourceFactory.createResource(uri + "Action");
	/**
	 * Selects a listener to receive the action event.
	 */
	public static final Property sendsTo =
		ResourceFactory.createProperty(uri + "sendsTo");
	/**
	 * The string to send with the action.
	 */
	public static final Property actionCommand =
		ResourceFactory.createProperty(uri + "actionCommand");

	/**
	 * Attempts to be the equivalent of the <code>^^ln</code>
	 * for strings for Resources.
	 */
	public static final Property lang =
		ResourceFactory.createProperty(uri + "lang");

	/**
	 * Indicates the name of the bean's currently loaded document.
	 * This probably won't be serialized, but instead kept in the 
	 * temporary preferences context.
	 */
	public static final Property documentName =
		ResourceFactory.createProperty(uri + "documentName");

	/**
	 * Indicates if bean's currently loaded document has been modified
	 * since it has been saved. A boolean property.
	 * This probably won't be serialized, but instead kept in the 
	 * temporary preferences context.
	 */
	public static final Property documentModified =
		ResourceFactory.createProperty(uri + "documentModified");

	
	/**
	 * Element used for invoking arbitrary parameters.
	 * Either use the bean :invoke method
	 * syntax or the method :invokedOn bean syntax.
	 * The second method may allow using the returned value. 
	 * This is dangerous, as this is run during initialization,
	 * which is nondeterministic.
	 */
	public static final Resource MethodInvocation =
		ResourceFactory.createResource(uri + "MethodInvocation");

	/**
	 * Invokes an arbitrary function. used in the form 
	 * [a bean] invoke [a MethodInvocation].
	 */
	public static final Property invoke =
		ResourceFactory.createProperty(uri + "invoke");

	/**
	 * Invokes an arbitrary function when applied
	 * as a bean property value. used in the form 
	 * [a MethodInvocation] invokedOn [a bean].
	 */
	public static final Property invokedOn =
		ResourceFactory.createProperty(uri + "invokedOn");

	/**
	 * The name of the method to invoke.
	 */
	public static final Property methodName =
		ResourceFactory.createProperty(uri + "methodName");

	/**
	 * The parameters of the method.
	 */
	public static final Property parameters =
		ResourceFactory.createProperty(uri + "parameters");

	/**
	 * The classes of the parameters of the method. Use the 
	 * java class name mangling, or actual classes (somehow).
	 */
	public static final Property parameterTypes =
		ResourceFactory.createProperty(uri + "parameterTypes");
	

	
	
	
	/**
	 * If this returns true, then the resource describes a bean.
	 * 
	 * @param r The resource to check
	 * @return <code>true</code> when the resource is a Bean.
	 * This is necessary, because I haven't hooked up an OWL reasoner
	 * to the apploader yet.
	 */
	public static final boolean aBean(Resource r) {
		return r.hasProperty(RDF.type, LAL.Bean)
			|| r.hasProperty(RDF.type, LAL.ApplicationBean)
			|| r.hasProperty(RDF.type, LAL.TemporaryBean);
	}
	
	/**
	 * Tests to see if the resource describes, specifically, 
	 * a temporary bean.
	 * @param r the resource to check. 
	 * @return <code>true</code> if the resources
	 * is <code>a lal:TemporaryBean</code>.
	 */
	public static final boolean aTemporaryBean(Resource r) {
		return r.hasProperty(RDF.type, LAL.TemporaryBean);
	}

	/**
	 * Tests to see if the resource describes 
	 * an application bean.
	 * @param r the resource to check. 
	 * @return <code>true</code> if the resources
	 * is <code>a lal:ApplicationBean</code>.
	 */
	public static final boolean anApplicationBean(Resource r) {
		return r.hasProperty(RDF.type, LAL.ApplicationBean);
	}
}
