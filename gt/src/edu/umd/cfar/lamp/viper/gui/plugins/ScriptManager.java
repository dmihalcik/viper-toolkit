/***************************************
 *            ViPER                    *
 *  The Video Processing               *
 *         Evaluation Resource         *
 *                                     *
 *  Distributed under the GPL license  *
 *        Terms available at gnu.org.  *
 *                                     *
 *  Copyright University of Maryland,  *
 *                      College Park.  *
 ***************************************/

package edu.umd.cfar.lamp.viper.gui.plugins;

import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.net.*;
import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.logging.*;

import javax.swing.*;

import org.codehaus.groovy.control.*;

import viper.api.*;
import viper.api.impl.*;
import viper.descriptors.*;

import com.hp.hpl.jena.mem.*;
import com.hp.hpl.jena.rdf.model.*;
import com.hp.hpl.jena.vocabulary.*;

import edu.umd.cfar.lamp.apploader.*;
import edu.umd.cfar.lamp.apploader.prefs.*;
import edu.umd.cfar.lamp.viper.gui.core.*;
import edu.umd.cfar.lamp.viper.util.*;
import groovy.lang.*;

/**
 * Searches the 'script' directory for files to run and adds them to the script
 * menu. It also watches the 'script' directory for changes.
 */
public class ScriptManager implements ActionListener {
	private static Logger logger = Logger
			.getLogger("edu.umd.cfar.lamp.viper.gui.plugins");
	private ViperViewMediator mediator;
	private Resource parentResource;
	private Action resetAction = new AbstractAction("Reset Menu") {
		private static final long serialVersionUID = 1L;

		public void actionPerformed(ActionEvent e) {
			resetScriptMenu();
		}
	};
	private CacheScripts cache = new CacheScripts();
	private class CacheScripts {
		private Map<URI, Pair> uri2script = new HashMap<>();
		/// load the script and put it in the map
		private Pair loadScript(URI uri) {
			File script = new File(uri);
			Long l = new Long(script.lastModified());
			Pair p;
			if (script.getName().toLowerCase().endsWith(".groovy")) {
				ClassLoader parent = getClass().getClassLoader();
				GroovyClassLoader loader = new GroovyClassLoader(parent);
				try {
					Class<?> groovyClass = loader.parseClass(script);
					p = new Pair(l, groovyClass.newInstance());
				} catch (CompilationFailedException e1) {
					logger.log(Level.SEVERE, "Error while trying to compile groovy script " + script, e1);
					return null;
				} catch (IOException e1) {
					logger.log(Level.SEVERE, "Error while trying to load groovy script " + script, e1);
					return null;
				} catch (InstantiationException e1) {
					logger.log(Level.SEVERE, "Error while trying to instantiate object in groovy script " + script, e1);
					return null;
				} catch (IllegalAccessException e1) {
					logger.log(Level.SEVERE, "Access error while loading groovy script " + script, e1);
					return null;
				}
			} else {
				p = new Pair(l, new FileScript(script));
			}
			uri2script.put(uri, p);
			return p;
		}
		public AppLoaderScript getScript(URI uri) {
			Pair p = (Pair) uri2script.get(uri);
			if (p == null) {
				p = loadScript(uri);
			} else {
				long time = ((Long) p.getFirst()).longValue();
				if (new File(uri).lastModified() > time) {
					p = loadScript(uri);
				}
			}
			if (p == null) {
				return null;
			}
			return (AppLoaderScript) p.getSecond();
		}
	}
	

	/**
	 * Gets the viper UI mediator the script manager should link with.
	 * 
	 * @return the associated UI mediator, or <code>null</code> if none is set
	 */
	public ViperViewMediator getMediator() {
		return mediator;
	}

	/**
	 * Sets the viper UI mediator the script manager should link with.
	 * TODO-davidm: add an event listener to get changes to the location of scripts
	 * FIXME-davidm: add a timer loop that checks for new scripts
	 * @param mediator
	 *            the UI mediator
	 */
	public void setMediator(ViperViewMediator mediator) {
		this.mediator = mediator;
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				resetScriptMenu();
			}
		});
	}

	/**
	 * Adds the menu that menuNode represents to the model m.
	 * @param menuNode the menu node. Can be a menu, group, or menu item.
	 * @param m the model
	 */
	private void addMenuTreeFromResource(Resource menuNode, Model m) {
		if (menuNode.hasProperty(RDF.type, MENU.Item)) {
		} else if (menuNode.hasProperty(RDF.type, MENU.Group) || menuNode.hasProperty(RDF.type, MENU.Menu)) {
			ResIterator kids = menuNode.getModel().listSubjectsWithProperty(MENU.attachment, menuNode);
			while (kids.hasNext()) {
				addMenuTreeFromResource(kids.nextResource(), m);
			}
		}
		StmtIterator itemDesc = menuNode.listProperties();
		m.add(itemDesc);
	}
	
	private static Resource createGroup(Model m, String label, int priority, Resource attach) {
		return m.createResource().addProperty(RDF.type, MENU.Group).addProperty(RDFS.label, label).addProperty(MENU.priority, priority).addProperty(MENU.attachment, attach);
	}
	private static Resource createItem(Model m, String label, int priority, Resource attach, Resource actionate) {
		return m.createResource().addProperty(RDF.type, MENU.Item).addProperty(RDFS.label, label).addProperty(MENU.priority, priority).addProperty(MENU.attachment, attach).addProperty(MENU.generates, actionate);
	}
	
	/**
	 * Reloads the script menu from the file system.
	 *  
	 */
	public void resetScriptMenu() {
		Model prefModel = mediator.getPrefs().model;
		prefModel.enterCriticalSection(ModelLock.READ);
		Model toAdd = new ModelMem();
		Model toRemove = new ModelMem();
		try {
			ResIterator groups = mediator.getPrefs().getTemporary().listSubjectsWithProperty(MENU.attachment, getParentResource());
			while (groups.hasNext()) {
				addMenuTreeFromResource(groups.nextResource(), toRemove);
			}
			
			Resource scriptGroup = createGroup(toAdd, "Scripts", 1, getParentResource());
//			Resource resetGroup = createGroup(toAdd, "Reset", -1, getParentResource());
//			Resource resetItem = createItem(toAdd, "Reset Menu", 0, resetGroup);
//			resetItem.addProperty(MENU.mnemonic, "R");
			
			URI userDir = null;
			URI systemDir = null;
			try {
				userDir = mediator.getPrefs().getUserDirectory();
			} catch (PreferenceException e) {
				logger.log(Level.CONFIG, "Unable to get userDir", e);
			}
			try {
				systemDir = mediator.getPrefs().getSystemDirectory();
			} catch (PreferenceException e1) {
				logger.log(Level.CONFIG, "Unable to get systemDir", e1);
			}
			File progDirs[] = new File[] {new File(systemDir), new File(userDir)};
			for (int sdirIndex = 0; sdirIndex < progDirs.length; sdirIndex++) {
				if (progDirs[sdirIndex] == null) {
					continue;
				}
				File scriptDir = new File(progDirs[sdirIndex]
						+ File.separator + "scripts");
				if (!scriptDir.isDirectory()) {
					continue;
				}
				File[] children = scriptDir.listFiles();
				int menuCount = 0;
				for (int i = 0; children != null && i < children.length; i++) {
					if (children[i].isFile()) {
						URI scriptUri = children[i].toURI();
						AppLoaderScript s = cache.getScript(scriptUri);
						if (s == null) 
							continue;
						String mnem = String.valueOf(++menuCount);
						String label = mnem + " " + s.getScriptName();
						Resource activatorR = toAdd.createResource();
						activatorR.addProperty(LAL.actionCommand, scriptUri);
						activatorR.addProperty(LAL.sendsTo, mediator.getPrefs()
								.getCore().getResourceForBean(this));
						Resource r = createItem(toAdd, label, -200+i, scriptGroup, activatorR);
						if (menuCount < 10) {
							r.addProperty(MENU.mnemonic, mnem);
						}
					}
				}
			}
		} finally {
			prefModel.leaveCriticalSection();
		}
		if (!toRemove.isEmpty() || !toAdd.isEmpty()) {
			mediator.getPrefs().changeTemporary(toRemove, toAdd);
		}
	}

	/**
	 * Gets the URI that this bean is attached to.
	 * 
	 * @return the URI of the container
	 */
	public Resource getParentResource() {
		return parentResource;
	}

	/**
	 * Sets the URI that this bean is attached to.
	 * This is the bean where menu items will be attached.
	 * 
	 * @param parentResource
	 *            the URI of the container
	 */
	public void setParentResource(Resource parentResource) {
		this.parentResource = parentResource;
	}

	/**
	 * Runs the script found specified in the action command string.
	 * 
	 * @param e
	 *            the action event object
	 */
	public void actionPerformed(ActionEvent e) {
		try {
			AppLoaderScript appScript = cache.getScript(new URI(e.getActionCommand()));
			if (appScript == null) {
				throw new RuntimeException("Cannot load script " + e.getActionCommand());
			}
			appScript.run(mediator.getPrefs().getCore());
		} catch (URISyntaxException e2) {
			logger.severe("Misregistered script: " + e.getActionCommand());
		}
	}
	
	private class FileScript implements AppLoaderScript {
		private File script;
		public FileScript(File file) {
			this.script = file;
		}
		
		public void run(AppLoader application) {
			Runtime r = Runtime.getRuntime();
			final Process scriptProc;
			try {
				if (mediator.getFocalFile() == null) {
					scriptProc = r.exec(new String[] {script.toString()});
				} else {
					scriptProc = r.exec(new String[] {script.toString(), mediator.getFocalFile()});
				}
			} catch (IOException e1) {
				logger.log(Level.WARNING, "Error while launching script: " + script, e1);
				return;
			}
			AppLoader appFrame = mediator.getPrefs().getCore();
			JOptionPane msg = new JOptionPane("Running Script:\n" + script.getName(),
					JOptionPane.INFORMATION_MESSAGE, JOptionPane.OK_CANCEL_OPTION);
			final JButton okayButton = new JButton("Okay");
			final JButton cancelButton = new JButton("Cancel");
			okayButton.setEnabled(false);
			msg.setOptions(new Object[] {okayButton, cancelButton});
			final JDialog dialog = msg.createDialog(appFrame, script.getName());
			final boolean[] hasBeenCanceled = new boolean[] {false};
			final SwingWorker<ViperData, Void> sw = new SwingWorker<ViperData, Void>() {
				@Override
				protected ViperData doInBackground() {
					BufferedInputStream in = new BufferedInputStream(scriptProc.getInputStream());
					try {
						ViperData v = new ViperParser().parseDoc(ImportExport.convertToModern(in));
						scriptProc.waitFor();
						return v;
					} catch (InterruptedException e1) {
						if (!hasBeenCanceled[0]) {
							logger.log(Level.SEVERE, "Unable to complete script, interrupted", e1);
						} else {
							logger.log(Level.INFO, "Unable to complete script, canceled", e1);
						}
						dialog.setVisible(false);
						dialog.dispose();
						return null;
					} catch (IOException e) {
						logger.log(Level.SEVERE, "Unable to complete script, error while parsing data: ", e);
						dialog.setVisible(false);
						dialog.dispose();
						return null;
					}
				}
				
				public void done() {
					okayButton.setEnabled(true);
				}
			};
			okayButton.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					ViperData v;
					try {
						v = sw.get();
					} catch (InterruptedException e1) {
						logger.log(Level.INFO, "Interrupted while opening script", e);
						v = null;
					} catch (ExecutionException e1) {
						logger.log(Level.SEVERE, "Error while opening script", e);
						v = null;
					}
					if (v != null && v.getConfigsNode().getNumberOfChildren() > 0) {
						ImportExport.importConfig(mediator.getViperData(), v.getConfigsNode());
						if (v.getSourcefilesNode().getNumberOfChildren() > 0) {
							Sourcefile sf = mediator.getCurrFile();
							if (sf != null) {
								ImportExport.importDataFromGTF(mediator.getCurrFile(), (Sourcefile) v.getSourcefiles().next());
							}
						}
					}
					dialog.setVisible(false);
					dialog.dispose();
				}
			});
			cancelButton.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent arg0) {
					hasBeenCanceled[0] = true;
					sw.cancel(true);
					dialog.setVisible(false);
					dialog.dispose();
				}
			});
			sw.execute();
			dialog.setVisible(true);
		}

		public String getScriptName() {
			return script.getName();
		}
		
	}
	public Action getResetAction() {
		return resetAction;
	}
}