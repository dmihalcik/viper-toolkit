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

package viper.descriptors;

import java.util.*;

import org.w3c.dom.*;

import viper.comparison.*;
import viper.descriptors.attributes.*;
import edu.umd.cfar.lamp.viper.util.*;

/**
 * Acts as a composition of Descriptors.
 */
public class DescAggregate extends Descriptor {
	TreeSet idList;

	/**
	 * Constructs a new aggregate descriptor with the
	 * given category, e.g. "CONTENT".
	 * @param designation the descriptor type, e.g. "CONTENT"
	 * @throws BadDataException if the designation is
	 * invalid.
	 */
	public DescAggregate(String designation) throws BadDataException {
		super();
		idList = new TreeSet();
		if (!Descriptor.isCategory(designation)) {
			throw new BadDataException("Bad descriptor category -- "
					+ designation);
		} else {
			setCategory(designation);
		}
	}

	/**
	 * Constructs a new aggregate descriptor that contains the 
	 * contents of the given single descriptor.
	 * @param D the descriptor
	 * @throws BadDataException from parent constructor
	 */
	public DescAggregate(DescSingle D) throws BadDataException {
		super();
		setName(D.getName());
		attributes = new Attribute[D.attributes.length];
		for (int i = 0; i < D.attributes.length; i++)
			attributes[i] = (Attribute) D.attributes[i].clone();

		span = (FrameSpan) D.span.clone();
		idList = new TreeSet();
		idList.add(new Integer(D.id));

		setCategory(D.getCategory());
	}

	/**
	 * Generates a new Descriptor Object sharing none of the references of the
	 * original but containing identical data.
	 * 
	 * @return new Descriptor initialized with this Descriptor's data
	 */
	public Object clone() {
		DescAggregate temp;
		try {
			temp = new DescAggregate(getCategory());
		} catch (BadDataException bdx) {
			throw new IllegalStateException(bdx.getMessage());
		}
		temp.setName(getName());
		temp.attributes = new Attribute[attributes.length];
		for (int i = 0; i < attributes.length; i++)
			temp.attributes[i] = (Attribute) attributes[i].clone();

		temp.idList = (TreeSet) idList.clone();
		temp.span = (span == null) ? null : (FrameSpan) span.clone();
		return temp;
	}

	/***************************************************************************
	 * ------------------------------------------------------------------------
	 * ------------------------------------------------------------------------
	 */
	/**
	 * Returns the ID number of the descriptor.
	 * 
	 * @return the ID number of the descriptor
	 */
	public Object getID() {
		return idList;
	}

	void setID(int id) {
		this.idList.add(new Integer(id));
	}

	/**
	 * Gets the number of descriptor ids associated
	 * with this aggregate descriptor. This is equal to 
	 * the number of single descriptors this aggregate 
	 * contains.
	 * @return the number of ids
	 */
	public int numIDs() {
		return idList.size();
	}

	/**
	 * @inheritDoc
	 */
	public FrameSpan getFrameSpan() {
		return span;
	}

	/**
	 * @inheritDoc
	 */
	public void moveFrame(int offset) {
		span.shift(offset);
	}

	/**
	 * @inheritDoc
	 */
	public void setFrameSpan(FrameSpan span) {
		this.span = span;
	}

	/**
	 * Returns a new descriptor eqiuvalent to this+D. Remember, composition is
	 * not commutative for all attributes, and may not exist for some. To
	 * compose, remember to call {@linkDescriptor#isComposable() isComposable}
	 * first and try both <code>A.compose (B)</code> and
	 * <code>B.compose (A)</code>.
	 * 
	 * @param D
	 *            The Descriptor to compose with this Descriptor.
	 * @param scope
	 *            The attribute scope and mapping.
	 * @return A new Descriptor that is equivalent to the composition of this
	 *         and the argument D.
	 * @throws BadDataException
	 *             if the compose semantics are not correct.
	 * @throws UncomposableException
	 *             this instance of the descriptor cannot be composed. Check
	 *             isComposable!
	 */
	public Descriptor compose(Descriptor D,
			EvaluationParameters.ScopeRules scope) throws BadDataException,
			UncomposableException {
		DescAggregate temp = (DescAggregate) this.clone();
		// Unify Descriptions and Spans
		temp.span = temp.span.union(D.getFrameSpan());
		if (D.getClass().equals(DescSingle.class)) {
			if (idList.contains(D.getID()))
				throw new BadDataException(
						"Attempting to compose the same descriptor multiple times");
			else
				temp.idList.add(D.getID());
		} else {
			// First, check to see if there are any dup ID numbers
			Iterator iterA = idList.iterator();
			Iterator iterB = ((TreeSet) D.getID()).iterator();
			Comparable A = (Comparable) iterA.next();
			Comparable B = (Comparable) iterB.next();

			/// difference will be negative iff A < B, and positive iff A > B.
			// I wish java had operator overloading. I really do.
			double difference = A.compareTo(B);
			while (iterA.hasNext() && iterB.hasNext() && (difference != 0)) {
				while ((difference < 0) && (iterA.hasNext() && iterB.hasNext())) {
					A = (Comparable) iterA.next();
					difference = A.compareTo(B);
				}
				while ((difference > 0) && (iterA.hasNext() && iterB.hasNext())) {
					B = (Comparable) iterB.next();
					difference = A.compareTo(B);
				}
			}
			if (difference == 0)
				// If there was a dup ID num, return 0
				throw new BadDataException(
						"Attempting to compose the same descriptor multiple times");
			temp.idList.addAll(((DescAggregate) D).idList);
		}

		// Unify Attributes
		String errMsg = null;
		for (Iterator iter = scope.getInScopeAttributesFor(temp); iter
				.hasNext();) {
			String currAttrName = (String) iter.next();
			int i = temp.getAttributeIndex(currAttrName, scope.getMap());
			try {
				temp.attributes[i] = Attribute.compose(this.span, this
						.getAttribute(currAttrName, scope.getMap()), D.span, D
						.getAttribute(currAttrName, scope.getMap()));
			} catch (UncomposableException ux) {
				if (errMsg == null) {
					errMsg = ux.getMessage();
				} else {
					errMsg += "\n" + ux.getMessage();
				}
			}
		}
		if (errMsg != null)
			System.err.println(errMsg + "\n fix your .epf");
		return temp;
	}

	/**
	 * Gets an XML representation of the descriptor.
	 * <em>FIXME :: Right now, just throws Unsupported Exception.</em>
	 * 
	 * @see Descriptor#getXMLFormat(Document)
	 * @throws UnsupportedOperationException
	 */
	public Element getXMLFormat(Document root) {
		throw new UnsupportedOperationException(
				"Cannot get XML for Aggregate data yet.");
	}


	/**
	 * @inheritDoc
	 */
	public Descriptor crop(FrameSpan span) {
		span.intersectWith(this.span);

		DescAggregate copy;
		try {
			copy = new DescAggregate(getCategory());
		} catch (BadDataException bdx) {
			throw new IllegalStateException(bdx.getMessage());
		}
		copy.setName(getName());
		copy.attributes = new Attribute[attributes.length];
		for (int i = 0; i < attributes.length; i++) {
			copy.attributes[i] = attributes[i].crop(span, this.span);
		}

		copy.idList = (TreeSet) idList.clone();
		copy.span = span;
		return copy;
	}

}

