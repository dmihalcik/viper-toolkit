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


package edu.umd.cfar.lamp.viper.gui.core;

import java.awt.event.*;
import java.util.*;

import javax.swing.*;
import javax.swing.event.*;

import viper.api.*;
import viper.api.time.*;

import edu.umd.cfar.lamp.viper.util.*;

/**
 * Maintains a list of user selection actions, and allows the user 
 * to page through them.
 * @author davidm
 */
public class SelectionHistory {
	private final class SelectionChangeListener implements ChangeListener {
		public void stateChanged(ChangeEvent e) {
			Instant when = mediator.getMajorMoment();
			ViperSelectionSetWithPrimarySelection what = (ViperSelectionSetWithPrimarySelection) mediator.getSelection();
			add(when, what.getPrimary().getLastSelectedNode());
		}
	}
	
	private AbstractAction goBackAction = new AbstractAction("Previous Selection"){
		public void actionPerformed(ActionEvent e) {
			back();
		}
	
	};
	private AbstractAction goForwardAction = new AbstractAction("Next Selection"){
		public void actionPerformed(ActionEvent e) {
			next();
		}
	
	};

	private List selectionHistory = new LinkedList();
	private int cursor;
	private ViperViewMediator mediator;
	private ViperData lastRoot;
	private boolean shuttling;

	public ViperViewMediator getMediator() {
		return mediator;
	}

	public void setMediator(ViperViewMediator mediator) {
		if (this.mediator != null) {
			this.mediator.getSelection().removeChangeListener(new SelectionChangeListener());
		}
		this.mediator = mediator;
		if (this.mediator != null) {
			this.mediator.getSelection().addChangeListener(new SelectionChangeListener());
		}
	}
	
	public void clear() {
		cursor = 0;
		selectionHistory.clear();
		lastRoot = null;
	}

	/**
	 * @param when
	 * @param what
	 */
	private void add(Instant when, Node what) {
		if (when == null || what == null) {
			return;
		}
		if (shuttling) {
			return;
		}
		if (what.getRoot() != lastRoot) {
			clear();
		}
		lastRoot = what.getRoot();
		if (!selectionHistory.isEmpty()) {
			cursor--;
			if (what.equals(getCurrSelection())) {
				cursor++;
				return;
			}
			cursor++;
		}
		if (!atEnd()) {
			selectionHistory.subList(cursor, selectionHistory.size()).clear();
		}
		cursor++;
		selectionHistory.add(new Pair(when, what));
		resetActions();
	}

	/**
	 * 
	 */
	private void resetActions() {
		goBackAction.setEnabled(!atFirst());
		goForwardAction.setEnabled(!atEnd());
	}
	
	public boolean atFirst() {
		return cursor <= 1;
	}
	public boolean atEnd() {
		return selectionHistory.size() <= cursor;
	}
	public Instant getCurrTime() {
		if (atEnd()) {
			throw new IndexOutOfBoundsException();
		}
		return (Instant) ((Pair) selectionHistory.get(cursor)).getFirst();
	}
	public Node getCurrSelection() {
		if (atEnd()) {
			throw new IndexOutOfBoundsException();
		}
		return (Node) ((Pair) selectionHistory.get(cursor)).getSecond();
	}
	public void back() {
		if (atFirst()) {
			return;
		}
		shuttling = true;
		try {
			// searches for previous nodex that still exists
			int failsafe = cursor;
			cursor --;
			do {
				if(atFirst()) {
					cursor = failsafe;
					return;
				}
				cursor--;
			} while(getCurrSelection().getRoot() != lastRoot);
			this.mediator.getSelection().setTo(getCurrSelection());
			this.mediator.setMajorMoment(getCurrTime());
			cursor++;
			resetActions();
		} finally {
			shuttling = false;
		}
	}
	public void next() {
		if (atEnd()) {
			return;
		}
		shuttling = true;
		try {
			this.mediator.getSelection().setTo(getCurrSelection());
			this.mediator.setMajorMoment(getCurrTime());
			cursor++;
			
			resetActions();
		} finally {
			shuttling = false;
		}
	}

	public AbstractAction getGoBackAction() {
		return goBackAction;
	}

	public AbstractAction getGoForwardAction() {
		return goForwardAction;
	}
}
