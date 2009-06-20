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

import java.io.*;
import java.net.*;
import java.util.*;
import java.util.logging.*;

import javax.swing.*;

import com.hp.hpl.jena.graph.*;
import com.hp.hpl.jena.mem.*;
import com.hp.hpl.jena.n3.*;
import com.hp.hpl.jena.rdf.model.*;
import com.hp.hpl.jena.shared.*;
import com.hp.hpl.jena.vocabulary.*;

import edu.umd.cfar.lamp.apploader.*;
import edu.umd.cfar.lamp.apploader.misc.*;
import edu.umd.cfar.lamp.viper.util.*;

/**
 * A PrefsManager holds the RDF preferences data for the current setup. It has
 * places to attach listeners, if we ever decide to have a pref editor, but
 * currently the RDF model (Jena 1.6) doesn't support events, so they aren't
 * called.
 * 
 * The basic idea is "system prefs &lt; user prefs &lt; file &lt; command line".
 * For example, if the system says that all bounding boxes are red, but the user
 * prefers blue, it will select blue, unless the file says these bounding boxes
 * should be green, in which case they are unless the user specifies another
 * color on the command line.
 * 
 * So, the question is, how are the prefs files discovered? The current idea is
 * "default location &lt; system property &lt; java property ".
 * 
 * Right now file and cli aren't implemented. The idea for file is to perhaps
 * have an RDF element in the header or something. As for the command line, we
 * would set a map of command line options to properties or something, and then
 * pass the system argument array to a method.
 */
public class PrefsManager {
	private static final String LOG_FILENAME = "log.xml";
	private static final String ERR_FILENAME = "error.txt";
	private OptionsManager optionsManager = new OptionsManager();
	private AppLoader loader;
	private Logger logger;

	/**
	 * Sets the associated Limn3 core.
	 * @param loader the core bean
	 */
	public void setCore(AppLoader loader) {
		this.loader = loader;
	}
	/**
	 * Gets the associated core bean.
	 * @return the associated core bean, if one has been assigned
	 */
	public AppLoader getCore() {
		return loader;
	}

	/**
	 * These are related to the basic operation of the software. These also
	 * include defaults.
	 */
	private Model system = new ModelMem();

	/**
	 * These are things specified by the user.
	 */
	private Model user = new ModelMem();

	/**
	 * These are temporary preferences, like setting the title of a window to a
	 * file, that are governed by preferences but neither saved to nor loaded
	 * from a file.
	 */
	private Model temporary = new ModelMem();

	/**
	 * If a specific file has some preferences associated with it (e.g. it
	 * always loads with a certain aspect visible), these should be stored here.
	 * This may not prove useful.
	 */
	private Model file = new ModelMem();

	private void addUserPrefixMaps(PrefixMapping p) {
		try {
			URI userDirUri = this.getUserDirectory();
			if (null != userDirUri) {
				String userDir = getUserDirectory().toString();
				p.setNsPrefix("user-home", userDir);
			}
		} catch (PreferenceException px) {
			// don't add missing prefix
		}
	}

	/**
	 * Adds the given namespace short-name (prefix). 
	 * This is useful during serialization.
	 * @param prefix the new prefix
	 * @param uri the uri that will use the prefix
	 */
	public void addPrefix(String prefix, String uri) {
		((PrefixMapping) system).setNsPrefix(prefix, uri);
		((PrefixMapping) user).setNsPrefix(prefix, uri);
		((PrefixMapping) temporary).setNsPrefix(prefix, uri);
		((PrefixMapping) file).setNsPrefix(prefix, uri);
	}
	private static String findFreePrefix(PrefixMapping p, String prefix) {
		String oldPrefix = prefix + "-";
		do {
			prefix = oldPrefix + System.currentTimeMillis() + "-"
					+ Double.doubleToLongBits(Math.random());
		} while (p.getNsPrefixURI(prefix) != null);
		return prefix;
	}
	/**
	 * Adds the given prefix to the given mapping. If the prefix is already
	 * assigned, it makes a new prefix for the old prefix based on the string.
	 * 
	 * @param p the prefix mapping to alter
	 * @param prefix the new prefix to try
	 * @param uri the uri to assign the prefix to
	 */
	private static void resetPrefix(PrefixMapping p, String prefix, String uri) {
		assert p != null;
		assert prefix != null;
		assert uri != null;
		String oldUri = p.getNsPrefixURI(prefix);
		if (oldUri == null) {
			p.setNsPrefix(prefix, uri);
		} else if (!oldUri.equals(uri)) {
			String oldPrefix = findFreePrefix(p, prefix);
			p.setNsPrefix(oldPrefix, oldUri);
			p.setNsPrefix(prefix, uri);
		}
	}

	private void addPrefixMaps() {
		addPrefix("rdf", "http://www.w3.org/1999/02/22-rdf-syntax-ns#");
		addPrefix("rdfs", "http://www.w3.org/2000/01/rdf-schema#");
		addPrefix("xsd", "http://www.w3.org/2001/XMLSchema#");
		addPrefix("foaf", "http://xmlns.com/foaf/0.1/");

		String limn3SchemaRoot = "http://viper-toolkit.sourceforge.net/owl/apploader";
		addPrefix("lal", limn3SchemaRoot + "#");
		addPrefix("menu", limn3SchemaRoot + "menu#");
		addPrefix("mru", limn3SchemaRoot + "mru#");
	}

	/**
	 * Create a new preference manager with no
	 * preferences and the default logger.
	 */
	public PrefsManager() {
		this.addUserPrefsListener();
		this.logger = Logger.getLogger("edu.umd.cfar.lamp.apploader.prefs");
		system = new ModelMem();
		user = new ModelMem();
		temporary = new ModelMem();
		file = new ModelMem();
		addPrefixMaps();
		addUserPrefixMaps((PrefixMapping) user);
		optionsManager.setPrefs(this);
	}

	/**
	 * Tries to get the localized version or r's property p. If not found, will
	 * return the one without a lang property or, if all have the lang property,
	 * the 'en' one if it exists or or the last one if not. A better solution
	 * would be to find the translation with the highest score, with 1 = a
	 * language the user knows, and .5 being something like French if the
	 * speaker knows English, down to 0 if all the speaker knows is Euskera or
	 * Japanese.
	 * 
	 * 
	 * If no property is found with the name, returns null, so watch out!
	 * 
	 * @param r
	 *            the resouce on which to check
	 * @param p
	 *            the property name to check
	 * @return the string, if found
	 * @throws RDFException
	 *             if the property takes a non-literal value, for example
	 */
	public String getLocalizedString(Resource r, Property p)
			throws RDFException {
		model.enterCriticalSection(ModelLock.READ);
		try {
			String lang = Locale.getDefault().getLanguage();
			if (!model.equals(r.getModel()) && !r.isAnon()) {
				r = model.getResource(r.getURI());
			}
			if (r == null || !r.hasProperty(p)) {
				return null;
			}
			StmtIterator iter = r.listProperties(p);
			String def = null;
			while (iter.hasNext()) {
				Statement currStmt = (Statement) iter.next();
				Literal curr = (Literal) currStmt.getObject();
				String currLang = curr.getLanguage() == null ? "" : curr
						.getLanguage().toLowerCase();
				if (currLang.length() == 0) {
					def = curr.getString();
				} else if (currLang == lang) {
					return curr.getString();
				} else if (def == null && "en".equals(currLang)) {
					def = curr.getString();
				}
			}
			return def;
		} finally {
			model.leaveCriticalSection();
		}
	}

	/**
	 * From the list of subject-verb-objects, get the first object that has an
	 * undo:forLanguage property whose object is the current user's preferred
	 * language. If none is found, uses the first object without a language
	 * specified.
	 * 
	 * @param r
	 *            the resource that takes a localizable property
	 * @param p
	 *            the property to search for
	 * @return The object that most closely matches the user's language
	 *         preference
	 * @see PrefsManager#getLocalizedString(Resource, Property)
	 * @see LAL#lang
	 */
	public Resource getLocalizedResource(Resource r, Property p) {
		model.enterCriticalSection(ModelLock.READ);
		try {
			Resource rval = null;
			String lang = Locale.getDefault().getLanguage();
			if (lang == null) {
				lang = "";
			}
			if (!model.equals(r.getModel()) && !r.isAnon()) {
				r = model.getResource(r.getURI());
			}
			if (r == null || !r.hasProperty(p)) {
				return null;
			}
			StmtIterator iter = r.listProperties(p);
			while (iter.hasNext()) {
				Resource curr = iter.nextStatement().getResource();
				if (!curr.hasProperty(LAL.lang)) {
					rval = curr;
				} else {
					String olang = curr.getProperty(LAL.lang).getString();
					if (olang.equalsIgnoreCase(lang)) {
						return curr;
					}
				}
			}
			return rval;
		} finally {
			model.leaveCriticalSection();
		}
	}

	/**
	 * The sink for all the preferences. Use it to get direct access to the
	 * prefs.
	 * 
	 * I trust you not to update this. Perhaps later I will surround it with an
	 * immutable nature, but don't wait.
	 */
	public Model model = new ModelMem();

	private void helpSetPrefs(Model m, URI filename) throws PreferenceException {
		try {
			Reader in = null;
			try {
				File f = new File(filename);
				if (f.exists()) {
					in = new BufferedReader(new FileReader(f));
				}
			} catch (IllegalArgumentException iax) {
				// bad URI
			}
			if (in == null) {
				try {
					URL toOpen = new URL(filename.toString());
					in = new BufferedReader(new InputStreamReader(toOpen
							.openStream()));
				} catch (MalformedURLException e) {
					throw new FileNotFoundException("File: " + filename
							+ " not found");
				} catch (IOException e) {
					throw new IllegalArgumentException("Error loading URL: "
							+ filename + "\n\t" + e.getLocalizedMessage());
				}
			}
			if (isXMLFormat(in)) {
				m.read(in, "");
			} else {
				N3JenaReader n3reader = new N3JenaReader();
				n3reader.read(m, in, "");
			}
		} catch (N3Exception e) {
			throw new PreferenceException(e);
		} catch (FileNotFoundException e) {
			throw new PreferenceException(e);
		} catch (IOException iox) {
			throw new PreferenceException(iox);
		} catch (RDFException e) {
			throw new PreferenceException(e);
		}
	}
	private void resetPrefs() throws PreferenceException {
		if (model == null) {
			model = new ModelMem();
		}
		model.enterCriticalSection(ModelLock.WRITE);
		try {
			model.remove(model);
			model.add(system);
			model.add(user);
			model.add(file);
			model.add(temporary);
		} catch (RDFException e) {
			throw new PreferenceException(e);
		} finally {
			model.leaveCriticalSection();
		}
		resetUserLogHandler();
	}
	
	/**
	 * Sets the logger to output to the user.log file in the .viper 
	 * directory, if possible.
	 * @throws PreferenceException
	 */
	private void resetUserLogHandler() throws PreferenceException {
		URI dir = getUserDirectory();
		if (dir == null) {
			if (userLogHandler != null) {
				Logger.getLogger("").removeHandler(userLogHandler);
				userLogHandler = null;
				if (System.err instanceof TargetedPrintStream) {
					System.setErr(((TargetedPrintStream) System.err).getOriginal());
				}
			}
		} else {
			if (userLogHandler != null) {
				Logger.getLogger("").removeHandler(userLogHandler);
				userLogHandler = null;
			}
			PrintStream original = System.err;
			if (System.err instanceof TargetedPrintStream) {
				original = ((TargetedPrintStream) System.err).getOriginal();
			}
			try {
				File err = new File(dir.resolve(ERR_FILENAME));
				System.setErr(new TargetedPrintStream(original, new FileOutputStream(err)));

				userLogHandler = new FileHandler(new File(dir.resolve(LOG_FILENAME)).getAbsolutePath());
				Logger.getLogger("").setLevel(Level.FINER);
				Logger.getLogger("").addHandler(userLogHandler);
			} catch (SecurityException e1) {
				logger.log(Level.SEVERE, "Cannot create user log due to security restrictions", e1);
			} catch (IOException e1) {
				logger.log(Level.SEVERE, "I/O error while creating user log", e1);
			}
		}
	}

	private Handler userLogHandler = null;
	private URI defaultSystemDirectory = null;


	/**
	 * Gets the system directory, if defined.
	 * This defaults to whatever directory contains
	 * the n3 file.
	 * @return the system directory
	 * @throws PreferenceException
	 */
	public URI getSystemDirectory() throws PreferenceException {
		model.enterCriticalSection(ModelLock.READ);
		URI sys = defaultSystemDirectory;
		try {
			NodeIterator iter = model.listObjectsOfProperty(LAL.systemDirectory);
			while (iter.hasNext()) {
				RDFNode curr = iter.nextNode();
				String currUri;
				if (curr.asNode().isURI()) {
					currUri = curr.asNode().getURI();
				} else if (curr.asNode().isLiteral()) {
					currUri = curr.toString();
				} else {
					logger
							.warning("Cannot have blank node as lal:systemDirectory.");
					continue;
				}
				URI uri = AppLoader.string2uri(currUri);
				File f = new File(uri);
				if (f.isDirectory()) {
					sys = uri;
					break;
				} else {
					logger
							.warning("Non-existant user directory listed: "
									+ uri);
				}
			}
		} finally {
			model.leaveCriticalSection();
		}
		return sys;
	}
	
	/**
	 * Gets the user directory, if defined.
	 * @return the user's home directory
	 * @throws PreferenceException
	 */
	public URI getUserDirectory() throws PreferenceException {
		model.enterCriticalSection(ModelLock.READ);
		try {
			NodeIterator iter = model.listObjectsOfProperty(LAL.userDirectory);
			URI prefsDirectory = null;
			while (iter.hasNext()) {
				RDFNode curr = iter.nextNode();
				String currUri;
				if (curr.asNode().isURI()) {
					currUri = curr.asNode().getURI();
				} else if (curr.asNode().isLiteral()) {
					currUri = curr.toString();
				} else {
					logger
							.warning("Cannot have blank node as lal:userDirectory.");
					continue;
				}
				URI uri = AppLoader.string2uri(currUri);
				File f = new File(uri);
				if (f.isDirectory()) {
					prefsDirectory = uri;
					break;
				} else {
					logger
							.warning("Non-existant user directory listed: "
									+ uri);
				}
			}
			if (prefsDirectory == null) {
				return null;
			}
			String abbr = null;
			// TODO: Should have a 'programName' thing
			// that would work replace the command line and the
			// neeed for this hack. For the command line, make a
			// script that generates the <progName>.sh and <progName>.bat
			// files, as well. For this to work, I'd have to
			// also write a mainJar thing, that would take the
			// main jar. It would also be nice if it generated a bunch
			// more stuff, but I don't want to start writing
			// an RDF version of apache's Maven.
			if (model.contains(LAL.Core, PREFS.abbr)) {
				abbr = getLocalizedString(LAL.Core, PREFS.abbr);
			} else if (model.contains(LAL.Core, RDFS.label)) {
				abbr = getLocalizedString(LAL.Core, RDFS.label);
			} else if (model.contains(LAL.Core, DC_11.title)) {
				abbr = getLocalizedString(LAL.Core, DC_11.title);
			} else {
				logger.severe("No program name; cannot load user prefs");
				return null;
			}
			abbr = StringHelp.encodeAsAnAcceptableFileName(abbr);
			prefsDirectory = prefsDirectory.resolve("." + abbr + "/");
			File prefD = new File(prefsDirectory);
			if (!prefD.isDirectory()) {
				prefD.mkdir();
			}
			return prefsDirectory;
		} finally {
			model.leaveCriticalSection();
		}
	}

	private URI getUserPrefsFileName() throws PreferenceException {
		URI dir = getUserDirectory();
		if (dir == null) {
			return null;
		}
		dir = dir.resolve("user.n3");
		return dir;
	}

	/**
	 * Save the iser model to the user.n3 file. 
	 */
	public void serializeUserPrefs() {
		URI prefsFileName;
		try {
			prefsFileName = getUserPrefsFileName();
			if (prefsFileName == null) {
				logger.warning("Cannot serialize user prefs");
				return;
			}
		} catch (PreferenceException e) {
			logger.log(Level.WARNING, "Cannot serialize user prefs", e);
			return;
		}
		File userOutput = new File(prefsFileName);
		File oldUserOutput = null;
		File tempUserOutput = null;
		try {
			String prefixStr = "_user_";
			tempUserOutput = File.createTempFile(prefixStr, ".n3", userOutput
					.getParentFile());
			FileOutputStream output = null;
			try {
				output = new FileOutputStream(tempUserOutput);
				Writer w = new BufferedWriter(new OutputStreamWriter(output,
						"UTF-8"));
				user.write(w, "N3", "file:///" + prefsFileName + "#");
				w.flush();
			} catch (java.io.UnsupportedEncodingException ex) {
				//UTF-8 is required so can't happen
				throw new RuntimeException(ex);
			} finally {
				if (output != null) {
					output.close();
					if (userOutput.canRead()) {
						oldUserOutput = new File(userOutput.getAbsolutePath()
								+ "_old");
						userOutput.renameTo(oldUserOutput); // may or may not
						// succeed. i'm all
						// out of ideas
					}
					if (!tempUserOutput.renameTo(userOutput)
							&& userOutput.canRead()) {
						userOutput.delete();
						tempUserOutput.renameTo(userOutput);
					}
				}
			}
		} catch (IOException iox) {
			logger
					.log(Level.WARNING, "Error while serializing user prefs",
							iox);
		} finally {
			if (tempUserOutput != null) {
				tempUserOutput.delete();
			}
			if (oldUserOutput != null) {
				oldUserOutput.delete();
			}
		}
	}

	private File userFile;
	private long userFileDate;

	/**
	 * Loads the se triplestore from a 
	 *n n3 file.
	 * @throws PreferenceException
	 */
	public void loadUserPrefs() throws PreferenceException {
		Model old = user;
		URI fname = getUserPrefsFileName();
		if (fname != null) {
			boolean load = false;
			File nf = new File(fname);
			if (nf.canRead()) {
				if (userFile == null) {
					load = true;
				} else {
					load = !nf.equals(userFile)
							|| nf.lastModified() > userFileDate;
				}
			}
			if (load) {
				userFile = nf;
				userFileDate = userFile.lastModified();
				user = new ModelMem();
				helpSetPrefs(user, fname);
				notifyModelListeners(old, user);
				resetPrefs();
			}
		}
	}

	/**
	 * Resets the system prefs to the contents of the given uri.
	 * @param filename the location of the system prefs
	 * @throws PreferenceException if there is a problem while loading
	 */
	public void setSystemPrefs(URI filename) throws PreferenceException {
		Model old = system;
		try {
			File f = new File(filename);
			setDefaultSystemDirectory(f.getParentFile().toURI());
		} catch (IllegalArgumentException iax) {
			logger.log(Level.CONFIG, "Cannot set default system directory to parent of system preferences file: " + filename, iax);
		}
		system = new ModelMem();
		helpSetPrefs(system, filename);
		if (getUserPrefsFileName() != null) {
			loadUserPrefs();
		} else {
			logger.fine("Cannot load user preferences; file name is unknown");
		}
		notifyModelListeners(old, system);
		resetPrefs();
	}

	private final Selector userDirectorySelector = new SimpleSelector(null, LAL.userDirectory, (RDFNode) null);
	/**
	 * Adds a listener to the userDirectory property, so when a user directory
	 * is added, the old user properties are removed, and the new ones are
	 * added.
	 */
	private void addUserPrefsListener() {
		addListener(new ModelListener() {
			public void changeEvent(ModelEvent event) {
				logger.config("Load user prefs change");
				Runnable R = new Runnable() {
					public void run() {
						try {
							loadUserPrefs();
						} catch (PreferenceException px) {
							logger.log(Level.WARNING,
									"Error while changing user prefs", px);
						}
					}
				};
				addLeftoverAction(R);
			}
			public Selector getSelector() {
				return userDirectorySelector;
			}
		});
	}

	/**
	 * Adds a listener for changes to the triplestore.
	 * @param ml the listener to add
	 */
	public void addListener(ModelListener ml) {
		ll.add(ml);
		Selector s = ml.getSelector();
		if (s == null) {
			allListeners.add(ml);
		} else if (s.getSubject() != null) {
			subjectListeners.put(s.getSubject(), ml);
		} else if (s.getPredicate() != null) {
			predicateListeners.put(s.getPredicate(), ml);
		} else if (s.getObject() != null) {
			objectListeners.put(s.getObject(), ml);
		} else {
			allListeners.add(ml);
		}
	}

	/**
	 * Removes a listener for changes to the triplestore.
	 * @param ml the listener to remove
	 */
	public void removeListener(ModelListener ml) {
		ll.remove(ml);
		Selector s = ml.getSelector();
		if (s == null) {
			allListeners.remove(ml);
		} else if (s.getSubject() != null) {
			subjectListeners.remove(ml);
		} else if (s.getPredicate() != null) {
			predicateListeners.remove(ml);
		} else if (s.getObject() != null) {
			objectListeners.remove(ml);
		} else {
			allListeners.remove(ml);
		}
	}

	private Node cnv(RDFNode r) {
		return r == null ? null : r.asNode();
	}

	private boolean notifyingListeners = false;
	private List leftoverActions = new LinkedList();
	private void runEvents() {
		while (!leftoverActions.isEmpty()) {
			Runnable[] R = new Runnable[leftoverActions.size()];
			R = (Runnable[]) leftoverActions.toArray(R);
			leftoverActions.clear();
			for (int i = 0; i < R.length; i++) {
				R[i].run();
			}
		}
	}
	
	/**
	 * Adds an action to be executed after the current event
	 * is finished being sent out to all listeners.
	 * A hack to avoid deadlock issues, a leftover action
	 * is an event that will be run after the currently 
	 * registered event is sent to all listeners.
	 * If not notifying listeners, the action is just run
	 * immediately, in process.
	 * @param R the action to perform
	 */
	public void addLeftoverAction(Runnable R) {
		if (notifyingListeners == false) {
			R.run();
		} else {
			leftoverActions.add(R);
		}
	}

	/**
	 * Notify relevant listeners that the triples in oldModel were removed and
	 * the ones in newModel were added.
	 * 
	 * XXX: Currently, just loops through all the listeners, checking to see if
	 * any are applicable, instead of doing something more intelligent.
	 * 
	 * @param oldModel the removed stuff
	 * @param newModel the new stuff
	 * @throws RDFException
	 */
	private void notifyModelListeners(Model oldModel, Model newModel)
			throws RDFException {
		if (notifyingListeners)
			throw new IllegalStateException();
		notifyingListeners = true;
		try {
			ModelEvent m = new ModelEvent(oldModel, newModel);
			ModelListener[] L = (ModelListener[]) ll
					.toArray(new ModelListener[ll.size()]);
			for (int i = 0; i < L.length; i++) {
				ModelListener curr = L[i];
				Selector s = curr.getSelector();
				if (s == null || (oldModel != null && oldModel.listStatements(s).hasNext()) || (newModel != null && newModel.listStatements(s).hasNext())) {
					curr.changeEvent(m);
				}
			}
		} finally {
			notifyingListeners = false;
			runEvents();
		}
	}
	private Set ll = new HashSet();
	private Set allListeners = new HashSet();
	private Map subjectListeners = new HashMap();
	private Map predicateListeners = new HashMap();
	private Map objectListeners = new HashMap();

	/**
	 * Gets the associated command-line options manager bean.
	 * @return the command-line options manager
	 */
	public OptionsManager getOptionsManager() {
		return optionsManager;
	}

	private void changeModel(Model toChange, Model toRemove, Model toAdd) {
		model.enterCriticalSection(ModelLock.WRITE);
		try {
			if (toRemove != null && toRemove.size() > 0) {
				toChange.remove(toRemove);
				model.remove(toRemove);
			}
			if (toAdd != null && toAdd.size() > 0) {
				toChange.add(toAdd);
				model.add(toAdd);
			}
		} finally {
			model.leaveCriticalSection();
		}
		notifyModelListeners(toRemove, toAdd);
	}

	/**
	 * Changes the user preferences model. Unlike the other models, this one is
	 * serialized, and editing it causes it to be saved.
	 * 
	 * @param toRemove
	 *            triples to remove from the user preferences triplestore
	 * @param toAdd
	 *            triples to add to the user preferences triplestore
	 */
	public void changeUser(final Model toRemove, final Model toAdd) {
		if (isModelLocked()) {
			logger.log(Level.WARNING, "changing user prefs while model is locked", new IllegalStateException());
			SwingUtilities.invokeLater(new Runnable() {
				public void run() {
					changeModel(user, toRemove, toAdd);
					serializeUserPrefs();
				}
			});
			return;
		}
		changeModel(user, toRemove, toAdd);
		serializeUserPrefs();
	}

	/**
	 * Change data in the temporary triplestore.
	 * @param toRemove triples to remove 
	 * @param toAdd triples to add
	 */
	public void changeTemporary(final Model toRemove, final Model toAdd) {
		if (isModelLocked()) {
			logger.log(Level.WARNING, "changing temporary prefs while model is locked", new IllegalStateException());
			SwingUtilities.invokeLater(new Runnable() {
				public void run() {
					changeModel(user, toRemove, toAdd);
				}
			});
			return;
		}
		changeModel(temporary, toRemove, toAdd);
	}

	/**
	 * Checks to see if the file begins with an xml processing directive, eg
	 * <code>&lt;?xml?&gt;</code>. This method does not check to see that the
	 * file is well-formed, or even if the processing directive is good, just
	 * that the first non-whitespace characters are "&lt;?xml".
	 * 
	 * @param fileName
	 *            The file to check for xml processing directive
	 * @throws IOException
	 *             if there is an error while reading the file, eg
	 *             FileNotFoundException
	 * @return <code>true</code> if the directive was found.
	 */
	public static boolean isXMLFormat(URI fileName) throws IOException {
		return isXMLFormat(new FileReader(new File(fileName)));
	}

	/**
	 * Checks to see if the file begins with an xml processing directive, eg
	 * <code>&lt;?xml?&gt;</code>. This method does not check to see that the
	 * file is well-formed, or even if the processing directive is good, just
	 * that the first non-whitespace characters are "&lt;?xml". Note that this
	 * calls mark and reset on the stream. So, the stream has to be in the
	 * starting position before and will be put back in after.
	 * 
	 * @param r
	 *            the source to check for xml processing directive
	 * @throws IOException
	 *             if there is an error while reading the file, eg
	 *             FileNotFoundException
	 * @throws IllegalArgumentException
	 *             if the Reader doesn't support mark and reset
	 * @return <code>true</code> if the directive was found.
	 */
	public static boolean isXMLFormat(Reader r) throws IOException {
		if (!r.markSupported()) {
			throw new IllegalArgumentException(
					"Reader must support mark and reset to verify if contains an xml prolog");
		}
		char[] toRead = new char[4];
		r.mark(toRead.length);
		int offset = 0;
		while (offset < toRead.length) {
			int read = r.read(toRead, offset, toRead.length - offset);
			if (read < 0) {
				return false;
			}
			offset += read;
		}
		boolean x = toRead[0] == '<' && toRead[1] == '?' && toRead[1] == 'x'
				&& toRead[1] == 'm' && toRead[1] == 'l';
		r.reset();
		return x;
	}

	/**
	 * Gets the error logger associated with the preference manager.
	 * @return the logger
	 */
	public Logger getLogger() {
		return logger;
	}

	/**
	 * Sets the error logger associated with the preference manager.
	 * @param logger the new logger
	 */
	public void setLogger(Logger logger) {
		this.logger = logger;
	}
	
	
	void setDefaultSystemDirectory(URI defaultSystemDirectory) {
		this.defaultSystemDirectory = defaultSystemDirectory;
	}
	
	
	public Model getFile() {
		return file;
	}
	public Model getSystem() {
		return system;
	}
	public Model getTemporary() {
		return temporary;
	}
	public Model getUser() {
		return user;
	}
	
	/**
	 * Reads the properties in using the file loated in the
	 * n3 file. (This is in the form <code>propID prefs:loadsProps filename .</code>.
	 * @param propID the file to look for
	 * @return
	 */
	public Properties getProperties(URI propID) {
		Resource propR = model.getResource(propID.toString());
		NodeIterator ni = model.listObjectsOfProperty(propR, PREFS.loadsProps);
		Properties p = new Properties();
		if (!ni.hasNext()) {
			File f = new File(propID);
			if (f.canRead()) {
				loadPropertiesFrom(p, propID);
			}
		}
		while (ni.hasNext()) {
			RDFNode currN = ni.nextNode();
			if (currN instanceof Resource) {
				URI uri;
				try {
					uri = new URI(((Resource) currN).getURI());
				} catch (URISyntaxException e2) {
					logger.log(Level.WARNING, "Error in properties 'loadsProps' directive.", e2);
					continue;
				}
				loadPropertiesFrom(p, uri);
			}
		}
		return p;
	}
	private void loadPropertiesFrom(Properties p, URI uri) {
		InputStream is = null;
		try {
			File f = new File(uri);
			// XXX should do a 'which' type command to find the file
			is = new BufferedInputStream(new FileInputStream(f));
			p.load(is);
		} catch (IOException e1) {
			logger.log(Level.WARNING, "Error while reading properties from " + uri, e1);
		} finally {
			if (is != null) {
				try {
					is.close();
				} catch (IOException e) {
					logger.log(Level.WARNING, "Error while reading properties from " + uri, e);
				}
			}
		}
	}
	
	/**
	 * If possible, saves the properties to the user's
	 * home directory.
	 * @param propID
	 * @param values
	 * @return true if the properties were saved
	 * @throws UnsupportedOperationException for now
	 */
	public boolean setProperties(URI propID, Properties values) {
		throw new UnsupportedOperationException();
	}
	public boolean isModelLocked() {
		boolean locked = true;
		try {
			model.enterCriticalSection(ModelLock.WRITE);
		} catch (JenaException x) {
			return true;
		} finally {
			model.leaveCriticalSection();
		}
		return false;
	}
}