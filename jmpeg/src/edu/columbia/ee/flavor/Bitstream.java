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
 * Implementation of class to access a bitstream.
 */
public class Bitstream implements IBitstream {
	
	public static final int BUF_LEN = 1024;			// Default buffer size
    public static long	LONG_SIGN = (long)1 << 31;  // Constants for double operations
	public static int MAX_SIZE_OF_BITS = 32;
	
	public static final int[] mask = {			    // Masks for bitstream manipulation
        0x00000000, 0x00000001, 0x00000003, 0x00000007,
        0x0000000f, 0x0000001f, 0x0000003f, 0x0000007f,
        0x000000ff, 0x000001ff, 0x000003ff, 0x000007ff,
        0x00000fff, 0x00001fff, 0x00003fff, 0x00007fff,
        0x0000ffff, 0x0001ffff, 0x0003ffff, 0x0007ffff,
        0x000fffff, 0x001fffff, 0x003fffff, 0x007fffff,
        0x00ffffff, 0x01ffffff, 0x03ffffff, 0x07ffffff,
        0x0fffffff, 0x1fffffff, 0x3fffffff, 0x7fffffff,
        0xffffffff
    };

	// Complement masks (used for sign extension)
	public static final int[] cmask = {
	    0xffffffff, 0xfffffffe, 0xfffffffc, 0xfffffff8,
	    0xfffffff0, 0xffffffe0, 0xffffffc0, 0xffffff80,
	    0xffffff00, 0xfffffe00, 0xfffffc00, 0xfffff800,
	    0xfffff000, 0xffffe000, 0xffffc000, 0xffff8000,
	    0xffff0000, 0xfffe0000, 0xfffc0000, 0xfff80000,
	    0xfff00000, 0xffe00000, 0xffc00000, 0xff800000,
	    0xff000000, 0xfe000000, 0xfc000000, 0xf8000000,
	    0xf0000000, 0xe0000000, 0xc0000000, 0x80000000,
	    0x00000000
	};

	// Sign masks (used for sign extension)
	public static final int[] smask = {
	    0x00000000, 0x00000001, 0x00000002, 0x00000004,
	    0x00000008, 0x00000010, 0x00000020, 0x00000040,
	    0x00000080, 0x00000100, 0x00000200, 0x00000400,
	    0x00000800, 0x00001000, 0x00002000, 0x00004000,
	    0x00008000, 0x00010000, 0x00020000, 0x00040000,
	    0x00080000, 0x00100000, 0x00200000, 0x00400000,
	    0x00800000, 0x01000000, 0x02000000, 0x04000000,
	    0x08000000, 0x10000000, 0x20000000, 0x40000000,
	    0x80000000
	};

	protected boolean close_fd = false;	// True if file needs to be closed
    protected int buf_len = BUF_LEN;	// Usable buffer size (for partially filled buffers
    protected byte[] buf;				// Input buffer
    protected int cur_bit;				// Current bit position in buffer
    protected int total_bits;			// Total bits read/written
    protected boolean eof = false;		// EOF of data flag
	protected InputStream in = null;	// Input file handle
    protected OutputStream out = null;	// Output file handle
    protected int type;					// Input or Output


	/**
	 * Constructs input bitstream.
	 * @param input 
	 * @author Jonathan Shneier
	 * @throws FlIOException
	 */
	public Bitstream(InputStream input) throws FlIOException 
	{
		this(input, BUF_LEN);
	}
	
	/**
	 * Constructs output bitstream.
	 * @param output
	 * @author Jonathan Shneier
	 * @throws FlIOException
	 */
	public Bitstream(OutputStream output) throws FlIOException 
	{
		this(output, BUF_LEN);
	}

	/**
	 * Constructs input bitstream with the given buffer length.
	 * @param input
	 * @param _buf_len
	 * @author Jonathan Shneier
	 * @throws FlIOException
	 */
	public Bitstream(InputStream input, int _buf_len) throws FlIOException 
	{
		cur_bit = 0;
		total_bits = 0;
		type = BS_INPUT;
		buf_len = _buf_len;
		buf = new byte[buf_len];

		try {
			in = input;
			cur_bit = BUF_LEN << 3;	// Fake we are at the eof of buffer
			fill_buf();
		} catch(Exception e) {
			e.printStackTrace();
			throw new FlIOException(FlIOException.FILEOPENFAILED,e.toString());
		}

		close_fd = false;
	}

	/**
	 * Constructs an output bitstream with the given buffer length.
	 * @param output
	 * @param _buf_len
	 * @author Jonathan Shneier
	 * @throws FlIOException
	 */
	public Bitstream(OutputStream output, int _buf_len) throws FlIOException 
	{
		cur_bit = 0;
		total_bits = 0;
		type = BS_OUTPUT;
		buf_len = _buf_len;
		buf = new byte[buf_len];
		
		try {
			out = new BufferedOutputStream(output);
		} catch(Exception e) {
			throw new FlIOException(FlIOException.FILEOPENFAILED,e.toString());
		}

		close_fd = false;
	}
	
	/**
	 * Constructs either an output or an input stream, depending on the type
	 * @param filename file name to open
	 * @param _type I/O type( BS_INPUT or BS_OUTPUT )
	 * @throws FlIOException
	 */
	public Bitstream(String filename, int _type) throws FlIOException 
	{
    	this(filename, _type, BUF_LEN);
	}
	
    /**
	 * Constructs either an output or an input stream, depending
	 * on the type, with the given buffer length.
	 * @param filename file name to open
	 * @param _type I/O type( BS_INPUT or BS_OUTPUT )
	 * @param _buf_len buffer size
	 * @throws FlIOException
     */
	public Bitstream(String filename, int _type, int _buf_len) throws FlIOException 
	{
		cur_bit = 0;
		total_bits = 0;
		type = _type;
		buf_len = _buf_len;
		buf = new byte[buf_len];
		
		switch (type) {
			case BS_INPUT:
				try {
					in = new BufferedInputStream(new FileInputStream(filename));
 					cur_bit = BUF_LEN << 3;	// Fake we are at the eof of buffer
					fill_buf();
				} catch(IOException e) {
					throw new FlIOException(FlIOException.FILEOPENFAILED,e.toString());
				}
				break;
			case BS_OUTPUT:
				try {
					out = new BufferedOutputStream(new FileOutputStream(filename));
				} catch(IOException e) {
					throw new FlIOException(FlIOException.FILEOPENFAILED,e.toString());
				}
				break;
			default:
				throw new FlIOException(FlIOException.INVALIDIOTYPE);
		}
		close_fd = true;
	}
    
	/**
	 *	close opened files
	 * @see Object#finalize()
	 */
    protected void finalize() throws Throwable  {
        super.finalize();
        close();
    }
    
	/**
	 *	close file
	 * @throws FlIOException
	 */
    public void close() throws FlIOException {
        try  {
		if (type==BS_OUTPUT) {
            flushbits();
            if (close_fd) {
				out.close();
                close_fd = false;
            }
        }
        else {
            if (close_fd) {
            	in.close();
                close_fd = false;
            }
        }
        } catch(IOException e) {
        	throw new FlIOException(FlIOException.SYSTEMIOFAILED,e.toString());
		}
        
    }
    
	/**
	 *	Check eof-of-file flag
	 * @return <code>true</code> if at end of file
	 */
    public boolean atend() {
        return eof;
    }
    
	/**
	 *	Current bit position
	 * @return the count of bits so far
	 */
	public int getpos() { 
        return total_bits; 
    }
    
	/**************/
	/* Big endian */
	/**************/

	/**
	 *	Get next n bits
	 *	@param n number of bits to be fetched
	 *	@return the value of the fetched bits
	 *	@exception FlIOException
	 *				when n > 32
	 */
	public int getbits(int n) throws FlIOException 
	{
		int x = nextbits(n);
        cur_bit += n;
        total_bits += n;
        return x;
	}
	
	/**
	 *	Get next n bits
	 * 	Java does not support unsigned type 
	 *	Remains only for compatibility with C++ language
	 *	Blindly calls sgetbits
	 *	@see #sgetbits(int n) throws FlIOException
	 */
    public int sgetbits(int n) throws FlIOException {
        int x = snextbits(n);
        cur_bit += n;
        total_bits += n;
        return x;
    }
   
    /**
	 *	Peek next n bits
	 *	@param n number of bits to peek
	 *	@return the value of the peeked bits
	 *	@exception FlIOException
	 *				when n > 32
	 *				eof-of-file
	 *				not enough data
	 */
    public int nextbits(int n) throws FlIOException {
        
		if (n > MAX_SIZE_OF_BITS || n < 1) throw new FlIOException(FlIOException.INVALIDBITSIZE);
		if (cur_bit + n > buf_len << 3) fill_buf();
        if ((buf_len << 3) - cur_bit < n) throw new FlIOException(FlIOException.NOTENOUGHDATA);
		
		int x = 0;							// Output value		
		int j = cur_bit >>> 3;				// Current byte position
		int end = (cur_bit + n - 1) >>> 3;	// End byte position
		int room = 8 - (cur_bit % 8);		// Room in the first byte
		
		if (room >= n) {
			x = (buf[j] >> room - n)&mask[n];
			return x;	
		}

		int leftover = (cur_bit + n) % 8;	// Leftover bits in the last byte

		x |= buf[j]&mask[room];				// Fill out first byte
		
		for (j++; j<end; j++) {
			x <<= 8;						// Shift and
			x |= buf[j]&mask[8];			// Put next byte
		}
		
						
		if (leftover > 0)  {
			x <<= leftover;					// Make room for remaining bits
			x |= (buf[j] >> (8-leftover))&mask[leftover];	// And put
		}
		else {
			x <<= 8;						// Shift
			x |= buf[j]&mask[8];			// Put next byte
		}
		return x;	
    }
    
	/**
	 *	Peek next n bits - signed value
	 *	@param n number of bits to peek
	 *	@return the signed value of the peeked bits
	 *	@exception FlIOException
	 *				when n > 32
	 *				eof-of-file
	 *				not enough data
	 */
    public int snextbits(int n) throws FlIOException {
    	int x = nextbits(n);
		if (n>1 && ((smask[n]&x) != 0)) 
			return x|cmask[n];
		else
			return x;	
	}
	
    /**
	 *	Get float value(32 bits) from the bistream
	 *	@return float value
	 *  @exception FlIOException
	 */
	public float getfloat() throws FlIOException {
        float f = nextfloat();
        skipbits(32);
		return f;
    }
        
    /**
	 *	Peak float value(32 bits) from the bistream
	 *	@return float value
	 *  @exception FlIOException
	 */
    public float nextfloat() throws FlIOException {
        return Float.intBitsToFloat(nextbits(32));
    }

    /**
	 *	Get double value(64 bits) from bistream
	 *  @return double value
	 *  @exception FlIOException
	 */
    public double getdouble() throws FlIOException {
        double d = nextdouble();
        skipbits(64);
		return d;
    }

    /**
	 *	Get next double value from bistream
	 *	@return double value 
	 * @throws FlIOException
     */
    public double nextdouble() throws FlIOException {
        if (cur_bit + 64 > (buf_len << 3))
    	    fill_buf();

        long l = sgetbits(32);
		l <<= 32;
		
		int i = sgetbits(32);

		int j = 0;
		if (i < 0)
			j = i & mask[31];
			
		l |= j;
		if (i < 0 )
			l |= LONG_SIGN;

		cur_bit -= 64;
		total_bits -= 64;
        return Double.longBitsToDouble(l);
    }
  
 	/**
	 *	Put n bits
	 *	@param y value to put
	 *	@param n number of bits to put ( <= 32 bits)
	 *  @return <code>y</code>
 	 * @exception FlIOException
	 */
    public int putbits(int y, int n) throws FlIOException {
        
		int x = y;

		// Sanity check
        if(n > MAX_SIZE_OF_BITS || n < 1) throw new FlIOException(FlIOException.INVALIDBITSIZE);
        
		// Make sure we have enough room
		if ((cur_bit + n) > (buf_len << 3)) {
            flush_buf();
		}
        
		int j = (cur_bit + n - 1) >>> 3;	// End byte position
		int begin = cur_bit >>> 3;			// Current byte position
		
		int room = 8 - (cur_bit % 8);		// Room in the first byte of the buffer
		if (room >= n) {					// Enough room
			buf[begin] &= cmask[room];
			buf[begin] |= mask[room] & (x << room - n);
			cur_bit += n;
        	total_bits += n;
			return y;
		}
		
		int remain = (n-room)%8;			// Number of bits to put in the last byte
		
		if (remain > 0)  {
			buf[j] = 0;
			buf[j] |= (x << 8-remain)&mask[8];	// Put the bits in the head of byte
			x >>= remain;					// And eat up
			j--;
		}
					
		for (; j>begin; j--) {
			buf[j] = 0;
			buf[j] |= x&mask[8];			// Put next byte
			x >>>= 8;						// Shift 
		}
		
		buf[j] &= cmask[room];
		buf[j] |= x&mask[room];
										
	    cur_bit += n;
        total_bits += n;
        return y;
    }
	
    /**
	 *	Put float value(32 bits) into bistream
	 *	@param	f float value
	 *  @return float value
	 *  @exception FlIOException
	 */
    public float putfloat(float f) throws FlIOException {
		putbits(Float.floatToIntBits(f),32);
		return f;
    }
    
    /**
	 *	Put a double value into bitstream
	 *	@param d double variable
	 *	@return the double value 
	 * @throws FlIOException if putbits() fails 
     */
    public double putdouble(double d) throws FlIOException {
        long l = Double.doubleToLongBits(d);
        int i = (int)(l >>> 32);
		putbits(i, 32);	
		i = (int)(l & 0x00000000FFFFFFFF);
        putbits(i, 32);
        return d;
    }
    
	/* DH 9/12/2001 ++ */

	/*****************/
	/* Little endian */
	/*****************/

    /**
     * Gets bits in little order.
     * @param n bits to get
     * @return value
     * @throws FlIOException
     */
	public int little_getbits(int n) throws FlIOException 
	{
		int x = little_nextbits(n);
        cur_bit += n;
        total_bits += n;
        return x;
	}
	
    public int little_sgetbits(int n) throws FlIOException {
        int x = little_snextbits(n);
        cur_bit += n;
        total_bits += n;
        return x;
    }
   
    public int little_nextbits(int n) throws FlIOException {
        
		if (n > MAX_SIZE_OF_BITS || n < 1) throw new FlIOException(FlIOException.INVALIDBITSIZE);
		
		int x = 0;							// Output value

        int bytes = n >>> 3;                // Number of bytes to read +
        int leftbits = n%8;                 // Number of bits to read

        int byte_x = 0;
        int i = 0;
        for (; i < bytes; i++) {
            byte_x = nextbits(8);
            cur_bit += 8;
            byte_x <<= (8*i);
            x |= byte_x;
        }

        if (leftbits > 0) {
            byte_x = nextbits(leftbits);
            byte_x <<= (8*i);
            x |= byte_x;
        }
        cur_bit -= i*8;

		return x;	
    }
    
    public int little_snextbits(int n) throws FlIOException {
    	int x = little_nextbits(n);
		if (n>1 && ((smask[n]&x) != 0)) 
			return x|cmask[n];
		else
			return x;	
	}
	
	public float little_getfloat() throws FlIOException {
        float f = little_nextfloat();
        skipbits(32);
		return f;
    }
        
    public float little_nextfloat() throws FlIOException {
        return Float.intBitsToFloat(little_nextbits(32));
    }

    public double little_getdouble() throws FlIOException {
        double d = little_nextdouble();
        skipbits(64);
		return d;
    }

    public double little_nextdouble() throws FlIOException {
        if (cur_bit + 64 > (buf_len << 3))
    	    fill_buf();

        int i = little_sgetbits(32);

        long l = little_sgetbits(32);
		l <<= 32;
		
		int j = 0;
		if (i < 0)
			j = i & mask[31];
			
		l |= j;
		if (i < 0 )
			l |= LONG_SIGN;
			
		cur_bit -= 64;
		total_bits -= 64;
        return Double.longBitsToDouble(l);
    }
  
    public int little_putbits(int y, int n) throws FlIOException {
        
		int x = y;

		// Sanity check
        if(n > MAX_SIZE_OF_BITS || n < 1) throw new FlIOException(FlIOException.INVALIDBITSIZE);
        
        int bytes = n >>> 3;                // Number of bytes to write +
        int leftbits = n%8;                 // NUmber of bits to write

        int byte_x = 0;
        int i = 0;
        for (; i < bytes; i++) {
            byte_x = (x >> (8*i)) & mask[8];
            putbits(byte_x, 8);
        }

        if (leftbits > 0) {
            byte_x = (x >> (8*i)) & mask[leftbits];
            putbits(byte_x, leftbits);
        }
       
         return y;
    }
	
    public float little_putfloat(float f) throws FlIOException {
		putbits(Float.floatToIntBits(f),32);
		return f;
    }
    
    public double little_putdouble(double d) throws FlIOException {
        long l = Double.doubleToLongBits(d);
		int i = (int)(l & 0x00000000FFFFFFFF);
        little_putbits(i, 32);
        i = (int)(l >>> 32);
		little_putbits(i, 32);	
        return d;
    }

 	/* DH 9/12/2001 -- */


	/**
	 *	Skip n bits
	 *	@param n number of bits to skip
	 *	@exception FlIOException
	 */
    public void skipbits (int n) throws FlIOException 
	{
		int x = n;
		int buf_size = buf_len << 3;
		
		while (cur_bit + x > buf_size) 
		{
			x -= buf_size - cur_bit;
			cur_bit = buf_size;
			if (type == BS_INPUT) 
				fill_buf();
			else
				flush_buf();
		}
		
		cur_bit += x;
		total_bits += n;
	}
    
    /**
	 *	Align bitstream
	 *	@param n number of bits to align on
	 *	@return	the number of skipped bits
	 * @throws FlIOException
     */
    public int align(int n) throws FlIOException 
	{

		if ((n % 8) != 0) 	// We only allow alignment on multiples of bytes
            throw new FlIOException(FlIOException.INVALIDALIGNMENT);        		
        int s = 0;        
        
		// align on next byte boundary
        if ((total_bits % 8) != 0) 
		{
            s = (total_bits % 8);
			skipbits (8-s);
        }
        
		// skip bits until total_bits are aligned on n
        while ((total_bits % n) != 0) 
		{
            s += 8;
			skipbits(8);
        }

		return s;
    }
    
   /**
	 *	Flush all content in the buffer
	 * @throws FlIOException
 */
	public void flushbits() throws FlIOException {
        flush_buf();
        if(cur_bit == 0) return;
        try {
			out.write(buf, 0, 1);
        } catch(IOException e)  {
        	throw new FlIOException(FlIOException.SYSTEMIOFAILED,e.toString());
		}
        buf[0] = 0;
        cur_bit = 0;		// Now only the left-over bits
    }
    
	/**
	 *	Return current bit position
	 *	@return	current bit position
	 */
	public int getCurrentBit() 
	{
		return cur_bit; 
	}
    
    /**
	 *	Fill out internal buffer from the file
	 *	@exception	FlIOException
	 *				System I/O failed or End of File
	 */
    private void fill_buf() throws FlIOException {
        int n, u, l;
                
        n = cur_bit >>> 3;	// Current byte offset
        u = buf_len - n;	// Remaining bytes

		System.arraycopy(buf, n, buf, 0, u);	// Copy remaining data into the head of the buffer
		       
        try {
			// Now we have a room for n bytes from offset u in the buffer
			l = in.read(buf, u, n);
		} catch(IOException e) {
			throw new FlIOException(FlIOException.SYSTEMIOFAILED, e.toString());
		}
				
		if (l == -1) {	// EOF?
			eof = true;
			throw new FlIOException(FlIOException.ENDOFDATA);
		}
		if (l < n) buf_len = u + l;	            // Adjust buffer size

	    cur_bit &= 7;						    // Now we are at the first byte
    }
    
    /**
	 *	Flush the buffer excluding the left-over bits
	 *	@exception	FlIOException
	 *				System I/O failed or End of File
	 */
    private void flush_buf() throws FlIOException {
        int l = cur_bit >>> 3;		// Size of data in the buffer
        
		try {
			out.write(buf, 0, l);	// File output
        } catch(IOException e)  {
        	throw new FlIOException(FlIOException.SYSTEMIOFAILED,e.toString());
		}
        
        cur_bit &= 0x7;	// Keep left-over bits only
		// Are there any left-over bits?
        if (cur_bit != 0) buf[0] = buf[l];
    }

	private static void integerTest() {
		int val = 0;
		int number_of_loop = 1000;
		try {
			System.out.println("Writing...");
			Bitstream output = new Bitstream("test.bs",BS_OUTPUT);
			for (int j=0; j<number_of_loop; j++)
				for (int i=1; i<=32; i++) {
					val = i==2?1:i;
					output.putbits(val,i);
					val = i==1?0:-i;
					output.putbits(val,i);
					
				}
			output.close();
			
			System.out.println("Reading...");
			Bitstream input = new Bitstream("test.bs",BS_INPUT);
			for (int j=0; j<number_of_loop; j++)  {
				for (int i=1; i<=32; i++) {
					val = input.getbits(i);
					if (val != (i==2?1:i))  {
						System.out.println("ERROR");
						System.exit(-1);
					}
					val = input.sgetbits(i);
					if (val != (i==1 ? 0 : -i))  {
						System.out.println("ERROR 2! " + j + i + val);
						System.exit(-1);
					}
				}
			}
			input.close();
		} catch( Exception e)  {
			System.out.println(e.toString());
		}
	}
	
	public static void main(String args[]) {
		
		float cf = (float)-1823000.3123, f=0;
		double cd = 22123.233, d=0;
		Bitstream.integerTest();
		
		
		try {
			Bitstream output = new Bitstream("test.bs",BS_OUTPUT);
			output.putdouble(cd);
			output.putfloat(cf);
			output.close();
			
			Bitstream input = new Bitstream("test.bs",BS_INPUT);
			d = input.getdouble();
			f = input.getfloat();
			input.close();
			System.out.println("d: " + d);
			System.out.println("f: " + f);
	
		} catch( Exception e)  {
			System.out.println(e.toString());
		}
	}
	
	/**
	 *	Return mode(BS_INPUT or BS_OUTPUT)
	 *  @return BS_INPUT or BS_OUPUT
	 */
	public int getmode()
	{
		return type;
	}
}
