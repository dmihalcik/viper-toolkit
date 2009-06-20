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
import java.util.logging.*;

import javax.swing.event.*;

import com.hp.hpl.jena.rdf.model.*;
import com.hp.hpl.jena.vocabulary.*;

import edu.umd.cfar.lamp.apploader.*;
import edu.umd.cfar.lamp.apploader.misc.*;
import edu.umd.cfar.lamp.apploader.prefs.*;

/**
 * A set of properties for an object that are specified
 * in the preferences.
 */
public abstract class DescriberBasedProperties
	extends FlattenedList
	implements InstancePropertyList {

	/// The object that we are taking properties of
	private Object bean;

	/// Logger for error messages and so forth
	private Logger logger;

	/// Maps listener RDF objects to their currently registered implementations
	private Map objectListeners = new HashMap();

	/// Application configuration
	private PrefsManager prefs;

	/**
	 * Creates a new, empty set of properties.
	 */
	public DescriberBasedProperties() {
		super(new ArrayList());
		logger = Logger.getLogger("edu.umd.cfar.lamp.apploader.propertysheets");
		getInnerList().add(new ExplicitProperties());
	}

	/**
	 * Create a new AllProperties list that 
	 * uses the given child <code>InstancePropertyList</code>s.
	 * @param inside The property lists to aggregate
	 */
	public DescriberBasedProperties(List inside) {
		super(inside);
		logger = Logger.getLogger("edu.umd.cfar.lamp.apploader.propertysheets");
	}

	/**
	 * Removes all properties, and listeners for changes to those
	 * properties.
	 * @see java.util.List#clear()
	 */
	public void clear() {
		removeAllObjectListeners();
		ExplicitProperties exp = (ExplicitProperties) getInnerList().get(0);
		exp.clear();
		getInnerList().clear();
		getInnerList().add(exp);
	}

	/**
	 * A harsher form of refresh, this also changes the extensions
	 * and resets the listeners.
	 */
	public void reset() {
		clear();
		loadDescribers();
	}

	/**
	 * 
	 */
	private void loadDescribers() {
		Collection describers = getAllDescribers();
		Iterator iter = describers.iterator();
		while (iter.hasNext()) {
			Resource curr = (Resource) iter.next();
			parsePropertyDescriber(curr);
		}
	}

	/**
	 * Resets all of the child property lists (extended properties).
	 */
	protected void resetChildren() {
		Iterator ipls = getInnerList().iterator();
		// Get all the child instance property lists
		while (ipls.hasNext()) {
			InstancePropertyList curr = (InstancePropertyList) ipls.next();
			curr.setObject(bean);
		}
	}

	/**
	 * Refreshes all of the child property lists (extended properties).
	 */
	protected void refreshChildren() {
		Iterator ipls = getInnerList().iterator();
		// Get all the child instance property lists
		while (ipls.hasNext()) {
			InstancePropertyList curr = (InstancePropertyList) ipls.next();
			curr.refresh();
		}
	}

	/**
	 * Sets the instance object, but assumes that it
	 * has all the old properties as the last one
	 * and doesn't refresh or reset anything.
	 * @param o The new instance object.
	 */
	protected void justSetObject(Object o) {
		bean = o;
	}

	/**
	 * Sets the object and resets the list of 
	 * properties.
	 * @param o the object to set to
	 */
	public void setObject(Object o) {
		clear();
		justSetObject(o);
		loadDescribers();
	}

	/**
	 * Gets the current instance object.
	 * @return the current instance object
	 */
	public Object getObject() {
		return bean;
	}

	private InvocationHandler listenerProxyHandler = new MyInvocationHandler();

	/**
	 * 
	 * @param parent
	 * @param child
	 * @return
	 */
	private static boolean checkMethod(Method parent, Method child) {
		if (!parent.getName().equals(child.getName())) {
			return false;
		}
		Class[] parentParams = parent.getParameterTypes();
		Class[] childParams = child.getParameterTypes();
		if (childParams.length != parentParams.length) {
			return false;
		}
		if (!parent.getReturnType().isAssignableFrom(child.getReturnType())) {
			return false;
		}
		for (int i = 0; i < childParams.length; i++) {
			if (!parentParams[i].isAssignableFrom(childParams[i])) {
				return false;
			}
		}
		return true;
	}
	private class MyInvocationHandler implements InvocationHandler {
		/**
		 * Called for any method on the proxy object
		 * @see java.lang.reflect.InvocationHandler#invoke(java.lang.Object, java.lang.reflect.Method, java.lang.Object[])
		 */
		public Object invoke(Object proxy, Method method, Object[] args)
			throws Throwable {
			Method[] M = MyInvocationHandler.class.getMethods();
			Method invokeM =
				MyInvocationHandler.class.getMethod(
					"invoke",
					new Class[] { Object.class, Method.class, Object[].class });
			if (method.equals(invokeM)) {
				return null;
			}
			for (int i = 0; i < M.length; i++) {
				if (checkMethod(method, M[i])) {
					assert null != this;
					return method.invoke(this, args);
				}
			}
			if (changeListeners.getListenerCount() > 0) {
				ChangeEvent e = new ChangeEvent(bean);
				Object[] L = changeListeners.getListenerList();
				for (int i = L.length - 2; i >= 0; i -= 2) {
					if (L[i] == ChangeListener.class) {
						((ChangeListener) L[i+1]).stateChanged(e);
					}
				}
			}
			return null;
		}
		/**
		 * true if the proxies are the same
		 * @see java.lang.Object#equals(java.lang.Object)
		 */
		public boolean equals(Object other) {
			if (this == other) {
				return true;
			} else if (other instanceof MyInvocationHandler) {
				return DescriberBasedProperties.this.equals(
					((MyInvocationHandler) other).outer());
			} else {
				return false;
			}
		}
		Object outer() {
			return DescriberBasedProperties.this;
		}
		/**
		 * hash based on the outer class
		 * @see java.lang.Object#hashCode()
		 */
		public int hashCode() {
			assert null != this;
			assert DescriberBasedProperties.this != null;
			return ~DescriberBasedProperties.this.hashCode();
		}
	};

	/**
	 * Attempts to add a listener to the object
	 * using the given object listener description.
	 * @param olr An RDF description of a listener to attach to the current instance
	 * @throws PreferenceException If the listener is not as described
	 * or if the description is faulty in other ways
	 */
	private void addObjectListener(Resource olr) throws PreferenceException {
		String name = getListenerName(olr);
		String type = prefs.getLocalizedString(olr, PROPS.listenerType);
		try {
			ClassLoader loader = getClass().getClassLoader();
			Class[] listenerClasses = new Class[] { loader.loadClass(type)};
			Class proxyClass = Proxy.getProxyClass(loader, listenerClasses);
			Class[] handlerClasses = new Class[] { InvocationHandler.class };
			Constructor con = proxyClass.getConstructor(handlerClasses);
			Object l = con.newInstance(new Object[] { listenerProxyHandler });

			Class beanClass = bean.getClass();

			Method amethod = beanClass.getMethod("add" + name, listenerClasses);
			amethod.invoke(bean, new Object[] { l });

			objectListeners.put(olr, l);
		} catch (ClassNotFoundException e) {
			throw new PreferenceException(
				"Not a valid listener class: " + type);
		} catch (SecurityException e) {
			throw new RuntimeException(e);
		} catch (NoSuchMethodException e) {
			throw new PreferenceException(
				"Not a valid listener class: " + type);
		} catch (IllegalArgumentException e) {
			throw new PreferenceException(
				"Not a valid listener class: " + type);
		} catch (InstantiationException e) {
			throw new RuntimeException(e);
		} catch (IllegalAccessException e) {
			throw new RuntimeException(e);
		} catch (InvocationTargetException e) {
			throw new RuntimeException(
				"While trying to add listener "
					+ type
					+ " to object with name "
					+ name,
				e);
		}

	}

	/**
	 * Remove all dynamically generated object listeners from
	 * the current instance object.
	 */
	private void removeAllObjectListeners() {
		Iterator iter = objectListeners.entrySet().iterator();
		while (iter.hasNext()) {
			Map.Entry curr = (Map.Entry) iter.next();
			Resource olr = (Resource) curr.getKey();
			Object[] l = new Object[] { curr.getValue()};
			String type = prefs.getLocalizedString(olr, PROPS.listenerType);
			ClassLoader loader = getClass().getClassLoader();
			Class beanClass = bean.getClass();
			String listName = getListenerName(olr);
			try {
				Class[] listenerClasses = new Class[] { loader.loadClass(type)};
				Method rmethod =
					beanClass.getMethod("remove" + listName, listenerClasses);
				rmethod.invoke(bean, l);
			} catch (SecurityException e) {
				logger.severe("Security error");
				throw new RuntimeException(e);
			} catch (NoSuchMethodException e) {
				logger.severe("Configuration Error");
				throw new RuntimeException(e);
			} catch (IllegalArgumentException e) {
				throw new RuntimeException(e);
			} catch (IllegalAccessException e) {
				throw new RuntimeException(e);
			} catch (InvocationTargetException e) {
				throw new RuntimeException(e);
			} catch (ClassNotFoundException e) {
				e.printStackTrace();
			}
		}
		objectListeners.clear();
	}

	/**
	 * Gets the name of the listener defined at the resource.
	 * @param olr Resource that is a props:Listener
	 * @return The listener name
	 */
	private String getListenerName(Resource olr) {
		String pname = prefs.getLocalizedString(olr, PROPS.listenerName);
		return pname.substring(0, 1).toUpperCase() + pname.substring(1);
	}

	/**
	 * Gets all the explicit properties associated
	 * with the given resource and adds them to <code>this</code>.
	 * @param describer RDF resource that is a props:Describer
	 */
	public void getExplicitPropertiesFor(Resource describer) {
		ExplicitProperties exp = (ExplicitProperties) getInnerList().get(0);
		if (describer.hasProperty(PROPS.propertyOrder)) {
			Resource cons =
				describer.getProperty(PROPS.propertyOrder).getResource();
			while (!cons.equals(RDF.nil)) {
				Resource beanProp = cons.getProperty(RDF.first).getResource();
				try {
					InstancePropertyDescriptor opd =
						new ForClassPropertyDescriptor(prefs, beanProp, bean);
					if (!exp.contains(opd)) {
						exp.add(opd);
					}
				} catch (PreferenceException e) {
					logger.warning(
						"Error while trying to load set " + beanProp + ": "
							+ e.getLocalizedMessage());
				}
				cons = cons.getProperty(RDF.rest).getResource();
			}
		} else {
			StmtIterator propIter = describer.listProperties(PROPS.hasProperty);
			while (propIter.hasNext()) {
				Resource beanProp = propIter.nextStatement().getResource();
				try {
					InstancePropertyDescriptor opd =
						new ForClassPropertyDescriptor(prefs, beanProp, bean);
					if (!exp.contains(opd)) {
						exp.add(opd);
					}
				} catch (PreferenceException e) {
					logger.warning(
						"Error while trying to load set " + beanProp + ": "
							+ e.getLocalizedMessage());
				}
			}
		}
	}
	/**
	 * Gets all the extended properties associated
	 * with the given resource and the current instance object
	 * and adds them to <code>this</code>.
	 * @param describer RDF resource that is a props:Describer
	 */
	public void getExtendedPropertiesFor(Resource describer) {
		StmtIterator iter = describer.listProperties(PROPS.extendedProperties);
		while (iter.hasNext()) {
			Resource extResource = iter.nextStatement().getResource();
			Object extInstance;
			try {
				extInstance = prefs.getCore().rdfNodeToValue(extResource, bean);
				if (extInstance instanceof InstancePropertyList) {
					getInnerList().add(extInstance);
				} else {
					logger.warning(
						"Error while loading extensions for bean: "
							+ bean
							+ "\n\t-from describer "
							+ describer);
				}
			} catch (PreferenceException e) {
				logger.warning(
					"Error while loading extensions for bean: "
						+ bean
						+ "\n\t-from describer "
						+ describer);
				e.printStackTrace();
			}
		}
	}
	/**
	 * Gets all the listeners types associated with the given
	 * resource and adds instances of them (through
	 * dynamic proxying) to the current instance object.
	 * @param describer RDF resource that is a props:Describer
	 */
	public void addListenersFor(Resource describer) {
		StmtIterator iter = describer.listProperties(PROPS.addListener);
		while (iter.hasNext()) {
			Resource r = iter.nextStatement().getResource();
			try {
				addObjectListener(r);
			} catch (PreferenceException e) {
				logger.severe(
					"Error in config for property listener: "
						+ e.getLocalizedMessage());
			}
		}
	}

	/**
	 * Sets up the describer-based properties using
	 * the current instance object and the passed props:Describer
	 * @param describer a resource that is a props:Describer
	 */
	protected void parsePropertyDescriber(Resource describer) {
		getExplicitPropertiesFor(describer);
		getExtendedPropertiesFor(describer);
		addListenersFor(describer);
	}

	/**
	 * Gets all property describers.
	 * @return the property describers for the object
	 */
	public abstract Collection getAllDescribers();

	private EventListenerList changeListeners = new EventListenerList();
	
	/**
	 * Adds a listener for changes to the watched properties.
	 * @param l the listener to add
	 */
	public void addChangeListener(ChangeListener l) {
		changeListeners.add(ChangeListener.class, l);
	}
	
	/**
	 * Removes a listener for changes to the watched properties.
	 * @param l the listener to remove
	 */
	public void removeChangeListener(ChangeListener l) {
		changeListeners.remove(ChangeListener.class, l);
	}
	
	/**
	 * Gets all current change listeners.
	 * @return all current change listeners
	 */
	public ChangeListener[] getChangeListeners() {
		return (ChangeListener[]) changeListeners.getListeners(ChangeListener.class);
	}

	/**
	 * Gets the application preferences associated with this
	 * properties object.
	 * @return The application preferences.
	 */
	public PrefsManager getPrefs() {
		return prefs;
	}

	/**
	 * Sets the application preferences associated with this
	 * properties object. 
	 * @param manager The application preferences
	 */
	public void setPrefs(PrefsManager manager) {
		prefs = manager;
		if (null != prefs && getObject() != null) {
			reset();
		}
	}

	/**
	 * Refreshes the extended attributes, but nothing else.
	 * (doesn't change explicit properties or listeners)
	 */
	public void refresh() {
		assert(null != getPrefs());
		if (null != getObject()) {
			ExplicitProperties exp = (ExplicitProperties) getInnerList().get(0);
			exp.clear();

			Iterator descs = getAllDescribers().iterator();
			while (descs.hasNext()) {
				Resource r = (Resource) descs.next();
				getExplicitPropertiesFor(r);
			}
		}

		refreshChildren();
	}
}
