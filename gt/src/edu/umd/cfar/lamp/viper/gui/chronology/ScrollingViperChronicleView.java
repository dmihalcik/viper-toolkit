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


package edu.umd.cfar.lamp.viper.gui.chronology;

import java.awt.event.*;

import javax.swing.*;
import javax.swing.border.*;

import edu.umd.cfar.lamp.apploader.*;
import edu.umd.cfar.lamp.apploader.prefs.*;
import edu.umd.cfar.lamp.chronicle.*;
import edu.umd.cfar.lamp.viper.gui.core.*;
import edu.umd.cs.piccolox.swing.*;

/**
 * A bean that is a viper chronicle already in a scroll pane. 
 * @author davidm
 */
public class ScrollingViperChronicleView extends PScrollPane {
	private ViperChronicleView wrappedChronicle;
	public ScrollingViperChronicleView () {
		super();
		this.wrappedChronicle = new ViperChronicleView();
		ChronicleViewer.ScrollViews sv = wrappedChronicle.getScrollViews();
		getViewport().setView(sv.content);
		setWheelScrollingEnabled(false); // scroll wheel zooms, not scrolls
		setRowHeaderView(sv.rowHeader);
		setColumnHeaderView(sv.columnHeader);
		setCorner(ScrollPaneConstants.UPPER_LEFT_CORNER, sv.cornerHeader);
		setBorder(new EmptyBorder(0,0,0,0));
		if (AppLoader.isMac()) {
			setVerticalScrollBarPolicy(
				ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
			setHorizontalScrollBarPolicy(
				ScrollPaneConstants.HORIZONTAL_SCROLLBAR_ALWAYS);
		} else {
			setVerticalScrollBarPolicy(
				ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED);
			setHorizontalScrollBarPolicy(
				ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);
		}
	}

	public ViperChronicleView getWrappedChronicle() {
		return wrappedChronicle;
	}
	
	
	
	public ViperViewMediator getMediator() {
		return wrappedChronicle.getMediator();
	}
	public void setMediator(ViperViewMediator med) {
		wrappedChronicle.setMediator(med);
		PrefsManager prefs = med.getPrefs();
		wrappedChronicle.setRef(prefs.getCore().getResourceForBean(this));
	}
	/**
	 * Gets an action that expands all the nodes in the timeline.
	 * @return an action that will expand the timeline tree completely
	 */
	public ActionListener getExpandAllActionListener() {
		return wrappedChronicle.getExpandAllActionListener();
	}

	/**
	 * Gets an action that resizes the timeline to fit the window.
	 * @return the fit-to-window action for the timeline
	 */
	public ActionListener getFitChronicleActionListener() {
		return wrappedChronicle.getFitChronicleActionListener();
	}
	

	public Action getRemoveAllMarksActionListener() {
		return wrappedChronicle.getRemoveAllMarksActionListener();
	}
	public ChronicleSelectionModel getSelectionModel() {
		return wrappedChronicle.getSelectionModel();
	}
}
