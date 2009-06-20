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

package edu.umd.cfar.lamp.viper.gui.chronology;

import java.awt.event.*;
import java.io.*;
import java.net.*;

import javax.swing.*;

import viper.api.*;

import com.hp.hpl.jena.rdf.model.*;

import edu.umd.cfar.lamp.apploader.prefs.*;
import edu.umd.cfar.lamp.chronicle.*;
import edu.umd.cfar.lamp.chronicle.markers.*;
import edu.umd.cfar.lamp.viper.gui.core.*;

/**
 * @author davidm
 */
public class ViperChronicleView extends ChronicleViewer {
	private Action removeAllMarksActionListener = new RemoveAllMarksAction();
	
	public void setRef(Resource ref) {
		ViperRendererCatalogue cat = (ViperRendererCatalogue) getRendererCatalogue();
		ViperTimelineEmblemModel em = (ViperTimelineEmblemModel) cat.getEmblemModel();
		if (ref != null) {
			Statement s = ref.getProperty(CHRONO.expandIcon);
			String dir;
			if (s != null) {
				dir = s.getString();
				em.setExpandIcon(new ImageIcon(dir).getImage());
			}
			s = ref.getProperty(CHRONO.contractIcon);
			if (s != null) {
				dir = s.getString();
				em.setContractIcon(new ImageIcon(dir).getImage());
			}
			
			s = ref.getProperty(GT.playbackSelectedIcon);
			if (s != null) {
				dir = s.getString();
				em.setPlaybackSelectedIcon(new ImageIcon(dir).getImage());
			}
			s = ref.getProperty(GT.playbackUnselectedIcon);
			if (s != null) {
				dir = s.getString();
				em.setPlaybackUnselectedIcon(new ImageIcon(dir).getImage());
			}
		} else {
			em.setExpandIcon(null);
			em.setContractIcon(null);
		}
	}
	
	/**
	 * Creates a new ChronicleViewer, using the mediator as the data model.
	 * @param mediator
	 * @param ref the reference preference resource
	 * @throws IOException
	 */
	public ViperChronicleView(ViperViewMediator mediator, Resource ref) throws IOException {
		super();
		ViperChronicleModel m = new ViperChronicleModel();
		m.setSelectionModel(new ViperChronicleSelectionModel());
		ViperRendererCatalogue cat = new ViperRendererCatalogue();
		cat.setEmblemModel(new ViperTimelineEmblemModel(m, m.getSelectionModel()));
		setRendererCatalogue(cat);
		super.setSelectionModel(m.getSelectionModel());
		m.setMediator(mediator);
		m.getSelectionModel().setMediator(mediator);
		setModel(m);
		setRef(ref);
	}

	private DescriptorTimeDragHandler dragDescTime;
	
	/**
	 * Creates a new ChronicleViewer.
	 */
	public ViperChronicleView() {
		super();
		ViperChronicleModel m = new ViperChronicleModel();
		m.setSelectionModel(new ViperChronicleSelectionModel());
		super.setSelectionModel(m.getSelectionModel());
		ViperRendererCatalogue cat = new ViperRendererCatalogue();
		cat.setEmblemModel(new ViperTimelineEmblemModel(m, m.getSelectionModel()));
		setRendererCatalogue(cat);
		setModel(m);
		dragDescTime = new DescriptorTimeDragHandler();
		dragDescTime.setViewer(this);
		addTimeLineInputEventListener(dragDescTime);
	}

	public static void printUsage() {
		System.err.println("Usage: viperview <metadata.xml>");
	}

	public ActionListener getFitChronicleActionListener() {
		return new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				fitInWindow();
			}
		};
	}

	/**
	 * Create a simple view of the viper file passed in 
	 * the command line argument. Attempts to create a
	 * view of the first sourcefile found in the metadatafile,
	 * searching the user's file system if necessary.
	 * @param args the command line arguments
	 * @throws URISyntaxException
	 */
	public static void main(String[] args) throws URISyntaxException {
		if (args.length != 2) {
			System.err.println("Args.length == " + args.length);
			printUsage();
			System.exit(2);
		}
		try {
			URI fileURI = new File(args[0]).toURI();
			URI prefsURI = new File(args[1]).toURI();
			PrefsManager prefs = new PrefsManager();
			prefs.setSystemPrefs(prefsURI);
			ViperViewMediator mediator = new ViperViewMediator();
			mediator.setPrefs(prefs);

			mediator.setFileName(fileURI);
			String firstFile =
				((Sourcefile) mediator
					.getViperData()
					.getAllSourcefiles()
					.get(0))
					.getReferenceMedia()
					.getSourcefileName();
			mediator.setFocalFile(new URI(firstFile));

			ViperChronicleView chronoView = new ViperChronicleView();
			//

			JFrame f = new JFrame();
			f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
			f.getContentPane().add(chronoView);
			f.pack();
			f.show();

//
//			((ViperChronicleModel) chronoView.getModel()).expandAll();
//
//			AgileJFrame container = new AgileJFrame("Viper Data View");
//			container.addWindowListener(new WindowAdapter() {
//				public void windowClosing(WindowEvent e) {
//					System.exit(0);
//				}
//			});
//			container.setSize(chronoView.getPreferredSize());
//			System.err.println ("Preferred size: " + chronoView.getPreferredSize());
//			container.getContentPane().add(chronoView);
//			container.validate();
//			chronoView.requestFocus();
//			container.setVisible(true);
//			System.out.println("here");
		} catch (IOException iox) {
			iox.printStackTrace();
		} catch (PreferenceException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	private ViperChronicleModel getViperChronicle() {
		return (ViperChronicleModel) this.getModel();
	}

	public void setMediator(ViperViewMediator med) {
//		getViperChronicle().getMediator().setMarkerModel(new DefaultChronicleMarkerModel());
		getViperChronicle().setMediator(med);
		((ViperChronicleSelectionModel) getSelectionModel()).setMediator(med);
		getViperChronicle().getMediator().setMarkerModel(this.getMarkerModel());
		if (getRendererCatalogue() != null) {
			ViperRendererCatalogue mtsm = (ViperRendererCatalogue) getRendererCatalogue();
			mtsm.setMediator(med);
		}
	}
	public ViperViewMediator getMediator() {
		return getViperChronicle().getMediator();
	}
	
	public ActionListener getExpandAllActionListener() {
		return new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				((ViperChronicleModel) getModel()).expandAll();
			}
		};
	}
	
	/**
	 * Gets an action that removes all the user-set marks on the timeline.
	 * @return an action to remove the user marks
	 */
	public Action getRemoveAllMarksActionListener() {
		if (removeAllMarksActionListener == null) {
			removeAllMarksActionListener = new RemoveAllMarksAction();
		}
		return removeAllMarksActionListener;
	}
	
	
	public void resetMarkers() {
		super.resetMarkers();
		boolean moreThanDefaultMarker = getMarkerModel() != null && getMarkerModel().getSize() > 1;
		getRemoveAllMarksActionListener().setEnabled(moreThanDefaultMarker);
	}
	private class RemoveAllMarksAction extends AbstractAction {
		public RemoveAllMarksAction() {
			setEnabled(false);
		}
		public void actionPerformed(ActionEvent e) {
			ChronicleMarkerModel mm = getMarkerModel();
			String[] L = new String[mm.getLabels().size()];
			L = (String[]) mm.getLabels().toArray(L);
			for (int i = 0; i < L.length; i++) {
				if (!L[i].equals(ChronicleViewer.CURR_FRAME_LABEL)) {
					mm.removeMarkersWithLabel(L[i]);
				}
			}
		}
	};
}
