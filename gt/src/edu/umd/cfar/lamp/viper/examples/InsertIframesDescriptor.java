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


package edu.umd.cfar.lamp.viper.examples;

import java.awt.event.*;
import java.util.*;

import javax.swing.*;

import viper.api.*;
import viper.api.time.*;
import edu.umd.cfar.lamp.viper.gui.core.*;
import edu.umd.cfar.lamp.viper.gui.players.*;

/**
 * A helper class which inserts an 'iframe' descriptor into 
 * the currently loaded file. This is useful for editing purposes,
 * and for only evaluating on the iframe. Requires a mediator
 * to be loaded with a viperdata file, and for the dataplayer
 * to properly implement the getImageType method.
 */
public class InsertIframesDescriptor {
	static final String DESCRIPTOR_NAME = "I-Frames";
	private ViperViewMediator mediator;
	
	/**
	 * Creates a new type of descriptor, called 'I-Frames',
	 * if it doesn't already exist. Otherwise, locates it.
	 * @return the I-Frame descriptor config information
	 */
	public Config insertIFrameDescriptorConfig() {
		ViperData V = mediator.getViperData();
		Config c = V.getConfig(Config.OBJECT, DESCRIPTOR_NAME);
		if (c == null) {
			c = V.createConfig(Config.OBJECT, DESCRIPTOR_NAME);
		}
		return c;
	}
	
	/**
	 * Inserts a new I-Frame descriptor into the currently selected
	 * file, using the mediator's current DataPlayer object to  
	 * find where the iframes are.
	 */
	public void insertIFrameDescriptor() {
		Config c = insertIFrameDescriptorConfig();
		Sourcefile sf = mediator.getCurrFile();
		if (sf != null) {
			Descriptor d = null;
			Iterator allIFrames = sf.getDescriptorsBy(c);
			if (allIFrames.hasNext()) {
				d = (Descriptor) allIFrames.next();
			} else {
				d = sf.createDescriptor(c);
			}
			InstantRange iframes = new InstantRange();
			DataPlayer p = mediator.getDataPlayer();
			InstantInterval frameSpan = p.getRate().asFrame(p.getSpan());
			for (Iterator frames = frameSpan.iterator(); frames.hasNext(); ) {
				Frame f = (Frame) frames.next();
				if (DataPlayer.I_FRAME.equals(p.getImageType(f))) {
					iframes.add(f);
				}
			}
			d.setValidRange(iframes);
		}
	}
	
	private Action addIframeDescriptorActionListener = new AbstractAction() {
		public void actionPerformed(ActionEvent e) {
			insertIFrameDescriptor();
		}
	};

	/**
	 * @return Returns the mediator.
	 */
	public ViperViewMediator getMediator() {
		return mediator;
	}

	/**
	 * @param mediator The mediator to set.
	 */
	public void setMediator(ViperViewMediator mediator) {
		this.mediator = mediator;
	}
	/**
	 * @return Returns the addIframeDescriptorActionListener.
	 */
	public Action getAddIframeDescriptorAction() {
		return addIframeDescriptorActionListener;
	}
}
