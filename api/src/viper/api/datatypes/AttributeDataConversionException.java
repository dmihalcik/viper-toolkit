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

/**
 * Indicates no conversion exists from the given data type
 * to the current data type. Possibly thrown by any attribute
 * data access method, since conversion might be performed lazily.
 * @author davidm
 */
public class AttributeDataConversionException extends BadAttributeDataException {
	/**
	 * 
	 */
	public AttributeDataConversionException() {
		super();
	}

	/**
	 * @param message
	 */
	public AttributeDataConversionException(String message) {
		super(message);
	}
}
