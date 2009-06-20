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
import java.util.*;

import javax.swing.*;

import viper.api.impl.Util;
import viper.api.time.*;
import viper.api.time.Frame;
import viper.api.Attribute;
import viper.api.Descriptor;
import viper.api.Sourcefiles;
import edu.umd.cfar.lamp.chronicle.markers.ChronicleMarker;
import edu.umd.cfar.lamp.chronicle.markers.ChronicleMarkerComparator;
import edu.umd.cfar.lamp.chronicle.markers.ChronicleMarkerModel;
import edu.umd.cfar.lamp.viper.gui.core.*;
import edu.umd.cfar.lamp.viper.gui.remote.*;


class InterpQuery extends JDialog {
	protected JInstantField fromField, toField;
	private Iterator which;
	private ViperViewMediator mediator;
	private JPanel panel;
	private JButton interpolateButton, cancelButton;
	private JLabel fromLabel, toLabel;
	private JComboBox fromExtras, toExtras;
	private JCheckBox useMilliBox, allMarksBox, allValidBox, allNonInterpBox;
	private JComponent validityChangingComponents[];

	private GridBagLayout layout;
	private GridBagConstraints c;
	
	private FakeFrame start = new FakeFrame(1);
	private FakeFrame stop = new FakeFrame(1);
	
	public InterpQuery(Iterator l, ViperViewMediator mediator) {
		super(mediator.getPrefs().getCore(), "Interpolate in Range");
		super.setLocationRelativeTo(mediator.getPrefs().getCore());
		super.setLocation((int)getLocation().getX() - 130, (int)getLocation().getY() - 80);
		super.setResizable(false);

		this.mediator = mediator;
		which = l;
		
		layout = new GridBagLayout();
		c = new GridBagConstraints();
		c.fill = GridBagConstraints.BOTH;

		interpolateButton = new JButton("Interpolate");
		cancelButton = new JButton("Cancel");
		
		fromField = new JInstantField();
		fromField.setPreferredSize(new Dimension(100, 24));
		toField = new JInstantField();
		toField.setPreferredSize(new Dimension(100, 24));
		
		fromLabel = new JLabel("From Frame:", SwingConstants.LEFT);
		toLabel = new JLabel("To Frame:", SwingConstants.LEFT);
		
		String[] markers = this.generateArrayOfLabels();
		fromExtras = new JComboBox(markers);
		toExtras = new JComboBox(markers);
		fromExtras.addActionListener(new ChangeMarkerActionListener(fromField, fromExtras));
		toExtras.addActionListener(new ChangeMarkerActionListener(toField, toExtras));
		
		useMilliBox = new JCheckBox("Use milliseconds");
		useMilliBox.setSelected(false);
		useMilliBox.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e){
				if(useMilliBox.isSelected()){
					fromLabel.setText("From Time (ms): ");
					toLabel.setText("To Time (ms): ");
					fromField.setUnitPreference(Time.class);
					toField.setUnitPreference(Time.class);
					fromExtras.setEnabled(false);
					toExtras.setEnabled(false);
				}
				else{
					fromLabel.setText("From Frame:");
					toLabel.setText("To Frame:");
					fromField.setUnitPreference(Frame.class);
					toField.setUnitPreference(Frame.class);
					fromExtras.setEnabled(true);
					toExtras.setEnabled(true);
				}
					
			}
		});
		
		allMarksBox = new JCheckBox("Use valid marks as anchors");
		allMarksBox.setSelected(false);
		allMarksBox.addActionListener(new CheckBoxAsRadioButtonActionListener(allMarksBox));
		
		allValidBox = new JCheckBox("Use valid frames as anchors");
		allValidBox.setSelected(false);
		allValidBox.addActionListener(new CheckBoxAsRadioButtonActionListener(allValidBox));
		
		allNonInterpBox = new JCheckBox("Use non-interpolated frames as anchors");
		allNonInterpBox.setSelected(false);
		allNonInterpBox.addActionListener(new CheckBoxAsRadioButtonActionListener(allNonInterpBox));
		
		validityChangingComponents = new JComponent[9];
		validityChangingComponents[0] = allMarksBox;
		validityChangingComponents[1] = allNonInterpBox;
		validityChangingComponents[2] = allValidBox;
		validityChangingComponents[3] = fromLabel;
		validityChangingComponents[4] = fromField;
		validityChangingComponents[5] = fromExtras;
		validityChangingComponents[6] = toLabel;
		validityChangingComponents[7] = toField;
		validityChangingComponents[8] = toExtras;
		
		interpolateButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				execute();
			}
		});
		cancelButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				InterpQuery.this.setVisible(false);
				InterpQuery.this.dispose();
			}
		});

		panel = new JPanel(layout);
		panel.setComponentOrientation(ComponentOrientation.RIGHT_TO_LEFT);

		c.ipadx = 2;
		c.ipady = 2;
		c.anchor = GridBagConstraints.WEST;
		c.insets = new Insets(4, 4, 4, 4);
		
		//From frame number line
		c.gridwidth = 4;
		c.gridheight = 1;
		layout.setConstraints(fromExtras, c);
		panel.add(fromExtras);
		c.gridwidth = 4;
		layout.setConstraints(fromField, c);
		panel.add(fromField);

		c.gridwidth = GridBagConstraints.REMAINDER;
				
		layout.setConstraints(fromLabel, c);
		panel.add(fromLabel);
		
		//From frame number line
		c.gridwidth = 4;
//		c.fill = GridBagConstraints.BOTH;
		layout.setConstraints(toExtras, c);
		panel.add(toExtras);
		c.gridwidth = 4;
		layout.setConstraints(toField, c);
		panel.add(toField);

		c.gridwidth = GridBagConstraints.REMAINDER;
//		c.fill = GridBagConstraints.NONE;
		c.anchor = GridBagConstraints.WEST;
		layout.setConstraints(toLabel, c);
		panel.add(toLabel);
		
		//Checkbox Line
		c.gridwidth = 4;
		layout.setConstraints(allMarksBox, c);
		panel.add(allMarksBox);
		
		c.gridwidth = GridBagConstraints.REMAINDER;
		c.anchor = GridBagConstraints.WEST;
		layout.setConstraints(allValidBox, c);
		panel.add(allValidBox);
		
		//Second Checkbox Line
		
		c.gridwidth = GridBagConstraints.REMAINDER;
		c.anchor = GridBagConstraints.EAST;
		layout.setConstraints(allNonInterpBox, c);
		panel.add(allNonInterpBox);
		
//		layout.setConstraints(useMilliBox, c);  //Using milliseconds currently does not work
//		panel.add(useMilliBox);		

		//Interp button and cancel button line
		c.gridwidth = 5;
		c.anchor = GridBagConstraints.EAST;
		c.fill = GridBagConstraints.NONE;
		layout.setConstraints(this.cancelButton, c);
		panel.add(cancelButton);
		
		c.gridwidth = GridBagConstraints.REMAINDER;
		c.anchor = GridBagConstraints.WEST;
		layout.setConstraints(interpolateButton, c);
		panel.add(interpolateButton);
		
		super.getContentPane().add(panel);
		super.pack();
		super.validate();
	}

	//Convience var for different types of interpolations
	private Vector vector;
	private String message;
	private String success;
	protected void execute() {
		
		Runnable interpolate = new Runnable(){
			public void run(){
				//Load out the descriptors into a vector for ease of use (current implementation always has the interator as a singleton iter)
				vector = new Vector();
				while(which.hasNext())
					vector.add(which.next());
				message = "";
				success = "Success";
				
				if(mediator.getChronicleSelectionModel().getSelectedTime() != null){
					if(JOptionPane.showConfirmDialog(
							mediator.getPrefs().getCore(),
							"Interpolation will only work on selected frames, continue?", 
							"Warning",
							JOptionPane.YES_NO_OPTION) != JOptionPane.YES_OPTION)
						return;
				}		
				//Lock all the attributes, greatly reduces size of memory in undo manager

				InstantRange b4, ater;//TODO:delete me
				for(int i = 0; i < vector.size(); i++){
					Descriptor desc = (Descriptor)vector.get(i);
					desc.startAggregating();
					desc.setFreezingInterp(true);
					b4 = desc.getInterpolatedOverRange();//TODO:delete me
					for(Iterator iter = desc.getAttributes(); iter.hasNext();){
						Attribute attr = (Attribute)iter.next();
						attr.startAggregating();
					}
				}
				
				if(mediator.getHiders().getDescriptorVisibility(((Descriptor)vector.get(0))) <= NodeVisibilityManager.RANGE_LOCKED){
					success = "Error";
					message = "Cannot interpolate while descriptor is locked";
				}else if(allValidBox.isSelected()){
					interpolateOverAllValid();
				}else if(allMarksBox.isSelected()){ 
					interpolateOverAllMarks();
				}else if(allNonInterpBox.isSelected()){
					interpolateOverManuallyChanged();					
				}else{
					//Normal Interpolation
					Instant to = null;
					Instant from = null;
					message = "";
					try{
						to = (Instant) toField.getValue();
						from = (Instant)fromField.getValue();
					} catch (NumberFormatException e){
						message = ": " + e.getLocalizedMessage(); 
					}
					
					//Error checking
					int lastFrame = (((Frame)mediator.getDataPlayer().getSpan().getEnd()).getFrame() - 1);
					if(to == null || from == null)
						message = "Invalid frame number" + message;
					else if(to.intValue() < 1 || to.intValue() > lastFrame)
						message = "Frame number " + to.intValue() + " is not within the range [1," + lastFrame + "]";
					else if(from.intValue() < 1 || from.intValue() > lastFrame)
						message = "Frame number " + from.intValue() + " is not within the range [1," + lastFrame  +"]";
					else if(to.intValue() <= from.intValue())
						message = "From frame number " + from.intValue() + " is not \nless than to frame number " + to.intValue();

					if(!message.equals("")){
						success = "Error";
					} else{
						mediator.getPropagator().interpolateDescriptors(vector.iterator(), from, to);
						message = "Success!  Interpolated from " + from.intValue() + " to " + to.intValue();

						for(int i = 0; i < vector.size(); i++){
							Descriptor desc = (Descriptor)vector.get(i);
							InstantRange range = desc.getInterpolatedOverRange();
							range.add(from.next(), to);
							desc.setInterpolatedOverRange(range);						
						}
					
						start.makeMe(to);
						stop.makeMe(from);
					}
				}
				
				//Finish aggregation of attributes, this fires off the all the changes
				boolean fireChange = true;
				if(success.equals("Error"))
					fireChange = false;
				for(int i = 0; i < vector.size(); i++){
					Descriptor desc = (Descriptor)vector.get(i);
					for(Iterator iter = desc.getAttributes(); iter.hasNext();){
						Attribute attr = (Attribute)iter.next();
						attr.finishAggregating(fireChange);
					}
					desc.finishAggregating(fireChange);
					desc.setFreezingInterp(false);
					ater = desc.getInterpolatedOverRange();//TODO:Delete me
				}
			}
		};
		
		Sourcefiles sourcefiles = mediator.getViperData().getSourcefilesNode();
		Object[] tprops = new Object[]{"start", start, "stop", stop};
		Util.tryTransaction(interpolate, sourcefiles, PropagateInterpolateImpl.INTERPOLATE, tprops);
		
		if(message.length() > 500) //long messages can kill swing
			message = "Success! Multiple interpolations between " + start.intValue() + " and " + stop.intValue();
		JOptionPane.showMessageDialog(
				mediator.getPrefs().getCore(),
				message, 
				success,
				JOptionPane.PLAIN_MESSAGE);

		InterpQuery.this.setVisible(false);
		InterpQuery.this.dispose();

	}
	
	/**
	 * Interpolates over all valid points of the descriptor
	 */
	private void interpolateOverAllValid(){
		LinkedList rangesInterpolatedOver = new LinkedList();
		for(int i = 0; i < vector.size(); i++){
			Descriptor desc = (Descriptor)vector.get(i);
			InstantRange range = desc.getValidRange();
			if(!interpolateOverRange(desc, range, rangesInterpolatedOver))
				break;
		}
		message = "Success!  Interpolated over ranges:\n";
		if(rangesInterpolatedOver.isEmpty()){
			message = "Two locations where this descriptor is valid were not found";
			success = "Error";
		}
		while(!rangesInterpolatedOver.isEmpty()){
			Frame begin, end;
			begin = (Frame)rangesInterpolatedOver.removeFirst();
			end = (Frame)rangesInterpolatedOver.removeFirst();
			message += "Frame " + begin.getFrame() + " to frame " + end.getFrame() + "\n";
		}
	}
	
	private void interpolateOverManuallyChanged(){
		LinkedList rangesInterpolatedOver = new LinkedList();
		for(int i = 0; i < vector.size(); i++){
			Descriptor desc = (Descriptor)vector.get(i);
			InstantRange validRange = desc.getValidRange();
			InstantRange interpRange = desc.getInterpolatedOverRange();
			for(Iterator iter = interpRange.iterator(); iter.hasNext();)
				validRange.remove(iter.next());
			if(!interpolateOverRange(desc, validRange, rangesInterpolatedOver))
				break;
		}
		message = "Success!  Interpolated over ranges:\n";
		if(rangesInterpolatedOver.isEmpty()){
			message = "Two locations to interpolate over were not found";
			success = "Error";
		}
		while(!rangesInterpolatedOver.isEmpty()){
			Frame begin, end;
			begin = (Frame)rangesInterpolatedOver.removeFirst();
			end = (Frame)rangesInterpolatedOver.removeFirst();
			message += "Frame " + begin.getFrame() + " to frame " + end.getFrame() + "\n";
		}
	}
	
	/**
	 * Interpolates over all marks where this descriptor is valid 
	 */
	private void interpolateOverAllMarks(){
		LinkedList markersInterpolatedOver = new LinkedList(); //TODO: adapt this (and popup message) to better handle a real iterator
		for(int i = 0; i < vector.size(); i++){
			Descriptor desc = (Descriptor)vector.get(i);
			Iterator markerIterator = this.getOrderedVectorOfMarkersByFrame().iterator();
			ChronicleMarker beginMarker;
			ChronicleMarker endMarker = null;
			if(!markerIterator.hasNext())
				return;

			//Set up the first marker, ignore current frame
			beginMarker = (ChronicleMarker)markerIterator.next();
			if(beginMarker.getLabel().equals("Current Frame")){
				if(markerIterator.hasNext())
					beginMarker = (ChronicleMarker)markerIterator.next();
				else
					continue;
			}
			InstantRange range = desc.getValidRange();
			while(!range.contains(beginMarker.getWhen()) && markerIterator.hasNext())
				beginMarker = (ChronicleMarker)markerIterator.next(); 
			
			start.makeMe(beginMarker.getWhen());
			
			//Iterpolate over all the markers where desc is valid
			while(markerIterator.hasNext()){
				endMarker = (ChronicleMarker)markerIterator.next();
				
				//Go to the next valid, non-current frame marker
				while(!range.contains(endMarker.getWhen()) || 
						endMarker.getLabel().equals("Current Frame") && 
						markerIterator.hasNext())
					endMarker = (ChronicleMarker)markerIterator.next();
				
				if(!range.contains(endMarker.getWhen()) || endMarker.getLabel().equals("Current Frame"))
					break; //Already interpolated over the last valid marker
				
				InstantRange interpRange = desc.getInterpolatedOverRange();
				interpRange.add(beginMarker.getWhen().next(), endMarker.getWhen());
				desc.setInterpolatedOverRange(interpRange);
				
				markersInterpolatedOver.add(beginMarker);
				markersInterpolatedOver.add(endMarker);
				mediator.getPropagator().interpolateDescriptors(Collections.singleton(desc).iterator(), beginMarker.getWhen(), endMarker.getWhen());
				beginMarker = endMarker;
			}
			stop.makeMe(endMarker.getWhen());
		}
		
		message = "Success!  Interpolated over ranges:\n";
		if(markersInterpolatedOver.isEmpty()){
			message = "Two markers were not found where this descriptor is valid";
			success = "Error";
		}
		while(!markersInterpolatedOver.isEmpty()){
			ChronicleMarker begin, end;
			begin = (ChronicleMarker)markersInterpolatedOver.removeFirst();
			end = (ChronicleMarker)markersInterpolatedOver.removeFirst();
			message += "(Mark " + begin.getLabel() + " at frame " + begin.getWhen().intValue() + ") to ";
			message += "(Mark " + end.getLabel() + " at frame " + end.getWhen().intValue() + ")\n";
		}		
	}
	
	private boolean interpolateOverRange(Descriptor desc, InstantRange range, Collection rangesInterpolatedOver){
		Iterator rangeIter = range.iterator();
		
		if(!rangeIter.hasNext() ){
			message = "Two locations where " + desc.getDescName() + "(" + desc.getDescId() + ")"
			 	+ " is valid were not found";
			success = "Error";
			return false;
		}
			
		Span beginSpan; 
		Span endSpan = null;
		beginSpan = (Span)rangeIter.next();
		if(!rangeIter.hasNext()){
			message = "Two locations where " + desc.getDescName() + "(" + desc.getDescId() + ")"
		 		+ " is valid were not found";
			success = "Error";
			return false;
		}				

		start.makeMe(beginSpan.getStartInstant());
		while(rangeIter.hasNext()){
			//System.gc(); //We seem to use a lot of memory
			endSpan = (Span)rangeIter.next();

			Frame beginFrame = (Frame)((Frame)beginSpan.getEnd()).previous();
			Frame endFrame = ((Frame)endSpan.getStart());
			mediator.getPropagator().interpolateDescriptors(vector.iterator(), 
					beginFrame, endFrame);

			//For output string
			rangesInterpolatedOver.add(beginFrame);
			rangesInterpolatedOver.add(endFrame);

			//Have desc keep track of areas it has interpolated over
			InstantRange interpRange = desc.getInterpolatedOverRange();
			interpRange.add(beginFrame.next(), endFrame);
			desc.setInterpolatedOverRange(interpRange);
			
			beginSpan = endSpan;
		}
		stop.makeMe(endSpan.getLastInstant());
		return true;
	}
	
	private String[] generateArrayOfLabels(){
		Vector vector = this.getOrderedVectorOfMarkersByFrame();
		//Find prev and next markers
		Instant currentInstant = mediator.getMajorMoment();
		ChronicleMarker prevMarker = null;
		ChronicleMarker nextMarker = null;
		for(int i = vector.size() - 1; i >= 0; i--){
			if(((ChronicleMarker)vector.get(i)).getWhen().compareTo(currentInstant) < 0){
				prevMarker = (ChronicleMarker)vector.get(i);
				break;
			}
		}
		for(int i = 0; i < vector.size(); i++){
			if(((ChronicleMarker)vector.get(i)).getWhen().compareTo(currentInstant) > 0){
				nextMarker = (ChronicleMarker)vector.get(i);
				break;
			}
		}

		//Load Current Frame, prev and next into the array
		Collections.sort(vector, new ChronicleMarkerComparator(true));
		String[] labelsWithFrames = new String[vector.size() + 2];
		ChronicleMarker currMarker = (ChronicleMarker)vector.get(0);
		labelsWithFrames[0] = currMarker.getLabel() + " (" + currMarker.getWhen().intValue() + ")";
		if(prevMarker != null)
			labelsWithFrames[1] = "Prev - " + prevMarker.getLabel() + " (" + prevMarker.getWhen().intValue() + ")";
		else
			labelsWithFrames[1] = "Begin (1)";
		if(nextMarker != null)
			labelsWithFrames[2] = "Next - " + nextMarker.getLabel() + " (" + nextMarker.getWhen().intValue() + ")";
		else
			labelsWithFrames[2] = "End (" + (((Frame)mediator.getDataPlayer().getSpan().getEnd()).getFrame() - 1) + ")";
		
		//Load the rest of the labels into the array
		for(int i = 1; i < labelsWithFrames.length - 2; i++){
			ChronicleMarker curr = (ChronicleMarker)vector.get(i);
			labelsWithFrames[i + 2] = curr.getLabel() + " (" + curr.getWhen().intValue() + ")";
		}
		return labelsWithFrames;
	}
	
	private Vector getOrderedVectorOfMarkersByFrame(){
		ChronicleMarkerModel markerModel = mediator.getMarkerModel();
		Iterator iter = markerModel.markerIterator();
		Vector vector = new Vector();
		while(iter.hasNext())
			vector.add(iter.next());

		Collections.sort(vector, new ChronicleMarkerComparator(false));
		return vector;
	}
	
	private class ChangeMarkerActionListener implements ActionListener{
		private JInstantField myInstantField;
		private JComboBox myComboBox;
		
		public ChangeMarkerActionListener(JInstantField field, JComboBox box){
			myInstantField = field;
			myComboBox = box;
		}
		
		public void actionPerformed(ActionEvent e){
			String str = (String)myComboBox.getSelectedItem();
			String[] splits = str.split("[\\)\\(]");
			myInstantField.setText(splits[1]);
		}
	}
	

	/**
	 * Convience class that changes all the components validity except its checkBox 
	 * when it is clicked
	 * @author jnewman
	 *
	 */
	private class CheckBoxAsRadioButtonActionListener implements ActionListener{
		private JCheckBox myCheckBox;
		
		public CheckBoxAsRadioButtonActionListener(JCheckBox box){
			myCheckBox = box;
		}
		
		public void actionPerformed(ActionEvent e){
			boolean changingTo;
			if(myCheckBox.isSelected())
				changingTo = false;
			else
				changingTo = true;

			for(int i = 0; i < validityChangingComponents.length; i++){
				if(validityChangingComponents[i] != myCheckBox)
					validityChangingComponents[i].setEnabled(changingTo);
			}
		}
	}
	
	private class FakeFrame extends Frame{
		public FakeFrame(int i){
			super(i);
		}
		
		public void makeMe(Instant other){
			currFrame = other.intValue();
		}
	}
}