package main;

//Utility class
public final class BitUtils {
	//Disallow invoking constructor
	private BitUtils(){}
	
	/*
	 * Fast log base 2 for integers
	 * Taken from http://stackoverflow.com/questions/3305059/how-do-you-calculate-log-base-2-in-java-for-integers#3305710
	 * Returns 0 for 0
	 */
	public static int binLog(int bits){
		int log = 0;
		if( ( bits & 0xffff0000 ) != 0 ) { bits >>>= 16; log = 16; }
		if( bits >= 256 ) { bits >>>= 8; log += 8; }
		if( bits >= 16  ) { bits >>>= 4; log += 4; }
		if( bits >= 4   ) { bits >>>= 2; log += 2; }
		return log + ( bits >>> 1 );
	}
}
