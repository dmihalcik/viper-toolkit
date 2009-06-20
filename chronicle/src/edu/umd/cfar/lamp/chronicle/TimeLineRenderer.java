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

import edu.umd.cs.piccolo.*;

/**
 * A time node renderer knows how to properly display a timeline.
 * This is equivalent to a cell renderer in a swing table or tree.
 */
public interface TimeLineRenderer {
	/**
	 * Get a label for the specified timeline.
	 * 
	 * @param v the viewer that will take the label
	 * @param tqe the timeline to label
	 * @param isSelected if the line is selected
	 * @param hasFocus if the line is the focus
	 * @param infoLength the number of piccolo pixels available for the label orthogonal
	 * to the line. The number of pixels in the direction of the line can be found from the 
	 * chronicle viewer's label length property.
	 * @param orientation
	 * @return
	 */
	public PNode generateLabel(ChronicleViewer v, TimeLine tqe, boolean isSelected, boolean hasFocus, double infoLength, int orientation);
	
	/**
	 * Constructs a new pnode for the given timeline.
	 * @param chronicle the chronicle the node will be attached to
	 * @param t the timeline
	 * @param isSelected if the timeline is selected by the current selection model
	 * @param hasFocus if the timeline is the current focus
	 * @param timeLength the number of pixels that the timeline subtends
	 * @param infoLength the number of pixels to use to draw the information in the line
	 * @param orientation the direction to draw the node
	 * @return a new node that represents
	 * the given interval of the timeline
	 */
	public PNode getTimeLineRendererNode(ChronicleViewer chronicle, TimeLine t, boolean isSelected, boolean hasFocus, double timeLength, double infoLength, int orientation);
	
	/**
	 * Gets the preferred information width for the given timeline and parameters.
	 * @param chronicle the chronicle the node will be attached to
	 * @param t the timeline
	 * @param isSelected if the timeline is selected by the current selection model
	 * @param hasFocus if the timeline is the current focus
	 * @param timeLength the number of pixels the extents of the timeline subtends
	 * @param orientation the direction to draw the node
	 * @return a new node that represents
	 * the given interval of the timeline
	 */
	public double getPreferedTimeLineInfoLength(ChronicleViewer chronicle, TimeLine t, boolean isSelected, boolean hasFocus, double timeLength, int orientation);
}
