package edu.union.graphics;

import java.io.IOException;
import java.io.InputStream;

/**
 * An InputStream which wraps an InputStream and can read numbers stored in LittleEndian order
 * @author bburns
 */
public class LittleEndianInputStream {
	InputStream is;

	/**
	 * Constructor
	 * @param is The InputStream to wrap.
	 */
	public LittleEndianInputStream(InputStream is) {
		this.is = is;
	}

	/**
	 * Read an int
	 * @return The int that is read in.
	 * @throws IOException If the underlying InputStream throws an exception
	 */
	public int readInt() 
	throws IOException
	{
		int b1 = is.read();
		int b2 = is.read();
		int b3 = is.read();
		int b4 = is.read();

		return (b4 << 24) 
		+ ((b3 << 24) >>> 8) 
		+ ((b2 << 24) >>> 16) 
		+ ((b1 << 24) >>> 24);
	}

	/**
	 * Read an unsigned short
	 * @return The short value that is read in.
	 * @throws IOException If the underlying InputStream throws an exception
	 */
	public short readUnsignedShort() 
	throws IOException 
	{
		int b1 = is.read();
		int b2 = is.read();
		if (b1 < 0)
			b1 += 256;
		if (b2 < 0)
			b2 += 256;

		return (short)(b2*256+b1);
	}

	/**
	 * Read a signed short
	 * @return The short value that is read in.
	 * @throws IOException If the underlying InputStream throws an exception
	 */
	public short readShort()
	throws IOException
	{
		int b1 = is.read();
		int b2 = is.read();
		if (b1 < 0)
			b1 += 256;

		return (short)(b2*256+b1);
	}

	/**
	 * Read an unsigned char/byte
	 * @return The char/byte value that is read in.
	 * @throws IOException If the underlying InputStream throws an exception
	 */
	public char readUnsignedChar()
	throws IOException
	{
		int b = is.read();
		if (b < 0)
			b+=256;
		return (char)b;
	}

	/**
	 * Read a float value
	 * @return The float value that is read in.
	 * @throws IOException If the underlying InputStream throws an exception
	 */
	public final float readFloat() throws IOException {
		return Float.intBitsToFloat(this.readInt());
	}

	/**
	 * Read in an array of bytes.  Simply wraps the call to the underlying InputStream.
	 * @param buff The buffer to read data into
	 * @return The number of bytes read.
	 * @throws IOException If the underlying InputStream throws an exception
	 */
	public int read(byte[] buff) 
	throws IOException
	{
		return is.read(buff);
	}

	/**
	 * Read in a String from the InputStream
	 * @param length The maximum length of the String to read.
	 * @return The String which is read in.
	 * @throws IOException If the underlying InputStream throws an exception
	 */
	public String readString(int length) 
	throws IOException
	{
		byte[] buff = new byte[length];
		is.read(buff);
		return new String(buff);
	}
}