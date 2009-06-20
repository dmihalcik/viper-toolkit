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

package edu.umd.cfar.lamp.viper.examples.persontracking;

import java.awt.*;
import java.awt.event.*;
import java.util.*;

import javax.swing.*;

import viper.api.*;
import viper.api.datatypes.*;
import viper.api.impl.*;
import viper.api.time.*;
import viper.api.time.Frame;
import edu.umd.cfar.lamp.apploader.prefs.*;
import edu.umd.cfar.lamp.viper.gui.core.*;

/**
 * A useful little widget that allows a user to quickly draw 
 * boxes around people in an image. Requires using a specfic viper schema:
 * an OBJECT Person descriptor with a bbox Location. Basically, this lets the
 * user create a new person, or, from an existing person, add new shapes.
 */
public class PersonSelector extends JToolBar {
	/// In order to get support from the RDF prefs manager, we need a namespace
	private static final String MyURI = "http://viper-toolkit.sf.net/personselector#";
	
	/// The view mediator controls the data and the relationship between all loaded beans
	private ViperViewMediator mediator;
	
	/// the limn3 preferences!
	private PrefsManager prefs;
	
	/// the descriptor the user is editing/will edit when they click on the canvas
	private Descriptor neoDesc;
	
	/// The 'Zone' descriptor schema, if it exists
	private Config personCfg;
	
	/// flag to make sure resetNeoDesc isn't by an event handler responding to itself
	private boolean resettingNeoDesc = false;
	
	/// flag for turning on and off 'autocreate' mode. Toggle between autocreate and standard edit...
	private boolean editing = true;
	
	/// Listens for changes to the data and interface
	private ViperMediatorChangeListener vmcl = new ViperMediatorChangeListener() {
		public void schemaChanged(ViperMediatorChangeEvent e) {
			if (!resettingNeoDesc) {
				EventQueue.invokeLater(new Runnable() {
					public void run() {
						resetSchema();
						resetNeoDesc();
						resetPriorities();
					}});
			}
		}

		public void currFileChanged(ViperMediatorChangeEvent e) {
			if (!resettingNeoDesc) {
				EventQueue.invokeLater(new Runnable() {
					public void run() {
						resetNeoDesc();
						resetPriorities();
					}});
			}
		}

		public void mediaChanged(ViperMediatorChangeEvent e) {
		}

		public void frameChanged(ViperMediatorChangeEvent e) {
			if (!resettingNeoDesc) {
				EventQueue.invokeLater(new Runnable() {
					public void run() {
						resetNeoDesc();
					}});
			}
		}

		public void dataChanged(ViperMediatorChangeEvent e) {
			if (!resettingNeoDesc) {
				EventQueue.invokeLater(new Runnable() {
					public void run() {
						resetNeoDesc();
						resetPriorities();
					}});
			}
		}
	};
	
	/**
	 * 
	 */
	public PersonSelector() {
		final JCheckBox hideOthers = new JCheckBox("Hide Non-Zone Shapes", false);
		hideOthers.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if (mediator == null)
					return; 
				if (hideOthers.isSelected()) {
					// TODO: hide/unhide other descriptors
				}
			}});
		this.add(hideOthers);
	}
	/**
	 * @return
	 */
	public ViperViewMediator getMediator() {
		return mediator;
	}

	/**
	 * @return
	 */
	public PrefsManager getPrefs() {
		return prefs;
	}

	/**
	 * @param mediator
	 */
	public void setMediator(ViperViewMediator mediator) {
		this.mediator = mediator;
		resetSchema();
		if (this.mediator != null) {
			this.mediator.addViperMediatorChangeListener(vmcl);
		}
	}

	/**
	 * @param manager
	 */
	public void setPrefs(PrefsManager manager) {
		prefs = manager;
	}


	/**
	 * Resets the colors and types for the Zone 
	 * descriptor.
	 *
	 */
	public void resetSchema() {
		if (mediator == null) {
			personCfg = null;
		} else {
			final boolean[] change = new boolean[] {false};
			Runnable resetSchema = new Runnable() {
				public void run() {
					ViperData v = mediator.getViperData();
					personCfg = v.getConfig(Config.OBJECT, "Person");
					if (personCfg == null) {
						personCfg = v.createConfig(Config.OBJECT, "Person");
					}
					
					// Remove all attributes with the right name but wrong type or whatever
					if (personCfg.hasAttrConfig("Location")) {
						AttrConfig ac = personCfg.getAttrConfig("Location");
						if (!ac.isDynamic() || !ViperDataFactoryImpl.BBOX.equals(ac.getAttrType())) {
							personCfg.removeChild(personCfg.getAttrConfig("Location"));
						}
					}
					if (personCfg.hasAttrConfig("Name")) {
						AttrConfig ac = personCfg.getAttrConfig("Name");
						if (ac.isDynamic() || !ViperDataFactoryImpl.SVALUE.equals(ac.getAttrType())) {
							personCfg.removeChild(personCfg.getAttrConfig("Name"));
						}
					}
					if (personCfg.hasAttrConfig("Priority")) {
						AttrConfig ac = personCfg.getAttrConfig("Priority");
						if (!ac.isDynamic() || !ViperDataFactoryImpl.DVALUE.equals(ac.getAttrType())) {
							personCfg.removeChild(personCfg.getAttrConfig("Priority"));
						}
					}
					
					if (!personCfg.hasAttrConfig("Location")) {
						personCfg.createAttrConfig("Location", ViperDataFactoryImpl.BBOX, true, null, new AttributeBbox());
						change[0] = true;
					}
					if (!personCfg.hasAttrConfig("Name")) {
						personCfg.createAttrConfig("Name", ViperDataFactoryImpl.SVALUE, false, null, new Svalue());
						change[0] = true;
					}
					if (!personCfg.hasAttrConfig("Priority")) {
						personCfg.createAttrConfig("Priority", ViperDataFactoryImpl.DVALUE, true, null, new Dvalue());
						change[0] = true;
					}
				}
			};
			ViperData v = mediator.getViperData();
			Util.tryTransaction(resetSchema, v, MyURI + "resetSchema", new Object[]{});
			if (change[0]) {
				getMediator().getActionHistory().markSavedNow();
			}
		}
	}

	
	/**
	 * Resets the currently selected descriptor, as far as the zone 
	 * selector is concerned. This is necessary to avoid creating
	 * empty zone descriptors in the data set.
	 */
	public void resetNeoDesc() {
		if (mediator == null || mediator.getCurrFile() == null || mediator.getCurrentFrame() == null) {
			neoDesc = null;
			return;
		}
		makeSureNewDescriptorIsLoaded();
	}
	
	/**
	 * Makes sure that a priority is set for each piece of evidence.
	 */
	public void resetPriorities() {
		if (mediator == null || mediator.getCurrFile() == null || mediator.getCurrentFrame() == null) {
			return;
		}
		Runnable resetPriority = new Runnable() {
			public void run() {
				Sourcefile sf = mediator.getCurrFile();
				Iterator people = sf.getDescriptorsBy(personCfg);
				while (people.hasNext()) {
					Descriptor person = (Descriptor) people.next();
					int maxPriority = 0;
					Iterator priorities = person.getAttribute("Priority").getAttrValuesOverWholeRange();
					while (priorities.hasNext()) {
						DynamicAttributeValue curr = (DynamicAttributeValue) priorities.next();
						maxPriority = Math.max(maxPriority, ((Integer) curr.getValue()).intValue());
					}
					Iterator locations = person.getAttribute("Location").getAttrValuesOverWholeRange();
					while (locations.hasNext()) {
						DynamicAttributeValue curr = (DynamicAttributeValue) locations.next();
						Iterator frames = curr.iterator();
						while (frames.hasNext()) {
							Frame frame = (Frame) frames.next();
							if (person.getAttribute("Priority").getAttrValueAtInstant(frame) == null) {
								person.getAttribute("Priority").setAttrValueAtSpan(new Integer(++maxPriority), new Span(frame, (Frame) frame.next()));
							}
						}
					}
				}
			}
		};
		Util.tryTransaction(resetPriority, mediator.getCurrFile(), MyURI + "resetPriorities", new Object[] {});
	}
	
	/**
	 * 
	 */
	private void makeSureNewDescriptorIsLoaded() {
		resettingNeoDesc = true;
		boolean descDetached = (neoDesc != null && neoDesc.getSourcefile() == null);
		boolean descDefinedHere = (neoDesc != null && !descDetached && neoDesc.getAttribute("Location").getAttrValueAtInstant(mediator.getCurrentFrame()) != null);
		boolean shouldReplace = descDetached || (!isEditing() && descDefinedHere);
		if (shouldReplace || isEditing()) {
			// if detached or already set to a value on this frame in create mode
			neoDesc = null;
		}
		if (isEditing()) {
			// First, check to see if a person is selected. If so, use that.
			if (mediator.getPrimarySelection().isFilteredBy(Descriptor.class)) {
				Descriptor tempDesc = (Descriptor) mediator.getPrimarySelection().getDescriptors().next();
				if (tempDesc.getConfig().equals(personCfg)) {
					neoDesc = tempDesc;
				}
			}
			// Then, check to see if a person exists. Grab the first one you can find.
			if (neoDesc == null) {
				Iterator personIter = mediator.getCurrFile().getDescriptorsBy(personCfg);
				if (personIter.hasNext()) {
					neoDesc = (Descriptor) personIter.next();
				}
			}
		} else if (!isEditing()) {
			if (neoDesc != null && (!neoDesc.getSourcefile().equals(mediator.getCurrFile()) || isEditing())) {
				mediator.getSelection().setTo(Collections.EMPTY_SET);
				Descriptor oldDesc = neoDesc;
				neoDesc = null;
				Node n = oldDesc.getParent();
				if (n != null) {
					n.removeChild(oldDesc);
				}
				// Will throw a change event, which will get us back to d'oh
				return;
			}
		}
		if (neoDesc == null) {
			neoDesc = mediator.getCurrFile().createDescriptor(personCfg);
		}
		Attribute a = neoDesc.getAttribute("Location");
		boolean alreadySelected = (mediator.getPrimarySelection().isFilteredBy(Attribute.class)) && mediator.getSelection().isSelected(a);
		if (!alreadySelected) {
			mediator.getSelection().setTo(a);
		}
		resettingNeoDesc = false;
	}
	public boolean isEditing() {
		return editing;
	}
	public void setEditing(boolean editing) {
		if (this.editing == editing) {
			return;
		}
		this.editing = editing;
		// TODO If switching to edit mode, remove neodesc 
		// and select the previous descriptor.
		resetNeoDesc();
	}
}
