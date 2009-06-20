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

package edu.umd.cfar.lamp.viper.examples;

import java.awt.*;
import java.awt.event.*;

import javax.swing.*;

import viper.api.*;
import viper.api.datatypes.*;
import viper.api.extensions.*;
import viper.api.time.*;
import viper.api.time.Frame;
import edu.umd.cfar.lamp.apploader.prefs.*;
import edu.umd.cfar.lamp.viper.gui.core.*;

/**
 * A useful little widget that allows a user to quickly draw 
 * text regions on an image. Requires using a specfic viper schema:
 * an OBJECT Zone descriptor with three fields: a shape Location, an
 * lvalue Type, and a relation Next.
 * 
 * For more information, see the Scripting ViPER manual, which
 * includes a description of this widget.
 */
public class ZoneSelector extends JPanel {
	/// In order to get support from the RDF prefs manager, we need a namespace
	private static final String MyURI = "http://viper-toolkit.sf.net/zoneselector";
	
	/// The view mediator controls the data and the relationship between all loaded beans
	private ViperViewMediator mediator;
	
	/// the limn3 preferences!
	private PrefsManager prefs;
	
	/// The current zone type the user has selected
	private String selectedType;
	
	/// the descriptor the user is editing/will edit when they click on the canvas
	private Descriptor neoDesc;
	
	/// The 'Zone' descriptor schema, if it exists
	private Config zoneCfg;
	
	/// the list of possible types, taken from the zoneCfg schema
	private String[] possibleTypes;
	
	/// panel with a list of buttons, one for each of the possible zone types
	private JPanel typeSelectionPanel;
	
	/// flag to make sure resetNeoDesc isn't by an event handler responding to itself
	private boolean resettingNeoDesc = false;
	
	/// The last button pressed; needs to be unpressed when another button is pushed
	private JButton lastButtonPressed;
	
	/// Listens for changes to the data and interface
	private ViperMediatorChangeListener vmcl = new ViperMediatorChangeListener() {
		public void schemaChanged(ViperMediatorChangeEvent e) {
			resetPossibles();
			resetNeoDesc();
		}

		public void currFileChanged(ViperMediatorChangeEvent e) {
			resetNeoDesc();
		}

		public void mediaChanged(ViperMediatorChangeEvent e) {
		}

		public void frameChanged(ViperMediatorChangeEvent e) {
			resetNeoDesc();
		}

		public void dataChanged(ViperMediatorChangeEvent e) {
			if (!resettingNeoDesc) {
				EventQueue.invokeLater(new Runnable() {
					public void run() {
						resetNeoDesc();
					}});
			}
		}
	};
	
	/// When the user presses a zone type button, this is invoked 
	private class SelectWhichType implements ActionListener {
		private String type;
		SelectWhichType(String type) {
			this.type = type;
		}
		/** @inheritDoc */
		public void actionPerformed(ActionEvent e) {
			JButton b = (JButton) e.getSource();
			System.err.println("Button " + type + ", selected = " + b.isSelected());
			setSelectedType(type);
			if (b != lastButtonPressed) {
				if (lastButtonPressed != null) {
					lastButtonPressed.setSelected(false);
				}
				lastButtonPressed = b;
			}
			b.setSelected(true);
		}
	}
	
	/**
	 * 
	 */
	public ZoneSelector() {
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
		typeSelectionPanel = new JPanel();
		typeSelectionPanel.setLayout(new BoxLayout(typeSelectionPanel, BoxLayout.PAGE_AXIS));
		this.add (typeSelectionPanel);
		this.setLayout(new BoxLayout(this, BoxLayout.PAGE_AXIS));
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
	 * Gets the currently selected zone type.
	 * <code>null</code> if no type is selected
	 * @return
	 */
	public String getSelectedType() {
		return selectedType;
	}

	/**
	 * @param mediator
	 */
	public void setMediator(ViperViewMediator mediator) {
		this.mediator = mediator;
		resetPossibles();
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
	public void resetPossibles() {
		if (mediator == null) {
			zoneCfg = null;
		} else {
			zoneCfg = mediator.getViperData().getConfig(Config.OBJECT, "Zone");
			if (zoneCfg != null) {
				if (zoneCfg.hasAttrConfig("Location") && zoneCfg.hasAttrConfig("Type")) {
					if (zoneCfg.getAttrConfig("Type").getParams() instanceof Lvalue) {
						
					} else {
						zoneCfg = null;
					}
				} else {
					zoneCfg = null;
				}
			}
		}
		if (zoneCfg == null) {
			selectedType = null;
			typeSelectionPanel.removeAll();
		} else {
			Lvalue types = (Lvalue) zoneCfg.getAttrConfig("Type").getParams();
			String[] possibles = types.getPossibles();
			if (possibles != this.possibleTypes) {
				typeSelectionPanel.removeAll();
				this.possibleTypes = possibles;
				boolean foundOldSelection = false;
				for (int i = 0; i < possibles.length; i++) {
					String curr = possibles[i];
					JButton possibleButton = new JButton(curr);
					possibleButton.addActionListener(new SelectWhichType(curr));
					typeSelectionPanel.add(possibleButton);
					// TODO: Add color legend to buttons
					if (!foundOldSelection && curr.equals(selectedType)) {
						foundOldSelection = true;
						possibleButton.setSelected(true);
					}
				}
				if (!foundOldSelection) {
					this.selectedType = null;
				}
			}
		}
	}

	/**
	 * Sets the zone type that is currently selected.
	 * @param zoneType the zone type
	 */
	public void setSelectedType(String zoneType) {
		selectedType = zoneType;
		resetNeoDesc();
	}
	
	/**
	 * Resets the currently selected descriptor, as far as the zone 
	 * selector is concerned. This is necessary to avoid creating
	 * empty zone descriptors in the data set.
	 */
	public void resetNeoDesc() {
		if (mediator.getCurrFile() == null) {
			neoDesc = null;
			return;
		}
		resettingNeoDesc = true;
		if (neoDesc != null && neoDesc.getAttribute("Location").getAttrValue() != null) {
			neoDesc = null;
		}
		if (neoDesc != null && !neoDesc.getSourcefile().equals(mediator.getCurrFile())) {
			Descriptor oldDesc = neoDesc;
			neoDesc = null;
			oldDesc.getParent().removeChild(neoDesc);
			// Will throw a change event, which will get us back to d'oh
			return;
		}
		if (selectedType == null) {
			return;
		}

		TransactionalNode trNode = (TransactionalNode) mediator.getCurrFile();
		TransactionalNode.Transaction trans = trNode.begin(MyURI + "#zonify");
		try {
			if (neoDesc == null) {
				neoDesc = mediator.getCurrFile().createDescriptor(zoneCfg);
			}
			InstantRange ir = new InstantRange();
			ir.add(mediator.getCurrentFrame(), (Frame) mediator.getCurrentFrame().next());
			neoDesc.setValidRange(ir);
			neoDesc.getAttribute("Type").setAttrValue(selectedType);
			mediator.getSelection().setTo(neoDesc.getAttribute("Location"));

			trans.commit();
		} finally {
			if (trans.isAlive()) {
				trans.rollback();
			}
		}
		resettingNeoDesc = false;
	}
}
