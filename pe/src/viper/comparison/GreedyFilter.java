/*******************************************************************************
 * ViPER * The Video Processing * Evaluation Resource * * Distributed under the
 * GPL license * Terms available at gnu.org. * * Copyright University of
 * Maryland, * College Park. *
 ******************************************************************************/

package viper.comparison;

import java.util.*;

import edu.umd.cfar.lamp.viper.util.*;

class GreedyFilter extends CompFilter {
	private static class CompSorter implements Comparator {
		/**
		 * {@inheritDoc}
		 */
		public int compare(Object a, Object b) {
			return compare((Comparison) a, (Comparison) b);
		}

		private int compare(Comparison a, Comparison b) {
			if (a == b) {
				return 0;
			} else if (a == null) {
				return 2; // a is not a match, so its distance is infinite
			} else if (b == null) {
				return -2;
			} else {
				double A = a.getDistance();
				double B = b.getDistance();
				return (A == B) ? 0 : (A < B) ? -1 : 1;
			}
		}

		/**
		 * {@inheritDoc}
		 */
		public boolean equals(Object o) {
			return (o instanceof CompSorter);
		}
	}

	private static final Comparison[] helpGetSortedComparisons(CompMatrix matrix) {
		MatrixIterator mi = matrix.getMatrixIterator();
		List remaining = new LinkedList();
		while (mi.hasNextColumn()) {
			mi.nextColumn();
			while (mi.hasNextInColumn()) {
				Comparison current = (Comparison) mi.nextInColumn();
				if (matrix.goodComp(current)) {
					remaining.add(current);
				}
			}
		}
		Comparison[] A = (Comparison[]) remaining
				.toArray(new Comparison[remaining.size()]);
		Arrays.sort(A, new CompSorter());
		return A;
	}

	/**
	 * Takes the best match for each target.
	 * {@inheritDoc}
	 */
	public Surreal filter(CompMatrix matrix,
			EvaluationParameters.ScopeRules scope) {
		Comparison[] A = helpGetSortedComparisons(matrix);

		Set targets = new HashSet();
		Set candidates = new HashSet();
		LinkedList goodComps = new LinkedList();
		int shorter = Math.min(matrix.T.size(), matrix.C.size());
		int longer = Math.max(matrix.T.size(), matrix.C.size());
		Surreal total = new Surreal();
		for (int i = 0; i < A.length && goodComps.size() < shorter; i++) {
			if (!targets.contains(A[i].T) && !candidates.contains(A[i].C)) {
				goodComps.add(A[i]);
				targets.add(A[i].T);
				candidates.add(A[i].C);
				total.add(A[i].getDistance());
				A[i].setFilterLevel(CompFilter.SINGLE_GREEDY);
			}
		}
		total.add(longer - goodComps.size(), 0.0);

		return total;
	}
}