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


import java.awt.*;

import viper.api.*;
import edu.umd.cfar.lamp.chronicle.*;
import edu.umd.cfar.lamp.chronicle.extras.emblems.*;
import edu.umd.cfar.lamp.viper.gui.core.*;
import edu.umd.cfar.lamp.viper.util.*;
import edu.umd.cs.piccolo.*;

/**
 */
public class ViperEnhancedTimeLineRenderer extends EmblemedTimeLineRenderer {
	private static final ShapeDisplayProperties normalStyle = new BasicShapeDisplayProperties(ColorUtilities.getColor("black"), new BasicStroke(1), ColorUtilities.changeAlpha(ColorUtilities.getColor("snow"), .9));
	private static final ShapeDisplayProperties selectedStyle = new BasicShapeDisplayProperties(ColorUtilities.getColor("black"), new BasicStroke(2), ColorUtilities.changeAlpha(ColorUtilities.getColor("powderblue"), .75));
	private static final ShapeDisplayProperties propagatingStyle = new BasicShapeDisplayProperties(ColorUtilities.getColor("black"), new BasicStroke(1), ColorUtilities.changeAlpha(ColorUtilities.getColor("salmon"), .75));
	private static final ShapeDisplayProperties bothStyle = new BasicShapeDisplayProperties(ColorUtilities.getColor("black"), new BasicStroke(2), ColorUtilities.changeAlpha(ColorUtilities.getColor("salmon"), .75));
	
	public PNode generateLabel(ChronicleViewer v, TimeLine tqe,
			boolean isSelected, boolean hasFocus, double infoLength,
			int orientation) {
		PNode n = super.generateLabel(v, tqe, isSelected, hasFocus, infoLength,
				orientation);
		return n;
	}
	public PNode getTimeLineRendererNode(ChronicleViewer chronicle, TimeLine t,
			boolean isSelected, boolean hasFocus, double timeLength,
			double infoLength, int orientation) {
		PNode n = super.getTimeLineRendererNode(chronicle, t, isSelected,
				hasFocus, timeLength, infoLength, orientation);
		if (t instanceof ViperNodeTimeLine) {
			SegmentedTimeLineView stlv = (SegmentedTimeLineView) n;
			ViperNodeTimeLine vtl = (ViperNodeTimeLine) t;
			ViperChronicleModel m = vtl.getViewModel();
			ViperViewMediator mediator = m.getMediator();
			ViperChronicleSelectionModel sm = vtl.getSelectionModel();
			boolean selected = (sm != null && sm.isSelected(vtl));
			boolean propagating = false;
			if (vtl instanceof VDescriptorTimeLine) {
				Descriptor d = ((VDescriptorTimeLine) vtl).getDescriptor();
				if (mediator.getPropagator().isPropagatingThis(d)) {
					propagating = true;
				}
			}
			if (propagating) {
				stlv.setBackgroundStyle(selected ? bothStyle : propagatingStyle);
			} else if (selected) {
				stlv.setBackgroundStyle(selectedStyle);
			} else {
				stlv.setBackgroundStyle(normalStyle);
			}
		}
		return n;
	}
}
