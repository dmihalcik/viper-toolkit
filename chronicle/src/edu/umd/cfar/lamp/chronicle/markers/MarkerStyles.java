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

package edu.umd.cfar.lamp.chronicle.markers;

import java.awt.*;

import edu.umd.cs.piccolo.*;

/**
 * Defines how markers should be drawn.
 */
public interface MarkerStyles {
	/**
	 * Gets the style for the given marker.
	 * @param label the label of the marker
	 * @return the style associated with the label
	 */
	public StyleForLabel getStyleForLabel(String label);
	
	/**
	 * Defines how to draw a marker, given the label.
	 */
	public static interface StyleForLabel {
		/**
		 * Gets the color for filling the flag/footer, if 
		 * required.
		 * @return the preferred style for filling shapes. Is used
		 * as a suggestion for the header and footer.
		 */
		public Paint getFillStyle();
	
		/**
		 * Gets the color for drawing the line.
		 * @return the preferred style for drawing. Is used
		 * as a suggestion for the header and footer as to how
		 * to draw, as well.
		 */
		public Paint getLineStyle();
	
		/**
		 * Gets a new node to act as a header. This
		 * is rendered above the line in the ruler.
		 * @return the header node
		 */
		public PNode createHeaderNode();
	
		/**
		 * Gets a new node to act as a footer. This
		 * is rendered below the last line.
		 * @return the footer node
		 */
		public PNode createFooterNode();
	}

}
