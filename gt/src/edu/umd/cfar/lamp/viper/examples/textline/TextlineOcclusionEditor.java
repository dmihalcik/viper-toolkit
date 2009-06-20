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

package edu.umd.cfar.lamp.viper.examples.textline;

import java.util.*;
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.border.*;
import javax.swing.event.*;
import javax.swing.text.*;
import javax.swing.text.html.*;

import viper.api.*;

import edu.umd.cfar.lamp.viper.gui.core.*;

/**
 * @author spikes51@umiacs.umd.edu
 * @since May 8, 2005
 *
 */

public class TextlineOcclusionEditor extends JFrame {

	protected JTextPane m_editor;
	protected StyleSheet m_context;
	protected HTMLDocument m_doc;
	protected HTMLEditorKit m_kit;

	protected SmallToggleButton occToggle;
	protected JTextField out;
	protected boolean m_textChanged = false;

	protected int m_xStart = -1;
	protected int m_xFinish = -1;
	
	private TextlineModel model;
	private ViperViewMediator mediator;
	private Attribute attrib;
	
	public TextlineOcclusionEditor() {
		super("Occlusion Editor");
		init();
	}
	
	public static void main(String[] args) {
		TextlineOcclusionEditor oe = new TextlineOcclusionEditor();
		oe.setVisible(true);
	}

	public void init() {
		setSize(new Dimension(380,100));
		setVisible(false);
		Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
		
		setResizable(false);
		
		int x = (screenSize.width  - 380)  /2;
		int y = (screenSize.height - 100) /2;
		setLocation(x,y);
		
		// catch the close event
		setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
		WindowListener l = new WindowAdapter() {
			public void windowClosing(WindowEvent e) {
				updateAndHide();
			}
		};
		addWindowListener(l);
		
		SpringLayout sl = new SpringLayout();
		getContentPane().setLayout(sl);
		
		m_editor = new JTextPane();
		m_editor.setFont(new Font("Courier", Font.PLAIN,12));
		StyleConstants.setFontFamily(new SimpleAttributeSet(), "Courier");
		m_kit = new HTMLEditorKit();
		m_editor.setEditorKit(m_kit);

		JScrollPane ps = new JScrollPane(m_editor);
		ps.setPreferredSize(new Dimension(335,35));
		getContentPane().add(ps);
		sl.putConstraint(SpringLayout.NORTH, ps, 3, SpringLayout.NORTH, getContentPane());
		sl.putConstraint(SpringLayout.WEST, ps, 3, SpringLayout.WEST, getContentPane());
		sl.putConstraint(SpringLayout.EAST, getContentPane(), 3, SpringLayout.EAST, ps);

		CaretListener clst = new CaretListener() {
			public void caretUpdate(CaretEvent e) {
				showAttributes(e.getDot());
			}
		};
		m_editor.addCaretListener(clst);

		FocusListener flst = new FocusListener() {
			public void focusGained(FocusEvent e) {
				int len = m_editor.getDocument().getLength();
				if (m_xStart>=0 && m_xFinish>=0 && m_xStart<len && m_xFinish<len)
					if (m_editor.getCaretPosition()==m_xStart) {
						m_editor.setCaretPosition(m_xFinish);
						m_editor.moveCaretPosition(m_xStart);
					}
					else
						m_editor.select(m_xStart, m_xFinish);
			}

			public void focusLost(FocusEvent e) {
				m_xStart = m_editor.getSelectionStart();
				m_xFinish = m_editor.getSelectionEnd();
			}
		};
		m_editor.addFocusListener(flst);

		//ImageIcon img1 = new ImageIcon("Bold16.gif");
		occToggle = new SmallToggleButton(false, "Occluded", "Toggle occluded");
		ActionListener lst = new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				int xStart = m_editor.getSelectionStart();
				int xFinish = m_editor.getSelectionEnd();
				if (!m_editor.hasFocus()) {
					xStart = m_xStart;
					xFinish = m_xFinish;
				}
				occludeRange(xStart, xFinish, occToggle.isSelected());
				m_editor.grabFocus();
			}
		};
		occToggle.addActionListener(lst);
		getContentPane().add(occToggle);
		sl.putConstraint(SpringLayout.NORTH, occToggle, 3, SpringLayout.SOUTH, ps);
		
		out = new JTextField();
		out.setPreferredSize(new Dimension(200,30));
		getContentPane().add(out);
		sl.putConstraint(SpringLayout.NORTH, out, 3, SpringLayout.SOUTH, ps);
		sl.putConstraint(SpringLayout.WEST, out, 10, SpringLayout.EAST, occToggle);
		sl.putConstraint(SpringLayout.SOUTH, getContentPane(), 0, SpringLayout.SOUTH, out);
		
		JButton showOcc = new JButton("Show");
		ActionListener slst = new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				out.setText("");

				int start = -2;
				int end = -2;
				boolean flushed = false;
				for(int i = 0; i < m_doc.getLength(); i++) {
					AttributeSet attr = m_doc.getCharacterElement(i).getAttributes();
					boolean bold = StyleConstants.isBold(attr);
					if(bold) {
						if(end == i - 1) {
							end = i;
						} else {
							start = end = i;
							flushed = false;
						}
					} else {
						if(end == i - 1) {
							out.setText(out.getText() + "[" + (start-1) + "," + (end-1) +"] ");
							flushed = true;
						}
					}
				}
				if(!flushed && end > 0) { // output separately if bold went to the end of the line
					out.setText(out.getText() + "[" + (start-1) + "," + (end-1) +"]");
				}
			}
		};
		showOcc.addActionListener(slst);
		getContentPane().add(showOcc);
		sl.putConstraint(SpringLayout.NORTH, showOcc, 3, SpringLayout.SOUTH, ps);
		sl.putConstraint(SpringLayout.WEST, showOcc, 5, SpringLayout.EAST, out);
		
		newDocument();
		
		pack();
	}
	
	// returns an ArrayList containing the zero-indexed occlusion ranges currently marked
	public ArrayList getOcclusionsArray() {
		ArrayList ret = new ArrayList();
		int start = -2;
		int end = -2;
		boolean flushed = false;
		for(int i = 0; i < m_doc.getLength(); i++) {
			AttributeSet attr = m_doc.getCharacterElement(i).getAttributes();
			boolean bold = StyleConstants.isBold(attr);
			if(bold) {
				if(end == i - 1) {
					end = i;
				} else {
					start = end = i;
					flushed = false;
				}
			} else {
				if(end == i - 1) {
					ret.add(new IntPair(start-1,end-1));
					flushed = true;
				}
			}
		}
		if(!flushed && end > 0) { // handle separately if bold went to the end of the line
			ret.add(new IntPair(start-1,end-1));
		}
		return ret;
	}
	
	// called right before hiding the window
	private void updateAndHide() {
		if(model != null) {
			model = (TextlineModel) model.clone();
			model.setOcclusions(getOcclusionsArray());
			mediator.setAttributeValueAtCurrentFrame(model,attrib);
		}
		setVisible(false); // hide the frame in either case
	}
	
	/**
	 * Refreshes the editor based on a new TextlineModel
	 * 
	 * @param tlm the TextlineModel to use
	 */
	public void setModelAndRefresh(TextlineModel tlm, ViperViewMediator med, Attribute attr) {
		model = tlm;
		mediator = med;
		attrib = attr;
		
		setText(tlm.getText(null));
		
		// display all existing occlusions
		ArrayList occlusions = tlm.getOcclusions();
		for(int i = 0; i < occlusions.size(); i++) {
			IntPair ip = (IntPair) occlusions.get(i);
			occludeRange(ip.getOne()+1, ip.getTwo()+2, true);
		}
	}
	
	/**
	 * Sets the text of the editor
	 * 
	 * @param text
	 */
	public void setText(String text) {
		m_editor.setText(text);
	}

	protected void newDocument() {
		m_doc = (HTMLDocument)m_kit.createDefaultDocument();
		m_context = m_doc.getStyleSheet();

		m_editor.setDocument(m_doc);

		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				showAttributes(0);	// NEW
				m_editor.scrollRectToVisible(new Rectangle(0,0,1,1));
				m_doc.addDocumentListener(new UpdateListener());
			}
		});
	}

	protected void showAttributes(int p) {
		AttributeSet attr = m_doc.getCharacterElement(p).getAttributes();
		//String name = StyleConstants.getFontFamily(attr);
		boolean bold = StyleConstants.isBold(attr);
		if (bold != occToggle.isSelected())
			occToggle.setSelected(bold);
	}

	/**
	 * Occludes the range [xStart, xFinish]
	 * 
	 * @param xStart
	 * @param xFinish
	 */
	public void occludeRange(int xStart, int xFinish, boolean on) {
		MutableAttributeSet attr = new SimpleAttributeSet();
		StyleConstants.setBold(attr, on);
		if (xStart != xFinish) {
			m_doc.setCharacterAttributes(xStart, xFinish - xStart, attr, false);
		}
	}

	class UpdateListener implements DocumentListener {

		public void insertUpdate(DocumentEvent e) {
			m_textChanged = true;
		}

		public void removeUpdate(DocumentEvent e) {
			m_textChanged = true;
		}

		public void changedUpdate(DocumentEvent e) {
			m_textChanged = true;
		}
	}
}

class SmallToggleButton extends JToggleButton
implements ItemListener {
	
	protected Border m_raised = new SoftBevelBorder(BevelBorder.RAISED);
	protected Border m_lowered = new SoftBevelBorder(BevelBorder.LOWERED);
	protected Insets m_ins = new Insets(4,4,4,4);
	
	//public SmallToggleButton(boolean selected, ImageIcon imgUnselected, ImageIcon imgSelected, String tip) {
	public SmallToggleButton(boolean selected, String text, String tip) {
		//super(imgUnselected, selected);
		super(text);
		setHorizontalAlignment(CENTER);
		setBorder(selected ? m_lowered : m_raised);
		setMargin(m_ins);
		setToolTipText(tip);
		setRequestFocusEnabled(false);
		//setSelectedIcon(imgSelected);
		addItemListener(this);
	}
	
	public float getAlignmentY() {
		return 0.5f;
	}
	
	// Overridden for 1.4 bug fix
	public Insets getInsets() {
		return m_ins;
	}
	
	public Border getBorder() {
		return (isSelected() ? m_lowered : m_raised);
	}
	
	public void itemStateChanged(ItemEvent e) {
		setBorder(isSelected() ? m_lowered : m_raised);
	}
}
