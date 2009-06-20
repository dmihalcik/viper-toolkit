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

package viper.descriptors.attributes;

import java.lang.reflect.*;
import java.util.*;

/**
 * Some static methods for dealing with Viper Attributes.
 */
public class Attributes {
	
	/**
	 * The viper data URI
	 */
	public static final String DEFAULT_NAMESPACE_URI =
		"http://lamp.cfar.umd.edu/viperdata#";
	
	/**
	 * The default namespace qualifier to use with viper data.
	 * This is important, as xml parsers that don't understand namespaces
	 * may be looking for this as part of the element name.
	 */
	public static final String DEFAULT_NAMESPACE_QUALIFIER = "data:";
	
	private static Map typeMap;

	
	/**
	 * 
	 * @param name
	 * @param type
	 */
	public static void addAttributeType(String name, Class type) {
		typeMap.put(name, type);
	}
	
	static {
		String[] types = {"bbox", "bvalue", "circle", "dvalue", "ellipse", "fvalue", "lvalue", "obox", "point", "polygon", "svalue"};
		typeMap = new HashMap();
		for (int i = 0; i < types.length; i++) {
			Class c = Attributes.getClassForAttribute(types[i]);
			addAttributeType(DEFAULT_NAMESPACE_URI + types[i], c);
		}
		addAttributeType(" framespan", FrameSpan.class);
	}
	
	/**
	 * 
	 * @param type
	 * @return
	 * @throws IllegalArgumentException If the given type isn't registered.
	 */
	public static Class getClassForAttribute(String type) {
		if (typeMap.containsKey(type)) {
			return (Class) typeMap.get(type);
		} else {
			try {
				String attributeClassName =
					"viper.descriptors.attributes.Attribute_" + type;
				return Attribute.class.getClassLoader().loadClass(attributeClassName);
			} catch (ClassNotFoundException cnfx) {
				throw new IllegalArgumentException ("Not a registered Attribute type: " + type);
			}
		}
	}

	/**
	 * Tests to see if the given attribute type is composible.
	 * @param type the type of attribute to check
	 * @return <code>false</code> if it is not composable.
	 * @throws UnsupportedOperationException if the type is improperly defined
	 * @throws IllegalStateException if the <code>isComposable</code>
	 *      method of the type throws an exception.
	 */
	public static boolean isComposable(String type) {
		Measurable meas = AbstractAttribute.loadAttributeType(type);
		return meas instanceof Composable;
	}

	/**
	 * Determines if the specified string is a possible Attribute data type.
	 * @param type   The string to be tested.
	 * @return true if the string is a proper data type
	 */
	public static boolean isType(String type) {
		try {
			getClassForAttribute(type);
		} catch (IllegalArgumentException iax) {
			return false;
		}
		return true;
	}

	/**
	 * Tests to see if the string represents a possible value of the 
	 * given attribute type. Depends on the
	 * {@link Attribute#possibleValueOf(String)} method. 
	 * @param type the attribute to check
	 * @param value the value to check
	 * @return <code>false</code> if it is a bad value or an unknown type.
	 * @throws UnsupportedOperationException if possibleValueOf is not defined
	 *    for the given type
	 * @throws IllegalStateException if the <code>possibleValueOf</code>
	 *      method of the type throws an exception.
	 */
	public static boolean isGoodValue(Attribute type, String value) {
		Class[] signature = { String.class };
		Object[] arguments = { value };
		try {
			Method possibleMethod =
				type.getClass().getMethod(
					"possibleValueOf",
					signature);
			Boolean returnVal =
				(Boolean) possibleMethod.invoke(type, arguments);
			return returnVal.booleanValue();
		} catch (NoSuchMethodException nsmx) {
			throw new UnsupportedOperationException(
				nsmx.getMessage()
					+ "\n\tAttribute type "
					+ type
					+ " cannot test a value");
		} catch (IllegalAccessException iax) {
			throw new UnsupportedOperationException(
				iax.getMessage()
					+ "\n\tAttribute type "
					+ type
					+ " cannot test a value!");
		} catch (IllegalArgumentException iax) {
			throw new UnsupportedOperationException(
				iax.getMessage()
					+ "\n\tAttribute type "
					+ type
					+ " cannot test a value!!");
		} catch (InvocationTargetException itx) {
			/* This is an exception that wraps an exception thrown by something
			 * invoked. In this case, this is any exceptions thrown by the constructor.
			 */
			throw new IllegalStateException(
				itx.getTargetException().getMessage());
		}
	}

	/**
	 * Converts from a String into the internal data type used
	 * by the attribute. This works by invoking the appropriate Attribute
	 * class's "parseValue" command.
	 * @param type The attribute to test against.
	 * @param value The data to test. A String in the old format, or a 
	 *      org.w3c.dom.Element in the new format.
	 * @return a internal object
	 * @throws UnsupportedOperationException if parseValue is not defined
	 *    for the given attribute or value type
	 * @throws IllegalStateException if the <code>parseValue</code>
	 *      method of the type throws an exception.
	 */
	public static Object parseValue(Attribute type, Object value) {
		Class[] signature = { value.getClass()};
		Object[] arguments = { value };
		try {
			Method possibleMethod =
				type.getClass().getMethod(
					"parseValue",
					signature);
			return possibleMethod.invoke(type, arguments);
		} catch (NoSuchMethodException nsmx) {
			throw new UnsupportedOperationException(
				nsmx.getMessage()
					+ "\n\tAttribute type "
					+ type
					+ " cannot parse a value");
		} catch (IllegalAccessException iax) {
			throw new UnsupportedOperationException(
				iax.getMessage()
					+ "\n\tAttribute type "
					+ type
					+ " cannot parse a value!");
		} catch (IllegalArgumentException iax) {
			throw new UnsupportedOperationException(
				iax.getMessage()
					+ "\n\tAttribute type "
					+ type
					+ " cannot parse a value!!");
		} catch (InvocationTargetException itx) {
			/* This is an exception that wraps an exception thrown by something
			 * invoked. In this case, this is any exceptions thrown by the constructor.
			 */
			throw new IllegalStateException(
				itx.getTargetException().getMessage());
		}
	}

	/**
	 * Copies an int []. Just a handy helper, not really necessary.
	 * @return a copy of the old array
	 * @param oldA  the array to be copied
	 */
	static final int[] copyArray(int[] oldA) {
		int[] newA = new int[oldA.length];
		for (int j = 0; j < oldA.length; j++)
			newA[j] = oldA[j];
		return (newA);
	}

	/**
	 * Copies a String [].
	 * @return a copy of the old array
	 * @param oldA   the array to be copied
	 */
	static final String[] copyArray(String[] oldA) {
		String[] newA = new String[oldA.length];
		for (int j = 0; j < oldA.length; j++)
			newA[j] = new String(oldA[j]);
		return (newA);
	}

	/**
	 * Copies a boolean [].
	 * @return a copy of the old array
	 * @param oldA   the array to be copied
	 */
	static final boolean[] copyArray(boolean[] oldA) {
		boolean[] newA = new boolean[oldA.length];
		for (int j = 0; j < oldA.length; j++)
			newA[j] = oldA[j];
		return (newA);
	}
}