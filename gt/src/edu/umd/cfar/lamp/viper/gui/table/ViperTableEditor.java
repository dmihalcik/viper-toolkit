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

import java.util.logging.*;

import javax.swing.*;

/**
 * Basic viper table holder. It is a JPanel that contains
 * a scrollable table and some manipulation buttons (outside
 * the scroll pane), like 'Delete Row'. The table inside
 * must be a child of {@link AbstractViperTable}, which isn't
 * actually a JTable, as it may have multiple tables (in
 * the form of the Content pane).
 * 
 * @author davidm@cfar.umd.edu
 */
public class ViperTableEditor extends JPanel {
	private Logger logger = Logger.getLogger("edu.umd.cfar.lamp.viper.gui.table");

	private AbstractViperTable table;

	public ViperTableEditor() {
		add(new JScrollPane());
	}
	
	
	/**
	 * @return
	 */
	public Logger getLogger() {
		return logger;
	}

	/**
	 * @return
	 */
	public AbstractViperTable getTable() {
		return table;
	}

	/**
	 * @param logger
	 */
	public void setLogger(Logger logger) {
		this.logger = logger;
	}

	/**
	 * @param table
	 */
	public void setTable(AbstractViperTable table) {
		this.table = table;
		((JScrollPane) this.getComponent(0)).setViewportView(table);
	}
}
