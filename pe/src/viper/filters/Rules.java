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

package viper.filters;

import java.util.*;

import viper.descriptors.attributes.*;
import edu.umd.cfar.lamp.viper.util.*;

/**
 * This class is simply a bunch of factory classes for standard rule types.
 */
public class Rules {
	/**
	 * Gets a rule that always returns false.
	 * 
	 * @return the rule that always fails
	 */
	public static Filterable.Rule getFalse() {
		return new Filterable.Rule() {
			public boolean passes(Measurable o) {
				return false;
			}

			public boolean isShortCircuit() {
				return true;
			}
		};
	}

	/**
	 * Gets a rule that always returns true.
	 * 
	 * @return the rule that always passes
	 */
	public static Filterable.Rule getTrue() {
		return new Filterable.Rule() {
			public boolean passes(Measurable o) {
				return true;
			}

			public boolean isShortCircuit() {
				return true;
			}
		};
	}

	/**
	 * Gets a rule that tests for equality with the target object.
	 * 
	 * @param o
	 *            The object to test against.
	 * @return rule that tests for equality
	 */
	public static Filterable.Rule getEquality(Object o) {
		return new Equals(o);
	}

	/**
	 * Tests to see if two objects are not equal.
	 * 
	 * @param o
	 *            the object to test against.
	 * @return <code>true</code> iff o != the tested object.
	 */
	public static Filterable.Rule getInequality(Object o) {
		return new NotEquals(o);
	}

	/**
	 * Gets a relation operation. If relation is <code>==</code> or
	 * <code>!=</code>, it uses the <code>equals</code> method of the
	 * object; otherwise, it casts the object to
	 * <code>java.lang.Comparable</code>.
	 * 
	 * @param o
	 *            The object to compare.
	 * @param relation
	 *            String representing the relation, eg "&lt;" or "&gt;=" <!-- eg
	 *            ">" or " <", not the entities, those ares for javadoc reasons.
	 *            -->
	 * @return the given comparison rule
	 * @throws BadDataException
	 */
	public static Filterable.Rule getComparisonRelation(Object o,
			String relation) throws BadDataException {
		if ("==".equals(relation)) {
			return new Equals(o);
		} else if ("!=".equals(relation)) {
			return new NotEquals(o);
		} else if (StringHelp.isRelationalOperator(relation)) {
			if (o instanceof Comparable) {
				return new Comparison((Comparable) o, relation);
			} else {
				throw new BadDataException("This data cannot be compared: " + o);
			}
		} else {
			throw new BadDataException("Not a valid comparison: " + relation);
		}
	}

	/**
	 * Parses a rule of the form <code><i>rule</i> "<i>value</i>"
	 *  (<em>||</em> / <em>&&</em>) ...</code>
	 * <br />
	 * The conjunctions and disjunctions have same precedence; to specify
	 * precedence, use parentheses
	 * 
	 * @param f
	 *            the rule type
	 * @param complexRule
	 *            the string describing the rule
	 * @param err
	 *            the error stream
	 * @return the complex rule
	 * @throws IllegalArgumentException
	 *             if the first rule was not valid. If a later rule is in error,
	 *             it prints an error to the error writer and returns the valid
	 *             rule so far. To get an exception on all errors, set err to
	 *             <code>null</code>. Note that in this case, warnings (lack
	 *             of quotes) will be ignored.
	 */
	public static Filterable.Rule getComplexRule(Filterable f,
			String complexRule, ErrorWriter err)
			throws IllegalArgumentException {
		Filterable.Rule resultingRule = null;
		String rule = null;
		String logicalOp = null;
		int index = 0;

		complexRule = complexRule.trim();

		try {
			while (!complexRule.equals("")) {
				if (resultingRule != null) {
					// Meaning that something came before, should have been a
					// <statement>
					// Parse the logical rule
					index = 2;
					logicalOp = complexRule.substring(0, index);
					if (!StringHelp.isLogicalOperator(logicalOp)) {
						String msg = "This is not a logical operator (&& or ||): "
								+ logicalOp;
						if (err != null) {
							err.printError(msg);
							return resultingRule;
						} else {
							throw new IllegalArgumentException(msg);
						}
					}
					if (index >= complexRule.length()) {
						String msg = "Rule has dangling " + logicalOp + ": "
								+ complexRule;
						if (err != null) {
							err.printError(msg);
							return resultingRule;
						} else {
							throw new IllegalArgumentException(msg);
						}
					}
					complexRule = complexRule.substring(index).trim();
					index = 0;
				}

				// Parse the next rule.
				// There are three statement types:
				//     (<complex rule>) ...
				//     <relational_op><quoted_value> ...
				//     <relational_op>(list of values) ...
				// where '...' is <log_op rule>*, to be parsed next time through
				// loop
				Filterable.Rule nextRule = null;
				if (complexRule.startsWith("(")) {
					// hideous multipass parsing... varies with the number of
					// parens.
					int depth = 1;
					while (depth > 0) {
						index++;
						if (complexRule.charAt(index) == '(') {
							depth++;
						} else if (complexRule.charAt(index) == ')') {
							depth--;
						}
					}
					nextRule = Rules.getComplexRule(f, complexRule.substring(1,
							index - 1).trim(), err);
					if (index + 1 < complexRule.length()) {
						complexRule = complexRule.substring(index).trim();
					} else {
						complexRule = "";
					}
				} else {
					// 1) Parsing the rule name
					int nextChar = complexRule.charAt(index++);
					while (nextChar != '"' && nextChar != ' '
							&& nextChar != '\t' && nextChar != '(') {
						nextChar = complexRule.charAt(++index);
					}
					rule = complexRule.substring(0, index).trim();
					if (!f.isValidRule(rule)) {
						String msg = "Not a valid rule for this type: " + rule;
						if (resultingRule != null && err != null) {
							err.printError(msg);
							return resultingRule;
						} else {
							throw new IllegalArgumentException(msg);
						}
					}

					complexRule = complexRule.substring(index).trim();
					index = 0;

					if (complexRule.length() == 0) {
						if (resultingRule != null && err != null) {
							// unary operators must pass "" or () the way it
							// currently works.
							err
									.printError("Rule found without a matching value: "
											+ rule);
							return resultingRule;
						} else {
							throw new IllegalArgumentException(
									"Rule found without a matching value: "
											+ rule);
						}
					}
					List values = null;
					try {
						if (complexRule.charAt(index) == '(') {
							// FIXME: does not work when strings contain parens,
							// eg for polygons
							index = complexRule.indexOf(")");
							values = StringHelp.getQuotedList(complexRule
									.substring(1, index));
							index++;
						} else if (complexRule.charAt(index) == '"') {
							while (++index < complexRule.length()
									&& complexRule.charAt(index) != '"') {
								if (complexRule.charAt(index) == '\\') {
									index++;
								}
							}
							index++;
							values = StringHelp.getQuotedList(complexRule
									.substring(0, index));
						} else {
							while (index < complexRule.length()
									&& complexRule.charAt(index) != ' '
									&& complexRule.charAt(index) != '\t')
								index++;
							values = new LinkedList();
							values.add(complexRule.substring(0, index));
							complexRule = complexRule.substring(index).trim();
							if ("NULL".equals(values.get(0))) {
								values.set(0, null);
							} else if (err != null) {
								err
										.printWarning("Rule values should be in quotes: "
												+ values.get(0));
							}
							index = 0;
						}
					} catch (BadDataException bdx) {
						if (err != null) {
							err.printWarning(bdx.getMessage());
						}
					}
					if (index >= complexRule.length()) {
						complexRule = "";
					} else {
						complexRule = complexRule.substring(index).trim();
					}
					try {
						nextRule = f.convertRule(rule, values, err);
					} catch (BadDataException bdx) {
						if (err != null && resultingRule != null) {
							err
									.printError("Rule/value pair is invalid, skipping: "
											+ rule);
						} else {
							throw new IllegalArgumentException(
									"Invalid rule/value pair: "
											+ rule
											+ "("
											+ StringHelp
													.getQuotedListString(values)
											+ ")\n\t" + bdx.getMessage());
						}
					}
				}

				index = 0;

				if (resultingRule == null) {
					resultingRule = nextRule;
				} else if (logicalOp.equals("&&")) {
					resultingRule = new Conjunction(resultingRule, nextRule);
				} else if (logicalOp.equals("||")) {
					resultingRule = new Disjunction(resultingRule, nextRule);
				} else {
					throw new IllegalArgumentException(
							"Invalid binary logical operator: " + logicalOp);
				}
			}
			return resultingRule;
		} catch (StringIndexOutOfBoundsException siioobx) {
			if (err != null) {
				err.printError("Ill formed rule: " + complexRule);
			}
			throw new IllegalArgumentException("Error while parsing rule: "
					+ complexRule);
		}
	}

	/**
	 * Returns true if the checked value equals the rule value.
	 */
	private static class Equals implements Filterable.Rule {
		private Object value;

		Equals(Object o) {
			value = o;
		}

		/** @inheritDoc */
		public boolean passes(Measurable o) {
			return value == o
					|| ((value != null) && (o != null) && value.equals(o));
		}

		/** @inheritDoc */
		public String toString() {
			return "== "
					+ (value == null ? "NULL" : ("\""
							+ StringHelp.backslashify(value.toString()) + "\""));
		}

		/**
		 * Tells the filter whether to apply short circuiting.
		 * 
		 * @return <code>true</code>
		 */
		public boolean isShortCircuit() {
			return true;
		}

	}

	/**
	 * Returns true if the checked value does not equal the rule value.
	 */
	private static class NotEquals implements Filterable.Rule {
		private Object value;

		NotEquals(Object o) {
			value = o;
		}

		/** @inheritDoc */
		public boolean passes(Measurable o) {
			if (value == null) {
				return o != null;
			} else {
				return !value.equals(o);
			}
		}

		/** @inheritDoc */
		public String toString() {
			return "!= "
					+ (value == null ? "NULL" : ("\""
							+ StringHelp.backslashify(value.toString()) + "\""));
		}

		/**
		 * Tells the filter whether to apply short circuiting.
		 * 
		 * @return <code>true</code>
		 */
		public boolean isShortCircuit() {
			return true;
		}

	}

	/**
	 * Does not bother doing == & !=, as those have their own already.
	 */
	private static class Comparison implements Filterable.Rule {
		private Comparable value;

		/**
		 * -2 : strictly less than -1 : less than or equal to 1 : greater than
		 * or equal to 2 : strictly greater than
		 */
		private int rel;

		Comparison(Comparable o, String relation) {
			value = o;
			rel = StringHelp.getRelationalOperatorEnum(relation);
			if (rel < 0) {
				throw new IllegalArgumentException("Not a valid comparison: "
						+ relation);
			}
		}

		/** @inheritDoc */
		public boolean passes(Measurable o) {
			int result = -value.compareTo(o);
			// since this is backwards, the negation inverts the sign.
			// This may have unexpected side effects if compareTo is not
			// implemented properly.
			switch (rel) {
			case StringHelp.REL_LT:
				return result < 0;
			case StringHelp.REL_LTEQ:
				return result <= 0;
			case StringHelp.REL_EQ:
				return result == 0;
			case StringHelp.REL_GTEQ:
				return result >= 0;
			case StringHelp.REL_GT:
				return result > 0;

			case StringHelp.REL_NEQ:
				return result != 0;

			default:
				throw new IllegalStateException(
						"Initiated comparison with invalid comparison type: "
								+ rel);
			}
		}

		/** @inheritDoc */
		public String toString() {
			return StringHelp.getRelationalOperatorString(rel) + " \""
					+ StringHelp.backslashify(value.toString()) + "\"";
		}

		/**
		 * Tells the filter whether to apply short circuiting.
		 * 
		 * @return <code>true</code>
		 */
		public boolean isShortCircuit() {
			return true;
		}

	}

	/**
	 * Returns true if both of the passed rules return true.
	 */
	private static class Conjunction implements Filterable.Rule {
		private Filterable.Rule a;

		private Filterable.Rule b;

		Conjunction(Filterable.Rule a, Filterable.Rule b) {
			this.a = a;
			this.b = b;
		}

		/** @inheritDoc */
		public boolean passes(Measurable o) {
			return a.passes(o) && b.passes(o);
		}

		/** @inheritDoc */
		public String toString() {
			return a + " && " + b;
		}

		/**
		 * Tells the filter whether to apply short circuiting.
		 * 
		 * @return <code>true</code> iff both child rules allow short
		 *         circuiting
		 */
		public boolean isShortCircuit() {
			return a.isShortCircuit() && b.isShortCircuit();
		}

	}

	/**
	 * Returns true if one of the passed rules return true.
	 */
	private static class Disjunction implements Filterable.Rule {
		private Filterable.Rule a;

		private Filterable.Rule b;

		Disjunction(Filterable.Rule a, Filterable.Rule b) {
			this.a = a;
			this.b = b;
		}

		/** @inheritDoc */
		public boolean passes(Measurable o) {
			return a.passes(o) || b.passes(o);
		}

		/** @inheritDoc */
		public String toString() {
			return a + " || " + b;
		}

		/**
		 * Tells the filter whether to apply short circuiting.
		 * 
		 * @return <code>true</code> iff both child rules allow short
		 *         circuiting
		 */
		public boolean isShortCircuit() {
			return a.isShortCircuit() && b.isShortCircuit();
		}

	}
}

