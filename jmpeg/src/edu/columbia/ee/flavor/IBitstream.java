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


/**
 * The definition of the Bitstream I/O interface,
 * this is defines the methods that the Flavor translator
 * expects from the underlying class that performs bitstream I/O.
 * @see edu.columbia.ee.flavor.FlIOException
 */
public interface IBitstream
{
	/** 
 	 * Input bitstream type
     */
	public static final int BS_INPUT = 0;	
	
	/** 
 	 * Output bitstream type
     */
	public static final int BS_OUTPUT = 1;

	/**************/
	/* Big endian */
	/**************/

    /**
	 * Gets next 'n' bits as <b>unsigned</b> value (input only)
	 * @param n The number of bits to get
	 * @return The value of next n bits
	 * @exception FlIOException if an I/O error occurs
	 */
	int getbits(int n) throws FlIOException;
	
	/**
	 * Gets next 'n' bits as <b>signed</b> value (input only)
	 * @param n The number of bits to get
	 * @return The value of next n bits
	 * @exception FlIOException if an I/O error occurs
	 */
    int sgetbits(int n) throws FlIOException;
	
	/**
	 * Probes 'n' bits as <b>unsigned</b> value (input only)
	 * @param n The number of bits to get
	 * @return The value of next n bits
	 * @exception FlIOException if an I/O error occurs
	 */
    int nextbits(int n) throws FlIOException;
    
	/**
	 * Probes 'n' bits as <b>signed</b> value (input only)
	 * @param n The number of bits to get
	 * @return The value of next n bits
	 * @exception FlIOException if an I/O error occurs
	 */
	int snextbits(int n) throws FlIOException;
	
    /**
	 * Gets next float value in next 32 bits (input only)
	 * @return The float value
	 * @exception FlIOException if an I/O error occurs
	 */
    float getfloat() throws FlIOException;;
	
	/**
	 * Probes next float value in next 32 bits (input only)
	 * @return The float value
	 * @exception FlIOException if an I/O error occurs
	 */
    float nextfloat() throws FlIOException;
   
    /**
	 * Gets next double value in next 32 bits (output only)
	 * @return The double value
	 * @exception FlIOException if an I/O error occurs
	 */
    double getdouble() throws FlIOException;
	
	/**
	 * Probes next double value in next 32 bits (input only)
	 * @return The double value
	 * @exception FlIOException if an I/O error occurs
	 */
    double nextdouble() throws FlIOException;
	 
	/**
	 * Puts 'n' bits (output only)
	 * @param bits The value to put
	 * @param n The number of bits to put
	 * @return The value put into the bitstream
	 * @exception FlIOException if an I/O error occurs
	 */
	int putbits(int bits, int n) throws FlIOException;
    
	/**
	 * Puts float value into next 32 bits (input only)
	 * @param f The float value to put
	 * @return The value put into the bit stream
	 * @exception FlIOException if an I/O error occurs
	 */
	float putfloat(float f) throws FlIOException;
    
	/**
	 * Puts double value into next 32 bits (output only)
	 * @param d The double value to put
	 * @return value put into the bit stream
	 * @exception FlIOException if an I/O error occurs
	 */
    double putdouble(double d) throws FlIOException;
    

	/* DH 9/12/2001 ++ */

	/*****************/
	/* Little endian */
	/*****************/

    /**
	 * Gets next 'n' bits as <b>unsigned</b> value using the little-endian method (input only)
	 * @param n The number of bits to get
	 * @return The value of next n bits
	 * @exception FlIOException if an I/O error occurs
	 */
 	int little_getbits(int n) throws FlIOException;

	/**
	 * Gets next 'n' bits as <b>signed</b> value using the little-endian method (input only)
	 * @param n The number of bits to get
	 * @return The value of next n bits
	 * @exception FlIOException if an I/O error occurs
	 */
    int little_sgetbits(int n) throws FlIOException;

	/**
	 * Probes 'n' bits as <b>unsigned</b> value using the little-endian method (input only)
	 * @param n The number of bits to get
	 * @return The value of next n bits
	 * @exception FlIOException if an I/O error occurs
	 */
    int little_nextbits(int n) throws FlIOException;

	/**
	 * Probes 'n' bits as <b>signed</b> value using the little-endian method (input only)
	 * @param n The number of bits to get
	 * @return The value of next n bits
	 * @exception FlIOException if an I/O error occurs
	 */
	int little_snextbits(int n) throws FlIOException;
 
    /**
	 * Gets next float value in next 32 bits using the little-endian method (input only)
	 * @return The float value
	 * @exception FlIOException if an I/O error occurs
	 */
    float little_getfloat() throws FlIOException;;
 
	/**
	 * Probes next float value in next 32 bits using the little-endian method (input only)
	 * @return The float value
	 * @exception FlIOException if an I/O error occurs
	 */
    float little_nextfloat() throws FlIOException;
   
    /**
	 * Gets next double value in next 32 bits using the little-endian method (output only)
	 * @return The double value
	 * @exception FlIOException if an I/O error occurs
	 */
    double little_getdouble() throws FlIOException;
 
	/**
	 * Probes next double value in next 32 bits using the little-endian method (input only)
	 * @return The double value
	 * @exception FlIOException if an I/O error occurs
	 */
    double little_nextdouble() throws FlIOException;
	 
	/**
	 * Puts 'n' bits using the little-endian method (output only)
	 * @param bits The value to put
	 * @param n The number of bits to put
	 * @return The value put into the bitstream
	 * @exception FlIOException if an I/O error occurs
	 */
	int little_putbits(int bits, int n) throws FlIOException;

	/**
	 * Puts float value into next 32 bits using the little-endian method (input only)
	 * @param f The float value to put
	 * @return The value put into the bit stream
	 * @exception FlIOException if an I/O error occurs
	 */
	float little_putfloat(float f) throws FlIOException;
    
	/**
	 * Puts double value into next 32 bits using the little-endian method (output only)
	 * @param d The double value to put
	 * @return value put into the bit stream
	 * @exception FlIOException if an I/O error occurs
	 */
    double little_putdouble(double d) throws FlIOException;
    
	/* DH 9/12/2001 -- */


	/**
	 * Skips next 'n' bits (both input/output)
	 * @param n The number of bits to skip
	 * @exception FlIOException if an I/O error occurs
	 */
    void skipbits(int n) throws FlIOException;

    /**
	 * Aligns the bitstream to 8 bit boundary (n must be a multiple of 8, both input/output)
	 * @param n The number of bits to align
	 * @return The number of bits skipped
	 * @exception FlIOException if an I/O error occurs or invalid alignment requested
	 */
	int align(int n) throws FlIOException;

     /**
	 * Gets current position (both input/output)
	 * @return The current bit position
	 */
    int getpos();
	
	/**
	 * Test end-of-data
	 * @return True when bitsream reaches end-of-data, false otherwise
	 */
	boolean atend();
	
	/**
	 * Returns mode(BS_INPUT or BS_OUTPUT)
	 * @return BS_INPUT or BS_OUPUT
	 */
	public int getmode();
}
