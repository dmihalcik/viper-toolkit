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

import java.io.*;
import java.text.*;
import java.util.*;

import viper.comparison.distances.*;
import viper.descriptors.*;
import viper.descriptors.attributes.*;
import edu.umd.cfar.lamp.viper.util.*;

/**
 * A collection of Descriptor Objects that supports various operations on them,
 * such as comparison between two DescriptorLists, collection of statistics,
 * reading and printing from files, and various others.
 * 
 * @author Felix Sukhenko
 * @author David Mihalcik
 * @author David Doermann
 * @version %I%, %G%
 */
public class CompMatrix implements Cloneable {
	/**
	 * Vector containing all Target descriptors.
	 */
	DescriptorList T;
	/**
	 * Vector containing all Candidate descriptors.
	 */
	DescriptorList C;

	private CanonicalFileDescriptor cfd;

	/**
	 * The matrix of comparisons. Each has a link back to its Descriptors and
	 * information concerning distance. Most of the operations are done on this
	 * matrix, and the C and T vectors should not be referred to often.
	 * 
	 * Each row represents a single candidate, and each column, a target.
	 */
	private DataMatrix2d edgeMatrix;

	/**
	 * Indicates that, at the current level of analysis, there are still some
	 * matching target/candidate pairs.
	 */
	private boolean continuable = true;

	/**
	 * Get the total number of target (truth) descriptors. NB: currently this
	 * includes descriptors that are excluded in any output filter.
	 * 
	 * @return the number of target descriptors
	 */
	int getTargetCount() {
		return getEdges().sizeWide();
	}

	/**
	 * Get the total number of candidate (result) descriptors. NB: currently
	 * this includes descriptors that are excluded in any output filter.
	 * 
	 * @return the number of candidate descriptors
	 */
	int getCandidateCount() {
		return getEdges().sizeHigh();
	}

	/**
	 * The matrix can be viewed as a bipartite graph, with the descriptors as
	 * nodes and the comparisons as edges. Taken this way, the matrix just a
	 * list of edges.
	 * 
	 * @return the edge comparisons
	 */
	DataMatrix2d getEdges() {
		return edgeMatrix;
	}

	/**
	 * Gets the mapping from target object and attribute names to candidate
	 * names.
	 * 
	 * @return the name equivalencies
	 */
	Equivalencies getEquivalencies() {
		return map;
	}

	CanonicalFileDescriptor getFileInformation() {
		return cfd;
	}

	/**
	 * Gets what level the object has been taken to. Useful in case you forget,
	 * I suppose, although it isn't too hard to keep track.
	 * 
	 * @return the level to which the graph has been compared
	 */
	int getCurrentLevel() {
		return level;
	}

	/**
	 * Gets a matrix iterator for the edges. NB the current implementation is
	 * _not_ fail fast, so don't try anything funny.
	 * 
	 * @return an iterator over all comparisons
	 */
	MatrixIterator getMatrixIterator() {
		return edgeMatrix.getMatrixIterator();
	}

	/**
	 * This simpler iterator just returns all the comparisons in some arbitrary
	 * order.
	 * 
	 * @return an iterator over all comparisons
	 */
	Iterator getUnorderedIterator() {
		return new Iterator() {
			private MatrixIterator mi = getMatrixIterator();
			private boolean inColumn = false;

			public boolean hasNext() {
				if (inColumn)
					return mi.hasNextInColumn();
				else
					return mi.hasNextColumn();
			}

			public Object next() throws NoSuchElementException {
				if (!inColumn)
					mi.nextColumn();
				Object o = mi.nextInColumn();
				inColumn = mi.hasNextInColumn();
				return o;
			}

			public void remove() {
				throw new UnsupportedOperationException();
			}
		};
	}

	/**
	 * The naming Equivalencies between the target and candidate data.
	 */
	private Equivalencies map;

	/**
	 * The level of analysis (detection, matching, localization, or statistical)
	 * that this matrix has been exposed to.
	 */
	private int level;

	/**
	 * What sort of match filtering has been applied. This can be preformed at
	 * any level, but no more analysis can be applied afterward.
	 */
	int filtered = CompFilter.NONE;

	private double distances[] = null;
	private Surreal completeSum;

	private boolean initialized = false;

	/**
	 * Used only after merging, this holds a set of target IDs, so that they
	 * aren't reported as missed.
	 */
	Set targetIdSet;
	Set candidateIdSet;

	PrintWriter log = new PrintWriter(System.err);

	private EvaluationParameters.ScopeRules scope;
	
	
	/**
	 * Gets the evaluation rule set currently associated with 
	 * this set of comparisons.
	 * @return the scope rules for this set of comparisons
	 */
	public EvaluationParameters.ScopeRules getScopeRules() {
		return scope;
	}

	/**
	 * Determines if the target should be ignored.
	 * @param d the target descriptor
	 * @return <code>true</code> if the target is not worth the time
	 */
	public boolean caresAboutTarget(Descriptor d) {
		return scope.isOutputableTarget(d);
	}

	/**
	 * Determines if the candidate should be ignored.
	 * @param d the candidate descriptor
	 * @return <code>true</code> if the candidate is not worth the time
	 */
	public boolean caresAboutCandidate(Descriptor d) {
		return scope.isOutputableCandidate(d);
	}

	/**
	 * Creates a new structure for the comparison of two DescriptorLists. The
	 * CompMatrix is simply a two dimensional array of Comparison objects. Each
	 * entry in the array corresponds to a Target/Candidate pair. At this point,
	 * a Comparison object is constructed for each entry in the matrix.
	 * 
	 * As the Comparison constructor specifies, those that are in the same
	 * category start with level set to Comparison.STARTED, and those that are
	 * not in the same category have level set to Comparison.UNCOMPARABLE.
	 * 
	 * 
	 * @param targets
	 *            the vector containing the target descriptors
	 * @param candidates
	 *            the vector containing the candidate descriptors
	 * @param cfd
	 *            the file descriptor describing te media file; needed for some
	 *            metrics, e.g. pixel metrics
	 * @param scope
	 *            attribute scope and equivalency rules
	 * @param log
	 *            the logger to accept error messages
	 */
	public CompMatrix(DescriptorList targets, DescriptorList candidates,
			CanonicalFileDescriptor cfd, EvaluationParameters.ScopeRules scope,
			PrintWriter log) {
		this(targets, candidates, cfd, scope, log, null);
	}

	/**
	 * Create a new descriptor comparison matrix.
	 * @param targets the target data set. This is the truth data.
	 * @param candidates the candidate data set
	 * @param cfd basic file metadata
	 * @param scope rules about evaluation
	 * @param log error log
	 * @param ticker ui for indicating how long things are taking
	 */
	public CompMatrix(DescriptorList targets, DescriptorList candidates,
			CanonicalFileDescriptor cfd, EvaluationParameters.ScopeRules scope,
			PrintWriter log, Revealer ticker) {
		if (Comparison.STATISTICED <= level)
			throw new IllegalArgumentException(
					"Cannot set starting level greater than LOCALIZED.");
		completeSum = new Surreal(0, 0);
		C = candidates;
		T = targets;
		this.cfd = cfd;
		this.scope = scope;
		this.log = log;
		map = scope.getMap();
		this.level = Comparison.STARTED;
		filtered = CompFilter.NONE;
	}

	/**
	 * Called after setting the descriptors and various other things, this goes
	 * through the matrix of comparisons and brings them all up to the specified
	 * level of analysis.
	 * 
	 * @param scope
	 *            The scope rules to use during analysis.
	 * @param level
	 *            What evaluation level to use.
	 * @param ticker
	 *            Ticker to tick off time marks on.
	 * @return <code>true</code> if some element of the CompMatrix qualifies
	 *         as a match at the specified level.
	 */
	public boolean initializeMatrix(EvaluationParameters.ScopeRules scope,
			int level, Revealer ticker) {
		if (initialized) {
			throw new IllegalStateException("Already Initialized Matrix");
		}
		edgeMatrix = new SparseMatrix(T.size(), C.size());
		initialized = true;

		int maxLevelGained = this.level;
		for (int i = 0; i < T.size(); i++) {
			for (int j = 0; j < C.size(); j++) {
				if (null != ticker) {
					ticker.tick();
				}
				if (scope.inScope((Descriptor) T.get(i))
						&& scope.inScope((Descriptor) C.get(j))) {
					Comparison temp = new Comparison(((Descriptor) T.get(i)),
							((Descriptor) C.get(j)), map);
					boolean store = true;
					if (level >= Comparison.STARTED) {
						store = store && goodComp(temp);
						if (store && Comparison.STARTED < level) {
							store = store && temp.match(scope);
							if (store && Comparison.MATCHED < level) {
								store = store && temp.detect(cfd, scope);
								if (store && Comparison.DETECTED < level) {
									store = store && temp.localize(cfd, scope);
									//if (Comparison.LOCALIZED < level)
								}
							}
						}
					}
					if (store) {
						edgeMatrix.set(i, j, temp);
						maxLevelGained = Math.max(maxLevelGained, temp
								.getLevel());
					}
				}
			}
		}
		return maxLevelGained == level;
	}

	private CompMatrix() {
		completeSum = new Surreal(0, 0);
		T = new DescVector(null);
		C = new DescVector(null);
		edgeMatrix = new SparseMatrix(0, 0);
	}

	/**
	 * Creates a new comparison matrix as a subset of an existing matrix.
	 * @param old the matrix to subset
	 * @param targetMask the target descriptors to use. 
	 * If the kth bit is set, the kth target descriptor will be
	 * used in the new matrix.
	 * @param candidateMask the candidate descriptors to use. 
	 * If the kth bit is set, the kth candidate descriptor will be
	 * used in the new matrix.
	 */
	public CompMatrix(CompMatrix old, BitSet targetMask, BitSet candidateMask) {
		this.scope = old.scope;
		this.cfd = old.cfd;
		this.map = old.map;
		this.level = old.level;
		this.filtered = old.filtered;
		this.log = old.log;

		this.T = new DescVector(old.T.getParent());
		this.C = new DescVector(old.C.getParent());
		for (int i = 0; i < old.edgeMatrix.width(); i++) {
			if (targetMask.get(i)) {
				this.T.add(old.T.get(i));
			}
		}
		for (int j = 0; j < old.edgeMatrix.height(); j++) {
			if (candidateMask.get(j)) {
				this.C.add(old.C.get(j));
			}
		}

		initialized = old.initialized;
		if (initialized) {
			edgeMatrix = new SparseMatrix(old.edgeMatrix, targetMask,
					candidateMask);
			this.getCompleteSum();
		}
	}

	/**
	 * Creates a copy of the <code>CompMatrix</code>.
	 * 
	 * @return a deep copy of the matrix
	 */
	public Object clone() {
		CompMatrix M = new CompMatrix();
		M.completeSum = (Surreal) completeSum.clone();
		M.C = (DescriptorList) C.clone();
		M.T = (DescriptorList) T.clone();
		M.cfd = cfd;
		M.map = map;
		M.level = level;
		M.filtered = filtered;
		M.scope = scope;

		if (initialized) {
			M.initialized = true;
			M.edgeMatrix = new SparseMatrix(M.T.size(), M.C.size());
			MatrixIterator mi = getMatrixIterator();
			while (mi.hasNextColumn())
				while (mi.hasNextInColumn()) {
					Comparison comp = (Comparison) ((Comparison) mi
							.nextInColumn()).clone();
					comp.setPointers((Descriptor) M.T.get(mi.currColumn()),
							(Descriptor) M.C.get(mi.currRow()));
					M.edgeMatrix.set(mi.currColumn(), mi.currRow(), comp);
				}
		} else {
			M.initialized = false;
		}
		return M;
	}

	/**
	 * Brings the comparisons up to the given level.
	 * @param type the level to go to
	 * @param scope the scoping rules
	 * @return if a comparison or more survived
	 */
	public boolean bringToLevel(int type, EvaluationParameters.ScopeRules scope) {
		if (!initialized)
			initializeMatrix(scope, type - 1, null);
		int next = level + 1;
		while (level < type && continuable) {
			helpMoveUpALevel(next++, scope);
		}
		return continuable;
	}

	private boolean helpMoveUpALevel(int type,
			EvaluationParameters.ScopeRules scope) {
		if (!continuable)
			return false;

		continuable = false;
		BitSet foundCandidate = new BitSet(C.size());
		BitSet foundTarget = new BitSet(T.size());

		//Reset the overall distance between the target set and candidate set
		completeSum.set(0, 0.0);

		//Cycle through all possible matches and perform matching.
		MatrixIterator mi = edgeMatrix.getMatrixIterator();
		while (mi.hasNextColumn()) {
			int x = mi.nextColumn();
			while (mi.hasNextInColumn()) {
				Comparison curr = (Comparison) mi.nextInColumn();
				if ((goodComp(curr))
						&& (((Comparison.MATCHED == type) && curr.match(scope))
								|| ((Comparison.DETECTED == type) && curr
										.detect(cfd, scope))
								|| ((Comparison.LOCALIZED == type) && curr
										.localize(cfd, scope)) || ((Comparison.STATISTICED == type) && curr
								.statistical(scope)))) {
					completeSum.add(0, curr.getDistance());
					foundTarget.set(x);
					continuable = true;
					foundCandidate.set(mi.currRow());
				}
			}
		}

		for (int i = 0; i < foundCandidate.size(); i++)
			if (!foundCandidate.get(i))
				completeSum.add(1, 0.0);
		for (int i = 0; i < foundTarget.size(); i++)
			if (!foundTarget.get(i))
				completeSum.add(1, 0.0);

		level = type;
		return continuable;
	}

	private boolean helpMoveUpALevel(int type) {
		if (!continuable)
			return false;
		if (!initialized) {
			initializeMatrix(scope, type - 1, null);
		}

		continuable = false;
		BitSet foundCandidate = new BitSet(C.size());
		BitSet foundTarget = new BitSet(T.size());

		//Reset the overall distance between the target set and candidate set
		completeSum.set(0, 0.0);

		//Cycle through all possible matches and perform matching.
		MatrixIterator mi = edgeMatrix.getMatrixIterator();
		while (mi.hasNextColumn()) {
			int x = mi.nextColumn();
			while (mi.hasNextInColumn()) {
				Comparison curr = (Comparison) mi.nextInColumn();
				if ((goodComp(curr))
						&& (((Comparison.MATCHED == type) && curr.match(scope))
								|| ((Comparison.DETECTED == type) && curr
										.detect(cfd, scope))
								|| ((Comparison.LOCALIZED == type) && curr
										.localize(cfd, scope)) || ((Comparison.STATISTICED == type) && curr
								.statistical(scope)))) {
					completeSum.add(0, curr.getDistance());
					foundTarget.set(x);
					continuable = true;
					foundCandidate.set(mi.currRow());
				}
			}
		}

		for (int i = 0; i < C.size(); i++)
			if (!foundCandidate.get(i))
				completeSum.add(1, 0.0);
		for (int i = 0; i < T.size(); i++)
			if (!foundTarget.get(i))
				completeSum.add(1, 0.0);

		level = type;
		return continuable;
	}

	/***************************************************************************
	 * ------------------------------------------------------------------------
	 * MATCH()
	 * ------------------------------------------------------------------------
	 */
	/***************************************************************************
	 * ------------------------------------------------------------------------
	 * Comparison.MATCHED (Simple Name) matching: Adds candidates from a list of
	 * possibles to each of the Descriptor objects in the list. Initially, we do
	 * a simple match that produces a list of correspondences. Here we are
	 * processing only level Comparison.STARTED
	 * 
	 * @see Comparison#match(EvaluationParameters.ScopeRules)
	 * @return if there are any remaining edges in the comparison graph after
	 *         the basic matching
	 */
	public boolean match() {
		return helpMoveUpALevel(Comparison.MATCHED);
	}

	/***************************************************************************
	 * ------------------------------------------------------------------------
	 * DETECT()
	 * ------------------------------------------------------------------------
	 */
	/**
	 * Comparison.DETECTED (Detection) matching:
	 * 
	 * @see Comparison#detect(CanonicalFileDescriptor,
	 *      EvaluationParameters.ScopeRules)
	 * @return if there are any remaining edges in the comparison graph after
	 *         the detection matching
	 */
	public boolean detect() {
		return helpMoveUpALevel(Comparison.DETECTED);
	}

	/***************************************************************************
	 * ------------------------------------------------------------------------
	 * LOCALIZE()
	 * ------------------------------------------------------------------------
	 */
	/**
	 * Comparison.LOCALIZED (Localization) matching:
	 * 
	 * @see Comparison#localize(CanonicalFileDescriptor,
	 *      EvaluationParameters.ScopeRules)
	 * @return if there are any remaining edges in the comparison graph after
	 *         the localization
	 */
	public boolean localize() {
		return helpMoveUpALevel(Comparison.LOCALIZED);
	}

	/***************************************************************************
	 * ------------------------------------------------------------------------
	 * STATISTICAL()
	 * ------------------------------------------------------------------------
	 */
	/**
	 * Comparison.STATISTICED (Statistical) matching: Match subject to
	 * constraints on overall attribute match A given candidate will only be
	 * considered if its average, minimum or median of the frame distance
	 * computations is above a given tolearnce. What this means, is that for any
	 * pair which meets the Comparison.MATCHED level criteria, we compute the
	 * average, minimum and median distances across all matching frames. If that
	 * average, minimum or median meeting the tolerance, then it is considered a
	 * match. In some sence, this should be performed in place of level II,
	 * instead of after it.
	 * 
	 * @param tolerance
	 *            the threshold to allow the candidate to still be considered
	 *            viable. <em>Currently ignored.</em>
	 * @see Comparison#statistical(EvaluationParameters.ScopeRules)
	 * @return if there are any remaining edges in the comparison graph after
	 *         the statistical matching
	 */
	public boolean statistical(double tolerance) {
		return helpMoveUpALevel(Comparison.STATISTICED);
	}

	/**
	 * If it is possible to upgrade the level of the matrix.
	 * @return
	 */
	public boolean isContinuable() {
		return continuable;
	}

	/**
	 * Gets the minimax sum of all distances.
	 * @return the sum of all distances
	 */
	public Surreal getCompleteSum() {
		BitSet foundTarget = new BitSet(T.size());
		BitSet foundCandidate = new BitSet(C.size());
		completeSum = new Surreal();

		MatrixIterator mi = edgeMatrix.getMatrixIterator();
		while (mi.hasNextColumn()) {
			int x = mi.nextColumn();
			while (mi.hasNextInColumn()) {
				Comparison curr = (Comparison) mi.nextInColumn();
				if (goodComp(curr)) {
					completeSum.add(0, curr.getDistance());
					foundTarget.set(x);
					foundCandidate.set(mi.currRow());
				}
			}
		}

		for (int i = 0; i < foundCandidate.size(); i++)
			if (!foundCandidate.get(i))
				completeSum.add(1, 0.0);
		for (int i = 0; i < foundTarget.size(); i++)
			if (!foundTarget.get(i))
				completeSum.add(1, 0.0);

		return completeSum;
	}

	/***************************************************************************
	 * ------------------------------------------------------ REMOVE DUPLICATES
	 * ------------------------------------------------------
	 */
	/**
	 * Comparison.COMPLETE matching: Removes the duplicates in the same rows by
	 * finding the minimum distance and promoting those targets to the next
	 * level.
	 * 
	 * @param match
	 *            the method for removing duplicates
	 */
	public void removeDuplicates(int match) {
		helpRemoveDuplicates(match);
		filtered = match;
		if (match == CompFilter.MULTIPLE)
			helpMergeLosses();
	}

	/**
	 * Displays a table of <code>Descriptor</code> s listed by ID and the
	 * distances between them. If there is no match, the distance is infinity,
	 * displayed as
	 * 
	 * <pre>
	 * infnt
	 * </pre>. Otherwise, the level of the match is listed followed by the
	 * distance. <BR>
	 * For example :
	 * 
	 * <PRE>| 0 | 99 | 1 | --------------------------------- 11 |2 0.0 |2 1.0 |
	 * infnt | --------------------------------- 12 | infnt |2 1.0 |2 0.0 |
	 * ---------------------------------
	 * 
	 * </PRE>
	 * 
	 * @return the overall matching table
	 */
	String printOverallMatchTable() {
		String s = "\t";
		DecimalFormat nf = (DecimalFormat) NumberFormat.getInstance();
		nf.applyLocalizedPattern("#.##");

		for (int j = 0; j < C.size(); j++)
			s += "| " + ((Descriptor) C.get(j)).getID() + "\t";
		s += "|\n";
		for (int i = 0; i < edgeMatrix.width(); i++) {
			s += "--------";
			for (int j = 0; j < edgeMatrix.height(); j++)
				s += "--------";
			s += "-\n";
			s += " " + ((Descriptor) T.get(i)).getID() + "\t";
			for (int j = 0; j < edgeMatrix.height(); j++) {
				Comparison curr = (Comparison) edgeMatrix.get(i, j);
				if (null != curr) {
					s += "|" + (curr.getLevel() + curr.getFilterLevel()) + " "
							+ nf.format(curr.getDistance()) + "\t";
				} else {
					s += "| infnt ";
				}
			}
			s += "|\n";
		}
		for (int j = -1; j < edgeMatrix.height(); j++)
			s += "--------";
		s += "\n";
		return s;
	}

	String printAllMatches() {
		Iterator iter = getUnorderedIterator();
		StringBuffer sb = new StringBuffer();
		while (iter.hasNext()) {
			Comparison temp = (Comparison) iter.next();
			sb.append(temp.T.getID()).append(" v ").append(temp.C.getID());
			sb.append(" = ").append(temp.getDistance()).append(" ").append(
					temp.getFilterLevel()).append(" ").append(temp.getLevel())
					.append("\n");
		}
		return sb.toString();
	}

	private void helpRemoveDuplicates(int matchType) {
		// In order to speed up the process of removing duplicates,
		// the algorithm operates on disjoint subgraphs.
		// This loops through and performs the specified
		// cropping procedure on these subgraphs.
		BitSet[] subsettingMask = new BitSet[edgeMatrix.width()];
		CompMatrix submatrix;

		for (int i = 0; i < edgeMatrix.width(); i++) {
			subsettingMask[i] = new BitSet(edgeMatrix.height());
			for (int j = 0; j < edgeMatrix.height(); j++) {
				if (goodComp((Comparison) edgeMatrix.get(i, j))) {
					subsettingMask[i].set(j);
				}
			}
		}
		completeSum.set(0, 0.0);

		while (null != (submatrix = getNextSubmatrix(this, subsettingMask))) {
			completeSum.add(CompFilter.filterSelect[matchType].filter(
					submatrix, getScopeRules()));
		}
	}

	/**
	 * Remove the detritus comparisons (the ones that 
	 * aren't still active).
	 */
	public void removeOld() {
		for (int i = 0; i < edgeMatrix.width(); i++) {
			for (int j = 0; j < edgeMatrix.height(); j++) {
				if (!goodComp((Comparison) edgeMatrix.get(i, j))) {
					edgeMatrix.remove(i, j);
				}
			}
		}
	}

	/**
	 * Creates the targetIdSet and CandidateIdSet, so merged descriptors aren't
	 * reported as false/missed.
	 */
	void helpMergeLosses() {
		targetIdSet = new TreeSet();
		candidateIdSet = new TreeSet();

		/* First get the IDs of all the descriptors still in play */
		MatrixIterator mi = getMatrixIterator();
		while (mi.hasNextColumn()) {
			mi.nextColumn();
			while (mi.hasNextInColumn()) {
				Comparison curr = (Comparison) mi.nextInColumn();
				if (goodComp(curr)) {
					Object id = curr.T.getID();
					if (id instanceof Collection)
						targetIdSet.addAll((Collection) id);
					else
						targetIdSet.add(id);
					id = curr.C.getID();
					if (id instanceof Collection)
						candidateIdSet.addAll((Collection) id);
					else
						candidateIdSet.add(id);
				}
			}
		}
	}

	/**
	 * This returns a submatrix of the distances matrix, where the next
	 * submatrix is the first one searching from right to left from the bottom
	 * to the top. A submatrix is a connected subgraph where there is a
	 * connection determined by goodComp(), and no such connections to the
	 * outside.
	 * 
	 * @param M
	 *            the value to iterate over
	 * @param edgeMask
	 *            the elements removed so far
	 * @return the submatrix
	 */
	static CompMatrix getNextSubmatrix(CompMatrix M, BitSet[] edgeMask) {
		int t = M.edgeMatrix.width();
		int c = 0;

		while ((t > 0) && (c == 0)) {
			c = edgeMask[--t].length();
		}
		if (c == 0)
			return (null);
		c--;

		// c and t mark the bottom right corner of the next sub-matrix.
		// The next section finds all adjacent nodes.
		BitSet candidateMask = new BitSet(M.edgeMatrix.height());
		BitSet targetMask = new BitSet(M.edgeMatrix.width());
		BitSet[] tempMask = new BitSet[edgeMask.length];

		candidateMask.set(c);
		targetMask.set(t);

		while (expandBitMaskToAdjacent(M, targetMask, candidateMask));

		for (int i = 0; i < M.edgeMatrix.width(); i++) {
			tempMask[i] = new BitSet(M.edgeMatrix.height());
			if (targetMask.get(i)) {
				for (int j = 0; j < M.edgeMatrix.height(); j++)
					if (candidateMask.get(j)) {
						tempMask[i].set(j);
					}
			}
		}
		for (int i = 0; i < edgeMask.length; i++)
			edgeMask[i].andNot(tempMask[i]);

		return (new CompMatrix(M, targetMask, candidateMask));
	}

	static boolean expandBitMaskToAdjacent(CompMatrix M, BitSet targetMask,
			BitSet candidateMask) {
		boolean expanded;
		expanded = false;

		//First, expand from targets to candidates
		MatrixIterator mi = M.getMatrixIterator();
		while (mi.hasNextColumn()) {
			int x = mi.nextColumn();
			if (targetMask.get(x)) {
				while (mi.hasNextInColumn()) {
					Comparison curr = (Comparison) mi.nextInColumn();
					if (M.goodComp(curr) && !candidateMask.get(mi.currRow())) {
						expanded = true;
						candidateMask.set(mi.currRow());
					}
				}
			}
		}

		mi = M.getMatrixIterator();
		while (mi.hasNextRow()) {
			int y = mi.nextRow();
			if (candidateMask.get(y)) {
				while (mi.hasNextInRow()) {
					Comparison curr = (Comparison) mi.nextInRow();
					if (M.goodComp(curr) && !targetMask.get(mi.currColumn())) {
						expanded = true;
						targetMask.set(mi.currColumn());
					}
				}
			}
		}
		return expanded;
	}

	/**
	 * This checks to see if the Comparison shows any similarity.
	 * 
	 * This checks three things:
	 * <UL>
	 * <LI>Is its level equal to the level of this CompMatrix?</LI>
	 * <LI>If the CompMatrix has been cropped, did it survive?</LI>
	 * <LI>Is it less than infinity?</LI>
	 * </UL>
	 * 
	 * @param test
	 *            the comparison to test
	 * @return if the comparison is still good
	 */
	boolean goodComp(Comparison test) {
		return (test != null)
				&& (test.getLevel() >= level)
				&& ((filtered == CompFilter.NONE) || (test.getFilterLevel() != CompFilter.NONE))
				&& (test.getDistance() < Double.POSITIVE_INFINITY);
	}

	/**
	 * This checks to see if the Comparison has been, by the standards of this
	 * CompMatrix, dead since before the last test. If it is still alive or only
	 * recently deceased, it returns true.
	 * 
	 * @param test
	 *            the comparison to test
	 * @return if the comparison is more than one level removed from the
	 *         comparison matrix
	 */
	boolean longDeadComp(Comparison test) {
		return ((test != null) && ((test.getLevel() < (level - 1)) || ((test
				.getLevel() < level) && ((filtered != CompFilter.NONE) && (test
				.getFilterLevel() == CompFilter.NONE)))));
	}

	boolean isMergeLostTarget(Descriptor desc) {
		return ((targetIdSet != null) && (targetIdSet.contains(desc.getID())));
	}
	boolean isMergeLostCandidate(Descriptor desc) {
		return ((candidateIdSet != null) && (candidateIdSet.contains(desc
				.getID())));
	}

	private boolean isInTargetConfigs(Descriptor to_test,
			Collection target_configs) {
		for (Iterator iter = target_configs.iterator(); iter.hasNext();) {
			Descriptor target_element = (Descriptor) iter.next();
			if (scope.inScope(target_element)) {
				if (to_test.sameCategoryAs(target_element, map)) {
					return true;
				}
			}
		}
		return false;
	}

	/**
	 * Print the most recent Descriptors to be false or missed.
	 * 
	 * This goes through and checks each Target and Candidate pair. If there
	 * exists a comparison, then each its Target and its Candidate still count
	 * as a detection. If a Candidate or Target has no matching surviving
	 * Comparison objects, then it is false or missed, respectively. This
	 * function only prints the false and missed that were discovered in the
	 * most recent level. ie, if this has been Statisticed, then it will only
	 * display those who where still considered detctions by Localization.
	 * 
	 * @param output
	 *            the human readable, verbose stream
	 * @param raw
	 *            the machine readable, terse stream
	 * @param targetConfigs
	 *            the configs to print out
	 */
	public void printCurrentFM(PrintWriter output, PrintWriter raw,
			Collection targetConfigs) {
		String l = null;
		if (output != null) {
			l = "LEVEL " + level;
			if (filtered != CompFilter.NONE) {
				l += "C";
			}
		}
		if (output != null) {
			output.print(StringHelp.banner(l + "\nFALSE DETECTIONS", 53));
		}
		printCurrentFalse(output, raw, targetConfigs);

		if (output != null) {
			output.print(StringHelp.banner(l + "\nMISSED DETECTIONS", 53));
		}
		printCurrentMissed(output, raw, targetConfigs);
	}

	/**
	 * Print out the current estimate of false candidate descriptors.
	 * @param output the human-readable output
	 * @param raw the raw formatted output
	 * @param targetConfigs the target descriptors to print out
	 */
	public void printCurrentFalse(PrintWriter output, PrintWriter raw,
			Collection targetConfigs) {
		boolean fine; //Has this survived this level of threshold?
		boolean already; //Did it survive the previous one?
		boolean in_target_configs = true;
		boolean aMergeLoss = false;

		if (level <= Comparison.MATCHED) {
			allCandidates : for (int j = 0; j < C.size(); j++) {
				fine = false;
				isFine : for (int i = 0; i < T.size(); i++) {
					if (goodComp((Comparison) edgeMatrix.get(i, j))) {
						fine = true;
						break isFine;
					}
				}

				// Following block added avoid printing candidates that are not
				// in the target set.
				Descriptor currentDesc = (Descriptor) C.get(j);
				in_target_configs = isInTargetConfigs(currentDesc,
						targetConfigs)
						&& scope.isOutputableCandidate(currentDesc);

				if (!fine && in_target_configs) {
					if (output != null) {
						output.println(currentDesc);
					}
					if (raw != null) {
						//Print out the results in raw format
						// <name> <number> <status> <level>
						//Here, status == FALSE
						raw
								.println(currentDesc.rawFormat() + " FALSE "
										+ level);
					}
				}
			} // allCandidates
		} else {
			MatrixIterator mi = getMatrixIterator();
			while (mi.hasNextRow()) {
				fine = false;
				already = true;
				Descriptor currentDesc = (Descriptor) C.get(mi.nextRow());
				while (mi.hasNextInRow()) {
					Comparison curr = (Comparison) mi.nextInRow();
					fine |= goodComp(curr);
					already &= longDeadComp(curr);
				}
				in_target_configs = isInTargetConfigs(currentDesc,
						targetConfigs)
						&& scope.isOutputableCandidate(currentDesc);

				// Check against the "ID set" to avoid printing the merged ones.
				aMergeLoss = isMergeLostCandidate(currentDesc);

				if (!fine && !already && in_target_configs && !aMergeLoss) {
					if (output != null)
						output.println(currentDesc);
					if (raw != null)
						//Print out the results in raw format
						// <name> <number> <status> <level>
						//Here, status == FALSE
						raw
								.println(currentDesc.rawFormat() + " FALSE "
										+ level);
				}
			}
		}
	}

	/**
	 * Prints out all of the descriptors that were "missed" in the last pass.
	 * This basically means invoking <code>goodComp</code> on all of the
	 * <code>Comparison</code> s in the matrix and outputing the targets that
	 * are not in any good comparison, are not <code>longDeadComp</code>, and
	 * are visible.
	 * 
	 * @param output
	 *            the human readable, verbose stream
	 * @param raw
	 *            the machine readable, terse stream
	 * @param targetConfigs
	 *            the configs to print out
	 */
	public void printCurrentMissed(PrintWriter output, PrintWriter raw,
			Collection targetConfigs) {
		boolean fine; //Has this survived this level of threshold?
		boolean already; //Did it survive the previous one?
		boolean aMergeLoss = false;

		if (level <= Comparison.MATCHED) {
			// First time, so there won't be any long-dead comparisons
			for (int i = 0; i < T.size(); i++) {
				fine = false;
				already = true;
				for (int j = 0; j < C.size(); j++)
					fine |= goodComp((Comparison) edgeMatrix.get(i, j));
				Descriptor currentDesc = (Descriptor) T.get(i);
				if (!fine && scope.isOutputableTarget(currentDesc)) {
					if (null != output)
						output.println(currentDesc);
					if (null != raw)
						raw.println(currentDesc.rawFormat() + " MISSED "
								+ level);
				}
			}
		} else {
			MatrixIterator mi = getMatrixIterator();
			while (mi.hasNextColumn()) { // for each target
				fine = false;
				already = true;
				Descriptor currentDesc = (Descriptor) T.get(mi.nextColumn());
				while (mi.hasNextInColumn()) {
					Comparison currentComp = (Comparison) mi.nextInColumn();
					fine |= goodComp(currentComp);
					already &= longDeadComp(currentComp);
				}

				// Check against the "ID set" to avoid printing the merged ones.
				aMergeLoss = isMergeLostTarget(currentDesc);

				if (!fine && !already && !aMergeLoss
						&& scope.isOutputableTarget(currentDesc)) {
					if (output != null)
						output.println(currentDesc);
					if (raw != null)
						//Print out the results in raw format
						// <name> <number> <status> <level>
						//Here, status == MISSED
						raw.println(currentDesc.rawFormat() + " MISSED "
								+ level);
				}
			}
		}
	}

	/**
	 * Prints out the candidates that are close to each target, if any haven't
	 * been filtered. At first, all candidates match all target objects in the
	 * same class, and fewer and fewer are matched, until only one, or a
	 * sequence, matches. It also checks against the output filters, and doesn't
	 * print out any descriptors that don't pass through.
	 * 
	 * @param output
	 *            the <code>PrintWriter</code> for outputting the information,
	 *            eg System.out. May be set to <code>NULL</code> if no raw
	 *            output requested.
	 * 
	 * @param raw
	 *            the <code>PrintWriter</code> for outputting the information
	 *            in raw format for postprocessing. May be set to
	 *            <code>NULL</code> if no raw output requested.
	 * 
	 * @param target_configs
	 */
	public void printCandidates(PrintWriter output, PrintWriter raw,
			Collection target_configs) {
		boolean found;
		MatrixIterator mi = getMatrixIterator();

		while (mi.hasNextColumn()) {
			StringBuffer rawCandidateSet = new StringBuffer();
			int rawNumberOfCandidates = 0;
			FrameSpan rawMatchSpan = new FrameSpan();
			FrameSpan rawUnionSpan = new FrameSpan();

			found = false;
			Descriptor currentTarget = (Descriptor) T.get(mi.nextColumn());
			if (scope.isOutputableTarget(currentTarget)) {
				while (mi.hasNextInColumn()) {
					Comparison currentComp = (Comparison) mi.nextInColumn();
					if (goodComp(currentComp)
							&& isInTargetConfigs(currentComp.C, target_configs)
							&& scope.isOutputableCandidate(currentComp.C)) {
						if (!found) {
							found = true;
							if (null != output)
								output.print(currentComp.T);
							if (null != raw)
								raw.print(currentComp.T.rawFormat()
										+ " DETECT " + level);
						}
						if (null != output) {
							output.println("");
							currentComp.printCandidate(output, "\t", scope);
							output.println("");
						}
						if (null != raw) {
							rawNumberOfCandidates++;
							rawMatchSpan = rawMatchSpan
									.union(currentComp.matchSpan);
							rawUnionSpan = rawUnionSpan.union(currentComp.C
									.getFrameSpan());

							Measurable.Difference timeDiff;
							try {
								timeDiff = currentComp.T.getFrameSpan()
										.getDifference(
												currentComp.C.getFrameSpan(),
												null, null, cfd);
							} catch (IgnoredValueException ivx) {
								throw new IllegalStateException(ivx
										.getMessage());
							}
							Distance fsmet = scope.getMeasure(currentComp.T,
									" framespan").getMetric();
							rawCandidateSet.append('[').append(
									currentComp.C.getID()).append(' ').append(
									fsmet.getDistance(timeDiff)).append(' ')
									.append(currentComp.getDistances(scope))
									.append(']');
						}
					}
				}
				if (found && (null != output)) // && in_target_configs)
					output.println("");
				if ((raw != null) && found) {
					Measurable.Difference timeDiff;
					try {
						timeDiff = currentTarget.getFrameSpan().getDifference(
								rawUnionSpan, null,
								rawUnionSpan.minus(rawMatchSpan), cfd);
					} catch (IgnoredValueException ivx) {
						throw new IllegalStateException(ivx.getMessage());
					}
					Distance fsmet = scope.getMeasure(currentTarget,
							" framespan").getMetric();
					raw.println(" " + fsmet.getDistance(timeDiff) + " "
							+ rawNumberOfCandidates + " " + rawCandidateSet);
				}
			}
		}
	}
	
	private void gatherDistanceInformation(String name,
			DescriptorConfigs target_configs) {
		boolean in_target_configs, found;
		Vector temp_distance_holder = new Vector();
		MatrixIterator mi = getMatrixIterator();
		while (mi.hasNextColumn()) {
			found = false;
			in_target_configs = false;
			Descriptor curr_target = (Descriptor) T.get(mi.nextColumn());
			while (mi.hasNextInColumn()) {
				Comparison curr_comp = (Comparison) mi.nextInColumn();
				if (goodComp(curr_comp)) {
					if (!found) {
						found = true;
						// Checking that what will be printed is what was
						// requested
						in_target_configs = isInTargetConfigs(curr_target,
								target_configs);
					}
					if (in_target_configs) {
						Double f = new Double(curr_comp.getDistanceFor(name));
						if (!temp_distance_holder.contains(f))
							temp_distance_holder.addElement(f);
					}
				}
			}
		}
		if (temp_distance_holder.size() > 0) {
			Object[] temp_array = new Object[temp_distance_holder.size()];
			temp_distance_holder.copyInto(temp_array);
			distances = new double[temp_distance_holder.size()];
			for (int i = 0; i < temp_array.length; i++)
				distances[i] = ((Double) temp_array[i]).doubleValue();
			Arrays.sort(distances);
		}
	}

	/**
	 * Prints out ROC information. This currently doesn't work.
	 * @param output the human-readable output
	 * @param raw the raw formatted output
	 * @param target_configs the target descriptors to print out
	 */
	public void printROCInfo(PrintWriter output, PrintWriter raw,
			DescriptorConfigs target_configs) {

		int total_targets = 0;
		int total_candidates = 0;

		String roc_name;
		if (output != null)
			output
					.println("\n****************************************************"
							+ "\n*            VARIABLE ATTRIBUTE RESULTS            *"
							+ "\n****************************************************");
		if (raw != null)
			raw.println("\n\n// \n// \n// \n#BEGIN_VARIABLE_ATTRIBUTE");
		for (int r = 0; r < target_configs.size(); r++) {
			Descriptor t = (Descriptor) target_configs.get(r);
			if (t.isRoc()) {
				roc_name = t.rocAttributeName();
				gatherDistanceInformation(t.rocAttributeName(), target_configs);
				boolean in_target_configs, found, marked;
				int detections;
				if (output != null)
					output.println("For : " + roc_name + "\n\n");
				if (raw != null)
					raw.println(roc_name);
				for (int k = distances.length - 1; k >= 0; k--) {
					detections = 0;
					if (output != null)
						output.println("Distance " + distances[k] + "\n");
					MatrixIterator mi = getMatrixIterator();
					while (mi.hasNextColumn()) {
						found = false;
						in_target_configs = false;
						marked = false;
						Descriptor curr_target = (Descriptor) T.get(mi
								.nextColumn());
						while (mi.hasNextInColumn()) {
							Comparison curr_comp = (Comparison) mi
									.nextInColumn();
							if (goodComp(curr_comp) && !found) {
								found = true;
								// Checking that what will be printed is what
								// was requested
								in_target_configs = isInTargetConfigs(
										curr_target, target_configs);
								if (in_target_configs
										&& (curr_comp.getDistanceFor(roc_name) <= distances[k])
										&& (!marked)) {
									detections++;
									marked = true;
								}
							}
						}
					}
					//printing the new measurements
					int p = 0;
					if (total_candidates > 0)
						p = (detections * 100 / total_candidates);
					int re = 0;
					if (total_targets > 0)
						re = (detections * 100 / total_targets);
					if (raw != null)
						raw.println(distances[k] + " " + p + " " + re);
					if (output != null)
						output.println("\tPRECISION : " + p + "% ("
								+ detections + "/" + total_candidates
								+ ")\n\tRECALL    : " + re + "% (" + detections
								+ "/" + total_targets + ")\n");
				}
			}
		}
		if (raw != null)
			raw.println("#END_VARIABLE_ATTRIBUTE");
	}

	/**
	 * Adds the descriptor precision and recall infromation
	 * to the running total.
	 * @param currConfigDesc the descriptor type to add
	 * @param counts the running total
	 */
	public void addPRInfo(Descriptor currConfigDesc, PrecisionRecall counts) {
		/* compute target info */
		for (int i = 0; i < T.size(); i++) {
			Descriptor currTarget = (Descriptor) T.get(i);
			if (currTarget.sameCategoryAs(currConfigDesc, map)
					&& scope.isOutputableTarget(currTarget)) {
				boolean found = false;
				boolean visible = false;

				for (int j = 0; j < C.size(); j++) {
					if (goodComp((Comparison) edgeMatrix.get(i, j))) {
						found = true;
						if (scope.isOutputableCandidate((Descriptor) C.get(j))) {
							visible = true;
							break;
						}
					}
				}

				if (visible || !found) {
					if (!found && !isMergeLostTarget(currTarget))
						counts.targetsMissed++;
					else
						counts.targetsHit++;
				}
			}
		}

		/* Compute for Candidates */
		for (int j = 0; j < C.size(); j++) {
			Descriptor currCandidate = (Descriptor) C.get(j);
			if (currCandidate.sameCategoryAs(currConfigDesc, map)
					&& scope.isOutputableCandidate(currCandidate)) {
				boolean found = false;
				boolean visible = false;
				for (int i = 0; i < T.size(); i++) {
					if (goodComp((Comparison) edgeMatrix.get(i, j))) {
						found = true;
						if (scope.isOutputableTarget((Descriptor) T.get(i))) {
							visible = true;
							break;
						}
					}
				}

				if (visible || !found) {
					if (!found && !isMergeLostCandidate(currCandidate))
						counts.candidatesMissed++;
					else
						counts.candidatesHit++;
				}
			}
		}
	}

	/**
	 * Print out precision and recall.
	 * 
	 * @param output
	 *            the <code>PrintWriter</code> for outputting the information,
	 *            eg System.out. May be set to <code>NULL</code> if no raw
	 *            output requested.
	 * @param raw
	 *            the <code>PrintWriter</code> for outputting the information
	 *            in raw format for postprocessing. May be set to
	 *            <code>NULL</code> if no raw output requested.
	 * @param targetConfigs
	 *            the target descriptor configs for the comparisons to print
	 * @deprecated CompMatrices should only be accessed through
	 *             {@link viper.comparison.CompEvaluator CompEvaluators}, which
	 *             have their own printPR function.
	 */
	public void printPR(PrintWriter output, PrintWriter raw,
			DescriptorConfigs targetConfigs) {
		int total_targets = 0;
		int total_candidates = 0;

		int correctCountTotal = 0, accurateCountTotal = 0, missCountTotal = 0, falseCountTotal = 0, total = 0;
		if (output != null) {
			output.print(StringHelp.banner("SUMMARY RESULTS", 53));
		}
		if (raw != null) {
			raw.println("\n\n// \n// \n// \n#BEGIN_SUMMARY");
		}

		/* Calculate the number of targets hit and the number missed */

		for (int h = 0; h < targetConfigs.size(); h++) {
			Descriptor currConfigDesc = (Descriptor) targetConfigs.get(h);
			if (scope.inScope(currConfigDesc)) {
				int correctCount = 0, accurateCount = 0;
				int missCount = 0, falseCount = 0;
				int subtotal = 0;

				for (int i = 0; i < T.size(); i++) {
					Descriptor currTarget = (Descriptor) T.get(i);
					if (currTarget.sameCategoryAs(currConfigDesc, map)) {
						subtotal++;
						boolean found = false;

						for (int j = 0; j < C.size(); j++)
							found |= goodComp((Comparison) edgeMatrix.get(i, j));

						if (!found && !isMergeLostTarget(currTarget))
							missCount++;
						else
							correctCount++;
					}
				}

				////////////////////////////
				// Compute for Candidates //
				///////////////////////////
				if (T.size() == 0) {
					falseCount = C.size();
					accurateCount = 0;
				} else {
					for (int j = 0; j < C.size(); j++) {
						Descriptor currCandidate = (Descriptor) C.get(j);
						if (currCandidate.sameCategoryAs(currConfigDesc, map)) {
							boolean found = false;
							for (int i = 0; i < T.size(); i++)
								found |= goodComp((Comparison) edgeMatrix.get(
										i, j));

							if (!found && !isMergeLostCandidate(currCandidate))
								falseCount++;
							else
								accurateCount++;
						}
					}
				}
				if (output != null) {
					if ((falseCount + accurateCount) != 0) {
						output
								.println("\nFor "
										+ currConfigDesc.getCategory()
										+ " "
										+ currConfigDesc.getName()
										+ ": Precision is "
										+ (accurateCount * 100 / (falseCount + accurateCount))
										+ " %  (" + accurateCount + "/"
										+ (falseCount + accurateCount) + ")");
					} else {
						output.println("\nFor " + currConfigDesc.getCategory()
								+ " " + currConfigDesc.getName()
								+ ": Precision is - % (0/0)");
					}
					if (subtotal != 0) {
						output.println("For " + currConfigDesc.getCategory()
								+ " " + currConfigDesc.getName()
								+ ": Recall is "
								+ (correctCount * 100 / subtotal) + " %  ("
								+ correctCount + "/" + subtotal + ")");
					} else {
						output.println("For " + currConfigDesc.getCategory()
								+ " " + currConfigDesc.getName()
								+ ": Recall is - % (0/0)");
					}
				}
				if (raw != null) {
					double p = 0;
					if ((accurateCount + falseCount) > 0)
						p = (accurateCount * 100 / (falseCount + accurateCount));
					double r = 0;
					if (subtotal > 0)
						r = (correctCount * 100 / subtotal);
					raw.println(currConfigDesc.getName() + " " + (subtotal)
							+ " " + (falseCount + accurateCount) + " " + p
							+ " " + r);
				}
				total += subtotal;
				missCountTotal += missCount;
				falseCountTotal += falseCount;
				correctCountTotal += correctCount;
				accurateCountTotal += accurateCount;
			}
		}
		if (total == 0) {
			if (output != null) {
				output.println("\nNo information found in gt file.");
			}
		} else {
			total_candidates = falseCountTotal + accurateCountTotal;
			total_targets = missCountTotal + correctCountTotal;
			if (output != null) {
				if (total_candidates != 0)
					output.println("\nTotal precision is "
							+ (accurateCountTotal * 100 / (total_candidates))
							+ " %  (" + accurateCountTotal + "/"
							+ (total_candidates) + ")");
				else
					output.println("\nTotal precision is " + accurateCountTotal
							+ "/" + (total_candidates));
				if (total_targets != 0)
					output.println("Total recall is  "
							+ (correctCountTotal * 100 / (total_targets))
							+ " %  (" + correctCountTotal + "/"
							+ (total_targets) + ")");
				else
					output.println("Total recall is  " + correctCountTotal
							+ "/" + (total_targets));
			}
		}
		if (raw != null) {
			double p = 0;
			if ((total_candidates) > 0)
				p = accurateCountTotal * 100 / total_candidates;
			double r = 0;
			if (total_targets > 0)
				r = correctCountTotal * 100 / total_targets;
			raw.println("TOTAL " + total_targets + " " + total_candidates + " "
					+ p + " " + r + "\n#END_SUMMARY");
		}

		if (level > Comparison.LOCALIZED)
			printROCInfo(output, raw, targetConfigs);
	}
}