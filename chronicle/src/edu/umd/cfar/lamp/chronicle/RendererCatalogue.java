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


/**
 * Given a ChronicleViewer and a TimeLine, this generates a 
 * SegmentFactory to create the segments for any given Interval related
 * to the larger line (represented by TimeLine). For example,
 * the factory might create a SegmentFactory that converts Bbox 
 * objects to different width bars when it sees a 'bbox' attribute,
 * and another segment factory that is appropriate for a given 
 * descriptor. 
 * 
 * Now I just have to think of a pretext for a 
 * <code>PTimeSegmentFactoryFactoryFactory</code>. Hmmmm....
 * 
 *  
 * @author davidm
 */
public interface RendererCatalogue {
	/**
	 * Select the appropriate renderer for a the TimeLine object tqe.
	 * 
	 * @param tqe the model on the factory will be asked to create segments
	 *    for, or a possible such model. 
	 * @return a new time segment factory, for giving a time line view 
	 *    its nifty style
	 */
	public TimeLineRenderer getTimeLineRenderer(TimeLine tqe);
	
	/**
	 * Set the default renderer for all TimeLine objects that aren't otherwise
	 * found out.
	 * 
	 * @param tqe the model on the factory will be asked to create segments
	 *    for, or a possible such model. 
	 * @return a new time segment factory, for giving a time line view 
	 *    its nifty style
	 */
	public void setDefaultTimeLineRenderer(TimeLineRenderer renderer);
}
