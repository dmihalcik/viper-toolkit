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

package edu.umd.cfar.lamp.viper.gui.table;

import javax.swing.*;

import viper.api.*;
import edu.umd.cfar.lamp.viper.gui.core.*;

/**
 * Java AWT Component beans that are used as cell editors
 * should implement this interface instead of using
 * the slower, reflection-driven table:attrProperty method. 
 * 
 * @author davidm@cfar.umd.edu
 * @since Jun 5, 2003
 */
public interface DataEditor extends CellEditor {
	public void setNode(Node a);
	public void setMediator (ViperViewMediator mediator);
}
