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
import java.lang.reflect.*;
import java.util.*;
import java.util.logging.*;

import com.hp.hpl.jena.rdf.model.*;
import com.hp.hpl.jena.vocabulary.*;

import edu.umd.cfar.lamp.apploader.*;

/**
 * Manages command-line arguments.
 */
public class OptionsManager {
	private static Logger logger = Logger
			.getLogger("edu.umd.cfar.lamp.apploader.prefs");
	private PrefsManager prefs;

	private Map longNames;
	private Map shortNames;
	private Map javaProps;
	private Map rlookup;
	private Map loadedTriggers;
	// XXX Should this manage memory in some fashion

	/**
	 * Constructs a new, empty options manager.
	 */
	public OptionsManager() {
		prefs = null;
		clearTriggerMaps();
	}

	private static class TrigNames {
		String longName;
		String shortName;
		String propName;
	}

	private void clearTriggerMaps() {
		if (longNames == null) {
			longNames = new HashMap();
			shortNames = new HashMap();
			javaProps = new HashMap();
			rlookup = new HashMap();
			loadedTriggers = new HashMap();
		} else {
			longNames.clear();
			shortNames.clear();
			javaProps.clear();
			rlookup.clear();
			loadedTriggers.clear();
		}
	}

	private final Selector userDirectorySelector = new SimpleSelector(null, LAL.userDirectory, (RDFNode) null);

	/**
	 * Adds a listener to the lal:userDirectory property, so when a trigger is added
	 * that might take immediate effect, it does. This is useful for when a
	 * user.n3 has longName triggers.
	 * 
	 * @return the listener that knows how to react to changes in the longName
	 *         property; removes/adds the given triggers, and fires them as
	 *         necessary
	 */
	private ModelListener getUserDirectoryListener() {
		return new ModelListener() {
			public void changeEvent(ModelEvent event) {
				try {
					if (event.getRemoved() != null) {
						ResIterator iter = event.getRemoved()
								.listSubjectsWithProperty(PREFS.invokes);
						while (iter.hasNext()) {
							removeTrigger(iter.nextResource());
						}
					}
					if (event.getAdded() != null) {
						ResIterator iter = event.getAdded()
								.listSubjectsWithProperty(PREFS.invokes);
						while (iter.hasNext()) {
							Resource added = iter.nextResource();
							addTrigger(added);
							TrigNames n = (TrigNames) rlookup.get(added);
							if (n.propName != null) {
								// execute propname if found
								String val = System.getProperty(n.propName);
								if (val != null) {
									Resource handler = (Resource) javaProps
											.get(n.propName);
									TriggerHandler t;
									try {
										t = findHandler(handler);
									} catch (PreferenceException e1) {
										throw new RuntimeException(e1);
									}
									try {
										t.invoke(prefs, handler, val);
									} catch (RDFException e) {
										throw new RuntimeException(
												"Model changed unexpectedly", e);
									}
								}
							}
						}
					}
				} catch (PreferenceException px) {
					logger.log(Level.WARNING,
							"Problem while trying to listen to model", px);
				}
			}
			public Selector getSelector() {
				return userDirectorySelector;
			}
		};
	}

	/**
	 * Apply all the bound triggers to any matching system 
	 * properties. 
	 */
	public void invokePropertyTriggers() {
		String prop, val;
		for (Iterator iter = javaProps.keySet().iterator(); iter.hasNext();) {
			prop = (String) iter.next();
			val = System.getProperty(prop);
			if (val != null) {
				Resource handler = (Resource) javaProps.get(prop);
				TriggerHandler t;
				try {
					t = findHandler(handler);
				} catch (PreferenceException e1) {
					throw new RuntimeException(e1);
				}
				try {
					t.invoke(prefs, handler, val);
				} catch (RDFException e) {
					throw new RuntimeException("Model changed unexpectedly", e);
				}
			}
		}
	}

	/**
	 * Parses the given command line arguments
	 * and invokes the corresponding triggers.
	 * @param args the command line arguments
	 * @throws ArgumentException
	 */
	public void parseArgumentList(String[] args) throws ArgumentException {
		int i = 0;
		while (i < args.length) {
			String key = args[i++];
			TriggerHandler t;
			Resource handler = null;
			if (key.startsWith("--")) {
				handler = (Resource) longNames.get(key.substring(2));
			} else if (key.startsWith("-")) {
				handler = (Resource) shortNames.get(key.substring(1));
			} else {
				throw new ArgumentException(
						"Each argument key must start with a '-': " + key);
			}
			if (handler == null) {
				throw new ArgumentException("Unrecognized flag: " + key);
			}
			try {
				t = findHandler(handler);
			} catch (PreferenceException e1) {
				throw new RuntimeException(e1);
			}
			try {
				if (!handler.hasProperty(RDF.type, PREFS.PreferenceFlag)) {
					if (i >= args.length) {
						throw new ArgumentException(
								"Argument must take a value: " + key);
					}
					t.invoke(prefs, handler, args[i++]);
				} else {
					t.invoke(prefs, handler, null);
				}
			} catch (RDFException e) {
				throw new RuntimeException("Model changed unexpectedly", e);
			}
		}
	}

	private TriggerHandler findHandler(Resource trigger)
			throws PreferenceException {
		TriggerHandler th = null;
		if (loadedTriggers.containsKey(trigger)) {
			return (TriggerHandler) loadedTriggers.get(trigger);
		}
		try {
			String className = prefs.getLocalizedString(trigger, PREFS.invokes);
			Class triggerClass = Class.forName(className);
			Constructor con = triggerClass.getConstructor(new Class[]{});
			th = (TriggerHandler) con.newInstance(new Object[]{});
			loadedTriggers.put(trigger, th);
		} catch (ClassNotFoundException e) {
			throw new PreferenceException(
					"Error while loading TriggerHandler: " + trigger, e);
		} catch (SecurityException e) {
			throw new PreferenceException(
					"Error while loading TriggerHandler: " + trigger, e);
		} catch (NoSuchMethodException e) {
			throw new PreferenceException(
					"Error while loading TriggerHandler: " + trigger, e);
		} catch (IllegalArgumentException e) {
			throw new PreferenceException(
					"Error while loading TriggerHandler: " + trigger, e);
		} catch (InstantiationException e) {
			throw new PreferenceException(
					"Error while loading TriggerHandler: " + trigger, e);
		} catch (IllegalAccessException e) {
			throw new PreferenceException(
					"Error while loading TriggerHandler: " + trigger, e);
		} catch (InvocationTargetException e) {
			throw new PreferenceException(
					"Error while loading TriggerHandler: " + trigger, e);
		} catch (RDFException e) {
			throw new PreferenceException(
					"Error while loading TriggerHandler: " + trigger, e);
		}
		return th;
	}

	/**
	 * Prints the command line usage to the system error stream.
	 * @throws PreferenceException
	 */
	public void printUsage() throws PreferenceException {
		printUsage(1);
	}

	/**
	 * Prints usage to the given level of detail, a non-negative integer. Zero
	 * just prints out a one-line command. One prints out all abbreviated
	 * commands with have rdfs:descriptions. Two prints out all labeled commands
	 * with rdfs:descriptions. Three prints out all commands.
	 * 
	 * Four or more prints out commands with their dc:descriptions.
	 * 
	 * @param lod
	 * @throws PreferenceException
	 */
	public void printUsage(int lod) throws PreferenceException {
		prefs.model.enterCriticalSection(ModelLock.READ);
		try {
			// command line is <command> <arguments>. This is defined in
			// "lal:Core --lal:shellCommand-> "command" .
			PrintWriter out = new PrintWriter(System.err);
			if (!prefs.model.contains(LAL.Core, LAL.shellCommand)) {
				throw new PreferenceException(
						"Error: Config does not contain shell command.");
			} else {
				out.println(prefs
						.getLocalizedString(LAL.Core, LAL.shellCommand));
			}
			if (lod == 0) {
				return;
			}

			ResIterator iter = prefs.model
					.listSubjectsWithProperty(PREFS.invokes);
			while (iter.hasNext()) {
				Resource trigger = (Resource) iter.next();
				String S = "";
				int level = 10;
				if (trigger.hasProperty(PREFS.longName)) {
					level = 3;
					S += ", -D"
							+ prefs.getLocalizedString(trigger, PREFS.longName);
				}
				if (trigger.hasProperty(RDFS.label)) {
					level = 2;
					S += ", --" + prefs.getLocalizedString(trigger, RDFS.label);
				}
				if (trigger.hasProperty(PREFS.abbr)) {
					level = 1;
					S += ", -" + prefs.getLocalizedString(trigger, PREFS.abbr);
				}
				if (S.length() > 0) {
					out.println(S.substring(2));
					if (lod >= level) {
						String desc = null;
						if (trigger.hasProperty(DC_11.description)) {
							desc = prefs.getLocalizedString(trigger,
									DC_11.description);
						} else if (trigger.hasProperty(RDFS.comment)) {
							desc = prefs.getLocalizedString(trigger,
									RDFS.comment);
						}
						if (lod < 4 || desc == null) {
							if (trigger.hasProperty(DC_11.title)) {
								out.println("\t"
										+ prefs.getLocalizedString(trigger,
												DC_11.title));
							}
						} else {
							out.println("\t" + desc);
						}
					}
				}
			}
			out.flush();
		} catch (RDFException e) {
			throw new PreferenceException(
					"Error while parsing java properties", e);
		} finally {
			prefs.model.leaveCriticalSection();
		}
	}

	/**
	 * Loads the three maps with trigger functor by name.
	 * 
	 * @param trigger
	 *            the trigger to add
	 * @throws PreferenceException
	 *             if the trigger has no name
	 */
	public void addTrigger(Resource trigger) throws PreferenceException {
		String longName = null, label = null, abbr = null, invokes = null;
		if (trigger.hasProperty(RDFS.label)) {
			longName = label = prefs.getLocalizedString(trigger, RDFS.label);
		}
		if (trigger.hasProperty(PREFS.longName)) {
			longName = prefs.getLocalizedString(trigger, PREFS.longName);
		}
		if (trigger.hasProperty(PREFS.abbr)) {
			abbr = prefs.getLocalizedString(trigger, PREFS.abbr);
		}
		if (trigger.hasProperty(PREFS.invokes)) {
			invokes = prefs.getLocalizedString(trigger, PREFS.invokes);
		}

		if (label == null && longName == null && abbr == null) {
			throw new PreferenceException(
					"Ignoring Preference Trigger without any name: " + trigger);
		}
		if (invokes == null) {
			throw new PreferenceException(
					"Cannot have a preference trigger without a matching class: "
							+ trigger);
		}
		TrigNames n = new TrigNames();
		if (abbr != null) {
			shortNames.put(abbr, trigger);
			n.shortName = abbr;
		}
		if (label != null) {
			longNames.put(label, trigger);
			n.longName = label;
		}
		if (longName != null) {
			prefs.getLogger().config("Adding trigger w/ name " + longName);
			javaProps.put(longName, trigger);
			n.propName = longName;
		}
		rlookup.put(trigger, n);
	}

	/**
	 * Removes the specified trigger. Won't do anything about triggers that have
	 * already been executed, though.
	 * 
	 * @param trigger
	 *            the trigger to remove
	 * @throws PreferenceException
	 *             if the trigger wasn't found
	 */
	public void removeTrigger(Resource trigger) throws PreferenceException {
		TrigNames n = (TrigNames) rlookup.get(trigger);
		if (n != null) {
			loadedTriggers.remove(trigger);
			if (n.shortName != null) {
				shortNames.remove(n.shortName);
			}
			if (n.longName != null) {
				longNames.remove(n.longName);
			}
			if (n.propName != null) {
				javaProps.remove(n.propName);
			}
		}
	}

	/**
	 * @return
	 */
	public PrefsManager getPrefs() {
		return prefs;
	}

	/**
	 * @param manager
	 */
	public void setPrefs(PrefsManager manager) {
		prefs = manager;
		clearTriggerMaps();
		prefs.model.enterCriticalSection(ModelLock.READ);
		try {
			ResIterator iter = prefs.model
					.listSubjectsWithProperty(PREFS.invokes);
			while (iter.hasNext()) {
				Resource trigger = (Resource) iter.next();
				addTrigger(trigger);
			}
		} catch (RDFException e) {
			throw new RuntimeException(e);
		} catch (PreferenceException e) {
			throw new RuntimeException(e);
		} finally {
			prefs.model.leaveCriticalSection();
		}
		invokePropertyTriggers();
		prefs.addListener(this.getUserDirectoryListener());
	}
}