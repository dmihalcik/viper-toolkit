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

import org.apache.commons.collections.*;

import edu.umd.cs.piccolo.event.*;

public class PersonDetailsView extends javax.swing.JPanel {
	private PersonGalleryModel model;
	private GalleryEntity entity;

	private JTextField personNameField;

	private ScrollableImageTable personRepresentativeImageField;

	private PersonIntervalListView personTimeline;
	
	private JButton ModifyTrack;
	
	public PersonDetailsView(PersonGallery parentGallery) {
		super();
		this.model = parentGallery.getModel();
		setLayout(new BoxLayout(this, BoxLayout.PAGE_AXIS));
		personNameField = new JTextField();
		personNameField.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				String newName = personNameField.getText();
				GalleryEntity curr = model.getSelectedEntity();
				if (curr != null) {
					curr.setName(newName);
				}
			}
		});
		personRepresentativeImageField = new ScrollableImageTable();
		personRepresentativeImageField.setTileSize(parentGallery.getTileSize());
		personRepresentativeImageField.getLayer().addInputEventListener(new PBasicInputEventHandler() {
			public void mouseClicked(PInputEvent event) {
				PVideoAnnotationItem item = VideoAnnotationEventListener.getAnnotationItemBeneath(event);
				model.setSelectedEvidence((GalleryEvidence) item.getClientProperty("evidence"));
			}
		});
		personTimeline = new PersonIntervalListView();
		personTimeline.setGallery(parentGallery);
		
		ModifyTrack=new JButton("Modify Track");
		ModifyTrack.addActionListener(new ModifyTrackActionListener());
		add(personNameField);
		add(personRepresentativeImageField);
		add(new JScrollPane(personTimeline));
//		add(ModifyTrack);
	}
	
	public void resetView() {
		if (!model.isLoaded()) {
			return;
		}
		if (null == entity) {
			return;
		}
		personNameField.setText(entity.getDisplayName());
		personRepresentativeImageField.clearTable();
		model.addEvidenceToImageTable(personRepresentativeImageField, false, new Predicate() {
			public boolean evaluate(Object arg0) {
				GalleryEvidence e = (GalleryEvidence) arg0;
				return e.getEntity().equals(entity);
			}
		}, EvidenceAndEntityComparisons.EVIDENCE_BY_PRIORITY);
		personTimeline.setEntity(entity);
		personTimeline.resetPanels();
	}

	public GalleryEntity getEntity() {
		return entity;
	}

	public void setEntity(GalleryEntity entity) {
		if (this.entity == entity) {
			return;
		}
		this.entity = entity;
		resetView();
	}

	public PersonGalleryModel getModel() {
		return model;
	}

	private class ModifyTrackActionListener implements ActionListener{

		public void actionPerformed(ActionEvent arg0) {
			// TODO Auto-generated method stub
			
		}
	}
	
	}

