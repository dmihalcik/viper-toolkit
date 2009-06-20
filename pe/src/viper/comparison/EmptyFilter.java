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

/**
 * Empty filter, it is just a placeholder. It doesn't change the matrix,
 */
class EmptyFilter extends CompFilter {
	/**
	 * The null filter.
	 * {@inheritDoc}
	 */
	public Surreal filter(CompMatrix matrix,
			EvaluationParameters.ScopeRules scope) {
		return matrix.getCompleteSum();
	}
}