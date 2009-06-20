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


package edu.umd.cfar.lamp.apploader.misc;

import java.io.*;

/**
 * A printstream to a file. This is designed to redirect
 * the system streams to a file, while still printing out
 * the system stream to the console.
 */
public class TargetedPrintStream extends PrintStream {

	private PrintStream original;
	
	/**
	 * Constructs a new two-target output stream.
	 * @param original the original stream
	 * @param out the secondary stream
	 */
	public TargetedPrintStream(PrintStream original, OutputStream out) {
		super(out);
		this.original = original;
	}
	/**
	 * Constructs a new two-target output stream.
	 * @param original the original stream
	 * @param out the secondary stream
	 * @param autoFlush sets the autoflush option on the secondary stream
	 */
	public TargetedPrintStream(PrintStream original, OutputStream out, boolean autoFlush) {
		super(out, autoFlush);
		this.original = original;
	}
	
	/**
	 * 
	 * @param original the original output stream
	 * @param out where to send output
	 * @param autoFlush to flush the buffer after every write method call
	 * @param encoding a supported character encoding
	 * @throws UnsupportedEncodingException
	 */
	public TargetedPrintStream(PrintStream original, OutputStream out, boolean autoFlush, String encoding) throws UnsupportedEncodingException {
		super(out, autoFlush, encoding);
		this.original = original;
	}

	/** @inheritDoc */
	public boolean checkError() {
		return original.checkError();
	}
	
	/** @inheritDoc */
	public void close() {
		super.close();
		original.close();
	}
	
	/** @inheritDoc */
	public boolean equals(Object arg0) {
		return original.equals(arg0) && super.equals(arg0);
	}
	
	/** @inheritDoc */
	public void flush() {
		super.flush();
		original.flush();
	}
	
	/** @inheritDoc */
	public int hashCode() {
		return original.hashCode() ^ super.hashCode();
	}
	
	/** @inheritDoc */
	public void print(boolean arg0) {
		super.print(arg0);
		original.print(arg0);
	}
	
	/** @inheritDoc */
	public void print(char arg0) {
		super.print(arg0);
		original.print(arg0);
	}
	
	/** @inheritDoc */
	public void print(char[] arg0) {
		super.print(arg0);
		original.print(arg0);
	}
	
	/** @inheritDoc */
	public void print(double arg0) {
		super.print(arg0);
		original.print(arg0);
	}
	
	/** @inheritDoc */
	public void print(float arg0) {
		super.print(arg0);
		original.print(arg0);
	}
	
	/** @inheritDoc */
	public void print(int arg0) {
		super.print(arg0);
		original.print(arg0);
	}
	
	/** @inheritDoc */
	public void print(Object arg0) {
		super.print(arg0);
		original.print(arg0);
	}
	
	/** @inheritDoc */
	public void print(String arg0) {
		super.print(arg0);
		original.print(arg0);
	}
	
	/** @inheritDoc */
	public void print(long arg0) {
		super.print(arg0);
		original.print(arg0);
	}
	
	/** @inheritDoc */
	public void println() {
		super.println();
		original.println();
	}
	
	/** @inheritDoc */
	public void println(boolean arg0) {
		super.println(arg0);
		original.println(arg0);
	}
	
	/** @inheritDoc */
	public void println(char arg0) {
		super.println(arg0);
		original.println(arg0);
	}
	
	/** @inheritDoc */
	public void println(char[] arg0) {
		super.println(arg0);
		original.println(arg0);
	}
	
	/** @inheritDoc */
	public void println(double arg0) {
		super.println(arg0);
		original.println(arg0);
	}
	
	/** @inheritDoc */
	public void println(float arg0) {
		super.println(arg0);
		original.println(arg0);
	}
	
	/** @inheritDoc */
	public void println(int arg0) {
		super.println(arg0);
		original.println(arg0);
	}
	
	/** @inheritDoc */
	public void println(Object arg0) {
		super.println(arg0);
		original.println(arg0);
	}
	
	/** @inheritDoc */
	public void println(String arg0) {
		super.println(arg0);
		original.println(arg0);
	}
	
	/** @inheritDoc */
	public void println(long arg0) {
		super.println(arg0);
		original.println(arg0);
	}
	
	/** @inheritDoc */
	public void write(byte[] arg0) throws IOException {
		super.write(arg0);
		original.write(arg0);
	}
	
	/** @inheritDoc */
	public void write(byte[] arg0, int arg1, int arg2) {
		super.write(arg0, arg1, arg2);
		original.write(arg0, arg1, arg2);
	}
	
	/** @inheritDoc */
	public void write(int arg0) {
		super.write(arg0);
		original.write(arg0);
	}
	
	/**
	 * Gets the original stream.
	 * @return
	 */
	public PrintStream getOriginal() {
		return original;
	}
}
