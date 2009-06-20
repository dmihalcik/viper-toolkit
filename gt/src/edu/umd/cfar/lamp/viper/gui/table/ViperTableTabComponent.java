/*
 * Created on Nov 11, 2003
 *
 * To change the template for this generated file go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
package edu.umd.cfar.lamp.viper.gui.table;

import edu.umd.cfar.lamp.viper.gui.core.*;

/**
 * @author davidm
 *
 * To change the template for this generated type comment go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
public interface ViperTableTabComponent extends HasMediator {
	public abstract void setMediator(ViperViewMediator mediator);
	public abstract void redoSelectionModel();
	public abstract void redoDataModel();
	public abstract void redoPropagateModel();
}