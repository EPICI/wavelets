package util;

/**
 * Utility class containing all bit twiddling hacks and the like
 * <br>
 * Integers in Java are two's complement with big endian, and
 * floating point follows IEEE format
 * <br>
 * In general, if it isn't provided here, the standard library
 * methods are already optimized
 * 
 * @author EPICI
 * @version 1.0
 */
public final class Bits {
	
	/**
	 * An int that reads 100000... in binary
	 */
	public static final int INT_LEFT_ONE = 1<<31;
	/**
	 * A long that reads 100000... in binary
	 */
	public static final long LONG_LEFT_ONE = 1L<<63;
	
	//Disallow invoking constructor
	private Bits(){}
	
	/**
	 * Sets a bit in an int value
	 * 
	 * @param encoded the original int value
	 * @param index the position of the bit to change, from 0 to 31 inclusive,
	 * 0 would be the sign bit
	 * @param bit the bit to write
	 * @return the modified int
	 */
	public static int writeBit(int encoded,int index,boolean bit){
		int bits = INT_LEFT_ONE >>> index;
		if(bit){
			return encoded | bits;
		}else{
			return encoded & (~bits);
		}
	}
	
	/**
	 * Reads a bit from an int value
	 * 
	 * @param encoded the int value to read from
	 * @param index the position of the bit to read, from 0 to 31 inclusive,
	 * 0 would be the sign bit
	 * @return true if the bit is 1, false if the bit is 0
	 */
	public static boolean readBit(int encoded,int index){
		return (encoded << index) < 0;
	}
	
	/**
	 * Sets a bit in a long value
	 * 
	 * @param encoded the original long value
	 * @param index the position of the bit to change, from 0 to 63 inclusive,
	 * 0 would be the sign bit
	 * @param bit the bit to write
	 * @return the modified long
	 */
	public static long writeBit(long encoded,int index,boolean bit){
		long bits = LONG_LEFT_ONE >>> index;
		if(bit){
			return encoded | bits;
		}else{
			return encoded & (~bits);
		}
	}
	
	/**
	 * Reads a bit from an long value
	 * 
	 * @param encoded the long value to read from
	 * @param index the position of the bit to read, from 0 to 63 inclusive,
	 * 0 would be the sign bit
	 * @return true if the bit is 1, false if the bit is 0
	 */
	public static boolean readBit(long encoded,int index){
		return (encoded << index) < 0;
	}
	
	/**
	 * Gets an int with a 1 at the specified position and
	 * the rest 0s
	 * 
	 * @param index the position, from 0 to 31 inclusive, 0 is sign bit
	 * @return said int value
	 */
	public static int oneAtInt(int index){
		return INT_LEFT_ONE >>> index;
	}
	
	/**
	 * Gets an long with a 1 at the specified position and
	 * the rest 0s
	 * 
	 * @param index the position, from 0 to 63 inclusive, 0 is sign bit
	 * @return said long value
	 */
	public static long oneAtLong(int index){
		return LONG_LEFT_ONE >>> index;
	}
	
	/**
	 * Fast (floor) log base 2 for integers
	 * <br>
	 * Taken from http://stackoverflow.com/questions/3305059/how-do-you-calculate-log-base-2-in-java-for-integers#3305710
	 * <br>
	 * Returns 0 for 0
	 * <br>
	 * If the original number was a power of 2, 1&lt;&lt;(the returned result) should be original number
	 * 
	 * @param bits the integer to calculate the log base 2 of
	 * @return the log base 2 of that integer
	 */
	public static int binLog(int bits){
		int log = 0;
		if( ( bits & 0xffff0000 ) != 0 ) { bits >>>= 16; log = 16; }
		if( bits >= 256 ) { bits >>>= 8; log += 8; }
		if( bits >= 16  ) { bits >>>= 4; log += 4; }
		if( bits >= 4   ) { bits >>>= 2; log += 2; }
		return log + ( bits >>> 1 );
	}
	
	/**
	 * Fast power of 2 test
	 * <br>
	 * Also returns true for 0, even though it isn't a power of 2
	 * 
	 * @param bits the number to test
	 * @return true if the number is a power of 2
	 */
	public static boolean isPo2(int bits){
		return (bits&(bits-1))==0;
	}
	
	/**
	 * Makes all bits to the right of the most significant bits 1
	 * <br>
	 * For any negative number, returns -1
	 * 
	 * @param bits the original number
	 * @return the modified number
	 */
	public static int fill(int bits){
		bits |= bits>>1;
		bits |= bits>>2;
		bits |= bits>>4;
		bits |= bits>>8;
		bits |= bits>>16;
		return bits;
	}
	
	/**
	 * Return the smallest power of 2 which is greater than
	 * the given number
	 * <br>
	 * No error checking
	 * 
	 * @param bits the number to find the next power of 2 for
	 * @return the next power of 2, or 0
	 */
	public static int gtPo2(int bits){
		return fill(bits)+1;
	}
	
	/**
	 * Returns the smallest power of 2 which is greater than
	 * or equals to the given number
	 * <br>
	 * No error checking
	 * 
	 * @param bits the number to find the next power of 2 for
	 * @return the next power of 2, or 0
	 */
	public static int gePo2(int bits){
		return fill(bits-1)+1;
	}
	
	/**
	 * Absolute value without branching
	 * <br>
	 * From https://graphics.stanford.edu/~seander/bithacks.html
	 * 
	 * @param bits some number
	 * @return the absolute value
	 */
	public static int abs(int bits){
		int mask = bits>>31;
		return (bits+mask)^mask;
	}
	
	/**
	 * Absolute value without branching
	 * <br>
	 * From https://graphics.stanford.edu/~seander/bithacks.html
	 * 
	 * @param bits some number
	 * @return the absolute value
	 */
	public static long abs(long bits){
		long mask = bits>>63;
		return (bits+mask)^mask;
	}
	
	/**
	 * Main method, used only for testing
	 * 
	 * @param args ignored
	 */
	public static void main(String[] args){
		//Log base 2 sanity tests
		System.out.println("--- Binary logarithm ---");
		System.out.println("log2(1024)="+binLog(1024)+" (expected 10)");
		System.out.println("log2(3000)="+binLog(3000)+" (expected 11)");
		System.out.println("log2(14)="+binLog(14)+" (expected 3)");
		System.out.println("log2(73902)="+binLog(73902)+" (expected 16)");
		//Power of 2 sanity tests
		System.out.println("--- Power of 2 test ---");
		System.out.println("ispo2(65535)="+isPo2(65535)+" (expected false)");
		System.out.println("ispo2(65536)="+isPo2(65536)+" (expected true)");
		System.out.println("ispo2(65537)="+isPo2(65537)+" (expected false)");
		System.out.println("ispo2(3)="+isPo2(3)+" (expected false)");
		System.out.println("ispo2(4)="+isPo2(4)+" (expected true)");
		//Next power of 2 sanity tests
		System.out.println("--- Next power of 2 ---");
		System.out.println("gtpo2(2)="+gtPo2(2)+" (expected 4)");
		System.out.println("gtpo2(23)="+gtPo2(23)+" (expected 32)");
		System.out.println("gtpo2(234)="+gtPo2(234)+" (expected 256)");
		System.out.println("gepo2(511)="+gePo2(511)+" (expected 512)");
		System.out.println("gepo2(512)="+gePo2(512)+" (expected 512)");
		System.out.println("gepo2(513)="+gePo2(513)+" (expected 1024)");
		System.out.println("gtpo2(1<<30)="+gtPo2(1<<30)+" (expected -1<<31)");
		//Absolute value sanity tests
		System.out.println("--- Absolute value ---");
		System.out.println("abs(0)="+abs(0));
		System.out.println("abs(1)="+abs(1));
		System.out.println("abs(1777)="+abs(1777));
		System.out.println("abs(-1)="+abs(-1));
		System.out.println("abs(-1777)="+abs(1777));
		System.out.println("abs(12345678987654321)="+abs(12345678987654321L));
		System.out.println("abs(-12345678987654321)="+abs(-12345678987654321L));
	}
}
