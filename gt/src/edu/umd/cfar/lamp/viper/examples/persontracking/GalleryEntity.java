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

import java.util.*;

import viper.api.time.*;

/**
 * Represents a 'unified entity'. For the forseeable future, this is 
 * a 'Person' in a video stream.
 * @author davidm
 */
public interface GalleryEntity {
	public int getId();
	public String getName();
	public String getDisplayName();
	public TemporalRange getRange();
	//public Descriptor getDescriptor();
	public Iterator getEvidence();
	public GalleryEvidence getEvidenceAtFrame(int f);
	public boolean upToDate();
	
	public GalleryEntity setName(String newName);
	public GalleryEntity setValidRange(InstantRange ir);
}
