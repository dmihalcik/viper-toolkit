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


package edu.umd.cfar.lamp.chronicle;

import javax.swing.*;

import edu.umd.cs.piccolo.*;
import edu.umd.cs.piccolo.nodes.*;

abstract public class AdapterForTimeLineRenderer implements TimeLineRenderer {
	private int preferredInfoLength = -1;

	public double getPreferedTimeLineInfoLength(ChronicleViewer chronicle,
			TimeLine t, boolean isSelected, boolean hasFocus, double timeLength,
			int orientation) {
		if (this.preferredInfoLength <= 0) {
			PNode test = this.getTimeLineRendererNode(chronicle, t, isSelected, hasFocus, timeLength, 0, orientation);
			if (test == null) {
				return 0;
			}
			if (orientation == SwingConstants.HORIZONTAL) {
				return test.getFullBounds().height;
			}
			return test.getFullBounds().width;
		}
		return this.preferredInfoLength;
	}

	public void setPreferredTimeLineInfoLength(int l) {
		this.preferredInfoLength = l;
	}
	
	public PNode generateLabel(final ChronicleViewer v, final TimeLine tqe,
			boolean isSelected, boolean hasFocus, double infoLength,
			int orientation) {
		PText label = new PText(tqe.getTitle());
		label.setConstrainHeightToTextHeight(false);
		label.setConstrainWidthToTextWidth(false);
		return label;
	}
}
