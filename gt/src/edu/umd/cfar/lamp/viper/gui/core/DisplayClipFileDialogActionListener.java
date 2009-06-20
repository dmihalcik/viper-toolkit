package edu.umd.cfar.lamp.viper.gui.core;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.ComponentOrientation;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.Collections;
import java.util.Iterator;
import java.util.Vector;
import java.util.logging.Level;

import javax.swing.AbstractAction;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JSeparator;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.WindowConstants;

import viper.api.impl.XmlSerializer;
import viper.api.time.Frame;
import viper.api.time.Span;
import edu.umd.cfar.lamp.chronicle.markers.ChronicleMarker;
import edu.umd.cfar.lamp.chronicle.markers.ChronicleMarkerComparator;
import edu.umd.cfar.lamp.chronicle.markers.ChronicleMarkerModel;
import edu.umd.cfar.lamp.viper.gui.players.DataPlayer;
import edu.umd.cfar.lamp.viper.gui.players.MpegTest;
import edu.umd.cfar.lamp.viper.gui.players.NativePlayer;

/**
 * This class manages the gui for file clipping
 * 
 * @author jnewman
 */
public class DisplayClipFileDialogActionListener extends AbstractAction {
	private ViperViewMediator mediator;

	//Swing variables
	private JDialog dialog;
	private JPanel jpanel;
	private JTextArea textArea;
	private JTextField fromTextField, toTextField, fileTextField, metaTextField;
	private JComboBox fromMarkers, toMarkers;
	private JLabel fromLabel, toLabel, fileLabel, metaLabel;
	private JButton clipButton, fileSearchButton, metaSearchButton, closeButton;
	private GridBagLayout layout;
	private GridBagConstraints c;

	//Extra information on clipping
	int actualBeginFrame, numFrames;
	/**
	 * Sets up the ActionListener
	 * @param vvm The ViperViewMediator being used in the project
	 */
	public DisplayClipFileDialogActionListener(ViperViewMediator vvm) {
		mediator = vvm;
	}

	/* (non-Javadoc)
	 * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
	 */
	public void actionPerformed(ActionEvent e) {
		if(mediator.getCurrFile() != null)
			displayClipFileDialog();
	}

	/**
	 * Displays the Clip File dialog box
	 */
	private void displayClipFileDialog() {
		dialog = new JDialog(mediator.getPrefs().getCore(), "Clip File");
		dialog.setLocationRelativeTo(mediator.getPrefs().getCore());
		dialog.setLocation((int)dialog.getLocation().getX() - 130, (int)dialog.getLocation().getY() - 80);
		dialog.setResizable(false);

		File fileToWriteTo = new File(new File(mediator.getCurrFileName()).toString() + ".clip(1).mpg");
		for(int i = 2; fileToWriteTo.exists(); i++)
			fileToWriteTo = new File(new File(mediator.getCurrFileName()).toString() + ".clip(" + i + ").mpg");
		
		File metaFileToWriteTo = new File(fileToWriteTo.toString() + ".xgtf");
		
		layout = new GridBagLayout();
		c = new GridBagConstraints();
		c.fill = GridBagConstraints.BOTH;
		
		jpanel = new JPanel(layout);
		jpanel.setComponentOrientation(ComponentOrientation.RIGHT_TO_LEFT);

		//Set up the array for the markers
		String[] labelsWithFrames = this.generateArrayOfLabels();
				
		//Initialize the components
		textArea = new JTextArea(
				"Specify the first and last frame to include in the\n" +
				"clipped file.  While it is guaranteed that these frames\n" +
				"will be included, it is likely there will be some extra\n" +
				"frames both before and after this range.");
		textArea.setEditable(false);
		textArea.setFont(mediator.getPrefs().getCore().getFont());
		textArea.setBackground(mediator.getPrefs().getCore().getBackground());
		textArea.setBorder(BorderFactory.createLineBorder(Color.GRAY));
		
		fromTextField = new JTextField(10);
		toTextField = new JTextField(10);
		fileTextField  = new JTextField(20);
		fileTextField.setText(fileToWriteTo.toString());
		metaTextField = new JTextField(20);
		metaTextField.setText(metaFileToWriteTo.toString());
		fromLabel = new JLabel("From Frame:", SwingConstants.LEFT);
		toLabel = new JLabel("To Frame:", SwingConstants.LEFT);
		fileLabel = new JLabel("Clip File:", SwingConstants.LEFT);
		metaLabel = new JLabel("Metadata:", SwingConstants.LEFT);

		targetFileDialogActionListener = new OpenFileDialogActionListener(fileToWriteTo, fileTextField);
		metadataFileDialogActionListner = new OpenFileDialogActionListener(metaFileToWriteTo, metaTextField);
		
		fromMarkers = new JComboBox(labelsWithFrames);
		toMarkers = new JComboBox(labelsWithFrames);
		this.changeFromMarkerActionListner = new ChangeMarkerActionListener(fromTextField, fromMarkers);
		this.changeToMarkerActionListener = new ChangeMarkerActionListener(toTextField, toMarkers);
		fromMarkers.addActionListener(this.changeFromMarkerActionListner);
		toMarkers.addActionListener(this.changeToMarkerActionListener);
		
		clipButton = new JButton("Clip File");
		clipButton.addActionListener(performClipFileActionListener);
		fileSearchButton = new JButton("...");
		fileSearchButton.addActionListener(this.targetFileDialogActionListener);
		fileSearchButton.setSize(3, fileSearchButton.getSize().height);
		closeButton = new JButton("Close");
		closeButton.addActionListener(closeDialogActionListener);
		metaSearchButton = new JButton("...");
		metaSearchButton.addActionListener(metadataFileDialogActionListner);
		metaSearchButton.setSize(3, fileSearchButton.getSize().height);

		c.ipadx = 2;
		c.ipady = 2;
		c.insets = new Insets(4, 4, 4, 4);

		//Description box
		c.gridwidth = GridBagConstraints.REMAINDER;
		c.gridheight = 5;
		layout.setConstraints(textArea, c);
		jpanel.add(textArea);
		
		//From frame number line
		c.gridwidth = 4;
		c.gridheight = 1;
		layout.setConstraints(fromMarkers, c);
		jpanel.add(fromMarkers);
		layout.setConstraints(fromTextField, c);
		jpanel.add(fromTextField);

		c.gridwidth = GridBagConstraints.REMAINDER;
		layout.setConstraints(fromLabel, c);
		jpanel.add(fromLabel);

		//To frame number line
		c.gridwidth = 4;
		layout.setConstraints(toMarkers, c);
		jpanel.add(toMarkers);
		layout.setConstraints(toTextField, c);
		jpanel.add(toTextField);
		
		c.gridwidth = GridBagConstraints.REMAINDER;
		layout.setConstraints(toLabel, c);
		jpanel.add(toLabel);

		//Separator line
		JSeparator sep = new JSeparator();
		layout.setConstraints(sep, c);
		jpanel.add(sep);

		//File line
		c.gridwidth = 1;
		layout.setConstraints(fileSearchButton, c);
		jpanel.add(fileSearchButton);
		
		c.gridwidth = 7;
		layout.setConstraints(fileTextField, c);
		jpanel.add(fileTextField);
		
		c.gridwidth = GridBagConstraints.REMAINDER;
		layout.setConstraints(fileLabel, c);
		jpanel.add(fileLabel);
		
		//Meta file line
		c.fill = GridBagConstraints.BOTH;
		c.gridwidth = 1;
		layout.setConstraints(metaSearchButton, c);
		jpanel.add(metaSearchButton);
		
		c.gridwidth = 7;
		layout.setConstraints(metaTextField, c);
		jpanel.add(metaTextField);
		
		c.gridwidth = GridBagConstraints.REMAINDER;
		layout.setConstraints(metaLabel, c);
		jpanel.add(metaLabel);		
		
		//Close and clip file buttons line
		c.gridwidth = 3;
		layout.setConstraints(closeButton, c);
		jpanel.add(closeButton);
		
		c.gridwidth = GridBagConstraints.REMAINDER;
		c.fill = GridBagConstraints.NONE;
		c.anchor = GridBagConstraints.WEST;
		layout.setConstraints(clipButton, c);
		jpanel.add(clipButton);

		dialog.getRootPane().setDefaultButton(clipButton);
		dialog.getContentPane().add(jpanel, BorderLayout.CENTER);

		dialog.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
		dialog.pack();
		dialog.setVisible(true);

	}

	//Action listener objects 
	private PerformClipFileActionListener performClipFileActionListener = new PerformClipFileActionListener();
	private CloseDialogActionListener closeDialogActionListener = new CloseDialogActionListener();
	private OpenFileDialogActionListener targetFileDialogActionListener;
	private OpenFileDialogActionListener metadataFileDialogActionListner;
	private ChangeMarkerActionListener changeFromMarkerActionListner, changeToMarkerActionListener;

	//Action listener classes
	private class ChangeMarkerActionListener implements ActionListener{
		private JTextField myTextField;
		private JComboBox myComboBox;
		
		public ChangeMarkerActionListener(JTextField field, JComboBox box){
			myTextField = field;
			myComboBox = box;
		}
		
		public void actionPerformed(ActionEvent e){
			String str = (String)myComboBox.getSelectedItem();
			String[] splits = str.split("[\\)\\(]");
			myTextField.setText(splits[1]);
		}
	}
	private class OpenFileDialogActionListener implements ActionListener{
		File myFile;
		JTextField myTextField;
		final JFileChooser fileChooser = new JFileChooser();
		public OpenFileDialogActionListener(File myFile, JTextField myTextField){
			this.myFile = myFile;
			this.myTextField = myTextField;
		}
		
		public void actionPerformed(ActionEvent e){
			fileChooser.setCurrentDirectory(mediator.getLastDirectory());
			if(fileChooser.showOpenDialog(mediator.getPrefs().getCore()) ==
				JFileChooser.APPROVE_OPTION){
				myTextField.setText(myFile.toString());
			}
		}
	}
	private class CloseDialogActionListener implements ActionListener{
		public void actionPerformed(ActionEvent e){
			dialog.dispose();
		}
	}
	private class PerformClipFileActionListener implements ActionListener {
		public void actionPerformed(ActionEvent e) {
			clipFile();
		}
	}

	/**
	 * Clips the current file, using information stored in the current active dialog box
	 * Therefore, the dialog must have valid values before calling this function
	 */
	private void clipFile(){
		System.gc();
		DataPlayer dataPlayer = mediator.getDataPlayer();
		if(dataPlayer instanceof NativePlayer){
			NativePlayer nativePlayer = (NativePlayer)dataPlayer;
			try{
				//Extract info from the dialog, announcing errors where appropriate
				Frame fromFrame = new Frame(Integer.parseInt(fromTextField.getText()));
				Frame toFrame = new Frame(Integer.parseInt(toTextField.getText()));
				Span span = nativePlayer.getSpan();
				if(fromFrame.compareTo(toFrame) >= 0){
					throw new NumberFormatException("Frame " + fromFrame.getFrame() + 
							" is not less than frame " + toFrame.getFrame());
				}
				if(!nativePlayer.getSpan().contains(fromFrame) ||
						!nativePlayer.getSpan().contains(toFrame)){
					throw new NumberFormatException("Frame number not within range:" + 
							((Frame)span.getStart()).getFrame() + " to " + 
							(((Frame)span.getEnd()).getFrame() - 1)); 
				}

				
				String errMessage = "File(s): ";
				File fileToWriteTo = new File(fileTextField.getText());
				File metaFileToWriteTo = new File(metaTextField.getText());
				if(fileToWriteTo.exists())
					errMessage += fileToWriteTo.toString() + "\n";
				if(metaFileToWriteTo.exists())
					errMessage += metaFileToWriteTo.toString() + "\n";
				if(fileToWriteTo.exists() || metaFileToWriteTo.exists()){
					int choice = JOptionPane.showConfirmDialog(
							mediator.getPrefs().getCore(), 
							errMessage +
							"Already exist, are you sure you want to overwrite?",
							"Warning: File Exists", JOptionPane.YES_NO_OPTION);
					if(choice != JOptionPane.YES_OPTION)
						return;
				}

				//Clip the file and save off the pruned metadata
				actualBeginFrame = nativePlayer.clipFile(fileToWriteTo.toString(), fromFrame.getFrame(), toFrame.getFrame()) + 1;
				if(saveMetaData(actualBeginFrame, fileToWriteTo, metaFileToWriteTo)){
					JOptionPane.showMessageDialog(
							mediator.getPrefs().getCore(),
							"Success!  Actual first frame in clip:" + actualBeginFrame,
							"Success",
							JOptionPane.PLAIN_MESSAGE);
				}else {
					JOptionPane.showMessageDialog(
							mediator.getPrefs().getCore(),
							"Error clipping file, see logs for more information",
							"Error",
							JOptionPane.ERROR);					
				}
				
				dialog.dispose();
			} catch(NumberFormatException ex){
				JOptionPane.showMessageDialog(
						mediator.getPrefs().getCore(),
						"Invalid Frame Number: " + ex.getMessage(), 
						"Invalid Frame Number",
						JOptionPane.ERROR_MESSAGE);
			}
		}else{
			//Can currently only clip when you have a NativePlayer
			JOptionPane.showMessageDialog(
					mediator.getPrefs().getCore(),
					"File clipping is not currently available with the current decoder:" + dataPlayer.getClass(),
					"Clipping not functional with current decoder",
					JOptionPane.ERROR_MESSAGE);
			dialog.dispose();
		}
		
	}
	
	/**
	 * Saves the metadata of the recently clipped file to the file clippedFile + ".xgtf" 
	 * All valid regions are shifted properly to accomidate the new range, or entirely removed 
	 * if no longer part of the valid region.
	 * @param actualBeginFrame The frame (first frame=1) that corresponds to the first frame in the clipped file
	 * @param clippedFile The clipped file
	 * @return True if everything is saved properly, false if there was some problem
	 */
	private boolean saveMetaData(int actualBeginFrame, File clippedFile, File metaFileToWriteTo){
		StringWriter sw = new StringWriter();
		try{
			XmlSerializer.toWriter(mediator.getViperData(), new PrintWriter(sw));
		} catch(IOException e){
			mediator.getPrefs().getLogger().log(Level.SEVERE,
					"Error serializing metadata:", e);
			return false;
		}
		numFrames = this.findNumFrames(clippedFile);
		String str = sw.toString();
		str = fixFileName(str, clippedFile);
		str = fixNumFrames(str);
		str = fixAllFrameSpans(str);
		str = removeWhiteLines(str);

		try{
			File clippedFileMetaData = File.createTempFile("_gtf-", clippedFile.getName(), clippedFile.getParentFile());
			FileWriter fWriter = new FileWriter(clippedFileMetaData);
			fWriter.write(str);
			fWriter.close();
			clippedFileMetaData.renameTo(metaFileToWriteTo);
		} catch(IOException e){
			mediator.getPrefs().getLogger().log(Level.SEVERE,
					"Error saving metadata:", e);
			return false;
		}
		mediator.modifyMostRecentlyUsed(metaFileToWriteTo.toURI());
		return true;
	}
	
	/**
	 * Finds the number of frames in the mpeg movie pointed to by file
	 * @param file The location of the mpeg movie
	 * @return The number of frames in the movie located at file
	 */
	private int findNumFrames(File file){
		System.gc();
		MpegTest mpegTest = new MpegTest(file.getAbsolutePath());
		return mpegTest.getNumFrames();
	}
	
	/**
	 * Finds the location of the NUMFRAMES attribute in str and fixes it to reflect 
	 * the new number of frames
	 * @param str The string to be corrected (XML metadata)
	 * @return The corrected string
	 */
	private String fixNumFrames(String str){
		String regEx = "<attribute name=\"NUMFRAMES\">\\s*<data:dvalue value=\"\\d+\"/>";
		String[] splits = str.split(regEx);
		if(splits.length != 2)
			return str;
		String numFrameSection = str.substring(splits[0].length(), str.length() - splits[1].length());
		regEx = "<data:dvalue value=\"\\d+\"/>";
		numFrameSection = numFrameSection.replaceAll(regEx, "<data:dvalue value=\"" + numFrames + "\"/>");
		return splits[0] + numFrameSection + splits[1];
	}

	/**
	 * Finds the first sourcefile section of str, which contains the name of the file that the  
	 * metadata describes, and fixes it to reflect the new (clipped) file name
	 * @param str The string to be corrected (XML metadata)
	 * @param newFile
	 * @return The corrected string
	 */
	private String fixFileName(String str, File newFile){
		String regEx = "<sourcefile filename=\"file:\\p{Graph}+g\">";
		String newFileStr = "<sourcefile filename=\"" + newFile.toURI().toString() + "\">";
		return str.replaceFirst(regEx,newFileStr);	
	}
	
	/**
	 * Finds and fixes all of the framespan sections of str
	 * @param str the string to be corrected (XML metadata)
	 * @return The corrected string
	 */
	private String fixAllFrameSpans(String str){
		/*
		 * The view employed of str (the xml file) here is a collection of random strings with 
		 * strings describing frame ranges in between them.  str looks like:
		 * <random><obj with range><random><obj with range>...<random>
		 * 
		 * The first step is to extract all the objects 
		 */
		
		String beginObject = "<object";
		String endObject = "</object>";
		Vector beginTags, endTags;
		beginTags = new Vector();
		endTags = new Vector();
		int location = 0;

		//Find the location of all the begin and ending object tags
		while((location = str.indexOf(beginObject, location)) != -1)
			beginTags.add(new Integer(location++));
		location = 0;

		//Make the first "end tag" the very beginning
		endTags.add(new Integer(0));
		while((location = str.indexOf(endObject, location)) != -1)
			endTags.add(new Integer(location++ + 9));
		if(beginTags.size() != (endTags.size() - 1) || beginTags.size() < 1)
			return str;
		
		String[] objects = new String[beginTags.size()];
		String[] nonObjects = new String[beginTags.size() + 1];
		boolean switch_currObject = false;
		location = 0;
		int index = 0;

		//Break str into object parts and non object parts
		while(index < nonObjects.length){
			if(switch_currObject){
				objects[index] = str.substring(((Integer)beginTags.get(index)).intValue(),
						((Integer)endTags.get(index + 1)).intValue());
				index++;
			} else{
				if(index < beginTags.size())
					nonObjects[index] = str.substring(((Integer)endTags.get(index)).intValue(),
							((Integer)beginTags.get(index)).intValue());
				else{
					nonObjects[index] = str.substring(((Integer)endTags.get(index)).intValue(),
							str.length());
					break;
				}
			}
			switch_currObject = !switch_currObject;
		}
			
		for(int i = 0; i < objects.length; i++)
			objects[i] = fixOneObject(objects[i]);
		
		String recombined = "";
		recombined += nonObjects[0];
		for(int i = 1; i < nonObjects.length; i++)
			recombined += objects[i - 1] + nonObjects[i];
		
		return recombined;
	}
	
	/**
	 * Takes one object, in the form <object ... </object>, and fixes all related areas in it
	 * @param object The object to be fixed
	 * @return The fixed object, or the empty string ("") if this object is no longer valid
	 */
	private String fixOneObject(String object){
		//Fix the frame span of the overall object
		String framespan = getFramespan(object);
		String fixedFramespan = fixFramespan(framespan);
		if(fixedFramespan.equals(""))
			return ""; //This object is no long valid anywhere
		object = object.replaceFirst(framespan, fixedFramespan);

		//Now, view the object as <random><data><random>...<random>
		//Note that its possible to have blank attributes (with no data) in the new file
		
		String beginData = "<data";
		String endData = "/>";
		Vector beginTags, endTags;
		beginTags = new Vector();
		endTags = new Vector();
		int location = 0;

		//Make the first "end-tag" the very beginning
		endTags.add(new Integer(0));
		
		//Find the location of all the begin and ending data tags
		while((location = object.indexOf(beginData, location)) != -1){
			beginTags.add(new Integer(location++));
			location = object.indexOf(endData, location);
			endTags.add(new Integer(location++ + 2));
		}

		location = 0;
			
		if(beginTags.size() != (endTags.size() - 1) || beginTags.size() < 1)
			return object;
		
		String[] datas = new String[beginTags.size()];
		String[] nonDatas = new String[beginTags.size() + 1];
		boolean switch_currData = false;
		location = 0;
		int index = 0;

		//Break str into data parts and non data parts
		while(index < nonDatas.length){
			if(switch_currData){
				datas[index] = object.substring(((Integer)beginTags.get(index)).intValue(),
						((Integer)endTags.get(index + 1)).intValue());
				index++;
			} else{
				if(index < beginTags.size())
					nonDatas[index] = object.substring(((Integer)endTags.get(index)).intValue(),
							((Integer)beginTags.get(index)).intValue());
				else{
					nonDatas[index] = object.substring(((Integer)endTags.get(index)).intValue(),
							object.length());
					break;
				}
			}
			switch_currData = !switch_currData;
		}
		
		for(int i = 0; i < datas.length; i++)
			datas[i] = fixOneData(datas[i]);
		
		String recombined = "";
		recombined += nonDatas[0];
		for(int i = 1; i < nonDatas.length; i++)
			recombined += datas[i - 1] + nonDatas[i];
		
		return recombined;
	}
	
	/**
	 * Takes one data object, in the form <data ... />, and fixes its framespan element
	 * @param data The data object to be fixed
	 * @return The fixed data object, or the empty string ("") if this data object is no longer valid
	 */
	private String fixOneData(String data){
		String framespan = getFramespan(data);
		String fixedFramespan = fixFramespan(framespan);
		if(fixedFramespan.equals(""))
			return "";
		return data.replaceFirst(framespan, fixedFramespan);
	}
	
	/**
	 * Finds and returns the first section of str that matches framespan=" ... "
	 * @param str The string to search for the framespan
	 * @return The first framespan in str, or the empty string ("") if no framespan found
	 */
	private String getFramespan(String str){
		int begin = str.indexOf("framespan=");
		if(begin == -1)
			return "";
		int end = str.indexOf("\" ", begin + 10);
		if(end == -1)
			return "";
		return str.substring(begin, end + 2);
	}
	
	/**
	 * Takes a framespan, in the from framespan=" ... " and fixes it according the the new 
	 * first frame and number of frames in the clipped file
	 * @param framespan The framespan to be fixed
	 * @return The fixed framespan, or the empty string ("") if this framespan is no longer valid
	 */
	private String fixFramespan(String framespan){
		String inside = framespan.substring(framespan.indexOf("\"") + 1, framespan.length() - 1);
		String[] rangesAsStrings = inside.split("(:)|(\\s)|(\")");
		assert (rangesAsStrings.length % 2) == 0;
		int[] ranges = new int[rangesAsStrings.length];
		try{
			for(int i = 0; i < ranges.length; i++)
				ranges[i] = Integer.parseInt(rangesAsStrings[i]);
		} catch(NumberFormatException e){
			return framespan;
		}
		
		//Shift all values down
		for(int i = 0; i < ranges.length; i++)
			ranges[i] = ranges[i] - actualBeginFrame + 1;

		//This ranges [i, i+1] begins beyond the current valid range, it needs to be deleted 
		for(int i = 0; i < ranges.length - 1; i+=2)
			if(ranges[i] > numFrames)
				ranges[i] = ranges[i + 1] = -1;

		//if a range began before the new valid range (aka its now negative) but becomes valid, shift to the beginning
		for(int i = 0; i < ranges.length - 1; i+=2)
			if(ranges[i] < 1 && ranges[i + 1]>= 1)
				ranges[i] = 1;

		//Move the end of a range that begins in a valid area but ends in a non valid area to the new end
		for(int i = 1; i < ranges.length; i+=2)
			if(ranges[i] > numFrames)
				ranges[i] = numFrames;

		int validCount = 0;
		for(int i = 0; i < ranges.length; i++)
			if(ranges[i] > 0)
				validCount++;

		assert (validCount % 2) == 0;

		if(validCount == 0) //This framespan no longer has any valid ranges
			return "";
		
		int[] fixedRanges = new int[validCount];
		int currIndex = 0;
		for(int i = 0; i < ranges.length - 1; i+=2)
			if(ranges[i] > 0){
				fixedRanges[currIndex] = ranges[i];
				fixedRanges[currIndex + 1] = ranges[i + 1];
				currIndex += 2;
			}


		String fixedString = "framespan=\"";
		for(int i = 0; i < fixedRanges.length - 1; i+= 2)
			fixedString += fixedRanges[i] + ":" + fixedRanges[i+1] + " ";

		fixedString = fixedString.substring(0, fixedString.length() - 1);
		fixedString += "\" ";
		
		return fixedString;
	}
	
	/**
	 * Removes all whitespace-only lines from str
	 * @param str The string to remove lines from
	 * @return The fixed string
	 */
	private static String removeWhiteLines(String str){
		StringReader sReader = new StringReader(str);
		BufferedReader bReader = new BufferedReader(sReader);
		StringWriter sWriter = new StringWriter();
		String lastLine = "";
		try{
			while((lastLine = bReader.readLine()) != null){
				while(lastLine != null && lastLine.matches("\\s*"))//whitespace
					lastLine = bReader.readLine();
				if(lastLine != null)
					sWriter.write(lastLine + "\n");
			}
		} catch(IOException e){
			return str;
		}
		return sWriter.toString();
	}
	
	/**
	 * Generates an array of all the labels currently stored in the mediators markerModel
	 * The strings are formatted as "name (framenumber)", and are sorted based on the label name
	 * @return The array of labels
	 */
	private String[] generateArrayOfLabels(){
		ChronicleMarkerModel markerModel = mediator.getMarkerModel();
		Iterator iter = markerModel.markerIterator();
		Vector vector = new Vector();
		while(iter.hasNext())
			vector.add(iter.next());
		Collections.sort(vector, new ChronicleMarkerComparator(true));
		String[] labelsWithFrames = new String[vector.size()];
		for(int i = 0; i < labelsWithFrames.length; i++){
			ChronicleMarker curr = (ChronicleMarker)vector.get(i);
			labelsWithFrames[i] = curr.getLabel() + " (" + curr.getWhen().intValue() + ")";
		}
		return labelsWithFrames;
	}

	public static void main(String args[]){
		DisplayClipFileDialogActionListener obj = new DisplayClipFileDialogActionListener(null);
		String[] testStrings = {
				"framespan=\"210:300\"",
				"framespan=\"200:350 360:370\"",
				"framespan=\"100:150 200:300\"",
				"framespan=\"100:150 170:220 300:500\"",
				"framespan=\"100:150 170:200\"",
				"framespan=\"100:150 399:500\"",
				"framespan=\"100:150 401:500\""
		};
		
		obj.actualBeginFrame = 200;
		obj.numFrames = 200;
		for(int i = 0; i < testStrings.length; i++)
			System.out.println("Before: " + testStrings[i] + " After: " + obj.fixFramespan(testStrings[i]));
	}
}
