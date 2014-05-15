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

import java.io.*;
import java.lang.reflect.*;
import java.util.*;

import viper.api.*;
import viper.api.extensions.*;

/**
 * Class that constructs viper data given the full URL of the data type.
 */
public class ViperDataFactoryImpl implements ViperDataFactory {
	/**
	 * URI for the boolean value type.
	 */
	public static final String BVALUE = ViperData.ViPER_DATA_URI + "bvalue";

	/**
	 * URI for the integer value type.
	 */
	public static final String DVALUE = ViperData.ViPER_DATA_URI + "dvalue";

	/**
	 * URI for the double floating point value type.
	 */
	public static final String FVALUE = ViperData.ViPER_DATA_URI + "fvalue";

	/**
	 * URI for the enumeration type.
	 */
	public static final String LVALUE = ViperData.ViPER_DATA_URI + "lvalue";

	/**
	 * URI for the character string type.
	 */
	public static final String SVALUE = ViperData.ViPER_DATA_URI + "svalue";

	/**
	 * URI for the bounding box type.
	 */
	public static final String BBOX = ViperData.ViPER_DATA_URI + "bbox";

	/**
	 * URI for the oriented bounding box type.
	 */
	public static final String OBOX = ViperData.ViPER_DATA_URI + "obox";

	/**
	 * URI for the point value type.
	 */
	public static final String POINT = ViperData.ViPER_DATA_URI + "point";

	/**
	 * URI for the polygon type.
	 */
	public static final String POLYGON = ViperData.ViPER_DATA_URI + "polygon";

	/**
	 * URI for the circle type.
	 */
	public static final String CIRCLE = ViperData.ViPER_DATA_URI + "circle";

	/**
	 * URI for the oriented ellipse type.
	 */
	public static final String ELLIPSE = ViperData.ViPER_DATA_URI + "ellipse";

	private Map<String, String> dataTypes;
	private Map<String, AttrValueWrapper> cachedTypes;

	public void helpLoadDefault(String type, Class<?> c) {
		String value = c.getName();
		dataTypes.put(type, value);
	}

	/**
	 * Constructs a new data factory containing the default types.
	 */
	public ViperDataFactoryImpl() {
		dataTypes = new HashMap<String, String>();
		cachedTypes = new WeakHashMap<String, AttrValueWrapper>();
		helpLoadDefault(BVALUE, Bvalue.class);
		helpLoadDefault(LVALUE, Lvalue.class);
		helpLoadDefault(DVALUE, Dvalue.class);
		helpLoadDefault(FVALUE, Fvalue.class);
		helpLoadDefault(SVALUE, Svalue.class);
		helpLoadDefault(POINT, Point.class);
		helpLoadDefault(POLYGON, Polygon.class);
		helpLoadDefault(BBOX, AttributeBbox.class);
		helpLoadDefault(OBOX, AttributeObox.class);
		helpLoadDefault(CIRCLE, CircleWrapper.class);
		helpLoadDefault(ELLIPSE, AttributeEllipse.class);
	}

	/**
	 * Adds the data types from the input stream.
	 * 
	 * @param props
	 *            the java properties file format string.
	 * @throws java.io.IOException
	 */
	public void remapTypes(InputStream props) throws java.io.IOException {
		Properties p = new Properties();
		p.load(props);
		copyToDataTypes(p);
	}

	private void copyToDataTypes(Properties p) {
		for (Object o : p.keySet()) {
			String k = (String) o;
			String v = p.getProperty(k);
			dataTypes.put(k, v);
		}
	}

	/**
	 * Adds the data types from the properties file.
	 * 
	 * @param props
	 *            the mapping from types to java class names
	 */
	public void loadTypes(Properties props) {
		copyToDataTypes(props);
	}

	/**
	 * Adds the given type, by namespace, local name and java class name
	 * 
	 * @param namespace
	 *            the namespace
	 * @param locator
	 *            the local part of the type name
	 * @param classname
	 *            the name of the class that implements {@link AttrValueWrapper}
	 */
	public void addType(String namespace, String locator, String classname) {
		dataTypes.put(namespace + locator, classname);
	}

	/**
	 * @see viper.api.extensions.ViperDataFactory#getAttribute(java.lang.String)
	 */
	public AttrValueWrapper getAttribute(String uri) {
		String className = dataTypes.get(uri);
		AttrValueWrapper newAttr = cachedTypes.get(className);
		if (newAttr == null && className != null) {
			Class<?>[] constructorType = {};
			Object[] constructorArguments = {};
			try {
				newAttr = ((AttrValueWrapper) (AttrValueWrapper.class
						.getClassLoader().loadClass(className).getConstructor(
								constructorType)
						.newInstance(constructorArguments)));
				cachedTypes.put(className, newAttr);
			} catch (ClassNotFoundException cnfx) {
				throw new UnknownAttributeTypeException(cnfx.getMessage()
						+ "\n\tAttribute type " + uri
						+ " not found (checked for " + className + ")");
			} catch (NoSuchMethodException nsmx) {
				throw new UnknownAttributeTypeException(nsmx.getMessage()
						+ "\n\tAttribute type " + uri
						+ " is improperly defined (missing constructor)");
			} catch (InstantiationException ix) {
				throw new UnknownAttributeTypeException(
						ix.getMessage()
								+ "\n\tAttribute type "
								+ uri
								+ "Attribute is improperly defined (not a concrete class)");
			} catch (IllegalAccessException iax) {
				throw new UnknownAttributeTypeException(iax.getMessage()
						+ "\n\tAttribute type " + uri
						+ " is missing or otherwise inaccessible");
			} catch (InvocationTargetException itx) {
				// This is an exception that wraps an exception thrown by
				// something
				// invoked. In this case, this is any exceptions thrown by the
				// constructor.
				itx.printStackTrace();
				throw new IllegalArgumentException(itx.getTargetException()
						.getMessage());
			}
		}
		return newAttr;
	}

	/**
	 * @see viper.api.extensions.ViperDataFactory#getTypes()
	 */
	public Iterator<String> getTypes() {
		return dataTypes.keySet().iterator();
	}
}
