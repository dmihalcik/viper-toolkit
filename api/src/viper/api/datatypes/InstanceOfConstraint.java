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

package viper.api.datatypes;

import viper.api.*;
import viper.api.time.*;

/**
 * A simple param for Attributes that constrains the
 * value to instances of a given class or interface.
 * It defaults to java.lang.Object, meaning that it
 * allows any java object as a value.
 */
public class InstanceOfConstraint implements AttrValueWrapper {
	/**
	 * Creates a new InstanceOfConstraint with the given class
	 * as the constraint.
	 * @param constraint the class that any value must be an instance of
	 */
	public InstanceOfConstraint(Class constraint) {
		this.constraint = constraint;
	}
	
	/**
	 * Constructs a new constraint that accepts 
	 * any class that is a child of Object.
	 */
	public InstanceOfConstraint() {
		// defaults to Object as constraint
	}

	private Class constraint = Object.class;

	/**
	 * Returns the specified object, as it is assumed to be of the same type.
	 * @see viper.api.AttrValueWrapper#getObjectValue(Object, Node, Instant)
	 */
	public Object getObjectValue(Object o, Node container, Instant instant) {
		return convert(o, container, instant);
	}

	/**
	 * @see viper.api.AttrValueWrapper#setAttributeValue(Object, Node)
	 */
	public Object setAttributeValue(Object o, Node container) {
		o = convert(o, container, null);
		if (o != null && !constraint.isInstance(o)) {
			throw new BadAttributeDataException("Value not of proper type: "
			 + o + " ("+ o.getClass() +"), wanted " + constraint);
		}
		return o;
	}

	/**
	 * Tests to see if the specified object contains the 
	 * same constraint.
	 * @param o the object to compare with
	 * @return <code>true</code> if o constrians the types
	 * to the same class as this one
	 */
	public boolean equals(Object o) {
		if (this == o) {
			return true;
		} else if (o instanceof InstanceOfConstraint) {
			InstanceOfConstraint that = (InstanceOfConstraint) o;
			return that.constraint.equals(this.constraint);
		} else {
			return false;
		}
	}
	/**
	 * Gets a hash code for the type.
	 * @return <code>constraint.hashCode()</code>
	 */
	public int hashCode() {
		return constraint.hashCode();
	}
	
	protected Object convert(Object o, Node container, Instant instant) {
		return o;
	}
}
