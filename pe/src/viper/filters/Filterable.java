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
 * This interface is used to allow the FILTER to work on Attributes and the
 * FrameSpan.
 */
public interface Filterable {
	/**
	 * This object tests a given value against a rule.
	 */
	public static interface Rule {
		/**
		 * The function method of this object, <code>passes</code> determines
		 * if the filterable object passes.
		 * 
		 * @param o
		 *            the measurable object to test
		 * @return <code>true</code> if the Filterable object passes the rule.
		 */
		public boolean passes(Measurable o);

		/**
		 * Tells the filter whether to apply short circuiting.
		 * 
		 * @return
		 */
		public boolean isShortCircuit();
	}

	/**
	 * This converts a string into a rule, writing out errors in the format to
	 * <code>err</code>.
	 * 
	 * @param unparsedRule
	 *            A string containing a single rule.
	 * @param unparsedValues
	 *            A string containing values.
	 * @param err
	 *            A place to log the errors.
	 * @return A List containing a rule (String) and value (Object) pair.
	 * @throws BadDataException
	 */
	public Rule convertRule(String unparsedRule, List unparsedValues,
			ErrorWriter err) throws BadDataException;

	/**
	 * Tell if a name is a valid rule or not
	 * 
	 * @param ruleName
	 *            the name of the rule
	 * @return true if the rule is valid.
	 */
	public boolean isValidRule(String ruleName);

	/**
	 * Tests to see if the value passes the given rule.
	 * @param rule the rule to check this value against
	 * @return if this value is good according to the given rule
	 */
	public boolean passes(Rule rule);
}