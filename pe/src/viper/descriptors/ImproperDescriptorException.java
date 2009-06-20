/*******************************************************************************
 * ViPER * The Video Processing * Evaluation Resource * * Distributed under the
 * GPL license * Terms available at gnu.org. * * Copyright University of
 * Maryland, * College Park. *
 ******************************************************************************/

package viper.descriptors;

import edu.umd.cfar.lamp.viper.util.*;

/**
 * Indicates a descriptor was attempted to be used that 
 * was not defined in the descriptor config section.
 */
public class ImproperDescriptorException extends BadDataException {
	
	/**
	 * Constructs a new exception with no detail message.
	 */
	public ImproperDescriptorException() {
	}

	/**
	 * Constructs a new exception with the given
	 * detail message.
	 * @param s the detail message
	 */
	public ImproperDescriptorException(String s) {
		super(s);
	}
	
	/**
	 * Constructs a new exception with the given
	 * detail message and line localization information.
	 * @param s the detail message
	 * @param startCharacter the first character where the error
	 * may have started
	 * @param endCharacter the last character of the error region
	 */
	public ImproperDescriptorException(String s, int startCharacter,
			int endCharacter) {
		super(s, startCharacter, endCharacter);
	}
}