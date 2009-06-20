/**
 * 
 */
package edu.umd.cfar.lamp.viper.examples.persontracking;

import javax.swing.*;

import edu.umd.cs.piccolo.event.*;
import edu.umd.cs.piccolo.util.*;

/**
 * Listener to handle clicks on the video annotation items. 
 * @author davidm
 *
 */
final class VideoAnnotationEventListener extends PBasicInputEventHandler {
	/**
	 * 
	 */
	private final PersonGallery gallery;
	private final int wait_for_resort;

	VideoAnnotationEventListener(PersonGallery gallery, int wait_for_resort) {
		super();
		this.gallery = gallery;
		this.wait_for_resort = wait_for_resort;
	}

	public static PVideoAnnotationItem getAnnotationItemBeneath(PInputEvent event) {
		PStack nodes = event.getPath().getNodeStackReference();
		for(int i = 0; i < nodes.size(); i++) {
			Object n = nodes.get(i);
			if (n instanceof PVideoAnnotationItem) {
				return (PVideoAnnotationItem) n;
			}
		}
		return null;
	}
	
	public void mouseClicked(PInputEvent event) {
		PVideoAnnotationItem curr = getAnnotationItemBeneath(event);
		if (curr != null) {
			mouseClickedOn(event, curr);
		}
	}
	
	private void mouseClickedOn(PInputEvent event, PVideoAnnotationItem item) {
		if (gallery.maybeShowPopup(event, item)) {
			return;
		}
		if (!event.isLeftMouseButton()) {
			return;
		}
		this.gallery.applyEvidenceSortCommands = Long.MAX_VALUE;
		GalleryEvidence evidence = (GalleryEvidence) item.getAttribute("evidence");
		if (event.getClickCount() > 1) {
			this.gallery.getModel().addEvidenceForSimilarity(null);
		}
		this.gallery.getModel().addEvidenceForSimilarity(evidence);
		this.gallery.applyEvidenceSortCommands = wait_for_resort/2 + System.currentTimeMillis();
		Timer t = new Timer(wait_for_resort, this.gallery.applyEvidence);
		t.setRepeats(false);
		t.start();
	}

	public void mousePressed(PInputEvent event) {
		PStack nodes = event.getPath().getNodeStackReference();
		for(int i = 0; i < nodes.size(); i++) {
			Object n = nodes.get(i);
			if (n instanceof PVideoAnnotationItem) {
				PVideoAnnotationItem curr = (PVideoAnnotationItem) n;
				mousePressedOn(event, curr);
				return;
			}
		}
	}

	public void mouseReleased(PInputEvent event) {
		PStack nodes = event.getPath().getNodeStackReference();
		for(int i = 0; i < nodes.size(); i++) {
			Object n = nodes.get(i);
			if (n instanceof PVideoAnnotationItem) {
				PVideoAnnotationItem curr = (PVideoAnnotationItem) n;
				mouseReleasedOn(event, curr);
				return;
			}
		}
	}

	private void mousePressedOn(PInputEvent event, PVideoAnnotationItem item) {
		if (gallery.maybeShowPopup(event, item)) {
			return;
		}
		if (!event.isLeftMouseButton()) {
			return;
		}
	}

	private void mouseReleasedOn(PInputEvent event, PVideoAnnotationItem item) {
		if (gallery.maybeShowPopup(event, item)) {
			return;
		}
		if (!event.isLeftMouseButton()) {
			return;
		}
	}
}