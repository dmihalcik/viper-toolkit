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

package edu.umd.cfar.lamp.viper.gui.canvas;

import java.lang.reflect.*;
import java.util.*;
import java.util.logging.*;

import viper.api.*;

import com.hp.hpl.jena.rdf.model.*;

import edu.umd.cfar.lamp.apploader.prefs.*;
import edu.umd.cs.piccolo.*;

/**
 * A map from datatype names to ViewableAttribute objects,
 * with a convenience function to convert directly. You should
 * probably get the converter and use that, if you want to save
 * time on object creation.
 * @author davidm
 */
public class DataViewGenerator {
	public static final String CANVAS =
		"http://viper-toolkit.sourceforge.net/owl/gt/canvas#";
	public static final String VIEW_CREATOR = "viewCreator";
	public static Logger logger =
		Logger.getLogger("edu.umd.cfar.lamp.viper.gui.canvas");
	private Map converters;
	private PrefsManager prefs;
	private ModelListener ml = new ModelListener() {
		private Selector pred = new SimpleSelector(null,  
			ResourceFactory.createProperty(CANVAS + VIEW_CREATOR), (RDFNode) null);
		public void changeEvent(ModelEvent event) {
			for (Iterator i = (Iterator) event.getRemoved(); i.hasNext();) {
				Statement stmt = (Statement) i.next();
				String type = stmt.getSubject().toString();
				converters.remove(type);
			}
			for (Iterator i = (Iterator) event.getAdded(); i.hasNext();) {
				Statement stmt = (Statement) i.next();
				String type = stmt.getSubject().toString();
				String value = stmt.getObject().toString();
				addViewConverterByName(type, value);
			}
		}
		public Selector getSelector() {
			return pred;
		}
	};
	public DataViewGenerator() {
		converters = new HashMap();
	}

	public void setPrefs(PrefsManager prefs) {
		this.prefs = prefs;
		resetFromPrefs();
	}

	private void resetFromPrefs() {
		Property vcProp;
		prefs.model.enterCriticalSection(ModelLock.READ);
		try {
			vcProp = prefs.model.getProperty(CANVAS, VIEW_CREATOR);
			StmtIterator iter =
				prefs.model.listStatements(null, vcProp, (RDFNode) null);
			while (iter.hasNext()) {
				Statement curr = (Statement) iter.next();
				String type = curr.getSubject().toString();
				String value = curr.getObject().toString();
				addViewConverterByName(type, value);
			}
		} catch (RDFException e) {
			logger.severe(
				"Error while parsing prefs for Canvas: \n\t"
					+ e.getLocalizedMessage());
		} finally {
			prefs.model.leaveCriticalSection();
		}
	}

	public void addViewConverterByName(String type, String className) {
		Class[] constructorType = {
		};
		Object[] constructorArguments = {
		};
		ViewableAttribute newView;
		try {
			newView =
				((ViewableAttribute) (ViewableAttribute
					.class
					.getClassLoader()
					.loadClass(className)
					.getConstructor(constructorType)
					.newInstance(constructorArguments)));
		} catch (ClassNotFoundException cnfx) {
			throw new UnknownAttributeTypeException(
				cnfx.getMessage()
					+ "\n\tAttribute type "
					+ type
					+ " not found (checked for "
					+ className
					+ ")");
		} catch (NoSuchMethodException nsmx) {
			throw new UnknownAttributeTypeException(
				nsmx.getMessage()
					+ "\n\tAttribute type "
					+ type
					+ " is improperly defined (missing constructor)");
		} catch (InstantiationException ix) {
			throw new UnknownAttributeTypeException(
				ix.getMessage()
					+ "\n\tAttribute type "
					+ type
					+ "Attribute is improperly defined (not a concrete class)");
		} catch (IllegalAccessException iax) {
			throw new UnknownAttributeTypeException(
				iax.getMessage()
					+ "\n\tAttribute type "
					+ type
					+ " is missing or otherwise inaccessible");
		} catch (InvocationTargetException itx) {
			// This is an exception that wraps an exception thrown by something
			// invoked. In this case, this is any exceptions thrown by the constructor.
			throw new IllegalArgumentException(
				itx.getTargetException().getMessage());
		}
		this.addViewConverter(type, newView);
	}
	private String canonicalTypeName(String type) {
		if (type.indexOf(':') == -1
			&& type.indexOf('#') == -1
			&& type.indexOf('/') == -1) {
			type = ViperData.ViPER_DATA_URI + "#" + type;
		}
		return type;
	}
	public void addViewConverter(String type, ViewableAttribute view) {
		converters.put(canonicalTypeName(type), view);
	}

	public ViewableAttribute getViewConverter(String type) {
		return (ViewableAttribute) converters.get(canonicalTypeName(type));
	}

	public PNode convert(String type, Object o) {
		return getViewConverter(canonicalTypeName(type)).getViewable(o);
	}
}
