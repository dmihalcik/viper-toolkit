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

import edu.umd.cfar.lamp.chronicle.*;
import edu.umd.cfar.lamp.viper.gui.core.*;

/**
 * Generator for attribute segments. Individual attribute typs should subclass this.
 */
public abstract class AbstractAttributeSegmentRenderer
	extends BasicTimeLineRenderer {
	protected ViperViewMediator med = null;
	protected TimeLine tqe = null;

	/** @inheritDoc */
	public void setViewer(ChronicleViewer v) {
		ViperChronicleView vcv = (ViperChronicleView) v;
		med = vcv.getMediator();
	}

	/** @inheritDoc */
	public void setTimeLine(TimeLine tqe) {
		this.tqe = tqe;
	}
}
