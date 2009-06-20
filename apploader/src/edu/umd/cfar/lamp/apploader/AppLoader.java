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

import java.awt.*;
import java.awt.Container;
import java.awt.event.*;
import java.io.*;
import java.lang.reflect.*;
import java.net.*;
import java.util.*;
import java.util.List;
import java.util.logging.*;

import javax.imageio.*;
import javax.swing.*;

import net.roydesign.app.*;
import viper.api.extensions.*;

import com.hp.hpl.jena.datatypes.*;
import com.hp.hpl.jena.mem.*;
import com.hp.hpl.jena.rdf.model.*;
import com.hp.hpl.jena.vocabulary.*;

import edu.umd.cfar.lamp.apploader.misc.*;
import edu.umd.cfar.lamp.apploader.prefs.*;

/**
 * A module loader uses the module rdf format to set up its menus and loads
 * modules as beans in seperate windows.
 * 
 * It extends JFrame, providing the main menu. The menu contains all the items
 * specified in the preferences as attached to lal:Core, the uri for the running
 * instance of this bean.
 * 
 * See the <a
 * href="http://viper-toolkit.sourceforge.net/owl/apploader">AppLoader owl
 * schema </a> for more information.
 * 
 * @author davidm
 */
public class AppLoader extends JFrame {
	/// Used to output error messages, warnings, and other notes
	private static Logger logger = Logger.getLogger("edu.umd.cfar.lamp.apploader");

	/// The triple store used to define the program and its current state.
	private PrefsManager prefs;

	/// The currently loaded beans (by uri)
	private Map resources = new HashMap();

	/// A list of the currently open windows; useful for the 'show window' menu
	// options
	private Map openWindows = new HashMap();

	/// Manages the JMenu attached to the application from the preferences
	// file.
	private AppLoaderMenu menuManager;

	///
	private HotkeyProcessor hotkeyManager;

	/// Indicates that the current platform conforms to the Max OS X HCI
	/// guidelines, instead of the windows/sun model
	private static boolean mac = (System.getProperty("mrj.version") != null);

	/**
	 * Create a new application window for the application described in the
	 * referenced preferences.
	 * 
	 * @param prefs
	 *            Preferences (RDF Triples) that describes an application using
	 *            the apploader schemata
	 * @throws RDFException
	 *             Something is wrong with the graph
	 * @throws PreferenceException
	 *             Something is wrong with the graph's semantics
	 */
	public AppLoader(PrefsManager prefs) throws RDFException,
			PreferenceException {
		this.prefs = prefs;
		this.prefs.setCore(this);
		this.menuManager = new AppLoaderMenu();
		this.menuManager.setCore(this);

		this.hotkeyManager = new HotkeyProcessor();
		this.hotkeyManager.setCore(this);
		KeyboardFocusManager kfm;
		kfm = KeyboardFocusManager.getCurrentKeyboardFocusManager();
		kfm.addKeyEventDispatcher(this.hotkeyManager);
		kfm.addKeyEventPostProcessor(this.hotkeyManager);

		resources.put(LAL.Core, this);
		setDefaultCloseOperation(javax.swing.WindowConstants.DO_NOTHING_ON_CLOSE);
		addWindowListener(new WindowAdapter() {
			public void windowClosing(WindowEvent e) {
				tryToShutdown();
			}
		});
		Application macApp = Application.getInstance();
		macApp.getQuitJMenuItem().addActionListener(getExitListener());
		setTitle("LAMP AppLoader"); // replaced during loadBeans
		setSize(426, 335);
		loadBeans();
		menuManager.resetMenu(this.getRootPane());
		setSize(getPreferredSize());
		validate();
		pack();
		setVisible(true);
	}
	private void tryToShutdown() {
		if (vetoCloseAction != null) {
			vetoCloseAction.attempt(shutdownRunnable);
		} else {
			shutdownRunnable.run();
		}
	}
	private AttemptToPerformAction vetoCloseAction = null;
	private Runnable shutdownRunnable = new Runnable() {
		public void run() {
			shutdown();
			System.exit(0);
		}
	};

	private static class BeanSort implements GraphUtilities.ObjectSorder {
		Property backlink;
		Model model;
		public Iterator getOutboundLinks(Object node) {
			return model.listSubjectsWithProperty(backlink, (Resource) node);
		}
	}

	/**
	 * Sorts the given map resources using the backlink property.
	 * @param m the model associated with the resources
	 * @param resources the resources to sort
	 * @param backlink the backlink property to use
	 * @return a sorted list of the given resources, using the specified
	 * backlink property
	 * @throws PreferenceException if there is an error with retrieving
	 * information about the resources from the model
	 */
	static List topologicalSortResourcesBy(Model m, Iterator resources,
			Property backlink) throws PreferenceException {
		BeanSort bs = new BeanSort();
		bs.backlink = backlink;
		bs.model = m;
		return GraphUtilities.topologicalSort(resources, bs);
	}

	/**
	 * Sort all of the lal:Bean nodes in m's graph such that the LAL:requires
	 * property is a back-link.
	 * 
	 * @param m
	 *            the model to use as reference
	 * @return a sorted list of lal:Bean Resource nodes
	 * @throws PreferenceException
	 */
	private static List sortApplicationBeansByRequires(Model m)
			throws PreferenceException {
		Iterator beanIter = m.listSubjectsWithProperty(RDF.type,
				LAL.ApplicationBean);
		return topologicalSortResourcesBy(m, beanIter, LAL.requires);
	}

	/**
	 * @throws PreferenceException 
	 * Gets the value of the given bean's property named <code>attr</code>.
	 * 
	 * @param bean
	 *            An instance of a java class that has a get(attr) or an
	 *            is(attr) method
	 * @param attr
	 *            The name of the property to read
	 * @return The value of the property
	 */
	private Object beanGet(Object bean, String attr) throws PreferenceException {
		if (bean instanceof ViperChangeEvent) {
			Object o = ((ViperChangeEvent) bean).getProperty(attr);
			if (o != null) {
				return o;
			}
		}
		try {
			Method m = bean.getClass().getMethod(toBeanGet(attr), null);
			if (m == null) {
				m = bean.getClass().getMethod(toBeanIs(attr), null);
			}
			return m.invoke(bean, null);
		} catch (InvocationTargetException e) {
			throw new RuntimeException("While getting " + attr + " from " + bean, e.getTargetException());
		} catch (SecurityException e) {
			throw new PreferenceException("While getting " + attr + " from " + bean, e);
		} catch (NoSuchMethodException e) {
			throw new PreferenceException("While getting " + attr + " from " + bean, e);
		} catch (IllegalArgumentException e) {
			throw new PreferenceException("While getting " + attr + " from " + bean, e);
		} catch (IllegalAccessException e) {
			throw new PreferenceException("While getting " + attr + " from " + bean, e);
		}
	}

	/**
	 * Gets a non-anonymous application bean by URI.
	 * @param uri the identifier of the bean to extract
	 * @return the bean value
	 * @throws IllegalArgumentException if the bean is not
	 * found, the identifier is not valid, or there is some
	 * error while loading the bean from the preference data
	 */
	public Object getBean(String uri) {
		try {
			return rdfNodeToValue(prefs.model.getResource(uri));
		} catch (PreferenceException e) {
			throw new IllegalArgumentException("Error while trying to find bean " + uri);
		}
	}
	
	/**
	 * Gets the java value represented by the RDFNode. If no appropriate
	 * interpretation is found, it returns the parameter as a Resource. For
	 * example, a Resource that is known to be an lal:Bean will be converted to
	 * the bean it represents.
	 * 
	 * @param propVal
	 *            An RDFNode to convert
	 * @return The value the node represents/references
	 * @throws PreferenceException
	 * @throws RDFException
	 */
	public Object rdfNodeToValue(RDFNode propVal) throws PreferenceException {
		return rdfNodeToValue(propVal, null);
	}

	/**
	 * Gets the java value represented by the RDFNode. If no appropriate
	 * interpretation is found, it returns the parameter as a Resource. For
	 * example, a Resource that is known to be an lal:Bean will be converted to
	 * the bean it represents.
	 * 
	 * @param propVal
	 *            The node to convert/interpret/find.
	 * @param parent
	 *            The context for the node; if it is a property value without a
	 *            lal:propertyOf link, it assumes it is a property of this.
	 * @return The value the property refers to.
	 * @throws PreferenceException
	 * @throws RDFException
	 */
	public Object rdfNodeToValue(RDFNode propVal, Object parent)
			throws PreferenceException {
		if (propVal instanceof Resource) {
			Resource r = (Resource) propVal;
			// This is UGLY. I'll need to think of an OO way to do this
			// sometime
			if (r.equals(LAL.Parent)) {
				// A reference to the parent.
				return parent;
			} else if (r.hasProperty(LAL.addressOf)) {
				return r.getProperty(LAL.addressOf).getResource();
			} else if (r.hasProperty(LAL.listenerBean)) {
				return findActionListener(r);
			} else if (r.hasProperty(LAL.propertyName)) {
				// implies the triple "?r a lal:BeanProperty ."
				// Interpret this as a property
				Object bean = parent;
				if (r.hasProperty(LAL.propertyOf)) {
					Statement poStmt = r.getProperty(LAL.propertyOf);
					bean = rdfNodeToValue(poStmt.getObject(), parent);
				}
				String pname = r.getProperty(LAL.propertyName).getString();
				return beanGet(bean, pname);
			} else if (LAL.aBean(r)) {
				// A bean. Load/initialize the bean if it isn't already there.
				if (resources.containsKey(propVal)) {
					return resources.get(propVal);
				} else {
					Object val = loadBeanFromResource(r);
					initializeBeanFromResource(val, parent, r);
					if (!r.isAnon() && LAL.aTemporaryBean(r)) {
						resources.put(r, val);
					}
					return val;
				}
			} else if (r.hasProperty(RDF.rest)) {
				// Part of an RDF List. Return it as a list.
				// NOTE this fails if the list isn't well formed.
				// This shouldn't come up, as I use n3, and malformed
				// lists are a syntax error in n3 (not so in rdf+xml)
				// FIXME circular lists cause infinite recursion
				LinkedList L = new LinkedList();
				L.add(rdfNodeToValue(r.getProperty(RDF.first).getObject(),
						parent));
				L.addAll((List) rdfNodeToValue(r.getProperty(RDF.rest)
						.getObject(), parent));
				return L;
			} else if (r.equals(RDF.nil)) {
				// An empty list
				return new LinkedList();
			} else if (r.hasProperty(RDF.type, LAL.Action)) {
				return getActionForResource(r);
			} else if (r.hasProperty(LAL.methodName)) {
				// A method invocation
				Object target = parent;
				if (r.hasProperty(LAL.invokedOn)) {
					target = rdfNodeToValue(r.getProperty(LAL.invokedOn).getObject());
				}
				
				Object[] params = new Object[0];
				if (r.hasProperty(LAL.parameters)) {
					params = ((List) rdfNodeToValue(r.getProperty(LAL.parameters).getObject())).toArray();
				}
				String methodName = r.getProperty(LAL.methodName).getString();
				Class[] sig;
				if (r.hasProperty(LAL.parameterTypes)) {
					List types = (List) rdfNodeToValue(r.getProperty(LAL.parameterTypes).getObject());
					sig = new Class[types.size()];
					for (int i = 0; i < sig.length; i++) {
						Object o = types.get(i);
						if (o instanceof Class) {
							sig[i] = (Class) o;
						} else if (o instanceof String) {
							try {
								sig[i] = Class.forName((String) o);
							} catch (ClassNotFoundException e) {
								throw new PreferenceException("Error while trying to invoke " + methodName, e);
							}
						} else {
							sig[i] = o.getClass();
						}
					}
				} else {
					sig = new Class[params.length];
					for (int i = 0; i < params.length; i++) {
						sig[i] = params[i].getClass();
					}
				}
				try {
					Method m = target.getClass().getMethod(methodName, sig);
					return m.invoke(target, params);
				} catch (SecurityException e) {
					throw new PreferenceException("Error while trying to invoke " + methodName, e);
				} catch (NoSuchMethodException e) {
					throw new PreferenceException("Error while trying to invoke " + methodName, e);
				} catch (IllegalArgumentException e) {
					throw new PreferenceException("Error while trying to invoke " + methodName, e);
				} catch (IllegalAccessException e) {
					throw new PreferenceException("Error while trying to invoke " + methodName, e);
				} catch (InvocationTargetException e) {
					throw new PreferenceException("Error while trying to invoke " + methodName, e);
				}
			} else {
				// Unknown. Assume the client knows what to
				// do with the resource and return it unmodified
				return r;
			}
		} else {
			// assume Literal - what about boolean, float, String? for now, only
			// String works
			Literal li = (Literal) propVal;
			RDFDatatype phylum = li.getDatatype();
			if (phylum == null) {
				return li.getString();
			} else {
				return phylum.parse(li.getLexicalForm());
			}
		}
	}

	/**
	 * Construct a new instance of the Class referenced by the referenced java
	 * name. It uses the no-arg constructor.
	 * 
	 * @param name
	 *            the name of the object to create
	 * @return A new instance of the object.
	 * @throws PreferenceException
	 *             When the object couldn't be constructed for one reason or
	 *             another, e.g. the named class can't be found or it doesn't
	 *             have a no-arg constructor
	 */
	public static Object loadObjectFromName(String name)
			throws PreferenceException {
		try {
			Class beanClass = Class.forName(name);
			Constructor con = beanClass.getConstructor(new Class[]{});
			return con.newInstance(new Object[]{});
		} catch (RDFException e) {
			throw new PreferenceException(e);
		} catch (ClassNotFoundException e) {
			throw new PreferenceException("Invalid class name: " + name, e);
		} catch (SecurityException e) {
			throw new PreferenceException(e);
		} catch (NoSuchMethodException e) {
			throw new PreferenceException(
					"The class does not have a no-argument constructor: "
							+ name, e);
		} catch (IllegalArgumentException e) {
			throw new PreferenceException(e);
		} catch (InstantiationException e) {
			throw new PreferenceException(e);
		} catch (IllegalAccessException e) {
			throw new PreferenceException(e);
		} catch (InvocationTargetException e) {
			throw new PreferenceException(e);
		}
	}

	/**
	 * Load a bean from its resource. Does not put it in the list of application
	 * beans.
	 * 
	 * @param r
	 *            The model node to reference f or construction.
	 * @return A new, unitialized instance of the bean.
	 * @throws PreferenceException
	 */
	public Object loadBeanFromResource(Resource r) throws PreferenceException {
		if (!r.equals(LAL.Core)) {
			if (r.hasProperty(LAL.className)) {
				Statement stmt = r.getProperty(LAL.className);
				String className = stmt.getString();
				return loadObjectFromName(className);
			} else if (r.hasProperty(LAL.propertyName)) {
				return rdfNodeToValue(r);
			} else {
				throw new PreferenceException("Invalid Bean: Bean must have a class name, or be specified as the property of another bean");
			}
		} else {
			return this;
		}
	}

	/**
	 * Gets an application bean that has already been loaded.
	 * 
	 * @param r
	 *            The resource describing the bean
	 * @return The bean, or null if a bean matching the resource hasn't been
	 *         loaded.
	 */
	public Object getLoadedBeanForResource(Resource r) {
		return resources.get(r);
	}

	/**
	 * Adds the given object to the list of loaded beans with the given URI. If
	 * the URI is already defined, this replaces it, but it does not replace it
	 * in any existing contexts.
	 * 
	 * @param r
	 * @param bean
	 */
	public void setLoadedBean(Resource r, Object bean) {
		resources.put(r, bean);
	}

	/**
	 * Gets the resource for the given application bean, if it has been loaded.
	 * It uses the .equals method, which is questionable.
	 * 
	 * @param bean
	 *            The bean whose URI to find
	 * @return The URI, if found, or <code>null</code>, otherwise
	 */
	public Resource getResourceForBean(Object bean) {
		Iterator entries = resources.entrySet().iterator();
		while (entries.hasNext()) {
			Map.Entry curr = (Map.Entry) entries.next();
			if (bean.equals(curr.getValue())) {
				return (Resource) curr.getKey();
			}
		}
		return null;
	}

	/**
	 * Initialize the bean specified by the resource. This means setting the
	 * properties as described in the preferences. It is a good idea to
	 * initialize the beans that r requires before initializing itself.
	 * 
	 * The object must have already been initialized by loadBeanFromResource.
	 * 
	 * @param r
	 *            The description of the bean to initialize.
	 * @throws PreferenceException
	 */
	public void initializeBeanFromResource(Resource r)
			throws PreferenceException {
		Object x = resources.get(r);
		initializeBeanFromResource(x, null, r);
	}

	/**
	 * Initializes bean in parent context. This is useful for dynamic beans.
	 * 
	 * @param bean
	 *            The object described by r that you wish to initialize
	 * @param parent
	 *            The parent for the bean
	 * @param r
	 *            The description of the bean to initialize.
	 * @throws PreferenceException
	 */
	public void initializeBeanFromResource(Object bean, Object parent,
			Resource r) throws PreferenceException {
		try {
			StmtIterator si = r.listProperties(LAL.setProperty);
			while (si.hasNext()) {
				Statement curr = (Statement) si.next();
				Resource bNode = (Resource) curr.getObject();
				String propName = bNode.getProperty(LAL.propertyName)
						.getString();
				Object trueVal;
				if (bNode.hasProperty(LAL.propertyValue)) {
					RDFNode propVal = bNode.getProperty(LAL.propertyValue)
							.getObject();
					trueVal = rdfNodeToValue(propVal, parent);
				} else if (bNode.hasProperty(LAL.icon)) {
					// hackhackhack
					Image iconIm = getIconFor(bNode);
					if (iconIm != null) {
						trueVal = new ImageIcon(iconIm);
					} else {
						continue;
					}
				} else {
					logger.warning("Cannot set property without a value: "
							+ propName);
					continue;
				}
				try {
					setProperty(bean, propName, trueVal);
				} catch (IllegalArgumentException iax) {
					throw new PreferenceException(iax);
				}
			}
			si = r.listProperties(LAL.invoke);
			while (si.hasNext()) {
				Statement curr = (Statement) si.next();
				rdfNodeToValue(curr.getObject(), bean);
			}
				
			JComponent theComponent = null;
			if (bean instanceof JFrame) {
				JFrame frame = (JFrame) bean;
				String label = getLabelFor(r);
				if (label != null && label.length() > 0) {
					frame.setTitle(label);
				}
				Image ico = getIconFor(r);
				if (null != ico) {
					frame.setIconImage(ico);
				}
				if (frame.getContentPane() instanceof JComponent) {
					theComponent = (JComponent) frame.getContentPane();
				}
			} else if (bean instanceof JComponent) {
				theComponent = (JComponent) bean;
				si = r.listProperties(LAL.addTo);
				while (si.hasNext()) {
					Statement curr = (Statement) si.next();
					Resource holderR = (Resource) curr.getObject();
					Object holder = resources.get(holderR);
					Container container;
					if (holder instanceof JComponent) {
						container = (JComponent) holder;
					} else if (holder instanceof JFrame) {
						container = ((JFrame) holder).getContentPane();
					} else {
						throw new PreferenceException(
								"Cannot load/find container for '" + r + "': '"
										+ holderR + "'");
					}
					container.add(theComponent);
				}
			}
			if (theComponent != null) {
				for (int i = 0; i < HOTKEYS.INPUT_TYPES.length; i++) {
					StmtIterator inputs = r
							.listProperties(HOTKEYS.INPUT_TYPES[i]);
					while (inputs.hasNext()) {
						hotkeyManager.addActionator(inputs.nextStatement());
					}
				}
			}
		} catch (RDFException e) {
			throw new PreferenceException(lberr(r, e), e);
		} catch (SecurityException e) {
			throw new PreferenceException(lberr(r, e), e);
		} catch (IllegalArgumentException e) {
			throw new PreferenceException(lberr(r, e), e);
		} catch (IllegalAccessException e) {
			throw new PreferenceException(lberr(r, e), e);
		} catch (InvocationTargetException e) {
			throw new PreferenceException(lberr(r, e), e);
		} catch (RuntimeException e) {
			throw new PreferenceException(lberr(r, e), e);
		}
	}

	/**
	 * Gets the localized label for the resource.
	 * 
	 * @param beanR
	 *            The first RDFS.label for the resource found in the closest
	 *            language to the user'
	 * @return The localized label
	 */
	public String getLabelFor(Resource beanR) {
		String s = prefs.getLocalizedString(beanR, RDFS.label);
		return s;
	}

	/**
	 * If the provided bean/resource has an LAL.icon specified, this finds it,
	 * and returns it as a java image.
	 * 
	 * @param beanR
	 *            The bean to check
	 * @return the image, if it is found, else <code>null</code>
	 */
	public Image getIconFor(Resource beanR) {
		prefs.model.enterCriticalSection(ModelLock.READ);
		try {
			Resource iconR = prefs.getLocalizedResource(beanR, LAL.icon);
			if (iconR != null) {
				URI icoUri;
				URL icoUrl;
				try {
					icoUri = new URI(iconR.getURI());
				} catch (URISyntaxException usx) {
					logger.warning("Icon for bean <" + beanR
							+ "> has invalid URI: <" + iconR.getURI() + ">");
					return null;
				}
				try {
					icoUrl = icoUri.toURL();
				} catch (MalformedURLException mux) {
					logger.warning("Icon for bean <" + beanR
							+ "> has invalid URI: <" + icoUri + ">");
					return null;
				}
				if (icoUrl != null) {
					try {
						logger.fine("Found icon for bean <" + beanR
								+ "> with URL " + icoUrl);
						return ImageIO.read(icoUrl);
					} catch (IOException iox) {
						logger.warning("Error loading icon " + icoUrl + " : "
								+ iox.getLocalizedMessage() );
						return null;
					}
				} else {
					logger.warning("Could not find icon for bean <" + beanR
							+ "> with from uri <" + icoUri + ">");
					return null;
				}
			} else {
				logger.fine("No icon defined for <" + beanR + ">");
				return null;
			}
		} finally {
			prefs.model.leaveCriticalSection();
		}
	}

	private String traceAndErr(Exception e) {
		e.printStackTrace();
		return e.getLocalizedMessage();
	}
	private String lberr(Resource r, Exception e) {
		assert false : traceAndErr(e);
		return "Error while loading bean: " + r + " : " + e.getMessage();
	}

	/**
	 * Returns the method name for the setter of the given property.
	 * 
	 * @param propName
	 *            The java name of the property
	 * @return a method name in in the form <code>set<em>Prop</em></code>
	 */
	public static String toBeanSet(String propName) {
		return "set" + capitalizeFirstLetter(propName);
	}
	/**
	 * Returns the method name for the getter of the given property. Note that
	 * there are two possible getter names, <code>get<em>prop</em></code>
	 * and <code>is<em>prop</em></code>.
	 * 
	 * @param propName
	 *            The java name of the property
	 * @return a method name in in the form <code>get<em>Prop</em></code>
	 */
	public static String toBeanGet(String propName) {
		return "get" + capitalizeFirstLetter(propName);
	}
	/**
	 * Returns the method name for the getter for boolean properties, aka the
	 * 'izzer'. Note that there are two possible getter names,
	 * <code>get<em>prop</em></code> and <code>is<em>prop</em></code>.
	 * 
	 * @param propName
	 *            The java name of the property
	 * @return a method name in in the form <code>is<em>Prop</em></code>
	 */
	public static String toBeanIs(String propName) {
		return "is" + capitalizeFirstLetter(propName);
	}

	/**
	 * Capitalize the first letter of the given word. This is useful for
	 * manipulating bean properties.
	 * 
	 * @param S
	 *            the name to capitalize
	 * @return S such that the first letter is capitalized
	 */
	private static String capitalizeFirstLetter(String S) {
		return Character.toUpperCase(S.charAt(0)) + S.substring(1);
	}

	/**
	 * Load and initialize all the beans that are in the subgraph connected to
	 * lal:Core by lal:requires links.
	 * 
	 * @throws PreferenceException
	 */
	private void loadBeans() throws PreferenceException {
		prefs.model.enterCriticalSection(ModelLock.READ);
		try {
			List beans = sortApplicationBeansByRequires(prefs.model);
			for (Iterator iter = beans.iterator(); iter.hasNext();) {
				Resource curr = (Resource) iter.next();
				logger.fine("Loading bean: " + curr);
				Object bean = loadBeanFromResource(curr);
				resources.put(curr, bean);
			}
			for (Iterator iter = beans.iterator(); iter.hasNext();) {
				Resource curr = (Resource) iter.next();
				try {
					logger.fine("Initializing bean: " + curr);
					initializeBeanFromResource(curr);
				} catch (PreferenceException px) {
					logger.log(Level.SEVERE, "While initializing bean: " + curr + ": "
							+ px.getLocalizedMessage(), px);
				}
			}
			super.pack();
		} finally {
			prefs.model.leaveCriticalSection();
		}
	}

	/**
	 * A ShowWindowAction tells the apploader to display a bean (its uri is
	 * passed as the command) in a JFrame, or bring it to the front if it
	 * already loaded in a JFrame.
	 * 
	 * This is useful for setting up a 'window' or 'view' menu. I'll probably
	 * modify it to support toolboxes and stuff later.
	 * 
	 * @return The listener for the action.
	 */
	public ActionListener getShowWindowActionListener() {
		return windowShowListener;
	}
	private ActionListener windowShowListener = new WindowShowListener();
	private class WindowShowListener implements ActionListener {
		/**
		 * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
		 */
		public void actionPerformed(ActionEvent e) {
			String cmd = e.getActionCommand();
			// cmd should be reference to a resource
			Model model = prefs.model;
			model.enterCriticalSection(ModelLock.READ); // or ModelLock.WRITE
			try {
				Resource beanR = model.getResource(cmd);
				Object bean = resources.get(beanR);
				if (bean == null) {
					logger.severe("Error: No known window named '" + cmd + "'");
				} else if (openWindows.containsKey(cmd)) {
					Window f = (Window) openWindows.get(cmd);
					if (isMac()) {
						try {
							menuManager.resetMenu(((RootPaneContainer) f).getRootPane());
						} catch (PreferenceException px) {
							logger.log(Level.SEVERE, px.getLocalizedMessage(),
									px);
						}
					}
					f.setVisible(true);
					f.toFront();
				} else {
					Window newFrame;
					JRootPane rootPane;

					if (bean instanceof Window) {
						newFrame = (Window) bean;
						rootPane = ((RootPaneContainer) newFrame).getRootPane();
					} else {
						JComponent c = (JComponent) bean;
						newFrame = new JFrame();
						Rectangle rect = newFrame.getBounds();
						rect.height = c.getPreferredSize().height;
						rect.width = c.getPreferredSize().width;
						newFrame.setBounds(rect);
						rootPane = ((RootPaneContainer) newFrame).getRootPane();
						rootPane.getContentPane().add(c);
					}

					if (newFrame instanceof Frame) {
						String label = prefs.getLocalizedString(beanR,
								RDFS.label);
						if (label != null && label.length() > 0) {
							((Frame) newFrame).setTitle(label);
						}
					}
					if (newFrame instanceof JFrame) {
						Image ico = getIconFor(beanR);
						if (null != ico) {
							((JFrame) newFrame).setIconImage(ico);
						}
					}

					if (isMac()) {
						try {
							menuManager.resetMenu(rootPane);
						} catch (PreferenceException px) {
							logger.log(Level.SEVERE, px.getLocalizedMessage(),
									px);
						}
					}

					newFrame.addWindowListener(new QuickWindowAdapter(cmd));
					openWindows.put(cmd, newFrame);

					newFrame.pack();
					newFrame.setLocationRelativeTo(AppLoader.this);
					newFrame.setVisible(true);
				}
			} finally {
				model.leaveCriticalSection();
			}
		}
	}

	private class QuickWindowAdapter extends WindowAdapter {
		private String cmd;
		QuickWindowAdapter(String cmd) {
			this.cmd = cmd;
		}
		/**
		 * @see java.awt.event.WindowAdapter#windowClosed(java.awt.event.WindowEvent)
		 */
		public void windowClosed(WindowEvent e) {
			openWindows.remove(cmd);
		}
	}

	/**
	 * Exit the system, executing the appropriate shut down actions. This does
	 * not close the program, but does serialize the user preferences.
	 */
	public void shutdown() {
		prefs.serializeUserPrefs();
	}

	/**
	 * Gets a listener for the exit action.
	 * 
	 * @return An ActionListener that runs 'shutdown' when an action is
	 *         performed.
	 */
	public ActionListener getExitListener() {
		return new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				tryToShutdown();
			}
		};
	}

	/**
	 * Returns an iterator over all resources in the the iter by menu:priority.
	 * Sorry, it isn't a resource iterator, so you'll have to cast stuff.
	 * 
	 * @param iter
	 *            an iterator of Resource items
	 * @return an iterator of Resource items, sorted by menu:priority
	 * @throws RDFException
	 *             if there is something wrong with the RDF
	 */
	Iterator sortByPriority(Iterator iter) throws RDFException {
		SortedMap positivePrio = new TreeMap();
		SortedMap negativePrio = new TreeMap();
		List noPrio = new LinkedList();
		while (iter.hasNext()) {
			Resource r = (Resource) iter.next();
			if (r.hasProperty(MENU.priority)) {
				Statement prioStmt = r.getProperty(MENU.priority);
				long priority = prioStmt.getLong();
				if (priority < 0) {
					negativePrio.put(new Long(priority), r);
				} else {
					positivePrio.put(new Long(priority), r);
				}
			} else {
				noPrio.add(r);
			}
		}
		Iterator[] is = new Iterator[]{positivePrio.values().iterator(),
				noPrio.iterator(), negativePrio.values().iterator()};
		return new MultiIterator(is);
	}

	/**
	 * Creates a new AppLoader and initializes it. Note that the preferences are
	 * loaded from the file specified by the "lal.prefs" property.
	 * 
	 * @param args
	 *            interpreted as triggers and flags specified in the preferences
	 * @throws IOException
	 * @throws RDFException
	 * @throws PreferenceException
	 */
	public static void main(String[] args) throws IOException, RDFException, PreferenceException {
		final PrefsManager p = new PrefsManager();
		String sysPrefs = System.getProperty("lal.prefs");
		if (sysPrefs == null || sysPrefs.length() == 0) {
			throw new PreferenceException("No system preferences specified.");
		}
		ClassLoader cl = p.getClass().getClassLoader();
		URL prefsUrl = cl.getResource(sysPrefs);
		URI prefsUri = null;
		if (prefsUrl == null) {
			prefsUri = string2uri(sysPrefs);
		} else {
			try {
				prefsUri = new URI(prefsUrl.toExternalForm());
			} catch (URISyntaxException e1) {
				e1.printStackTrace();
				prefsUri = string2uri(sysPrefs);
			}
		}
		try {
			p.setSystemPrefs(prefsUri);
		} catch (IllegalArgumentException iax) {
			String errmsg = "Error while loading application configuration"
					+ "\n\t" + iax.getLocalizedMessage()
					+ "\n\t(Tried to load from " + prefsUri + ")";
			System.err.println(errmsg);
			System.exit(1);
		}
		final OptionsManager o = p.getOptionsManager();
		o.setPrefs(p);
		try {
			o.parseArgumentList(args);
			SwingUtilities.invokeLater(new Runnable() {
				public void run() {
					try {
						new AppLoader(p);
					} catch (PreferenceException e) {
						e.printStackTrace();
						System.exit(2);
					}
				}
			});
		} catch (ArgumentException e) {
			System.err.println(e.getMessage());
			o.printUsage();
			System.exit(1);
		}
	}

	/**
	 * Get the preferences for the application
	 * 
	 * @return PrefsManager The preferences.
	 */
	public PrefsManager getPrefs() {
		return prefs;
	}

	/**
	 * Sets the prefs. This won't cause them to be reparsed. It isn't clear what
	 * should be done, in that case.
	 * 
	 * @param prefs
	 *            The prefs to set
	 */
	public void setPrefs(PrefsManager prefs) {
		this.prefs = prefs;
	}

	/**
	 * Set a property on a bean using the javabeans patterns.
	 * 
	 * @param bean
	 *            The instance to modify.
	 * @param prop
	 *            The name of the property to set
	 * @param val
	 *            The new value of the property
	 * @throws IllegalArgumentException
	 * @throws IllegalAccessException
	 * @throws InvocationTargetException
	 */
	public static void setProperty(Object bean, String prop, Object val)
			throws IllegalArgumentException, IllegalAccessException,
			InvocationTargetException {
		String propSetName = AppLoader.toBeanSet(prop);
		Class beanClass = bean.getClass();
		Method[] M = beanClass.getMethods();
		int k = 0;
		Method meth = null;
		Object[] args = null;
		while (k < M.length && args == null) {
			if (M[k].getName().equals(propSetName)) {
				meth = M[k];
				args = checkMethod(meth, val);
			}
			k++;
		}
		if (args == null) {
			throw new IllegalArgumentException(
					"Unable to find property setter named: " + propSetName
							+ " (" + val + "), on property type: " + beanClass);
		}
		meth.invoke(bean, args);
	}

	/**
	 * Checks to see that the object is assignable from the given type. Unlike
	 * <code>Class.isAssignableFrom</code>, this also works for transfering
	 * between the boxed types and the primatives (e.g. Integer and int).
	 * 
	 * @param type
	 * @param val
	 * @return
	 */
	public static boolean ofType(Class type, Object val) {
		if (val == null) {
			return true;
		}
		Class vType = val.getClass();
		if (type.isAssignableFrom(vType)) {
			return true;
		} else if (type.isPrimitive()) {
			return box(type).isAssignableFrom(vType);
		} else if (vType.isPrimitive()) {
			return type.isAssignableFrom(box(vType));
		}
		return false;
	}
	private static final Map boxer = new HashMap();

	static {
		boxer.put(Boolean.TYPE, Boolean.class);
		boxer.put(Character.TYPE, Character.class);
		boxer.put(Byte.TYPE, Byte.class);
		boxer.put(Short.TYPE, Short.class);
		boxer.put(Integer.TYPE, Integer.class);
		boxer.put(Long.TYPE, Long.class);
		boxer.put(Float.TYPE, Float.class);
		boxer.put(Double.TYPE, Double.class);
		boxer.put(Void.TYPE, Void.class);
	}

	/**
	 * Get the box class for a primitive type. For example, if this is the Class
	 * for the <code>int</code> type, this returns <code>Integer.class</code>
	 * 
	 * @param primitive
	 *            The primitive type. If not a primitive, this returns
	 *            <code>null</code>
	 * @return The corresponding box type
	 */
	public static Class box(Class primitive) {
		return (Class) boxer.get(primitive);
	}

	/**
	 * Checks that the method can take the given parameter.
	 * 
	 * @param meth
	 *            The method to check.
	 * @param val
	 *            The value to pass, or list of values for a multi-arg method
	 * @return the object array to pass to the method, if it is valid.
	 *         Otherwise, returns <code>null</code>
	 */
	public static Object[] checkMethod(Method meth, Object val) {
		Class[] T = meth.getParameterTypes();
		if (T.length == 1 && ofType(T[0], val)) {
			return new Object[]{val};
		} else if (val instanceof Collection) {
			Collection args = (Collection) val;
			int i = 0;
			Iterator iter = args.iterator();
			while (i < T.length && iter.hasNext()) {
				Object param = iter.next();
				if (!ofType(T[i], param)) {
					return null;
				}
				i++;
			}
			if ((i == T.length) && !iter.hasNext()) {
				return args.toArray();
			}
		}
		return null;
	}

	/**
	 * Converts a string to a URI. If the string isn't already a valid URI, it
	 * is interpreted as a path to a file, and a file: URI is returned.
	 * 
	 * @param fname
	 *            The String to convert to a URI
	 * @return A valid URI from the given String.
	 */
	public static URI string2uri(String fname) {
		try {
			URI rval = new URI(fname);
			if (null == rval.getScheme()) {
				File f = new File(fname);
				return f.toURI();
			}
			return rval;
		} catch (URISyntaxException e1) {
			File f = new File(fname);
			return f.toURI();
		}
	}

	/**
	 * Assuming <code>r</code> describes an action, gets the action it
	 * describes.
	 * 
	 * @param r
	 *            The description of an action to take
	 * @return An instance (perhaps already constructed) fitting the description
	 *         passed in
	 * @throws IllegalArgumentException
	 *             When the resource doesn't describe an understandable action
	 */
	public Actionator getActionForResource(Resource r) {
		if (resources.containsKey(r)) {
			Object o = resources.get(r);
			if (o instanceof Actionator) {
				return (Actionator) o;
			}
			throw new IllegalArgumentException("Not an action: " + r);
		} else {
			Actionator a;
			try {
				a = Actionator.parseAction(this, r);
				resources.put(r, a);
				return a;
			} catch (PreferenceException e) {
				logger.log(Level.SEVERE, "Error while parsing action: " + r, e);
				throw new IllegalArgumentException(e.getLocalizedMessage());
			}
		}
	}

	ActionListener findActionListener(Resource actionListener)
			throws PreferenceException {
		try {
			Statement beanS = actionListener.getProperty(LAL.listenerBean);
			if (beanS == null) {
				throw new PreferenceException("Cannot find a listener if no lal:listenerBean is set. Checking " + actionListener);
			}
			Statement methS = actionListener.getProperty(LAL.listenerType);
			if (methS == null) {
				throw new PreferenceException("Cannot find a listener if no lal:listenerType is set. Checking " + actionListener);
			}
			Object bean = rdfNodeToValue(beanS.getObject());
			String methodName = toBeanGet(methS.getString());
			Method method = bean.getClass()
					.getMethod(methodName, new Class[]{});
			return (ActionListener) method.invoke(bean, new Object[]{});
		} catch (RDFException e) {
			throw new PreferenceException(e);
		} catch (SecurityException e) {
			throw new PreferenceException(e);
		} catch (NoSuchMethodException e) {
			throw new PreferenceException(e);
		} catch (IllegalArgumentException e) {
			throw new PreferenceException(e);
		} catch (IllegalAccessException e) {
			throw new PreferenceException(e);
		} catch (InvocationTargetException e) {
			throw new PreferenceException(e);
		}
	}

	/**
	 * Gets the action that is currently invoked when the user tries to close
	 * the main frame.
	 * 
	 * @return AttemptToPerformAction
	 */
	public AttemptToPerformAction getVetoCloseAction() {
		return vetoCloseAction;
	}

	/**
	 * Sets action handler to be invoked when the user tries to close the main
	 * window.
	 * 
	 * @param vetoCloseAction
	 *            The vetoCloseAction to set
	 */
	public void setVetoCloseAction(AttemptToPerformAction vetoCloseAction) {
		this.vetoCloseAction = vetoCloseAction;
	}

	/**
	 * Determines if the application is running on a Macintosh. This is useful
	 * for configuring the user interface.
	 * 
	 * @return Returns <code>true</code> if the application is running on a
	 *         mac.
	 */
	public static boolean isMac() {
		return mac;
	}

	/**
	 * Sets the title of the document window to reflect the name of the
	 * document.
	 * 
	 * @param r
	 *            the bean to retitle
	 * @param docTitle
	 *            the title of the edited document
	 */
	public void setWindowDocumentTitle(Resource r, String docTitle) {
		Model oldM = null;
		if (prefs.model.contains(r, LAL.documentName)) {
			if (prefs.model.contains(r, LAL.documentName, docTitle)) {
				return;
			}
			oldM = new ModelMem();
			r = prefs.model.getResource(r.getURI());
			StmtIterator iter = r.listProperties(LAL.documentName);
			while (iter.hasNext()) {
				oldM.add(iter.nextStatement());
			}
		}
		Model m = new ModelMem();
		m.add(r, LAL.documentName, docTitle);
		prefs.changeTemporary(oldM, m);
		// XXX: Really, this should be in a preference listener
		resetWindowTitle(r);
	}

	/**
	 * Sets the windowModified field of the given frame. If isMac is true, it
	 * adds the dark dot to the red button. On others platforms, it appends an
	 * asterisk before the window title.
	 * 
	 * @param r
	 *            the bean to mark as modified/unmodified
	 * @param val
	 *            <code>true</code> if the bean's contents have been modified
	 *            since the last save point
	 */
	public void setWindowModified(Resource r, boolean val) {
		final Model m = new ModelMem();
		m.add(r, LAL.documentModified, val);
		Model oldM = null;
		prefs.model.enterCriticalSection(ModelLock.READ);
		try {
			if (prefs.model.contains(r, LAL.documentModified)) {
				if (prefs.model.contains(r, LAL.documentModified, val)) {
					return;
				}
				oldM = new ModelMem();
				oldM.add(r, LAL.documentModified, !val);
			}
		} finally {
			prefs.model.leaveCriticalSection();
		}
		
		prefs.changeTemporary(oldM, m);
		// XXX: Really, this should be in a preference listener
		resetWindowTitle(r);
	}

	private void resetWindowTitle(Resource r) {
		r = prefs.model.getResource(r.getURI());
		Object o = getLoadedBeanForResource(r);
		RootPaneContainer rpc = null;
		if (o instanceof RootPaneContainer) {
			rpc = (RootPaneContainer) o;
		} else {
			throw new IllegalArgumentException("Not a valid frame: " + r + " ("
					+ o + ")");
		}

		boolean isModified = false;
		String documentTitle = "untitled";
		String windowTitle = rpc.getRootPane().getName();
		if (r.hasProperty(LAL.documentModified)) {
			isModified = r.getProperty(LAL.documentModified).getBoolean();
		}
		if (r.hasProperty(LAL.documentName)) {
			documentTitle = prefs.getLocalizedString(r, LAL.documentName);
		}
		for (int i = 0; i < TITLES.length; i++) {
			if (r.hasProperty(TITLES[i])) {
				windowTitle = prefs.getLocalizedString(r, TITLES[i]);
				break;
			}
		}

		JComponent rp = rpc.getRootPane();
		rp.putClientProperty("windowModified", Boolean.valueOf(isModified));
		if ("untitled" == documentTitle && !isModified) {
			setTitleOf(rpc, windowTitle);
		} else if (isModified) {
			setTitleOf(rpc, '[' + documentTitle + "] - " + windowTitle);
		} else {
			setTitleOf(rpc, documentTitle + " - " + windowTitle);
		}
	}

	private static Property[] TITLES = new Property[]{RDFS.label, DC_11.title,
			PREFS.abbr};

	static String getTitleOf(RootPaneContainer rpc) {
		if (rpc instanceof Dialog) {
			return ((Dialog) rpc).getTitle();
		} else if (rpc instanceof Frame) {
			return ((Frame) rpc).getTitle();
		} else if (rpc instanceof JInternalFrame) {
			return ((JInternalFrame) rpc).getTitle();
		} else if (rpc == null) {
			throw new NullPointerException();
		} else {
			throw new ClassCastException("Cannot set the title of " + rpc);
		}
	}
	private static void setTitleOf(RootPaneContainer rpc, String title) {
		if (rpc instanceof Dialog) {
			((Dialog) rpc).setTitle(title);
		} else if (rpc instanceof Frame) {
			((Frame) rpc).setTitle(title);
		} else if (rpc instanceof JInternalFrame) {
			((JInternalFrame) rpc).setTitle(title);
		} else if (rpc == null) {
			throw new NullPointerException();
		} else {
			throw new ClassCastException("Cannot set the title of " + rpc);
		}
	}

	/**
	 * Gets the 'window modified' property of the given
	 * Swing component.
	 * @param jc the swing component to check
	 * @return <code>true</code> when the windowModified
	 * property is set to <code>true</code>
	 */
	public static boolean getWindowModified(JComponent jc) {
		Object o = jc.getClientProperty("windowModified");
		if (o instanceof Boolean) {
			return ((Boolean) o).booleanValue();
		}
		return false;
	}
	/**
	 * Gets the java logger associated with this application's core bean.
	 * 
	 * @return the logger
	 */
	public Logger getLogger() {
		return logger;
	}
	/**
	 * @return Returns the hotkeyManager.
	 */
	public HotkeyProcessor getHotkeyManager() {
		return hotkeyManager;
	}
}