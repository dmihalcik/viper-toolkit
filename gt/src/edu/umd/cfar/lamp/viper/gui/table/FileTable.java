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

import java.awt.event.*;
import java.util.*;

import javax.swing.*;
import javax.swing.table.*;

import viper.api.*;
import edu.umd.cfar.lamp.apploader.misc.*;
import edu.umd.cfar.lamp.viper.gui.core.*;
import edu.umd.cfar.lamp.viper.util.*;

public class FileTable extends AbstractViperTable {
	protected void maybeShowPopup(MouseEvent e) {
	}

	public class FileDataModel extends ViperTableModel {
		public FileDataModel(Config cfg) {
			super(cfg);
		}
		public ViperViewMediator getMediator() {
			return FileTable.this.getMediator();
		}
	}

	public FileTable(TablePanel tp) {
		super(tp);
	}

	public void setConfig(Config cfg) {
		ViperTableModel mod = new FileDataModel(cfg);
		mod.setDisplayOfColumn(ViperTableModel.BY_ID, false);
		mod.setDisplayOfColumn(ViperTableModel.BY_VALID, false);
		mod.setDisplayOfColumn(ViperTableModel.BY_PROPAGATING, false);
		super.setCurrentModel(mod);
	}

	protected EnhancedTable getTable() {
		JScrollPane scrollPane = (JScrollPane) this.getComponent(0);
		return (EnhancedTable) scrollPane.getViewport().getView();
	}

	public Descriptor getSelectedRow() {
		return getCurrentModel().getDescriptorAtRow(
			getTable().getSelectedRow());
	}

	public void redoDataModel() {
		ViperTableModel m = getCurrentModel();
		if (m.resetDescs()) {
			getTable().resizeAllColumnsToNaturalWidth();
		} 
		m.fireTableStructureChanged();
		redoSelectionModel();
	}

	public void redoSelectionModel() {
		int[] oldRows = getTable().getSelectedRows();
		int[] oldCols = getTable().getSelectedColumns();
		if (oldRows.length > 0 || oldCols.length > 0) {
			getTable().clearSelection();
		}
		if (getMediator() == null) {
			return;
		}
		ViperSelectionSet selection = getMediator().getSelection();
		Config currCfg = getCurrentModel().getConfig();
		Iterator iter = selection.getSelectionBeneath(currCfg);
		while (iter.hasNext()) {
			Node n = (Node) iter.next();
			Descriptor d = null;
			Attribute a = null;
			if (n instanceof Descriptor) {
				d = (Descriptor) n;
			} else if (n instanceof Attribute) {
				a = (Attribute) n;
				d = a.getDescriptor();
			} else {
				continue;
			}
			int newRow = getCurrentModel().getRowForDescriptor(d);
			int newCol = (a != null) ? getCurrentModel().getColumnForAttribute(
					a) : 0;
			if (d != null) {
				getTable().addRowSelectionInterval(newRow, newRow);
			}
			if (a != null) {
				getTable().addColumnSelectionInterval(newCol, newCol);
				scrollToAttribute(a);
			}
		}
	}

	public void setMediator(ViperViewMediator mediator) {
		super.setMediator(mediator);
		boolean none = true;
		if (mediator != null) {
			Iterator cfgs = mediator.getViperData().getConfigsOfType(Config.FILE);
			if (cfgs.hasNext()) {
				Config curr = (Config) cfgs.next();
				this.setConfig(curr);
				none = false;
			}
		}
		if (none && this.getCurrentModel() != null) {
			// No file descriptor exists for this file
			this.getTable().setModel(new DefaultTableModel());
		} else if (!none) {
			getCurrentModel().fireTableStructureChanged();
		}
	}

	public Config getConfig() {
		return super.getCurrentModel().getConfig();
	}
}
