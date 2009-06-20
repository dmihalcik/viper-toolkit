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

import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.net.*;
import java.util.*;
import java.util.List;
import java.util.logging.*;

import javax.imageio.*;
import javax.swing.*;
import javax.swing.event.*;
import javax.swing.table.*;

import viper.api.*;
import viper.api.extensions.*;
import viper.api.time.*;
import edu.umd.cfar.lamp.apploader.misc.*;
import edu.umd.cfar.lamp.viper.gui.core.*;
import edu.umd.cfar.lamp.viper.gui.data.*;
import edu.umd.cfar.lamp.viper.gui.table.*;

/**
 * Edits the source files associated with a given set of ViPER metadata. These
 * are usually associated together for a project or because they share a common
 * schema.
 */
public class SourcefileEditor extends JComponent implements HasMediator {
	private static final String REMOVE_SELECTED_FILE_BUTTON_TEXT = "Remove Selected File";
	private static final String REMOVE_SELECTED_FILES_BUTTON_TEXT = "Remove Selected Files";
	private static final String ADD_NEW_FILE_BUTTON_TEXT = "Add New File";
	private ViperViewMediator mediator;
	private Logger logger = Logger
			.getLogger("edu.umd.cfar.lamp.viper.gui.sourcefiles");
	

	public ViperData getViperData() {
		if (null == mediator) {
			return null;
		} else {
			return mediator.getViperData();
		}
	}
	private EnhancedTable getTable() {
		JScrollPane scrollPane = (JScrollPane) this.getComponent(0);
		return (EnhancedTable) scrollPane.getViewport().getView();
	}

	private SourcefileTableModel model;

	private static final int BY_URI = -1;
	public static final Object REMOVE_CURRENT_FILE_BUTTON_TEXT = "Remove Current Media File";
	private final class DeleteSelectedSourcefilesAction extends AbstractAction {
		public DeleteSelectedSourcefilesAction() {
			super();
			putValue(Action.NAME, SourcefileEditor.REMOVE_SELECTED_FILE_BUTTON_TEXT);
		}
		public void actionPerformed(ActionEvent e) {
			int[] rows = getTable().getSelectedRows();
			if (rows == null || rows.length == 0) 
				return;
			List sfs = new LinkedList();
			for (int i = 0; i < rows.length; i++) {
				sfs.add(model.getSourcfileByRow(rows[i]));
			}
			Sourcefiles parent = getViperData().getSourcefilesNode();
			for (Iterator si = sfs.iterator(); si.hasNext(); ) {
				Sourcefile sf = (Sourcefile) si.next();
				logger.fine("Removing file "
						+ sf.getReferenceMedia().getSourcefileName());
				parent.removeChild(sf);
			}
		}
	}

	private class DeleteCurrentSourcefileAction extends AbstractAction {
		public DeleteCurrentSourcefileAction() {
			super();
			putValue(Action.NAME, SourcefileEditor.REMOVE_CURRENT_FILE_BUTTON_TEXT);
		}
		public void actionPerformed(ActionEvent e) {
			Sourcefile sf = mediator.getCurrFile();
			Sourcefiles parent = getViperData().getSourcefilesNode();
			parent.removeChild(sf);
		}
	}

	private class SourcefileTableModel extends AbstractTableModel {
		public boolean isCellEditable(int rowIndex, int columnIndex) {
			if (columnIndex == 0) {
				return false;
			}
			return true;
		}
		private class TableSorter implements Comparator {
			public int compare(Object o1, Object o2) {
				if (o1.equals(o2)) {
					return 0;
				}
				CanonicalSourcefile a = (CanonicalSourcefile) o1;
				CanonicalSourcefile b = (CanonicalSourcefile) o2;
				Iterator iter = sortOrder.keySet().iterator();
				Integer[] order = new Integer[sortOrder.size()];
				int countDown = order.length;
				while (iter.hasNext()) {
					order[--countDown] = (Integer) iter.next();
				}
				for (int i = 0; i < order.length; i++) {
					Integer p = order[i];
					Boolean ascending = (Boolean) sortOrder.get(p);
					int c = compareBy(a, b, p.intValue(), ascending
							.booleanValue());
					if (c != 0) {
						return c;
					}
				}
				return 0;
			}
			public int compareBy(CanonicalSourcefile a, CanonicalSourcefile b, int by,
					boolean ascending) {
				int score = 0;
				Instant now = getMediator().getMajorMoment();
				switch (by) {
					case BY_URI :
						String aName = a.getReferenceMedia()
								.getSourcefileName();
						String bName = b.getReferenceMedia()
								.getSourcefileName();
						score = aName.compareTo(bName);
						break;
					default :
						// sort by attribute
						Attribute aAt = (Attribute) a.getChild(by);
						Object aVal = aAt.getAttrValueAtInstant(now);
						Attribute bAt = (Attribute) b.getChild(by);
						Object bVal = bAt.getAttrValueAtInstant(now);
						String type = aAt.getAttrConfig().getAttrType();
						if (aVal == bVal) {
							score = 0;
						} else if (aVal == null) {
							score = 1;
						} else if (bVal == null) {
							score = -1;
						} else if (ViperSorters.getCmpFor(type) != null) {
							score = ViperSorters.getCmpFor(type).compare(aVal,
									bVal);
						} else if (aVal instanceof Comparable) {
							Comparable aCmp = (Comparable) aVal;
							score = aCmp.compareTo(bVal);
						} else {
							score = 0;
						}
				}
				return ascending ? score : -score;
			}
		}
		private LinkedHashMap sortOrder;
		private List descs;
		private Comparator sort = new TableSorter();
		public SourcefileTableModel() {
			this.sortOrder = new LinkedHashMap();
			this.sortOrder.put(new Integer(BY_URI), Boolean.TRUE);
			this.descs = new ArrayList();
			this.reset();
			this.resortDescs();
		}
		public int getRowCount() {
			ViperData v = getViperData();
			return null == v ? 0 : v.getSourcefilesNode().getNumberOfChildren();
		}
		public int getColumnCount() {
			int count = 1;
			if (getMediator() != null) {
				Iterator iter = getMediator().getViperData().getConfigsOfType(
						Config.FILE);
				if (iter.hasNext()) {
					Config c = (Config) iter.next();
					count += c.getNumberOfChildren();
				}
			}
			return count;
		}
		public String getColumnName(int c) {
			if (c == 0) {
				return "File Name";
			} else {
				Iterator iter = getMediator().getViperData().getConfigsOfType(
						Config.FILE);
				if (iter.hasNext()) {
					Config cfg = (Config) iter.next();
					AttrConfig ac = (AttrConfig) cfg.getChild(c - 1);
					return ac.getAttrName();
				}
				throw new IllegalArgumentException("Not a valid column: " + c);
			}
		}
		public Class getColumnClass(int c) {
			if (c == 0) {
				return Sourcefile.class;
			} else {
				return Attribute.class;
			}
		}
		public Object getValueAt(int row, int col) {
			CanonicalSourcefile s = getSourcfileByRow(row);
			if (col == 0) {
				return s;
			} else {
				Descriptor d = s.getCanonicalFileDescriptor().getDescriptor();
				return d.getChild(col - 1);
			}
		}
		public CanonicalSourcefile getSourcfileByRow(int row) {
			ViperData v = getViperData();
			return null == v ? null : (CanonicalSourcefile) v.getSourcefilesNode()
					.getChild(row);
		}
		/**
		 * Makes sure that the currently displayed list of descriptors is in
		 * sync with the background. This is like telling Windows Explorer to
		 * 'Refresh' (F5). Right now, this must be called manually, for all
		 * changes to what should be displayed.
		 * 
		 * @return <code>true</code> if the table should reset its column
		 *         widths
		 */
		public boolean reset() {
			ViperData v = getViperData();
			if (v == null) {
				// No sourcefile selected; display empty
				if (this.descs.isEmpty()) {
					return true;
				}
				this.descs.clear();
			} else {
				// switched sourcefile; reload all descriptors
				this.descs.clear();
				Iterator iter = getMediator().getViperData().getSourcefiles();
				while (iter.hasNext()) {
					Object o = iter.next();
					descs.add(o);
				}
				resortDescs();
			}
			return false;
		}
		public void resortDescs() {
			Collections.sort(this.descs, sort);
		}
	}
	public SourcefileEditor() {
		this(5);
	}

	public SourcefileEditor(int lead) {
		SpringLayout layout = new SpringLayout();
		setLayout(layout);
		SpringLayout.Constraints pCons = layout.getConstraints(this);

		model = new SourcefileTableModel();
		EnhancedTable table = new EnhancedTable(model);
		table
				.setDefaultRenderer(Sourcefile.class,
						new SourcefileCellRenderer());
		table.setDefaultRenderer(Attribute.class, new AttributeRenderer(this));
		table.setDefaultEditor(Attribute.class, new AttributeEditor(this));
		table.addTableListener(new TableListener() {
			public void contextClick(TableEvent e) {
			}
			public void actionClick(TableEvent e) {
				ViperData v = getViperData();
				if (null != v) {
					Sourcefiles sfs = v.getSourcefilesNode();
					if (e.getRow() < 0) {
						// TODO sort command for sourcefile list

					} else if (e.getRow() < sfs.getNumberOfChildren()) {
						Sourcefile sf = (Sourcefile) sfs.getChild(e.getRow());
						try {
							mediator.setFocalFile(new URI(sf
									.getReferenceMedia().getSourcefileName()));
						} catch (IOException e1) {
							logger.severe("Error while trying to load: "
									+ sf.getReferenceMedia()
											.getSourcefileName());
							logger.severe(e1.getLocalizedMessage());
						} catch (URISyntaxException et) {
							logger.severe("Error in file: invalid URI: "
									+ sf.getReferenceMedia()
											.getSourcefileName());
							logger.severe(et.getLocalizedMessage());
						}
					}
				}
			}
			public void click(TableEvent e) {
				//				int oldRow = getTable().getSelectedRow();
				//				if (0 <= oldRow) {
				//					getTable().removeRowSelectionInterval(oldRow, oldRow);
				//					System.err.println ("Removed selection from " + oldRow);
				//				}
				//				if (e.getRow() != oldRow) {
				//					getTable().addRowSelectionInterval(e.getRow(), e.getRow());
				//					System.err.println ("Added selection to " + e.getRow());
				//				}
			}
			public void altClick(TableEvent e) {
			}
		});
		table.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
//		table.setRowSelectionAllowed(true);
		table.setColumnSelectionAllowed(false);
		table.setSelectionBackground(Color.blue);
		table.getSelectionModel().addListSelectionListener(new ListSelectionListener() {
			public void valueChanged(ListSelectionEvent e) {
				resetButtonAndActionEnablings();
			}
		});

		JScrollPane sp = new JScrollPane(table);
		SpringLayout.Constraints spCons = layout.getConstraints(sp);
		super.add(sp);

		JButton addNewFileButton = new JButton(addSourceAction);
		super.add(addNewFileButton);

		JButton deleteFileButton = new JButton(deleteSelectedSourcefilesAction);
		super.add(deleteFileButton);

		Spring leading = Spring.constant(lead);
		Spring x_end = leading;

		Spring x = leading;
		Spring y = leading;

		spCons.setX(x);
		spCons.setY(y);
		y = spCons.getConstraint(SpringLayout.SOUTH);
		x_end = Spring.max(x_end, spCons.getConstraint(SpringLayout.EAST));

		SpringLayout.Constraints anfCons = layout.getConstraints(addNewFileButton);
		anfCons.setX(x);
		anfCons.setY(Spring.sum(leading, y));

		SpringLayout.Constraints dfCons = layout.getConstraints(deleteFileButton);
		x = Spring.sum(anfCons.getConstraint(SpringLayout.EAST), Spring
				.constant(lead));
		dfCons.setX(x);
		dfCons.setY(anfCons.getConstraint(SpringLayout.NORTH));
		y = anfCons.getConstraint(SpringLayout.SOUTH);
		x_end = Spring.max(x_end, dfCons.getConstraint(SpringLayout.EAST));

		pCons.setConstraint(SpringLayout.SOUTH, Spring.sum(leading, y));
		pCons.setConstraint(SpringLayout.EAST, Spring.sum(x_end, Spring
				.constant(lead)));
		resetButtonAndActionEnablings();
	}

	public Action getAddNewSourcefileAction() {
		return addSourceAction;
	}
	public Action getDeleteCurrentSourcefileAction() {
		return deleteCurrentSourcefileActionListener;
	}
	public Action getDeleteSelectedSourcefilesAction() {
		return deleteSelectedSourcefilesAction;
	}

	private Action deleteSelectedSourcefilesAction = new DeleteSelectedSourcefilesAction();

	private Action deleteCurrentSourcefileActionListener = new DeleteCurrentSourcefileAction();

	private AddNewSourcefileAction addSourceAction = new AddNewSourcefileAction();
	private class AddNewSourcefileAction extends AbstractAction {
		public AddNewSourcefileAction() {
			super();
			putValue(Action.NAME, SourcefileEditor.ADD_NEW_FILE_BUTTON_TEXT);
		}
		public void actionPerformed(ActionEvent e) {
			getMediator().addNewSourcefile(null);
		}
	}

	private AddDirectoryAsInfoAction addDirectoryAsInfoFileAction = new AddDirectoryAsInfoAction();
	public static final Object ADD_DIRECTORY_AS_INFO_BUTTON_TEXT = "Add Directory Contents as Single File";

	/**
	 * Creates an info file using all of the valid image files in the 
	 * directory, in the order in which they are found. It might be a good idea to 
	 * make a .info 	editor widget that allows a user to modify the order and 
	 * add/remove individual files from the playlist.
	 * @param directory
	 * @param targetInfoFilename
	 * @return
	 * @throws IOException
	 */
	private static File createInfoForDirectory(File directory, File targetInfoFilename) throws IOException {
		if (!directory.isDirectory()) {
			throw new IllegalArgumentException("Must select a directory");
		}
		if (targetInfoFilename == null) {
			targetInfoFilename = new File(directory, directory.getName() + ".info");
		}
		File[] contents = directory.listFiles();
		PrintWriter output = new PrintWriter(new FileWriter(targetInfoFilename));
		try {
			output.println("#VIPER_VERSION_3.0");
			output.println("1");
			for (int i = 0; i < contents.length; i++) {
				if (contents[i].isFile()) {
					String s = contents[i].getName();
					int l = s.lastIndexOf(".");
					if (l >= 0) {
						String suffix = s.substring(l+1);
						if (ImageIO.getImageReadersBySuffix(suffix).hasNext()) {
							output.println(s);
						} else if ("bmp".equalsIgnoreCase(suffix)) {
							output.println(s);
						}
					}
				}
			}
		} finally {
			output.close();
		}
		return targetInfoFilename;
	}
	
	private class AddDirectoryAsInfoAction extends AbstractAction {
		public AddDirectoryAsInfoAction() {
			super();
			putValue(Action.NAME, SourcefileEditor.ADD_DIRECTORY_AS_INFO_BUTTON_TEXT);
		}
		public void actionPerformed(ActionEvent e) {
			ViperData v = getViperData();
			if (null != v) {
				JFileChooser dialog = new JFileChooser();
				if (getMediator().getLastDirectory()!=null) {
					dialog.setCurrentDirectory(getMediator().getLastDirectory());
				}
				dialog.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
				dialog.setApproveButtonMnemonic('a');
				dialog.setName("Select a Directory of Image Files to Add...");
				int returnValue = dialog.showDialog(null, "Add Folder");
				switch (returnValue) {
					case JFileChooser.APPROVE_OPTION :
						File f = dialog.getSelectedFile();
						getMediator().setLastDirectory(f.getParentFile());
						try {
							f = createInfoForDirectory(f, null);
						} catch (IOException e1) {
							logger.log(Level.SEVERE, "Error while creating info file for directory", e1);
							break;
						}
						URI fname = f.toURI();
						if (null == v.getSourcefile(fname.toString())) {
							logger.fine("Creating sourcefile for " + fname);
							v.createSourcefile(fname.toString());
						}
						break;
				}

			}
		}
	}

	/**
	 * @return ViperViewMediator
	 */
	public ViperViewMediator getMediator() {
		return mediator;
	}

	/**
	 * Sets the mediator.
	 * 
	 * @param mediator
	 *            The mediator to set
	 */
	public void setMediator(ViperViewMediator mediator) {
		if (this.mediator != null) {
			this.mediator.removeViperMediatorChangeListener(vmcl);
		}
		this.mediator = mediator;
		model.reset();
		model.fireTableStructureChanged();
		resetButtonAndActionEnablings();
		if (this.mediator != null) {
			this.mediator.addViperMediatorChangeListener(vmcl);
		}
	}

	private ListensForFileChanges vmcl = new ListensForFileChanges();
	
	private void resetButtonAndActionEnablings() {
		if (this.mediator == null) {
			addSourceAction.setEnabled(false);
			deleteCurrentSourcefileActionListener.setEnabled(false);
		} else {
			addSourceAction.setEnabled(true);
			deleteCurrentSourcefileActionListener.setEnabled(mediator.getViperData().getSourcefilesNode().getNumberOfChildren() > 0);
			int rowSelectCount = getTable().getSelectedRowCount();
			if (rowSelectCount > 1) {
				deleteSelectedSourcefilesAction.setEnabled(true);
				deleteSelectedSourcefilesAction.putValue(Action.NAME, SourcefileEditor.REMOVE_SELECTED_FILES_BUTTON_TEXT);
			} else if (rowSelectCount < 1) {
				deleteSelectedSourcefilesAction.setEnabled(false);
				deleteSelectedSourcefilesAction.putValue(Action.NAME, SourcefileEditor.REMOVE_SELECTED_FILE_BUTTON_TEXT);
			} else {
				deleteSelectedSourcefilesAction.setEnabled(true);
				deleteSelectedSourcefilesAction.putValue(Action.NAME, SourcefileEditor.REMOVE_SELECTED_FILE_BUTTON_TEXT);
			}
		}
	}
	
	private class ListensForFileChanges implements ViperMediatorChangeListener {
		public void schemaChanged(ViperMediatorChangeEvent e) {
			model.fireTableStructureChanged();
			resetButtonAndActionEnablings();
		}
		public void frameChanged(ViperMediatorChangeEvent e) {
		}

		/**
		 * The chosen file has changed.
		 * 
		 * @param e the change event
		 */
		public void currFileChanged(ViperMediatorChangeEvent e) {
		}

		public void dataChanged(ViperMediatorChangeEvent e) {
			if (!model.reset()) {
				model.fireTableDataChanged();
				resetButtonAndActionEnablings();
			}
		}

		public void mediaChanged(ViperMediatorChangeEvent e) {
		}
	}

	private class SourcefileCellRenderer extends DefaultTableCellRenderer {
		public Component getTableCellRendererComponent(JTable table,
				Object value, boolean isSelected, boolean hasFocus, int row,
				int column) {
			String s = "NULL";
			if (null != table) {
				s = ((Sourcefile) value).getReferenceMedia().getSourcefileName();
			}
			return super.getTableCellRendererComponent(table, s, isSelected, hasFocus, row, column);
		}
	}
	/**
	 * Pops up a dialog asking the user to add a directory for image files.
	 * Creates a .info file in the directory listing its contents.
	 * @return Returns an action listener which adds a directory as an info file
	 */
	public Action getAddDirectoryAsInfoFileAction() {
		return addDirectoryAsInfoFileAction;
	}
}