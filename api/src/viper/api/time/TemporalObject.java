package viper.api.time;

import java.io.*;

import viper.api.*;

/**
 * A span that takes a value.
 */
public class TemporalObject extends Span implements DynamicAttributeValue, Serializable {
	private Object o;
	
	/**
	 * Creates a new temporal object with the given value for
	 * the specified span.
	 * @param start the first instant in the span
	 * @param end the end of the span, exclusive
	 * @param value the value for the object
	 */
	public TemporalObject (Instant start, Instant end, Object value) {
		super(start, end);
		this.o = value;
	}
	
	/**
	 * Gets the object value
	 * @return the object value
	 */
	public Object getValue() {
		return o;
	}
}
