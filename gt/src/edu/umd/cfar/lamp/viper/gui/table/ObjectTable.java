/*******************************************************************************
 * ViPER * The Video Processing * Evaluation Resource * * Distributed under the
 * GPL license * Terms available at gnu.org. * * Copyright University of
 * Maryland, * College Park. *
 ******************************************************************************/

package edu.umd.cfar.lamp.viper.gui.table;

import java.awt.event.*;

import viper.api.*;
import edu.umd.cfar.lamp.apploader.prefs.*;
import edu.umd.cfar.lamp.viper.gui.core.*;
import edu.umd.cfar.lamp.viper.util.*;

public class ObjectTable extends AbstractViperTable {
	public class ObjectDataModel extends ViperTableModel {
		public ObjectDataModel(Config cfg) {
			super(cfg);
		}
		public ViperViewMediator getMediator() {
			return ObjectTable.this.getMediator();
		}
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
		ViperSubTree selection = getMediator().getPrimarySelection();
		Config currCfg = getCurrentModel().getConfig();
		if (currCfg == selection.getFirstConfig()) {
			Descriptor d = selection.getFirstDescriptor();
			Attribute a = selection.getFirstAttribute();
			int newRow = getCurrentModel().getRowForDescriptor(d);
			int newCol = (a != null) ? getCurrentModel().getColumnForAttribute(
					a) : 0;
			if (d != null && newRow > -1) {
				getTable().addRowSelectionInterval(newRow, newRow);
			}
			if (a != null && newCol > -1) {
//				getTable().addColumnSelectionInterval(newCol, newCol);
				ObjectTable.this.scrollToAttribute(a);
			}
		}
	}

	public ObjectTable(TablePanel tp) {
		super(tp);
	}
	protected void maybeShowPopup(MouseEvent e) {
		if (e.isPopupTrigger()) {
			popup.show(e.getComponent(), e.getX(), e.getY());
		}
	}

	public void setConfig(Config cfg) {
		setCurrentModel(new ObjectDataModel(cfg));
	}

	public Config getConfig() {
		if (super.getCurrentModel() != null) {
			return super.getCurrentModel().getConfig();
		}
		return null;
	}

	public Descriptor getSelectedRow() {
		return getCurrentModel()
				.getDescriptorAtRow(getTable().getSelectedRow());
	}

	public PrefsManager getPrefs() {
		ViperViewMediator m = getMediator();
		return (null == m) ? null : m.getPrefs();
	}

	public void redoDataModel() {
		ObjectDataModel m = (ObjectDataModel) getCurrentModel();
		if (m != null) {
			if (m.resetDescs()) {
				getTable().resizeAllColumnsToNaturalWidth();
			}
			m.fireTableDataChanged();
			redoSelectionModel();
		}
	}

	public void setMediator(ViperViewMediator mediator) {
		super.setMediator(mediator);
		redoDataModel();
	}
}