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

package viper.api;

import java.util.*;

import viper.api.time.*;

/**
 * Contains an instance of an attribute. If it is static, it may take
 * only one value. If dynamic, it may vary for the life of a 
 * descriptor.
 */
public interface Attribute extends TemporalNode {
	/**
	 * Sets the value of the attribute. Note that this method only
	 * works for static attributes; dynamic attributes must specify
	 * a range of frames or a time span to set.
	 * 
	 * @param v   the attribute value to set
	 * 
	 * @throws NotStaticException This method is thrown when trying to
	 *    use this on a dynamic attribute.
	 * @throws BadAttributeDataException If the specified object is 
	 *    not the right data type, or does not fulfill some other
	 *    requirements (eg not a possible lvalue).
	 */
	public void setAttrValue(Object v)
		throws NotStaticException, BadAttributeDataException;

	/**
	 * Sets the value of an attribute for the given set of frames
	 * or period of microseconds.
	 * When applied to a static attribute, it will set the value v.
	 * 
	 * @param v          the attribute value to set
	 * @param span       the first through last frame or microt to set
	  *
	 * @throws UnknownFrameRateException If the data is specified
	 *    in time, not frames, and there is no framerate specified
	 *    for this sourcefile.
	 * @throws BadAttributeDataException If the specified object is 
	 *    not the right data type, or does not fulfill some other
	 *    requirements (eg not a possible lvalue).
	 */
	public void setAttrValueAtSpan(Object v, InstantInterval span)
		throws UnknownFrameRateException, BadAttributeDataException;

	// Get Functions 
	/**
	 * Gets the attribute name.
	 * Convenience function for <code>getConfig().getName()</code>.
	 * @return the attribute name
	 */
	public String getAttrName();

	/**
	 * Gets the attribute value of static attributes.
	 * @return the attribute value. It may be <code>null</code>, if 
	 *     the attribute has just been created without a defualt 
	 *     (or with a default of NULL), or if it has been set to
	 *     <code>null</code>.
	 * @throws NotStaticException if called on a dynamic attribute
	 */
	public Object getAttrValue();

	/**
	 * Gets the attribute value of dynamic attributes at a specific
	 * Instant. If the frame is outside the descriptor's range, it will 
	 * return <code>null</code> for dynamic attributes, even those with 
	 * non-null defaults; but for static attributes, this always returns 
	 * the same thing as {@link #getAttrValue getAttrValue()} regardless 
	 * of the <code>i</code> parameter.
	 * @param i the Instant to check
	 * @return the value of the attribute at the given moment. If the 
	 *     Instant is out of bounds, return <code>null</code> for
	 *     dynamic attributes and the static value for static
	 *     attributes.
	 * @throws UnknownFrameRateException If the data is stored in time
	 *    format and the framerate is not specified for the sourcefile
	 *    or vice versa
	 */
	public Object getAttrValueAtInstant(Instant i);

	/**
	 * Gets the attribute value of dynamic attributes as they change over
	 * time. If the span is outside the descriptor's range, it will return
	 * an empty iterator. 
	 * @param s the span to check
	 * @return a java.util.Iterator of viper.api.DynamicAttributeValue
	 *     objects.
	 * @throws UnknownFrameRateException If the data is stored in time
	 *    format and the framerate is not specified for the sourcefile
	 */
	public Iterator getAttrValuesOverSpan(InstantInterval s);

	/**
	 * Returns an iterator over DynamicAttributeValue objects 
	 * for all valid values of the attribute. Equivalent to
	 * <code>desc.getAttr("a").getAttrValuesOverSpan(desc.getRange().getExtrema())</code>.
	 * This is the same as {@link #iterator()}.
	 * @return Iterator of DynamicAttributeValues
	 * @see #getAttrValuesOverSpan(Span)
	 */
	public Iterator getAttrValuesOverWholeRange();

	/**
	 * Returns an iterator of {@link DynamicAttributeValue} objects.
	 * This is the same as {@link #getAttrValuesOverWholeRange()}.
	 * @return an iterator of {@link DynamicAttributeValue} objects.
	 */
	public Iterator iterator();

	/**
	 * Gets the containing descriptor. Like {@link Node#getParent()},
	 * but you don't need an additional cast.
	 * @return the descriptor of which this attribute value is a 
	 *     member
	 */
	public Descriptor getDescriptor();

	/**
	 * Gets the AttrConfig describing this attribute.
	 * @return the AttrConfig that describes this attribute
	 */
	public AttrConfig getAttrConfig();

	/**
	 * Begins aggregating changes to the attribute, so that only one undo object 
	 * gets created.  Proper call order should be:
	 * startAggregating();
	 * aggregateSetAttrValueAtSpan(...);*
	 * finishAggregatin(...);
	 */
	public void startAggregating();
	

	/**
	 * Sets the value of an attribute for the given set of frames
	 * or period of microseconds.
	 * When applied to a static attribute, it will set the value v.
	 * 
	 * Changes are not recorded through the undo manager, and this must be used
	 * in conjunction with startAggregating() and stopAggregating()
	 * 
	 * @param v          the attribute value to set
	 * @param span       the first through last frame or microt to set
	  *
	 * @throws UnknownFrameRateException If the data is specified
	 *    in time, not frames, and there is no framerate specified
	 *    for this sourcefile.
	 * @throws BadAttributeDataException If the specified object is 
	 *    not the right data type, or does not fulfill some other
	 *    requirements (eg not a possible lvalue).
	 */
	public void aggregateSetAttrValueAtSpan(Object v, InstantInterval span)
	throws UnknownFrameRateException, BadAttributeDataException;
	
	/**
	 * Ends an aggregate session, launching the undoable event
	 * @param undoable Whether to launch an undoable event, false in case of an error
	 */
	public void finishAggregating(boolean undoable);
}
