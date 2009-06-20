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

import java.awt.*;
import java.awt.Component;
import java.awt.event.*;
import java.util.*;
import java.util.logging.*;

import javax.swing.*;
import javax.swing.event.*;
import javax.swing.table.*;

import viper.api.*;
import viper.api.time.*;
import edu.umd.cfar.lamp.apploader.misc.*;
import edu.umd.cfar.lamp.chronicle.*;
import edu.umd.cfar.lamp.chronicle.markers.*;
import edu.umd.cfar.lamp.viper.examples.textline.*;
import edu.umd.cfar.lamp.viper.geometry.*;
import edu.umd.cfar.lamp.viper.gui.core.*;

/**
 * @author davidm@cfar.umd.edu
 * @since Jun 4, 2003
 */
public abstract class AbstractViperTable extends JPanel
		implements
			ViperTableTabComponent {
	private Logger logger = Logger
			.getLogger("edu.umd.cfar.lamp.viper.gui.table");
	private ViperViewMediator mediator;
	public abstract Descriptor getSelectedRow();
	protected JPopupMenu popup;
	private AttributeRenderer ar;
	private AttributeEditor ae;
	private TablePanel outerTablePanel;

	/**
	 * Get the model of the currently selected table (since a vipertable may
	 * have more than one table model, like the content pane).
	 * 
	 * @return the table model that has the user focus
	 */
	public ViperTableModel getCurrentModel() {
		TableModel mod = getTable() == null ? null : getTable().getModel();
		if (mod instanceof ViperTableModel) {
			return (ViperTableModel) getTable().getModel();
		} else {
			return null;
		}
	}
	public void setCurrentModel(ViperTableModel model) {
		getTable().setModel(model);
	}
	
	private ChangeListener hiddenNodesChangeListener = new ChangeListener() {
		public void stateChanged(ChangeEvent e) {
			AbstractViperTable.this.getTable().getTableHeader().repaint();
		}};

	private class ProxyTableCellRenderer  implements TableCellRenderer {
		private TableCellRenderer candidate;
		
		public ProxyTableCellRenderer (TableCellRenderer delegate) {
			this.candidate = delegate;
		}
		
		/** @inheritDoc */
		public boolean equals(Object arg0) {
			return candidate.equals(arg0);
		}
		/** @inheritDoc */
		public Component getTableCellRendererComponent(JTable table,  Object value,  boolean isSelected,  boolean hasFocus,  int row, int column) {
			Component c = candidate.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
			if (c instanceof JLabel && table != null) {
				ViperTableModel m = getCurrentModel();
				int modelIndex = table.convertColumnIndexToModel(column);
				AttrConfig ac = m.getAttributeForColumn(modelIndex);
				JLabel l = (JLabel) c;
				if (ac != null) {
					int visibility = mediator.getHiders().getAttrConfigVisibility(ac);
					l.setIcon(outerTablePanel.visibilityIcons[visibility]);
				} else if (m.getInternalColumn(modelIndex) == ViperTableModel.BY_VALID) {
					Config config = m.getConfig();
					int visibility = mediator.getHiders().getConfigVisibility(m.getConfig());
					if (visibility == NodeVisibilityManager.RANGE_LOCKED) {
						visibility = NodeVisibilityManager.LOCKED;
					}
					l.setIcon(outerTablePanel.visibilityIcons[visibility]);
				} else {
					l.setIcon(null);
				}
			}
			return c;
		}
		/** @inheritDoc */
		public int hashCode() {
			return candidate.hashCode();
		}
		/** @inheritDoc */
		public String toString() {
			return candidate.toString();
		}
	}
	
	
	/**
	 * Adds the default renderers and editors for all known data types
	 * 
	 * @param table
	 */
	private void initAttributeTable(final EnhancedTable table) {
		ar = new AttributeRenderer(this);
		ae = new AttributeEditor(this);
		ae.setEditClickCount(2);
		TableCellRenderer r = table.getTableHeader().getDefaultRenderer();
		table.getTableHeader().setDefaultRenderer(new ProxyTableCellRenderer(r));
		table.setDefaultRenderer(Descriptor.class, ar);
		table.setDefaultRenderer(Attribute.class, ar);
		table.setDefaultEditor(Attribute.class, ae);
		table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		table.addTableListener(new TableListener() {
			public void contextClick(TableEvent e) {
				// TODO: Should display context menu offering: sort ascending/descending; show/hide/lock
			}
			public void actionClick(TableEvent e) {
			}
			public void click(TableEvent e) {
				if (e.getRow() == -1) {
					ViperTableModel m = getCurrentModel();
					int modelIndex = table.convertColumnIndexToModel(e.getColumn());
					AttrConfig ac = m.getAttributeForColumn(modelIndex);
					NodeVisibilityManager H = mediator.getHiders();
					if (ac != null) {
						int oldV = H.getAttrConfigVisibility(ac);
						H.setVisibilityByAttrConfig(ac, NodeVisibilityManager.ROTATE_VISIBILITY[oldV]);
					} else if (m.getInternalColumn(modelIndex) == ViperTableModel.BY_VALID) {
						Config config = m.getConfig();
						int oldV = H.getConfigVisibility(config);
						H.setVisibilityByConfig(config, NodeVisibilityManager.ROTATE_RANGE_VISIBILITY[oldV]);
					}
				}
			}
			public void altClick(TableEvent e) {
			}
		});
	}
	private int rowEditPolicy = ALLOW_ROW_EDIT;
	public int getRowEditPolicy() {
		return rowEditPolicy;
	}
	public void setRowEditPolicy(int policy) {
		rowEditPolicy = policy;
	}
	public static int NO_ROW_EDIT = 0;
	public static int ALLOW_ROW_EDIT = 1;
	// added by Ping on 10/31/2000
	// for toggle through objects
	public static boolean ENABLE = true;
	public static boolean DISABLE = false;
	// Handle some of the common steps between creating the content
	// and object tables.
	public AbstractViperTable(TablePanel tp) {
		super();
		this.outerTablePanel = tp;
		setLayout(new BorderLayout());
		EnhancedTable table = new EnhancedTable() {
			public void changeSelection(int rowIndex, int columnIndex,
					boolean toggle, boolean extend) {
				ViperTableModel currModel = AbstractViperTable.this
						.getCurrentModel();
				columnIndex = convertColumnIndexToModel(columnIndex);
				AttrConfig ac = currModel.getAttributeForColumn(columnIndex);
				Descriptor d = currModel.getDescriptorAtRow(rowIndex);
				Node n = null;
				if (ac != null) {
					Attribute a = d.getAttribute(ac);
					n = a;
				} else if (currModel.getInternalColumn(columnIndex) == ViperTableModel.BY_ID) {
					n = d;
				}
				if (n != null) {
					if (extend) {
						mediator.getSelection().addNode(n);
					} else {
						mediator.getSelection().setTo(n);
					}
				}
			}
			public boolean isCellSelected(int row, int column) {
				ViperTableModel currModel = AbstractViperTable.this
						.getCurrentModel();
				column = convertColumnIndexToModel(column);
				AttrConfig ac = currModel.getAttributeForColumn(column);
				Descriptor d = currModel.getDescriptorAtRow(row);
				if (ac != null) {
					Attribute a = d.getAttribute(ac);
					return mediator.getSelection().isSelected(a);
				} else if (currModel.getInternalColumn(column) == ViperTableModel.BY_ID) {
					return mediator.getSelection().isSelected(d);
				}
				return false;
			}
		};
		table.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
		table.resizeAllColumnsToNaturalWidth();
		table.setCellSelectionBackground(table.getSelectionBackground()
				.brighter().brighter().brighter());
		table.setCellSelectionForeground(table.getForeground().darker());
		initAttributeTable(table);
		JScrollPane scrollPane = new JScrollPane(table);
		this.add(scrollPane);
		popup = new DescPropPopup();
		popup.setInvoker(getTable());
		getTable().addMouseListener(new MouseAdapter() {
			public void mousePressed(MouseEvent e) {
				maybeShowPopup(e);
			}
			public void mouseReleased(MouseEvent e) {
				maybeShowPopup(e);
			}
		});
	}

	protected abstract void maybeShowPopup(MouseEvent e);

	protected EnhancedTable getTable() {
		JScrollPane scrollPane = (JScrollPane) this.getComponent(0);
		return (EnhancedTable) scrollPane.getViewport().getView();
	}
	public ViperViewMediator getMediator() {
		return mediator;
	}
	public void setMediator(ViperViewMediator mediator) {
		if (this.mediator != mediator) {
			if (this.mediator != null) {
				this.mediator.getHiders().removeChangeListener(hiddenNodesChangeListener);
			}
			this.mediator = mediator;
			if (this.mediator != null) {
				this.mediator.getHiders().addChangeListener(hiddenNodesChangeListener);
			}
		}
	}
	
	public void scrollToAttribute(Attribute a) {
		if (!a.getDescriptor().getConfig().equals(getConfig())) {
			logger
					.fine("Cannot scroll to attribute that isn't attached to this type of descriptor");
			return;
		}
		int rowIndex = getCurrentModel().getRowForDescriptor(a.getDescriptor());
		int colIndex = getCurrentModel().getColumnForAttribute(a);
		JScrollPane scrollPane = (JScrollPane) this.getComponent(0);
		JViewport viewport = (JViewport) scrollPane.getViewport();
		EnhancedTable table = (EnhancedTable) viewport.getView();

		// This rectangle is relative to the table where the
		// northwest corner of cell (0,0) is always (0,0).
		Rectangle rect = table.getCellRect(rowIndex, colIndex, true);

		// The location of the viewport relative to the table
		Point pt = viewport.getViewPosition();

		// Translate the cell location so that it is relative
		// to the view, assuming the northwest corner of the
		// view is (0,0)
		rect.setLocation(rect.x - pt.x, rect.y - pt.y);

		// Scroll the area into view
		viewport.scrollRectToVisible(rect);
	}

	// XXX Move to TablePanel - here there is one copy for each descriptor
	// config
	private class DescPropPopup extends JPopupMenu {
		private JCheckBoxMenuItem v;
		private JCheckBoxMenuItem p;
		private JMenuItem delete;
		private JMenuItem duplicate;
		private JMenuItem interp;
		private JCheckBoxMenuItem wrt;
		private JMenu interpToMark;
		private JMenuItem shift;
		private JMenu shiftToMark;
		private ShiftToMarkAction stmAction;
		private InterpToMarkAction itmAction;
		private Descriptor desc;
		private Attribute attr;
		private class WithRespectToAction implements ActionListener {
			public void actionPerformed(ActionEvent e) {
				if (attr == null) {
					return;
				}
				ViperViewMediator m = getMediator();
				Attribute oldWRT = m.getDisplayWRTManager().getAttribute();
				if (attr.equals(oldWRT)) {
					m.getDisplayWRTManager().setAttribute(null, null);
				} else {
					m.getDisplayWRTManager().setAttribute(attr,
							m.getCurrentFrame());
				}
			}
		}
		private class ValidAction implements ActionListener {
			public void actionPerformed(ActionEvent e) {
				boolean makeValid = v.isSelected();
				InstantRange oldRange = (InstantRange) desc.getValidRange()
						.clone();
				boolean frame = oldRange.isFrameBased();
				InstantInterval toAlter = getMediator().getCurrInterval(frame);
				if (!makeValid) {
					oldRange.remove(toAlter);
				} else {
					oldRange.add(toAlter);
				}
				desc.setValidRange(oldRange);
				v.setSelected(!makeValid);
			}
		}
		private class PropAction implements ActionListener {
			public void actionPerformed(ActionEvent e) {
				boolean propagate = p.isSelected();
				ViperViewMediator m = getMediator();
				PropagateInterpolateModule proper = m.getPropagator();
				if (propagate) {
					proper.startPropagating(desc);
				} else {
					proper.stopPropagating(desc);
				}
				p
						.setSelected(proper.getPropagatingDescriptors()
								.contains(desc));
			}
		}
		private class DeleteAction implements ActionListener {
			public void actionPerformed(ActionEvent e) {
				desc.getParent().removeChild(desc);
			}
		}
		private class DuplicateAction implements ActionListener {
			public void actionPerformed(ActionEvent e) {
				getMediator().duplicateDescriptor(desc);
			}
		}
		private class InterpAction implements ActionListener {
			public void actionPerformed(ActionEvent e) {
				Iterator toInterp = Collections.singleton(desc).iterator();
				ViperViewMediator m = getMediator();
				InterpQuery iq = new InterpQuery(toInterp, m);
				iq.setVisible(true);
			}
		}
		private class ShiftAction implements ActionListener {
			public void actionPerformed(ActionEvent e) {
				ViperViewMediator m = getMediator();
				ShiftQuery sq = new ShiftQuery(new Descriptor[]{desc}, m);
				sq.setVisible(true);
			}
		}
		private class InterpToMarkAction implements ActionListener {
			public void actionPerformed(ActionEvent e) {
				Iterator toInterp = Collections.singleton(desc).iterator();
				JMenuItem jmi = (JMenuItem) e.getSource();
				Iterator marks = mediator.getMarkerModel().getMarkersWithLabel(
						jmi.getText());
				if (marks.hasNext()) {
					ChronicleMarker marker = (ChronicleMarker) marks.next();
					Instant to = marker.getWhen();
					Instant from = mediator.getMajorMoment();
					mediator.getPropagator().interpolateDescriptors(toInterp,
							from, to);
				}
			}
		}
		private class ShiftToMarkAction implements ActionListener {
			public void actionPerformed(ActionEvent e) {
				JMenuItem jmi = (JMenuItem) e.getSource();
				Iterator marks = mediator.getMarkerModel().getMarkersWithLabel(
						jmi.getText());
				if (marks.hasNext()) {
					ChronicleMarker marker = (ChronicleMarker) marks.next();
					Instant to = marker.getWhen();
					Instant from = mediator.getMajorMoment();
					viper.api.impl.Util.shiftDescriptors(
							new Descriptor[]{desc}, from, to);
				}
			}
		}
		
		private JMenuItem occlusions;
		private TextlineOcclusionEditor occWindow = new TextlineOcclusionEditor();
		private class OccAction implements ActionListener {
			public void actionPerformed(ActionEvent e) {
				ViperViewMediator med = getMediator();
				TextlineModel tlm = (TextlineModel) med.getAttributeValueAtCurrentFrame(attr);
				if(tlm != null) {
					occWindow.setVisible(true);
					occWindow.setModelAndRefresh(tlm, med, attr);
				}
			}
		}
		private OccAction occAction;
		private JSeparator occSeparator;
		
		public DescPropPopup() {
			super("Descriptor Properties");
			v = new JCheckBoxMenuItem("Valid");
			v.addActionListener(new ValidAction());
			p = new JCheckBoxMenuItem("Propagating");
			p.addActionListener(new PropAction());
			delete = new JMenuItem("Delete");
			delete.addActionListener(new DeleteAction());
			duplicate = new JMenuItem("Duplicate");
			duplicate.addActionListener(new DuplicateAction());
			interp = new JMenuItem("Interpolate...");
			interp.addActionListener(new InterpAction());
			interpToMark = new JMenu("Interpolate to Mark");
			interpToMark.setEnabled(false);
			itmAction = new InterpToMarkAction();
			shift = new JMenuItem("Shift...");
			shift.addActionListener(new ShiftAction());
			shiftToMark = new JMenu("Shift to Mark");
			shiftToMark.setEnabled(false);
			stmAction = new ShiftToMarkAction();
			
			occlusions = new JMenuItem("Occlusions...");
			occAction = new OccAction();
			occlusions.addActionListener(occAction);
			occSeparator = new JSeparator();

			wrt = new JCheckBoxMenuItem("Display with Respect To", false);
			wrt.addActionListener(new WithRespectToAction());

			add(occlusions);
			add(occSeparator);
			add(v);
			add(p);
			add(occSeparator);
			add(delete);
			add(duplicate);
			add(occSeparator);
			add(interp);
			add(interpToMark);
			add(occSeparator);
			add(shift);
			add(shiftToMark);
			add(occSeparator);
			add(wrt);
		}
		public void show(Component invoker, int x, int y) {
			ViperViewMediator mediator = getMediator();
			Point pnt = new Point(x, y);
			EnhancedTable tab = getTable();
			int row = tab.rowAtPoint(pnt);
			desc = getCurrentModel().getDescriptorAtRow(row);
			int col = tab.columnAtPoint(pnt);
			Object cellValue = tab.getValueAt(row, col);
			if (cellValue instanceof Attribute) {
				attr = (Attribute) cellValue;
				
				// hide the "Occlusions..." option when we're not dealing with a Textline object
				boolean isTextline = attr.getAttrConfig().getAttrType().endsWith("textline");
				occlusions.setVisible(isTextline);
				occSeparator.setVisible(isTextline);
					
				Instant now = mediator.getCurrentFrame();
				if (now == null) {
					mediator.getDisplayWRTManager().setAttribute(null, null);
					wrt.setEnabled(false);
					wrt.setSelected(false);
				} else {
					boolean isDwrt = attr == mediator.getDisplayWRTManager()
							.getAttribute();
					boolean dwrtable = (attr.getAttrValueAtInstant(now) instanceof HasCentroid && attr
							.getDescriptor().getValidRange().contains(now));
					wrt.setEnabled(dwrtable);
					wrt.setSelected(isDwrt);
				}
			} else {
				attr = null;
				wrt.setEnabled(false);
				wrt.setSelected(false);
			}
			if (null != desc) {
				PropagateInterpolateModule proper = getMediator()
						.getPropagator();
				p.setSelected(proper.isPropagatingThis(desc));
				v.setSelected(mediator.isThisValidNow(desc));
				resetMarks();
				super.show(invoker, x, y);
			}
		}
		private void resetMarks() {
			interpToMark.removeAll();
			shiftToMark.removeAll();
			Iterator marks = mediator.getMarkerModel().getLabels().iterator();
			boolean hasMark = false;
			while (marks.hasNext()) {
				String mark = (String) marks.next();
				if (!ChronicleViewer.CURR_FRAME_LABEL.equals(mark)) {
					JMenuItem mi = new JMenuItem(mark);
					mi.addActionListener(itmAction);
					interpToMark.add(mi);
					mi = new JMenuItem(mark);
					mi.addActionListener(stmAction);
					shiftToMark.add(mi);
					hasMark = true;
				}
			}
			shiftToMark.setEnabled(hasMark);
			interpToMark.setEnabled(hasMark);
		}
	}
	public abstract void redoSelectionModel();
	public abstract void redoDataModel();
	public abstract Config getConfig();
	public void redoPropagateModel() {
		ViperTableModel m = (ViperTableModel) AbstractViperTable.this
				.getTable().getModel();
		m.fireTableDataChanged();
	}
}