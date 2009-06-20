/*
 * Copyright (c) 1997-2001 Alexandros Eleftheriadis, Yuntai Kyong and 
 * Danny Hong
 *
 * This file is part of Flavor, developed at Columbia University
 * (www.ee.columbia.edu/flavor)
 *
 * Flavor is free software; you can redistribute it and/or modify
 * it under the terms of the Flavor Artistic License as described in
 * the file COPYING.txt. 
 *
 */

/*
 * Authors:
 * Alexandros Eleftheriadis <eleft@ee.columbia.edu>
 * Yuntai Kyong <yuntaikyong@ieee.org>
 * Danny Hong <danny@ee.columbia.edu>
 *
 */


package edu.columbia.ee.flavor;
import java.io.*;


/**
 * Implementors of the IBitstream interface throw subclasses of this
 * exception base class.
 */
public class FlIOException extends IOException 
{
	private static int NUMBER_OF_EXCEPTION_TYPE = 10;
	
	/**
	 * End of data (exception condition)
	 */
	public static int ENDOFDATA = 0;
	
	/**
	 * Invalid alignment for align() method (exception condition)
	 */
	public static int INVALIDALIGNMENT = 1;
	
	/**
	 * The (little_)getxxx() or (little_)nextxxx() method failed (exception condition)
     */
	public static int READFAILED = 2;
	
	/**
	 * The (little_)putxxx() method failed (exception condition)
	 */
	public static int WRITEFAILED = 3;
	
	/**
	 * File open failed (exception condition)
	 */
	public static int FILEOPENFAILED = 4;
	
	/**
	 * System I/O operation failed (exception condition)
	 */
	public static int SYSTEMIOFAILED = 5;
	
	/**
	 * Unknown I/O type (exception condition)
	 */
	public static int INVALIDIOTYPE = 6;
	
	/**
	 * Invalid bitsize specified (exception condition)
	 */
	public static int INVALIDBITSIZE = 7;
	
	/**
	 * Not enough data in bitstream (exception condition)
	 */
	public static int NOTENOUGHDATA = 8;
	
	/**
	 * Unknown reason (exception condition)
	 */
	public static int UNKNOWN = 9;
	
	
	private int reason = UNKNOWN;
	private String system_error_msg = "";
	
	private static String errormsg[] =  {
		"End of Data",
		"Invalid Alignment",
		"Read I/O Failed",
		"Write I/O Failed",
		"File Open Failed",
		"System I/O Error",
		"Invalid Input Type",
		"Invalid bit size",
		"Not enough data",
		"Unknown Error"
	};
	
	/**
	 * Creates FlIOException 
	 */
	public FlIOException() {
		super();
	}
	
	/**
	 * Creates FlIOException with description
	 * @param s error detail message
	 */
	public FlIOException(String s) {
		super(s);
	}
	
	/**
	 * Creates FlIOException with exception condition
	 * @param error_code The error code representing the exception condition
	 */
	public FlIOException(int error_code) {
		super(errormsg[error_code>=NUMBER_OF_EXCEPTION_TYPE?NUMBER_OF_EXCEPTION_TYPE-1:error_code]);
		reason = error_code>=NUMBER_OF_EXCEPTION_TYPE?NUMBER_OF_EXCEPTION_TYPE-1:error_code;
	}
	
	/**
	 * Creates FlIOException with exception condition
	 * @param error_code The error code representing the exception condition
	 * @param error_msg Additional error description
	 */
	public FlIOException(int error_code, String error_msg) {
		super(errormsg[error_code>NUMBER_OF_EXCEPTION_TYPE?NUMBER_OF_EXCEPTION_TYPE-1:error_code]);
		reason = error_code>=NUMBER_OF_EXCEPTION_TYPE?NUMBER_OF_EXCEPTION_TYPE-1:error_code;
		system_error_msg = error_msg;
	}
	
	/**
	 * Converts exception to string
	 * @return Descriptive message about the error
	 */
	public String toString()  {
		if(reason != SYSTEMIOFAILED) 
			return super.toString();
		else
			return super.toString() + "\n" + system_error_msg;	
	}
}
