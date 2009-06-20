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

package edu.umd.cfar.lamp.apploader.undo;

import java.util.*;

import javax.swing.undo.*;

import com.hp.hpl.jena.rdf.model.*;
import com.hp.hpl.jena.vocabulary.*;

import edu.umd.cfar.lamp.apploader.*;
import edu.umd.cfar.lamp.apploader.prefs.*;

/**
 * Converts a {@link LabeledUndoableEdit}into a Swing undoable.
 * 
 * @author davidm@cfar.umd.edu
 */
public class LalUndoableEditWrapper extends AbstractUndoableEdit {
	private LabeledUndoableEdit lue;
	private String shortName;
	private RDFNode longName;

	/**
	 * Create a new swing undoable from the given AppLoader undoable and its
	 * associated application preferences.
	 * 
	 * @param lue
	 *            The undoable to wrap
	 * @param prefs
	 *            The preferences to use while generating the swing undoable
	 */
	public LalUndoableEditWrapper(LabeledUndoableEdit lue, PrefsManager prefs) {
		this.lue = lue;
		this.prefs = prefs;
		shortName = lue.toString();
		String uri = lue.getUri();
		if (uri != null) {
			shortName = uri;
			prefs.model.enterCriticalSection(ModelLock.READ);
			try {
				Resource described = prefs.model.getResource(uri);
				ResIterator iter = prefs.model.listSubjectsWithProperty(
						UNDO.forEdit, described);
				if (iter.hasNext()) {
					Resource describer = iter.nextResource();
					if (describer.hasProperty(RDFS.label)) {
						shortName = prefs.getLocalizedString(describer,
								RDFS.label);
					}
					Resource pourLangue = prefs.getLocalizedResource(describer,
							UNDO.text);
					if (null != pourLangue) {
						longName = pourLangue.getProperty(UNDO.value)
								.getObject();
					}
				}
			} finally {
				prefs.model.leaveCriticalSection();
			}
		}
	}

	private PrefsManager prefs;

	/**
	 * Get the presentation name for the undoable. If it exists, this is the
	 * long name. Otherwise, it is the short name (rdfs:label) for the
	 * describer, if found. Otherwise, it is the URI of the undoable.
	 * 
	 * @return the presentation name for the undoable, to show up in menus or
	 *         lists of events
	 */
	public String getPresentationName() {
		if (longName != null) {
			Object value;
			try {
				value = prefs.getCore().rdfNodeToValue(longName,
						lue.getClient());
			} catch (PreferenceException e1) {
				prefs.getLogger().warning(e1.getLocalizedMessage());
				return shortName;
			}
			if (value instanceof List) {
				StringBuffer sb = new StringBuffer();
				for (Iterator e = ((List) value).iterator(); e.hasNext();) {
					sb.append(String.valueOf(e.next()));
				}
				return sb.toString();
			} else {
				return value.toString();
			}
		} else {
			return shortName;
		}
	}

	/**
	 * Gets the short name (rdfs:label).
	 * 
	 * @return the rdfs:label name of the undoable action
	 */
	public String toString() {
		return shortName;
	}

	/**
	 * {@inheritDoc}
	 */
	public boolean canRedo() {
		return lue.canRedo();
	}

	/**
	 * {@inheritDoc}
	 */
	public boolean canUndo() {
		return lue.canUndo();
	}

	/**
	 * {@inheritDoc}
	 */
	public void die() {
		lue.die();
	}

	/**
	 * {@inheritDoc}
	 */
	public void redo() throws CannotRedoException {
		lue.redo();
	}

	/**
	 * {@inheritDoc}
	 */
	public void undo() throws CannotUndoException {
		lue.undo();
	}
}