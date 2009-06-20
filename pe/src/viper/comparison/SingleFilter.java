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

package viper.comparison;

import java.util.*;

import edu.umd.cfar.lamp.viper.util.*;

/**
 * SINGLE-OPTIMUM filter. It uses Hungarian assignment to get the set of
 * target/candidate pairs that minimizes the sum of the distances.
 */
class SingleFilter extends CompFilter {
	
	private static final Longitude costFunctor = new SingleFilter.Longitude();
	
	private static class Longitude implements DataMatrices.GetCost {
		/**
		 * @inheritDoc
		 */
		public long cost(Object obj) {
			return (long) (((Comparison) obj).getDistance() * 0xFFFFFFL);
		}
	}
	
	/**
	 * {@inheritDoc}
	 */
	public Surreal filter(CompMatrix matrix,
			EvaluationParameters.ScopeRules scope) {
		List goodOnes = DataMatrices.assign(matrix.getEdges(), costFunctor);
		for (Iterator iter = goodOnes.iterator(); iter.hasNext();) {
			Comparison curr = (Comparison) iter.next();
			curr.setFilterLevel(CompFilter.SINGLE_OPTIMUM);
		}
		return matrix.getCompleteSum();
	}
}