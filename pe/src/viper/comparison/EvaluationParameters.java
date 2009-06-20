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
import java.util.*;

import viper.comparison.distances.*;
import viper.descriptors.*;
import viper.filters.*;
import edu.umd.cfar.lamp.viper.util.*;
import edu.umd.cfar.lamp.viper.util.reader.*;

/**
 * Describes a set of parameters for setting up an evaluation.
 */
public class EvaluationParameters {
	/**
	 * Sorts descriptors by their full name.
	 */
	static class DescNameComparator implements Comparator {
		/**
		 * Places the descriptors in alphabetical order by full name.
		 * {@inheritDoc}
		 */
		public int compare(Object o1, Object o2) {
			return ((Descriptor) o1).getFullName().compareTo(
					((Descriptor) o2).getFullName());
		}

	}

	/**
	 * Sorts descriptors by their full name.
	 */
	public static final DescNameComparator descriptorComparator = new EvaluationParameters.DescNameComparator();

	private Equivalencies map = new Equivalencies();
	private List evals = new java.util.LinkedList();
	private RuleHolder targetOutputFilter = null;
	private RuleHolder candidateOutputFilter = null;
	private RuleHolder targetInputFilter = null;
	private RuleHolder candidateInputFilter = null;
	private DescriptorConfigs dcfgs;

	/**
	 * Gets the rules for target descriptors that are worthy of even parsing.
	 * 
	 * @return rules for which target descriptors to parse
	 */
	public RuleHolder getTargetInputFilter() {
		return targetInputFilter;
	}

	/**
	 * Gets the rules for candidate descriptors that are worthy of even parsing.
	 * 
	 * @return rules for which candidate descriptors to parse
	 */
	public RuleHolder getCandidateInputFilter() {
		return candidateInputFilter;
	}

	/**
	 * Parses the given evaluation parameters file, using
	 * the given level and match filter information, which must
	 * somehow be acquired elsewhere, for now.
	 * @param reader the source to parse
	 * @param level the level to bring the comparisons to, if using them
	 * @param targetMatch the type of match filter to use, if applicable
	 */
	public void parse(VReader reader, int level, int targetMatch) {
		String nextBegin = reader.advanceToBeginDirective();
		try {
			while (nextBegin != null) {
				if ("EQUIVALENCE".equals(nextBegin)) {
					map.parseMapping(reader);
				} else if (nextBegin.endsWith("_EVALUATION")) {
					reader.gotoNextRealLine();
					Evaluation current = null;
					if ("TRACKING_EVALUATION".equals(nextBegin)) {
						current = new TrackingEvaluation(this);
					} else if ("FRAMEWISE_EVALUATION".equals(nextBegin)) {
						current = new FramewiseEvaluation(this);
					} else if ("OBJECT_EVALUATION".equals(nextBegin)) {
						current = new ObjectEvaluation(this, level, targetMatch);
					} else {
						current = null;
					}
					if (current != null) {
						current.parseEvaluation(reader, dcfgs);
						evals.add(current);
					}
				} else if (nextBegin.endsWith("_FILTER")) {
					if ("GROUND_FILTER".equals(nextBegin)) {
						targetInputFilter = LimitationsParser.parse(reader,
								dcfgs);
					} else if ("GROUND_OUTPUT_FILTER".equals(nextBegin)) {
						targetOutputFilter = LimitationsParser.parse(reader,
								dcfgs);
					} else if ("RESULT_FILTER".equals(nextBegin)) {
						candidateInputFilter = LimitationsParser.parse(reader,
								dcfgs);
					} else if ("RESULT_OUTPUT_FILTER".equals(nextBegin)) {
						candidateOutputFilter = LimitationsParser.parse(reader,
								dcfgs);
					} else {
						reader.printError("Not a valid filter name: "
								+ nextBegin);
						reader.gotoNextRealLine();
					}
				} else {
					reader.printError("Unrecognized EPF section title: "
							+ nextBegin);
					reader.gotoNextRealLine();
				}
				nextBegin = reader.advanceToBeginDirective();
			}
		} catch (IOException iox) {
			reader
					.printGeneralError("I/O Exception while parsing "
							+ nextBegin);
		}

		if (evals.size() == 0) {
			reader.printGeneralError("Unable to find an evaluation section.");
		}
	}

	/**
	 * Creates a new instance of EvaluationParameters
	 * 
	 * @param descriptorProtos
	 *            the descriptor schema
	 */
	public EvaluationParameters(DescriptorConfigs descriptorProtos) {
		dcfgs = descriptorProtos;
	}

	/**
	 * Get the map from target to candidate descriptors and attributes.
	 * @return the map from targets to candidates
	 */
	public Equivalencies getMap() {
		return map;
	}

	/**
	 * Prints the parameters in raw format.
	 * @param raw the raw output stream
	 */
	public void printTerseParameters(PrintWriter raw) {
		raw.println("\n// \n// \n// \n#BEGIN_GROUND_FILTER\n"
				+ ((targetInputFilter != null) ? targetInputFilter
						.toRawFormat() : "\n")
				+ "#END_GROUND_FILTER\n\n// \n// \n// \n#BEGIN_RESULT_FILTER\n"
				+ ((candidateInputFilter != null) ? candidateInputFilter
						.toRawFormat() : "\n")
				+ "#END_RESULT_FILTER\n\n// \n// \n// \n#BEGIN_METRICS");
		for (Iterator iter = evals.iterator(); iter.hasNext();) {
			Evaluation curr = (Evaluation) iter.next();
			curr.printRawMetricsTo(raw);
		}
		raw.println("#END_METRICS\n");
	}

	/**
	 * Prints the parameters in long form.
	 * @param out the legible output stream
	 */
	public void printVerboseParameters(PrintWriter out) {
		if (targetInputFilter != null) {
			out.print(StringHelp.banner("GROUND INPUT FILTER", 53));
			out.println(targetInputFilter);
			out.println();
		}
		if (candidateInputFilter != null) {
			out.print(StringHelp.banner("RESULT INPUT FILTER", 53));
			out.println(candidateInputFilter);
			out.println();
		}

		if (targetOutputFilter != null) {
			out.print(StringHelp.banner("GROUND OUTPUT FILTER", 53));
			out.println(targetOutputFilter);
			out.println();
		}
		if (candidateOutputFilter != null) {
			out.print(StringHelp.banner("RESULT OUTPUT FILTER", 53));
			out.println(candidateOutputFilter);
			out.println();
		}

		out.print(StringHelp.banner("METRICS", 53));

		for (Iterator iter = evals.iterator(); iter.hasNext();) {
			Evaluation curr = (Evaluation) iter.next();
			curr.printMetricsTo(out);
			if (iter.hasNext())
				out
						.println("----------------------------------------------------");
		}
	}

	/**
	 * Get the scope rules for the the given evaluation.
	 * @param current the evaluation
	 * @return rules regarding which descriptor or attribute
	 * is worth evaluating or which should be marked as don't care 
	 */
	public EvaluationParameters.ScopeRules getScopeRulesFor(Evaluation current) {
		return new ScopeRules(current);
	}
	EvaluationParameters.ScopeRules getScopeRulesFor(Map evas) {
		return new ScopeRules(evas);
	}

	/**
	 * Gets all evaluations.
	 * @return the evaluations
	 */
	public Iterator getEvas() {
		return evals.iterator();
	}

	/**
	 * Rules for what attributes and descriptors are to
	 * be regarded as visible or important.
	 */
	public class ScopeRules {
		Map evas;
		
		/**
		 * Gets the ontology mapping this set of scope rules uses.
		 * @return the mapping
		 */
		public Equivalencies getMap() {
			return map;
		}
		
		/**
		 * Constructs a new set of scope rules.
		 * @param evas the associated ontology mapping
		 */
		public ScopeRules(Map evas) {
			this.evas = evas;
		}
		
		/**
		 * Constructs a new, empty set of scope rules.
		 * @param current map of descriptors to their evaluations
		 */
		public ScopeRules(Evaluation current) {
			evas = current.getMeasureMap();
		}
		
		/**
		 * Determine if two descriptors are comparable.
		 * @param T the target
		 * @param C the candidate
		 * @return true when the scope rules and ontology mapping
		 * indicate the two descriptors are compatable
		 */
		public boolean comparable(Descriptor T, Descriptor C) {
			return C.sameCategoryAs(T, map);
		}
		
		/**
		 * Determine if the target descriptor meets the output
		 * scoping rules.
		 * @param desc a target descriptor
		 * @return if the target is worthy of using for evaluation.
		 * <code>false</code> indicates a don't-care descriptor.
		 */
		public boolean isOutputableTarget(Descriptor desc) {
			return inScope(desc)
					&& (targetOutputFilter == null || targetOutputFilter
							.meetsCriteria(desc));
		}
		
		/**
		 * Determine if the candidate descriptor meets the output
		 * scoping rules.
		 * @param desc a candidate descriptor
		 * @return if the candidate is worthy of using for evaluation.
		 * <code>false</code> indicates a don't-care descriptor.
		 */
		public boolean isOutputableCandidate(Descriptor desc) {
			return inScope(desc)
					&& (candidateOutputFilter == null || candidateOutputFilter
							.meetsCriteria(desc));
		}
		
		/**
		 * Determine if the descriptor's type is compatable with 
		 * the current set of evaluations.
		 * @param desc the descriptor to check
		 * @return if the descriptor type is to among those evaluated
		 */
		public boolean inScope(Descriptor desc) {
			for (Iterator iter = evas.keySet().iterator(); iter.hasNext();) {
				if (comparable(desc, (Descriptor) iter.next())) {
					return true;
				}
			}
			return false;
		}

		/**
		 * Gets the major measure for the given attribute. Note that some
		 * evaluation types can have multiple measures for each attribute; for
		 * these, use the more appropriate
		 * {@link #getAllMeasuresFor(Descriptor)}method.
		 * 
		 * @param desc
		 *            the descriptor type
		 * @param attr
		 *            the attribute
		 * @return the attribute measure on the specified attribute
		 */
		public AttrMeasure getMeasure(Descriptor desc, String attr) {
			for (Iterator iter = evas.keySet().iterator(); iter.hasNext();) {
				Descriptor curr = (Descriptor) iter.next();
				if (comparable(desc, curr)) {
					Map attribMap = (Map) evas.get(curr);
					return (AttrMeasure) attribMap.get(attr);
				}
			}
			return null;
		}

		/**
		 * Return all current mappings for the given
		 * descriptor type
		 * @param desc the descriptor type to look up
		 * @return the measure map for the descriptor type
		 */
		public Map getAllMeasuresFor(Descriptor desc) {
			for (Iterator iter = evas.keySet().iterator(); iter.hasNext();) {
				Descriptor curr = (Descriptor) iter.next();
				if (comparable(desc, curr)) {
					return (Map) evas.get(curr);
				}
			}
			return new java.util.HashMap();
		}

		/**
		 * Gets the names of the attributes that are in scope. The ordering
		 * depends on the implementation of the evaluator, but it should be
		 * deterministic.
		 * 
		 * @param desc
		 *            The type of descriptor to check for scope.
		 * @return <code>java.util.Iterator</code> containing
		 *         <code>String</code> s
		 */
		public Iterator getInScopeAttributesFor(Descriptor desc) {
			Map m = getAllMeasuresFor(desc);
			return new ExceptIterator(efs, m.keySet().iterator());
		}
	}
	private static ExceptFramespans efs = new ExceptFramespans();
	private static class ExceptFramespans
			implements
				ExceptIterator.ExceptFunctor {
		/**
		 * Doesn't return the implicit frame span attribute.
		 * {@inheritDoc}
		 */
		public boolean check(Object o) {
			return !o.equals(" framespan");
		}
	}
}