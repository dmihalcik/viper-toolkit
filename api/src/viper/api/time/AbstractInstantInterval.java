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

package viper.api.time;

import java.util.*;

import edu.umd.cfar.lamp.viper.util.*;

/**
 * An instant interval that doesn't implement getStartInstant and getEndInstant.
 */
public abstract class AbstractInstantInterval extends AbstractInterval implements InstantInterval {
	/**
	 * Gets the number of Instants in the Span. Basically,
	 * it returns <code>(int) getEnd().minus(getStart())</code>.
	 * @return the number of Instants in the Span.
	 */
	public long width() {
		return getEndInstant().minus(getStartInstant());
	}
	
	/**
	 * {@inheritDoc}
	 * @return {@link #getStartInstant()}
	 */
	public Comparable getStart() {
		return getStartInstant();
	}

	/**
	 * {@inheritDoc}
	 * @return {@link #getEndInstant()}
	 */
	public Comparable getEnd() {
		return getEndInstant();
	}
	
	/**
	 * {@inheritDoc}
	 * @return <code>{@link #getStartInstant()} instanceof Frame</code>
	 */
	public boolean isFrameBased() {
		return getStartInstant() instanceof Frame;
	}
	
	/**
	 * {@inheritDoc}
	 * @return <code>{@link #getStartInstant()} instanceof Time</code>
	 */
	public boolean isTimeBased() {
		return getStartInstant() instanceof Time;
	}
	
	/**
	 * {@inheritDoc}
	 */
	public Iterator iterator() {
		return new Iterator() {
			private Instant curr = getStartInstant();
			public void remove() {
				throw new UnsupportedOperationException();
			}
			public boolean hasNext() {
				return curr.compareTo(getEndInstant()) < 0;
			}

			public Object next() {
				Instant now = curr;
				curr = (Instant) curr.next();
				return now;
			}
		};
	}
}
