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

import viper.api.*;
import edu.umd.cfar.lamp.chronicle.*;

/**
 * 
 */
public abstract class ViperNodeTimeLine implements TimeLine {
	private ViperChronicleModel viewModel;
	private ViperChronicleSelectionModel selectionModel;
	public abstract Node getNode();
	public ViperChronicleModel getViewModel() {
		return viewModel;
	}
	public void setViewModel(ViperChronicleModel viewModel) {
		this.viewModel = viewModel;
	}
	public ViperChronicleSelectionModel getSelectionModel() {
		return selectionModel;
	}
	public void setSelectionModel(ViperChronicleSelectionModel selectionModel) {
		this.selectionModel = selectionModel;
	}
}
