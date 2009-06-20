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
 * Classes that implement this take in a CompMatrix and upgrade all comparisons
 * that meet a certain criterion.
 */
abstract public class CompFilter {
	/**
	 * Don't do any match filtering.
	 */
	public static final int NONE = 0;

	/**
	 * Match filtering 1 to 1, adding in a linear fashion. Basically, go through
	 * and grabs the first best one for each row.
	 * 
	 * @see CompMatrix#removeDuplicates(int)
	 */
	public static final int SINGLE_GREEDY = 1;

	/**
	 * Match filtering 1 to 1, checking all possible permutation matrices.
	 * 
	 * @see CompMatrix#removeDuplicates(int)
	 */
	public static final int SINGLE_OPTIMUM = 2;

	/**
	 * This method of MultiMatching operates even and oddly stepping through
	 * options and trying to find a nice way of combining the candidates and
	 * targets to get the most general set of aggregate descriptors. That is, if
	 * the target says there are two columns, and the candidates say there are
	 * two rows, they will all merge to non-contiguous shapes that mark the
	 * basic area. The figure below shows this case:
	 * 
	 * <pre>
	 * 
	 * 
	 *          Target           Candidate
	 *       +----------+     +----+  +---+
	 *       | T1       |     | C1 |  |C2 |
	 *       +----------+     |    |  |   |
	 *                        |    |  |   |
	 *       +----------+     |    |  |   |
	 *       | T2       |     |    |  |   |
	 *       +----------+     +----+  +---+
	 *  
	 *    
	 *    	            Candidates   
	 *               |  C1  |  C2  | C1+C2 |
	 *      T  ------+------+------+-------+
	 *      a    T1  | 0.62 | 0.58 |  0.47 |
	 *      r  ------+------+------+-------+
	 *      g    T2  | 0.62 | 0.58 |  0.47 |
	 *      e  ------+------+------+-------+
	 *      t  T1+T2 | 0.47 | 0.44 |  0.19 |
	 *      s  ------+------+------+-------+
	 *    (Distances are 1 - Dice Coefficient)
	 * 
	 *  
	 * </pre>
	 *  
	 */
	public static final int MULTIPLE = 3;

	/**
	 * Names of the matching types, e.g. <q>Single Optimum</q> or 
	 * <q>Multiple</q>.
	 */
	public static final String[] name = {"NONE          ", "SINGLE_GREEDY ",
			"SINGLE_OPTIMUM", "MULTIPLE      "};

	/**
	 * Get the name of the matching type.
	 * @param filterType the level number for the matching type
	 * @return the name
	 */
	public static final String matchFilterTitle(int filterType) {
		if ((filterType < 0) || (filterType > name.length - 1))
			return ("BAD FILTERING ");
		else
			return name[filterType];
	}

	/**
	 * Gets the Cropping Type identified by the given string.
	 * 
	 * @param S
	 *            one of SINGLE-GREEDY, SINGLE-BEST, MULTIPLE or NONE
	 * @return the constant associated with the type name
	 * @throws IllegalArgumentException
	 *             if it isn't a recognized cropping type
	 */
	public static final int getCroppingType(String S) {
		if (S.equalsIgnoreCase("SINGLE") || S.equalsIgnoreCase("SINGLE-GREEDY")
				|| S.equalsIgnoreCase("SINGLE_GREEDY"))
			return CompFilter.SINGLE_GREEDY;
		else if ((S.equalsIgnoreCase("SINGLE-OPTIMUM"))
				|| (S.equalsIgnoreCase("SINGLE-OPTIMAL"))
				|| (S.equalsIgnoreCase("SINGLE-BEST")))
			return CompFilter.SINGLE_OPTIMUM;
		else if (S.equalsIgnoreCase("MULTI-BEST")
				|| S.equalsIgnoreCase("MULTIPLE"))
			return CompFilter.MULTIPLE;

		else if ((S.equalsIgnoreCase("NONE")) || (S.equalsIgnoreCase("ALL"))
				|| (S.equalsIgnoreCase("DEFAULT")))
			return CompFilter.NONE;
		else
			throw new IllegalArgumentException("Invalid cropping type: " + S);
	}

	/**
	 * Maps from matching type level numbers to function objects. 
	 */
	public static final CompFilter[] filterSelect = {new EmptyFilter(),
			new GreedyFilter(), new SingleFilter(), new MultipleMatchFilter()};

	/**
	 * Runs the match filter on the set of comparisons.
	 * @param matrix the comparisons to filter
	 * @param scope the rules and measurements to use
	 * @return the distance score
	 */
	public abstract Surreal filter(CompMatrix matrix,
			EvaluationParameters.ScopeRules scope);
}