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

import java.awt.geom.*;
import java.util.*;

import org.apache.commons.collections.*;
import org.apache.commons.collections.iterators.*;

import edu.umd.cs.piccolo.*;
import edu.umd.cs.piccolo.util.*;

/**
 * Do layout. Each child either with a client property 'goal'
 * will try to stay as close to that Point2D as possible. 
 * Otherwise shapes follow a generic boid algorithm to find 
 * free space on the plane. The algorithm for layout is iterative.
 * First, shapes are placed with slightly random offsets, then
 * the boid algorithm takes over.
 * @author davidm
 */
public class HoverForceLayout extends PNode {
	private final class NodeIsNear implements Predicate {
		private Rectangle2D globalBoundsToTest;
		
		public boolean evaluate(Object otherNode) {
			assert otherNode instanceof PNode;
			Rectangle2D aBounds = (Rectangle2D) puffyGlobalBoundsCache.get(otherNode);
			return aBounds.intersects(globalBoundsToTest);
		}

		public void setGlobalBoundsToTest(Rectangle2D globalBoundsToTest) {
			this.globalBoundsToTest = globalBoundsToTest;
		}
	}

	private double preferredMargin = 4;
	private double decay = .3;
	private int maxIterations = 100;
	private double searchRadius = 16;
	
	
	private NodeIsNear near = new NodeIsNear();
	private Map globalBoundsCache = new HashMap();
	private Map puffyGlobalBoundsCache = new HashMap();
	
	protected void layoutChildren() { 
		resetBoundsCache();
		double[] newOffsetsX = new double[getChildrenCount()];
		double[] newOffsetsY = new double[getChildrenCount()];
		Iterator i = getChildrenIterator();
		while (i.hasNext()) {
			PNode each = (PNode) i.next();
			near.setGlobalBoundsToTest((Rectangle2D) puffyGlobalBoundsCache.get(each));
			Iterator thoseNear = new FilterIterator(getChildrenIterator(), near);
			PBounds myBounds = (PBounds) globalBoundsCache.get(each);
			while (thoseNear.hasNext()) {
				PNode other = (PNode) thoseNear.next();
				PBounds yourBounds =  (PBounds) globalBoundsCache.get(other);
				if (myBounds.intersects(yourBounds)) {
					
				}
			}
		}
		for (int idx = 0; idx < newOffsetsX.length; idx++) {
			getChild(idx).setOffset(newOffsetsX[idx], newOffsetsY[idx]);
		}
	}

	/**
	 * 
	 */
	private void resetBoundsCache() {
		puffyGlobalBoundsCache.clear();
		globalBoundsCache.clear();
		
		Iterator j = getChildrenIterator();
		while (j.hasNext()) {
			PNode n = (PNode) j.next();
			PBounds b = n.getGlobalBounds();
			globalBoundsCache.put(n, b);
			puffyGlobalBoundsCache.put(n, new Rectangle2D.Double(b.x - searchRadius, b.y - searchRadius, b.width + searchRadius*2, b.height + searchRadius*2));
		}
	}
}
