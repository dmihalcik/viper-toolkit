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
import java.awt.event.*;

import javax.swing.*;

import viper.api.*;
import viper.api.time.*;
import viper.api.time.Frame;
import edu.umd.cfar.lamp.viper.gui.core.*;
import edu.umd.cfar.lamp.viper.gui.remote.*;


class ShiftQuery extends JFrame {
	protected JInstantField number;
	private Descriptor[] which;
	private ViperViewMediator mediator;
	private JPanel panel;
	private JRadioButton preferFrame;
	private JRadioButton preferTime;
	ShiftQuery(Descriptor[] l, ViperViewMediator mediator) {
		super("Shift from Current Frame");
		this.mediator = mediator;
		which = l;
		JButton shiftButton = new JButton("Shift");
		JButton cancel = new JButton("Cancel");
		preferFrame = new JRadioButton("Frame");
		preferFrame.setSelected(true);
		preferTime = new JRadioButton("Millisecond");
		number = new JInstantField();
		number.setPreferredSize(new Dimension(100, 24));

		shiftButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				execute();
			}
		});
		cancel.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				ShiftQuery.this.setVisible(false);
				ShiftQuery.this.dispose();
			}
		});
		preferFrame.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				number.setUnitPreference(Frame.class);
				preferTime.setSelected(false);
			}
		});
		preferTime.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				number.setUnitPreference(Time.class);
				preferFrame.setSelected(false);
			}
		});

		panel = new JPanel();
		panel.add(preferFrame);
		panel.add(preferTime);
		panel.add(number);
		panel.add(cancel);
		panel.add(shiftButton);
		panel.validate();
		super.getContentPane().add(panel);
		super.pack();
		super.validate();
	}
	protected void execute() {
		Instant to = (Instant) number.getValue();
		Instant from = mediator.getMajorMoment();
		viper.api.impl.Util.shiftDescriptors(which, from, to);
		ShiftQuery.this.setVisible(false);
		ShiftQuery.this.dispose();
	}
}