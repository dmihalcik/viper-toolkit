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

import viper.descriptors.*;
import viper.descriptors.attributes.*;
import edu.umd.cfar.lamp.viper.util.*;

/**
 * Multiple Matching: This type of filter is designed to try and find
 * descriptors that are split up. It requires that all of the monitored
 * attributes implement the "compose" function, otherwise it reverts to the
 * "Greedy" filter, and there is no exception thown (FIXME?). It alternates
 * between trying to merge
 */
class MultipleMatchFilter extends CompFilter {

	/**
	 * {@inheritDoc}
	 */
	public Surreal filter(CompMatrix matrix,
			EvaluationParameters.ScopeRules scope) {
		try {
			Surreal twoAgo;
			Surreal previousValue;
			Surreal currentValue;

			// Unlike the others, which work by promoting to the
			// match filter level, MULTIPLE_TEST removes the filter
			// level data from those that do not succeed. So, before
			// it begins, we need to set the filtered field of each
			// Comparison in the matrix.
			matrix.removeOld();
			Iterator iter = matrix.getUnorderedIterator();
			while (iter.hasNext()) {
				Comparison curr = (Comparison) iter.next();
				if (!matrix.goodComp(curr)) {

				} else {
					curr.setFilterLevel(CompFilter.MULTIPLE);
				}
			}
			previousValue = helpAnOddTurn(matrix, scope);

			currentValue = helpAnEvenTurn(matrix, scope);

			int iterations = 2;
			do {
				iterations++;

				currentValue = helpAnEvenTurn(matrix, scope);

				twoAgo = previousValue;
				previousValue = currentValue;
				currentValue = ((iterations & 1) == 1) ? helpAnOddTurn(matrix,
						scope) : helpAnEvenTurn(matrix, scope);
			} while (twoAgo.compareTo(currentValue) > 0);
			return currentValue;
		} catch (MethodNotSupportedException mnsx) {
			matrix.log
					.println("[Error] MultipleMatchFilter: There has been an error in determining "
							+ "the appropriate method of cropping."
							+ "\n"
							+ mnsx.getMessage());
			return new GreedyFilter().filter(matrix, scope);
		}
	}

	/**
	 * This maintains the data. desc is the possible match Descriptor, and
	 * offset is its offset in the matrix.
	 */
	private static class DescPair {
		private Comparison comp;
		private Descriptor desc;
		private int offset;
		DescPair(Comparison c, Descriptor d, int index) {
			comp = c;
			desc = d;
			offset = index;
		}
		Comparison getComp() {
			return comp;
		}
		Descriptor getDesc() {
			return desc;
		}
		int getOffset() {
			return offset;
		}
	}

	/**
	 * Need to assign a unique key, so handle collisions is some stupid manner.
	 * Collisions are actually very likely, adding a random offset might make
	 * more sense than this. It must keep the differences down, however, or bad
	 * mojo will result. This way, however, <code>best</code> is assured to be
	 * among those tied for the shortest distance to the ideal.
	 * 
	 * @param pair
	 *            the descriptor pair to add
	 * @param distance
	 *            the distance
	 * @param distanceOrdering
	 *            the ordering of distances
	 * @param matrix
	 *            the matrix
	 */
	private static void addUniqueKeyed(DescPair pair, double distance,
			TreeMap distanceOrdering, CompMatrix matrix) {
		Double key = new Double(distance);
		while (distanceOrdering.containsKey(key))
			key = new Double(Double.longBitsToDouble(1 + Double
					.doubleToLongBits(key.doubleValue())));
		distanceOrdering.put(key, pair);
	}

	/**
	 * This tries to find the best position of ideal within the composition
	 * list. For descriptors that contain composition operators that are not
	 * symmetric. An assymetricly composed attribute could be a string, but
	 * symmetric ones are boxes.
	 * 
	 * @param nextAttempt
	 *            the descriptor to attempt
	 * @param distanceToBeat
	 *            the best distance found so far
	 * @param composition
	 *            the composition so far
	 * @param ideal
	 *            the ideals descriptor
	 * @param matrix
	 *            the comparison matrix
	 * @param idealIsCandidate
	 *            If trying to compose the candidate, set this to true.
	 *            Necessary since not all distance metrics are symmetric,
	 *            either.
	 * @param scope
	 *            the evaluation scoping rules
	 * @return the distance for the given aggregate comparison
	 * @throws MethodNotSupportedException
	 */
	private double helpOrderedComposition(Descriptor nextAttempt,
			double distanceToBeat, LinkedList composition, Descriptor ideal,
			CompMatrix matrix, boolean idealIsCandidate,
			EvaluationParameters.ScopeRules scope)
			throws MethodNotSupportedException {
		int properInsertionPoint = -1;
		int j = 0;
		ListIterator li = composition.listIterator();
		boolean done = !li.hasNext();

		while (!done) {
			li.add(nextAttempt);
			try {
				Descriptor compositionTest = Descriptor.composeThese(
						composition.listIterator(), scope);
				Comparison tempComp;
				if (idealIsCandidate)
					tempComp = new Comparison(compositionTest, ideal, matrix
							.getEquivalencies());
				else
					tempComp = new Comparison(ideal, compositionTest, matrix
							.getEquivalencies());
				Comparison.takeComparisonToThisLevel(tempComp, matrix
						.getCurrentLevel(), matrix.getFileInformation(), scope);
				double newDistance = tempComp.getDistance();
				if (newDistance <= distanceToBeat) {
					properInsertionPoint = j;
					distanceToBeat = newDistance;
				}
			} catch (BadDataException bdx) {
				// FIXME can be thrown for any number of reasons
				// should only ignore the 'multiple compose' ones
				throw new IllegalStateException(bdx.getMessage());
			} catch (UncomposableException ux) {
				throw new MethodNotSupportedException(
						"Cannot compose these descriptors\n\t"
								+ ux.getMessage());
			}
			try {
				li.previous();
				li.remove();
			} catch (NoSuchElementException mnsex) {
				matrix.log.println("[Error] MultipleMatchFilter: "
						+ "Could not remove " + nextAttempt + " from "
						+ composition + " on iteration #" + j);
				System.exit(-2);
			}

			j++;
			done = !li.hasNext();
			if (!done)
				li.next();
		}

		if (properInsertionPoint >= 0)
			composition.add(properInsertionPoint, nextAttempt);
		return distanceToBeat;
	}

	/**
	 * Test to see if this ideal is worth adding to the composition list. Used
	 * for unordered compositions.
	 * 
	 * @param nextAttempt
	 *            the descriptor to attempt
	 * @param distanceToBeat
	 *            the best distance found so far
	 * @param composition
	 *            the composition so far
	 * @param ideal
	 *            the ideals descriptor
	 * @param matrix
	 *            the comparison matrix
	 * @param idealIsCandidate
	 *            If trying to compose the candidate, set this to true.
	 *            Necessary since not all distance metrics are symmetric,
	 *            either.
	 * @param scope
	 *            the evaluation scoping rules
	 * @return the distance for the given aggregate comparison
	 * @throws MethodNotSupportedException
	 */
	private double helpUnorderedComposition(Descriptor nextAttempt,
			double distanceToBeat, LinkedList composition, Descriptor ideal,
			CompMatrix matrix, boolean idealIsCandidate,
			EvaluationParameters.ScopeRules scope)
			throws MethodNotSupportedException {
		composition.add(nextAttempt);
		try {
			Descriptor compositionTest = Descriptor.composeThese(composition
					.listIterator(), scope);
			Comparison tempComp;
			if (idealIsCandidate)
				tempComp = new Comparison(compositionTest, ideal, scope
						.getMap());
			else
				tempComp = new Comparison(ideal, compositionTest, scope
						.getMap());
			Comparison.takeComparisonToThisLevel(tempComp, matrix
					.getCurrentLevel(), matrix.getFileInformation(), scope);
			double newDistance = tempComp.getDistance();
			if (newDistance > distanceToBeat) {
				composition.removeLast();
			}
		} catch (BadDataException bdx) {
			// FIXME ignores errors other than adding the same descriptor twice
			composition.removeLast();
		} catch (UncomposableException ux) {
			throw new MethodNotSupportedException(
					"Cannot compose these descriptors\n\t" + ux.getMessage());
		}
		return distanceToBeat;
	}

	/**
	 * This tries to find the best way to add items from distanceOrdering into
	 * composition.
	 * 
	 * @param distanceOrdering
	 *            the ordering of descriptors
	 * @param distanceToBeat
	 *            the best one so far
	 * @param composition
	 *            the composition
	 * @param ideal
	 *            the ideal descriptor
	 * @param matrix
	 *            the comparison matrix
	 * @param idealIsCandidate
	 *            if the ideal is a candidate descriptor
	 * @param lowestIndex
	 *            the lowest pair index so far
	 * @param scope
	 *            the evaluation scoping rules
	 * @return the number of things actually in theseAreComposed. It is assumed
	 *         that theseAreComposed[0] already contains the first item. This is
	 *         the new lowestIndex
	 * @throws MethodNotSupportedException
	 */
	private int generateComposition(Iterator distanceOrdering,
			double distanceToBeat, LinkedList composition, Descriptor ideal,
			CompMatrix matrix, boolean idealIsCandidate, int lowestIndex,
			EvaluationParameters.ScopeRules scope)
			throws MethodNotSupportedException {
		while (distanceOrdering.hasNext()) {
			DescPair pair = (DescPair) distanceOrdering.next();
			int previousSize = composition.size();
			if (false) {
				distanceToBeat = helpOrderedComposition(pair.getDesc(),
						distanceToBeat, composition, ideal, matrix,
						idealIsCandidate, scope);
			} else {
				distanceToBeat = helpUnorderedComposition(pair.getDesc(),
						distanceToBeat, composition, ideal, matrix,
						idealIsCandidate, scope);
			}
			if ((previousSize < composition.size())
					&& (pair.getOffset() < lowestIndex))
				lowestIndex = pair.getOffset();
		}
		return lowestIndex;
	}

	/**
	 * Targets = first dimenstion of matrix. Candidates = second dimension of
	 * matrix.
	 * 
	 * @param matrix
	 *            the comparison matrix
	 * @param scope
	 *            the evaluation scoping rules
	 * @return the distance so far
	 * @throws MethodNotSupportedException
	 */
	private Surreal helpAnOddTurn(CompMatrix matrix,
			EvaluationParameters.ScopeRules scope)
			throws MethodNotSupportedException {
		TreeMap distanceOrdering = new TreeMap();
		LinkedList thisColumn = new LinkedList();
		Surreal total = new Surreal(matrix.getTargetCount(), 0.0);

		// Set up the distance ordering, so that the composition takes
		// place in the proper order.
		MatrixIterator mi = matrix.getMatrixIterator();
		while (mi.hasNextColumn()) {
			distanceOrdering.clear();
			thisColumn.clear();

			mi.nextColumn();
			double compDistance = Double.POSITIVE_INFINITY;
			Comparison testing = (Comparison) mi.nextInColumn();
			Descriptor idealTarget = testing.T;
			thisColumn.add(testing);
			int j = 0;

			if (matrix.goodComp(testing)) {
				addUniqueKeyed(new DescPair(testing, testing.C, j++), testing
						.getDistance(), distanceOrdering, matrix);
				compDistance = Math.min(testing.getDistance(), compDistance);
			} else {
				// This shouldn't happen, right?
				System.err.println("\n Bad Comparison:  "
						+ testing.getDistance() + "\n" + testing + " T="
						+ testing.T + " C= " + testing.C);
			}
			while (mi.hasNextInColumn()) {
				testing = (Comparison) mi.nextInColumn();
				if (matrix.goodComp(testing)
						&& testing.getFilterLevel() == CompFilter.MULTIPLE) {
					thisColumn.add(testing);
					addUniqueKeyed(new DescPair(testing, testing.C, j++),
							testing.getDistance(), distanceOrdering, matrix);
					compDistance = Math
							.min(testing.getDistance(), compDistance);
				}
			}

			// Next, go through the ordering and try
			// to compose the values.
			Iterator orderedPairs = distanceOrdering.values().iterator();
			if (orderedPairs.hasNext()) {
				int firstPossibleIndex;
				DescPair pair = (DescPair) orderedPairs.next();
				Descriptor best = pair.getDesc();
				firstPossibleIndex = pair.getOffset();

				if (orderedPairs.hasNext()) {
					LinkedList composition = new LinkedList();
					composition.add(best);
					firstPossibleIndex = generateComposition(orderedPairs,
							compDistance, composition, idealTarget, matrix,
							false, firstPossibleIndex, scope);
					Comparison comp = (Comparison) thisColumn
							.remove(firstPossibleIndex);
					Object id = comp.T.getID();
					if ((!(id instanceof Collection) && (composition.size() > 1))
							|| ((id instanceof Collection) && composition
									.size() > ((Collection) id).size())) {
						try {
							Descriptor newC = Descriptor.composeThese(
									composition.listIterator(), scope);
							comp.reset(comp.T, newC, scope.getMap());
							Comparison.takeComparisonToThisLevel(comp, matrix
									.getCurrentLevel(), matrix
									.getFileInformation(), scope);
							comp.setFilterLevel(CompFilter.MULTIPLE);
							total.subtract(1, -comp.getDistance());
						} catch (BadDataException bdx) {
							throw new IllegalStateException(bdx.getMessage());
						} catch (UncomposableException ux) {
							throw new MethodNotSupportedException(
									"Cannot compose these descriptors\n\t"
											+ ux.getMessage());
						}
					}
					Iterator iter = thisColumn.iterator();
					while (iter.hasNext()) {
						((Comparison) iter.next())
								.setFilterLevel(CompFilter.NONE);
					}
				} else {
					total.subtract(1, -pair.getComp().getDistance());
				}
			}// else (no cookie for the candidates because they missed the
			 // idealTarget[i])
		}
		return total;
	}

	private Surreal helpAnEvenTurn(CompMatrix matrix,
			EvaluationParameters.ScopeRules scope)
			throws MethodNotSupportedException {
		TreeMap distanceOrdering = new TreeMap();
		LinkedList thisRow = new LinkedList();
		Surreal total = new Surreal(matrix.getCandidateCount(), 0.0);

		// Set up the distance ordering, so that the composition takes
		// place in the proper order.
		MatrixIterator mi = matrix.getMatrixIterator();
		while (mi.hasNextRow()) {
			distanceOrdering.clear();
			thisRow.clear();

			mi.nextRow(); // Candidate offset
			double compDistance = Double.POSITIVE_INFINITY;
			Comparison testing = (Comparison) mi.nextInRow(); //One we may add
															  // to. Otherwise,
															  // delete.
			Descriptor idealCandidate = testing.C; // == T[y]
			thisRow.add(testing);
			int j = 0;

			if (matrix.goodComp(testing)) {
				addUniqueKeyed(new DescPair(testing, testing.T, j++), testing
						.getDistance(), distanceOrdering, matrix);
				compDistance = Math.min(testing.getDistance(), compDistance);
			} else {
				// This shouldn't happen, right?
				System.err.println("\n Bad Comparison:  "
						+ testing.getDistance() + "\n" + testing + " T="
						+ testing.T + " C= " + testing.C);
			}
			while (mi.hasNextInRow()) {
				testing = (Comparison) mi.nextInRow();
				if (matrix.goodComp(testing)
						&& testing.getFilterLevel() == CompFilter.MULTIPLE) {
					thisRow.add(testing);
					addUniqueKeyed(new DescPair(testing, testing.T, j++),
							testing.getDistance(), distanceOrdering, matrix);
					compDistance = Math
							.min(testing.getDistance(), compDistance);
				}
			}

			// Next, go through the ordering and try
			// to compose the values.
			Iterator orderedPairs = distanceOrdering.values().iterator();
			if (orderedPairs.hasNext()) {
				int firstPossibleIndex;
				DescPair pair = (DescPair) orderedPairs.next();
				Descriptor best = pair.getDesc();
				firstPossibleIndex = pair.getOffset();

				if (orderedPairs.hasNext()) {
					LinkedList composition = new LinkedList();
					composition.add(best);
					firstPossibleIndex = generateComposition(orderedPairs,
							compDistance, composition, idealCandidate, matrix,
							true, firstPossibleIndex, scope);
					Comparison comp = (Comparison) thisRow
							.remove(firstPossibleIndex);
					Object id = comp.T.getID();
					if ((!(id instanceof Collection) && (composition.size() > 1))
							|| ((id instanceof Collection) && composition
									.size() > ((Collection) id).size())) {
						try {
							Descriptor newT = Descriptor.composeThese(
									composition.iterator(), scope);
							comp.reset(newT, comp.C, scope.getMap());
							Comparison.takeComparisonToThisLevel(comp, matrix
									.getCurrentLevel(), matrix
									.getFileInformation(), matrix
									.getScopeRules());
							comp.setFilterLevel(CompFilter.MULTIPLE);
							total.subtract(1, -comp.getDistance());
						} catch (BadDataException bdx) {
							throw new IllegalStateException(bdx.getMessage());
						} catch (UncomposableException ux) {
							throw new MethodNotSupportedException(
									"Cannot compose these descriptors\n\t"
											+ ux.getMessage());
						}
					}
					Iterator iter = thisRow.iterator();
					while (iter.hasNext()) {
						((Comparison) iter.next())
								.setFilterLevel(CompFilter.NONE);
					}
				} else {
					total.subtract(1, -pair.getComp().getDistance());
				}
			}// else (no cookie for the candidates because they missed the
			 // idealTarget[i])
		}
		return total;
	}
}