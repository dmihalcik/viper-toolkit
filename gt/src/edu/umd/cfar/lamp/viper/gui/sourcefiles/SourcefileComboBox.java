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


package edu.umd.cfar.lamp.viper.gui.sourcefiles;

import java.awt.event.*;
import java.io.*;
import java.net.*;
import java.util.logging.*;

import javax.swing.*;

import viper.api.*;
import edu.umd.cfar.lamp.viper.gui.core.*;

/**
 * A combo box control for displaying a list of available source
 * files.
 */
public class SourcefileComboBox extends JComboBox {
	private ViperViewMediator mediator;
	private static Logger logger = Logger.getLogger("edu.umd.cfar.lamp.viper.gui.sourcefiles");

	private Action addAction;
	private Action removeAction;
	private Action relinkAction;

	private ViperMediatorChangeListener vmcl;
	
	private void resetEnabledActions() {
		boolean enableAdd = false;
		boolean enableRemove = false;
		boolean enableSwap = false;
		if (mediator != null) {
			enableAdd = true;
			if (mediator.getCurrFile() != null) {
				enableRemove = true;
				enableSwap = true;
			}
		}
		addAction.setEnabled(enableAdd);
		removeAction.setEnabled(enableRemove);
		relinkAction.setEnabled(enableSwap);
	}

	public SourcefileComboBox() {
		super();
		super.setModel(new SourcefileComboModel());
		vmcl = new ListensForFileChanges();

		addAction = new AddNewSourcefileAction();
		removeAction = new DeleteSourcefileAction();
		relinkAction = new RelinkSourcefileAction();
		resetEnabledActions();
	}
	
	public SourcefileComboModel getSourcefileComboModel() {
		return (SourcefileComboModel) getModel();
	}
	
	/**
	 * @return
	 */
	public ViperViewMediator getMediator() {
		return mediator;
	}

	/**
	 * @param mediator
	 */
	public void setMediator(ViperViewMediator mediator) {
		if (this.mediator != null) {
			this.mediator.removeViperMediatorChangeListener(vmcl);
		}
		this.mediator = mediator;
		resetEnabledActions();
		if (this.mediator != null) {
			this.mediator.addViperMediatorChangeListener(vmcl);
		}
	}

	private class AddNewSourcefileAction extends AbstractAction {
		public AddNewSourcefileAction () {
			super(" + ");
		}
		public void actionPerformed(ActionEvent e) {
			getMediator().addNewSourcefile(null);
		}
	}

	private class ListensForFileChanges
		implements ViperMediatorChangeListener {
		public void schemaChanged(ViperMediatorChangeEvent e) {
			getSourcefileComboModel().resetContents();
			resetEnabledActions();
		}
		public void frameChanged(ViperMediatorChangeEvent e) {
		}

		/**
		 * The chosen file has changed.
		 * @param e the change event
		 */
		public void currFileChanged(ViperMediatorChangeEvent e) {
			SourcefileComboModel model = getSourcefileComboModel();
			model.fireContentsChanged(model, -1, -1);
			resetEnabledActions();
		}

		public void dataChanged(ViperMediatorChangeEvent e) {
			SourcefileComboModel model = getSourcefileComboModel();
			model.resetContents();
			resetEnabledActions();
		}

		public void mediaChanged(ViperMediatorChangeEvent e) {
		}
	}

	private class SourcefileComboModel
		extends AbstractListModel
		implements ComboBoxModel {
		private int oldSize = 0;
		private Sourcefile curr() {
			return mediator.getCurrFile();
		}

		/**
		 * 
		 */
		public void resetContents() {
			if (oldSize != getSize()) {
				if (oldSize > 0) {
					fireIntervalRemoved(this, 0, oldSize);
				}
				oldSize = getSize();
				if (oldSize > 0) {
					fireIntervalAdded(this, 0, oldSize);
				}
			} else {
				fireContentsChanged(this, 0, oldSize);
			}
		}

		public void setSelectedItem(Object anItem) {
			if (anItem == null) {
				return;
			}
			try {
				URI uri = new URI((String) anItem);
				String cName = curr() == null ? null : curr().getReferenceMedia().getSourcefileName();
				if (mediator != null && (cName == null || !cName.equals(anItem))) {
					mediator.setFocalFile(uri);
				}
			} catch (IOException e) {
				logger.severe(e.getMessage());
			} catch (URISyntaxException e) {
				logger.severe(e.getMessage());
				assert false;
			}
		}
		public Object getSelectedItem() {
			if (mediator == null) {
				return null;
			} else if (curr() == null) {
				return null;
			} else {
				return curr().getReferenceMedia().getSourcefileName();
			}
		}
		public int getSize() {
			if (mediator == null) {
				return 0;
			} else {
				return mediator.getViperData().getAllSourcefiles().size();
			}
		}
		public Object getElementAt(int index) {
			if (mediator == null) {
				return null;
			} else {
				Sourcefile sf =
					(Sourcefile) mediator
						.getViperData()
						.getAllSourcefiles()
						.get(
						index);
				String sfName = sf.getReferenceMedia().getSourcefileName();
				return sfName;
			}
		}

		public void fireContentsChanged(
			Object source,
			int index0,
			int index1) {
			super.fireContentsChanged(source, index0, index1);
		}
	}

	private class DeleteSourcefileAction extends AbstractAction {
		public DeleteSourcefileAction () {
			super(" - ");
		}
		public void actionPerformed(ActionEvent e) {
			ViperData v = mediator.getViperData();
			Sourcefiles parent = v.getSourcefilesNode();
			Sourcefile toRemove = mediator.getCurrFile();
			if (toRemove != null) {
				parent.removeChild(toRemove);
			}
		}
	}
	
	private class RelinkSourcefileAction extends AbstractAction {
		public RelinkSourcefileAction () {
			super(" \u2261 ");
		}
		public void actionPerformed(ActionEvent e) {
			ViperData v = mediator.getViperData();
			Sourcefiles parent = v.getSourcefilesNode();
			Sourcefile toLink = mediator.getCurrFile();
			if (toLink != null) {
				JFileChooser chooser = new JFileChooser();
				File d = getMediator().getLastDirectory();
				if (d != null) {
					chooser.setCurrentDirectory(d);
				}
				MediaElement info = toLink.getReferenceMedia();
				URI canon = info.getSourcefileIdentifier();
				chooser.setDialogTitle("Substitute for " + canon);
				int returnVal = chooser.showOpenDialog(SourcefileComboBox.this);
				if(returnVal == JFileChooser.APPROVE_OPTION) {
					File foundFile = chooser.getSelectedFile();
					URI local = foundFile.toURI();
					getMediator().openCanonicalFileAsLocalFile(canon, local, info);
				}
			}
		}
	}
	
	public Action getAddAction() {
		return addAction;
	}
	public Action getRelinkAction() {
		return relinkAction;
	}
	public Action getRemoveAction() {
		return removeAction;
	}
}
