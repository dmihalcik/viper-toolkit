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

import javax.swing.*;

import com.hp.hpl.jena.rdf.model.*;
import com.hp.hpl.jena.vocabulary.*;

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
 * "AttrName". The preferences must also define an interfacer,
 * as a java bean (see the apploader information) with the uri :setAttrName
 * and the same bean for the renderer and editor with the uri 
 * :setConfigNameEditor. These are described below:
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
public class ExplicitPropertyDescriptor implements InstancePropertyDescriptor {
	private PropertyInterfacer proxy;
	private String displayName;
	private Class beanType;
	private Resource res;

	/**
	 * Tests to see that this and the other object are both
	 * explicit property descriptors with the same proxy.
	 * @param o the object to compare to
	 * @return <code>true</code> when this and <code>o</code> refer to
	 * equal proxies
	 */
	public boolean equals(Object o) {
		if (this == o) {
			return true;
		} else if (o instanceof InstancePropertyDescriptor) {
			ExplicitPropertyDescriptor that = (ExplicitPropertyDescriptor) o;
			return proxy == that.proxy || (proxy != null && proxy.equals(that.proxy));
		} else {
			return false;
		}
	}
	/**
	 * Computes the hash code on the proxy object.
	 * @return a hash code
	 */
	public int hashCode() {
		return proxy.hashCode();
	}
	
	/**
	 * Creates a new descriptor for the property of the given instance 
	 * as described in the preferences by the resource <code>prop</code>
	 * @param prefs The apploader prefs that describe this resource.
	 * @param prop The RDF node that represents this kind of bean property
	 * @param bean The instance to describe
	 * @throws PreferenceException
	 */
	public ExplicitPropertyDescriptor(PrefsManager prefs, Resource prop, Object bean) throws PreferenceException {
		displayName = prefs.getLocalizedString(prop, RDFS.label);
		res = prop;
		beanType = bean.getClass();

		initialize(prefs, bean);
	}
	/**
	 * Set the interfacer proxy.
	 * @param proxy
	 */
	protected void setProxy(PropertyInterfacer proxy) {
		this.proxy = proxy;
	}
	private void initialize (PrefsManager prefs, Object bean) throws PreferenceException {
		if (res.hasProperty(PROPS.interfacer)) {
			Resource n = res.getProperty(PROPS.interfacer).getResource();
			Object f = prefs.getCore().loadBeanFromResource(n);
			prefs.getCore().initializeBeanFromResource(f, bean, n);
			this.setInterfacer((PropertyInterfacer) f);
		}
	}
	
	/**
	 * Get the value from the bean instance.
	 * @param bean The instance to get this property from 
	 * @return The value of the bean
	 * @throws PropertyException When the value can't be extracted.
	 */
	public Object applyGetter(Object bean) {
		return proxy.getValue(bean);
	}

	/**
	 * Set the value of this property on the given bean
	 * @param bean The instance to set this property of
	 * @param toValue The new value for the property
	 * @throws PropertyException When the value can't be set.
	 */
	public void applySetter(Object bean, Object toValue) {
		proxy.setValue(bean, toValue);
	}
	
	/**
	 * Checks to see if the property is currently settable on the 
	 * given instance bean.
	 * @param bean the instance to check
	 * @return <code>false</code> if the bean may not be set
	 */
	public boolean isSettable(Object bean) {
		return proxy != null && proxy.isWritableOn(bean) && proxy.isWritable();
	}
	

	private JComponent editor;
	private Resource editorResource;
	private JComponent renderer;
	private Resource rendererResource;

	/**
	 * @see edu.umd.cfar.lamp.apploader.propertysheets.InstancePropertyDescriptor#getEditor(java.lang.Object, edu.umd.cfar.lamp.apploader.AppLoader)
	 */
	public JComponent getEditor (Object bean, AppLoader core) throws PreferenceException {
		if (editorResource == null) {
			if (!res.hasProperty(PROPS.editor)) {
				if (editor == null) {
					editor = new JLabel();
				}
				((JLabel) editor).setText(bean.toString());
				return editor;
			} else {
				editorResource = res.getProperty(PROPS.editor).getResource();
				editor = (JComponent) core.rdfNodeToValue(editorResource, bean);
			}
		}
		core.initializeBeanFromResource(editor, bean, editorResource);
		return editor;
	}

	/**
	 * @see edu.umd.cfar.lamp.apploader.propertysheets.InstancePropertyDescriptor#getRenderer(java.lang.Object, edu.umd.cfar.lamp.apploader.AppLoader)
	 */
	public JComponent getRenderer (Object bean, AppLoader core) throws PreferenceException {
		if (rendererResource == null) {
			if (!res.hasProperty(PROPS.renderer)) {
				if (renderer == null) {
					renderer = new JLabel();
				}
				((JLabel) renderer).setText(bean.toString());
				return renderer;
			} else {
				rendererResource = res.getProperty(PROPS.renderer).getResource();
				String className = rendererResource.getProperty (LAL.className).getString();
				renderer = (JComponent) AppLoader.loadObjectFromName(className);
			}
		}
		core.initializeBeanFromResource(renderer, bean, rendererResource);
		return renderer;
	}
	
	/**
	 * Gets the property functor.
	 * @return functor the property interfacer for the current
	 * property on the current instance object, as set by 
	 * {@link #setInterfacer(PropertyInterfacer)}.
	 */
	public PropertyInterfacer getInterfacer() {
		return proxy;
	}
	/**
	 * Sets the property functor.
	 * @param functor the property interfacer for the current
	 * property on the current instance object
	 */
	public void setInterfacer(PropertyInterfacer functor) {
		proxy = functor;
	}
	
	/**
	 * Gets the class of the bean.
	 * @return The java class that the associated bean is an instnce of 
	 */
	public Class getBeanType() {
		return beanType;
	}

	/**
	 * Gets the display name of the property.
	 * @return The (possibly localized) display name
	 */
	public String getName() {
		return displayName;
	}

}