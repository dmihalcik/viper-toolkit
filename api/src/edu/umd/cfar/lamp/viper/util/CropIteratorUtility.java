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


package edu.umd.cfar.lamp.viper.util;

import java.util.*;

import org.apache.commons.collections.*;
import org.apache.commons.collections.iterators.*;

/**
 * Class for clamping an Iterator of Interval objects
 * to just return intervals that fall in the given span.
 * This is not the most efficient way to do things, especially
 * when the span is much smaller than the iterated interval.
 */
public class CropIteratorUtility {
	private Interval i;
	Predicate pred;
	Transformer trans;
	public CropIteratorUtility(Interval i) {
		this.i = i;
		this.pred = new IntersectsThisFilter();
		this.trans = new IntersectionTransformer();
	}
	public Iterator getIterator(Iterator iter) {
		return new TransformIterator(new FilterIterator(iter, pred), trans);
	}
	private class IntersectsThisFilter implements Predicate {
		public boolean evaluate(Object o) {
			Interval toCheck = (Interval) o;
			return i.intersects(toCheck);
		}
		
	}
	private class IntersectionTransformer implements Transformer {
		public Object transform(Object i) {
			return Intervals.intersection((Interval) i, CropIteratorUtility.this.i);
		}
	}
}