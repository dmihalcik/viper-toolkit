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

package edu.umd.cfar.lamp.apploader;

import java.awt.event.*;
import java.beans.*;
import java.util.logging.*;

import javax.swing.*;
import javax.swing.event.*;

import com.hp.hpl.jena.rdf.model.*;
import com.hp.hpl.jena.rdf.model.Statement;
import com.hp.hpl.jena.vocabulary.*;

import edu.umd.cfar.lamp.apploader.prefs.*;

/**
 * An action proxy that passes on invocations to an action or
 * actionListener specified by the AppLoader.
 * @author davidm
 */
class Actionator extends AbstractAction {
	private EventListenerList handlers = new EventListenerList();
	private static Logger logger = Logger.getLogger("edu.umd.cfar.lamp.apploader");
	private String description;

	private PropertyChangeListener enabledListener = new PropertyChangeListener() {
		public void propertyChange(PropertyChangeEvent evt) {
			if ("enabled".equals(evt.getPropertyName())) {
				checkEnabled();
			}
		} 
	};
	
	private void checkEnabled() {
		boolean oldVal = isEnabled();
		boolean enabled = false;
		boolean hasAction = false;
		Object[] H = handlers.getListenerList();
		for (int i = H.length - 2; i >= 0; i-=2) {
			if (H[i] == Action.class) {
				enabled = enabled || ((Action) H[i+1]).isEnabled();
				hasAction = true;
			}
		}
		enabled = hasAction ? enabled : true;
		if (oldVal != enabled) {
			setEnabled(enabled);
		}
	}

	/**
	 * Adds a delegate to this actionator.
	 * @param al the action this is proxy for
	 */
	public void addHandler(ActionListener al) {
		if (al instanceof Action) {
			((Action) al).addPropertyChangeListener(enabledListener);
			handlers.add(Action.class, al);
		} else {
			handlers.add(ActionListener.class, al);
		}
	}
	
	/**
	 * Removes the delegate action from this actionator.
	 * @param al the delegate action or actionlistener to remove
	 */
	public void removeHandler(ActionListener al) {
		if (al instanceof Action) {
			((Action) al).removePropertyChangeListener(enabledListener);
			handlers.remove(Action.class, al);
		} else {
			handlers.remove(ActionListener.class, al);
		}
	}

	/**
	 * Gets the command string that will be sent to the
	 * delegates when an action is invoked.
	 * @return the current command string
	 */
	public String getCommand() {
		return (String) getValue(Action.ACTION_COMMAND_KEY);
	}

	/**
	 * Sets the command that will be passed to the delegate actions.
	 * @param command the command to send the delegates when an action is performed
	 */
	public void setCommand(String command) {
		putValue(Action.ACTION_COMMAND_KEY, command);
	}

	/**
	 * Passes the event to the delegate actions.
	 * @param e the event to pass on
	 */
	public void actionPerformed(ActionEvent e) {
		logger.fine(
			"Recieved action event with command " + e.getActionCommand());

		Object[] H = handlers.getListenerList();
		for (int i = H.length - 2; i >= 0; i-=2) {
			ActionListener currHandler = (ActionListener) H[i+1];
			currHandler.actionPerformed(e);
		}
	}
	
	/**
	 * Tests to see if this proxy consumes events. It does.
	 * @return <code>true</code>
	 */
	public boolean isConsumer() {
		return true;
	}

	/**
	 * Parses the given actionator in from the specified RDF resource.
	 * @param apl the apploader containing all the loaded beans
	 * @param r the resource describing the action proxy
	 * @return the action proxy
	 * @throws PreferenceException if there is an error in the resource
	 */
	public static Actionator parseAction(AppLoader apl, Resource r) throws PreferenceException {
		Actionator a = new Actionator();
		String command = null;

		if (r.hasProperty(LAL.actionCommand)) {
			RDFNode commandN = r.getProperty(LAL.actionCommand).getObject();
			if (commandN != null) {
				command = commandN.toString();
				a.setCommand(command);
			}
		}

		if (r.hasProperty(LAL.sendsTo)) {
			Statement sts = r.getProperty(LAL.sendsTo);
			Resource actionListener =  sts.getResource();
			a.addHandler((ActionListener) apl.rdfNodeToValue(actionListener));
			a.checkEnabled();
		} else {
			apl.getLogger().warning("Missing recipient for action: " + r);
		}
		
		if (r.hasProperty(DC_11.description)) {
			a.setDescription(apl.getPrefs().getLocalizedString(r, DC_11.description));
		}

		apl.getLogger().config(
			"Created action "
				+ r
				+ ((command != null) ? ("with command " + command) : ""));

		return a;
	}
	
	/**
	 * Gets the description of the action.
	 * @return Returns the description.
	 */
	public String getDescription() {
		return description;
	}
	/**
	 * Sets the description.
	 * @param description a description of the effects of the action
	 */
	public void setDescription(String description) {
		this.description = description;
	}
}
