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

import java.util.*;

import viper.api.time.*;

/**
 * A TimeLine is the proposed model node for the time line view. 
 * It is a single node in a directed graph. At first, we will only
 * be using trees, but in the future we will probably want to be able
 * to support DAG structures (to display inheritance of Config files)
 * and generic directed graphs (to support relation attributes).
 * 
 * The basic idea is that all a node cares about is itself. It has a 
 * variety of standard interfaces for visualization: its range, its 
 * title, and its type name (which also has a plural form for 
 * convenience).  
 * 
 * @author davidm
 */
public interface TimeLine {
	/**
	 * Gets the range where the timeline is defined.
	 * @return the range where the timeline is defined
	 */
	public TemporalRange getMyRange();
	
	/**
	 * Gets the title to be used in the display
	 * or debugging of the timeline.
	 * @return the timeline's title
	 */
	public String getTitle();

	/**
	 * Gets neighbors in the timeline graph.
	 * @return the neighbors of the timeline
	 */
	public Iterator getChildren();
	
	/**
	 * The number of timelines in this timeline's adjacency list.
	 * @return the count of neighbor timelines
	 */
	public int getNumberOfChildren();

	/**
	 * Gets the name of the timeline, to be used
	 * in text boxes describing the type of timeline.
	 * @return the name of the timeline type
	 */
	public String getSingularName();
	
	/**
	 * Gets the plural of the timeline name, to be used
	 * in text boxes describing the type of timeline.
	 * @return the plural form of the timeline type name
	 */
	public String getPluralName();
	
	public boolean hasInterpolatedInformation();

	public InstantRange getInterpolatedOverRange();
	
	
}

