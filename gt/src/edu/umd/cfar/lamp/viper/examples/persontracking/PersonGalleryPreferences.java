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


package edu.umd.cfar.lamp.viper.examples.persontracking;

import java.awt.event.*;

import javax.swing.*;
import javax.swing.border.*;

public class PersonGalleryPreferences extends JPanel {
	private JCheckBox allowSimilarityCalculationCheckBox;
	private JCheckBox allowTrackingCheckBox;
	private JCheckBox displayThumbnailsInTimeLineCheckBox;
	private JCheckBox groupByPersonCheckBox;
	
	private PersonGallery gallery;
	private JPanel displayOptionsPanel;
	private JPanel visionOptionsPanel;
	
	public PersonGalleryPreferences() {
		super();
		
		// create checkboxes and register listener with them
		allowSimilarityCalculationCheckBox = new JCheckBox("Use Image Similarity Measures");
		allowSimilarityCalculationCheckBox.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				gallery.setSimilarityEnabled(allowSimilarityCalculationCheckBox.getModel().isSelected());
			}
		});
		allowTrackingCheckBox = new JCheckBox("Use Object Tracking");
		allowTrackingCheckBox.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				gallery.setTrackingEnabled(allowTrackingCheckBox.getModel().isSelected());
			}
		});
		displayThumbnailsInTimeLineCheckBox = new JCheckBox("Display Thumbnails in Timeline");
		displayThumbnailsInTimeLineCheckBox.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				gallery.setDisplayThumbsInTimeline(displayThumbnailsInTimeLineCheckBox.getModel().isSelected());
			}
		});
		
		// do layout
		setLayout(new BoxLayout(this, BoxLayout.PAGE_AXIS));
		
		visionOptionsPanel = new JPanel();
		visionOptionsPanel.setLayout(new BoxLayout(visionOptionsPanel, BoxLayout.LINE_AXIS));
		visionOptionsPanel.add(allowSimilarityCalculationCheckBox);
		visionOptionsPanel.add(allowTrackingCheckBox);
		TitledBorder smartsTitle;
		smartsTitle = BorderFactory.createTitledBorder("Machine Vision Assistance");
		visionOptionsPanel.setBorder(smartsTitle);
		add(visionOptionsPanel);
		
		displayOptionsPanel = new JPanel();
		displayOptionsPanel.setLayout(new BoxLayout(displayOptionsPanel, BoxLayout.LINE_AXIS));
		displayOptionsPanel.add(displayThumbnailsInTimeLineCheckBox);
		TitledBorder displayTitle;
		displayTitle = BorderFactory.createTitledBorder("Display Preferences");
		displayOptionsPanel.setBorder(displayTitle);
		add(displayOptionsPanel);
	}
	
	public boolean isSimilarityCalculationAllowed() {
		return allowSimilarityCalculationCheckBox.getModel().isSelected();
	}
	public boolean isTrackingAllowed() {
		return allowTrackingCheckBox.getModel().isSelected();
	}
	public boolean isDisplayThumbnailsInTimeLine() {
		return displayThumbnailsInTimeLineCheckBox.getModel().isSelected();
	}

	public PersonGallery getGallery() {
		return gallery;
	}

	public void setGallery(PersonGallery gallery) {
		if (this.groupByPersonCheckBox != null) {
			displayOptionsPanel.remove(this.groupByPersonCheckBox);
		}
		this.gallery = gallery;
		allowSimilarityCalculationCheckBox.getModel().setSelected(gallery.isSimilarityEnabled());
		allowTrackingCheckBox.getModel().setSelected(gallery.isTrackingEnabled());
		displayThumbnailsInTimeLineCheckBox.getModel().setSelected(gallery.isDisplayThumbsInTimeline());
		this.groupByPersonCheckBox = gallery.getGroupByPersonCheckBox();
		if (this.groupByPersonCheckBox != null) {
			displayOptionsPanel.add(this.groupByPersonCheckBox);
		}
		displayOptionsPanel.validate();
	}
}
